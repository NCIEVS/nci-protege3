/**
 * 
 */
package gov.nih.nci.protegex.workflow;

import gov.nih.nci.protegex.workflow.util.XML;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An assignment is a unit of work given to a modeler by a manager. The actual
 * work itself might be many things so it's stored in the assignment as a plain
 * string. A description field can be used to provide a comment about the
 * assignment, and the modeler can also edit a note field associated with the
 * assignment. The date it is created is noted and a last updated field tracks
 * the last time the assignment was changed. As part of a worklfow component,
 * the assignment moves through a sequence of states : CREATED --> ASSIGNED -->
 * ACCEPTED --> UPDATED --> COMPLETED
 * 
 * When it is completed the last update timestamp reflects the completion date.
 * It is an error for the Assignment to be in any other state.
 * 
 * @author Flora B. Workflow
 * 
 */
public class Assignment implements Comparable {

	/**
	 * string used to distinguish what type of packet the is inside the actual
	 * work field
	 */
	private final String httpPrefix = "http";

	/**
	 * A status enum is used to track the current status of an assignment as it
	 * moves through the stages of the Workflow process. The order of the states
	 * is
	 * <ul>
	 * <li>CREATED</li>
	 * <li>ASSIGNED</li>
	 * <li>ACCEPTED</li>
	 * <li>UPDATED</li>
	 * <li>COMPLETED</li>
	 * </ul>
	 * 
	 * The state <b>SUGGESTED</b> is used for modelers to suggest work that
	 * might possibly be made into an assignment. However only a Workflow
	 * manager (admin) can create an assignment or change the state to assigned.
	 * 
	 * @author E. Numb
	 * 
	 */
	public enum Status {
		CREATED("created"), ASSIGNED("assigned"), ACCEPTED("accepted"), UPDATED(
				"updated"), COMPLETED("completed"), SUGGESTED("suggested"), REJECTED("rejected");

		private String name = "no status";

		public String getName() {
			return name;
		}

		private Status(String s) {
			name = s;
		}

		/**
		 * converts a string t a Status object
		 * 
		 * @param s
		 * @return A Status object or null if the string does not match
		 */
		public static Status getStatus(String s) {

			for (Status status : Status.values()) {
				if (status.name.equalsIgnoreCase(s)) {
					return status;
				}
			}
			return null;

		}
	}

	/**
	 * a unique identifier, hopefully 32 bits gives us enough assignments for
	 * the near future This is entirely an internal number. It can be seeded as
	 * part of the Protege config
	 * 
	 */
	private int identifier = -1;

	/**
	 * 
	 * @return the unique integer identifier of this Assignment object
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * This method is package private as all Assignments newly created are
	 * created by the Workflow object, each of which is guaranteed to have a
	 * unique integer indentifier.
	 * 
	 * @param i
	 *            a unique integer that will be permanently assigned to this
	 *            Assignment
	 * 
	 */
	void setIdentifier(int i) {
		identifier = i;
	}

	/**
	 * date assignment was created
	 * 
	 */
	private Date dateCreated = null;

	/**
	 * The date the Assignment is created will be the timestamp when the create
	 * method is called on the Workflow object. It cannot be modified thereafter
	 * 
	 * @return The date the Assignment was created
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * This method is pacage private as only the create method of the Workflow
	 * will ever set this, and only set it once at creation time
	 * 
	 * @param d
	 */
	void setDateCreated(Date d) {
		dateCreated = d;
		lastUpdated = d;
	}

	/**
	 * A simple DateFormat object with default format set. If we decide on an
	 * alternative later this will simply be adding a String to the constructor
	 */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

	/**
	 * An assignment is created by a manager
	 */
	private String manager = null;

	/**
	 * Retrieve the manager who created this assignment. THis will be the
	 * manager logged into Protege when the create method on the Workflow object
	 * is called
	 * 
	 * @return The manager who created this asignment
	 * 
	 */
	public String getManager() {
		return manager;
	}

	/**
	 * This method is package private as only the Workflow object will call it
	 * and only once when the assignment is created
	 * 
	 * @param s
	 *            The name of the manager who created this assignment
	 */
	void setManager(String s) {
		manager = s;
	}

	/**
	 * An assignment is assigned to a modeler
	 */
	private String currentModeler = null;

