/**
 * 
 */
package edu.stanford.smi.protegex.NCIEVSHistory;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;


/**
 * @author bitdiddle
 *
 */
public class EVSHistory {
	
	private KnowledgeBase kb = null;
	private RemoteEVSHistory revs = null;
	
	public EVSHistory(KnowledgeBase k) {
		kb = k;
		revs = new RemoteEVSHistory(kb);
		
	}
	
	public void recordHistory(String[] record) {
		revs.setCurrentRecord(record);
		try {
			revs.execute();
		} catch (ProtegeException e) {
			Log.getLogger(getClass()).warning("Unable to generate a code, see server log");
		}
		
	}

}
