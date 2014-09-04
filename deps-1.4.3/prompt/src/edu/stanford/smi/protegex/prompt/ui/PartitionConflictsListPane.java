 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.Frame;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.conflict.*;
import edu.stanford.smi.protegex.prompt.operation.*;

public class PartitionConflictsListPane extends ListSplitPane {
  private ArrayList _currentSuggestions;
  private ActionListPane _todoList;

  static private AbstractAction _createAction;
  static private AbstractAction _viewAction;
  static private AbstractAction _showSourcesAction;
  static private  int [] _columnSizes = new int [] {150, 190, 190};
  static private  String [] _columnNames = new String [] {"Name", "Arg", "Params"};



  private int _oldIndex = 0;

  PartitionConflictsListPane () {
    initialize();
  }

  private void initialize () {
  	createActions();
    initializeSuggestionList ();
    createListSplitPane (DisplayUtilities.getSuggestionListName(), _todoList,
    					"Frames from including project referenced by the selected frame",
                        createReasonBox(), TOP, true);
    addButtons (getLabeledComponent ());
  }

  private void createActions () {
  	_createAction = new CreateOperationAction ("Create operation");
  	_viewAction = new ViewOperationAction ("View operation");
    _showSourcesAction = new ShowSourcesAction ("Show sources");

  }

  private void initializeSuggestionList () {
    _currentSuggestions = SuggestionsAndConflicts.getTodoListInPriorityOrder ();
    removeUneditable (_currentSuggestions);
    _todoList = new ActionListPane (_currentSuggestions, _columnSizes, _columnNames, _viewAction);
 }

  private void removeUneditable (Collection c) {
    if (c == null) return;

    Collection toRemove = new ArrayList();
    Iterator i = c.iterator();
    while (i.hasNext()) {
      Operation next = (Operation)i.next();
      if (!next.canView())
        toRemove.add (next);
    }
    c.removeAll(toRemove);
  }

  private JComponent createReasonBox () {
	final ReferencesPanel reasonList = new ReferencesPanel ();
    _todoList.addActionListSelectionListener(new ListSelectionListener () {
        public void valueChanged(ListSelectionEvent e) {
			setReason (reasonList);
      }
    });
    return reasonList;
  }

  private void setReason (ReferencesPanel table) {
  	Action a = _todoList.getFirstSelection();
	if (a == null || !(a instanceof MoveOperation))
		table.setList (null);
	else {
        table.setList ((MoveOperation)a);
   }
 }




  public void addSelectionListener () {
   	_todoList.addSelectionListener();
  }

  private void addButtons (LabeledComponent c) {
    c.addHeaderButton (_showSourcesAction);
    disableButtonsForEmptyList ();
  }

  public void postChange () {
   	postChange (null);
  }

  public void postChange (Operation editedOperation) {
  	_currentSuggestions = SuggestionsAndConflicts.getTodoListInPriorityOrder();
  	if (editedOperation != null)
    	setEditedOperationAtOldIndex (editedOperation);
	_todoList.changeList(_currentSuggestions);
	disableButtonsForEmptyList ();
  }

  private void setEditedOperationAtOldIndex (Operation editedOperation) {
  	int index = _currentSuggestions.indexOf(editedOperation);
    if (index != _oldIndex) {
   		_currentSuggestions.remove(editedOperation);
        _currentSuggestions.add (_oldIndex, editedOperation);
    }
  }

  private void disableButtonsForEmptyList () {
    boolean enabled = true;
  	if (_currentSuggestions == null || _currentSuggestions.size() == 0)
    	enabled = false;
    _viewAction.setEnabled(enabled);
    _createAction.setEnabled (true);
  }

  public String toString () {
    return "SuggestionListPane";
  }


  private void showDialog (JDialog dialog, JPanel panel) {
       dialog.getContentPane().add (panel);
       dialog.pack();
       ComponentUtilities.center(dialog);
       dialog.show();
  }

  private class ReferencesPanel extends  SelectableContainer {
	private Project _project = ProjectManager.getProjectManager().getCurrentProject();
    private JTable _table;

  	ReferencesPanel () {
        _table = ComponentFactory.createSelectableTable(null);
        _table.setModel(createTableModel(null));
        add (ComponentFactory.createScrollPane(_table));
        setSelectable(null);
        createColumns();
    }

