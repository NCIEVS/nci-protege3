 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.actionLists;

import java.util.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.conflict.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class SuggestionsAndConflicts {

  static ActionList _conflicts = new ActionList();
  static ActionList _todo = new ActionList();
  static Collection _pendingMoves = new HashSet(); //<action args: action>
  static TabComponent _win = PromptTab.getTabComponent();

  static public ArrayList getTodoListInPriorityOrder() {
    return _todo.listInPriorityOrder();
  }

  static public int getCurrentTodoPriority() {
    return _todo.getCurrentPriority();
  }

  static public void addConflicts (Collection c) {
    if (c!= null) Statistics.increaseTotalNumberOfConflictsDetected(c.size());

    _conflicts.addAll (c);
    if (PromptTab.merging())
    		((MergingTabComponent)getWindow()).conflictsListChanged (true);
  }

  static public void addSuggestions (Collection c) {
     addSuggestions (c, true);
  }

  static public Collection getSuggestionsOfType (String type) {
   	return _todo.getActionsOfType(type);
  }

  static public void addSuggestions (Collection c, boolean checkForDuplicates) {
  	if (c == null) return;

	int oldNumber = _todo.getNumberOfActionsInList();
    _todo.addAll (c, checkForDuplicates);
    int newNumber = _todo.getNumberOfActionsInList() - oldNumber;
    if (newNumber > 0)
    	Statistics.increaseTotalNumberOfSuggestions(newNumber);

    if (PromptTab.keepInQueue()) {
     	PromptTab.addToQueue(c);
    }
    postSuggestionsListChange (null);
  }

  static private void postSuggestionsListChange (Operation toSelect) {
    TabComponent win = getWindow();
    if (win != null)
      win.suggestionsListChanged (true, toSelect);
  }

  static private void postConflictsListChange (Operation toSelect) {
    TabComponent win = getWindow();
    if (win != null && PromptTab.merging())
    	((MergingTabComponent)win).conflictsListChanged(true);
  }

  static public void replaceSuggestion (Operation newOperation, Operation originalOperation) {
	_todo.remove(originalOperation);
    _todo.add (newOperation);
	postSuggestionsListChange(newOperation);
  }

  static public void incrementCurrentPriority() {
    _todo.incrementCurrentPriority();
    _conflicts.incrementCurrentPriority();
  }

  static public ActionList getConflictsList () { return _conflicts; }

  static public ActionList getSuggestionsList () { return _todo; }

  static public Collection getConflictsListInPriorityOrder () {
	Collection result = _conflicts.listInPriorityOrder();
    return result;
  }

  static public void removeAll (Collection c) {
    if (c == null || c.size() == 0) return;
    
    Collection lc = new ArrayList (c);

    Iterator i = lc.iterator();
    while (i.hasNext()) {
      Action next = (Action)i.next();
      removeActionFromList (next);
    }
	postSuggestionsListChange(null);
  }

  static private void removeActionFromList (Action a) {
    if (a instanceof Conflict)
      removeConflictFromList ((Conflict)a);
    if (a instanceof Operation)
      removeSuggestionFromList ((Operation)a);
    if (a instanceof Explanation)
      removeExplanationFromList ((Explanation) a);
  }

  static public Object removeConflictFromList (Conflict conflict) {
    Object r = _conflicts.remove (conflict);
    if (PromptTab.merging())
    		((MergingTabComponent)getWindow()).conflictsListChanged (true);
    return r;
  }

  static public Object removeSuggestionFromList (Operation operation) {
    return removeSuggestionFromList (operation, false);
  }

  static public Object removeExplanationFromList (Explanation exp) {
  	Operation op = exp.getOperation ();
    if (op == null) return null;

    op.removeExplanation (exp);
    getWindow().suggestionsListChanged (true);
  	return exp;
  }

  static public Object removeSuggestionFromList (Operation operation, boolean suggestionFollowed) {
  	Mappings.removeFromFrameActionsMap(operation);
    Object r = _todo.remove (operation);

    getWindow().suggestionsListChanged (true);
    if (r != null && suggestionFollowed) Statistics.increaseNumberOfSuggestionsFollowed();
	if (r instanceof Operation) {
    	removeSolvedConflicts ((Operation)r);
        postConflictsListChange (null);
    }
  	postSuggestionsListChange(null);
    return r;
  }

   private static void removeSolvedConflicts (Operation o) {
     Collection conflictsItSolves = o.getConflictsItSolves();

     if (conflictsItSolves != null) {
       Statistics.increaseNumberOfConflictSolutionsUsed(conflictsItSolves.size());
       Iterator i = conflictsItSolves.iterator();
       while (i.hasNext())
         removeConflictFromList ((Conflict)i.next());
     }
   }

  static public void changePriorityToCurrent (Action a) {
  	_todo.changePriorityToCurrent (a);
    postSuggestionsListChange (null);
  }

  static public void incrementPriorityOfCurrentAction (Operation a, int diff) {
    _todo.setPriority (a, getCurrentTodoPriority() + diff);
   	postSuggestionsListChange(a);
  }

  static private TabComponent getWindow () {
    if (_win == null)
      _win = PromptTab.getTabComponent();
    return _win;
  }

  static public Operation findInSuggestionsList (Operation a) {
    return (Operation)_todo.findValueInList (a);
  }

  static public Collection findValuesInSuggestionsList (Operation a) {
    return _todo.findValuesInList (a);
  }

  static public void clearAll () {
    _todo.clear();
    _conflicts.clear();
  }

  static public void addToDoListOperation (Operation newOperation, Operation originalOperation,
  											boolean fromUser) {
	if (originalOperation == null)
    	addToDoListOperation (newOperation, fromUser);
    else {
    	newOperation.setFromUser(fromUser);
    	replaceSuggestion (newOperation, originalOperation);
    }
   	postSuggestionsListChange(newOperation);
  }

  static public void addToDoListOperation (Operation a, boolean fromUser) {
  	incrementCurrentPriority();
	a.setPriority(getCurrentTodoPriority());
  	a.setFromUser(fromUser);

	Operation existing = findInSuggestionsList (a);
	if (existing != null)
		SuggestionsAndConflicts.changePriorityToCurrent(existing);
	else
		SuggestionsAndConflicts.addSuggestions(CollectionUtilities.createCollection(a));
  }

	public static void addToPendingMoves(Action action) {
		_pendingMoves.add(action);
	}
	
	public static void executePendingMoves () {
		Iterator i = _pendingMoves.iterator();
		while (i.hasNext() ){
			Action next = (Action)i.next();
			changePriorityToCurrent (next);
		}
		_pendingMoves.clear();
	}
	
	private static String createKey (Action action) {
		return action.getShortName() + action.getArgs().toString();
	}

}
