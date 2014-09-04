 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class MoveInstanceDownOperation extends MoveDownOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public MoveInstanceDownOperation () {
    super ();
    initialize ();
  }

  public MoveInstanceDownOperation (Frame frame) {
    super (frame);
    initialize ();
  }

  public MoveInstanceDownOperation (Frame frame, boolean copyEverythingRequired) {
    super (frame);
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  public MoveInstanceDownOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    initialize ();
  }

  public MoveInstanceDownOperation (Frame frame, Cls cls, boolean copyEverythingRequired) {
    super (frame);
    _frameType = cls;
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  private void initialize () {
    _name = "shallow-move-instance-down";
    _prettyName = "move instance down";
    _shortName = "move down";
  }


}
