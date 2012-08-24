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

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PulseExtenderMod extends JavaPlugin {
	
	private static final String COMMAND_PULSE_EXTENDER = "pulseextender";
	private static final String COMMAND_BUTTON_LENGTH = "buttonlength";

	public static final String ADMIN_PERMISSION = "pulseextender.admin";
	
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
			
			if (!checkPermission(sender))
				return true;
				
			try {
				double duration = Double.parseDouble(args[0].trim());
				configuration.setButtonDuration(duration);
				saveConfig();
				
				sender.sendMessage("Updated button duration!");

			} catch (Exception e) {
				sender.sendMessage("Cannot parse number.");
			}
			
			return true;
			
		} else if (command.getName().equalsIgnoreCase(COMMAND_PULSE_EXTENDER) && checkPermission(sender)) {
			
			// Reload everything
			if (checkPermission(sender)) {
				reloadOptions(true);
				sender.sendMessage("Reloaded configuration.");
			}
			
			return true;
	
		} else {
			return false;
		}
	}
	
	private boolean checkPermission(CommandSender sender) {
		
		// Make sure the sender has permissions
		if (sender.hasPermission(ADMIN_PERMISSION)) {
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "You haven't got permission to execute this command.");
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
