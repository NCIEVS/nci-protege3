/*
 * Contributor(s): Sandhya Kunnatur kunnatur@smi.stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;


class RejectChangeAction extends AbstractAction {
	private AcceptorRejector _changeAcceptorRejector;

	 public RejectChangeAction (String prompt) {
	   super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/Cancel.gif"));
	   _changeAcceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
	 }

     public void actionPerformed(ActionEvent e) {
  	   DiffSubclassPane subclassPane = ((DiffTabComponent)PromptTab.getTabComponent()).getTreeView().getSubclassPane();
  	 
		Object selection = subclassPane.getFirstSelection();
		Object parent = subclassPane.getSelectionParent();
       	
		if (selection == null) return;
//		  Log.trace (""+selection, this, "actionPerformed");
		_changeAcceptorRejector.rejectChange((Cls)selection,(Cls)parent);

//		FrameStatus frameStatuses = _viewSetup.getFrameStatuses();
//		frameStatuses.clearAllStatuses();
//		
//		TreePath path = ((DiffSubclassPane)_subclassPane).getPathToSelection();
//		((DiffParentChildNode)path.getLastPathComponent()).notifyNodeChanged();		
//		((LazyTreeNode)path.getPathComponent(1)).reload();


		((DiffTabComponent)PromptTab.getTabComponent()).reset();
     }
  
  }