 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class DeepMoveFrameDownOperation extends MoveDownOperation {

  private Frame _newFrame;
  private boolean _continueCopy = true;

  public DeepMoveFrameDownOperation (Frame frame) {
    super ();
    initialize (frame);
  }

  private void initialize (Frame frame) {
    _name = "deep-move-frame";
    _shortName = "move";
    _args.setArg(0, frame);
    _prettyName = _name;
  }

  protected void moveRequiredFrames (Frame oldFrame) {
  	if (!(oldFrame instanceof Cls)) return;
  	Collection superclasses = Util.getDirectSuperclasses ((Cls)oldFrame);
    Iterator i = superclasses.iterator();
    while (i.hasNext()) {
    	Cls next = (Cls)i.next();
        if (!Util.isSystem(next))
    		PromptTab.addToQueue(new DeepMoveClsDownOperation (next, _copySlots, _copyEverythingRequired ));
    }

	Cls type = ((Cls)oldFrame).getDirectType();
    if (!Util.isSystem(type))
    	PromptTab.addToQueue(new DeepMoveClsDownOperation (type, _copySlots, _copyEverythingRequired ));

  }


}
