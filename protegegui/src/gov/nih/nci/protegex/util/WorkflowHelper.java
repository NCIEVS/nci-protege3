package gov.nih.nci.protegex.util;

import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.workflow.Assignment;
import gov.nih.nci.protegex.workflow.Workflow;
import gov.nih.nci.protegex.workflow.WorkflowException;
import gov.nih.nci.protegex.workflow.WorkflowUser;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * This singleton class provides a generic way of creating assignments 
 * anywhere within Protege.  Once an instance is retrieved, a developer
 * can call any of its createAssignment methods as well as other utility
 * methods.
 * 
 * @author David Yee
 */
public class WorkflowHelper {
    // Member variables:
    private static WorkflowHelper _instance = null;
    private Workflow _workflow;
    
    /**
     * Constructs this class.
     */
    private WorkflowHelper()
    {
    }
    
    /**
     * Sets the Workflow object.
     * @param workflow The Workflow.
     */
    private void set(Workflow workflow) {
        _workflow = workflow;
    }
    
    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static WorkflowHelper getInstance() {
        if (_instance == null)
            _instance = new WorkflowHelper();
        return _instance;
    }
    
    /**
     * Sets and returns the singleton instance of this class.
     * @param workflow The Workflow.
     * @return the singleton instance of this class.
     */
    public static WorkflowHelper getInstance(Workflow workflow) {
        _instance = getInstance();
        _instance.set(workflow);
        return _instance;
    }
    
    /**
     * Returns true if Workflow is currently active.
     * @return true if Workflow is currently active.
     */
    public boolean isWorkflowActive() {
        return _workflow != null;
    }
    
    /**
     * Returns the current logged in Workflow user.
     * @return the current logged in Workflow user.
     */
    public WorkflowUser getCurrentUser() {
        return _workflow.getUser();
    }
    
    /**
     * Returns true if current user is an administrator.
     * @return true if current user is an administrator.
     */
    public boolean isCurrentUserAdmin() {
        return WorkflowUtil.isAdmin(_workflow.getUser().getUserRole());
    }
    
    public ArrayList<WorkflowUser> getAllUsers() {
    	if (isCurrentUserAdmin()) {
    		try {
    		return _workflow.getAllUsers();
    		} catch (Exception e) {
    			return null;
    		}
    	}
    	return null;
    }

    /**
     * Creates a Workflow assignment.
     * @param modeler The modeler name.
     * @param status The status value.
     * @param actualWork The actual work value.
     * @param taskDescription The task description value.
     * @param workSource The work source value.
     * @param modelerNote The modeler's note value.
     * @return the newly created Workflow assignment.
     */
    public Assignment createAssignment(String modeler, Assignment.Status status, 
            String actualWork, String taskDescription, String workSource, 
            String modelerNote) {
        try {
            if (_workflow == null)
                return null;
            boolean suggested = ! isCurrentUserAdmin();
            Assignment a = _workflow.createAssignment(suggested);
            if (modeler != null)
                a.setModeler(modeler);
            if (status != null)
                a.setStatus(status);
            if (actualWork != null)
                a.setTheActualWork(actualWork);
            if (taskDescription != null)
                a.setTaskDescription(taskDescription);
            if (workSource != null)
                a.setWorkSource(workSource);
            if (modelerNote != null)
                a.setModelerNote(modelerNote);
            //a.setLastUpdated(new Date());
            _workflow.storeAssignment(a);
            return a;
        } catch (WorkflowException e) {
            Log.getLogger().log(Level.WARNING, "Exception caught", e);
            return null;
        }
    }

    /**
     * Creates a default Workflow assignment.
     * @param modeler The modeler's name.
     * @return the newly created Workflow assignment.
     */
    public Assignment createDefaultAssignmentForModeler(String modeler) {
        return createAssignment(modeler, null, null, null, null, null);
    }
    
    /**
     * Creates a default Workflow assignment.
     * @param actualWork The actual work value.
     * @param taskDescription The task description value.
     * @param modelerNote The modeler note.
     * @return the newly created Workflow assignment.
     */
    public Assignment createDefaultTaskAssignment(String user, String actualWork, 
        String taskDescription, String modelerNote) {
        String formattedNote = WorkflowUtil.formatModelerNote(modelerNote, 
            isCurrentUserAdmin() ? "Created" : "Suggested");
        Assignment.Status status = null;
        if (isCurrentUserAdmin()) {
        	status = Assignment.Status.ASSIGNED;
        } else {
        	status = Assignment.Status.SUGGESTED;
        }
        return createAssignment(user, status, actualWork, taskDescription, 
            null, formattedNote);
    }
    
    /**
     * Stores a Workflow assignment.
     * @param assignment The assignment.
     * @return true is successful.
     */
    public boolean storeAssignment(Assignment assignment) {
        try {
            if (assignment == null)
                return false;
            if (assignment.getCurrentStatus() == Assignment.Status.ACCEPTED ||
                    (assignment.getCurrentStatus() == Assignment.Status.CREATED && isCurrentUserAdmin())) {
                assignment.setStatus(Assignment.Status.UPDATED);
            }
            
            _workflow.storeAssignment(assignment);
            return true;
        } catch (WorkflowException e) {
            Log.getLogger().log(Level.WARNING, "Exception caught", e);
            return false;
        }
    }
}
