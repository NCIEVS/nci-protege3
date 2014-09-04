package gov.nih.nci.protegex.test;

import java.rmi.Naming;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import gov.nih.nci.protegex.workflow.Assignment;
import gov.nih.nci.protegex.workflow.Workflow;
import gov.nih.nci.protegex.workflow.WorkflowException;

public class CreateSuggestionTest{
    // Constant(s):
    private static final String HOSTNAME = "localhost";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String PROJNAME = "SmallBase";
    
    //Member Variable(s):
    private Logger _logger = Log.getLogger(getClass());
    private Workflow _workflow = null;
    
    /**
     * Instantiates this class.
     */
    public CreateSuggestionTest() {
        if (init())
            createSuggestion();
    }
    
    /**
     * Initializes this class.
     * @return true if no exception occurred.
     */
    private boolean init() {
        try {
            Project project = connect(HOSTNAME, USERNAME, PASSWORD, PROJNAME);
            _workflow = getWorkflow(project);
            return true;
        } catch (Exception e) {
            _logger.warning(e.getMessage());
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Connects to Protege server.
     * @param hostname The server hostname.
     * @param username The Protege user's name.
     * @param password The Protege user's password.
     * @param projectName The Protege project name.
     * @return The Protege project.
     * @throws Exception
     */
    private Project connect(String hostname, String username, 
        String password, String projectName) throws Exception {
        RemoteServer server = (RemoteServer) Naming.lookup("//" + hostname + "/"
                + Server.getBoundName());
        RemoteSession session = server.openSession(username,
                SystemUtilities.getMachineIpAddress(), password);
        if (session == null)
            throw new Exception("Invalid username and/or password.");
        RemoteServerProject serverProject = server.openProject(
                projectName, session);
        if (serverProject == null)
            throw new Exception("Invalid project name: " + projectName);
        Project project = RemoteClientProject.createProject(server,
                serverProject, session, true);
        return project;
    }
    
    /**
     * Opens a Workflow.
     * @param project The Project.
     * @return The Workflow.
     */
    private Workflow getWorkflow(Project project) {
        String name = project.getLocalUser();
        try {
            KnowledgeBase kb = project.getKnowledgeBase();
            return Workflow.openWorkflow(name, kb);
        } catch (WorkflowException e) {
            String msg = "Unable to open workflow: " + e.getMessage() + "\n"
                + name + " is currently not a Workflow user.\n"
                + "In order to access the Workflow functionalities, \n"
                + "a Workflow Manager must grant you the \n"
                + "proper Workflow privileges.";
            _logger.warning(msg);
            return null;
        }
    }
    
    /**
     * Creates a workflow assignment.
     * @param actualWork The actual work.
     * @param taskDescription The task description.
     * @param modelerNote The modeler's note.
     * @return The assignment.
     */
    private Assignment createAssignment(String actualWork, 
        String taskDescription, String modelerNote) {
        try {
            if (_workflow == null)
                return null;
            Assignment a = _workflow.createAssignment(true);
            if (actualWork != null)
                a.setTheActualWork(actualWork);
            if (taskDescription != null)
                a.setTaskDescription(taskDescription);
            if (modelerNote != null)
                a.setModelerNote(modelerNote);
            _workflow.storeAssignment(a);
            return a;
        } catch (WorkflowException e) {
            Log.getLogger().log(Level.WARNING, "Exception caught", e);
            return null;
        }
    }
    
    /**
     * Creates a test assignment.
     */
    private void createSuggestion() {
        String actualWork = "actualWork";
        String taskDescription = "taskDescription";
        String modelerNote = "modelerNote";
        Assignment a = createAssignment(actualWork, taskDescription, modelerNote);
        if (a == null)
            _logger.warning("Assignment not created.");
    }
    
    /**
     * Main Driver.
     * @param args The list of arguments.
     */
    public static void main(String[] args) {
        new CreateSuggestionTest();
    }
}
