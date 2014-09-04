 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.TraversalDirective;
import edu.stanford.smi.protegex.prompt.explanation.Explanation;
import edu.stanford.smi.protegex.prompt.util.Util;

public class MoveUpOperation extends MoveOperation {
  protected Collection _willMoveWithIt = new ArrayList();


  public MoveUpOperation () {
    super (MOVE_OPERATION_ARITY);
    setDefaultParameters ();
  }

  public MoveUpOperation (Frame frame) {
    super (MOVE_OPERATION_ARITY);
    _args.setArg (0, frame);
    setDefaultParameters ();
  }

  public MoveUpOperation (Frame frame, Explanation exp) {
    super (MOVE_OPERATION_ARITY, exp);
    _args.setArg (0, frame);
    setDefaultParameters ();
  }

//  protected static Operation createOperation (Frame f, boolean copySubclasses, boolean copyInstances, boolean everythingRequired) {
	protected static Operation createOperation (Frame f, TraversalDirective td, boolean everythingRequired) {
    	if (f instanceof Slot)
        	return new MoveSlotUpOperation ((Slot)f, everythingRequired);
        if (f instanceof Cls)
        	return new MoveClsUpOperation ((Cls)f, td.sameDirective(f), everythingRequired);
        else
                return new MoveInstanceUpOperation ((Instance)f, everythingRequired);
   }

  protected void setDefaultParameters () {
   	if (PromptTab.moving () && _traversalDirective.nullDirective()) {
//    	_copyInstances = false;
//        _copySubclasses = false;
		_traversalDirective = new TraversalDirective (null, false, false);
        _copySlots = true;
        _copyEverythingRequired = true;
    }
 }

  public void actualOperation (){
    Frame oldFrame = (Frame) _args.getArg(0);
    if (oldFrame.isIncluded() || Util.isSystem(oldFrame)) return;

    if (_copyEverythingRequired)
      moveRequiredFrames (oldFrame);
    _newFrame = moveFrame (oldFrame);
    moveDependentFrames (oldFrame, _newFrame);
    moveOptionalFrames (oldFrame, _newFrame);
  }
  
  protected void moveOptionalFrames (Frame oldFrame, Frame newFrame) {
  }

  protected void moveDependentFrames (Frame oldFrame, Frame newFrame) {
    moveAndSetOwnSlotValues (oldFrame, _newFrame);

//    checkReferences (_newFrame);
  }

  protected void moveRequiredFrames (Frame oldFrame) {
    moveType(oldFrame);
  }

  protected static String checkValidFrames (Frame oldFrame) {
    String otherResult = null;
    if (oldFrame instanceof Cls)
      otherResult = MoveClsUpOperation.checkOtherReferences(oldFrame);

    if (oldFrame instanceof Slot)
      otherResult = MoveSlotUpOperation.checkOtherReferences(oldFrame);

    if (otherResult != null) {
      _allowedToMove.put(oldFrame, new Boolean (false));
      return otherResult;
    }

    String typeResult = checkType (oldFrame);
    if (typeResult != null) {
      _allowedToMove.put(oldFrame, new Boolean (false));
      return typeResult;
    }

    String ownSlotResult = checkOwnSlots (oldFrame);
    if (ownSlotResult != null) {
      _allowedToMove.put(oldFrame, new Boolean (false));
      return ownSlotResult;
    }

     return null;
  }

  protected static String checkType (Frame oldFrame) {
    Cls type = ((Instance)oldFrame).getDirectType();
    String warning = "Frame " + type + ", which is a direct type for " + oldFrame + " is in a different included project.";
    return checkRequiredFrame (oldFrame, type, warning);
  }

  protected static String checkOwnSlots (Frame oldFrame) {
    Collection ownSlots = oldFrame.getOwnSlots();
    if (ownSlots == null || ownSlots.isEmpty()) return null;
	Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore (oldFrame.getKnowledgeBase());
    Iterator i = ownSlots.iterator();
    while (i.hasNext()) {
      Slot next = (Slot)i.next();
      if (ownSlotsToIgnore.contains (next)) continue;
      Collection values = oldFrame.getOwnSlotValues(next);
      if (values == null || values.isEmpty()) continue;

      Iterator j = values.iterator();
      while (j.hasNext()) {
        Object nextValue = j.next();
        if (nextValue instanceof Frame) {
          Frame nextFrame = (Frame)nextValue;
          String warning = "Frame " + nextFrame + ", which is a value for slot " + next + " for frame " + oldFrame + "is in a different included project.";
          String checkResult = checkRequiredFrame (oldFrame, nextFrame, warning);
          if (checkResult != null) return checkResult;
        }
      }
    }
    return null;
  }

