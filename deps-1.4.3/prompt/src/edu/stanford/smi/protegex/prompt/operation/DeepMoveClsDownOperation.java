 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;

public class DeepMoveClsDownOperation extends DeepMoveFrameDownOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public DeepMoveClsDownOperation (Frame frame) {
    super (frame);
    initialize();
  }

  public DeepMoveClsDownOperation (Frame frame, boolean copySlots, boolean copyEverythingRequired) {
    super (frame);
    _copyEverythingRequired = copyEverythingRequired;
    _copySlots = copySlots;
    initialize();
  }

  private void initialize (){
    _name = "deep-move-class";
    _prettyName = "move class";
    _shortName = "move";
  }

  public void removeAlternativeSuggestionsFromList () {
    Collection currentRelatedOperations = Mappings.getCurrentOperations((Frame)_args.getArg (0));
    if (currentRelatedOperations != null && currentRelatedOperations.size() > 0) {
      Iterator i = currentRelatedOperations.iterator();
      while (i.hasNext()) {
        Object next = i.next();
        if (next instanceof KeepClsOperation){
//          ((Operation)next).removeSolvedConflicts ();
          SuggestionsAndConflicts.removeSuggestionFromList((Operation)next, true);

        }
      }
    }
  }

}
