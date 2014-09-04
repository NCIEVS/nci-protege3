 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;


import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.ui.DisplayUtilities;

public class SuperclassOf extends Explanation {
  final static int SUPERCLASS_OF_ARITY = 2;

  public SuperclassOf (Frame parent, Frame child) {
    super (SUPERCLASS_OF_ARITY);
    _args.setArg(0, parent);
    _args.setArg(1, child);
    _name = "superclass-of";
    _shortName = "";
    _connectorString = "is a superclass of";
  }

  public String toString () {
    return DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(0)) +
                          " is a superclass of: " + DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(1));
  }
}


