 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.anchorPrompt.AnchorPair;
import edu.stanford.smi.protegex.anchorPrompt.ScoreTableElement;
import edu.stanford.smi.protegex.anchorPrompt.analysis.Analysis;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.actionLists.ActionArgs;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;
import edu.stanford.smi.protegex.prompt.event.PromptAdapter;
import edu.stanford.smi.protegex.prompt.event.PromptEvent;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.explanation.AnchorPromptResultExplanation;
import edu.stanford.smi.protegex.prompt.operation.MergeClsesOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeSlotsOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;

public class SuggestionListPane extends ListSplitPane {
	private static final String DEFAULT_FILTER_TEXT = "type filter text";
  private ArrayList _currentSuggestions;
  private ActionListPane _todoList;
	private JTextField searchTextField;

  static private AbstractAction _createAction;
  static private AbstractAction _viewAction;
  static private AbstractAction _removeAction;
  static private AbstractAction _showSourcesAction;
  static private AbstractAction _anchorPromptAction;

  private int _oldIndex = 0;

  public SuggestionListPane () {
    initialize();
  }

  public ActionListPane getTodoList() {
	  return _todoList;
  }
  
  private void initialize () {
		searchTextField = new JTextField(20);
		searchTextField.setText(DEFAULT_FILTER_TEXT);
		searchTextField.addFocusListener(getSearchFocusListener());
		searchTextField.addKeyListener(getSearchKeyAdapter());

	  	createActions();    
	  	initializeSuggestionList();
	
	    createListSplitPane(DisplayUtilities.getSuggestionListName(), _todoList, "Reason for selected suggestion", createReasonBox(), TOP, true);
	    addButtons (getLabeledComponent ());

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(_todoList, BorderLayout.CENTER);
		panel.add(searchTextField, BorderLayout.NORTH);
		getLabeledComponent().setCenterComponent(panel);
		
		PromptListenerManager.addListener(new PromptAdapter() {

			public void operationCompleted(Operation operation, PromptEvent event) {
				if (operation instanceof MergeClsesOperation || operation instanceof MergeSlotsOperation) {
					_todoList.changeList(_currentSuggestions);
				}
			}
		});
  }

