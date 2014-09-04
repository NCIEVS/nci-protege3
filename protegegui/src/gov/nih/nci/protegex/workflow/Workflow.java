/**
 * 
 */
package gov.nih.nci.protegex.workflow;

import edu.stanford.smi.protege.model.KnowledgeBase;
import gov.nih.nci.protegex.workflow.Assignment.Status;
import gov.nih.nci.protegex.workflow.WorkflowUser.Type;
import gov.nih.nci.protegex.workflow.event.WorkflowEvent;
import gov.nih.nci.protegex.workflow.event.WorkflowListener;
import gov.nih.nci.protegex.workflow.storage.WorkflowStore;
import gov.nih.nci.protegex.workflow.storage.WorkflowStoreProtegeJob;
import gov.nih.nci.protegex.workflow.wiki.BiomedGTParser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Workflow is the main container class and housekeeper for the Workflow
 * component. The major object of interest is the Assignment, a unit of work
 * assigned to a modeler on a certain date. The primary task of workflow is to
 * track the progress of these assignments as they are worked on and to ensure
 * they are completed. Auditing reports can be generated based on the
 * assignments.
 * 
 * As a first pass, the file system is used for persistence, on the Protege
 * Server. Assignments will be serialized to XML and stored as files, one folder
 * for each modeler. Each assignment, uniquely identified by an integer id will
 * be stored in an XML file in the folder for the assigned modeler.
 * 
 * The Workflow class is responsible for maintaining the relationships between
 * Protege Users and Workflow store. The Protege login will be use to validate
 * users. Users can have one of three roles, manager, modeler or both, The
 * Workflow storage layer uses the user name to create unique storage area for
 * each user. Although the storage layer is based on the file system, it is
 * readily remoted over RMI using a utility class in Protege.
 * 
 * The Workflow class will also enforce the restrictions based on the user
 * roles. For example only admin users can create other users, and make
 * assignments. Modelers can only see their own assignments
 * 
 * @see WorkflowUser.Type
 * 
 * @author Flora B. Workflow
 * 
 */
public class Workflow {

    public static Logger logger = Logger.getLogger(Workflow.class.getName());

    public enum RequestedAction {
        EDIT, MODIFY, ACCEPT, COMPLETE, REJECT, DELETE
    }

    /**
     * canDo decides if the requested action is allowed on the given assignment.
     * An assignment is owned by a workflow user and it is assigned to a
     * modeler. An instance of this workflow class also has a user associated
     * with it corresponding to whoever is logged in. Together with the current
     * state of the assignment the decision as to whether or not a requested
     * action is doable can be made.
     * 
     * @param assignment
     *            the assignment on which the action is to be performed
     * @param action
     *            the requested action
     * @return true is the action is allowed, false otherwise
     */
    public boolean canDo(Assignment assignment, RequestedAction action) {

        // there are two major cases, based on the workflow user being
        // either a manager(admin) or modeler
        if (user.getUserRole() == WorkflowUser.Type.ADMIN) {

            if ((action == RequestedAction.EDIT)
                    || (action == RequestedAction.MODIFY)) {
                return true;
            } else if (action == RequestedAction.DELETE) {
                /**
                 * This is logic pulled from WorkflowUtil
                 */
                Assignment.Status status = assignment.getCurrentStatus();
                if (status == Assignment.Status.CREATED
                        || status == Assignment.Status.COMPLETED
                        || status == Assignment.Status.REJECTED
                        || status == Assignment.Status.SUGGESTED)
                    return true;
                if (user.getUserName()
                        .equalsIgnoreCase(assignment.getModeler())) {
                    return true;
                }
                return false;

            } else {
                Assignment.Status status = assignment.getCurrentStatus();
                boolean acceptOrUpdatedOrAssigned = (status == Assignment.Status.ASSIGNED
                        || status == Assignment.Status.ACCEPTED || status == Assignment.Status.UPDATED);

                if ((action == RequestedAction.EDIT)
                        || (action == RequestedAction.MODIFY)
                        || (action == RequestedAction.COMPLETE)
                        || (action == RequestedAction.REJECT)) {
                    return assignment.getModeler().equalsIgnoreCase(user.getUserName());
                }
                return false;
            }

        } else {
            Assignment.Status status = assignment.getCurrentStatus();
            boolean acceptOrUpdatedOrAssigned = (status == Assignment.Status.ASSIGNED
                    || status == Assignment.Status.ACCEPTED || status == Assignment.Status.UPDATED);

            if ((action == RequestedAction.EDIT)
                    || (action == RequestedAction.MODIFY)
                    || (action == RequestedAction.COMPLETE)
                    || (action == RequestedAction.REJECT)) {
                return acceptOrUpdatedOrAssigned;
            }

            if (action == RequestedAction.MODIFY
                    || action == RequestedAction.DELETE) {
                if (status == Assignment.Status.SUGGESTED) {
                    return true;
                }
            }

            if (action == RequestedAction.ACCEPT) {
                return (status == Assignment.Status.ASSIGNED);
            } else {
                // request for delete
                return false;
            }
        }

    }

