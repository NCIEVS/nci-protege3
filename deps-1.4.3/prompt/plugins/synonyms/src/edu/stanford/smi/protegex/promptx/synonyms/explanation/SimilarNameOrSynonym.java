/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.synonyms.explanation;

import edu.stanford.smi.protegex.prompt.explanation.*;

public class SimilarNameOrSynonym extends Explanation {
	 final  static int SIMILAR_NAMES_ARITY = 0;

	  public SimilarNameOrSynonym () {
	    super (SIMILAR_NAMES_ARITY);
	    _name = "similar-names-or-synonyms";
	    _prettyName = "frame names or synonyms are similar";
	  }

	  public String toString () {
	    return "Frames names or synonyms are similar";
	  }

}
