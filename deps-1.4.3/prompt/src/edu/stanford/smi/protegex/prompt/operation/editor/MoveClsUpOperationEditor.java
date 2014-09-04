 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class MoveClsUpOperationEditor extends Editor{
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "MoveClsUpOperation"};

  public static String getPrettyName () {
    return "move class up";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
    _argumentWidgets = new GetValueWidget [4];
    _argumentWidgets[0] = new SelectClsWidget ("Source ontology", 1, chooseOntology, true, willBeModal);
    _argumentWidgets[1] = new CheckBoxWidget (COPY_SUBCLASSES, false);
    _argumentWidgets[2] = new CheckBoxWidget (COPY_INSTANCES, false);
    _argumentWidgets[3] = new CheckBoxWidget (EVERYTHING_REQUIRED, true, false);
//    _argumentWidgets[2] = new CheckBoxWidget (COPY_SLOTS, true);
    return _argumentWidgets;
  }

  public  Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();

//    boolean copySlots = ((Boolean)_argumentWidgets[2].getValue()).booleanValue();
    boolean everythingRequired = ((Boolean)_argumentWidgets[3].getValue()).booleanValue();
    boolean copyTree = ((Boolean)_argumentWidgets[1].getValue()).booleanValue();
    boolean copyInstances = ((Boolean)_argumentWidgets[2].getValue()).booleanValue();

	TraversalDirective td = new TraversalDirective ((Cls)arg, copyInstances, copyTree);
    result = new MoveClsUpOperation ((Cls)arg, td, everythingRequired);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();

    return result;
  }

  public Object [] createArgs (Operation a) {
// _args   = new Object [4]
    Object [] args = a.getArgs().toArray();
    _args = new Object [4];
    _args[0] = args[0];
    _args[1] = new Boolean (a.copySubclasses());
    _args[2] = new Boolean (a.copyInstances());
    return _args;
  }

  public String [] responsibleFor () {
   	return _responsibleFor;
  }
}
