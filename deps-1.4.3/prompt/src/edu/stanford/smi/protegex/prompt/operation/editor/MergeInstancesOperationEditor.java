 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protegex.prompt.ui.GetValueWidget;
import edu.stanford.smi.protegex.prompt.ui.SelectInstanceWidget;
import edu.stanford.smi.protegex.prompt.util.*;

public class MergeInstancesOperationEditor extends Editor {
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MergeInstancesOperation"};

  public static String getPrettyName () {
    return Util.mergeOrMapString() + " instances";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [2];
    _argumentWidgets[0] = new SelectInstanceWidget ("Ontology for the first instance", 0,
    											chooseOntology, willBeModal);
    _argumentWidgets[1] = new SelectInstanceWidget ("Ontology for the second instance", 1,
    											chooseOntology, willBeModal);
    return _argumentWidgets;
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }


/*
  protected Object _arg2 = null;

  public boolean allArgumentsDefined () {
  	return (_arg != null && _arg2 != null);
  }

  public MergeInstancesOperationEditor () {
    _name = "merge-instances";
    _prettyName = "merge instances";
  }
  static public GetValueWidget [] getValueWidgets () {
   	return _argumentWidgets;
  }

  public void collectData () {
	//get frame
    _arg = _argumentWidgets[0].getValues ();
      if (_arg instanceof Collection)
        _arg = CollectionUtilities.getFirstItem ((Collection)_arg);

    _arg2 = _argumentWidgets[1].getValues ();
      if (_arg2 instanceof Collection)
        _arg2 = CollectionUtilities.getFirstItem ((Collection)_arg2);

    _dispatchOperation =
    	new MergeInstancesOperation ((Instance)_arg, (Instance)_arg2);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
  }
 */
}
