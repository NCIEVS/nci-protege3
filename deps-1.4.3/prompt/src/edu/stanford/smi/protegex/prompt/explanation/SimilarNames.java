 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;


public class SimilarNames extends Explanation {
  final  static int SIMILAR_NAMES_ARITY = 0;

  public SimilarNames () {
    super (SIMILAR_NAMES_ARITY);
    _name = "similar-names";
    _prettyName = "frames have similar names";
  }

  public String toString () {
    return "Frames have similar names";
  }
}

