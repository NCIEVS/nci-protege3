/**
 * 
 */
package gov.nih.nci.protegex.workflow;

/*
 * 
 * 
 * @author Flora B. Workflow
 * 
 */
public class WorkflowUser implements Comparable<WorkflowUser> {

	public enum Type {
		/**
		 * An admin user can do pretty much anything, create users, create
		 * assignments and assign them to other users.
		 */
		ADMIN("1"),

		/**
		 * A modeler can basically only edit and modify assignments assigned to
		 * her. She must frst accept the assignment. She can mark the assignment
		 * complete at some point, which indicates to the manager that the work
		 * is ready for review
		 */
		MODELER("2"),

		/**
		 * A user with BOTH roles can do it all
		 */
		BOTH("3");

		private String name = "no type";

		public String getName() {
			return name;
		}

		private Type(String s) {
			name = s;
		}

		static Type getType(int i) {
			switch (i) {
			case 1:
				return ADMIN;
			case 2:
				return MODELER;
			case 3:
				return BOTH;
			default:
				return MODELER;
			}
		}
		
		
		
	}

	/**
	 * Every user has a role, the default being modeler
	 */
	private Type userRole = Type.MODELER;

	/**
	 * 
	 * @return the role of the user
	 */
	public Type getUserRole() {
		return userRole;
	}

	/**
	 * Every user has a unique name
	 */
	private String name = "NoUser";

	/**
	 * 
	 * @return the user's unique name
	 */
	public String getUserName() {
		return name;
	}

	/**
	 * package private, new users are instantiated by the Workflow object
	 * 
	 * @see Workflow#createUser
	 * 
	 * @param s
	 *            the user's name
	 * @param t
	 *            the user's role
	 */
	WorkflowUser(String s, Type t) {
		name = s;
		userRole = t;
	}

	/**
	 * Implements Comparable, for sorting by name.
	 * @param other The other WorkflowUser.
	 * @return the value 0 if the argument string is equal to this string; 
	 *   a value less than 0 if this string is lexicographically less
	 *   than the string argument; and a value greater than 0 if this
	 *   string is lexicographically greater than the string argument.
	 */
    public int compareTo(WorkflowUser other) {
        return name.compareTo(((WorkflowUser) other).name);
    }
}
