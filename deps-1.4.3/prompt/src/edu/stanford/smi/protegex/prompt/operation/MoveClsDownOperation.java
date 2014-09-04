 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class MoveClsDownOperation extends MoveDownOperation {
  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [MOVE_OPERATION_ARITY];

  public MoveClsDownOperation (Frame frame) {
    super (frame);
    setDefaultParameters ();
    initialize ();
  }

  public MoveClsDownOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    setDefaultParameters ();
    initialize ();
  }

  public MoveClsDownOperation (Frame frame, boolean copySlots) {
    super (frame);
   	_copySlots = copySlots;
    initialize ();
  }

  public MoveClsDownOperation (Frame frame, boolean copySlots, boolean copyEverythingRequired) {
    super (frame);
   	_copySlots = copySlots;
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  private void initialize () {
    _name = "shallow-move-class-down";
    _prettyName = "move class down";
    _shortName = "move down";
  }

  protected static String checkOtherReferences (Frame oldFrame) {
	String subclassesResult = checkSubclasses ((Cls)oldFrame);
	if (subclassesResult != null) {
	  return subclassesResult;
	}
	
	String instancesResult = checkInstances ((Cls)oldFrame);
	if (instancesResult != null) {
	  return instancesResult;
	}
	
	return null;
  }

  protected static String checkSubclasses (Cls oldFrame) {
	Collection subclasses = oldFrame.getDirectSubclasses();
	Iterator i = subclasses.iterator();
	while (i.hasNext()) {
	  Cls next = (Cls)i.next();
	  String warning = "Class " + next + ", which is a subclass of class " + oldFrame + ", is in a different included project.";
	  String checkResult = checkRequiredFrame (oldFrame, next, warning);
	  if (checkResult != null) return checkResult;
	}
	return null;
  }

  protected static String checkInstances (Cls oldFrame) {
	Collection instances = oldFrame.getDirectInstances();
	Iterator i = instances.iterator();
	while (i.hasNext()) {
	  Frame next = (Frame)i.next();
	  String warning = "Frame " + next + ", which is an instance of class " + oldFrame + ", is in a different included project.";
	  String checkResult = checkRequiredFrame (oldFrame, next, warning);
	  if (checkResult != null) return checkResult;
	}
	return null;
  }


  protected void moveRequiredFrames (Frame oldFrame) {
  	moveInstances ((Cls)oldFrame);
    moveTree ((Cls)oldFrame);
	moveTemplateSlots ((Cls)oldFrame, (Cls)_newFrame);
  }

  protected void  moveInstances (Cls cls) {
  	Collection instances = new ArrayList (cls.getDirectInstances());
    if (instances == null || instances.size() == 0) return;
   	Iterator i = instances.iterator();
    while (i.hasNext()) {
        Instance next = (Instance)i.next();
        MoveDownOperation.createOperation (next, _copySlots, _copyEverythingRequired).performOperation ();
    }
  }

  protected void moveTree (Cls cls) {
  	Collection subclasses = new ArrayList(cls.getDirectSubclasses());
    if (subclasses == null || subclasses.size() == 0) return;
    Iterator i = subclasses.iterator();
    while (i.hasNext())
        MoveDownOperation.createOperation ((Frame)i.next(), _copySlots, _copyEverythingRequired).performOperation ();
  }

   private void moveTemplateSlots (Cls oldFrame, Cls newFrame) {
     Collection oldSlots = new ArrayList(oldFrame.getTemplateSlots());

     KnowledgeBase sourceKb = oldFrame.getKnowledgeBase();

     if (oldSlots == null) return;

     Iterator i = oldSlots.iterator();
     Slot next, nextCopy;

     while (i.hasNext()) {
       next = (Slot) i.next();
       if (moveSlot (next, oldFrame))
       		PromptTab.addToQueue(new MoveSlotDownOperation (next, _copyEverythingRequired));
     }
   }

   private boolean moveSlot (Slot slot, Frame oldFrame) {
    	if (!_copySlots) return false;
        Collection clses = slot.getDirectDomain();
        Iterator i = clses.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            if (!next.equals(oldFrame))  {
            	return false;
            }
        }
        return true;
   }


}
