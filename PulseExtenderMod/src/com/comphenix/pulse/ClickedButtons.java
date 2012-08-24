package com.comphenix.pulse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

public class ClickedButtons {

	// Store every clicked button in a nested dictionary
	protected Map<UUID, Map<Location, Long>> worlds;

	public ClickedButtons() {
		worlds = new HashMap<UUID, Map<Location, Long>>();
	}
	
	/**
	 * Invoked every tick to check for buttons to disable.
	 */
	public void onTick(Server server) {
		
		long currentTime = System.currentTimeMillis();
		
		for (UUID uuid : worlds.keySet()) {
			World world = server.getWorld(uuid);
			
			// Determine if any worlds have been unloaded
			if (world == null) {
				worlds.remove(world);
			} else {
				 Map<Location, Long> clickedButtons = worlds.get(uuid);
				 handleMap(world, clickedButtons, currentTime);
			}
		}
	}
	
	protected void handleMap(World world, Map<Location, Long> clickedButtons, long currentTime) {

		for (Iterator<Entry<Location, Long>> it = clickedButtons.entrySet().iterator(); it.hasNext();) {
			Entry<Location, Long> entry = it.next();

			if (entry.getValue() < currentTime) {
			
				// We don't want future events to disable the redstone update
				it.remove();
	
				// Disable the "button pressed" bit
				Block block = world.getBlockAt(entry.getKey());
				Button button = new Button(block);
				button.updateTick();
			}
		}
	}
	
	/**
	 * Adds a clicked button to the central registry.
	 * @param button - the button to add.
	 * @param disableTime - when the button should be disabled.
	 */
	public void addButton(Block button, long disableTime) {
		Map<Location, Long> clickedButtons = getClickedButtons(button.getWorld(), true);
		clickedButtons.put(button.getLocation(), disableTime);
	}
	
	public boolean hasButton(Block button) {
		Map<Location, Long> clickedButtons = getClickedButtons(button.getWorld(), false);
		return clickedButtons != null && clickedButtons.containsKey(button.getLocation());
	}
	
	protected Map<Location, Long> getClickedButtons(World world, boolean createNew) {
		UUID uuid = world.getUID();

		// Create the dictionary if it hasn't already
		if (!worlds.containsKey(uuid)) {
			if (createNew)
				worlds.put(uuid, createButtonMap());
			else
				return null;
		}
		
		return worlds.get(uuid);
	}
	
	protected Map<Location, Long> createButtonMap() {
		return new HashMap<Location, Long>();
	}
}
