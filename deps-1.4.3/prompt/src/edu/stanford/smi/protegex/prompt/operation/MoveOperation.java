 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MoveOperation extends Operation {
  static final int MOVE_OPERATION_ARITY = 1;
  protected static KnowledgeBase _includedKb  = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.INCLUDED_PROJECT_INDEX);
  protected static KnowledgeBase _includingKb = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.INCLUDING_PROJECT_INDEX);
  protected static HashMap _allowedToMove = new HashMap (ProjectsAndKnowledgeBases.getTargetKnowledgeBase().getFrameCount()); //map from frames to a collection of frames on which

  protected static final int UP = 0;
  protected static final int DOWN = 1;
  public String allArgumentsValid () {
	Frame oldFrame = (Frame) _args.getArg(0);
	int direction = (this instanceof MoveUpOperation) ? UP : DOWN;
	return  requiredFramesValid (oldFrame, direction);
  }

  protected static String requiredFramesValid (Frame oldFrame, int direction) {
	Object existingValue = _allowedToMove.get(oldFrame);
	if (existingValue != null) {
	  if (existingValue instanceof Collection)
		return null;
	  else
		return "Cannot move " + oldFrame;
	}

	 _allowedToMove.put(oldFrame, new HashSet());

	return checkValidFrames(oldFrame, direction);
  }
  
  protected static String checkValidFrames (Frame oldFrame, int direction) {
  	if (direction == UP)
  		return MoveUpOperation.checkValidFrames(oldFrame);
  	else
  		return MoveDownOperation.checkValidFrames(oldFrame);
  }

  protected static void addRequiredFrame (Frame oldFrame, Frame addFrame) {
	if (addFrame.isIncluded() || Util.isSystem(addFrame)) return;
	Collection existingValues = (Collection)_allowedToMove.get(oldFrame);
	existingValues.add(addFrame);
	existingValues.addAll((Collection)_allowedToMove.get(addFrame));
	_allowedToMove.put(oldFrame, existingValues);
  }

  public MoveOperation (int arity) {
    super (MOVE_OPERATION_ARITY);
  }

  public MoveOperation (int arity, Explanation exp) {
    super (MOVE_OPERATION_ARITY, exp);
  }

}

