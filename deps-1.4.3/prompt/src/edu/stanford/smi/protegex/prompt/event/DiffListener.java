/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.event;

import java.util.EventListener;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;

/**
 * 
 * @deprecated This is a redundant mechanism. Use a PromptListener instead.
 */
@Deprecated
public interface DiffListener extends EventListener {
	public void diffDone (DiffEvent event);
	public void diffTableViewBuilt (DiffTableView view, DiffEvent event);
	public void diffUIBuilt (JTabbedPane mainPane, DiffEvent event);
}


