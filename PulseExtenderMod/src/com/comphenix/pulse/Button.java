/*
 *  PulseExtenderMod - Allows you to customize the duration a button is down in Bukkit.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.pulse;

import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.server.BlockButton;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

public class Button {

	private static final int pressedBit = 0x8;
	
	// Whether or not to completely ignore CraftBukkit-only methods
	private static boolean disableDirectAccess;
	private static boolean useReflection;
	
	// The underlying block
	private Block block;

	// Reflection method to call
	private static Method updateMethod;
	
	public Button(Block block) {
		this.block = block;
	}
	
	public boolean isPressed() {
		return (block.getData() & pressedBit) != 0;
	}
	
	public void setPressed(boolean state, boolean updatePhysics) {
		
		byte data = block.getData();
		
		// Set bit
		if (state)
			data |= pressedBit;
		else
			data &= ~pressedBit;
		
		block.setData(data, updatePhysics);
	}
	
	public void updateTick() {

		// Guard
		if (disableDirectAccess) {
			return;
		}
		
		Random ignored = new Random();
		
		try {
			
			CraftWorld craftWorld = (CraftWorld) block.getWorld();
			WorldServer server = craftWorld.getHandle();
			BlockButton notchButton = (BlockButton) net.minecraft.server.Block.byId[77];

			try {
				// Try to call the method directly
				if (!useReflection) {
					notchButton.b(server, block.getX(), block.getY(), block.getZ(), ignored);
					return;
				}
				
			} catch (NoSuchMethodError e) {
				// Nope, didn't work.
				useReflection = true;
			}
			
			if (updateMethod == null) {
				updateMethod = findObsfucatedMethod(BlockButton.class);
			}
			
			// Call this method
			updateMethod.invoke(notchButton, (World) server, 
					block.getX(), block.getY(), block.getZ(), ignored);
			
			System.out.println("Used reflection!");
			
		} catch (Exception ex) {
			System.err.println("Incompatible CraftBukkit version! " + ex.getMessage());
			ex.printStackTrace();
			disableDirectAccess = true;
		}
	}
	
	private Method findObsfucatedMethod(Class<BlockButton> clazz) {
		
		Class<?>[] expected = new Class<?>[] { World.class, int.class, int.class, int.class, Random.class };
		
		// Find the correct method to call
		for (Method method : clazz.getMethods()) {
			if (ArrayUtils.isEquals(method.getParameterTypes(), expected)) {
				return method;
			}
		}
		
		// Damn
		throw new RuntimeException("Unable to find updateTick-method in BlockButton.");
	}
	
	/**
	 * Retrieves the block this button is standing on.
	 * @return The block the button is standing on.
	 */
	public Block getOffset() {
		
		int data = block.getData() & 0x7;
		
        if (data == 1) {
            return block.getRelative(-1, 0, 0);
        } else if (data == 2) {
            return block.getRelative(1, 0, 0);
        } else if (data == 3) {
            return block.getRelative(0, 0, -1);
        } else if (data == 4) {
            return block.getRelative(0, 0, 1);
        } else {
            return block.getRelative(0, -1, 0);
        }
	}
}
