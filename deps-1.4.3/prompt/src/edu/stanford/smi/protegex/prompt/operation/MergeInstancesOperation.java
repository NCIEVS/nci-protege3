 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.explanation.Explanation;
import edu.stanford.smi.protegex.prompt.util.*;

public class MergeInstancesOperation extends MergeFramesOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [MERGE_OPERATION_ARITY];

  public MergeInstancesOperation () {
    super ();
    initialize ();
  }

  public MergeInstancesOperation (Frame f1, Frame f2) {
    super (f1, f2);
    initialize ();
  }

  public MergeInstancesOperation (Frame f1, Frame f2, Explanation exp) {
    super (f1, f2, exp);
    initialize ();
  }

  private void initialize () {
		String mergeOrMap = Util.mergeOrMapString ();
	    _name = mergeOrMap + "-instances";
	    _prettyName = mergeOrMap + " instances";
	    _shortName = mergeOrMap;
   }

  public void actualOperation () {
    super.actualOperation();
  }


/*
  static public void createValuesWidgetsImplementation () {
    _argumentWidgets[0] = new SelectInstanceWidget ("Ontology for the first instance", 0);
    _argumentWidgets[1] = new SelectInstanceWidget ("Ontology for the second instance", 1);

  }
*/
}
