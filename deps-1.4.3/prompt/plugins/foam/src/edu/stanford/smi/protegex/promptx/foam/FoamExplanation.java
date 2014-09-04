package edu.stanford.smi.protegex.promptx.foam;

import edu.stanford.smi.protegex.prompt.explanation.Explanation;

public class FoamExplanation extends Explanation {
	final static int FOAM_NAMES_ARITY = 0;

	public FoamExplanation(String confidence) {
		super(FOAM_NAMES_ARITY);
		_name = "foam-similar-names";
		_prettyName = "frames are similar according to FOAM with confidence = " + confidence;
	}

	public String toString() {
		return "Frames are similar according to FOAM";
	}
}
