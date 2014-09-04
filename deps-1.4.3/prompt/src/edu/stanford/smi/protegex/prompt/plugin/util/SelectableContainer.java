 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  */

package edu.stanford.smi.protegex.prompt.plugin.util;

import java.util.Collection;

import javax.swing.AbstractAction;

import edu.stanford.smi.protegex.prompt.plugin.SelectionListener;

/**
 * An interface representing a container object with a list object and header buttons.
 * An example of this type of interface is the suggestion tab in the mapping or merging UI.
 * @author seanf
 */
public interface SelectableContainer extends UIPluginUtility {
	/**
	 * Adds a SelectionListener to the list object's selection event.
	 * @param selectionListener
	 */
	public abstract void addSelectionListener(SelectionListener selectionListener);

	/**
	 * Gets the list object's viewable list.
	 * @return
	 */
	public abstract Collection getList();
	
	/**
	 * Updates the list object's viewable list.
	 * @param list
	 */
	public abstract void updateList(Collection list);
	
	/**
	 * Adds a new button to the header buttons for this UI.
	 * @param headerButton
	 */
	public abstract void addHeaderButton(AbstractAction headerButton);
}