	private KeyAdapter getSearchKeyAdapter() {
		return new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				//if (searchTextField.getText().length() == 0) {
				//	_todoList.changeList(SuggestionsAndConflicts.getTodoListInPriorityOrder());
				//} else {
				_todoList.filterOperations(SuggestionsAndConflicts.getTodoListInPriorityOrder(), searchTextField.getText());
				//}
			}
		};
	}

	/**
	 * Creates a focus listener for the text boxes to remove the default search text on focus and
	 * add it back in if nothing has been searched and focus has been lost.
	 */
	private FocusListener getSearchFocusListener() {
		return new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (searchTextField.getText().equals(DEFAULT_FILTER_TEXT)) {
					searchTextField.setText("");
				}
			}

			public void focusLost(FocusEvent e) {
				if (searchTextField.getText().length() == 0) {
					searchTextField.setText(DEFAULT_FILTER_TEXT);
				}
			}
		};
	}

  private void createActions () {
  	_createAction = new CreateOperationAction ("Create operation");
  	_viewAction = new ViewOperationAction ("View operation");
  	_removeAction = new RemoveOperationAction ("Remove operation");
	_showSourcesAction = new ShowSourcesAction ("Show sources");
	_anchorPromptAction = new AnchorPromptAction ("Invoke AnchorPrompt with selected anchors");
  }

  private void initializeSuggestionList () {
    _currentSuggestions = SuggestionsAndConflicts.getTodoListInPriorityOrder ();
    removeUneditable (_currentSuggestions);
    _todoList = new ActionListPane (_currentSuggestions, _viewAction);
 }

  private void removeUneditable (Collection c) {
		if (c == null) {
			return;
		}

    Collection toRemove = new ArrayList();
    Iterator i = c.iterator();
    while (i.hasNext()) {
      Operation next = (Operation)i.next();
			if (!next.canView()) {
        toRemove.add (next);
    }
		}
    c.removeAll(toRemove);
  }

  private JComponent createReasonBox () {
	final JList reasonList = ComponentFactory.createList (null);
    reasonList.setCellRenderer(new ActionRenderer());
    _todoList.addActionListSelectionListener(new ListSelectionListener () {
        public void valueChanged(ListSelectionEvent e) {
			setReason (reasonList);
      }
    });
    return ComponentFactory.createScrollPane (reasonList);
  }

  private void setReason (JList reasonList) {
  	Action a = _todoList.getFirstSelection();
	if (a == null) {
    	Collection emptyList = new ArrayList ();
      	ComponentUtilities.setListValues(reasonList, emptyList);
     	reasonList.repaint();
		} else {
      Collection exp = ((Operation)a).getReason();
      ComponentUtilities.setListValues(reasonList, exp);
      reasonList.repaint();
   }
 }

  public void addSelectionListener () {
   	_todoList.addSelectionListener();
  }
 
  /** 
   * Public extension point to add a header button to this user interface.
	 * 
   * @param action
   */
  public void addHeaderButton(AbstractAction action) {
	  getLabeledComponent().addHeaderButton(action);
  }

  private void addButtons (LabeledComponent c) {
    c.addHeaderButton(_viewAction);
    c.addHeaderButton(_createAction);
    c.addHeaderButton(_removeAction);
		if (PromptTab.merging()) {
    		c.addHeaderButton (_showSourcesAction);
		}
	c.addHeaderButton (_anchorPromptAction);
		//c.add(searchTextField);
    disableButtonsForEmptyList ();
  }

  public void postChange () {
   	postChange (null);
  }

  public void postChange (boolean  b) {
   	postChange (null);
  }

  public void postChange (Operation editedOperation) {
  	_currentSuggestions = SuggestionsAndConflicts.getTodoListInPriorityOrder();
		if (editedOperation != null) {
    	setEditedOperationAtOldIndex (editedOperation);
		}
	//_todoList.changeList(_currentSuggestions);
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
		if (_currentSuggestions == null || _currentSuggestions.size() == 0) {
    	enabled = false;
		}
    _viewAction.setEnabled(enabled);
    _createAction.setEnabled (true);
    _removeAction.setEnabled (enabled);
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

  private  class CreateOperationAction extends AbstractAction {

     public CreateOperationAction(String prompt) {
       super(prompt, Icons.getCreateIcon());
     }

     public void actionPerformed(ActionEvent e) {
			JDialog dialog = new JDialog(PromptTab.getMainWindow(), "Create Operation", true);
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
			if (selection == null) {
				return;
			}
        PromptTab.getTabComponent().showSourcesDialog (getSourceArgs (selection.getArgs()), TabComponent.RIGHT_FRAME);
     }
     
     private Object [] getSourceArgs (ActionArgs args) {
     	Collection result = new ArrayList();
     	KnowledgeBase targetKb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
     	Iterator i = args.toCollection().iterator();
      	while (i.hasNext()) {
      		Object next = i.next();
				if (!(next instanceof edu.stanford.smi.protege.model.Frame)) {
					continue;
				}
      		edu.stanford.smi.protege.model.Frame nextFrame = (edu.stanford.smi.protege.model.Frame)next;
      		if (nextFrame.getKnowledgeBase().equals (targetKb)) {
      			Collection sources = Mappings.getSources(nextFrame);
					if (sources != null && !sources.isEmpty()) {
      				result.addAll(sources);
					}
				} else {
      			result.add (next);
       	}
			}
      	return result.toArray();
     }
  }

  private  class ViewOperationAction extends AbstractAction {

	 public ViewOperationAction(String prompt) {
	  super(prompt, Icons.getViewIcon());
	}

	public void actionPerformed(ActionEvent e) {
	   Action selection = _todoList.getFirstSelection();
			if (selection == null) {
				return;
			}

	   _oldIndex = _todoList.getSelectedIndex ();

			JDialog dialog = new JDialog(PromptTab.getMainWindow(), "Edit Operation", true);
	   CreateNewOperationPane panel = new CreateNewOperationPane ((Operation)selection, dialog);
	   showDialog (dialog, panel);
	   _todoList.setSelectedRow (_oldIndex);
	}
  }

  private  class AnchorPromptAction extends AbstractAction {

	 public AnchorPromptAction(String prompt) {
	   super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/Anchor.jpg"));
	}

	public void actionPerformed(ActionEvent e) {
	   Collection selections = _todoList.getSelection();
	   if (selections == null) {
			return;
	   }
	   
	   Collection anchors = createAnchors (selections);
	   
	   if (anchors.size() < 2) {
				Warning.inform("<html><center>Not enough anchors selected. <p> " + "(Need at least 2 and only Merge operations count as anchors)</center></html>");
			return;
	   }	  

	   Collection result = Analysis.analyze (anchors);
	   
	   addResultsToSuggestions (result);

	   _todoList.setSelectedRow (0);
	}
	
	private void addResultsToSuggestions (Collection result){
		Iterator i = result.iterator();
		while (i.hasNext()) {
	 		ScoreTableElement next = (ScoreTableElement)i.next();
	 		Operation suggestion = MergeFramesOperation.selectMergeOperation(next.getFirstElement(), next.getSecondElement(), new AnchorPromptResultExplanation ());
	 		suggestion.setPriority(SuggestionsAndConflicts.getCurrentTodoPriority() + 1);
		 	SuggestionsAndConflicts.addSuggestions(CollectionUtilities.createCollection (suggestion), true);
		}
	}

	private Collection createAnchors (Collection selections){
	   	Iterator i = selections.iterator();	   
		Collection anchors = new ArrayList();
		while (i.hasNext()) {
			Operation next = (Operation)i.next();
		 	if (next instanceof MergeClsesOperation) {
				anchors.add (new AnchorPair ((Cls)((MergeClsesOperation)next).getArgs().getArg(0), (Cls)((MergeClsesOperation)next).getArgs().getArg(1)));
		 	}
		}
		return anchors;
	}

  }

  private  class RemoveOperationAction extends AbstractAction {
     public RemoveOperationAction(String prompt) {
       super(prompt, Icons.getDeleteIcon());
    }

    public void actionPerformed(ActionEvent e) {
      Action selected = _todoList.getFirstSelection();
      int oldIndex = _todoList.getSelectedIndex();
      SuggestionsAndConflicts.removeSuggestionFromList ((Operation)selected, false);
			if (_todoList.getNumberOfRows() <= oldIndex) {
      	_todoList.setSelectedRow(_todoList.getNumberOfRows() - 1);
			} else {
      	_todoList.setSelectedRow(oldIndex);
    }
  }
	}

}
