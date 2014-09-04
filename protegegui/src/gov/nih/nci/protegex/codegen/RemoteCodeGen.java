/**
 * 
 */
package gov.nih.nci.protegex.codegen;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;

/**
 * @author Manual Re Count
 *
 */
public class RemoteCodeGen extends ProtegeJob {
	
	public static final long serialVersionUID = 223456792L;
	
	public RemoteCodeGen(KnowledgeBase kb) {
		super(kb);
	}
	
	public Object run() throws ProtegeException {	
		
		return NCICodeGenerator.getInstance().getCode(); 
		
	}

}
