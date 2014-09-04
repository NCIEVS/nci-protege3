 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class RemoveTemplateSlotOperation extends Operation {
  static final int REMOVE_TEMPLATE_SLOT_OPERATION_ARITY = 2;
  boolean _fromListener = false;
   
  public RemoveTemplateSlotOperation () {
    super (REMOVE_TEMPLATE_SLOT_OPERATION_ARITY);
    initialize(null, null, false);
  }


  public RemoveTemplateSlotOperation (Cls cls, Slot slot) {
    super (REMOVE_TEMPLATE_SLOT_OPERATION_ARITY);
    initialize (cls, slot, false);
  }

  public RemoveTemplateSlotOperation (Cls cls, Slot slot, boolean fromListener) {
    super (REMOVE_TEMPLATE_SLOT_OPERATION_ARITY);
    initialize (cls, slot, fromListener);
  }

  public void initialize (Cls cls, Slot slot, boolean fromListener) {
    _name = "remove-template-slot";
    _prettyName = "remove template slot";
    _args.setArg (0, cls);
    _args.setArg (1, slot);
    _canView = false;
    _fromListener = fromListener;
  }

  public void actualOperation (){
    Cls cls = (Cls)_args.getArg(0);
    Slot slot = (Slot)_args.getArg(1);

    if (!_fromListener)
       cls.removeDirectTemplateSlot(slot);
    ReplaceReferences.removeClsToClsReferences (cls, slot);
  }


}
