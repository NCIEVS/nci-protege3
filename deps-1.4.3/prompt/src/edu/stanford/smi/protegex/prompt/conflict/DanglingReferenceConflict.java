 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.conflict;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class DanglingReferenceConflict extends Conflict {
  static final int DANGLING_REFERENCE_CONFLICT_ARITY = 3;

  DanglingReferenceConflict (Frame from, Slot slot, Frame to, Collection solutions) {
    super (DANGLING_REFERENCE_CONFLICT_ARITY, solutions);
    _name = "danglingReference";
    _prettyName = "dangling reference";
    _shortName = "Not defined";
    _connectorString = "referenced by";
    _connectorString2 = "at";

    _args.setArg(0, to);
    _args.setArg(1, slot);
    _args.setArg(2, from);  // can be null

	addStandardSolutions ();
    addConflictItSolvesToOperation ();
  }

  protected void addStandardSolutions () {
    Operation standardSolution = null;
    Explanation exp = ReferencedBy.selectExplanation ((Frame)_args.getArg(0),
                                            			(Slot)_args.getArg(1),
                                                        (Frame)_args.getArg(2),
                                                        false);
    if (PromptTab.merging() || PromptTab.mapping() || PromptTab.extracting())
    	standardSolution = KeepFrameOperation.createOperation ((Frame)_args.getArg(0), exp);

    if (standardSolution != null && !_solutions.contains(standardSolution)) {
      _solutions.add(standardSolution);

      Collection currentRelatedOperations = Mappings.getCurrentOperations ((Frame)_args.getArg(0));

      if (currentRelatedOperations == null || currentRelatedOperations.size() == 0) 
	      SuggestionsAndConflicts.addSuggestions
          		(CollectionUtilities.createCollection(standardSolution));
    }
  }

  public String toString () {
    return _args.getArg(0) + " referenced by " + _args.getArg(1) + " at " +
           DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(2)) + " is not defined";
  }

  public ActionArgs getShortArgs () {
  	ActionArgs newArgs = new ActionArgs (1);
    newArgs.setArg (0, _args.getArg(0));
	return newArgs;
  }

  public void removeClsToClsReference (Cls from, Slot forSlot) {
    if (_args.getArg(2).equals (from) && _args.getArg(1).equals(forSlot))
      SuggestionsAndConflicts.removeConflictFromList (this);
  }

}

