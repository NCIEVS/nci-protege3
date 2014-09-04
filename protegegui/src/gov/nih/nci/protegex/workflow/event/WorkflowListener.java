/**
 * 
 */
package gov.nih.nci.protegex.workflow.event;

import java.util.EventListener;

/**
 * A simple listener interface that receives events from thw Workflow
 * class as methods like store Assignment are called
 * 
 * @see gov.nih.nci.protegex.workflow.Workflow
 * 
 * @author Bob Dionne
 *
 */
public interface WorkflowListener extends EventListener {
	
	/**
	 * handle the action that occured, create user or addignment, store and assignment
	 * or delete an assignment
	 * 
	 * @see WorkflowEvent.Type
	 * 
	 * @param evt
	 */
	void workActionPerformed(WorkflowEvent evt);

}
