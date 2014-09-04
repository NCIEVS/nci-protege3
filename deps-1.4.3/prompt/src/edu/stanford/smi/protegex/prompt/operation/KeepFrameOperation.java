 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class KeepFrameOperation extends CopyOperation {
//  static private GetValueWidget [] _argumentWidgets;

  public KeepFrameOperation () {
    super ();
    initialize (null);
  }

  public KeepFrameOperation (Frame frame) {
    super ();
    initialize (frame);
  }

  public KeepFrameOperation (Frame frame, TraversalDirective td, boolean fromMerge) {
    super (td);
    _temporary = fromMerge;
    initialize (frame);
  }

  public KeepFrameOperation (Frame frame, Explanation exp) {
    super (exp);
    initialize (frame);
  }

  private void initialize (Frame frame) {
    _name = "shallow-copy-frame";
    _args.setArg (0, frame);
    _prettyName = _name;
    _shortName = "copy";
  }

  public static KeepFrameOperation createOperation (Frame frame, TraversalDirective td,
  													boolean fromMerge) {
    if (frame instanceof Cls)
      return new KeepClsOperation (frame, td, fromMerge);
    else if (frame instanceof Slot)
      return new KeepSlotOperation (frame, td, fromMerge);
    else if (frame instanceof Instance)
      return new KeepInstanceOperation (frame, td, fromMerge);
    return null;
  }

  public static KeepFrameOperation createOperation (Frame frame, Explanation exp) {
  	if (Mappings.getWhatBecameOfIt (frame) == null) {
    	if (frame instanceof Cls)
      		return new KeepClsOperation (frame, exp);
    	else if (frame instanceof Slot)
      		return new KeepSlotOperation (frame, exp);
		else if (frame instanceof Instance)
      		return new KeepInstanceOperation (frame, exp);
    }
    return null;
  }

   protected Cls copyDirectType (Frame oldFrame, KnowledgeBase sourceKb, boolean copyLevelOnly) {
     Cls type = ((Instance)oldFrame).getDirectType();

     if (oldFrame instanceof Cls && type.equals(oldFrame)) {
      _typeSameAsFrame = true;
      return Util.getStandardMetaclass (_targetKb);
     }

     Cls whatBecameOfType = (Cls) Mappings.getWhatBecameOfIt(type);
     if (whatBecameOfType == null) {
        Operation newOp = new KeepClsOperation ( true, type, _traversalDirective.nextDirective(type));
		newOp.setTraversalDirective(TraversalDirective.createCopyLevelOnlyDirective(type));
        newOp.performOperation();
        type = (Cls) Mappings.getWhatBecameOfIt(type);
     } else {
      	type = whatBecameOfType;
     }
     return type;
   }



}
