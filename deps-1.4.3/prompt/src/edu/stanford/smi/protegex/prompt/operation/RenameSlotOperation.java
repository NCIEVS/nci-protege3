 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.explanation.Explanation;

public class RenameSlotOperation extends RenameFrameOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [RENAME_OPERATION_ARITY];


  public RenameSlotOperation () {
    super (null, null, null);
    initialize();
  }

  public RenameSlotOperation (Frame f, String newName) {
    super (f, newName, f.getName(), false);
    initialize ();
  }

  public RenameSlotOperation (Frame f, String newName, String oldName) {
    super (f, newName, oldName, false);
    initialize ();
  }

  public RenameSlotOperation (Frame f, String newName, String oldName,  boolean fromListener) {
    super (f, newName, oldName, fromListener);
    initialize ();
  }

  public RenameSlotOperation (Frame f, String newName, String oldName, Explanation exp) {
    super (f, newName, oldName, exp);
    initialize();
  }

  public void initialize () {
    _name = "rename-slot";
    _prettyName = "rename slot";
  }

/*
  static public void createValuesWidgetsImplementation () {
    _argumentWidgets[0] = new SelectSlotWidget ();
    _argumentWidgets[1] = new TextFieldWidget ("The new name for the slot");
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