 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;

public class DeleteFrameOperation extends Operation {
  static final int DELETE_OPERATION_ARITY = 2;
  boolean _fromListener = false;


  public DeleteFrameOperation () {
    super (DELETE_OPERATION_ARITY);
    initialize(null, null, false);
  }


//  public DeleteFrameOperation (Frame frame, boolean fromListener) {
//    super (DELETE_OPERATION_ARITY);
//    initialize (frame, "", fromListener);
//  }

  public DeleteFrameOperation (Frame frame, String oldName, boolean fromListener) {
    super (DELETE_OPERATION_ARITY);
    initialize (frame, oldName, fromListener);
  }

  public void initialize (Frame frame, String oldName, boolean fromListener) {
    _name = "delete-frame";
    _prettyName = "delete frame";
    _newFrame = null;
    _args.setArg(0, frame);
    _args.setArg(1, oldName);
    _canView = false;
    _fromListener = fromListener;
  }

  public void actualOperation (){
//  	Util.removeFrame((Frame)_args[0]);
	Frame f = (Frame)_args.getArg(0);
    Collection sources = Mappings.getSources(f);
    if (sources != null && sources.size() > 0) {
     	Iterator i = sources.iterator();
        while (i.hasNext()) {
         	Frame next = (Frame)i.next();
            Mappings.removeWhatBecameOfIt(next);
        }
    }

   	Collection relatedOperations = Mappings.getCurrentOperations((Frame)_args.getArg(0));
    SuggestionsAndConflicts.removeAll(relatedOperations);
  }

  public String  toString (){
    String result;
//    result = getPriority() + ": ";
    result = PromptTab.getOperationsCount() + ": ";

    if (_prettyName != null)
       result += _prettyName.toUpperCase();
    else
       result += _name.toUpperCase();
    result += " ";
	result += _args.getArg(1);

    return result;
  }


}
