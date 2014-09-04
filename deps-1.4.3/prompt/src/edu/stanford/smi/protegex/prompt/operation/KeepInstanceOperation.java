 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class KeepInstanceOperation extends KeepFrameOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public KeepInstanceOperation () {
    super ();
    initialize ();
  }

  public KeepInstanceOperation (Frame frame) {
	super (frame);
	initialize ();
  }

  public KeepInstanceOperation (Frame frame, TraversalDirective td) {
	super (frame, td, false);
	initialize ();
  }

  public KeepInstanceOperation (Frame frame, Cls cls, TraversalDirective td) {
    super (frame, td, false);
    _frameType = cls;
    initialize ();
  }


  public KeepInstanceOperation (Frame frame, TraversalDirective td, boolean fromMerge) {
    super (frame, td, fromMerge);
    initialize ();
  }

  public KeepInstanceOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    initialize ();
  }

  private void initialize () {
    _name = "shallow-copy-instance";
    _prettyName = "copy instance";
    _shortName = "copy";
  }


}
