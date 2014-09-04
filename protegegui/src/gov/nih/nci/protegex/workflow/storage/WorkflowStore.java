/**
 * 
 */
package gov.nih.nci.protegex.workflow.storage;

/**
 * A workflow store abstracts out the details of underlying storge
 * of workflow assignments, users, etc..
 * 
 * The store may be local or remote, file based or database and so on.
 * One goal is to adopt a REST like architecture for these APIs, anticipating
 * serving this data up readily over the web. This night make reporting and
 * auditing easier.
 * 
 * @author Bob Dionne
 *
 */
public interface WorkflowStore {
	
	/**
	 * find a given user, identified by name.
	 * 
	 * @param userName
	 * @return true if this user exists in the store, false otherwise
	 */
	boolean userExistsP(String userName);
	
	/**
	 * for a given user, identified by name, determine if the user is:
	 * 1. admin
	 * 2. modeler
	 * 3. both
	 * 
	 * @param userName
	 * @return an integer denoting the user's role
	 */
	int userRole(String userName);
	
	/**
	 * Every assignment has a unique id. These are maintained by the store
	 * and retireved as needed for new assignments
	 * 
	 * @return the next available integer id
	 */
	int getNextAssignmentId();
	
	/**
	 * Package id is used to group several assignments into a package.
	 * 
	 * @return the next available package id
	 */
	int getNextPackageId();
	
	/**
	 * Store a new assignment, with given integer id, for the given user
	 * 
	 * @param userName the owner of the assignment
	 * @param assignment the actual assignment, typically an XML string
	 * @param assid the unique integer id of the assignment
	 */
	void storeAssignment(String userName, String assignment, int assid);
	
	void archiveAssignment(String userName, String assignment, int assid);
	
	void deleteAssignment(String userName, int assid);
	
	/**
	 * create a new user, with given role. if successful it returns the int
	 * corresponding to the role
	 * 
	 * @see WorkflowStore#userRole(String)
	 * 
	 * @param userName a unique name
	 * @param role 
	 * @return a int that specifies the user's role, -1 otherwise
	 */
	int createUser(String userName, String role);
	
	String[] getAllAssignments(String userName);
	
	String[] getAllUsers();
	
	

}
