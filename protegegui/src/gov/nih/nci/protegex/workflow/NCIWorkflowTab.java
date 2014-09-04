package gov.nih.nci.protegex.workflow;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import gov.nih.nci.protegex.util.Permission;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowHelper;
import gov.nih.nci.protegex.util.WorkflowUtil;
import gov.nih.nci.protegex.workflow.event.WorkflowEvent;
import gov.nih.nci.protegex.workflow.event.WorkflowListener;
import gov.nih.nci.protegex.workflow.wiki.BiomedGTParser;

/**
 * This tab handles the Workflow for Protege. It contains a list of assignments
 * stored in a table. Each assignment can contain a proposed concept and/or
 * existing concept. This tab allows a manager to assign tasks to various
 * modelers. When a modeler is logged-in, this tabs displays the specific
 * assignments for this user. Each assignment goes through the following series
 * of steps:
 * 
 * Created --> Assigned --> Accepted --> Updated --> Completed.
 * 
 * As the user goes through these steps, he/she can add notes to the assignment.
 * This allows the manager to keep track of the progress for this assignment.
 * 
 * @author David Yee
 */
public class NCIWorkflowTab extends AbstractTabWidget {
	// Serial Version UID
	private static final long serialVersionUID = 441936038070346933L;

	private static final String TAB_NAME = NCIWorkflowTab.class.getSimpleName();

	private static final String TITLE = "NCI Workflow";

	private static final String TITLE_NEW = TITLE + " *";

	private static final String KEYSTORE_FILE = "biomedgt.keystore";

	public static String WIKI_URL = BiomedGTParser.DEFAULT_WIKI_URL;

	public static String WIKI_NS_PREFIX = "";

	public static String WIKI_TOP_PAGE = BiomedGTParser.EXPORT_LIST;

	// UI Labels
	private static final String IMPORT_SMW = "Import SMW";

	private static final String EXPORT_SMW = "Export SMW";

	private static final String CREATE = "Create";

	private static final String CREATE_USER = "[ Create User ]";

	// Member Variables:
	private Logger _logger = Log.getLogger(getClass());

	private KnowledgeBase _kb;

	private OWLModel _owlModel;

	private JComboBox _usersComboBox;

	private AssignmentTable _table;

	private Workflow _workflow;

	private WorkflowHelper _workflowHelper;

	private WorkflowUser _user;

	private boolean _isAdmin = false;

	private ArrayList<WorkflowUser> _users;

	private Permission _permission;

	private CreateUserDialog _createUserDialog;

	private String _prevUsersComboBoxSelection = "";
	
	private Preferences preferences = null;

	/**
	 * Closes this tab. Allows this tab to clean up any temporary objects.
	 */
	public void close() {
		super.close();
		preferences.put("workflow_tab_title", UIUtil.getTabTitle(TAB_NAME));

		if (_table != null)
			_table.close();
	}

	/**
	 * Initializes the GUI components of this tab.
	 */
	public void initialize() {
		preferences = Preferences.userNodeForPackage(NCIWorkflowTab.class);
		setLabel(preferences.get("workflow_tab_title", TITLE));
		setShortDescription(TITLE);
		setIcon(Icons.getClsesIcon());

		// Note: Allows HTTPS to work on the Wiki.
		System.setProperty("javax.net.ssl.trustStore", KEYSTORE_FILE);

		if (!init(getProject()))
			return;
		create();

	}

	/**
	 * Disposes and cleanup any unused objects.
	 */
	public void dispose() {

		// getMainWindowMenuBar().remove(_workflowMenu);
		super.dispose();
	}

