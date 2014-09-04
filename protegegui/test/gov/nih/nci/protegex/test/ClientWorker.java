/**
 * 
 */
package gov.nih.nci.protegex.test;

import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * @author Bob Dionne
 *
 */
public interface ClientWorker {
	
	public void init(OWLModel model, String fname);
	public void doWork(int iterate);
	public void cleanUp();

}