	/**
	 * If the currentModeler is set to a new value, it is first placed in the
	 * previous modeler field. This enables the workflow to track ownership when
	 * an Assinment is reassigned. An assignment will only exist in one place in
	 * the data store.
	 * 
	 * However setModeler could be called multiple times in the same session so
	 * the previousModeler field will only get set once, determined if it
	 * currently null, which is the init field. Subsequent calls to setModeler
	 * will have no impact. The one boundary case is when the currentMOdeler is
	 * initially set to the manager and then set to another modeler before
	 * saving. In this case the store will look to delete an assignment that's
	 * not there, resulting in a nop.
	 */
	private String previousModeler = null;
	
	
	
	void reset() {previousModeler = null;}

	/**
	 * 
	 * @return The name of the modeler who owns this assignment
	 */
	public String getModeler() {
		return currentModeler;
	}

	/**
	 * This method is package private and only used by Workflow to delete
	 * assignments when they are begin reassigned to new owners
	 * 
	 * @return The name of the previous modeler
	 */
	String getPreviousModeler() {
		return previousModeler;
	}

	/**
	 * Only an Admin user, .ie. a Manager can set the modeler or owner of the
	 * assignment. If the modeler has been set, the assignment previously
	 * stored, and the modeler is set again, this results in a delete and an add
	 * from the dta store. An Assignment will only exist once in the store and
	 * will have a unique id that is used to name it in the store. So resetting
	 * the modeler has the effect of reassigning the assignment to someone else.
	 * 
	 * @param s
	 */
	public void setModeler(String s) {

		if (currentModeler != null) {
			if (s.equalsIgnoreCase(currentModeler)) {
				return;
			}
		}

		if (previousModeler != null) {

		} else {
			previousModeler = currentModeler;
		}

		currentModeler = s;

	}

	/**
	 * An assignment will have different status fields depending on the current
	 * state. It is first created, assigned, accepted, and finally completed.
	 * tere is no pulbic set of the Status field, it is set by explicit methods
	 * that maintain the workflow state
	 * 
	 */
	private Status currentStatus = Status.CREATED;

	public Status getCurrentStatus() {
		return currentStatus;
	}

	public void setStatus(Status s) {

		currentStatus = s;		

	}

	/**
	 * The last time the assignment was updated This is set implicitly by the
	 * actions that update the assignment
	 */
	private Date lastUpdated = null;

	/**
	 * Lat updated is the date/time that a modeler did some work on the
	 * assignment. THis happens when they've actually made some edits to the
	 * terms in the NCI Edit Tab
	 * 
	 * @return The Date this assignment was last worked on
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	void upDateLastUpdate() {
		setLastUpdated(new Date());
		
	}

	/**
	 * Last Updated is set whenever the assignment is worked on
	 * 
	 * @param d
	 */
	void setLastUpdated(Date d) {
		lastUpdated = d;
	}

	/**
	 * The actual work can be anything in theory as it's typed as a String. For
	 * the current design it is one of three things:
	 * <ul>
	 * <li>An RDF document from the Wiki, representing a structured term</li>
	 * <li>A url representing an unstructured document, .ie. a Wiki page</li>
	 * <li>A string representing the code of an existing Protege term</li>
	 * </ul>
	 * 
	 */
	private String theActualWork = "";

	/**
	 * return the actual work as a String object. This could be RDF, a Class code, or an http 
	 * reference.
	 * 
	 * @return The actual work as a string
	 */
	public String getTheActualWork() {
		return theActualWork;
	}

	/**
	 * Called when the asignment is first created. From example when imports
	 * from the WIKI are made, for each RDF packect an assignment is created
	 * with the RDF packet as the work.
	 * 
	 * @param w
	 *            A string representing the actual work to be performed
	 */
	public void setTheActualWork(String w) {

		theActualWork = w;

		// check if structured or not
		if (w.startsWith(httpPrefix)) {
			isStructured = false;
		} else {
			isStructured = true;
		}
	}

	/**
	 * The actual work will be of three types, either a structured proposal or a
	 * url to an unstructured proposal, .ie. a Wiki page, or a code for an
	 * existing term in Protege. A code for an existing term will also be
	 * considered structured. Structured types can be resolved into a term that
	 * can be edited
	 */
	private boolean isStructured = false;

