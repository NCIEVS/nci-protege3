 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  */

package edu.stanford.smi.protegex.prompt.plugin;

/**
 * Generic interface used for exposing selection listening functionality from a list in Prompt 
 * to a plugin.
 * @author seanf
 *
 */
public interface SelectionListener {
	/**
	 * Method is called when a selection event happens within 
	 * the class this listener was attached to.
	 * @param selectedItem The item selected in the list.
	 */
	public void selectionPerformed(Object selectedItem);
}
