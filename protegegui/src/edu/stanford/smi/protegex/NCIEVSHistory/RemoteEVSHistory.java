/**
 * 
 */
package edu.stanford.smi.protegex.NCIEVSHistory;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;

/**
 * @author bitdiddle
 *
 */
public class RemoteEVSHistory extends ProtegeJob {
	
	public static final long serialVersionUID = 227456792L;
	
	private String[] currentRecord = null;
	
	public void setCurrentRecord(String[] r) {
		currentRecord = r;
	}

	/**
	 * @param kb
	 */
	public RemoteEVSHistory(KnowledgeBase kb) {
		super(kb);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.util.RemoteJob#run()
	 */
	@Override
	public Object run() throws ProtegeException {
		return NCIEVSDBConnector.getInstance(getKnowledgeBase()).recordRecord(currentRecord);
	}

}
