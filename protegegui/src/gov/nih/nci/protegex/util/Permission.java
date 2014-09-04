package gov.nih.nci.protegex.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Sets the Protege permission for this application.
 *
 * @author David Yee
 */
public class Permission {
    // Constants:
    public static final String CREATE_ASSIGNMENT = "CreateWFAssignment";
    public static final String CREATE_SUGGESTION_ASSIGNMENT =
        "CreateWFSuggestionAssignment";
    public static final String EDIT_ASSIGNMENT = "EditWFAssignment";
    public static final String EDIT_USER = "EditWFUser";
    public static final String DELETE_ASSIGNMENT = "DeleteWFAssignment";

    // Member variables:
    private Logger _logger = Log.getLogger(getClass());
    private HashMap<String, Boolean> _hashMap;
    private OWLModel _owlModel;

    /**
     * Constructs this class.
     * @param kb The knowledge base.
     */
    public Permission(KnowledgeBase kb) {
        _owlModel = (OWLModel) kb;
        _hashMap = new HashMap<String, Boolean>();
    }

    /**
     * Sets a permission based on what is stored in the remote client
     *   frame store.
     * @param permission The permission.
     */
    public void set(String permission) {
        if (_hashMap.containsKey(permission))
            return;
        //TODO: DB inclusions
        
        _hashMap.put(permission, new Boolean(
            RemoteClientFrameStore.isOperationAllowed(
                _owlModel, new UnbackedOperationImpl(permission, permission))));
                
    }

    /**
     * Returns true if this permission is allowed.
     * @param permission The permission.
     * @return true if this permission is allowed.
     */
    public boolean isAllowed(String permission) {
        if (! _hashMap.containsKey(permission))
            return false;
        Boolean allowed = (Boolean) _hashMap.get(permission);
        return allowed.booleanValue();
    }

    /**
     * Prints the permission to see if it is allowed.
     * @param permission The permission.
     * @param prefix The prefix string before the permission is printed.
     *   Can be used for indenting.
     */
    public void print(String permission, String prefix) {
        _logger.fine(prefix + permission + " = " + isAllowed(permission));
    }

    /**
     * Prints the permission to see if it is allowed.
     * @param permission The permission.
     */
    public void print(String permission) {
        print(permission, "");
    }

    /**
     * Prints the list of permissions to see if they are allowed.
     */
    public void print() {
        Object[] keys = _hashMap.keySet().toArray();
        Arrays.sort(keys);
        _logger.fine("Workflow Permission(s):");
        for (int i=0; i<keys.length; ++i)
            print((String) keys[i], "    ");
    }

    /**
     * Returns true if logged in protege user is a workflow admin.
     * @return true if loggin in protege user is a workflow admin.
     */
    public boolean isWorkflowAdmin() {
        return isAllowed(DELETE_ASSIGNMENT);
    }

    /**
     * Returns true if logged in protege user is a workflow modeler.
     * returns true if logged in protege user is a workflow modeler.
     */
    public boolean isWorkflowModeler() {
        return ! isWorkflowAdmin() && isAllowed(CREATE_SUGGESTION_ASSIGNMENT);
    }

    /**
     * Returns true if logged in protege user is a workflow user.
     * @return true if loggin in protege user is a workflow user.
     */
    public boolean isWorkflowUser() {
        return isAllowed(CREATE_ASSIGNMENT) ||
            isAllowed(CREATE_SUGGESTION_ASSIGNMENT);
    }
}
