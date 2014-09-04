 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class KeepClsOperation extends KeepFrameOperation {
  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [COPY_OPERATION_ARITY];

  public KeepClsOperation (Frame frame) {
	super (frame);
	setDefaultParameters ();
	initialize ();
  }

  public KeepClsOperation (Frame frame, TraversalDirective td) {
	super (frame, td, false);
	setDefaultParameters ();
	initialize ();
  }

// used to be: copyInstances, copyTree
  public KeepClsOperation (Frame frame, TraversalDirective td, boolean fromMerge) {
    super (frame, td, fromMerge);
    if (_traversalDirective.copyInstances())
    	setLocalConsiderInheritedSlots (true);
    initialize ();
  }

  public KeepClsOperation (boolean fromInstanceCopy, Cls cls, TraversalDirective td) {
    super (cls, td, false);
    setDefaultParameters ();
    initialize ();
    if (fromInstanceCopy)
    	setLocalConsiderInheritedSlots (true);
  }

  public KeepClsOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    setDefaultParameters ();
    initialize ();
  }

  private void initialize () {
    _name = "shallow-copy-class";
    _prettyName = "copy class";
    _shortName = "copy";
  }



}
