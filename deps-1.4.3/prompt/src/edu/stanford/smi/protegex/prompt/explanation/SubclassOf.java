 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;


import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class SubclassOf extends Explanation {
  final static int SUBCLASS_OF_ARITY = 2;

  public SubclassOf (Frame child, Frame parent) {
    super (SUBCLASS_OF_ARITY);
    _args.setArg (0, child);
    _args.setArg (1, parent);
    _name = "subclass-of";
    _shortName = "";
    _connectorString = "is a subclass of";
  }

  public String getShortName () {
	return super.getShortName();
  }

  public String toString () {
    return DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(0)) +
                          " is a subclass of: " + DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(1));
  }
}


