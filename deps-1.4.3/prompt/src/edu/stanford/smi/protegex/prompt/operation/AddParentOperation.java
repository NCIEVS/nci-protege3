 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.explanation.*;

public class AddParentOperation extends Operation {
  static final int ADD_PARENT_OPERATION_ARITY = 2;
  boolean _fromListener = false;

  public AddParentOperation () {
    super (ADD_PARENT_OPERATION_ARITY);
    initialize(null, null, false);
  }


  public AddParentOperation (Cls child, Cls parent) {
    super (ADD_PARENT_OPERATION_ARITY);
    initialize (child, parent, false);
  }

  public AddParentOperation (Cls child, Cls parent, boolean fromListener) {
    super (ADD_PARENT_OPERATION_ARITY);
    initialize (child, parent, fromListener);
  }

  public AddParentOperation (Cls child, Cls parent, Explanation exp) {
    super (ADD_PARENT_OPERATION_ARITY, exp);
    initialize(child, parent, false);
  }

  public void initialize (Cls child, Cls parent, boolean fromListener) {
    _name = "add-parent";
    _prettyName = "add parent";
    _connectorString = "to";
    _args.setArg(0, child);
    _args.setArg(1, parent);
    _newFrame = child;
    _canView = false;
    _fromListener = fromListener;
  }

  public void actualOperation (){
    Cls child = (Cls) _args.getArg (0);
    Cls parent = (Cls) _args.getArg (1);
    if (!_fromListener)
      addDirectSuperclasses(child, CollectionUtilities.createCollection(parent));
  }
}
