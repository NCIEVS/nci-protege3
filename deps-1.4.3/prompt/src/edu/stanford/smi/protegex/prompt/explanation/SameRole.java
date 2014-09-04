 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.ui.DisplayUtilities;

public class SameRole extends Explanation {
  static private int SAME_ROLE_ARITY = 2;

  public SameRole (String role, Frame newFrame) {
    super (SAME_ROLE_ARITY);
    _args.setArg (0, role);
    _args.setArg (1, newFrame);
    _name = "same-role";
    _shortName = "both frames are";
    _connectorString = "for";
  }

  public String toString () {
    return "Both frames are " + _args.getArg(0) + " for " +
                          DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg (1));
  }
}

