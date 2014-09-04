
 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 * 		   Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.ui.RelationshipPane;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameStatus;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.util.Util;

public class DiffClsesPanel extends ClsesPanel {
  private static Action _nextChangeAction;
  private static Action _previousChangeAction;
  private static Action _acceptChangeAction;
  private static Action _rejectChangeAction;
  private static Action _acceptTreeAction;
  private static Action _rejectTreeAction;
  public static int _totalChanges;
  public static JLabel _numberOfChanges;
  private static DiffClsesPanel DiffClsesPanelObject = null;
  
  private static KnowledgeBase _kb;

  private  ResultTable _diffTable;
  private  DiffViewSetUp _viewSetup;
  public DiffClsesPanel(Project project, DiffViewSetUp vs) {
    super (project);
    _viewSetup = vs;
    _diffTable = vs.getResultsTable();
    PromptTab.getPromptDiff().setAcceptorRejector (new AcceptorRejector());
 	initialize (project);
  }
  
  public static DiffClsesPanel getInstance(Project project, DiffViewSetUp vs){
	  if(DiffClsesPanelObject == null)
		  DiffClsesPanelObject = new DiffClsesPanel(project, vs);
	  return DiffClsesPanelObject;
  }

//  public DiffClsesPanel(Project project, KnowledgeBase kb, ResultTable table) {
//        super (project);
//        initialize (project, kb);
//  }
//
  protected void initialize (Project project) {
      _project = project;
      _kb = project.getKnowledgeBase();


      _nextChangeAction = new NextChangeAction ("Next change");
      _previousChangeAction = new PreviousChangeAction ("Previous change");
      _acceptChangeAction = new AcceptChangeAction ("Accept class change");
      _rejectChangeAction = new RejectChangeAction ("Reject class change");
      _acceptTreeAction = new AcceptTreeChangeAction("Accept changes for the entire subtree");
      _rejectTreeAction = new RejectTreeChangeAction("Reject changes for the entire subtree");

      remove(_labeledComponent);
      _labeledComponent = new LabeledComponent("Subclass", _subclassPane, true);

      ChangeStatistics cs = new ChangeStatistics();

      Font font = new Font("Serif",Font.BOLD,14);
      _totalChanges = cs.getTotalChanges();

      _numberOfChanges = new JLabel("    " + Integer.toString(_totalChanges));

      _numberOfChanges.setToolTipText("Number of Changed Classes");
      _numberOfChanges.setFont(font);

      _labeledComponent.setHeaderComponent(_numberOfChanges);


      _labeledComponent.addHeaderButton(_viewAction);
      _labeledComponent.addHeaderButton(_nextChangeAction);
      _labeledComponent.addHeaderButton(_previousChangeAction);
      _labeledComponent.addHeaderButton(_acceptChangeAction);
      _labeledComponent.addHeaderButton(_rejectChangeAction);
      _labeledComponent.addHeaderButton(_acceptTreeAction);
      _labeledComponent.addHeaderButton(_rejectTreeAction);
      add(_labeledComponent);


  }

    

    protected void createPanes() {
//        _subclassPane = new DiffSubclassPane (getKnowledgeBase().getRootCls(), _viewAction, _previousChangeAction, _nextChangeAction,
//                                              _acceptChangeAction, _rejectChangeAction);
        _subclassPane = new DiffSubclassPane (_viewAction, PromptTab.getPromptDiff().getViewSetUp().getKb2().getRootCls(), PromptTab.getPromptDiff().getViewSetUp());
        _subclassPane.setSelectedCls(PromptTab.getPromptDiff().getViewSetUp().getKb2().getRootCls());

        _relationshipPane = new RelationshipPane(_viewAction);
    }
    
    public DiffSubclassPane getSubclassPane () {
    		return (DiffSubclassPane)_subclassPane;
    }


/*
  parent is currently selected
*/
  protected boolean findChangedClsInList (Cls parent, LinkedList c, int startIndex) {
    return findChangedClsInList (parent, c, startIndex, c.size(), true);
  }


