 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.explanation.Explanation;

public class RenameClsOperation extends RenameFrameOperation {
//  static private GetValueWidget [] _argumentWidgets =
//                       {new SelectClsWidget (),
//                        new TextFieldWidget ("The new name for the class")};

//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [RENAME_OPERATION_ARITY];


  public RenameClsOperation () {
    super (null, null, null);
    initialize();
  }

  public RenameClsOperation (Frame f, String newName) {
    super (f, newName, f.getName(), false);
    initialize ();
  }

  public RenameClsOperation (Frame f, String newName, String oldName) {
    super (f, newName, oldName, false);
    initialize ();
  }

  public RenameClsOperation (Frame f, String newName, String oldName,  boolean fromListener) {
    super (f, newName, oldName, fromListener);
    initialize ();
  }

  public RenameClsOperation (Frame f, String newName, String oldName, Explanation exp) {
    super (f, newName, oldName, exp);
    initialize();
  }

  public void initialize () {
    _name = "rename-class";
    _prettyName = "rename class";
  }

/*
  static public void createValuesWidgetsImplementation () {
    _argumentWidgets[0] = new SelectClsWidget ();
    _argumentWidgets[1] = new TextFieldWidget ("The new name for the class");
  }

  static public JPanel createActionBox () {
     createValuesWidgetsImplementation();
    return Operation.createActionBox (_argumentWidgets);
  }

  public JPanel createEditBox () {
     createValuesWidgetsImplementation();
    return createEditBox (_argumentWidgets);
  }

  public void collectData () {
    super.collectData (_argumentWidgets);
  }
*/
}