    /**
     * A protege OWL model is needed to reconcile terms in the Kb with new
     * proposals from the Wiki
     */
    // private OWLModel model = null;
    // private RdfToOwlConverter rtoConvert = null;
    /**
     * intiialize a logger to record various events
     */
    static {
        try {
            FileHandler fh = new FileHandler("logs" + File.separatorChar
                    + "workflow_%u.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.severe("can't configure logger, goodbye, you lose");
        }
    }

    /**
     * Every Workflow object belongs to a user, when created the user must be
     * specified. The user's role determines the operations this Workflow object
     * can perform
     */
    private WorkflowUser user = null;

    /**
     * retrieve the Workflow user that created this Workflow object
     * 
     * @return A Workflow User
     */
    public WorkflowUser getUser() {
        return user;
    }

    /**
     * a simple listener mechanism to notify registered listeners when
     * assignments and users are created modified and/or deleted.
     * 
     * @see WorkflowListener
     */
    private ArrayList<WorkflowListener> listeners = new ArrayList<WorkflowListener>();

    /**
     * register a listener to receive events
     * 
     * @see WorkflowListener
     * 
     * @param wfl
     *            A WorkflowListener
     */
    public void addWorkflowListener(WorkflowListener wfl) {
        listeners.add(wfl);
    }

    private void processWorkflowEvent(WorkflowEvent wfe) {
        for (WorkflowListener wfl : listeners) {
            wfl.workActionPerformed(wfe);
        }
    }

    /**
     * Look up a WorkflowUser by name. Only a Workflow manager can perform this
     * operation
     * 
     * @param name
     * @return the WorkflowUser with the given name
     * @throws WorkflowException
     */
    public WorkflowUser getUser(String name) throws WorkflowException {

        checkUserRole(Type.ADMIN);
        // since the number of users is small to begin with just retrieve them
        // all
        // and loop over them
        ArrayList<WorkflowUser> allUsers = getAllUsers();
        for (WorkflowUser user : allUsers) {
            if (user.getUserName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;

    }

    /**
     * Retrieve all the Workflow users as an ArrayList
     * 
     * @return all users, both managers and modelers
     * @throws WorkflowException
     * 
     */
    public ArrayList<WorkflowUser> getAllUsers() throws WorkflowException {

        checkUserRole(Type.ADMIN);

        String[] users = store.getAllUsers();
        ArrayList<WorkflowUser> res = new ArrayList<WorkflowUser>(users.length);

        for (int i = 0; i < users.length; i++) {

            // Workflow User name are modified to be more sable as folder names.
            // Spaces in the names are replaced by "_". The spaces are put back
            // when the names are retrieved.
            StringTokenizer tok = new StringTokenizer(users[i]);
            String name = tok.nextToken();
            String role = tok.nextToken();
            res.add(new WorkflowUser(name.replaceAll("@@@", " "),
                                     Type.getType(Integer.parseInt(role))));

        }

        WorkflowUser[] as = new WorkflowUser[res.size()];
        res.toArray(as);
        Arrays.sort(as);
        res = new ArrayList<WorkflowUser>(Arrays.asList(as));

        return res;

    }

    /**
     * An underlying WorkflowStore object. This is an interface, enabling
     * multiple implementations of the store. Currently we're using a simple
     * scheme that leverages the ProtegeJob utility in Protege. It enables codes
     * to execute over RMI on the server. On the server we use a simple file
     * based persistence scheme where each assignment is stored as an XML file
     * 
     * @see edu.stanford.smi.protege.util.ProtegeJob
     */
    private WorkflowStore store = null;

    /**
     * package private, as the Workflow object must be created using the static
     * method that first validates a user.
     * 
     * @see Workflow#createWorkflow(String, WorkflowUser.Type, KnowledgeBase)
     * 
     * @see Workflow#openWorkflow(String, KnowledgeBase)
     * @param u
     * @param kb
     *            knowledge base is needed for remote ProtegeJob class.
     *            Currently if this is null the local file system is used. This
     *            will make life simple if we want to support distributed
     *            workflow stores in the future
     */
    Workflow(WorkflowUser u, KnowledgeBase kb) {
        user = u;
        // model = (OWLModel) kb;
        store = new WorkflowStoreProtegeJob(kb);
        changePoller = new ChangePoller(10000);
        changePoller.start();

    }

    /**
     * Open an existing Workflow store. If the user doesn't exist an exception
     * is thrown. It usually means create should have been called
     * 
     * @see Workflow#createWorkflow(String, WorkflowUser.Type, KnowledgeBase)
     * 
     * @param userName
     *            the owner of the store
     * @param kb
     * @return a Workflow object
     * @throws WorkflowException
     */
    public static Workflow openWorkflow(String userName, KnowledgeBase kb)
                                                                          throws WorkflowException {

        WorkflowStore wf = new WorkflowStoreProtegeJob(kb);

        if (!wf.userExistsP(userName)) {
            throw new WorkflowException("User not found in store");
        }

        int roleint = wf.userRole(userName);
        Type role = Type.getType(roleint);

        return new Workflow(new WorkflowUser(userName, role), kb);

    }

    /**
     * createWorkflow is used to create a new workflow object for the given
     * user, who has the specified role. In the default store implementation the
     * user names is used to create a directory in the filesystem. If the user
     * already exists an exception is thrown
     * 
     * @see Workflow#openWorkflow(String, KnowledgeBase)
     * 
     * @param userName
     * @param kb
     * @return a Workflow object
     * @throws WorkflowException
     *             if the User exists.
     */
    public static Workflow createWorkflow(String userName, Type role,
                                          KnowledgeBase kb)
                                                           throws WorkflowException {

        // create a temp store for use only in this static method
        WorkflowStore wf = new WorkflowStoreProtegeJob(kb);

        if (wf.userExistsP(userName)) {
            throw new WorkflowException("User already exists in store");
        }

        int rint = wf.createUser(userName, role.getName());

        return new Workflow(new WorkflowUser(userName, Type.getType(rint)), kb);

    }

    /**
     * Only an ADMIN can create new assignments, unless the assignment is a
     * suggestion. When it is created the creation date is set to the current
     * date. The assignment id is generated by the store and is guaranteed
     * unique across all assignments. No modeler id is set until it is assigned,
     * except if the user has BOTH privileges, in which case the modelerid is
     * originally set to the managerid.
     * 
     * @see Assignment
     * @see WorkflowUser.Type#ADMIN
     * 
     * @return an Assignment object
     * @throws WorkflowException
     */
    public Assignment createAssignment(boolean isSuggestion)
                                                            throws WorkflowException {

        if (!isSuggestion) {
            checkUserRole(Type.ADMIN);
        }

        int id = store.getNextAssignmentId();

        Assignment nass = new Assignment();

        if (isSuggestion) {
            nass.setStatus(Status.SUGGESTED);
        }
        nass.setIdentifier(id);
        nass.setDateCreated(new Date());
        nass.setManager(user.getUserName());

        // by default the modeler is set to the manager who creates the
        // assignment
        // If this is stored before assigning this, it gets treated like a
        // reassign
        // when it is finally set
        nass.setModeler(user.getUserName());

        // TODO: throw events here, discuss with Dave
        processWorkflowEvent(new WorkflowEvent(
                                               WorkflowEvent.Type.CREATEASSIGNMENT,
                                               nass));

        return nass;

    }

    private ArrayList<Assignment> retrieveAllAssignments(WorkflowUser usr)
                                                                          throws WorkflowException {

        // A user can retrieve their own assignments but must have ADMIN
        // privileges to retrieve others
        if (!user.getUserName().equalsIgnoreCase(usr.getUserName())) {
            checkUserRole(Type.ADMIN);
        }

        String[] res = store.getAllAssignments(usr.getUserName());

        ArrayList<Assignment> retres = null;

        if (res != null) {

            retres = new ArrayList<Assignment>(res.length);

            for (int i = 0; i < res.length; i++) {

                XmlPullParser parser = null;

                // Assignment a = null;

                try {
                    parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setInput(new StringReader(res[i]));
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                                      true);

                } catch (XmlPullParserException e) {
                    Workflow.logger.severe("Error while creating new parser"
                            + e.toString());

                }

                AssignmentParser assignParser = new AssignmentParser(
                                                                     parser,
                                                                     Workflow.logger);
                try {
                    assignParser.processDocument();
                    retres.add(assignParser.getAssignment());
                } catch (Exception e) {
                    Workflow.logger.severe("Error while parsing "
                            + e.toString());

                }

            }

        } else {
            retres = new ArrayList<Assignment>();
        }

        return retres;

    }

    /**
     * retrieve all the assignments for a given user. Only user's with ADMIN
     * privileges can retrieve the assignments of others. So if the owner of a
     * Workflow object is only a modeler, they can only retrive their own
     * assignments.
     * 
     * @param usr
     *            The user whose assignments are
     * @return an ArrayList of assignments
     * @throws WorkflowException
     */
    public ArrayList<Assignment> getAllAssignments(WorkflowUser usr)
                                                                    throws WorkflowException {

        ArrayList<Assignment> retres = retrieveAllAssignments(usr);

        changePoller.setAssignmentView(retres, usr);

        return retres;
    }

    private ArrayList<Assignment> retrieveAllAssignments()
                                                          throws WorkflowException {

        checkUserRole(Type.ADMIN);

        ArrayList<Assignment> allAssignments = new ArrayList<Assignment>();

        ArrayList<WorkflowUser> users = getAllUsers();

        for (WorkflowUser user : users) {
            ArrayList<Assignment> assigns = retrieveAllAssignments(user);
            for (Assignment a : assigns) {
                allAssignments.add(a);
            }
        }

        return allAssignments;

    }

    /**
     * retrieves all assignments for all the users This requires ADMIN
     * privileges
     * 
     * @return an ArrayList of Assignment objects
     * 
     * @throws WorkflowException
     */
    public ArrayList<Assignment> getAllAssignments() throws WorkflowException {

        ArrayList<Assignment> allAssignments = retrieveAllAssignments();

        changePoller.setAssignmentView(allAssignments, null);
        return allAssignments;

    }

    /**
     * Only an admin can reassign an assignment, from one modeler to another
     * Modeler can save their own assignments as the fields are updated. A
     * modeler can only save a new assignment if it's a suggestion.
     * 
     * @param a
     *            an Assignment to store
     */
    public void storeAssignment(Assignment a) throws WorkflowException {

        if (a.getCurrentStatus() == Status.SUGGESTED) {
            // anyone can make a suggestion
        } else {
            if (user.getUserName().equalsIgnoreCase(a.getModeler())) {
                // okay modeler is storing their own
            } else {
                // user and assignment modeler differ, this require admin
                checkUserRole(Type.ADMIN);
            }
        }

        // first check if this is a reassign
        if (a.getPreviousModeler() != null) {
            // then delete assignment provided the user is an admin

            checkUserRole(Type.ADMIN);
            store.deleteAssignment(a.getPreviousModeler(), a.getIdentifier());

        }

        a.upDateLastUpdate();
        store.storeAssignment(a.getModeler(), a.toXML(), a.getIdentifier());

        processWorkflowEvent(new WorkflowEvent(
                                               WorkflowEvent.Type.STOREASSIGNMENT,
                                               a));

    }

    /**
     * Delete the asignment from the store.
     * 
     * @param a
     *            the assignment to delete
     * @throws WorkflowException
     */
    public void deleteAssignment(Assignment a) throws WorkflowException {

        Assignment.Status status = a.getCurrentStatus();

        if ((status == Status.CREATED) || (status == Status.ASSIGNED)
                || (status == Status.COMPLETED) || (status == Status.REJECTED)) {
            checkUserRole(Type.ADMIN);
            store.archiveAssignment(a.getModeler(), a.toXML(),
                                    a.getIdentifier());
            store.deleteAssignment(a.getModeler(), a.getIdentifier());
        } else if (status == Status.SUGGESTED) {
            // checkUserRole(Type.MODELER);
            store.deleteAssignment(a.getModeler(), a.getIdentifier());
        } else {
            throw new WorkflowException(
                                        "Assignment cannot be deleted, user does not have permission or assignment is in process");
        }

        processWorkflowEvent(new WorkflowEvent(
                                               WorkflowEvent.Type.DELETEASSIGNMENT,
                                               a));

    }

    /**
     * Create a new user with the given name and role. Names must be unique.
     * Roles are specified using ints in the store. A negative -1 indicates a
     * problem assigning the correct role
     * 
     * @see Type
     * 
     * @param userName
     *            the name of the user. It must be unique
     * @param role
     * @return a valid Workflow User
     * @throws WorkflowException
     */
    public WorkflowUser createUser(String userName, Type role)
                                                              throws WorkflowException {

        checkUserRole(Type.ADMIN);

        if (store.userExistsP(userName)) {
            throw new WorkflowException(
                                        "Cannot create, user name already in use");
        }

        int res = store.createUser(userName, role.getName());

        if (res < 1) {
            throw new WorkflowException("Unable to create valid user");
        }

        WorkflowUser user = new WorkflowUser(userName, Type.getType(res));

        processWorkflowEvent(new WorkflowEvent(WorkflowEvent.Type.CREATEUSER,
                                               user));

        return user;

    }

    /**
     * This delimiter constant is used to delimit multiple values stored in the
     * Assignment's actual work field.
     */
    public static final String ACTUAL_WORK_DELIMITER = "@_DELIMITER_@";

    /**
     * This method communicates with a SMW using the given url and produces an
     * array of assignments. It can only be executed by a Workflow manager and
     * the asignments are created for the manager. She can them manually assign
     * them to others modelers as required. The assignments are roughly
     * characterized as one of three types:
     * 
     * <ul>
     * <li>Structured proposals for new terms</li>
     * <li>Structure proposals for modifications to existing terms</li>
     * <li>Unstructured proposals</li>
     * </ul>
     * 
     * @see BiomedGTParser
     * @see Assignment
     * @param url
     * @param topPage
     *            The top page when parsing. When topPage is null, then this
     *            method uses the DEFAULT_EXPORT_LIST as its top page.
     * @param nsPrefix
     *            The namespace prefix.
     * @return an ArrayList of Assignments
     */
    public ArrayList<Assignment> importAssignmentsFromWiki(String url,
                                                           String topPage,
                                                           String nsPrefix)
                                                                           throws WorkflowException {
        checkUserRole(Type.ADMIN);
        BiomedGTParser parser = new BiomedGTParser(url);
        nsPrefix = BiomedGTParser.adjustNSPrefix(nsPrefix);

        String importUrl = topPage + "?namespace=" + nsPrefix;
        ArrayList<BiomedGTParser.Proposal> imports = null;
        try {
            imports = parser.importPackets(importUrl, nsPrefix);
        } catch (Exception e) {
            throw new WorkflowException(e.getMessage());
        }

        if (imports == null)
            throw new WorkflowException("Unable to import from SMW, bad URL?");

        ArrayList<Assignment> assignments = new ArrayList<Assignment>();
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        for (BiomedGTParser.Proposal proposal : imports) {
            String pkgURL = proposal.getPackageURL();
            String propURL = proposal.getProposalURL();
            String item = proposal.getContent();

            Integer pkgId = map.get(pkgURL);
            if (pkgId == null) {
                pkgId = new Integer(getNextPackageId());
                map.put(pkgURL, pkgId);
            }

            Assignment ao = createAssignment(false);
            ao.setPackageId(pkgId.intValue());

            if (parser.isStructured(item))
                ao.setStructured(true);
            ao.setTaskDescription(parser.isAbout(pkgURL, item));
            ao.setTheActualWork(item + ACTUAL_WORK_DELIMITER + pkgURL
                    + ACTUAL_WORK_DELIMITER + propURL);
            assignments.add(ao);
        }

        return assignments;
    }

    /**
     * A helper method that checks the role of the user that owns this workflow
     * object against a role needed for a given operation. For example if the
     * method createAssignment is called the user must have ADMIN privileges
     * 
     * @param role
     *            the role to check against
     * @throws WorkflowException
     *             if the user hasn't the right privileges
     */
    private void checkUserRole(Type role) throws WorkflowException {

        if (user.getUserRole() == Type.BOTH) {
            return;
        } else if (user.getUserRole() != role) {
            throw new WorkflowException(
                                        "User does not have required privileges");
        } else {
            return;
        }
    }

    private ChangePoller changePoller = null;

    private class ChangePoller extends Thread {

        private long pollInterval = 10000;

        private long lastTime = 0;

        private ArrayList<Assignment> currentlyViewedAssignments = null;

        private WorkflowUser currentlyViewedUser = null;

        private boolean existsInArrayList(Assignment a, ArrayList<Assignment> al) {
            for (Assignment as : al) {
                if (a.getIdentifier() == as.getIdentifier()) {
                    return true;
                }
            }
            return false;
        }

        public void setAssignmentView(ArrayList<Assignment> a, WorkflowUser u) {
            currentlyViewedAssignments = a;
            currentlyViewedUser = u;
        }

        public ChangePoller(long pin) {
            pollInterval = pin;
            lastTime = System.currentTimeMillis();
        }

        public void run() {

            while (true) {

                try {
                    Thread.sleep(pollInterval);

                } catch (InterruptedException e) {
                    logger.warning("poller interrupted, not a likely event");
                }

                ArrayList<Assignment> ta = null;

                try {
                    if (currentlyViewedUser != null) {
                        ta = retrieveAllAssignments(currentlyViewedUser);
                    } else if (currentlyViewedAssignments == null) {
                    } else {
                        ta = retrieveAllAssignments();
                    }

                } catch (WorkflowException wfe) {
                    logger.warning("Poller can't retireve assignments");
                }

                if (ta != null) {
                    for (Assignment a : ta) {
                        if (!existsInArrayList(a, currentlyViewedAssignments)) {
                            processWorkflowEvent(new WorkflowEvent(
                                                                   WorkflowEvent.Type.CREATEASSIGNMENT,
                                                                   a));
                        } else if (a.getLastUpdated().getTime() > lastTime) {
                            processWorkflowEvent(new WorkflowEvent(
                                                                   WorkflowEvent.Type.STOREASSIGNMENT,
                                                                   a));

                        }
                    }

                    for (Assignment a : currentlyViewedAssignments) {
                        if (!existsInArrayList(a, ta)) {
                            processWorkflowEvent(new WorkflowEvent(
                                                                   WorkflowEvent.Type.DELETEASSIGNMENT,
                                                                   a));

                        }
                    }
                }

                currentlyViewedAssignments = ta;
                lastTime += pollInterval;

            }

        }
    }

    /**
     * Returns the next available package id.
     * 
     * @return the next available package id.
     */
    public int getNextPackageId() {
        int id = store.getNextPackageId();
        return id;
    }
}
