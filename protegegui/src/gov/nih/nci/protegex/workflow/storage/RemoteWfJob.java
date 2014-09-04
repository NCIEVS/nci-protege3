package gov.nih.nci.protegex.workflow.storage;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;

class RemoteWfJob extends ProtegeJob {
	
	public static final long serialVersionUID = 223456792L;
	
	WfJob job = null;
	
	RemoteWfJob(KnowledgeBase kb, WfJob j) {
		super(kb);
		job = j;
	}
	
	public Object run() throws ProtegeException {		
		return job.run();
	}
}
