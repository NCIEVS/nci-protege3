 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.Frame;

public class DeepMoveSlotDownOperation extends DeepMoveFrameDownOperation {
  public DeepMoveSlotDownOperation (Frame frame) {
    super (frame);
    _name = "deep-move-slot";
    _prettyName = "move slot";
    _shortName = "move";
  }

  public DeepMoveSlotDownOperation (Frame frame, boolean everythingRequired) {
    super (frame);
    _name = "deep-move-slot";
    _prettyName = "move slot";
    _shortName = "move";
    _copyEverythingRequired = everythingRequired;
  }


}
