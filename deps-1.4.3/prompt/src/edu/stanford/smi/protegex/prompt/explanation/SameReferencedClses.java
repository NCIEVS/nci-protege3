 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;


public class SameReferencedClses extends Explanation {
  static private int SAME_REFERENCES_ARITY = 0;

  public SameReferencedClses () {
    super (SAME_REFERENCES_ARITY);
    _name = "same-references";
    _shortName = "Both frames reference the same sets of classes as allowed classes for their slots";
  }

  public String toString () {
    return _shortName;
  }
}

