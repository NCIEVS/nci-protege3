/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class SourceClsesPane extends SourceFramesPane {
	JTree _tree;
	KnowledgeBaseInMerging _kbInMerging;
	SelectableContainer _treeComponent;
	Action _viewAction;
	
//	public  SourceClsesPane (KnowledgeBaseInMerging kbInMerging) {
//	init (kbInMerging, true);
//	}
//	
	public  SourceClsesPane (KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
		init (kbInMerging, allowSelection, isSource);
	}
	
	public  SourceClsesPane (Dimension size, KnowledgeBaseInMerging kbInMerging, Boolean allowSelection, Boolean isSource) {
		init (kbInMerging, allowSelection.booleanValue(), isSource.booleanValue());
	}
	
	public JTree getTree() {
		return _tree;
	}
	
	protected void init (KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
		_kbInMerging = kbInMerging;
		Action viewAction =  getViewClsAction ();
		createSubclassesTree (viewAction, allowSelection);
		
		LabeledComponent labeledComponent =
			new LabeledComponent  (kbInMerging.getPrettyName(), _treeComponent, true);
		labeledComponent.addHeaderButton(viewAction);
		labeledComponent.addHeaderButton(new ReferencersAction (_treeComponent));
		if (kbInMerging.isTarget() && !PromptTab.moving())
			labeledComponent.addHeaderButton (new ShowSourcesAction (this));
		
		setLayout (new BorderLayout());
		add (labeledComponent, BorderLayout.CENTER);
		setSelectable (_treeComponent);
		if (!allowSelection)
			_tree.setEnabled(false);
		super.initialize (kbInMerging, allowSelection, isSource);
	}
	
	protected Action getViewClsAction() {
		return new ViewAction("View selected class", this) {
			public void onView(Object o) {
				Instance instance = (Instance)o;
				Project project = _kbInMerging.getProject();
//				if (PromptTab.kbInOWL())
//				project.setWidgetMapper(new OWLWidgetMapper((AbstractOWLKnowledgeBase)(_kbInMerging.getKnowledgeBase())));
				project.show (instance);
			}
		};
	}    
	
	public void select(Frame frame) {
		selectClsInTree (frame, _tree);
	}
	
	public void addSelection (Frame frame) {
		addSelection (frame, _tree);
	}
	
	private void createSubclassesTree (Action viewAction, boolean allowSelection) {
		_treeComponent = new SelectableContainer();
		_viewAction = viewAction;
		setSubclassesTree(allowSelection);
	}
	
	public void updateDisplay () {
		updateDisplay (_tree);
	}
	
	public void addSelectionListener () {
		addSourceSelectionListener (_tree);
	}
	
	public void unselect () {
		_tree.getSelectionModel().clearSelection();
	}
	
	public void revalidateDisplay() {
		setSubclassesTree(true);
	}
	
	private void setSubclassesTree (boolean allowSelection) {
		KnowledgeBase kb = _kbInMerging.getKnowledgeBase();
		SelectableTree tree =
			ComponentFactory.createSelectableTree(_viewAction, new PromptParentChildRoot(kb.getRootCls()));
		tree.setSelectionRow(0);
		tree.setAutoscrolls(true);
		_treeComponent.setSelectable(tree);
		_treeComponent.removeAll();
		_treeComponent.add (ComponentFactory.createScrollPane(tree), BorderLayout.CENTER);
		_treeComponent.add(new ClsTreeFinder(kb, tree, "Find Class"), BorderLayout.SOUTH);
		_tree = tree;
		if (allowSelection)
			_treeComponent.add(new ClsTreeFinder(kb, tree, "Find Class"), BorderLayout.SOUTH);
		setRenderer (_tree);
	}
	
	public String toString () {
		return "SourceClsesPane";
	}
	
	
	
}
