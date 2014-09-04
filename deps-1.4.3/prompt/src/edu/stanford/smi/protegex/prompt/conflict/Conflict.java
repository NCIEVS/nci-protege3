 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.conflict;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.operation.*;

public class Conflict extends Action {
  Collection _solutions;
  boolean _alwaysDisplaySolutions = true;

  Conflict (int ar, Collection solutionSet) {
    super (ar);
//    _arity = ar;
//    _args = new Object [ar];
    _priority = SuggestionsAndConflicts.getCurrentTodoPriority();
    _solutions = solutionSet;
    if (solutionSet == null)
    	_solutions = new ArrayList();
//    addConflictItSolvesToOperation ();
  }

  protected void addConflictItSolvesToOperation () {
    if (_solutions != null) {
      Iterator i = _solutions.iterator();
      Action next;
      while (i.hasNext()) {
        next = (Action)i.next();
        if (next instanceof Operation)
          ((Operation)next).addConflictItSolvesToOperation (this);
      }
    }
  }

  static public Conflict danglingReference (Frame from, Slot slot, Frame to, Collection solutions) {
    return new DanglingReferenceConflict (from, slot, to, solutions);
  }

  static public Conflict referenceToIncludingProject (Frame from, Slot slot, Frame to) {
    return new ReferenceToIncludingProjectConflict (from, slot, to);
  }

  static public Conflict referenceToIncludingProject (Frame from, Slot slot, Frame to, Facet facet) {
    return new ReferenceToIncludingProjectConflict (from, slot, to, facet);
  }

  static public Conflict cycle (Cls f, Cls p) {
    return new CycleConflict (f, p);
  }

  static public Conflict duplicateFrameNames (Frame f1, Frame f2, Collection solutions) {
    return new DuplicateFrameNamesConflict (f1, f2, solutions);
  }

  public void removeClsToClsReference (Cls from, Slot forSlot) {
  }

  public Collection getSolutions () {return _solutions;}

  public void addSolution (Operation o) {
  	_solutions.add (o);
  }

  public void removeSolution (Operation o) {
  	_solutions.remove (o);
  }
}


