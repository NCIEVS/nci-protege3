 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  * 				  Natasha Noy noy@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.plugin.model;

/**
 * Top level interface for Prompt plugins.
 * @author seanf
 *
 */
public interface PromptPlugin {
	/**
	 * Gets the name of the plugin.
	 * This is used for display and management purposes.
	 * @return The name of the plugin.
	 */
	public String getPluginName();
	
//	/**
//	 * Gets the directory of the plugin.
//	 * relative to the edu.stanford.smi.protegex.prompt/plugins directory
//	 * @return The name of the plugin.
//	 */
//	public String getPluginDirectoryName ();
	
	/**
	 * Called by the PluginManager in order to tell the plugin it has been invoked.
	 */
	public void invokePlugin();

}
