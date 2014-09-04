 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protegex.prompt.ui.GetValueWidget;
import edu.stanford.smi.protegex.prompt.ui.SelectClsWidget;

public class RemoveParentOperationEditor extends Editor {
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "RemoveParentOperation"};

  public static String getPrettyName () {
    return "remove parent";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [2];
    _argumentWidgets[0] = new SelectClsWidget ("Source ontology", 0,
    											chooseOntology, willBeModal);
    _argumentWidgets[1] = new SelectClsWidget ("Source ontology", 1,
    											chooseOntology, willBeModal);
    return _argumentWidgets;
  }


  public  String [] responsibleFor () {
   	return _responsibleFor;
  }

/*
  public CopyInstanceOperationEditor () {
    _name = "copy-instance";
    _prettyName = "copy instance";
  }

  static public GetValueWidget [] getValueWidgets () {
   	return _argumentWidgets;
  }

  public void collectData () {
	//get frame
    _arg = _argumentWidgets[0].getValues ();
      if (_arg instanceof Collection)
        _arg = CollectionUtilities.getFirstItem ((Collection)_arg);

    _dispatchOperation = new KeepInstanceOperation ((Instance)_arg);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
  }
*/
}
