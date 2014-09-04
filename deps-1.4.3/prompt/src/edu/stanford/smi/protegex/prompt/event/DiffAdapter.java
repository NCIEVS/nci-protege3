/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.event;

import javax.swing.*;

import edu.stanford.smi.protegex.prompt.ui.diffUI.*;

/**
 * 
 * @deprecated This is a redundant mechanism. Use a PromptListener instead.
 */
@Deprecated
public class DiffAdapter implements DiffListener {
	public void diffDone (DiffEvent event) {}
	
	public void diffTableViewBuilt (DiffTableView view, DiffEvent event){}

	public void diffUIBuilt (JTabbedPane mainPane,  DiffEvent event){}
}
