 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MergeClsesOperationEditor extends Editor{
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MergeClsesOperation"};

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
  	_argumentWidgets = new GetValueWidget [5];
    _argumentWidgets[0] = new SelectClsWidget ("Ontology for the first class", 0,
    											chooseOntology, willBeModal);
    _argumentWidgets[1] = new SelectClsWidget ("Ontology for the second class", 1,
    											chooseOntology, willBeModal);
    _argumentWidgets[2] = new CheckBoxWidget (DEEP_COPY, false);
    _argumentWidgets[3] = new CheckBoxWidget (COPY_SUBCLASSES, false);
    _argumentWidgets[4] = new CheckBoxWidget (COPY_INSTANCES, false);
    return _argumentWidgets;
  }

  public static String getPrettyName () {
    return Util.mergeOrMapString() + " classes";
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }

  public Object [] createArgs (Operation a) {
   	Object [] args = a.getArgs().toArray();
	_args = new Object [5];
    _args[0] = args[0];
    _args[1] = args[1];
    _args[2] = new Boolean (((MergeFramesOperation)a).deepCopy());
    _args[3] = new Boolean (a.copySubclasses());
    _args[4] = new Boolean (a.copyInstances());
    return _args;
  }

/*
  protected Object _arg2 = null;

  public Class [] responsibleFor () {
   	try {
    	return new Class [] {Class.forName (OPERATIONS_PACKAGE + "MergeClsesOperation")};
    } catch (Exception e) {}
  }
*/
//  static public int numberOfArgs () { return 5; }

/*
  public boolean allArgumentsDefined () {
  	return (_arg != null && _arg2 != null);
  }

  public MergeClsesOperationEditor () {
  	_name = "merge-class";
  	_prettyName = "merge classes";
  }
*/

/*
  static public GetValueWidget [] getValueWidgets () {
   	return _argumentWidgets;
  }

  public void collectData () {
Log.stack ("In collectData", this, "collectData");
	//get frame
    _arg = _argumentWidgets[0].getValues ();
      if (_arg instanceof Collection)
        _arg = CollectionUtilities.getFirstItem ((Collection)_arg);

    _arg2 = _argumentWidgets[1].getValues ();
      if (_arg2 instanceof Collection)
        _arg2 = CollectionUtilities.getFirstItem ((Collection)_arg2);

	//deep or shallow copy
    boolean deepOrShallow = ((Boolean)_argumentWidgets[2].getValues()).booleanValue();
    boolean copyTree = ((Boolean)_argumentWidgets[3].getValues()).booleanValue();
    boolean copyInstances = ((Boolean)_argumentWidgets[4].getValues()).booleanValue();

    _dispatchOperation =
    	new MergeClsesOperation ((Cls)_arg, (Cls)_arg2, copyInstances, copyTree, deepOrShallow);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
  }
*/
}
