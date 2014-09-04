package gov.nih.nci.protegex.workflow;

import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.StringUtil;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowUtil;
import gov.nih.nci.protegex.workflow.Assignment;
import gov.nih.nci.protegex.workflow.Workflow;
import gov.nih.nci.protegex.workflow.WorkflowException;
import gov.nih.nci.protegex.workflow.WorkflowUser;

import java.util.ArrayList;
import java.util.*;
import java.util.HashSet;
import java.util.logging.Level;

import java.awt.event.*;

import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * This tables model contains Workflow assignment tasks. Each assignment makes
 * reference to a proposed and/or existing concept.
 * 
 * @author David Yee
 */
public class AssignmentTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 123456007L;

	public enum Col {
		ASSIGNMENT("ASSIGNMENT", 0, 20), ID("ID", 1, 40), STATUS("Status", 2,
				100), DATE_CREATED("Date Created", 3, 200), LAST_UPDATED(
				"Last Updated", 4, 200), MANAGER("Manager", 5, 150), MODELER(
				"Modeler", 6, 150), ACTUAL_WORK("Actual Work", 7, 300), TASK_DESCRIPTION(
				"Task Description", 8, 250), MODELER_NOTES("Modeler Notes", 9,
				400), PACKAGE_ID("Package ID", 10, 20), PACKAGE_NAME(
				"Package Name", 11, 50), ;

		// Name member variable and methods
		private String _name = null;

		public String getName() {
			return _name;
		}

		// Width member variable and methods
		private int _width = -1;

		public int getWidth() {
			return _width;
		}

		public void setWidth(int i) {
			if (i > 0) {
				_width = i;
			}
		}

		// Column index member variable and methods
		private int _columnIndex = -1;

		public int getColumnIndex() {
			return _columnIndex;
		}

		// IsVisible member variable and methods
		private boolean _isVisible = true;

		public boolean isVisible() {
			return _isVisible;
		}

		public void setVisible(boolean visible) {
			_isVisible = visible;
		}

		// Column's initial position member variable and methods
		private int _initPosition = -1;

		public int getInitPosition() {
			return _initPosition;
		}

		public void setInitPosition(int position) {
			_initPosition = position;
		}

		/**
		 * Returns the column by the column name.
		 * 
		 * @param name
		 *            The column name.
		 * @return the column by the column name.
		 */
		public static Col find(String name) {
			for (Col value : values())
				if (value.getName().equalsIgnoreCase(name))
					return value;
			return null;
		}

		/**
		 * Constructs this class.
		 * 
		 * @param name
		 *            The column name.
		 * @param index
		 *            The column index.
		 * @param width
		 *            The column width.
		 */
		Col(String name, int index, int width) {
			_name = name;
			_columnIndex = index;
			_width = width;
		}

		static {
			ASSIGNMENT.setVisible(false);
			DATE_CREATED.setVisible(false);
			MANAGER.setVisible(false);
			ACTUAL_WORK.setVisible(false);
			PACKAGE_NAME.setVisible(false);
		}

		/**
		 * Returns the list of column names.
		 * 
		 * @return the list of column names.
		 */
		private static String[] getNames() {
			ArrayList<String> list = new ArrayList<String>();
			for (Col col : values())
				list.add(col.getName());
			return list.toArray(new String[list.size()]);
		}
	}

	// Member variables:
	private Workflow _workflow;

	private boolean _isAdmin = false;

	private String _loginUser;

	private String _displayModeler;

	/**
	 * Constructs this class.
	 * 
	 * @param workflow
	 *            The Workflow.
	 */
	public AssignmentTableModel(Workflow workflow) {
		super(new Object[0][], Col.getNames());
		_workflow = workflow;
		_isAdmin = WorkflowUtil.isAdmin(_workflow.getUser().getUserRole());
		_loginUser = _workflow.getUser().getUserName();

	}

	/**
	 * Returns true if the cell located at (row, column) is editable.
	 * 
	 * @param row
	 *            The row location.
	 * @param column
	 *            The column location.
	 * @return true if the cell located at (row, column) is editable.
	 */
	public boolean isCellEditable(int row, int column) {
		Assignment a = (Assignment) getValueAt(row, 0);
		// if (column == STATUS_COLUMN)
		// return isStatusFieldEditable(a, _isAdmin);
		if (!_isAdmin)
			return false;
		if (column == Col.MODELER._columnIndex)
			return isModelerFieldEditable(a, _isAdmin);

		return false;
	}

	/**
	 * Returns true if the assignment's status field is editable.
	 * 
	 * @param assignment
	 *            The assignment.
	 * @param isAdmin
	 *            Is admin flag.
	 * @return true if the assignment's status field is editable.
	 */
	public static boolean isStatusFieldEditable(Assignment assignment,
			boolean isAdmin) {
		Assignment.Status status = assignment.getCurrentStatus();
		return (status == Assignment.Status.SUGGESTED && isAdmin)
				|| status != Assignment.Status.COMPLETED;
	}

	/**
	 * Returns true if the assignment's modeler field is editable.
	 * 
	 * @param assignment
	 *            The assignment.
	 * @param isAdmin
	 *            Is admin flag.
	 * @return true if the assignment's status field is editable.
	 */
	public static boolean isModelerFieldEditable(Assignment assignment,
			boolean isAdmin) {
		Assignment.Status status = assignment.getCurrentStatus();
		return isAdmin
				&& (status == Assignment.Status.CREATED
						|| status == Assignment.Status.ASSIGNED
						|| status == Assignment.Status.COMPLETED
						|| status == Assignment.Status.REJECTED || status == Assignment.Status.SUGGESTED);
	}

	/**
	 * Converts the assignment into array of objects.
	 * 
	 * @param assignment
	 *            The assignment.
	 * @return The array.
	 */
	private Object[] newAssignmentArray(Assignment assignment) {
		return new Object[] {
				assignment,
				assignment.getIdentifier(),
				assignment.getCurrentStatus(),
				assignment.getDateCreated(),
				assignment.getLastUpdated(),
				assignment.getManager(),
				assignment.getModeler(),
				assignment.getTheActualWork(),
				assignment.getTaskDescription(),
				assignment.getModelerNote(),
				assignment.getPackageId(),
				WorkflowUtil.getActualWorkValue(assignment,
						WorkflowUtil.ACTUAL_WORK_FIELD.PACKAGE_URL), };
	}

	/**
	 * Sorts the array of assignment.
	 * 
	 * @param list
	 *            The array of assignment.
	 * @return The sorted array of assignment.
	 */
	private ArrayList<Assignment> sort(ArrayList<Assignment> list) {
		Assignment[] as = new Assignment[list.size()];
		list.toArray(as);
		Arrays.sort(as);
		return new ArrayList<Assignment>(Arrays.asList(as));
	}

	/**
	 * Converts an array list of assignments into double array of objects.
	 * 
	 * @param list
	 *            The assignments.
	 * @return The double array of objects.
	 */
	private Object[][] convertAssignments(ArrayList<Assignment> list) {
		try {
			list = sort(list);
			int max = list.size();
			Object as[][] = new Object[max][];
			for (int i = 0; i < max; ++i)
				as[i] = newAssignmentArray((Assignment) list.get(i));
			return as;
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
			return new Object[0][];
		}
	}

	/**
	 * Removes all the rows from the model.
	 */
	public void removeAll() {
		int n = getRowCount();
		for (int i = n - 1; i >= 0; --i)
			removeRow(i);
	}

	/**
	 * Returns true if the assignment can be added as a row in the model.
	 * 
	 * @param assignment
	 *            The assignment.
	 * @return true if the assignment can be added.
	 */
	private boolean canAddRow(Assignment assignment) {
		// If admin, add rows that he/she is managing.
		if (_isAdmin && _displayModeler.equals(_loginUser))
			return true;
		// return assignment.getManager().equals(_loginUser);

		// If modeler, add rows that belongs to the current modeler.
		return assignment.getModeler().equals(_displayModeler);
	}

	/**
	 * Adds the assignment in the model.
	 * 
	 * @param assignment
	 *            The assignment.
	 */
	public void addRow(Assignment assignment) {
		int row = getRowWithID(assignment.getIdentifier());
		if (row >= 0)
			return;

		if (canAddRow(assignment))
			addRow(newAssignmentArray(assignment));
	}

	/**
	 * Loads the workflow user's assignments to the model.
	 * 
	 * @param user
	 *            The WorkflowUser.
	 */
	public void load(WorkflowUser user) {
		try {
			_displayModeler = user.getUserName();
			removeAll();
			Object[][] rows;
			if (_isAdmin && _displayModeler.equals(_loginUser))
				rows = convertAssignments(_workflow.getAllAssignments());
			// rows = convertAssignments(WorkflowUtil.getManagerAssignments(
			// _workflow, _loginUser));
			else
				rows = convertAssignments(_workflow.getAllAssignments(user));
			for (int i = 0; i < rows.length; ++i)
				addRow(rows[i]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Displays a warning message informing the user he/she could not remove the
	 * following rows.
	 * 
	 * @param list
	 *            contain the row numbers.
	 */
	private void displayCannotRemoveMessage(ArrayList<Object> list) {
		if (list.size() <= 0)
			return;

		list = UIUtil.sort(list);
		String message = StringUtil
				.formatMessage(
						"Can not remove the following assignment(s):\n\n",
						list,
						"\n\nNote: Administrators can only delete assignments with"
								+ "\n          statuses of CREATED, SUGGESTED. or COMPLETED."
								+ "\n      Modelers can delete only SUGGESTED assignements.",
						"    ", 5);
		MsgDialog.warning(null, message);
	}

	/**
	 * Removes a list of rows from the model. In addition, removes it from the
	 * workflow.
	 * 
	 * @param workflow
	 *            The Workflow.
	 * @param rows
	 *            The list of row indexes.
	 */
	public void removeRows(Workflow workflow, int[] rows) {
		try {
			ArrayList<Object> cannotRemoveList = new ArrayList<Object>();
			for (int i = rows.length - 1; i >= 0; --i) {
				Assignment a = (Assignment) getValueAt(rows[i],
						Col.ASSIGNMENT._columnIndex);
				if (!workflow.canDo(a, Workflow.RequestedAction.DELETE)) {
					cannotRemoveList.add(new Integer(a.getIdentifier()));
					continue;
				}
				Log.getLogger().log(Level.INFO,
						"Deleting Assignment: " + a.getIdentifier());
				// removeRow(rows[i]);
				workflow.deleteAssignment(a);
			}
			displayCannotRemoveMessage(cannotRemoveList);
		} catch (WorkflowException e) {
			// Log.getLogger().log(Level.WARNING, "Exception caught", e);
			MsgDialog.warning(null, e.getMessage());
		}
	}

	/**
	 * Returns the row that contains the specified assignement ID.
	 * 
	 * @param id
	 *            The assignment ID.
	 * @return The row index.
	 */
	public int getRowWithID(int id) {
		int n = getRowCount();
		for (int i = 0; i < n; ++i) {
			int value = ((Integer) getValueAt(i, Col.ID._columnIndex))
					.intValue();
			if (value == id)
				return i;
		}
		return -1;
	}

	/**
	 * Removes the row that contains a specific assignment ID.
	 * 
	 * @param id
	 *            The assignment ID.
	 */
	public void removeRowWithID(int id) {
		int row = getRowWithID(id);
		if (row < 0)
			return;
		removeRow(row);
	}

	/**
	 * Updates the data associated with an assignment that is stored in a
	 * specific row.
	 * 
	 * @param assignment
	 *            The assignment.
	 */
	public void update(Assignment assignment) {
		int row = getRowWithID(assignment.getIdentifier());
		if (row < 0) {
			// This block is called when a new assignment is created when
			// the table is not displaying the current logged in user's
			// assignments. Example: Dave (admin) is creating an assignment
			// for Bob (modeler) while Bob's assignments are currently
			// displayed. This occurs because the initial created assignment
			// is assigned (by setting the modeler field) to the logged in
			// user (Dave). Upon calling Workflow.storeAssignment, it gets
			// reassigned (Bob).
			addRow(assignment);
			return;
		}

		if (!canAddRow(assignment)) {
			// This block is called when the current user reassigns the
			// current assignment to another modeler. The table only
			// shows a particular modeler's data. Once changed, it no
			// longer belongs to this table.
			removeRow(row);
			return;
		}

		Object[] objs = this.newAssignmentArray(assignment);
		for (int j = 0; j < this.getColumnCount(); j++)
			this.setValueAt(objs[j], row, j);
	}

	/**
	 * Returns a list of package names.
	 * 
	 * @param appendPkdId
	 *            if true, appends the pkgId to the pkgName.
	 * @return a list of package names.
	 */
	public String[] getPackageNames(boolean appendPkdId) {
		HashSet<String> hash = new HashSet<String>();
		for (int i = 0; i < getRowCount(); ++i) {
			int pkgId = (Integer) getValueAt(i, Col.PACKAGE_ID.getColumnIndex());
			if (pkgId < 0)
				continue;
			String pkgName = (String) getValueAt(i, Col.PACKAGE_NAME
					.getColumnIndex());
			// pkgName = StringUtil.getLastToken(pkgName, "/");
			pkgName = StringUtil.getLastToken(pkgName, ":");
			if (appendPkdId)
				hash.add(pkgName + " (pkgId=" + pkgId + ")");
			else
				hash.add(pkgName);
		}
		String[] list = hash.toArray(new String[hash.size()]);
		Arrays.sort(list);
		return list;
	}

	protected int sortCol = 0;

	protected boolean isSortAsc = true;

	class ColumnListener extends MouseAdapter {
		protected JTable table;

		public ColumnListener(JTable t) {
			table = t;
		}

		public void mouseClicked(MouseEvent e) {
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex)
					.getModelIndex();

			if (modelIndex < 0)
				return;
			if (sortCol == modelIndex)
				isSortAsc = !isSortAsc;
			else
				sortCol = modelIndex;

			/**
			 * int columnsCount = table.getModel().getColumnCount(); for (int i =
			 * 0; i < columnsCount; i++) { TableColumn column =
			 * colModel.getColumn(i);
			 * column.setHeaderValue(getColumnName(column.getModelIndex())); }
			 */
			table.getTableHeader().repaint();

			Collections.sort(AssignmentTableModel.super.getDataVector(),
					new MyComparator(isSortAsc, sortCol));
			table.tableChanged(new TableModelEvent(AssignmentTableModel.this));
			table.repaint();
		}
	}
}

class MyComparator implements Comparator {
	protected boolean isSortAsc;

	protected int col;

	public MyComparator(boolean sortAsc, int sortCol) {
		isSortAsc = sortAsc;
		col = sortCol;
	}

	private int compareObjs(Object o1, Object o2) {
		if (o1 instanceof Assignment.Status) {
			return ((Assignment.Status) o1).compareTo((Assignment.Status) o2);
		} else if (o1 instanceof String) {
			return ((String) o1).compareTo((String) o2);
		} else if (o1 instanceof Date) {
			return ((Date) o1).compareTo((Date) o2);
        } else if (o1 instanceof Integer) {
            return ((Integer) o1).compareTo((Integer) o2);
		} else {
			return 0;
		}
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof Vector) || !(o2 instanceof Vector))
			return 0;
		Vector s1 = (Vector) o1;
		Vector s2 = (Vector) o2;
		int result = 0;
		result = compareObjs(s1.get(col), s2.get(col));
		if (!isSortAsc)
			result = -result;
		return result;
	}

	public boolean equals(Object obj) {
		if (obj instanceof MyComparator) {
			MyComparator compObj = (MyComparator) obj;
			return ((compObj.isSortAsc == isSortAsc) && (compObj.col == col));
		}
		return false;
	}
}
