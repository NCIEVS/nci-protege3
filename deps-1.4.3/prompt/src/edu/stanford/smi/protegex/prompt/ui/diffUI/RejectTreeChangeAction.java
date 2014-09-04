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


class RejectTreeChangeAction extends AbstractAction {
	private AcceptorRejector _changeAcceptorRejector;
	
	public RejectTreeChangeAction (String prompt) {
		super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/rejectTree.gif"));
		_changeAcceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
	}
	
	public void actionPerformed(ActionEvent e) {
		DiffSubclassPane subclassPane = ((DiffTabComponent)PromptTab.getTabComponent()).getTreeView().getSubclassPane();
		
		Object selection = ((DiffSubclassPane)subclassPane).getFirstSelection();
		if (selection == null) return;
//		Log.trace (""+selection, this, "actionPerformed");
		Cls selectedCls = (Cls)selection;
		Object parent = ((DiffSubclassPane)subclassPane).getSelectionParent();
		_changeAcceptorRejector.rejectTreeChange(selectedCls,(Cls)parent);
		
		//FrameStatus frameStatuses = _viewSetup.getFrameStatuses();
		//frameStatuses.clearAllStatuses();
		
		//TreePath path = ((DiffSubclassPane)_subclassPane).getPathToSelection();		
		//((LazyTreeNode)path.getPathComponent(1)).reload();
		
		((DiffTabComponent)PromptTab.getTabComponent()).reset();
	}
	
}