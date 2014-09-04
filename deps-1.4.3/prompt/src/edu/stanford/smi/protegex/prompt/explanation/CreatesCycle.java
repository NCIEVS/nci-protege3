 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;
 

public class CreatesCycle extends Explanation {
  final  static int CREATES_CYCLE_ARITY = 0;

  public CreatesCycle () {
    super (CREATES_CYCLE_ARITY);
    _name = "creates-cycle";
    _prettyName = "creates a cycle";
  }

  public String toString () {
    return "Creates a cycle";
  }
}

