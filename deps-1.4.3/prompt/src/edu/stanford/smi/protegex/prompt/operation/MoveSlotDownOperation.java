 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class MoveSlotDownOperation extends MoveDownOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public MoveSlotDownOperation () {
    super ();
    initialize ();
  }

  public MoveSlotDownOperation (Frame frame, boolean copyEverythingRequired) {
    super (frame);
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  public MoveSlotDownOperation (Frame frame) {
    super (frame);
    initialize ();
  }

  public MoveSlotDownOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    initialize ();
  }

  private void initialize () {
    _name = "shallow-move-slot-down";
    _prettyName = "move slot down";
    _shortName = "move down";
  }

  protected static String checkOtherReferences (Frame oldFrame) {
	String subslotsResult = checkSubslots ((Slot)oldFrame);
	return subslotsResult;
  }

  protected static String checkSubslots (Slot oldFrame) {
	Collection subslots = oldFrame.getDirectSubslots();
	Iterator i = subslots.iterator();
	while (i.hasNext()) {
	  Slot next = (Slot)i.next();
	  String warning = "Slot " + next + ", which is a subslot of slot " + oldFrame + ", is in a different included project.";
	  String checkResult = checkRequiredFrame (oldFrame, next, warning);
	  if (checkResult != null) return checkResult;
	}
	return null;
  }

  protected void moveRequiredFrames (Frame oldFrame) {
	Slot slot = (Slot)oldFrame;
	Collection clses =  slot.getDirectDomain();
	if (clses != null) {
		Iterator i = clses.iterator();
		while (i.hasNext()) {
			Cls next = (Cls)i.next();
			MoveDownOperation.createOperation (next, _copySlots, _copyEverythingRequired).performOperation ();
		}
	}
  }
}
