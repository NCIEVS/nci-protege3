package gov.nih.nci.protegex.util;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.workflow.Assignment;
import gov.nih.nci.protegex.workflow.SuggestionDialog;
import gov.nih.nci.protegex.workflow.Workflow;
import gov.nih.nci.protegex.workflow.WorkflowException;
import gov.nih.nci.protegex.workflow.WorkflowUser;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.table.TableModel;

/**
 * Contains utility methods specific to the Workflow.
 * 
 * @author David Yee
 */
public class WorkflowUtil {
	// Constants:
	private static final String ADMIN_MARKER = " *";

	// Member variables:
	private static DateFormat _formatter = UIUtil.getDateFormatter();

	/**
	 * Returns true if the specfied role has administrative properties.
	 * 
	 * @param role
	 *            The role.
	 * @return true if the specfied role has administrative properties.
	 */
	public static boolean isAdmin(WorkflowUser.Type role) {
		return role == WorkflowUser.Type.ADMIN
				|| role == WorkflowUser.Type.BOTH;
	}
    
	/**
	 * Returns true if the specified user's name exists within the Workflow.
	 * 
	 * @param workflow
	 *            The Workflow.
	 * @param name
	 *            The user's name.
	 * @return true if the specified user's name exists within the Workflow.
	 */
	public static boolean userExists(Workflow workflow, String name) {
		try {
			return workflow.getUser(name) != null;
		} catch (WorkflowException e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
			return false;
		}
	}

