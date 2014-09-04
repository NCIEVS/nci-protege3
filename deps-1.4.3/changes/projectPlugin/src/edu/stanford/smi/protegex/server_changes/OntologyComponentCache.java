package edu.stanford.smi.protegex.server_changes;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.Log;


public class OntologyComponentCache {
	
	private static final Logger log = Log.getLogger(OntologyComponentCache.class);
	
	private static HashMap<Frame, Ontology_Component> frame2OntoCompMap = new HashMap<Frame, Ontology_Component>();

	public static Ontology_Component getOntologyComponent(Frame frame) {
    	return getOntologyComponent(frame, false);
    }

	public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
		if (frame == null) { return null; }		
		if (frame2OntoCompMap.containsKey(frame)) {
			Ontology_Component oc = frame2OntoCompMap.get(frame);
			if (oc != null) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Get ontology componenent from cache: " + frame);
				}
				return frame2OntoCompMap.get(frame);	
			}			
		}
		
				
		Ontology_Component ontologyComp = null;
		try {
			GetOntologyComponentFromServer job = new GetOntologyComponentFromServer(frame.getKnowledgeBase(), frame, create);
			ontologyComp = job.execute();
			// Hack!! should be fixed later
			if (ontologyComp != null) {
				Instance inst = ((AbstractWrappedInstance)ontologyComp).getWrappedProtegeInstance();
				inst.getFrameID().localize(ChAOKbManager.getChAOKb(frame.getKnowledgeBase()));
			}

			frame2OntoCompMap.put(frame, ontologyComp);
		} catch (Throwable e) {
			log.log(Level.WARNING, "Errors at retrieving ontology component from server for frame: " + frame, e);			
		}
		return ontologyComp;
	}
	
	public static void put(Frame frame, Ontology_Component ontComp) {		
		if (frame == null) { return;}
		frame2OntoCompMap.put(frame, ontComp);
	}

	public static void clearCache() {
		frame2OntoCompMap.clear();
	}
	
}