  protected boolean findChangedClsInList (Cls parent, LinkedList c, int startIndex, int endIndex, boolean goForward) {
      ListIterator i = goForward ?  c.listIterator(startIndex) : c.listIterator(endIndex) ;
      while ((goForward && i.hasNext() && i.nextIndex() <= endIndex) ||
             (!goForward && i.previousIndex() >= 0)) {
        Cls nextCls = goForward ? (Cls)i.next() : (Cls)i.previous();
        int treeStatus = _viewSetup.getFrameStatuses().getTreeStatus(nextCls, parent);
        int frameStatus = _viewSetup.getFrameStatuses().getFrameStatus(nextCls, parent);
        boolean childrenChangedStatus = _viewSetup.getFrameStatuses().getChildrenChangedStatus(nextCls);
        
        TreePath path =  ((DiffSubclassPane)_subclassPane).getPathToSelection();
       LazyTreeNode nodeToSelect = ComponentUtilities.getChildNode ((LazyTreeNode)path.getLastPathComponent(), nextCls);
        if ((frameStatus != FrameStatus.NO_STATUS && frameStatus != FrameStatus.FRAME_UNCHANGED)
        	|| Util.haveInstancesChanged(_diffTable,nextCls)) {
          ((DiffSubclassPane)_subclassPane).setSelectedCls(nextCls, path.pathByAddingChild(nodeToSelect));
          return true;
        }

        if ((treeStatus != FrameStatus.TREE_UNCHANGED && treeStatus != FrameStatus.NO_STATUS) || childrenChangedStatus) {
          ((DiffSubclassPane)_subclassPane).setSelectedCls(nextCls, path.pathByAddingChild(nodeToSelect));
          LinkedList nextChildObjects = new LinkedList (Util.getDiffChildObjects(nextCls, _diffTable));
//          if (nextChildObjects.isEmpty()) return false;
          boolean foundChange = findChangedClsInList (nextCls, nextChildObjects, 0, nextChildObjects.size(), goForward);
          if (foundChange) return true;
        }

      }
      return false;
  }

  private  class NextChangeAction extends AbstractAction {

     public NextChangeAction (String prompt) {
       super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/Next.gif"));
     }

     public void actionPerformed(ActionEvent e) {
       	Object selection = ((DiffSubclassPane)_subclassPane).getFirstSelection();
        if (selection == null || !(selection instanceof Cls)) return;


        TreePath path =  ((DiffSubclassPane)_subclassPane).getPathToSelection();
        Cls selectedCls = (Cls)selection;

        LinkedList childObjects = new LinkedList(Util.getDiffChildObjects(selectedCls, _diffTable));
        findNextChange (selectedCls, childObjects, 0);
      }

/*
  when we get into this method, the selected class is a root of the tree where we are looking
  for changes; mainly, selectedCls
*/
      private void findNextChange (Cls selectedCls, LinkedList childObjects, int startIndex) {
        boolean changedClsFound = findChangedClsInList (selectedCls, childObjects, startIndex);
        if (!changedClsFound) {
          ((DiffSubclassPane)_subclassPane).selectParent();
          Object selection = ((DiffSubclassPane)_subclassPane).getFirstSelection();
          if (selection != null) {
            LinkedList parentsChildObjects = new LinkedList(Util.getDiffChildObjects((Cls)selection, _diffTable));
            int selectedClsIndex = parentsChildObjects.indexOf(selectedCls);
            findNextChange ((Cls)selection, parentsChildObjects, selectedClsIndex + 1);
          }
        }
     }

  }

  private  class PreviousChangeAction extends AbstractAction {

     public PreviousChangeAction (String prompt) {
       super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/Previous.gif"));
     }

     public void actionPerformed(ActionEvent e) {
       	Object selection = ((DiffSubclassPane)_subclassPane).getFirstSelection();
        if (selection == null) return;

        TreePath path =  ((DiffSubclassPane)_subclassPane).getPathToSelection();
        Cls selectedCls = (Cls)selection;

        findPreviousChange (selectedCls);
     }

     private void findPreviousChange (Cls selectedCls) {
        if (selectedCls.getFrameID().equals(edu.stanford.smi.protege.model.Model.ClsID.THING)) return;
        ((DiffSubclassPane)_subclassPane).selectParent();
        Object selection = ((DiffSubclassPane)_subclassPane).getFirstSelection();
        LinkedList parentsChildObjects = new LinkedList(Util.getDiffChildObjects((Cls)selection, _diffTable));
        int selectedClsIndex = parentsChildObjects.indexOf(selectedCls);

        boolean changedClsFound = findChangedClsInList ((Cls) selection, parentsChildObjects, 0, selectedClsIndex, false);
        if (!changedClsFound) {
          findPreviousChange ((Cls)selection);
        }
     }
  }

  public String toString () {
    return "DiffClsesPanel";
  }
  
  /*
	  public LazyTreeNode getChildNode(LazyTreeNode node, Object userObject) {
//	Log.enter (this, "getChildNode", node, userObject);
		  LazyTreeNode childNode = null;
		  int nChildren = node.getChildCount();
		  for (int i = 0; i < nChildren; ++i) {
			  childNode = (LazyTreeNode) node.getChildAt(i);
//	Log.trace ("childNode = " + childNode, this, "getChildNode");
			  if (childNode.getUserObject() == userObject) {
				  break;
			  }
		  }
		  return childNode;
	  }

*/

}

