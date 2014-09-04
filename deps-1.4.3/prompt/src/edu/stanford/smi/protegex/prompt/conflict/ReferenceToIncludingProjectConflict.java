 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.conflict;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class ReferenceToIncludingProjectConflict extends Conflict {
  static final int DANGLING_REFERENCE_CONFLICT_ARITY = 4;

  ReferenceToIncludingProjectConflict (Frame from, Slot slot, Frame to) {
    super (DANGLING_REFERENCE_CONFLICT_ARITY, null);
    initialize (from, slot, to, null);
  }

  ReferenceToIncludingProjectConflict (Frame from, Slot slot, Frame to, Facet facet) {
    super (DANGLING_REFERENCE_CONFLICT_ARITY, null);
    initialize (from, slot, to, facet);
  }

  private void initialize (Frame from, Slot slot, Frame to, Facet facet) {
    _name = "referenceToIncludingProject";
    _prettyName = "reference to including project";
    _shortName = "Reference to including project";

    _connectorString = "references including project frame";
    _connectorString2 = "at slot";

    _args.setArg(0, from);
    _args.setArg(1, to);
    _args.setArg(2, slot);
    _args.setArg(3, facet);

	addStandardSolutions ();
  }

  protected void addStandardSolutions () {
    Operation standardSolution = MoveDownOperation.createOperation ((Frame)_args.getArg (0), true, true);
    standardSolution.addConflictItSolvesToOperation(this);

    if (!_solutions.contains(standardSolution)) {
      _solutions.add(standardSolution);
     SuggestionsAndConflicts.addSuggestions (CollectionUtilities.createCollection(standardSolution), true);
    }
  }

  public String toString () {
    return "In included: " + DisplayUtilities.displayFrameWithAffiliation((Frame)_args.getArg(0))
    + " in including " + DisplayUtilities.displayFrameWithAffiliation((Frame)_args.getArg(1)) + " through " +
           DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(2)) +
           " and facet: " + _args.getArg(3) +
    ", solutions: " + _solutions;
//    return "In included: " + _args.getArg(0) + " in including " + _args.getArg(1) + " through " +
//           DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(2));
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