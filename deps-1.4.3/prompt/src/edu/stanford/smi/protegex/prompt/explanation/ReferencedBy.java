 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;


import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class ReferencedBy extends Explanation {
  final static int REFERENCED_BY_ARITY = 3;

  public ReferencedBy (Frame referredTo, Slot newSlot, Frame referredBy) {
    super (REFERENCED_BY_ARITY);
    _args.setArg (0, referredTo);
    _args.setArg (1, newSlot);
    _args.setArg (2, referredBy);  // can be null
    _name = "referenced-by";
    _shortName = "";
    _connectorString = "is referenced by";
    _connectorString2 = "at";
  }

  public String toString () {
    String referencedByWhom;
    if (_args.getArg(1) != null && _args.getArg(2) != null)
      referencedByWhom = DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(1)) +
                       " at " + DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(2));
    else 
      referencedByWhom = DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(1));

    return DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(0)) +
                          " is referenced by: " + referencedByWhom;
  }

  public static Explanation selectExplanation (Frame referredTo, Slot newSlot, Frame referredBy,
                                               boolean subclassesOrSuperclasses) {
    if (newSlot == null && subclassesOrSuperclasses == true)
      return new SubclassOf (referredTo, referredBy);
    if (newSlot == null && subclassesOrSuperclasses == false)
      return new SuperclassOf (referredTo, referredBy);
    return new ReferencedBy (referredTo, newSlot, referredBy);
  }
}


