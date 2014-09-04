/**
 * 
 */
package gov.nih.nci.protegex.codegen;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
/**
 * @author Bob Dionne
 *
 */
public class CodeGenerator {
	
	private KnowledgeBase kb = null;
	
	private String cfg_filename = null;
	
	public CodeGenerator(KnowledgeBase k) {kb = k;}
	
	public CodeGenerator(String local_props) {
		cfg_filename = local_props;		
	}
	
	public String getCode() {
		if (kb != null) {
			try {
				return (String) (new RemoteCodeGen(kb)).run();
			} catch (ProtegeException e) {
				Log.getLogger(getClass()).warning("Unable to generate a code, see server log");
				return null;
			}
		} else {
			return NCICodeGenerator.getInstance(cfg_filename).getCode();
		}
	}

}
