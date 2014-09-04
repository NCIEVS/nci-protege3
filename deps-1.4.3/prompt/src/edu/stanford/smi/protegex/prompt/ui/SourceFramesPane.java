 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;

public abstract class SourceFramesPane extends SelectableContainer {

  	public void initialize (KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
        if (!allowSelection)
	        setEnabled(false);
        if (!isSource)
        	TabComponent.addToKbToBuildTreesMap(kbInMerging.getKnowledgeBase(), this);
        else
        	TabComponent.addToKbToSourceTreesMap(kbInMerging.getKnowledgeBase(), this);
 	}

  public Frame getFirstSelection () {
   	Collection selection = getSelection ();
    if (selection == null) return null;
    return (Frame) CollectionUtilities.getFirstItem (selection);
  }

  public abstract void unselect ();

  public abstract void select(Frame frame);

  public abstract void addSelection (Frame frame);

  public void setRenderer(JComponent component ) {
  	DefaultRenderer renderer = new FrameInMergingRenderer ();
    if (component instanceof JTree)
    	((JTree)component).setCellRenderer (renderer);
    if (component instanceof JList)
    	((JTree)component).setCellRenderer (renderer);
  }

  public String toString () {
    return "SourceFramesPane";
  }

  public abstract void  updateDisplay ();

  public abstract void addSelectionListener ();

  public void addSourceSelectionListener (JComponent component) {
  	if (component instanceof JList)
    	((JList)component).addListSelectionListener (new ListSelectionListener () {
        	public void valueChanged(ListSelectionEvent e) {
      			PromptTab.getTabComponent().selectArgumentsInTrees (getSourcesArray ());
   			}
    	});
    if (component instanceof JTree)
    	((JTree)component).addTreeSelectionListener(new TreeSelectionListener () {
       		public void valueChanged(TreeSelectionEvent e) {
       			PromptTab.getTabComponent().selectArgumentsInTrees (getSourcesArray ());
   			}
    	});
  }

  public Object [] getSourcesArray () {
   	return SourceFramesPane.getSourcesArray(this);
  }

  public static Object [] getSourcesArray (SelectableContainer container) {
  	Collection selection = container.getSelection();
    if (selection == null) return null;

    Object firstSelection = CollectionUtilities.getFirstItem(selection);
    if (firstSelection == null) return null;

    Collection sources = Mappings.getSources((Frame)firstSelection);
    Object [] sourcesArray = null;
    if (sources != null)
       	sourcesArray = sources.toArray();
    return sourcesArray;
  }

  public void selectClsInTree (Frame frame, JTree tree) {
	ArrayList clses = new ArrayList();
	getPathToRoot((Cls)frame, clses, tree);
	Collections.reverse(clses);
	ComponentUtilities.setSelectedObjectPath(tree, clses);
  }


  public static void updateDisplay (JComponent component) {
  	if (component instanceof JTree) {
    	JTree tree = (JTree)component;
    	DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
     	int displayIndex = tree.getRowCount();
        for (int i = 0; i < displayIndex; i++) {
        	TreePath nextPath = (TreePath)tree.getPathForRow(i);
			TreeNode nextNode = (TreeNode)nextPath.getLastPathComponent();
            model.nodeChanged(nextNode);
        }
    }
    component.repaint();
  }

  public static void addSelection (Frame frame, JTree tree) {
	ArrayList clses = new ArrayList();
	getPathToRoot((Cls)frame, clses, tree);
	Collections.reverse(clses);
    TreePath path = ComponentUtilities.getTreePath(tree, clses);
    tree.scrollPathToVisible(path);
    tree.addSelectionPath(path);
  }

        protected static void getPathToRoot(Cls cls, Collection clses, JTree tree) {
            Collection rootClses = (Collection) ((LazyTreeNode) tree.getModel().getRoot()).getUserObject();
            clses.add(cls);
            Collection superclasses = Util.getDirectSuperclasses(cls);
            Cls parent = (Cls) CollectionUtilities.getFirstItem(superclasses);
            if (parent == null) {
                Log.getLogger().severe("no parents");
            } else if (rootClses.contains(parent)) {
                clses.add(parent);
            } else {
                getPathToRoot(parent, clses, tree);
            }
        }



  public  class ShowSourcesAction extends AbstractAction {
     SelectableContainer _container;

     public ShowSourcesAction (SelectableContainer container) {
       super ("Show sources", ComponentUtilities.loadImageIcon(SuggestionListPane.class, "images/Source.gif"));
       _container = container;
     }

     public void actionPerformed(ActionEvent e) {
    	 PromptTab.getTabComponent().showSourcesDialog (SourceFramesPane.getSourcesArray (_container),
        								TabComponent.LEFT_FRAME);
     }
  }

  protected class PromptParentChildRoot extends ParentChildRoot {
    public PromptParentChildRoot(Cls root) {
        super(root);
    }

    public PromptParentChildRoot(Collection roots) {
        super(roots);
    }

    public LazyTreeNode createNode(Object o) {
        return new PromptParentChildNode(this, (Cls) o);
    }
  }

  protected class PromptParentChildNode extends ParentChildNode {
    public PromptParentChildNode(LazyTreeNode parentNode, Cls parentCls) {
    	super (parentNode, parentCls);
	}

    protected Collection getChildObjects() {
        Collection childObjects;
        if (showHidden())
        	childObjects = (getCls().getDirectSubclasses());
        else
        	childObjects = (getCls().getVisibleDirectSubclasses());
//		if (childObjects != null)
//        	Collections.sort (childObjects, new FrameComparator ());
//Log.trace ("childObjects: " + childObjects, this, "getChildObjects");
        return childObjects;
    }

    private boolean showHidden() {
        return getCls().getProject().getDisplayHiddenClasses();
    }

    public LazyTreeNode createNode (Object o) {
        return new PromptParentChildNode(this, (Cls) o);
    }

  }
}
