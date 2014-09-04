 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.conflict;

import java.util.Collection;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.prompt.explanation.IdenticalNames;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.operation.RenameFrameOperation;

public class DuplicateFrameNamesConflict extends Conflict {
  static final int DUPLICATE_NAMES_CONFLICT_ARITY = 2;

  DuplicateFrameNamesConflict (Frame f1, Frame f2, Collection solutions) {
    super (DUPLICATE_NAMES_CONFLICT_ARITY, solutions);
    _name = "duplicateFrameNames";
    _prettyName = "duplicate frame names";
    _shortName = "Duplicate frame names: ";
    _args.setArg (0, f1);
    _args.setArg (1, f2);
    if (solutions == null && f1.getClass() == f2.getClass()) {
      solutions = CollectionUtilities.createCollection
                    (MergeFramesOperation.selectMergeOperation (f1, f2, new IdenticalNames ()));
      solutions.add (RenameFrameOperation.selectRenameOperation (f1, new IdenticalNames()));
      solutions.add (RenameFrameOperation.selectRenameOperation (f2, new IdenticalNames()));
    }
    addConflictItSolvesToOperation ();
  }

  public String toString () {
    return "Duplicate frame names: " + _args.getArg(0) + " and " + _args.getArg(1);
  }

 }

