/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.event;

import java.util.EventListener;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;

public interface PromptListener extends EventListener {
		public void operationStarted (Operation operation, PromptEvent event);
		public void operationCompleted (Operation operation, PromptEvent event);
		public void initializationDone (PromptEvent event);
		public void taskComplete(PromptEvent event, boolean interrupted);
		public void diffDone (PromptEvent event);
		public void diffTableViewBuilt (DiffTableView view, PromptEvent event);
		public void diffUIBuilt (JTabbedPane mainPane, PromptEvent event);
		public void UIBuilt (TabComponent promptTab, PromptEvent event);
		public void beforeClose(PromptEvent event);
}


