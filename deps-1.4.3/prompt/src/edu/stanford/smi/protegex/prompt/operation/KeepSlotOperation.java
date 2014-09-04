 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class KeepSlotOperation extends KeepFrameOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public KeepSlotOperation () {
    super ();
    initialize ();
  }

  public KeepSlotOperation (Frame frame) {
	super (frame);
	initialize ();
  }

  public KeepSlotOperation (Frame frame, TraversalDirective td) {
	super (frame, td, false);
	initialize ();
  }

  public KeepSlotOperation (Frame frame, TraversalDirective td,  boolean fromMerge) {
    super (frame, td, fromMerge);
    initialize ();
  }

  public KeepSlotOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    initialize ();
  }

  private void initialize () {
    _name = "shallow-copy-slot";
    _prettyName = "copy slot";
    _shortName = "copy";
  }

}
