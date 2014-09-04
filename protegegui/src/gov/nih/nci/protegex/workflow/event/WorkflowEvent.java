/**
 * 
 */
package gov.nih.nci.protegex.workflow.event;


/**
 * Simple event class. Workflow will throw events to registered listeners
 * when events occur, specifically create a new assignment, new user, store and
 * assignment and delete an assignment
 * 
 * @see gov.nih.nci.protegex.workflow.Workflow
 * @see WorkflowListener
 * 
 * @author Bob Dionne
 *
 */
public class WorkflowEvent {
	
	/**
	 * The type of event
	 * 
	 * @author Bob Dionne
	 *
	 */
	public enum Type {
		CREATEUSER("createuser"), 
		CREATEASSIGNMENT("createassignment"), 
		STOREASSIGNMENT("storeassignment"),
		DELETEASSIGNMENT("deleteassignment");
		
		private String name = "no type";

		public String getName() {
			return name;
		}

		private Type(String s) {
			name = s;
		}

		/**
		 * convert a string to a an event Typ
		 * 
		 * @param s
		 * @return The corresponding Type constant or null if it doesn't match
		 */
		public static Type getType(String s) {

			for (Type typ : Type.values()) {
				if (typ.name.equalsIgnoreCase(s)) {
					return typ;
				}
			}
			return null;
		}	
	}
	
	private Type type = null;
	
    private Object object = null;
	
	/**
	 * @return the event Type of this object
	 */
	public Type getType() { return type; }
	
    /**
     * @return the underlying Object of this event
     */
    public Object getObject() { return object; }

    /**
     * construct and event object with the given type and assignment id
     * 
     * @param t
     * @param o
     */
    public WorkflowEvent(Type t, Object o) {
        type = t;
        object = o;
    }
    
}
