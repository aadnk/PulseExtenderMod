package com.comphenix.pulse;

import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.comphenix.pulse.parsing.DoubleParser;
import com.comphenix.pulse.parsing.ParsingException;

public class PulseConfiguration {
	
	public static final String BUTTON_DURATION_SETTING = "button length";
	
	// The number of ticks per second
	public static final int TICKS_PER_SECOND = 20;
	
	// Fields
	private int buttonDuration = 20;

	// The current configuration
	private Configuration config;
	
	// Error logger
	private Logger currentLogger;
	private DoubleParser parser = new DoubleParser();
	
	public PulseConfiguration(Logger currentLogger) {
		this.currentLogger = currentLogger;
	}

	public void loadConfig() {
		
		// Try to initialize parameters
		try {
			ConfigurationSection global = config.getConfigurationSection("global");
			
			setButtonDuration(parser.parse(global, BUTTON_DURATION_SETTING));
			
		} catch (ParsingException e) {
			currentLogger.warning("Config error: " + e.getMessage());
		}
	}
	
	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	/**
	 * Gets the current global button duration in game ticks.
	 * @return Button duration in ticks.
	 */
	public int getButtonDuration() {
		return buttonDuration;
	}

	/**
	 * Sets the current global button duration in game ticks.
	 * @param buttonDuration - new button duration in ticks.
	 */
	public void setButtonDuration(int buttonDuration) {
		ConfigurationSection global = config.getConfigurationSection("global");
		
		global.set(BUTTON_DURATION_SETTING, buttonDuration / (double) TICKS_PER_SECOND);
		this.buttonDuration = buttonDuration;
	}

	/**
	 * Sets the current global button duration in seconds.
	 * @param seconds - new button duration in seconds.
	 */
	public void setButtonDuration(double seconds) {
		setButtonDuration((int) (seconds * TICKS_PER_SECOND));
	}
}