	/**
	 * Opens or creates a new Workflow.
	 * 
	 * @param project
	 *            The Project.
	 * @return The Workflow.
	 * @throws WorkflowException
	 */
	private Workflow getWorkflow(Project project) throws WorkflowException,
			Exception {
		Workflow workflow = null;
		String name = project.getLocalUser();

		// Note: If user is currently a workflow user, open his/her workflow.
		try {
			workflow = Workflow.openWorkflow(name, _kb);
		} catch (WorkflowException e) {
			_logger.warning("Unable to open workflow " + e.getMessage());
		}

		if ((workflow == null) && _permission.isWorkflowAdmin()) {

			// check if admin and then try to create
			try {

				workflow = Workflow.createWorkflow(name,
						WorkflowUser.Type.ADMIN, _kb);
			} catch (WorkflowException e) {
				_logger.warning("Unable to create a workflow object, "
						+ e.getMessage());
			}
		}

		if (workflow == null) {
			// user is modeler with no workflow created yet or admin but
			// workflow can't be created
			String msg = name + " is currently not a Workflow user.  \n"
					+ "In order to access this Workflow tab, \n"
					+ "a Workflow Manager must grant you the \n"
					+ "proper Workflow privileges.";
			throw new Exception(msg);
		} else {
			return workflow;
		}
	}

	
	/**
	 * Initializes the member variables.
	 * 
	 * @param project
	 *            The Project.
	 * @return true if no exception occurred.
	 */
	private boolean init(Project project) {

		if (!project.isMultiUserClient()) {
			add(new JLabel("Workflow is not available in DB mode"));
			return false;

		}
		try {
			_kb = getProject().getKnowledgeBase();
			_owlModel = (OWLModel) _kb;
			if (WIKI_NS_PREFIX.length() <= 0)
				WIKI_NS_PREFIX = _owlModel.getNamespaceManager().getPrefix(
						_owlModel.getNamespaceManager().getDefaultNamespace())
						.toUpperCase();

			_permission = newPermissions(_kb);
			_workflow = getWorkflow(project);
			_workflowHelper = WorkflowHelper.getInstance(_workflow);
			_user = _workflow.getUser();
			_isAdmin = WorkflowUtil.isAdmin(_user.getUserRole());
			if (_isAdmin)
				_users = _workflow.getAllUsers();
			_workflow.addWorkflowListener(new WorkflowHandler());
			return true;
		} catch (WorkflowException e) {
			add(new JLabel("Workflow will be available in the 1.2.1 release"));
			return false;
		} catch (Exception e) {
			add(new JLabel(e.getMessage(), SwingConstants.CENTER));
			_logger.warning(e.getMessage());
			return false;
		}
	}

	/**
	 * Creates and sets permissions based on what is stored in the remote client
	 * frame store.
	 * 
	 * @param kb
	 *            The KnowledgeBase.
	 * @return The permission.
	 */
	private Permission newPermissions(KnowledgeBase kb) {
		Permission permission = new Permission(kb);
		permission.set(Permission.EDIT_ASSIGNMENT);
		permission.set(Permission.CREATE_ASSIGNMENT);
		permission.set(Permission.EDIT_USER);
		permission.set(Permission.CREATE_SUGGESTION_ASSIGNMENT);
		permission.set(Permission.DELETE_ASSIGNMENT);
		permission.print();
		return permission;
	}

