 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class MoveInstancesOfClsDownOperation extends MoveDownOperation {
  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [MOVE_OPERATION_ARITY];

  public MoveInstancesOfClsDownOperation (Frame frame) {
    super (frame);
    setDefaultParameters ();
    initialize ();
  }

  public MoveInstancesOfClsDownOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    setDefaultParameters ();
    initialize ();
  }

  public MoveInstancesOfClsDownOperation (Frame frame, boolean copyEverythingRequired) {
    super (frame);
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  private void initialize () {
    _name = "move-all-instances-of-class-down";
    _prettyName = "move all instances of class down";
    _shortName = "move instances down";
  }

  public void actualOperation (){
    Cls cls = (Cls) _args.getArg(0);
	Collection instances = cls.getDirectInstances();
    if (instances == null) return;
    Iterator i = (new ArrayList (instances)).iterator();
    while (i.hasNext()) {
     	MoveDownOperation.createOperation ((Frame)i.next(), false, _copyEverythingRequired).performOperation ();
    }
  }

}
