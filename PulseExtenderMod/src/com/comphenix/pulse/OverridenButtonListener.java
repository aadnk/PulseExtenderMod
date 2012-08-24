package com.comphenix.pulse;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class OverridenButtonListener implements Listener {

	// Current configuration
	private PulseConfiguration configuration;
	private ClickedButtons buttons;

	// Number of milliseconds in a tick
	private static final int MILLISECONDS_PER_TICK = 50;
	
	// Storage of button clicks
	
	public OverridenButtonListener(PulseConfiguration configuration, ClickedButtons buttons) {
		this.configuration = configuration;
		this.buttons = buttons;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		
	    long current = System.currentTimeMillis();
	    long delta = MILLISECONDS_PER_TICK * configuration.getButtonDuration();

	    if (event.getAction() == Action.LEFT_CLICK_BLOCK ||
	    	event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        Block clicked = event.getClickedBlock();
	        Button button = new Button(clicked);
	        
	        // Remember this event
	        if (!button.isPressed() && clicked.getType() == Material.STONE_BUTTON) {
	        	buttons.addButton(clicked, current + delta);
	        }
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
		
		// Don't disable ON state
		if (event.getNewCurrent() > 0) {
			return;
		}
		
		// Is this a clicked button we intend to override?
		if (buttons.hasButton(event.getBlock())) {
			Button button = new Button(event.getBlock());
			
			// Make sure it is still pressed
			button.setPressed(true, false);
			event.setNewCurrent(event.getOldCurrent());
		}
	}
}
