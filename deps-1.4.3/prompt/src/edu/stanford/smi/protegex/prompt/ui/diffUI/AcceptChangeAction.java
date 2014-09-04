/*
 * Contributor(s): Sandhya Kunnatur kunnatur@smi.stanford.edu
 *                 Abhita Chugh abhita@stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;


class AcceptChangeAction extends AbstractAction {
	private AcceptorRejector _changeAcceptorRejector;

     public AcceptChangeAction (String prompt) {
       super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/OK.gif"));
	   _changeAcceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
    }

     public void actionPerformed(ActionEvent e) {
  	   DiffSubclassPane subclassPane = ((DiffTabComponent)PromptTab.getTabComponent()).getTreeView().getSubclassPane();
  	 	Object selection = ((DiffSubclassPane)subclassPane).getFirstSelection();
       	Object parent = ((DiffSubclassPane)subclassPane).getSelectionParent();
       	
        if (selection == null) return;
//        Log.trace (""+selection, this, "actionPerformed");
		_changeAcceptorRejector.acceptChange((Cls)selection,(Cls)parent);
		ProjectsAndKnowledgeBases.getAcceptChangesKb().addToKB(AcceptChangesKnowledgeBase.ACCEPT_CLS,((Cls)selection).getName(),((Cls)parent).getName(),null,null);

		//FrameStatus frameStatuses = _viewSetup.getFrameStatuses();
		//frameStatuses.clearAllStatuses();
		
		//TreePath path = ((DiffSubclassPane)_subclassPane).getPathToSelection();
		//((DiffParentChildNode)path.getLastPathComponent()).notifyNodeChanged();		
		//((LazyTreeNode)path.getPathComponent(1)).reload();
		
		((DiffTabComponent)PromptTab.getTabComponent()).reset();
     }
 
  }