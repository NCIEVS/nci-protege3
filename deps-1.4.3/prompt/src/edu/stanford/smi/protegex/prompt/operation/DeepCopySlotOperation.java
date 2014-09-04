 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;

public class DeepCopySlotOperation extends DeepCopyFrameOperation {
	public DeepCopySlotOperation (Frame frame) {
	  super (frame);
	  _name = "deep-copy-slot";
	  _prettyName = "copy slot";
	  _shortName = "copy";
	}

	public DeepCopySlotOperation (Frame frame, TraversalDirective td) {
	  super (frame, td);
	  _name = "deep-copy-slot";
	  _prettyName = "copy slot";
	  _shortName = "copy";
	}


}