	/**
	 * 
	 * @return true is the actual work is a structured proposal or code for a
	 *         term in Protege
	 */
	public boolean isStructured() {
		return isStructured;
	}

	void setStructured(boolean b) {
		isStructured = b;
	}

	/**
	 * The source of the work, typically this will be a URL and can be used to
	 * retrieve the work again if need be.
	 */
	private String workSource = "";

	public String getWorkSource() {
		return workSource;
	}

	public void setWorkSource(String s) {
		workSource = s;
	}

	/**
	 * A short description of the editing task
	 */
	private String taskDescription = "";

	/**
	 * A bief decsription of the work assignment
	 * 
	 * @return A string
	 */
	public String getTaskDescription() {
		return taskDescription;
	}

	/**
	 * Modeifies the work assignment
	 * 
	 * @param s
	 */
	public void setTaskDescription(String s) {
		taskDescription = s;
	}

	/**
	 * modeler note. A brief note for modelers, things like "I modeled this just
	 * like the other ..."
	 */
	private String modelerNote = "";

	/**
	 * 
	 * @return A Modeler's note as a string
	 */
	public String getModelerNote() {
		return modelerNote.replaceAll("FOOBARBAZ", "\n");
	}

	public void setModelerNote(String s) {
		
		modelerNote = s;
	}
	
	/**
	 * The package identifier that corresponds to the assignment.  This 
	 * identifier allows multiple assigments to be group into a package.
	 */
	private int packageId = -1;
	
	/**
	 * 
	 * @return The assignment's package identifier.
	 */
	public int getPackageId() {
	    return packageId;
	}
	
	/**
	 * Sets the assignment's package identifier.
	 * @param id The package identifier.
	 */
	public void setPackageId(int id) {
	    packageId = id;
	}

	/**
	 * Serialize the Assigment object to XML for sending over the wire or to a
	 * file
	 * 
	 * @return a string of XML
	 */
	public String toXML() {

		StringBuffer sb = new StringBuffer();

		XML.asStartTag(sb, "Assignment");
		XML.asStartTag(sb, "identifier");
		sb.append(getIdentifier());
		XML.asEndTag(sb, "identifier");
		XML.asStartTag(sb, "dateCreated");
		sb.append(dateFormatter.format(getDateCreated()));
		XML.asEndTag(sb, "dateCreated");
		XML.asStartTag(sb, "managerId");
		sb.append(getManager());
		XML.asEndTag(sb, "managerId");
		XML.asStartTag(sb, "modelerId");
		sb.append(getModeler());
		XML.asEndTag(sb, "modelerId");
		XML.asStartTag(sb, "currentStatus");
		sb.append(getCurrentStatus().getName());
		XML.asEndTag(sb, "currentStatus");
		XML.asStartTag(sb, "lastUpdated");
		sb.append(dateFormatter.format(getLastUpdated()));
		XML.asEndTag(sb, "lastUpdated");
		XML.asStartTag(sb, "workSource");
		sb.append(getWorkSource());
		XML.asEndTag(sb, "workSource");
		XML.asStartTag(sb, "theActualWork");
		XML.asCdata(sb, getTheActualWork());
		XML.asEndTag(sb, "theActualWork");
		XML.asStartTag(sb, "taskDescription");
		sb.append(getTaskDescription());
		XML.asEndTag(sb, "taskDescription");
		XML.asStartTag(sb, "modelerNote");
		XML.asCdata(sb, mungeNewLines(getModelerNote()));
		XML.asEndTag(sb, "modelerNote");
        XML.asStartTag(sb, "packageId");
        sb.append(getPackageId());
        XML.asEndTag(sb, "packageId");
		XML.asEndTag(sb, "Assignment");

		//Workflow.logger.info(sb.toString());

		return sb.toString();

	}
	
	private String mungeNewLines(String s) {
		return s.replaceAll("\n", "FOOBARBAZ");
	}

	/**
	 * implements Comparable, for sorting by id,
	 * 
	 * TODO: needs to be generalized to handle any field
	 */
	public int compareTo(Object o) {
		int id1 = this.getIdentifier();
		int id2 = ((Assignment) o).getIdentifier();
		if (id1 == id2)
			return 0;
		return id1 > id2 ? 1 : -1;
	}

}
