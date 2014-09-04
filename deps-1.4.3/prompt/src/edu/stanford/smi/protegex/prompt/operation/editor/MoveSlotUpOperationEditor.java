 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.operation.MoveSlotUpOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.ui.CheckBoxWidget;
import edu.stanford.smi.protegex.prompt.ui.GetValueWidget;
import edu.stanford.smi.protegex.prompt.ui.SelectSlotWidget;

public class MoveSlotUpOperationEditor  extends Editor {
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MoveSlotUpOperation"};

  public static String getPrettyName () {
    return "move slot up";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [2];
    _argumentWidgets[0] = new SelectSlotWidget ("Source ontology", 1, chooseOntology, true, willBeModal);
    _argumentWidgets[1] = new CheckBoxWidget (EVERYTHING_REQUIRED, true, false);
    return _argumentWidgets;
  }

  public Object [] createArgs (Operation a) {
	_args = new Object [1];
   	Object [] args = a.getArgs().toArray();
    _args[0] = args[0];
//    _args[1] = new Boolean (a.copyEverythingRequired());
    return _args;
  }

  public Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();

    boolean everythingRequired = ((Boolean)_argumentWidgets[1].getValue()).booleanValue();

    result = new MoveSlotUpOperation ((Frame)arg, everythingRequired);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
    return result;
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }
}