	/**
	 * Creates a Workflow user with the specified role.
	 * 
	 * @param workflow
	 *            The Workflow.
	 * @param name
	 *            The user's name.
	 * @param role
	 *            The role.
	 * @return true if successful.
	 */
	public static WorkflowUser createUser(Workflow workflow, String name,
			WorkflowUser.Type role) {
		try {
			WorkflowUser user = workflow.getUser(name);
			if (user != null)
				return user;
			return workflow.createUser(name, role);
		} catch (WorkflowException e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
		return null;
	}

	/**
	 * Removes the administrative marker from the specified text.
	 * 
	 * @param text
	 *            The text string.
	 * @return the modified text.
	 */
	public static String removeMarker(String text) {
		int i = text.indexOf(ADMIN_MARKER);
		if (i >= 0)
			return text.substring(0, i);
		return text;
	}

	/**
	 * Returns the user's display name. If displayMarker is true and this user
	 * is an administrator, then his/her name will be displayed with a marker.
	 * 
	 * @param user
	 *            The Workflow user.
	 * @param displayMarker
	 *            If true, displays an administrative marker.
	 * @return The user's display name.
	 */
	public static String getDisplayName(WorkflowUser user, boolean displayMarker) {
		String name = user.getUserName();
		if (!displayMarker)
			return name;
		if (WorkflowUtil.isAdmin(user.getUserRole()))
			name += ADMIN_MARKER;
		return name;
	}

	/**
	 * Returns the list of Workflow users as a string. If displayMarker is true
	 * and any of the users is an administrator, then the corresponding user's
	 * name will be displayed with a marker.
	 * 
	 * @param users
	 *            The list of Workflow users.
	 * @param displayMarker
	 *            If true, displays an administrative marker.
	 * @return the list of Workflow users as a string.
	 */
	public static String[] getUsers(ArrayList<WorkflowUser> users,
			boolean displayMarker) {
		try {
			if (users == null)
				return new String[0];
			int max = users.size();
			String[] names = new String[max];
			for (int i = 0; i < max; ++i)
				names[i] = getDisplayName((WorkflowUser) users.get(i),
						displayMarker);
			return names;
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
			return new String[0];
		}
	}

	/**
	 * Returns true if MetaProject (Protege) user exists.
	 * 
	 * @param kb
	 *            The KnowledgeBase.
	 * @param userName
	 *            The user's name.
	 * @return true if MetaProject (Protege) user exists.
	 */
	public static boolean userMPExists(KnowledgeBase kb, String userName) {
		return new MetaProjectHelper(kb).userExists(userName);
	}

	/**
	 * Creates a MetaProject (Protege) user.
	 * 
	 * @param kb
	 *            The KnowledgeBase.
	 * @param userName
	 *            The user's name.
	 * @param password
	 *            The user's password.
	 * @param role
	 *            The user's role.
	 * @return the newly created MetaProject (Protege) user.
	 */
	public static MetaProjectHelper.CreateUserStatus createMPUser(
			KnowledgeBase kb, String userName, String password,
			WorkflowUser.Type role) {
		try {
			return new MetaProjectHelper(kb).createUser(userName, password,
					role);
		} catch (Exception e) {
			e.printStackTrace();
			return MetaProjectHelper.CreateUserStatus.FAILURE;
		}
	}

	/**
	 * Modifies the specified MetaProject (Protege) user.
	 * 
	 * @param kb
	 *            The KnowledgeBase.
	 * @param userName
	 *            The user's name.
	 * @param role
	 *            The user's role.
	 * @return ModifyUserStatus.
	 */
	public static MetaProjectHelper.ModifyUserStatus modifyMPUser(
			KnowledgeBase kb, String userName, WorkflowUser.Type role) {
		try {
			return new MetaProjectHelper(kb).modifyUser(userName, role);
		} catch (Exception e) {
			e.printStackTrace();
			return MetaProjectHelper.ModifyUserStatus.FAILURE;
		}
	}

	/**
	 * Creates a generic java Action for creating Workflow assignments.
	 * 
	 * @param parent
	 *            The parent window associated with this action.
	 * @param actionName
	 *            The name associated with the action.
	 * @param actualWork
	 *            The assignment's actual work value.
	 * @param taskDescription
	 *            The assignment's task description value.
	 * @return the generic java Action.
	 */
	
	
	public static Action createAssignmentAction(String actionName, String code, String taskDescription) {
		Action action = new AbstractAction(actionName) {
			private static final long serialVersionUID = 123456006L;

			public void actionPerformed(ActionEvent e) {
				ArrayList<String> codes = new ArrayList<String>();
                ArrayList<String> names = new ArrayList<String>();
				codes.add((String) getValue("Code"));
                names.add((String) getValue("Task Description"));
                new SuggestionDialog((String) getValue("Task Description"), codes, names);
                
			}
		};
		//action.putValue("Parent", parent);
		action.putValue("Code", code);
		action.putValue("Task Description", taskDescription);
		action.setEnabled(WorkflowHelper.getInstance().isWorkflowActive());
		return action;
	}

	public static boolean canEdit(Workflow workflow, Assignment assignment) {

		return workflow.canDo(assignment, Workflow.RequestedAction.EDIT);

	}

	public static boolean canModify(Workflow workflow, Assignment assignment) {

		return workflow.canDo(assignment, Workflow.RequestedAction.MODIFY);

	}

	// /**
	// * Returns the next valid list of statuses for the current status value.
	// * @param status The current status value.
	// * @return the next valid list of statuses for the current status value.
	// */
	// public static Assignment.Status[] getStatus(Assignment.Status status) {
	// if (status == Assignment.Status.CREATED
	// || status == Assignment.Status.SUGGESTED)
	// return new Assignment.Status[] { status,
	// Assignment.Status.ASSIGNED, Assignment.Status.ACCEPTED };
	// if (status == Assignment.Status.ASSIGNED)
	// return new Assignment.Status[] { status,
	// Assignment.Status.ACCEPTED, Assignment.Status.COMPLETED };
	// if (status == Assignment.Status.ACCEPTED)
	// return new Assignment.Status[] { status, Assignment.Status.UPDATED,
	// Assignment.Status.COMPLETED };
	// if (status == Assignment.Status.UPDATED)
	// return new Assignment.Status[] { status,
	// Assignment.Status.COMPLETED };
	// if (status == Assignment.Status.COMPLETED)
	// return new Assignment.Status[] { status };
	// return Assignment.Status.values();
	// }

	/**
	 * Updates modeler's notes by prepending the new notes.
	 * 
	 * @param assignment
	 *            The assignment.
	 * @param newNotes
	 *            The new notes to be prepended.
	 * @param action
	 *            The value displayed next to the current user's name. Examples:
	 *            "Edited" or "Rejected".
	 * @return true if updated.
	 */
	public static boolean updateModelersNotes(Assignment assignment,
			String newNotes, String action) {
        String formattedNote = formatModelerNote(newNotes, action);
        if (formattedNote.length() <= 0)
            return false;
        
        StringBuffer buffer = new StringBuffer(formattedNote);
		// Set the following value to false to clear previous notes.
		// Use mostly for testing.
		boolean appendPrevious = true;
		if (appendPrevious) {
			String prevValue = assignment.getModelerNote().trim();
			if (prevValue.length() > 0) {
				buffer.append("\n");
				// buffer.append("----------------------------------------");
				// buffer.append("----------------------------------------\n");
				buffer.append(prevValue);
			}
		}
		assignment.setModelerNote(buffer.toString());
		return true;
	}
    
    /**
     * Formats the notes.
     * @param note The modeler note.
     * @param action The value displayed next to the current user's name.
     *   Examples: "Edited" or "Rejected".
     * @return the formatted note.
     */
    public static String formatModelerNote(String note, String action) {
        String formattedNote = note.trim();
        if (formattedNote.length() <= 0)
            return "";
        String user = WorkflowHelper.getInstance().getCurrentUser().getUserName();
        StringBuffer buffer = new StringBuffer();
        buffer.append("* Date: " + _formatter.format(new Date()) + "\n");
        buffer.append("* " + action + " by: " + user + "\n");

        // Set the following value to true to indent each line. However,
        // does not work too well when using word wrap is turned on.
        boolean indentNewValue = false;
        if (indentNewValue) {
            StringTokenizer tokenizer = new StringTokenizer(formattedNote, "\n");
            while (tokenizer.hasMoreTokens())
                buffer.append("  " + tokenizer.nextToken() + "\n");
        } else {
            buffer.append(formattedNote + "\n");
        }
        return buffer.toString();
    }
    
    /**
     * Parses the modeler notes as a string to get note values.
     * @param value The modeler notes as a string.
     * @return An ArrayList that contains two separate ArrayList.
     *   The first ArrayList is that actual notes from the modeler.
     *   The second ArrayList is the date information.
     *   The third ArrayList is the edit by information.
     */
    public static ArrayList<ArrayList<String>> parseModelerNotes(String value) {
        ArrayList<String> notes = new ArrayList<String>();
        ArrayList<String> dates = new ArrayList<String>();
        ArrayList<String> editBys = new ArrayList<String>();
        StringBuffer note = new StringBuffer();
        StringBuffer date = new StringBuffer();
        StringBuffer editBy = new StringBuffer();

        ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        list.add(notes);
        list.add(dates);
        list.add(editBys);
        
        value = value.trim();
        if (value.length() <= 0)
            return list;
        
        StringTokenizer tokenizer = new StringTokenizer(value, "\n", true);
        while (tokenizer.hasMoreTokens()) {
            String text = tokenizer.nextToken();
            if (text.startsWith("* Date: ")) {
                if (addModelerNote(notes, note.toString(), dates, date.toString(),
                    editBys, editBy.toString())) {
                    note.delete(0, note.length());
                    date.delete(0, date.length());
                    editBy.delete(0, editBy.length());
                }
                date.append(text + "\n");
            } else if (text.startsWith("* ") && text.contains(" by: "))
                editBy.append(text + "\n");
            else note.append(text);
        }
        addModelerNote(notes, note.toString(), dates, date.toString(),
            editBys, editBy.toString());
        return list;
    }

    /**
     * Returns the latest note.
     * @param notes The modeler notes as a string.
     * @return the latest note.
     */
    public static String getLatestNote(String notes) {
        if (notes == null)
            return "";
        notes = notes.trim();
        if (notes.length() <= 0)
            return "";
        
        StringTokenizer tokenizer = new StringTokenizer(notes, "\n", true);
        StringBuffer buffer = new StringBuffer();
        int ctr = 0;
        while (tokenizer.hasMoreTokens()) {
            String text = tokenizer.nextToken();
            if (text.startsWith("* Date: ")) {
            	if (ctr++ <= 0)
            		continue;
            	else break;
            } else if (text.startsWith("* ") && text.contains(" by: ")) {
                continue;
            } else buffer.append(text);
        }
        return buffer.toString();
    }

    /**
     * Adds a note to the notes list and adds an extra info to the extras list. 
     * @param notes The list of notes.
     * @param note The note.
     * @param dates The list of extra information.
     * @param date The list of extra information.
     * @return true if both list has been updated.
     */
    private static boolean addModelerNote(ArrayList<String> notes, String note,
        ArrayList<String> dates, String date, ArrayList<String> editBys,
        String editBy) {
        note = note.trim();
        date = date.trim();
        editBy = editBy.trim();
        if (note.length() <= 0 && date.length() <= 0 && editBy.length() <= 0)
            return false;

        //Note: The note or extra information could be blank.
        notes.add(note);
        dates.add(date);
        editBys.add(editBy);
        return true;
    }

    /**
	 * Removes _v[NUMBER] postfix from the text.
	 * 
	 * @param text
	 *            The text string.
	 * @return the text without postfix.
	 */
	public static String stripsVersionNumberPostfix(String text) {
		int i = text.lastIndexOf("_v");
		if (i >= 0)
			text = text.substring(0, i);
		return text;
	}

	/**
     * Returns a list of assignments for a specific package.
     * @param model The TableModel that contains assignments in the first
     *   column.
     * @param packageId The specific package identifier.
     * @return a list of assignments for a specific package.
     */
    public static ArrayList<Assignment> getAssignmentsFromAPackage(
        TableModel model, int packageId) {
        ArrayList<Assignment> list = new ArrayList<Assignment>();
        int n = model.getRowCount();
        for (int i=0; i<n; ++i) {
            Assignment a = (Assignment) model.getValueAt(i, 0);
            if (a.getPackageId() == packageId)
                list.add(a);
        }
        return list;
    }
    
    /**
     * The enumerated list of actual work field.
     * @author David Yee
     */
    public enum ACTUAL_WORK_FIELD {
        CONTENT, PACKAGE_URL, PROPOSAL_URL, NEW_CONCEPT_CODE
    }
    
    /**
     * Returns the assignment object's specific actual work field value.
     * @param assignment The assignemnt.
     * @param field The specific field.
     * @return the assignment object's specific actual work field value.
     */
    public static String getActualWorkValue(Assignment assignment, 
        ACTUAL_WORK_FIELD field) {
        return StringUtil.getToken(assignment.getTheActualWork(),
            Workflow.ACTUAL_WORK_DELIMITER,
            field.ordinal());
    }
    
    /**
     * Updates a specific subfield within the assignement's actual work field.
     * @param assignment The Assignment.
     * @param field The subfield.
     * @param value The new value.
     */
    public static void updateActualWorkValue(Assignment assignment,
        ACTUAL_WORK_FIELD field, String value) {
        Assignment a = assignment;
        String content = getActualWorkValue(a, ACTUAL_WORK_FIELD.CONTENT);
        String pkgUrl = getActualWorkValue(a, ACTUAL_WORK_FIELD.PACKAGE_URL);
        String propUrl = getActualWorkValue(a, ACTUAL_WORK_FIELD.PROPOSAL_URL);
        String code = getActualWorkValue(a, ACTUAL_WORK_FIELD.NEW_CONCEPT_CODE);
        
        if (field == ACTUAL_WORK_FIELD.CONTENT)
            content = value;
        else if (field == ACTUAL_WORK_FIELD.PACKAGE_URL)
            pkgUrl = value;
        else if (field == ACTUAL_WORK_FIELD.PROPOSAL_URL)
            propUrl = value;
        else if (field == ACTUAL_WORK_FIELD.NEW_CONCEPT_CODE)
            code = value;
        
        StringBuffer buffer = new StringBuffer();
        buffer.append(content + Workflow.ACTUAL_WORK_DELIMITER);
        buffer.append(pkgUrl + Workflow.ACTUAL_WORK_DELIMITER);
        buffer.append(propUrl + Workflow.ACTUAL_WORK_DELIMITER);
        buffer.append(code);
        
        a.setTheActualWork(buffer.toString());
        WorkflowHelper.getInstance().storeAssignment(assignment);
    }
    
    /**
     * Returns a unique concept name by post pending an incremental number.
     * @param name The initial concept name.
     * @return a unique concept name.
     */
    public static String getUniqueConceptName(OWLWrapper wrapper, String name) {
        name = name.replaceAll(" ", "_");
        Cls cls = wrapper.getCls(name);
        if (cls == null)
            return name;
        
        int i=2;
        while (true) {
            String nameVersion = name + "_" + i;
            cls = wrapper.getCls(nameVersion);
            if (cls == null)
                return nameVersion;
            ++i;
        }
    }
    
    /**
     * Returns the concept code from proposal URL.
     * @param url The proposal URL.
     * @return the concept code from proposal URL.
     */
    public static String getCodeFromUrl(String url) {
        String code = StringUtil.getLastToken(url, "(");
        code = code.substring(0, code.length()-1);
        return code;
    }
    
    /**
     * Returns the concept name from proposal URL.
     * @param url The proposal URL.
     * @return the concept name from proposal URL.
     */
    public static String getNameFromUrl(String url) {
        String name = StringUtil.getLastToken(url, "/");
        int i = name.indexOf('_');
        if (i >= 0) name = name.substring(i+1);
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("_v[0-9]*");
        Matcher m = p.matcher(name);
        if (m.find()) m.appendReplacement(sb, "");
        name = sb.toString();
        return name;
    }
}
