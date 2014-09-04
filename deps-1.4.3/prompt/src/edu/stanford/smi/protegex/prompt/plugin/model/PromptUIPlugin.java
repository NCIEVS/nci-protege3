/*
 * Contributor(s): Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt.plugin.model;

/**
 * Top level interface for a UI plugin.
 * 
 * @author seanf
 * 
 */
public interface PromptUIPlugin extends PromptPlugin {
	/**
	 * Called when the UI component that is being extended has finished loading.
	 */
	public void afterLoad();

	/**
	 * Called when the UI component that is being closed.
	 */
	public void beforeClose();
}