	/**
	 * Creates the GUI components for this tab.
	 */
	private void create() {
		_table = newTable();

		setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(_table);
		add(scrollPane, BorderLayout.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(newUserPanel(), BorderLayout.WEST);
		panel.add(newButtonPanel(), BorderLayout.EAST);
		add(panel, BorderLayout.SOUTH);
		loadTable();
	}

	/**
	 * Creates the users panel that contains user specific GUI components.
	 * 
	 * @return The panel.
	 */
	private JPanel newUserPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel(" User: "));
		if (_isAdmin) {
			_usersComboBox = new JComboBox(new DefaultComboBoxModel(
					WorkflowUtil.getUsers(_users, true)));
			_usersComboBox.addItem(CREATE_USER);
			_usersComboBox.setSelectedItem(WorkflowUtil.getDisplayName(_user,
					true));
			_usersComboBox.addActionListener(new ButtonHandler());
			panel.add(_usersComboBox);
		} else
			panel.add(new JLabel(_user.getUserName()));
		return panel;
	}

	/**
	 * Creates the button panel that contains button specific GUI components.
	 * 
	 * @return The panel.
	 */
	private JPanel newButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton(CREATE);
		button.addActionListener(new ButtonHandler());
		// hide this button for time being
		button.setVisible(false);
		// button.setEnabled(_isWorkflowUser);
		panel.add(button);
		if (_isAdmin) {
			button = new JButton(IMPORT_SMW);
			button.addActionListener(new ButtonHandler());
			panel.add(button);
			button = new JButton(EXPORT_SMW);
			button.addActionListener(new ButtonHandler());
			panel.add(button);
		}
		UIUtil.componentsSameHeight(panel);
		return panel;
	}

	/**
	 * Creates a new assignment table.
	 * 
	 * @return The assignment table.
	 */
	private AssignmentTable newTable() {
		AssignmentTable table = new AssignmentTable(_kb, _workflow);
		table.setModelers(WorkflowUtil.getUsers(_users, false));
		return table;
	}

	/**
	 * Returns the current selected user. Shows which modeler's assignments are
	 * displayed in the table.
	 * 
	 * @return The Workflow user.
	 */
	private WorkflowUser getSelectedUser() {
		if (_isAdmin)
			return _users.get(_usersComboBox.getSelectedIndex());
		return _user;
	}

	/**
	 * Loads the selected user's assignments into the table.
	 */
	private void loadTable() {
		if (_usersComboBox != null)
			_prevUsersComboBoxSelection = (String) _usersComboBox
					.getSelectedItem();
		_table.loadAssignments(getSelectedUser());
	}

	/**
	 * Displays a dialog that allows the user to create a new Workflow user.
	 */
	private void createUser() {
		_usersComboBox.setSelectedItem(_prevUsersComboBoxSelection);
		_createUserDialog = new CreateUserDialog(UIUtil.getFrame(this), _kb,
				_workflow);
		_createUserDialog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(CreateUserDialog.APPLY))
					updateUsers(_createUserDialog.getUser());
			}
		});
		_createUserDialog.setVisible(true);
	}

	/**
	 * Updates the users comboBox when a new user has been created. Then set the
	 * comboBox to the specified selected user.
	 * 
	 * @param selectedUser
	 *            The selected user.
	 */
	private void updateUsers(WorkflowUser selectedUser) {
		try {
			boolean displayMarker = true;
			boolean switchToCreatedUser = false;

			_users = _workflow.getAllUsers();
			_usersComboBox.setModel(new DefaultComboBoxModel(WorkflowUtil
					.getUsers(_users, displayMarker)));
			_usersComboBox.addItem(CREATE_USER);
			if (selectedUser == null)
				return;

			if (switchToCreatedUser)
				_usersComboBox.setSelectedItem(WorkflowUtil.getDisplayName(
						selectedUser, displayMarker));
			else
				_usersComboBox.setSelectedItem(_prevUsersComboBoxSelection);

			_table.setModelers(WorkflowUtil.getUsers(_users, false));
			if (switchToCreatedUser)
				loadTable();
		} catch (WorkflowException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Imports assignments from Semantic Media Wiki.
	 */
	private void importSMW() {
		_table.importSMW(WorkflowUtil.removeMarker((String) _usersComboBox
				.getSelectedItem()), _user.getUserName());
	}

	/**
	 * Exports package back to the Semantic Media Wiki.
	 */
	private void exportSMW() {
		_table.exportSMW();
	}

	/**
	 * The class handles performing the button actions in this tab.
	 */
	private class ButtonHandler implements ActionListener {
		/**
		 * Performs the action specified by the event.
		 * 
		 * @param event
		 *            The ActionEvent.
		 */
		public void actionPerformed(ActionEvent event) {
			Object obj = event.getSource();
			if (obj instanceof JButton) {
				JButton button = (JButton) obj;
				if (button.getText().equals(CREATE))
					_workflowHelper
							.createDefaultAssignmentForModeler(getSelectedUser()
									.getUserName());
				else if (button.getText().equals(IMPORT_SMW))
					importSMW();
				else if (button.getText().equals(EXPORT_SMW))
					exportSMW();
				else
					_logger.log(Level.INFO, "Selected button: "
							+ button.getText());
			} else if (obj == _usersComboBox) {
				String selected = (String) _usersComboBox.getSelectedItem();
				if (selected.equals(CREATE_USER))
					createUser();
				else
					loadTable();
			}
		}
	}

	public void addChangeListener() {

		UIUtil.getTabbedPane().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (UIUtil.tabHasFocus(TAB_NAME))
					UIUtil.setTabTitle(TAB_NAME, TITLE);
			}

		});
	}

	

	/**
	 * This class handles any event changes to any of the assignment objects.
	 */
	private class WorkflowHandler implements WorkflowListener {
		/**
		 * Performs the action specified by the event.
		 * 
		 * @param event
		 *            The ActionEvent.
		 */
		public void workActionPerformed(WorkflowEvent event) {
			WorkflowEvent.Type type = event.getType();
			if (type != WorkflowEvent.Type.CREATEASSIGNMENT
					|| UIUtil.tabHasFocus(TAB_NAME))
				return;
			UIUtil.setTabTitle(NCIWorkflowTab.class.getSimpleName(), TITLE_NEW);

		}
	}
}
