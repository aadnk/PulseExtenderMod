package com.comphenix.pulse;

import java.util.Random;

import net.minecraft.server.BlockButton;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

public class Button {

	private static final int pressedBit = 0x8;
	
	// Whether or not to completely ignore CraftBukkit-only methods
	private static boolean disableDirectAccess;
	
	// The underlying block
	private Block block;

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
		
		try {
			
			CraftWorld craftWorld = (CraftWorld) block.getWorld();
			BlockButton notchButton = (BlockButton) net.minecraft.server.Block.STONE_BUTTON;
			
			notchButton.b(craftWorld.getHandle(), block.getX(), block.getY(), block.getZ(), new Random());
			
		} catch (Exception ex) {
			System.err.println("Incompatible CraftBukkit version! " + ex.getMessage());
			disableDirectAccess = true;
		}
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
