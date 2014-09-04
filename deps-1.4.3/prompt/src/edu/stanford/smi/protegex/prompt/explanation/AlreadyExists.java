 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.ui.DisplayUtilities;

public class AlreadyExists extends Explanation {
  final static int ALREADY_EXISTS_ARITY = 2;

  public AlreadyExists (Frame f) {
    super (ALREADY_EXISTS_ARITY);
    _args.setArg (0, f.getName());
    _args.setArg (1, f);
    _name = "already-exists";
    _shortName = "frame with the name";
    _connectorString = "already exists:";
  }

  public String toString () {
    return "Frame with the name " + ((Frame)_args.getArg (0)).getName() +
           " already existis: " +  DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg (0));
  }
}

