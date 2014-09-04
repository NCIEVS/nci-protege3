 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 * 		   Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.ReferencersPanel;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.MutableSelectable;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.ui.SuggestionListPane;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;

public class DiffTableView extends JPanel implements MutableSelectable {
  private static ResultsPane _resultsPane;
  private ResultTable _diffTable;
  protected DiffViewSetUp _viewSetup = null;
  private boolean _resetTable;
  private Action refreshAction;
  private LabeledComponent _lc;
  
  public DiffTableView (List<TableRow> results) {
      _diffTable = PromptTab.getPromptDiff().getResultsTable();
      _viewSetup = PromptTab.getPromptDiff().getViewSetUp();
      initialize(results);
  }
   
   public DiffTableView(Project oldVersion, Project newVersion, ResultTable diffTable){
	  _diffTable = diffTable;
	  _viewSetup = new DiffViewSetUp (oldVersion, newVersion, diffTable);	
	  initialize(DiffTabComponent.filterNonDisplayedRows(_diffTable.sort()));
	}
  

   
   private void initialize (List<TableRow> results) {
	_resultsPane = new ResultsPane (_diffTable, results);
	refreshAction = new RefreshAction();
	_lc = new LabeledComponent ("Image table", _resultsPane, true);
	_lc.addHeaderButton(new ViewFramesAction ());
	_lc.addHeaderButton(new FrameReferencersAction ());
	_lc.addHeaderButton(new ShowSourcesAction ());
	_lc.addHeaderButton(new SaveToFileAction());
	_lc.addHeaderButton(new ChangeStatisticsAction());
	_lc.addHeaderButton(refreshAction);
//	lc.addHeaderButton(new SaveInEONFormatAction (DiffTableView.this));
	setLayout(new BorderLayout());
	add (_lc, BorderLayout.CENTER);
	PromptTab.getPromptDiff().fireTableBuilt (DiffTableView.this);
  }
   
   public void addHeaderButton (AbstractAction action) {
   		_lc.addHeaderButton(action);
   }

   public void reset(){
	  /*DiffClsesPanel clsesPanel = DiffClsesPanel.getInstance(PromptTab.getProject(PromptDiff.getKb2()),_viewSetup);
	  ChangeStatistics cs = new ChangeStatistics();
	  clsesPanel._numberOfChanges.setText("    " + Integer.toString(cs.getTotalChanges()));
	  */
	  if(!_resetTable){
		  _resetTable = true;
		  ((DiffTabComponent)PromptTab.getTabComponent()).setTableViewTitle("Table View *");
		  refreshAction.setEnabled(true);
	  }
	  _resultsPane.reset();
	}

  public String toString () {

   	return "DiffTableView";
  }

  private  class ShowSourcesAction extends AbstractAction {

     public ShowSourcesAction () {
       super("show sources", ComponentUtilities.loadImageIcon(SuggestionListPane.class, "images/Source.gif"));
     }

     public void actionPerformed(ActionEvent e) {
       	TableRow selection = _resultsPane.getFirstSelection();
        if (selection == null) return;
        PromptTab.getTabComponent().showSourcesDialog (_resultsPane, selection.createArrayFromEntries (), TabComponent.RIGHT_FRAME);
     }

  }

  public class ViewFramesAction  extends AbstractAction {

     public ViewFramesAction() {
      super("view frames", Icons.getViewIcon());
    }

    public void actionPerformed(ActionEvent e) {
       TableRow selection = _resultsPane.getFirstSelection();
       if (selection == null) return;

       Frame f1 = selection.getF1Value();
       if (f1 != null)
       	ProjectsAndKnowledgeBases.getProject (f1.getKnowledgeBase()).show ((Instance)f1);


       Frame f2 = selection.getF2Value();
       if (f2 != null)
	    ProjectsAndKnowledgeBases.getProject (f2.getKnowledgeBase()).show ((Instance)f2);
     }
  }

  public class SaveToFileAction extends AbstractAction {

	 public SaveToFileAction() {
	  super("save to file", Icons.getSaveProjectIcon());
	}

	public void actionPerformed(ActionEvent e) {
		SaveToFileDialog input = new SaveToFileDialog(DiffTableView.this);
		  if (input.saveFile())
			  PromptTab.getPromptDiff().getResultsTable().saveToFile(input.getFileName(), input.printAdded(), input.printDeleted(), input.printRenamed(),
													input.printDirectlyChanged(), input.printChanged(), input.printIsomorphic(), input.printUnchanged(),
													input.printFrameDifferences(), input.getFileSaveType());
	  
	 }
  }
  
  public class ChangeStatisticsAction  extends AbstractAction {

		 public ChangeStatisticsAction() {
		  super("Change Statistics", ComponentUtilities.loadImageIcon(TabComponent.class, "images/change.gif"));
		}

		public void actionPerformed(ActionEvent e) {
		  ChangeStatisticsDialog _changeDialog = new ChangeStatisticsDialog(DiffTableView.this);
		  
				
		 }
	  }

  public class FrameReferencersAction extends AbstractAction {
     public FrameReferencersAction() {
      super("back-references for frames", Icons.getUpIcon());
    }

    public void actionPerformed(ActionEvent e) {
       TableRow selection = _resultsPane.getFirstSelection();
       if (selection == null) return;

       Frame f1 = selection.getF1Value();
       if (f1 != null)
       	displayReferencersPanel ((Instance)f1);


       Frame f2 = selection.getF2Value();
       if (f2 != null)
	    displayReferencersPanel ((Instance)f2);
     }

     private void displayReferencersPanel (Instance instance) {
     	ReferencersPanel panel = new ReferencersPanel(instance);
        ComponentFactory.showInFrame(panel, "References to " + instance.getBrowserText());
     }

  }
    
    private class RefreshAction extends AbstractAction
	{
	
	  public RefreshAction(){
		  super("Refresh Table",ComponentUtilities.loadImageIcon(TabComponent.class, "images/Refresh.gif"));
		  setEnabled(false);
	  }
	
	  public void actionPerformed(ActionEvent evt){
		  _resetTable = false;
		  setEnabled(false);
		  ((DiffTabComponent)PromptTab.getTabComponent()).setTableViewTitle("Table View");  		
		  _resultsPane.reset();	
	  }
	}

    public void addSelectionListener(SelectionListener listener) {
        _resultsPane.addSelectionListener(listener);
    }

    public void clearSelection() {
        _resultsPane.clearSelection();
    }

    public Collection<Frame> getSelection() {
        Set<Frame> selection = new HashSet<Frame>();
        for (Object o : _resultsPane.getSelection()) {
            TableRow row = (TableRow) o;
            if (row.getF2Value() != null) {
                selection.add(row.getF2Value());
            }
            else {
                selection.add(row.getF1Value());
            }
        }
        return selection;
    }

    public void notifySelectionListeners() {
        _resultsPane.notifySelectionListeners();
    }

    public void removeSelectionListener(SelectionListener listener) {
        _resultsPane.removeSelectionListener(listener);
    }

    
    public void setSelection(@SuppressWarnings("unchecked")
                             Collection objects) {
        Collection<Frame> frames = new ArrayList<Frame>();
        for (Object o : objects) {
            if (o instanceof Frame) {
                frames.add((Frame) o);
            }
        }
        _resultsPane.setSelectionFromFrames(frames);
    }

}

