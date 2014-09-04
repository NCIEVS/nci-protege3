 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;

import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.operation.Operation;

public class Explanation extends Action {
  static final String  UNDEFINED_INDICATOR = "undefined";
//  Collection explanations = null;
  static final int EXPLANATION_ARITY = 1;

  Operation _operationItExplains;

  public Explanation () {
    super (0);
    _name = "explanation";
  }

  public Explanation (int arity) {
    super (arity);
    _name = "explanation";
  }

  public Explanation (String str) {
    super (EXPLANATION_ARITY);
    _args.setArg (0, str);
    _name = "explanation";
  }

  public void setOperation (Operation op) {
  	_operationItExplains = op;
  }

  public Operation getOperation () {
  	return _operationItExplains;
  }

  public static String noReason () {
    return  UNDEFINED_INDICATOR;
  }


}
