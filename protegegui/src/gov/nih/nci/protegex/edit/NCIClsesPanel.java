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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import edu.stanford.smi.protege.action.ClsReferencersAction;
import edu.stanford.smi.protege.action.CreateClsAction;
import edu.stanford.smi.protege.action.DeleteClsAction;
import edu.stanford.smi.protege.action.ViewClsAction;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.ConfigureAction;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.ui.RelationshipPane;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.StandardAction;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import gov.nih.nci.protegex.dialog.EditDialog;

/**
 *@Author: NGIT, Kim Ong, Iris Guo
 */

public class NCIClsesPanel extends SelectableContainer {
	public static final long serialVersionUID = 122456792L;

	protected Project _project;
	protected LabeledComponent _labeledComponent;
	protected AllowableAction _createAction;
	protected Action _viewAction;
	protected AllowableAction _deleteAction;
	protected NCISubclassPane _subclassPane;
	protected RelationshipPane _relationshipPane;
	// protected HeaderComponent _clsBrowserHeader;
	protected static final String ResourcesKey = null;

	private NCIEditTab tab;

	protected boolean canDelete = false;

	public NCIClsesPanel(NCIEditTab t, Project project, boolean canDelete) {
		// super(project, p);
		tab = t;
		_project = project;
		this.canDelete = canDelete;

		_viewAction = getViewClsAction();
		_createAction = getCreateClsAction();
		_deleteAction = getDeleteClsAction();
		createPanes();
		String subclassesLabel = LocalizedText
				.getText(ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
		_labeledComponent = new LabeledComponent(subclassesLabel,
				_subclassPane, true);
		_labeledComponent.setBorder(ComponentUtilities.getAlignBorder());

		_labeledComponent.addHeaderButton(_viewAction);
		_labeledComponent.addHeaderButton(new ClsReferencersAction(this));

		if (canDelete) {
			_labeledComponent.addHeaderButton(_deleteAction);
		}

		_labeledComponent.addHeaderButton(createConfigureAction());

		add(_labeledComponent, BorderLayout.CENTER);
		add(createClsBrowserHeader(), BorderLayout.NORTH);
		setSelectable(_subclassPane);
		updateDeleteActionState();
	}

	public LabeledComponent getLabeledComponent() {
		return _labeledComponent;
	}

	protected HeaderComponent createClsBrowserHeader() {
		JLabel label = ComponentFactory.createLabel(_project.getName(), Icons
				.getProjectIcon(), SwingConstants.LEFT);
		String forProject = LocalizedText
				.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
		String classBrowser = LocalizedText
				.getText(ResourceKey.CLASS_BROWSER_TITLE);
		return new HeaderComponent(classBrowser, forProject, label);
	}

	protected void createPanes() {
		_subclassPane = createSubclassPane(_viewAction, getKnowledgeBase()
				.getRootCls());

		// .getFinder().addButton(new ToggleSuperclassExplorerAction(this,
		// false));
		_relationshipPane = createRelationshipPane(_viewAction);
	}

	protected RelationshipPane createRelationshipPane(Action viewAction) {
		return new RelationshipPane(viewAction);
	}

	protected NCISubclassPane createSubclassPane(Action viewAction, Cls root) {
		return new NCISubclassPane(tab, viewAction, (RDFSNamedClass) root);
	}

	protected void enableButton(AllowableAction action, boolean enabled) {
		if (action != null) {
			action.setAllowed(enabled);
		}
	}

	public JTree getClsesTree() {
		return (JTree) _subclassPane.getDropComponent();
	}

	protected AllowableAction getCreateClsAction() {
		return new CreateClsAction() {
			public static final long serialVersionUID = 121456792L;

			public void onCreate() {
				Collection parents = _subclassPane.getSelection();
				if (!parents.isEmpty()) {
					Cls cls = getKnowledgeBase().createCls(null, parents);
					_subclassPane.extendSelection(cls);
				}
			}
		};
	}

	protected AllowableAction getDeleteClsAction() {
		AllowableAction action = new DeleteClsAction(this) {
			public static final long serialVersionUID = 120456792L;

			public void onAboutToDelete(Object o) {
				_subclassPane.removeSelection();
			}

			public void onSelectionChange() {
				updateDeleteActionState();
			}
		};
		action.setEnabled(true);
		return action;
	}

	protected Action createConfigureAction() {
		return new ConfigureAction() {
			public static final long serialVersionUID = 119456792L;

			public void loadPopupMenu(JPopupMenu menu) {
				menu.add(createShowSubclassesAction());
				menu.add(createShowAllRelationsAction());
				Iterator i = getRelationSlots().iterator();
				while (i.hasNext()) {
					Slot slot = (Slot) i.next();
					menu.add(createShowRelationAction(slot));
				}
			}
		};
	}

	public Collection getRelationSlots() {
		Collection<Slot> slots = new HashSet<Slot>();
		Collection c = getSelection();
		if (c.size() == 1) {
			Frame selectedFrame = (Frame) c.iterator().next();
			if (selectedFrame instanceof Cls) {
				Cls selectedCls = (Cls) selectedFrame;
				Iterator i = selectedCls.getTemplateSlots().iterator();
				while (i.hasNext()) {
					Slot slot = (Slot) i.next();
					ValueType type = selectedCls.getTemplateSlotValueType(slot);
					if (type == ValueType.INSTANCE || type == ValueType.CLS) {
						slots.add(slot);
					}
				}
			}
			Iterator j = selectedFrame.getOwnSlots().iterator();
			while (j.hasNext()) {
				Slot slot = (Slot) j.next();
				ValueType type = selectedFrame.getOwnSlotValueType(slot);
				if (!slot.isSystem()
						&& (type == ValueType.INSTANCE || type == ValueType.CLS)) {
					slots.add(slot);
				}
			}
		}
		return slots;
	}

	protected Action createShowSubclassesAction() {
		return new StandardAction(
				ResourceKey.CLASS_BROWSER_SHOW_CLASS_HIERARCHY_MENU_ITEM) {
			public static final long serialVersionUID = 118456792L;

			public void actionPerformed(ActionEvent event) {
				_subclassPane.setSelectedClasses(getSelection());
				loadComponent(_subclassPane,
						ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
			}
		};
	}

	protected Action createShowAllRelationsAction() {
		return new StandardAction(
				ResourceKey.CLASS_BROWSER_SHOW_ALL_RELATIONS_MENU_ITEM) {
			public static final long serialVersionUID = 117456792L;

			public void actionPerformed(ActionEvent event) {
				_relationshipPane.load((Frame) getSoleSelection(), null);
				loadComponent(_relationshipPane,
						ResourceKey.CLASS_BROWSER_ALL_RELATIONS_LABEL);
			}
		};
	}

	protected Action createShowRelationAction(final Slot slot) {
		String showLabel = LocalizedText.getText(
				ResourceKey.CLASS_BROWSER_SHOW_RELATION_MENU_ITEM, slot
						.getBrowserText());
		return new StandardAction(showLabel) {
			public static final long serialVersionUID = 116456792L;

			public void actionPerformed(ActionEvent event) {
				_relationshipPane.load((Frame) getSoleSelection(), slot);
				loadComponent(_relationshipPane, slot.getBrowserText());
			}
		};
	}

	protected JComponent getDisplayedComponent() {
		return (JComponent) _labeledComponent.getCenterComponent();
	}

	public JComponent getDropComponent() {
		return _subclassPane.getDropComponent();
	}

	protected KnowledgeBase getKnowledgeBase() {
		return _project.getKnowledgeBase();
	}

	protected Selectable getRelationshipPane() {
		return _relationshipPane;
	}

	public Collection getSelection() {
		return ((Selectable) getDisplayedComponent()).getSelection();
	}

	protected NCISubclassPane getSubclassPane() {
		return _subclassPane;
	}

	protected Action getViewClsAction() {
		return new ViewClsAction(this) {
			public static final long serialVersionUID = 115456792L;

			public void onView(Object o) {
				showInstance((Cls) o);
			}
		};
	}

	protected boolean isDisplayingSubclasses() {
		return _labeledComponent.getCenterComponent() == _subclassPane;
	}

	protected void loadComponent(Selectable component) {
		loadComponent(component, "");
	}

	protected void loadComponent(Selectable component, ResourceKey key) {
		loadComponent(component, LocalizedText.getText(key));
	}

	protected void loadComponent(Selectable component, String title) {
		_labeledComponent.setCenterComponent((JComponent) component);
		_labeledComponent.setHeaderLabel(title);
		setSelectable(component);
	}

	private Component getView() {
		return _labeledComponent.getCenterComponent();
	}

	/**
	 * An obscure method to change the displayed parent of the selected class.
	 * Imagine a selected class A with two parents "B" and "C". Currently "A" is
	 * selected beneath "B". Calling setDisplayParent("C") will cause "A" to be
	 * displayed beneath "C". This is the method used by the component below the
	 * classes panel in the classes tab.
	 */
	public void setDisplayParent(Cls cls) {
		if (isDisplayingSubclasses()) {
			_subclassPane.setDisplayParent(cls);
		}
	}

	public void setExpandedCls(Cls cls, boolean expanded) {
		if (isDisplayingSubclasses()) {
			_subclassPane.setExpandedCls(cls, expanded);
		} else {
		}
	}

	public void setSelectedCls(Cls cls) {
		try {
			// notify the explanation tab of the selection event. Since this is
			// usually called outside of the normal
			// protege plumbing for this stuff, we have to propogate it
			// manually.
			ProjectView aView = ProjectManager.getProjectManager()
					.getCurrentProjectView();
			// view might not be available, .eg. during init
			if (aView != null) {
				TabWidget aExplanationTab = aView
						.getTabByClassName("com.clarkparsia.protege.explanation.ExplanationTab");

				// only try to pass through the event if the explanation tab is
				// loaded
				if (aExplanationTab != null) {
					// call setSelection on the ExplanationTab to propogate the
					// selection.
					Method aMethod = aExplanationTab.getClass().getMethod(
							"setSelection", RDFSClass.class);
					aMethod.invoke(aExplanationTab, (RDFSClass) cls);
				}
			}
		} catch (Throwable e) {
			Log
					.getLogger(NCIClsesPanel.class)
					.warning(
							"Propagating the selection event from the NCIEditTab to the "
									+ "Explanation Tab failed.  Most likely an invalid version of "
									+ "the Explanation Tab is loaded which does not support "
									+ "setting the selection this way, or there was an error while "
									+ "setting it.  The error message is: "
									+ e.getMessage());
		}

		if (isDisplayingSubclasses()) {
			_subclassPane.setSelectedClass((RDFSClass) cls);
		} else {
		}
	}

	protected void showInstance(Instance instance) {
		// EditDialog dlg = new EditDialog(tab, (Cls)
		// tab.getSelectedInstance());
		new EditDialog(tab, tab.getSelectedCls());
	}

	protected void updateDeleteActionState() {
		if (_deleteAction != null) {
			boolean isEditable = true;
			Iterator i = getSelection().iterator();
			while (i.hasNext()) {
				Frame frame = (Frame) i.next();
				if (!frame.isEditable()) {
					isEditable = false;
					break;
				}
			}
			boolean isCorrectView = getView() == _subclassPane;
			_deleteAction.setAllowed(isEditable && isCorrectView);
		}
	}

}