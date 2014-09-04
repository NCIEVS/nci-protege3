/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.prompt.explanation;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.Mappings;

public class RejectedMapping extends Explanation {
	final static int REJECTED_MAPPING_ARITY = 2;

	public RejectedMapping(Frame f1, Frame f2) {
		super(REJECTED_MAPPING_ARITY);
		_args.setArg(0, f1);
		_args.setArg(1, f2);
		_name = "rejected-mapping";
		_shortName = "mapping previously rejected:";
		_connectorString = "and";
	}

	public String toString() {
		return "Mapping from frame " + Mappings.getRealName((Frame) _args.getArg(0)) + " to " + Mappings.getRealName((Frame) _args.getArg(1)) + "was previously rejected";
	}
}
