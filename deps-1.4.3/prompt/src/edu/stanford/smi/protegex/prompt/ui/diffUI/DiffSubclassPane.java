


 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;


import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.ui.ClsTreeFinder;
import edu.stanford.smi.protege.ui.SubclassPane;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeModel;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.TreePopupMenuMouseListener;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.PromptDiff;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameStatus;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;

public class DiffSubclassPane extends SubclassPane {
    private KnowledgeBase _knowledgeBase;
	private  DiffViewSetUp _viewSetup;
	private AcceptorRejector _acceptorRejector;
	
    private final static int MAX_EXPANSIONS = 100;

    public DiffSubclassPane(Action doubleClickAction, Cls root, DiffViewSetUp vs) {
        super (null, root, null, null);
        _viewSetup = vs;
        _knowledgeBase = root.getKnowledgeBase();
        SelectableTree tree = ComponentFactory.createSelectableTree(doubleClickAction, new DiffParentChildRoot(root, _viewSetup));
        tree.setSelectionRow(0);
        tree.setAutoscrolls(true);
        setSelectable(tree);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(tree);

        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
        add(new ClsTreeFinder(_knowledgeBase, tree, "Find Class"), BorderLayout.SOUTH);
//        setupDragAndDrop();
        getTree().setCellRenderer(new DiffTreeNodeRenderer(_viewSetup));
		getTree().addMouseListener(new TreePopupMenuMouseListener(tree) {
			public JPopupMenu getPopupMenu() {
				return DiffSubclassPane.this.getPopupMenu();
			}
		});        
    }

    public void setSelectedCls(Cls cls, LinkedList path) {
      ComponentUtilities.setSelectedObjectPath(getTree(), path);
    }

    public void setSelectedCls(Cls cls, TreePath path) {
        JTree tree = getTree();

//        setSelectedObjectPath(getTree(), Arrays.asList(path.getPath()));

        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
    }


    public JTree getTree() {
        return (JTree) getSelectable();
    }

    public String toString() {
        return "DiffSubclassPane";
    }

    public Object getFirstSelection () {
      Collection selection = getSelection();
      if (selection == null || selection.isEmpty()) return null;
      return CollectionUtilities.getFirstItem(selection);
    }

    public TreePath getPathToSelection () {
      JTree tree = getTree();
      int[]selectedRows = tree.getSelectionRows();
      if (selectedRows.length < 1) return null;

      return tree.getSelectionPath();
    }

    public Object getSelectionParent () {
      JTree tree = getTree();
      int[]selectedRows = tree.getSelectionRows();
      if (selectedRows.length < 1) return null;

      TreePath path = tree.getPathForRow(selectedRows[0]);
      if (path!=null) {
        LazyTreeNode node = (LazyTreeNode) path.getParentPath().getLastPathComponent();
        Object o = node.getUserObject();
        if (o != null && o instanceof Frame)
          return o;
      }
      return null;
    }

    public void selectParent() {
      JTree tree = getTree();
      int[]selectedRows = tree.getSelectionRows();
      if (selectedRows.length < 1) return;
      TreePath path = tree.getPathForRow(selectedRows[0]);
      if (path!=null) {
        LazyTreeNode node = (LazyTreeNode) path.getParentPath().getLastPathComponent();
        ComponentUtilities.setSelectedNode(tree, node);
      }
    }
    
    public void reset() {
    	
    	LazyTreeModel model = (LazyTreeModel)getTree().getModel();
    	model.reload();
    	
    }
    
	private JPopupMenu getPopupMenu() {
		JPopupMenu menu = null;
		Collection selection = getSelection();
		if (selection.size() == 1) {
			Cls cls = (Cls) CollectionUtilities.getFirstItem(selection);
			Object parent = getSelectionParent();
			Cls parentCls = (parent instanceof Cls)? (Cls)parent : null; 
			menu = createPopupMenu(cls,parentCls);
		}
		return menu;
	}
	
	protected JPopupMenu createPopupMenu(Cls cls,Cls parentCls) {
		JPopupMenu menu;
		menu = new JPopupMenu();
		FrameStatus frameStatuses = _viewSetup.getFrameStatuses();
		
		if(frameStatuses.getFrameStatus(cls,parentCls) != FrameStatus.FRAME_UNCHANGED) {
			menu.add(new AcceptChangeAction("Accept change"));
			menu.add(new RejectChangeAction("Reject change"));
		}
		menu.addSeparator();
		
		if(frameStatuses.getTreeStatus(cls,parentCls) != FrameStatus.TREE_UNCHANGED){
			menu.add(new AcceptTreeChangeAction("Accept all changes in subtree"));
			menu.add(new RejectTreeChangeAction("Reject all changes in subtree"));			}
		
		return menu;
	}
	
	public void setAcceptorRejector(AcceptorRejector acceptorRejector) {
		_acceptorRejector = acceptorRejector;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.stanford.smi.protege.ui.SubclassPane#setSelectedClses(java.util.Collection)
	 * This is essentially copied from SubclassPane.java but the getPathToRoot functionality there 
	 * is inadequate for this case.
	 */
    @SuppressWarnings("unchecked")
    public void setSelectedClsses(Collection clses) {
        Collection paths = new ArrayList();
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Cls) {
                Cls cls = (Cls) o;
                paths.add(getPathToRoot(cls));
            }
        }
        ComponentUtilities.setSelectedObjectPaths(getTree(), paths);
    }
    
    @SuppressWarnings("unchecked")
    public List getPathToRoot(Cls cls) {
        PromptDiff diff = PromptTab.getPromptDiff();
        return getPathToRoot(diff, cls, new LinkedList());
    }

    @SuppressWarnings("unchecked")
    private List getPathToRoot(PromptDiff diff, Cls cls, LinkedList list) {
        KnowledgeBase kb2 = diff.getKb2();
        cls = tryToGetKb2Class(diff, cls);
        list.add(0, cls);
        Iterator i = cls.getDirectSuperclasses().iterator();
        Cls rootCls2 = kb2.getRootCls();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            if (list.contains(superclass))
                continue;
            if (cls.isVisible()) {
                List copy = new ArrayList(list);
                getPathToRoot(diff, superclass, list);
                if(list.getFirst().equals(rootCls2)) {
                    break;
                } 
                // Backtracking
                list.clear();
                list.addAll(copy);
            }
        }
        return list;
    }
    
    private Cls tryToGetKb2Class(PromptDiff diff, Cls cls) {
        KnowledgeBase kb1 = diff.getKb1();
        KnowledgeBase kb2 = diff.getKb2();
        if (cls.equals(kb1.getRootCls())) {
            return kb2.getRootCls();
        }
        if (cls.getKnowledgeBase().equals(kb2)) {
            return cls;
        }
        ResultTable results = diff.getResultsTable();
        Collection<TableRow> differences = results.getRows(cls);
        if (differences == null || differences.isEmpty()) {
            String name = cls.getName();
            return kb2.getCls(name);
        }
        for (TableRow difference : differences) {
            Frame kb2Cls = difference.getF2Value();
            if (kb2Cls != null && kb2Cls instanceof  Cls) {
                return (Cls) kb2Cls;
            }
        }
        return cls;
    }
}
