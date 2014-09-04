 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MergeSlotsOperationEditor extends Editor {
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MergeSlotsOperation"};

  public static String getPrettyName () {
    return Util.mergeOrMapString() + " slots";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [2];
    _argumentWidgets[0] = new SelectSlotWidget ("Ontology for the first slot", 0,
    											chooseOntology, willBeModal);
    _argumentWidgets[1] = new SelectSlotWidget ("Ontology for the second slot", 1,
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

  public MergeSlotsOperationEditor () {
    _name = "merge-slots";
    _prettyName = "merge slots";
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
    	new MergeSlotsOperation ((Slot)_arg, (Slot)_arg2);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
  }
*/
}
