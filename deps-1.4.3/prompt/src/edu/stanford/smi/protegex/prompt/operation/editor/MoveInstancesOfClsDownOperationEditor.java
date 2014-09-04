 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class MoveInstancesOfClsDownOperationEditor extends Editor {
  static final String [] _responsibleFor = new String [] {
  	OPERATIONS_PACKAGE + "MoveInstancesOfClsDownOperation"};

  public static String getPrettyName () {
    return "move all instances of class down";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [2];
    _argumentWidgets[0] = new SelectClsWidget ("Source ontology", 0, chooseOntology, true, willBeModal);
    _argumentWidgets[1] = new CheckBoxWidget (EVERYTHING_REQUIRED, true);

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

   	result = new MoveInstancesOfClsDownOperation ((Cls)arg, everythingRequired);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
    return result;
  }
}
