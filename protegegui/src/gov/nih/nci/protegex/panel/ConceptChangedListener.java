/**
 * 
 */
package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protegex.owl.model.*;

/**
 * @author bitdiddle
 *
 */
public interface ConceptChangedListener {
	
	void conceptChanged(OWLNamedClass concept, String msg);

}
