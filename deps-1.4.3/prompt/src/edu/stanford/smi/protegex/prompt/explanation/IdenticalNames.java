 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;


public class IdenticalNames extends Explanation {
  final  static int IDENTICAL_NAMES_ARITY = 0;

  public IdenticalNames () {
    super (IDENTICAL_NAMES_ARITY);
    _name = "identical-names";
    _prettyName = "frames have identical names";
  }

  public String toString () {
    return "Frames have identical names";
  }
}

