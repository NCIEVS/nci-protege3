 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.conflict.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MoveDownOperation extends MoveOperation {
  private KnowledgeBase _sourceKb;

  public MoveDownOperation () {
    super (MOVE_OPERATION_ARITY);
    setDefaultParameters ();
  }

  public MoveDownOperation (Frame frame) {
    super (MOVE_OPERATION_ARITY);
    _args.setArg (0, frame);
    setDefaultParameters ();
  }

  public MoveDownOperation (Frame frame, Explanation exp) {
    super (MOVE_OPERATION_ARITY, exp);
    _args.setArg (0, frame);
    setDefaultParameters ();
  }

  protected void setDefaultParameters () {
    if (PromptTab.moving () && _traversalDirective.nullDirective()) {
//    	_copyInstances = true;
//        _copySubclasses = true;
		_traversalDirective = new TraversalDirective (null, true, true);
        _copySlots = true;
        _copyEverythingRequired = true;
    }
 }

  public void actualOperation (){
    Frame oldFrame = (Frame) _args.getArg(0);
    if (Util.isSystem(oldFrame)) return;
    _sourceKb = oldFrame.getKnowledgeBase ();

    _newFrame = moveFrame (oldFrame);

    moveRequiredFrames (oldFrame);

    deleteFrame (oldFrame);
    moveBackReferences (_newFrame);
  }

  private void deleteFrame (Frame oldFrame) {

    String frameName = oldFrame.getName();
    Util.removeFrame(oldFrame);

	Frame frame = _sourceKb.getFrame(frameName);

    if (frame == null) return;

    // else
    Collection existingFrames = new ArrayList();
    Cls cls = (Cls)frame;
	existingFrames.addAll (cls.getInstances ());
	existingFrames.addAll (cls.getSubclasses ());

	Log.getLogger().severe ("Could not delete frame: " + DisplayUtilities.displayFrameWithAffiliation(oldFrame) +
    			". It has the following instances or subclasses: " + existingFrames);

  }

  protected static String checkValidFrames (Frame oldFrame) {
/*	String otherResult = null;
	if (oldFrame instanceof Cls)
	  otherResult = MoveClsDownOperation.checkOtherReferences(oldFrame);

	if (oldFrame instanceof Slot)
	  otherResult = MoveSlotDownOperation.checkOtherReferences(oldFrame);

	if (otherResult != null) {
	  _allowedToMove.put(oldFrame, new Boolean (false));
	  return otherResult;
	}
*/
	String backRefsResult = checkBackReferences (oldFrame);
	if (backRefsResult != null) {
	  _allowedToMove.put(oldFrame, new Boolean (false));
	  return backRefsResult;
	}

	 return null;
  }
  
  protected static String checkBackReferences (Frame oldFrame) {
	Frame frameInFullKb = _includingKb.getFrame(oldFrame.getName());
  	Collection backRefs = frameInFullKb.getReferences();
	Iterator i = backRefs.iterator();
	while (i.hasNext()) {
		Reference nextRef = (Reference)i.next();
		if (!nextRef.getSlot ().getFrameID().equals (Model.SlotID.DIRECT_SUBCLASSES) &&
			!nextRef.getSlot ().getFrameID().equals (Model.SlotID.DIRECT_TEMPLATE_SLOTS) &&
			!nextRef.getSlot ().getFrameID().equals (Model.SlotID.DIRECT_INSTANCES))  {
			Frame nextFrame = nextRef.getFrame();
			String warning = "Frame " + nextFrame + ", which refers to " + oldFrame + " is in another included project";
			String result = checkRequiredFrame (oldFrame, nextFrame, warning);
			if (result != null) return result;
		}
	}
	return null;
  }

  protected static String checkRequiredFrame (Frame oldFrame, Frame frameToCheck, String warning) {
	if (!frameToCheck.isIncluded()) return null;
	
	Frame frameInKbToMoveFrom = _includedKb.getFrame (frameToCheck.getName());
	if (frameToCheck.isIncluded() && frameInKbToMoveFrom == null)
		 return warning;
	String checkResult = MoveOperation.requiredFramesValid(frameInKbToMoveFrom, DOWN);
	if (checkResult != null)
	  return checkResult;
	addRequiredFrame (oldFrame, frameInKbToMoveFrom);
	return null;
  }



  protected void moveRequiredFrames (Frame oldFrame) {}

  private void moveBackReferences (Frame newFrame) {
	Collection backRefs = newFrame.getReferences ();
    Iterator i = backRefs.iterator();
    while (i.hasNext()) {
    	Reference nextRef = (Reference)i.next();
        if (nextRef.getFrame().isIncluded() &&
        	!nextRef.getSlot ().getName().equals (Model.Slot.DIRECT_SUBCLASSES) &&
        	!nextRef.getSlot ().getName().equals (Model.Slot.DIRECT_INSTANCES))  {
			Conflict conflict =
            	Conflict.referenceToIncludingProject(getFrameInSource(nextRef.getFrame()),
                									(Slot)getFrameInSource(nextRef.getSlot()), newFrame,
                                                    (nextRef.getFacet () == null) ? null : (Facet)getFrameInSource(nextRef.getFacet())  );
        	_currentFoundConflicts.add (conflict);
            if (_copyEverythingRequired)
            	PromptTab.addToQueue ((Operation)CollectionUtilities.getFirstItem (conflict.getSolutions()));
		}
    }
  }

  protected Frame moveFrame (Frame oldFrame) {
	Frame newFrame = _targetKb.getFrame (oldFrame.getName());
    if (Util.isSystem(oldFrame))
     	return newFrame;
    else
    	newFrame.setIncluded (false);

     return newFrame;

   }

  private Frame getFrameInSource (Frame frame) {
  	return _sourceKb.getFrame (frame.getName());
  }

   public static Operation createOperation (Frame f, boolean copySlots, boolean everythingRequired) {
    	if (f instanceof Slot)
        	return new MoveSlotDownOperation ((Slot)f, everythingRequired);
        if (f instanceof Cls)
        	return new MoveClsDownOperation ((Cls)f, copySlots, everythingRequired);
        else  {
			Cls type =  ((Instance)f).getDirectType();
            Frame mapping = Mappings.getWhatBecameOfIt(type);
            if (mapping != null)
            	type = (Cls)mapping;
            else
            	type = _targetKb.getCls (type.getName());
        	return new MoveInstanceDownOperation (f, type, everythingRequired);
        }
   }


}

