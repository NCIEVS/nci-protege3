 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class MoveSlotUpOperation extends MoveUpOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public MoveSlotUpOperation () {
    super ();
    initialize ();
  }

  public MoveSlotUpOperation (Frame frame, boolean copyEverythingRequired) {
    super (frame);
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  public MoveSlotUpOperation (Frame frame) {
    super (frame);
    initialize ();
  }

  public MoveSlotUpOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    initialize ();
  }
  
  protected static String checkOtherReferences (Frame oldFrame) {
	String superslotsResult = checkSuperslots ((Slot)oldFrame);
	if (superslotsResult != null) {
	  return superslotsResult;
	}
	return null;
  }

  protected static String checkSuperslots (Slot oldFrame) {
	Collection superslots = oldFrame.getDirectSuperslots();
	Iterator i = superslots.iterator();
	while (i.hasNext()) {
	  Slot next = (Slot)i.next();
	  String warning = "Slot " + next + ", which is a superslot of slot " + oldFrame + ", is in a different included project.";
	  String checkResult = checkRequiredFrame (oldFrame, next, warning);
	  if (checkResult != null) return checkResult;
	}
	return null;
  }

  private void initialize () {
    _name = "shallow-move-slot-up";
    _prettyName = "move slot up";
    _shortName = "move up";
  }

}
