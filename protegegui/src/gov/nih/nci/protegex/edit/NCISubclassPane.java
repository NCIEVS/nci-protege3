/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Protege-2000.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2005.  All Rights Reserved.
 *
 * Protege was developed by Stanford Medical Informatics
 * (http://www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the National Library of Medicine, the National Science
 * Foundation, and the Defense Advanced Research Projects Agency.  Current
 * information about Protege can be obtained at http://protege.stanford.edu.
 *
 */

package gov.nih.nci.protegex.edit;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.query.ui.QueryTreeFinderPanel;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.TransferableCollection;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTree;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTreeWithBrowserText;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTreeWithBrowserTextRoot;
import edu.stanford.smi.protegex.owl.ui.cls.OWLSubclassPane;

/**
 * @Author: NGIT, Kim Ong, Iris Guo
 */

public class NCISubclassPane extends OWLSubclassPane implements
		DragSourceListener, DragGestureListener {
	private static final long serialVersionUID = -3669329920305497870L;

	private JPanel finder = null;

	private NCIEditTab tab = null;

	private AbstractAction workflowAction = null;

	private AbstractAction refactorNameSpaceAction = null;

	public NCISubclassPane(NCIEditTab t, Action doubleClickAction,
			RDFSNamedClass rootClass) {
		super(t.getOWLModel(), doubleClickAction, rootClass);
		tab = t;
		initFinderAndPopup();

		workflowAction = new AbstractAction("Create Workflow Task") {

			public static final long serialVersionUID = 123457892L;

			public void actionPerformed(ActionEvent ev) {
				tab.showSuggestionDialog("Concepts Need Updating",
						getSelection());
				getSelection().clear();

			}
		};

		refactorNameSpaceAction = new AbstractAction("Rename Namespace") {

			public static final long serialVersionUID = 123457892L;

			public void actionPerformed(ActionEvent ev) {
				tab.showRefactorNamespaceDialog(getSelection());
				getSelection().clear();

			}
		};

	}

	protected void initFinderAndPopup() {

		// finder =
		// QueryTreeFinderComponent.getQueryTreeFinderComponent(owlModel);
		finder = QueryTreeFinderPanel.getQueryTreeFinderPanel(owlModel, this
				.getTree(), false);

		add(BorderLayout.SOUTH, finder);
		/**
		 * MouseListener[] mls = getTree().getMouseListeners(); for (int i = 0;
		 * i < mls.length; i++) { if (mls[i] instanceof
		 * TreePopupMenuMouseListener) { getTree().removeMouseListener(mls[i]);
		 * } }
		 */

	}

	protected JPopupMenu createPopupMenu() {

		if (getSelection().size() > 0) {

			JPopupMenu menu = new JPopupMenu();
			menu.add(workflowAction);
			if (getSelection().size() > 1) {

			} else {
				if (tab.isActionAllowed(NCIEditTab.CHANGE_NAMESPACE)) {
				menu.add(refactorNameSpaceAction);
				}
			}
			return menu;

		} else {
			return null;
		}

	}

	private DragSource dragSource;

	protected void setupDragAndDrop() {
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(getTree(),
				DnDConstants.ACTION_COPY_OR_MOVE, this);

	}

	public void dragEnter(DragSourceDragEvent event) {
	}

	public void dragOver(DragSourceDragEvent event) {
	}

	public void dropActionChanged(DragSourceDragEvent event) {
	}

	public void dragExit(DragSourceEvent event) {
	}

	public void dragDropEnd(DragSourceDropEvent event) {
	}

	public void dragGestureRecognized(DragGestureEvent e) {
		JTree tree = (JTree) e.getComponent();
		// Object[] selectionPaths = tree.getSelectionPaths();
		// Collection _paths = (selectionPaths == null) ? Collections.EMPTY_LIST
		// : Arrays.asList(selectionPaths);
		// Collection objects = ComponentUtilities.getSelection(tree);
		Collection objects = ((SelectableTree) tree).getSelection();
		if (objects != null) {// && canStartDrag(objects)) {
			Transferable t = new TransferableCollection(objects);
			e.startDrag(DragSource.DefaultMoveDrop, t, this);
		}
	}

	/*
	 * Bob, please remove the following methods if you do not want to use the
	 * tree with browser text
	 */

	@Override
	protected ClassTree createSelectableTree(Action doubleClickAction,
			Cls rootCls) {
		this.owlModel = (OWLModel) rootCls.getKnowledgeBase();
		ClassTreeWithBrowserTextRoot root = new ClassTreeWithBrowserTextRoot(
				rootCls, false);
		return new ClassTreeWithBrowserText(owlModel, doubleClickAction, root);
	}

	@Override
	public Collection getSelection() {

		return ((ClassTreeWithBrowserText) getTree()).getSelection();

	}

	@Override
	protected void initializeTreeRenderer() {
	}

	@Override
	public void setSelectedClass(RDFSClass cls) {
		((ClassTreeWithBrowserText) getTree()).setSelectedCls(cls);
	}

}