  public static Collection getFramesThatWillBeMoved (Frame frame) {
    Object value = _allowedToMove.get(frame);
    if (value == null || value instanceof Boolean) {
      Log.getLogger().info("Should not be here");
      return null;
    }
    return (Collection) value;

  }

  protected static String checkRequiredFrame (Frame oldFrame, Frame frameToCheck, String warning) {
    if (frameToCheck.isIncluded() && _includedKb.getFrame (frameToCheck.getName()) == null)
         return warning;
    String checkResult = MoveOperation.requiredFramesValid(frameToCheck, UP);
    if (checkResult != null)
      return checkResult;
    addRequiredFrame (oldFrame, frameToCheck);
    return null;
  }

  private void moveType (Frame oldFrame) {
    Cls type = ((Instance)oldFrame).getDirectType();
    (new MoveClsUpOperation (type, new TraversalDirective (type, false, false), _copyEverythingRequired)).performOperation();
  }


//  private void checkReferences (Frame newFrame) {}

  protected void moveAndSetOwnSlotValues (Frame oldFrame, Frame newFrame) {
    Collection ownSlots = oldFrame.getOwnSlots();
    if (ownSlots == null || ownSlots.isEmpty()) return;
	Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore (oldFrame.getKnowledgeBase());
    Iterator i = ownSlots.iterator();
    while (i.hasNext()) {
      Slot next = (Slot)i.next();
      if (ownSlotsToIgnore.contains (next)) continue;
      Collection values = oldFrame.getOwnSlotValues(next);
      if (values == null || values.isEmpty()) continue;

      next = _includedKb.getSlot (next.getName());
      Iterator j = values.iterator();
      while (j.hasNext()) {
        Object nextValue = j.next();
        if (nextValue instanceof Frame) {
			MoveUpOperation.createOperation((Frame)nextValue,
				_traversalDirective.sameDirective ((Frame)nextValue), _copyEverythingRequired).performOperation();
//			MoveUpOperation.createOperation((Frame)nextValue,
//							  _copySubclasses, _copyInstances, _copyEverythingRequired).performOperation();
          newFrame.addOwnSlotValue(next, _includedKb.getFrame(((Frame)nextValue).getName()));
        }  else {
          //special-case slots with default values
          if (hasDefaultValuesOnly ((Instance)newFrame, next))
            newFrame.setOwnSlotValue(next, nextValue);
          else
            newFrame.addOwnSlotValue(next, nextValue);
        }
      }
    }
  }

  private boolean hasDefaultValuesOnly (Instance instance, Slot slot) {
    Cls type = instance.getDirectType();
    if (!type.hasTemplateSlot(slot)) {
      Log.getLogger().severe("Shouldn't be here");
      return false;
    }

    Collection defaultValues = type.getTemplateSlotDefaultValues(slot);
    Collection values = instance.getOwnSlotValues(slot);
    return defaultValues.containsAll(values) && values.containsAll(defaultValues);
  }

  protected Frame moveFrame (Frame oldFrame) {
    Frame newFrame;
    if (Util.isSystem(oldFrame))
     	return oldFrame;
    else {
    	newFrame = createFrame (oldFrame);
        oldFrame.setIncluded (true);
     }
     return newFrame;

   }

   protected Frame createFrame (Frame oldFrame) {
    Frame result;
    Cls type = ((Instance)oldFrame).getDirectType();
    type = _includedKb.getCls(type.getName());
    if (oldFrame instanceof Cls) {
      Collection superclasses =  Util.getDirectSuperclasses((Cls)oldFrame);

      Collection newSuperclasses = new ArrayList();
      Iterator i = superclasses.iterator();
      while (i.hasNext()) {
        Cls next = (Cls)i.next();
        newSuperclasses.add(_includedKb.getFrame(next.getName()));
      }
      result = _includedKb.createCls(oldFrame.getName(), newSuperclasses, type);

    } else if (oldFrame instanceof Slot)
      result = _includedKb.createSlot(oldFrame.getName(), type);
    else
      result = _includedKb.createInstance(oldFrame.getName(), type);
//    ReplaceReferences.replaceAllReferences(oldFrame, result, _kbToMoveFrom);
    return result;
   }

  private Frame getFrameInSource (Frame frame) {
  	return _includingKb.getFrame (frame.getName());
  }


}

