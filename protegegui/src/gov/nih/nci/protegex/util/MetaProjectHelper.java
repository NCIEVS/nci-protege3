package gov.nih.nci.protegex.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protege.util.URIUtilities;
import gov.nih.nci.protegex.workflow.WorkflowUser;

public class MetaProjectHelper extends ProtegeJob implements Serializable {
	
    private static final long serialVersionUID = 1462754518885525160L;
    private static final String METAPROJECT = System.getenv("METAPROJECT");
    private static final String WF_MODELER_GROUP = "NCI_Workflow_Modeler";
    private static final String WF_MANAGER_GROUP = "NCI_Workflow_Manager";
    private enum Mode { DoNothing, UserExists, CreateUser, ModifyUser };

    private static Logger _logger = Logger.getLogger(
            MetaProjectHelper.class.getName());
    private MetaProject _mp = null;
    private Mode _mode = Mode.DoNothing;
    private String _userName;
    private String _password;
    private WorkflowUser.Type _role = WorkflowUser.Type.MODELER;

    /**
     * Constructor.
     * @param kb
     */
    public MetaProjectHelper(KnowledgeBase kb) {
        super(kb);
    }

    /**
     * Prints a debug statement.
     * @param text The printed string.
     */
    private void debug(String text) {
        _logger.info(text);
    }
    
//    /**
//     * Prints debug information about a user.
//     * @param name User's name.
//     */
//    private void debugUser(String name) {
//        User user = _mp.getUser(name);
//        String value = user != null ? "exists" : "does not exist";
//        debug("User " + name + ": " + value);
//    }
    
    /**
     * Prints the list of users from a particular group.
     * @param name Group name.
     */
    private void debugGroup(String name) {
        Group group = _mp.getGroup(name);
        if (group == null) {
            debug(name + " group does not exist.");
            return;
        }
        Set<User> users = group.getMembers();
        Iterator iterator = users.iterator();
        debug(group.getName() + " group has the following members:");
        while (iterator.hasNext()) {
            User user = (User) iterator.next();
            debug("  " + user.getName());
        }
    }

    /**
     * Verifies if a user exists in a group.
     * @param user User object.
     * @param group Group object.
     * @return true if user already exists in the specified group.
     */
    private boolean userExistsInGroup(User user, Group group) {
        Set<User> users = group.getMembers();
        Iterator iterator = users.iterator();
        while (iterator.hasNext()) {
            User u = (User) iterator.next();
            if (u.equals(user))
                return true;
        }
        return false;
    }
    
    /**
     * Adds a user to a group.
     * @param user User object.
     * @param groupName Group name.
     * @return true if user is added to the specified group.
     */
    private boolean addUserToGroup(User user, String groupName) {
        Group group = _mp.getGroup(groupName);
        if (group == null) {
            _logger.warning(groupName + " group does not exist.");
            return false;
        }
        if (userExistsInGroup(user, group)) {
            _logger.warning(user.getName() + " already exists in " + 
                    groupName + " group.");
            return false;
        }
        group.addMember(user);
        return true;
    }

    /**
     * Adds a user to a protege group based on the workflow role.
     * @param user User object.
     * @param role Workflow role.
     */
    private void addUserRole(User user, WorkflowUser.Type role) {
        if (role == WorkflowUser.Type.BOTH) {
            addUserToGroup(user, WF_MANAGER_GROUP);
            addUserToGroup(user, WF_MODELER_GROUP);
        } else if (role == WorkflowUser.Type.ADMIN) {
            addUserToGroup(user, WF_MANAGER_GROUP);
        } else if (role == WorkflowUser.Type.MODELER) {
            addUserToGroup(user, WF_MODELER_GROUP);
        }            
        debugGroup(WF_MODELER_GROUP);
        debugGroup(WF_MANAGER_GROUP);
    }
    
    /**
     * Verifies if the user exists.
     * @param userName User's name.
     * @return true if users exists.
     */
    public Boolean userExists(String userName) {
        _mode = Mode.UserExists;
        _userName = userName;
        return ((Boolean) execute()).booleanValue();
    }
    
    /**
     * Verifies user from server side.
     * @return true if users exists.
     */
    private Object userExists() {
        System.out.println("The luser is " + _userName);
        
        //return new Boolean(_mp.getUser(_userName) != null);
        User user = _mp.getUser(_userName);
        Boolean value = new Boolean(user != null); 
        System.out.println("  * userExists == " + value);
        return value;
    }
    
    /**
     * Lists of create user statuses. 
     */
    public enum CreateUserStatus { SUCCESSFUL, FAILURE, ALREADY_EXISTS };

    /**
     * Creates a protege user.
     * @param userName User's name.
     * @param password User's password.
     * @param role User's workflow role.
     * @return CreateUserStatus.
     */
    public CreateUserStatus createUser(String userName, String password,
            WorkflowUser.Type role) {
        _mode = Mode.CreateUser;
        _userName = userName;
        _password = password;
        _role = role;
        return (CreateUserStatus) execute();
    }

    /**
     * Creates a user from the server side.
     * @return CreateUserStatus.
     */
    private Object createUser() {
        User user = _mp.getUser(_userName);
        if (user != null) {
            _logger.warning(user.getName() + " already exists.");
            return CreateUserStatus.ALREADY_EXISTS;
        }

        user = _mp.createUser(_userName, _password);
        addUserRole(user, _role);
        _mp.save(null);
        return CreateUserStatus.SUCCESSFUL;
    }

    /**
     * Lists of modify user statuses. 
     */
    public enum ModifyUserStatus { SUCCESSFUL, FAILURE, DOES_NOT_EXIST };

    /**
     * Modifies a protege user.
     * @param userName User's name.
     * @param role User's workflow role.
     * @return ModifyUserStatus.
     */
    public ModifyUserStatus modifyUser(String userName, WorkflowUser.Type role) {
        _mode = Mode.ModifyUser;
        _userName = userName;
        _role = role;
        return (ModifyUserStatus) execute();
    }

    /**
     * Modifies a user from the server side.
     * @return ModifyUserStatus.
     */
    private Object modifyUser() {
        User user = _mp.getUser(_userName);
        if (user == null) {
            _logger.warning(user.getName() + " does not exists.");
            return ModifyUserStatus.DOES_NOT_EXIST;
        }

        addUserRole(user, _role);
        _mp.save(null);
        return ModifyUserStatus.SUCCESSFUL;
    }

    /**
     * Runs a particular job based on current mode.
     * @return a specific object from the job.
     */
    public Object run() throws ProtegeException {
        // Note: _mp could not be initialized in the constructor because it 
        //    will load the client's version instead of the server's version.
        //if (_mp == null)
            //_mp = new MetaProjectImpl(URIUtilities.createURI("examples/metaproject.pprj"));
            System.out.println("METAPROJECT: " + METAPROJECT);
            _mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));
        
        Mode currMode = _mode;
        _mode = Mode.DoNothing;
        
        if (currMode == Mode.UserExists)
            return userExists();
        if (currMode == Mode.CreateUser)
            return createUser();
        if (currMode == Mode.ModifyUser)
            return modifyUser();
        return null;
    }
}
