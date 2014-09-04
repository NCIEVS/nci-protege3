 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.util.*;
import edu.stanford.smi.protegex.prompt.util.CompareNames;

public class ApproximateMatch extends Explanation {
  final  static int APPROXIMATE_MATCH = 2;

  public ApproximateMatch (Frame f1, Frame f2) {
    super (APPROXIMATE_MATCH);
    _args.setArg (0, f1);
    _args.setArg (1, f2);
    _name = "approximate_match";
    _shortName = "frame names are similar:";
    _connectorString = "and";
  }

  public String toString () {
    return "Frame names are similar: " + Mappings.getRealName ((Frame)_args.getArg(0)) +
                                  " and " + Mappings.getRealName ((Frame)_args.getArg(1));
  }

  public void replaceFrameReference (Frame from, Frame to) {
    super.replaceFrameReference (from, to);
    if (CompareNames.compareNames (Util.getLocalBrowserText((Frame)_args.getArg(0)), Util.getLocalBrowserText((Frame)_args.getArg(1))) != CompareNames.APPROXIMATE_MATCH)
      removeParentSuggestion ();
  }

  private void removeParentSuggestion () {

  }
}

