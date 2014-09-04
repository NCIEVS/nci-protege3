 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class MoveInstanceUpOperation extends MoveUpOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public MoveInstanceUpOperation () {
    super ();
    initialize ();
  }

  public MoveInstanceUpOperation (Frame frame) {
    super (frame);
    initialize ();
  }

  public MoveInstanceUpOperation (Frame frame, boolean copyEverythingRequired) {
    super (frame);
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  public MoveInstanceUpOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    initialize ();
  }

  public MoveInstanceUpOperation (Frame frame, Cls cls, boolean copyEverythingRequired) {
    super (frame);
    _frameType = cls;
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  private void initialize () {
    _name = "shallow-move-instance-up";
    _prettyName = "move instance up";
    _shortName = "move up";
  }


}
