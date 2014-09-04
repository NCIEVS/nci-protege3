 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class RemoveParentOperation extends Operation {
  static final int REMOVE_PARENT_OPERATION_ARITY = 2;
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [REMOVE_PARENT_OPERATION_ARITY];
  boolean _fromListener = false;


  public RemoveParentOperation () {
    super (REMOVE_PARENT_OPERATION_ARITY);
    initialize(null, null, false);
  }


  public RemoveParentOperation (Cls child, Cls parent) {
    super (REMOVE_PARENT_OPERATION_ARITY);
    initialize (child, parent, false);
  }

  public RemoveParentOperation (Cls child, Cls parent, boolean fromListener) {
    super (REMOVE_PARENT_OPERATION_ARITY);
    initialize (child, parent, fromListener);
  }

  public RemoveParentOperation (Cls child, Cls parent, Explanation exp) {
    super (REMOVE_PARENT_OPERATION_ARITY, exp);
    initialize(child, parent, false);
  }

  public void initialize (Cls child, Cls parent, boolean fromListener) {
    _name = "remove-parent";
    _prettyName = "remove parent";
    _connectorString = "from";
    _args.setArg (0, parent);
    _args.setArg (1, child);
    _newFrame = child;
    _canView = false;
    _fromListener = fromListener;
  }

  public void actualOperation (){
    Cls parent = (Cls) _args.getArg (0);
    Cls child = (Cls) _args.getArg (1);

    if (!_fromListener)
       child.removeDirectSuperclass(parent);

    removeAlternativesFromSuggestionList ();
    }

    private void removeAlternativesFromSuggestionList () {
     	Collection currentRemoveParentOperations =
        		SuggestionsAndConflicts.getSuggestionsOfType(_name);
		if (currentRemoveParentOperations != null && currentRemoveParentOperations.size() >0) {
        	Collection currentRemoveParentOperationsToModify =
            	new ArrayList (currentRemoveParentOperations);
			Iterator i = currentRemoveParentOperationsToModify.iterator();
            Collection alternatives = new ArrayList();
            while (i.hasNext()) {
             	RemoveParentOperation next = (RemoveParentOperation)i.next();
                if (((Cls)_args.getArg (1)).equals (next._args.getArg (1)))
                	alternatives.add (next);
            }
			if (alternatives.size() == 1)
            	SuggestionsAndConflicts.removeSuggestionFromList
                	((Operation)CollectionUtilities.getFirstItem(alternatives), true);
        }
    }

/*
  static public void createValuesWidgetsImplementation () {
    _argumentWidgets[0] = new SelectClsWidget ("Ontology for the superclass", 0);
    _argumentWidgets[1] = new SelectClsWidget ("Ontology for the subclass", 1);

  }


  static public JPanel createActionBox () {
     createValuesWidgetsImplementation();
    return Operation.createActionBox (_argumentWidgets);
  }


  public JPanel createEditBox () {
     createValuesWidgetsImplementation();
    return createEditBox (_argumentWidgets);
  }

  public void collectData () {
    super.collectData (_argumentWidgets);
  }
*/
}
