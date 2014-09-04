/**
 * 
 */
package gov.nih.nci.protegex.workflow;

/**
 * @author Flora B. Workflow 
 *
 */
public class WorkflowException extends Exception {
	
	/**
	 * Object serialization identifier.
	 */
	private static final long serialVersionUID = 751130814190345874L;

	public WorkflowException() {
		super();
	}

	public WorkflowException(String msg) {
		super(msg);
	}

	public WorkflowException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
