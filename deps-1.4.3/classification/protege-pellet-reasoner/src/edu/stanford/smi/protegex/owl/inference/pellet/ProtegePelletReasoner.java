package edu.stanford.smi.protegex.owl.inference.pellet;

import org.mindswap.pellet.KnowledgeBase;

import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;

public interface ProtegePelletReasoner extends ProtegeReasoner {

	public KnowledgeBase getPelletKB();
	
}
