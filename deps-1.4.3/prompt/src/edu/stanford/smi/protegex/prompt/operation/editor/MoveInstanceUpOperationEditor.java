 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protegex.prompt.operation.MoveInstanceUpOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.ui.CheckBoxWidget;
import edu.stanford.smi.protegex.prompt.ui.GetValueWidget;
import edu.stanford.smi.protegex.prompt.ui.SelectInstanceWidget;

public class MoveInstanceUpOperationEditor extends Editor {
  static final String [] _responsibleFor = new String [] {
  	OPERATIONS_PACKAGE + "MoveInstanceUpOperation"};

  public static String getPrettyName () {
    return "move instance up";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [2];
    _argumentWidgets[0] = new SelectInstanceWidget ("Source ontology", 1, chooseOntology, true, willBeModal);
    _argumentWidgets[1] = new CheckBoxWidget (EVERYTHING_REQUIRED, true, false);

    return _argumentWidgets;
  }

  public Object [] createArgs (Operation a) {
   	Object [] args = a.getArgs().toArray();
	_args = new Object [1];
    _args[0] = args[0];
    return _args;
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }

  public Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();
    boolean everythingRequired = ((Boolean)_argumentWidgets[1].getValue()).booleanValue();

   	result = new MoveInstanceUpOperation ((Instance)arg, everythingRequired);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
    return result;
  }
}
