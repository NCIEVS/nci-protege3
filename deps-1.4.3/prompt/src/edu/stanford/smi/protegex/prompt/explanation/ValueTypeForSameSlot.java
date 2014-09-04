 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;

import edu.stanford.smi.protege.model.*;

public class ValueTypeForSameSlot extends Explanation {
  final  static int VALUE_TYPE_FOR_SAME_SLOT_ARITY = 1;

  public ValueTypeForSameSlot (Slot slot) {
    super (VALUE_TYPE_FOR_SAME_SLOT_ARITY);
    _args.setArg (0, slot);
    _name = "value-type-for-same-slot";
    _shortName = "Frame were value types for merged slots that are now";
  }

/*
  public String toString () {
  	if (_args[0] == null)
    	return "Frame were value types for merged slots that are now " + ((Slot)_args[1]).getName();
    else
    	return "Frame were value types for merged slots at class " + ((Cls)_args[0]).getName();
  }
*/
}

