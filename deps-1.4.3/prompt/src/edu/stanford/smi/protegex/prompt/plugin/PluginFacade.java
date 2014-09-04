/*
 * Contributor(s): Sean Falconer seanf@uvic.ca
 * 	               
 */

package edu.stanford.smi.protegex.prompt.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptUIPerspective;

public class PluginFacade {
	private static final String DEFAULT_VIEW_LABEL = "Default";

	private static final String PLUGIN_CONFIG_FILE = PromptTab.getPromptDirectory() + "/plugin.config";

	/** stores map between UI plugin names and whether they should be visible in the UI or not */
	private static Map<String, Boolean> pluginVisibilityMap = new HashMap<String, Boolean>();

	/** stores map between UI perspectives and the default view to display */
	private static Map<Integer, String> defaultPerspectivesMap = new HashMap<Integer, String>();

	/**
	 * Saves all the plugin visibility information to the prompt plugin config file.
	 */
	public static void savePluginConfig() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(PLUGIN_CONFIG_FILE, false));

			for (String pluginName : pluginVisibilityMap.keySet()) {
				out.write(pluginName + "=" + isPluginVisible(pluginName) + "\n");
			}
			out.write("$\n");

			for (Integer perspectiveTypeId : defaultPerspectivesMap.keySet()) {
				out.write(perspectiveTypeId + "=" + defaultPerspectivesMap.get(perspectiveTypeId) + "\n");
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the plugin configuration file if it exists
	 * 
	 * @param pluginList The list of plugins
	 */
	public static void readPluginConfig() {
		try {
			if (new File(PLUGIN_CONFIG_FILE).exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(PLUGIN_CONFIG_FILE));

				readPluginVisibility(reader);
				readPerspectiveDefaults(reader);

				reader.close();
			} else {
				defaultInitPluginVisibility(PluginManager.getInstance().getAllUIPlugins());
				defaultInitPerspective();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readPluginVisibility(BufferedReader reader) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null && !line.equals("$")) {
			StringTokenizer tokenizer = new StringTokenizer(line, "=");

			String pluginName = tokenizer.nextToken();
			String pluginVisibility = tokenizer.nextToken();

			pluginVisibilityMap.put(pluginName, new Boolean(pluginVisibility));
		}
	}

	private static void readPerspectiveDefaults(BufferedReader reader) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null && !line.equals("$")) {
			StringTokenizer tokenizer = new StringTokenizer(line, "=");

			String perspectiveName = tokenizer.nextToken();
			String pluginName = tokenizer.nextToken();

			defaultPerspectivesMap.put(new Integer(perspectiveName), pluginName);
		}
	}

	/**
	 * Sets up the default values for the plugin visibility list
	 * 
	 * @param pluginList The list of prompt plugins.
	 */
	private static void defaultInitPluginVisibility(LinkedList<PromptPlugin> pluginList) {
		for (PromptPlugin plugin : pluginList) {
			pluginVisibilityMap.put(plugin.getPluginName(), new Boolean(true));
		}
	}

	/**
	 * Sets up the default values for the supported perspectives. They are all set to "Default",
	 * meaning the built in Prompt look.
	 * 
	 * @param pluginList The list of prompt plugins.
	 */
	private static void defaultInitPerspective() {
		defaultPerspectivesMap.put(PluginManager.PLUGIN_UI_MAP_PERSPECTIVE, DEFAULT_VIEW_LABEL);
	}

	/**
	 * Checks the plugin visibility map to determine if this plugin is visible
	 * 
	 * @param pluginName The unique name for the plugin
	 * @return True if visible, otherwise False
	 */
	public static boolean isPluginVisible(String pluginName) {
		Boolean pluginVisibility = pluginVisibilityMap.get(pluginName);
		if (pluginVisibility != null) {
			return pluginVisibility.booleanValue();
		}

		return false;
	}

	/**
	 * Sets the given plugin's visibility based on the visible flag
	 * 
	 * @param pluginName The name of the plugin name
	 * @param visible The value for the visibility of this plugin
	 */
	public static void setPluginVisibility(String pluginName, boolean visible) {
		pluginVisibilityMap.put(pluginName, new Boolean(visible));
	}

	/**
	 * For the given perspective type, the default plugin name is returned.
	 * 
	 * @param pluginType
	 * @return
	 */
	public static PromptUIPerspective getDefaultPerspective(int pluginType) {
		PromptPlugin plugin = PluginManager.getInstance().getPlugin(pluginType, defaultPerspectivesMap.get(pluginType));
		if (plugin != null) {
			return (PromptUIPerspective) plugin;
		}

		return null;
	}

	/**
	 * Sets the new default perspective for the given pluginType.
	 * 
	 * @param pluginType
	 * @param pluginName
	 */
	public static void setDefaultPerspective(int pluginType, String pluginName) {
		defaultPerspectivesMap.put(pluginType, pluginName);
	}
}
