 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import edu.stanford.smi.protege.action.ReferencersAction;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.SlotSubslotRoot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protegex.prompt.KnowledgeBaseInMerging;
import edu.stanford.smi.protegex.prompt.PromptTab;

public class SourceSlotsPane extends SourceFramesPane {
	SelectableTree _tree;
    KnowledgeBaseInMerging _kbInMerging;

//  	public  SourceSlotsPane (KnowledgeBaseInMerging kbInMerging) {
//    	init (kbInMerging, true);
//	}
//
  	public  SourceSlotsPane (Dimension size, KnowledgeBaseInMerging kbInMerging, Boolean allowSelection, Boolean isSource) {
    	init (kbInMerging, allowSelection.booleanValue(), isSource.booleanValue());
    }

  	public  SourceSlotsPane (KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
    	init (kbInMerging, allowSelection, isSource);
    }

    protected void init (KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
    	_kbInMerging = kbInMerging;
	    Action viewAction =  getViewSlotAction();

        SelectableContainer container = createSlotTree (viewAction);
        LabeledComponent labeledComponent =
        	new LabeledComponent  (kbInMerging.getPrettyName(), container, true);
	    labeledComponent.addHeaderButton(viewAction);
	    labeledComponent.addHeaderButton(new ReferencersAction (container));
        if (kbInMerging.isTarget() && !PromptTab.moving())
	        labeledComponent.addHeaderButton (new ShowSourcesAction (this));

    	setLayout (new BorderLayout());
    	add (labeledComponent, BorderLayout.CENTER);
        setSelectable (container);
		setRenderer (_tree);
        if (!allowSelection)
        	_tree.setEnabled(false);

		super.initialize(kbInMerging, allowSelection, isSource);
 	}

  protected Action getViewSlotAction() {
    return new ViewAction("View selected slot", this) {
       public void onView(Object o) {
           _kbInMerging.getProject().show ((Instance) o);
       }
    };
  }

  private SelectableContainer createSlotTree (Action viewAction) {
  	SelectableContainer treeComponent = new SelectableContainer();
	LazyTreeRoot root = new SlotSubslotRoot(_kbInMerging.getKnowledgeBase());
    _tree = ComponentFactory.createSelectableTree(viewAction, root);

    _tree.setSelectionRow(0);
	_tree.setAutoscrolls(true);
    treeComponent.setSelectable(_tree);
	treeComponent.setLayout(new BorderLayout());
	treeComponent.add(new JScrollPane(_tree), BorderLayout.CENTER);


	return treeComponent;
  }

  public void select(Frame frame) {
  	if (! (frame instanceof Slot)) return;

    Slot slot = (Slot)frame;
	if (!_tree.getSelection().contains(slot)) {
    	ComponentUtilities.setSelectedObjectPath(_tree, getPath(slot, new LinkedList()));
    }
  }

  private List getPath(Slot slot, List list) {
            list.add(0, slot);
            Slot superslot = (Slot) CollectionUtilities.getFirstItem(slot.getDirectSuperslots());
            if (superslot != null) {
                getPath(superslot, list);
            }
            return list;
  }

  public void addSelection (Frame frame) {
  	if (!(frame instanceof Slot)) return;

    ArrayList slots = new ArrayList();
	getPath ((Slot)frame, slots);
	Collections.reverse(slots);
    TreePath path = ComponentUtilities.getTreePath(_tree, slots);
    _tree.scrollPathToVisible(path);
    _tree.addSelectionPath(path);
  }

  public void unselect (){
  	_tree.getSelectionModel().clearSelection();
  }

  public void updateDisplay () {
  	updateDisplay (_tree);
  }

  public void addSelectionListener () {
   	addSourceSelectionListener (_tree);
  }

  public String toString () {
    return "SourceSlotPane";
  }

}
