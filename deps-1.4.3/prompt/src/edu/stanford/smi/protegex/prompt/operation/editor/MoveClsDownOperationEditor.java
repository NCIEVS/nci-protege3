 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class MoveClsDownOperationEditor extends Editor{
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MoveClsDownOperation",
        OPERATIONS_PACKAGE + "DeepMoveDownOperation"
  };

  public static String getPrettyName () {
    return "move class down";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
	_argumentWidgets = new GetValueWidget [3];
    _argumentWidgets[0] = new SelectClsWidget ("Source ontology", 0, chooseOntology, true, willBeModal);
    _argumentWidgets[1] = new CheckBoxWidget (DEEP_COPY, false);
    _argumentWidgets[2] = new CheckBoxWidget (EVERYTHING_REQUIRED, true);
//    _argumentWidgets[2] = new CheckBoxWidget (COPY_SLOTS, true);
    return _argumentWidgets;
  }

  public  Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();

	//deep or shallow copy
    boolean deepOrShallow = ((Boolean)_argumentWidgets[1].getValue()).booleanValue();
//    boolean copySlots = ((Boolean)_argumentWidgets[2].getValue()).booleanValue();
    boolean everythingRequired = ((Boolean)_argumentWidgets[2].getValue()).booleanValue();

    if (deepOrShallow == true)  //perform deep copy
    	result = new DeepMoveClsDownOperation ((Cls)arg, true, everythingRequired);
    else
        result = new MoveClsDownOperation ((Cls)arg, true, everythingRequired);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();

    return result;
  }

  public Object [] createArgs (Operation a) {
// _args   = new Object [4]
   	Object [] args = a.getArgs().toArray();
	_args = new Object [3];
    _args[0] = args[0];
    _args[1] = new Boolean (a instanceof DeepMoveFrameDownOperation);
    _args[2] = new Boolean (a.copySlots());
    return _args;
  }

  public String [] responsibleFor () {
   	return _responsibleFor;
  }
}
