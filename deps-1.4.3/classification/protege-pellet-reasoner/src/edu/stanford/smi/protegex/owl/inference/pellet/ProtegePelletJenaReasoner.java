package edu.stanford.smi.protegex.owl.inference.pellet;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.Pellet;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.reasoner.Reasoner;

import edu.stanford.smi.protegex.owl.inference.jena.DefaultJenaReasoner;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class ProtegePelletJenaReasoner extends DefaultJenaReasoner 
										implements ProtegePelletReasoner{

	public ProtegePelletJenaReasoner() {
		super();
		
		setAutoSynchronizationEnabled(true);

	}
	
	@Override
	protected OntModelSpec getDefaultOntModelSpec() {
		OntModelSpec ontModelSpec = super.getDefaultOntModelSpec();
		Reasoner reasoner = PelletReasonerFactory.theInstance().create();
		ontModelSpec.setReasoner(reasoner);
		
		return ontModelSpec;
	}

	@Override
	protected OntModel getOntModel() {
		if (ontModel == null) {
			ontModel = super.getOntModel();
			ontModel.setStrictMode(false);
		}
				
		return ontModel;
	}
	
	public static String getReasonerName() {
		return "Pellet " + Pellet.getVersionInfo().getVersionString() + " (direct - Jena)";
	}
	
	public void releaseKB(OWLModel owlModel) {
		ontModel.close();
		/*PelletInfGraph p = null;		
		p.getKB().clear();
		*/		
	}
	
	public KnowledgeBase getPelletKB() {
		try {
			return ((PelletInfGraph)getOntModel().getGraph()).getKB();
		} catch (Throwable t) {
			//do nothing
		}
		
		return null;
	}
	
}
