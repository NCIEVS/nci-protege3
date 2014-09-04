/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
/**
 * 
 */
package edu.stanford.smi.protegex.promptx.umls;

import edu.stanford.smi.protegex.prompt.explanation.*;

public class IdenticalUMLSCUI extends Explanation {
	 final  static int IDENTICAL_CUIS_ARITY = 0;

	  public IdenticalUMLSCUI () {
		    super (IDENTICAL_CUIS_ARITY);
		    _name = "identical-cuis";
		    _prettyName = "classes have the same CUI in UMLS";
		  }

		  public String toString () {
		    return "Classes have the same CUI in UMLS";
		  }

} 