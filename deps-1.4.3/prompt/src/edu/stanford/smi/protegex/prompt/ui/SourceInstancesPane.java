/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class SourceInstancesPane extends SourceFramesPane {
	InstancesList _tree = null;
	KnowledgeBaseInMerging _kbInMerging;
	
//	public  SourceInstancesPane (Dimension size, KnowledgeBaseInMerging kbInMerging) {
//	init (size, kbInMerging, true);
//	}
//	
	public  SourceInstancesPane (Dimension size, KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
		init (size, kbInMerging, allowSelection, isSource);
	}
	
	public  SourceInstancesPane (Dimension size, KnowledgeBaseInMerging kbInMerging, Boolean allowSelection, Boolean isSource) {
		init (size, kbInMerging, allowSelection.booleanValue(), isSource.booleanValue());
	}
	
	protected void init (Dimension size, KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
		_kbInMerging = kbInMerging;
		
		_tree = new InstancesList (size, kbInMerging, allowSelection);
		
		setLayout (new BorderLayout());
		add (_tree, BorderLayout.CENTER);
		
		super.initialize(kbInMerging, allowSelection, isSource);
	}
	
	public void setSelectedListIndex(int index) {
		_tree.itsInstanceList.setSelectedIndex(index);
	}
	
	public Collection getSelection () {
		return _tree.getSelection();
	}
	
	public Cls getClsSelection () {
		return _tree.getClsSelection();
	}
	
	public void select(Frame frame) {
		_tree.select(frame);
	}
	
	public void addSelection (Frame frame) {
		_tree.addSelection(frame);
	}
	
	public void addSelectionListener () {
		_tree.addSelectionListener();
	}
	
	public void unselect (){
		_tree.unselect();
	}
	
	protected Cls getSelectedCls () {
		return ((_tree != null) ? _tree.getClsSelection() : null);
	}
	
	protected Instance getSelectedInstance () {
		return _tree.getInstanceSelection();
	}
	
	public String toString () {
		return "SourceInstancesPane";
	}
	
	public void updateDisplay () {
		_tree.updateDisplay();
	}
	
	public void synchronizeMappingSelection () {}
	
	private class InstancesList extends JComponent {
		boolean _allowSelection;
		protected JTree clsTree;
		protected JList itsInstanceList;
		private KnowledgeBase _kb;
		
		InstancesList (Dimension size, KnowledgeBaseInMerging kbInMerging, boolean allowSelection) {
			_allowSelection = allowSelection;
			kbInMerging.getKnowledgeBase().addKnowledgeBaseListener(new KnowledgeBaseAdapter () {
				public void instanceDeleted(KnowledgeBaseEvent event) {
					getInstanceModel().removeValue(event.getFrame());
				}
			});
			createWidgets (CollectionUtilities.createCollection(kbInMerging.getKnowledgeBase().getRootCls()),
					size, kbInMerging);
			if (!allowSelection) {
				clsTree.setEnabled(false);
				itsInstanceList.setEnabled(false);
			}
		}
		
		protected void loadInstances() {
			ArrayList instances = new ArrayList();
			Iterator i = ComponentUtilities.getSelection(clsTree).iterator();
			while (i.hasNext()) {
				Cls cls = (Cls) i.next();
				instances.addAll(cls.getDirectInstances());
			}
			Collections.sort(instances, new FrameComparator());
			getInstanceModel().setValues(instances);
			if (!instances.isEmpty() && _allowSelection) {
				itsInstanceList.setSelectedIndex(0);
			}
		}
		
		
		public Collection getSelection () {
			return ComponentUtilities.getSelection(itsInstanceList);
		}
		
		public Instance getInstanceSelection () {
			return (Instance)CollectionUtilities.getFirstItem (ComponentUtilities.getSelection(itsInstanceList));
		}
		
		public Cls getClsSelection () {
			return (Cls)CollectionUtilities.getFirstItem (ComponentUtilities.getSelection(clsTree));
		}
		
		public void unselect () {
			clsTree.getSelectionModel().clearSelection();
			itsInstanceList.clearSelection();
		}
		
		protected SimpleListModel getInstanceModel() {
			return (SimpleListModel) itsInstanceList.getModel();
		}
		
		public void select (Frame frame) {
			Instance instance = (Instance)frame;
			Cls cls = instance.getDirectType();
			selectClsInTree (cls, clsTree);
			itsInstanceList.setSelectedValue(instance, true);
		}
		
		public void addSelection (Frame frame) {
			Instance instance = (Instance)frame;
			Cls cls = instance.getDirectType();
			SourceFramesPane.addSelection (cls, clsTree);
			SimpleListModel model = (SimpleListModel)itsInstanceList.getModel();
			int index = model.indexOf(instance);
			if (index >= 0)
				itsInstanceList.addSelectionInterval(index, index);
		}
		
		public void updateDisplay () {
			SourceFramesPane.updateDisplay(clsTree);
			itsInstanceList.repaint();
		}
		
		public void addSelectionListener () {
			addSourceSelectionListener(itsInstanceList);
		}
		
		protected LabeledComponent createClsesLabeledComponent(Collection clses) {
			SelectableContainer container = new SelectableContainer ();
			container.setSelectable((SelectableTree)clsTree);
			container.setLayout(new BorderLayout());
			container.add(new JScrollPane(clsTree), BorderLayout.CENTER);
			
			LabeledComponent clsesComponent = new LabeledComponent("Classes", container, true);
			clsesComponent.addHeaderButton(getViewClsAction(container));
			clsesComponent.addHeaderButton(new ReferencersAction (container));
			KnowledgeBase kb = ((Cls) CollectionUtilities.getFirstItem(clses)).getKnowledgeBase();
			return clsesComponent;
		}
		
		
		protected LabeledComponent createInstanceLabeledComponent(KnowledgeBaseInMerging kbInMerging) {
			SelectableContainer container = new SelectableContainer ();
			container.setSelectable((SelectableList)itsInstanceList);
			container.setLayout(new BorderLayout());
			container.add(new JScrollPane(itsInstanceList), BorderLayout.CENTER);
			
			LabeledComponent c = new LabeledComponent("Direct Instances", container, true);
			c.addHeaderButton(getViewInstanceAction(container));
			c.addHeaderButton(new ReferencersAction (container));
			c.addHeaderButton(getCreateInstanceAction (container));
			c.addHeaderButton(getDeleteInstanceAction (container));
			if (kbInMerging.isTarget() && !PromptTab.moving())
				c.addHeaderButton(new ShowSourcesAction (container));
			return c;
		}
		
		protected void createWidgets(Collection clses, Dimension size, KnowledgeBaseInMerging kbInMerging) {
			createInstanceList();
			LabeledComponent instancesComponent = createInstanceLabeledComponent(kbInMerging);
			
			createClsTree(clses);
			LabeledComponent clsesComponent = createClsesLabeledComponent(clses);
			
			JSplitPane main = ComponentFactory.createTopBottomSplitPane();
			main.setTopComponent(clsesComponent);
			main.setBottomComponent(instancesComponent);
			main.setDividerLocation(size.height/2);
			
			setLayout(new BorderLayout());
			add(main);
		}
		
		
		protected JComponent createInstanceList() {
			itsInstanceList = ComponentFactory.createList(null);
			itsInstanceList.setCellRenderer(FrameRenderer.createInstance());
			return itsInstanceList;
		}
		
		protected Action getViewInstanceAction(SelectableContainer container) {
			return new ViewAction("View selected instance", container) {
				public void onView(Object o) {
					_kbInMerging.getProject().show ((Instance) o);
				}
			};
		}
		
		protected Action getDeleteInstanceAction(SelectableContainer container) {
			return new DeleteAction("Delete selected instance", container) {
				public void onDelete(Object o) {
					_kbInMerging.getKnowledgeBase().deleteInstance((Instance)o);
				}
			};
		}
		
		protected Action getCreateInstanceAction(SelectableContainer container) {
			return new CreateAction("Create instance of the selected") {
				public void onCreate() {
					Cls cls = getSelectedCls();
					Instance newInstance = _kbInMerging.getKnowledgeBase().createInstance(null, cls);
					_tree.updateDisplay();
					select (newInstance);
					_kbInMerging.getProject().show (newInstance);
				}
			};
		}
		
		protected Action getViewClsAction(SelectableContainer container) {
			return new ViewAction("View selected class", container) {
				public void onView(Object o) {
					_kbInMerging.getProject().show ((Instance) o);
				}
			};
		}
		
		protected JComponent createClsTree(Collection clses) {
			clsTree = ComponentFactory.createSelectableTree(null);
			clsTree.addTreeSelectionListener(
					new TreeSelectionListener() {
						public void valueChanged(TreeSelectionEvent event) {
							loadInstances();
							synchronizeMappingSelection ();
						}
					}
			);
			LazyTreeRoot root = new PromptParentChildRoot(clses);
			clsTree.setModel(new LazyTreeModel(root));
			clsTree.setShowsRootHandles(true);
			clsTree.setRootVisible(false);
			FrameRenderer renderer = FrameRenderer.createInstance();
			renderer.setDisplayDirectInstanceCount(true);
			clsTree.setCellRenderer(renderer);
			if (clses.size() == 1) {
				clsTree.expandRow(0);
				clsTree.setSelectionRow(0);
			}
			return clsTree;
		}
		
		
	}
	
}

