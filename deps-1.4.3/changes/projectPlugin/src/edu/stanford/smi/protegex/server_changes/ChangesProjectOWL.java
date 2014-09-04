package edu.stanford.smi.protegex.server_changes;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesTransListener;
import edu.stanford.smi.protegex.server_changes.listeners.owl.ChangesOwlKBListener;
import edu.stanford.smi.protegex.server_changes.listeners.owl.OwlChangesClassListener;
import edu.stanford.smi.protegex.server_changes.listeners.owl.OwlChangesFrameListener;
import edu.stanford.smi.protegex.server_changes.listeners.owl.OwlChangesPropertyListener;


public class ChangesProjectOWL {
	public static void registerOwlListeners(KnowledgeBase knowledgeBase) {
		OWLModel om = (OWLModel) knowledgeBase;
		om.addClassListener(new OwlChangesClassListener(om));
		om.addPropertyListener(new OwlChangesPropertyListener(om));
		((KnowledgeBase) om).addFrameListener(new OwlChangesFrameListener(om));
		om.addTransactionListener(new ChangesTransListener(om));
		((KnowledgeBase) om).addKnowledgeBaseListener(new ChangesOwlKBListener(om)); // Handles Class Deletes
	}

}
