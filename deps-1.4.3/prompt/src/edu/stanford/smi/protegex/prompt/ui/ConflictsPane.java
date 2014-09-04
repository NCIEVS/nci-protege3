 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;
import edu.stanford.smi.protegex.prompt.conflict.Conflict;
import edu.stanford.smi.protegex.prompt.operation.Operation;

public class ConflictsPane extends ListSplitPane {
    private Collection _currentConflicts;

    private ActionListPane _conflictList = null;
    private ActionListPane _solutionList = null;
    private JComponent _explanationBox = null;

    private static final Collection _emptyCollection = new ArrayList();

    static private  int [] _columnSizes = new int [] {150, 190, 190};
    static private  String [] _columnNames = new String [] {"Conflict", "Arg1", "Arg2"};


    ConflictsPane() {
      createConflictList();
      createExplanationBox ();
      createSolutionsList();

      _conflictList.add (_explanationBox, BorderLayout.SOUTH);
      createListSplitPane ("Current conflicts", _conflictList,
      						"Suggested solutions for selected conflict", _solutionList, BOTTOM, true);
    }

	private void createConflictList () {
      _currentConflicts = SuggestionsAndConflicts.getConflictsListInPriorityOrder();
      _conflictList = new ActionListPane (_currentConflicts, _columnSizes, _columnNames, null);
    }

    private void updateCurrentConflicts() {
      _currentConflicts = SuggestionsAndConflicts.getConflictsListInPriorityOrder();
      _conflictList.changeList(_currentConflicts);
    }

    private void createExplanationBox () {
    // maybe change to text box later (?? how to set the renderer??)
        final ExplanationPane explanationPane = new ExplanationPane();
        _conflictList.addActionListSelectionListener (new ListSelectionListener () {
           	public void valueChanged (ListSelectionEvent e) {
             	Action a = _conflictList.getFirstSelection();
                Conflict explanation;
                if (a == null)
                	explanation = null;
                else
                	explanation = (Conflict)a;
               	explanationPane.setValue (explanation);
            }
        });
        _explanationBox = ComponentFactory.createScrollPane(explanationPane);
    }

   private void createSolutionsList(){
	  _solutionList = new ActionListPane (_emptyCollection, null);
      _conflictList.addActionListSelectionListener(new ListSelectionListener () {
        	public void valueChanged(ListSelectionEvent e) {
             	Action a = _conflictList.getFirstSelection();
                Collection solutions;
                if (a == null)
                	solutions = _emptyCollection;
                else
                	solutions = getCurrentSolutions ((Conflict)a);
                _solutionList.changeList(solutions);
        	}
		});
    }


    private Collection getCurrentSolutions(Conflict selectedConflict) {
      return selectedConflict.getSolutions();
    }

    public void postChange () {
        updateCurrentConflicts ();
    }

       public void postChange (Operation o) {}

  public String toString () {
    return "ConflictsPane";
  }

  public void addSelectionListener () {}

  public class ExplanationPane extends JPanel {
  	JList _content;
    final private int NUMBER_OF_LINES = 3;
    ExplanationRenderer _renderer = new ExplanationRenderer (NUMBER_OF_LINES);

    ExplanationPane () {
    	_content = ComponentFactory.createList (null);
        _content.setCellRenderer(_renderer);

    	setLayout (new BorderLayout ());
        add (_content, BorderLayout.CENTER);
    }

    public void setValue (Conflict c) {
        if (c != null) {
        	Collection listValues = new ArrayList ();
        	for (int i = 0; i < NUMBER_OF_LINES; i++) {
             	listValues.add (c);
            }
            ComponentUtilities.setListValues (_content, listValues);
        } else {
         	ComponentUtilities.clearListValues (_content);
        }
    }

  }
}

