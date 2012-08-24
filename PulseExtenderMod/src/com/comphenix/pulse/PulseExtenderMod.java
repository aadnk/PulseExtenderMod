package com.comphenix.pulse;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PulseExtenderMod extends JavaPlugin {
	
	private static final String COMMAND_PULSE_EXTENDER = "pulseextender";
	private static final String COMMAND_BUTTON_LENGTH = "buttonlength";

	private Logger currentLogger;
	
	// Main listeners
	private OverridenButtonListener buttonListener;
	
	// Clicked button registry
	ClickedButtons buttons;
	
	// Configuration
	private PulseConfiguration configuration;
	
	// The task we're currently running
	private int tickTask;
	
	@Override
	public void onLoad() {
		currentLogger = this.getLogger();
		configuration = new PulseConfiguration(currentLogger);
	}
	
	@Override
	public void onEnable() {
		
		final Server server = getServer();
		final PluginManager manager = server.getPluginManager();
		
		// Reload options
		reloadOptions(false);
		
		// Initialize registry
		buttons = new ClickedButtons();
		
		// Initialize listeners
		buttonListener = new OverridenButtonListener(configuration, buttons);
		manager.registerEvents(buttonListener, this);
		
		// Initialize tick counter
		tickTask = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				buttons.onTick(server);
			}
		}, 1, 1);
		
		// HEY!
		if (tickTask == -1) {
			currentLogger.severe("Unable to register task!");
		}
	}
	
	@Override
	public void onDisable() {
		
		// Cancel task too
		if (tickTask != -1) {
			getServer().getScheduler().cancelTask(tickTask);
			tickTask = -1;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// Handle commands
		if (command.getName().equalsIgnoreCase(COMMAND_BUTTON_LENGTH)) {
			
			try {
				double duration = Double.parseDouble(args[0].trim());
				configuration.setButtonDuration(duration);
				saveConfig();
				
				sender.sendMessage("Updated button duration!");
				
			} catch (Exception e) {
				sender.sendMessage("Cannot parse number.");
			}
			
			return true;
			
		} else if (command.getName().equalsIgnoreCase(COMMAND_PULSE_EXTENDER)) {
			
			sender.sendMessage("Reloaded configuration.");
			
			// Reload everything
			reloadOptions(true);
			return true;
	
		} else {
			return false;
		}
	}
	
	private void reloadOptions(boolean reload) {

		reloadConfig();
		
		if (!reload) {
			// On first run
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
		
		// Update reference to configuration
		configuration.setConfig(getConfig());
		configuration.loadConfig();
	}
}
