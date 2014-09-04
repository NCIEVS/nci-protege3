 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.conflict;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class CycleConflict extends Conflict {
  static final int CYCLE_CONFLICT_ARITY = 2;

  public CycleConflict (Cls f, Cls p) {
    super (CYCLE_CONFLICT_ARITY, null);
    _name = "cycle";
    _prettyName = "cycle";
    _shortName = "Multiple paths from";
    _connectorString = "to parent";
    _args.setArg(0, f);
    _args.setArg(1, p);
    Collection parents = Util.getDirectSuperclasses(f);
    if (parents != null && parents.size() > 1) {
      Iterator i = parents.iterator();
      _solutions = new ArrayList();
      while (i.hasNext()) {
        Operation removeParent = new RemoveParentOperation (f, (Cls)i.next(), new CreatesCycle());
        _solutions.add (removeParent);
		SuggestionsAndConflicts.addSuggestions(CollectionUtilities.createCollection(removeParent));
      }
    }
    addConflictItSolvesToOperation ();
  }

  public String toString () {
    return "Multiple paths from " + _args.getArg (0) + " to parent " + _args.getArg (1);
  }

}

