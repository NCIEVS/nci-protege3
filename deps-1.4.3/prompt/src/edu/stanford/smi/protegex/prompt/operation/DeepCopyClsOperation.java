 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;

public class DeepCopyClsOperation extends DeepCopyFrameOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public DeepCopyClsOperation (Frame frame) {
    super (frame);
    setDefaultParameters ();
    initialize();
  }

//  public DeepCopyClsOperation (Frame frame, boolean copyInstances, boolean copyTree) {
  public DeepCopyClsOperation (Frame frame, TraversalDirective td) {
    super (frame, td);
    initialize();
  }

  private void initialize (){
    _name = "deep-copy-class";
    _prettyName = "copy class";
    _shortName = "copy";
  }

  public void removeAlternativeSuggestionsFromList () {
    Collection currentRelatedOperations = Mappings.getCurrentOperations((Frame)_args.getArg (0));
    if (currentRelatedOperations != null && currentRelatedOperations.size() > 0) {
      Iterator i = new ArrayList(currentRelatedOperations).iterator();
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