    public void setList (Operation operation) {
    	DefaultTableModel model = (DefaultTableModel)_table.getModel();
		removeAllRows (model);
        addNewRows (model, (operation == null) ? null : operation.getConflictsItSolves());
    }

    private void removeAllRows (DefaultTableModel model) {
        while (model.getRowCount() >= 1)
        	model.removeRow(model.getRowCount() - 1);
    }

    private void addNewRows (DefaultTableModel model, Collection conflicts) {
        if (conflicts == null) return;

        Iterator i = conflicts.iterator();
        while (i.hasNext()) {
            Object next = i.next();
            if (next instanceof ReferenceToIncludingProjectConflict) {
                ActionArgs args = ((Conflict)next).getArgs();
            	model.addRow(new Object[]{args.getArg(1),
                						  args.getArg(2),
                                          ((args.getArg(3) == null)? null :
                                            new ClsSlotFacetCombination ((Cls)args.getArg(0),
                                          								(Slot) args.getArg(2),
                                                                        (Facet) args.getArg(3))) });
            }
        }

    }

    private TableModel createTableModel(Collection conflicts) {
        DefaultTableModel model =
            new DefaultTableModel() {
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            }
        ;
        model.addColumn("Frame");
        model.addColumn("Slot");
        model.addColumn("Facet");
		return model;
    }

    private void createColumns() {
        addColumn(250, FrameRenderer.createInstance());
        addColumn(250, FrameInMergingRenderer.createInstance() );

        DefaultRenderer facetsRenderer =
            new DefaultRenderer() {
                public void load(Object o) {
                	if (o == null) {
                    	setMainText ("--");
                        return;
                    }
                    ClsSlotFacetCombination comb = (ClsSlotFacetCombination)o;
                    if (comb._facet != null && comb._facet.getName().equals(":VALUE-TYPE")) {
                        setMainIcon(Icons.getFacetIcon());
                        ValueType type = comb._cls.getTemplateSlotValueType(comb._slot);
                        if (type == ValueType.INSTANCE) {
                            setMainText("allowed-classes");
                        } else if (type == ValueType.CLS) {
                            setMainText("allowed-parents");
                        } else {
                        	setMainText ("--");
                        }
                    } else {
                        setMainText (comb._facet.getName());
                    }
                }
            }
        ;
        addColumn(125, facetsRenderer);
    }

    private void addColumn(int width, TableCellRenderer renderer) {
        _table.addColumn(new TableColumn(_table.getColumnCount(), width, renderer, null));
    }

    public class ClsSlotFacetCombination {
     	public Cls _cls;
        public Slot _slot;
        public Facet _facet;
        ClsSlotFacetCombination (Cls cls, Slot slot, Facet facet) {
         	_cls = cls;
            _slot = slot;
            _facet = facet;
        }
    }

  }

  private  class CreateOperationAction extends AbstractAction {

     public CreateOperationAction(String prompt) {
       super(prompt, Icons.getCreateIcon());
     }

     public void actionPerformed(ActionEvent e) {
       JDialog dialog = new JDialog ((Frame)PromptTab.getMainWindow(), "Create Operation", true);
       CreateNewOperationPane panel = new CreateNewOperationPane (dialog);
       showDialog (dialog, panel);
     }
  }

  private  class ShowSourcesAction extends AbstractAction {

     public ShowSourcesAction (String prompt) {
       super(prompt, ComponentUtilities.loadImageIcon(SuggestionListPane.class, "images/Source.gif"));
     }

     public void actionPerformed(ActionEvent e) {
       	Action selection = _todoList.getFirstSelection();
        if (selection == null) return;
        PromptTab.getTabComponent().showSourcesDialog (selection.getArgs().toArray(), TabComponent.RIGHT_FRAME);
     }
  }

  private  class ViewOperationAction extends AbstractAction {


     public ViewOperationAction(String prompt) {
      super(prompt, Icons.getViewIcon());
    }

    public void actionPerformed(ActionEvent e) {
       Action selection = _todoList.getFirstSelection();
       if (selection == null) return;

       _oldIndex = _todoList.getSelectedIndex ();

       JDialog dialog = new JDialog ((Frame)PromptTab.getMainWindow(), "Edit Operation", true);
       CreateNewOperationPane panel = new CreateNewOperationPane ((Operation)selection, dialog);
       showDialog (dialog, panel);
       _todoList.setSelectedRow (_oldIndex);
    }
  }

}

