 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.prompt.operation.DeepCopyFrameOperation;
import edu.stanford.smi.protegex.prompt.operation.DeepMoveSlotDownOperation;
import edu.stanford.smi.protegex.prompt.operation.MoveSlotDownOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.ui.CheckBoxWidget;
import edu.stanford.smi.protegex.prompt.ui.GetValueWidget;
import edu.stanford.smi.protegex.prompt.ui.SelectSlotWidget;

public class MoveSlotDownOperationEditor  extends Editor {
//  static private Object [] _args = new Object [2];
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MoveSlotDownOperation",
  		OPERATIONS_PACKAGE + "DeepMoveSlotDownOperation",
  };

  public static String getPrettyName () {
    return "move slot down";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [3];
    _argumentWidgets[0] = new SelectSlotWidget ("Source ontology", 0, chooseOntology, true, willBeModal);
    _argumentWidgets[1] = new CheckBoxWidget (DEEP_COPY, false);
    _argumentWidgets[2] = new CheckBoxWidget (EVERYTHING_REQUIRED, true);
    return _argumentWidgets;
  }

  public Object [] createArgs (Operation a) {
	_args = new Object [2];
   	Object [] args = a.getArgs().toArray();
    _args[0] = args[0];
    _args[1] = new Boolean (a instanceof DeepCopyFrameOperation);
    return _args;
  }

  public Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();

    boolean deepOrShallow = ((Boolean)_argumentWidgets[1].getValue()).booleanValue();
    boolean everythingRequired = ((Boolean)_argumentWidgets[2].getValue()).booleanValue();

    if (deepOrShallow == true)  //perform deep copy
    	result = new DeepMoveSlotDownOperation ((Slot)arg, everythingRequired);
    else
        result = new MoveSlotDownOperation ((Frame)arg, everythingRequired);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
    return result;
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }
}
