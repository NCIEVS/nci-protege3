/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.synonyms.explanation;

import edu.stanford.smi.protegex.prompt.explanation.*;


public class IdenticalMatchForNameOrSynonym extends Explanation {
	 final  static int IDENTICAL_NAMES_ARITY = 0;

	  public IdenticalMatchForNameOrSynonym () {
		    super (IDENTICAL_NAMES_ARITY);
		    _name = "identical-names-or-synonyms";
		    _prettyName = "frame names or synonyms are identical";
		  }

		  public String toString () {
		    return "Frame names or synonyms are identical";
		  }

}
