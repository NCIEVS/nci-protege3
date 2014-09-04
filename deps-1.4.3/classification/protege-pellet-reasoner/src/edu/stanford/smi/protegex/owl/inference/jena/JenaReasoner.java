package edu.stanford.smi.protegex.owl.inference.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;

import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;


public interface JenaReasoner extends ProtegeReasoner {

	Reasoner getJenaReasoner() throws ProtegeReasonerException;
	
	OntModel getJenaModel() throws ProtegeReasonerException;

	ValidityReport getValidityReport() throws ProtegeReasonerException;
		
}
