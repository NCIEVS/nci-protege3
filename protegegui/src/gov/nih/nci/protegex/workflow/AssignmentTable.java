package gov.nih.nci.protegex.workflow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protegex.owl.ui.dialogs.*;
import edu.stanford.smi.protegex.owl.ui.*;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.panel.WorkflowPanel;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.util.ClsUtil;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.PopupMenuUtil;
import gov.nih.nci.protegex.util.StringUtil;
import gov.nih.nci.protegex.util.TableUtil;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowUtil;
import gov.nih.nci.protegex.util.XMLToOWLParser;
import gov.nih.nci.protegex.util.WorkflowUtil.ACTUAL_WORK_FIELD;
import gov.nih.nci.protegex.workflow.AssignmentTableModel.Col;
import gov.nih.nci.protegex.workflow.event.WorkflowEvent;
import gov.nih.nci.protegex.workflow.event.WorkflowListener;

/**
 * This tables contains Workflow assignment tasks. Each assignment makes
 * reference to a proposed and/or existing concept.
 * 
 * @author David Yee
 */
public class AssignmentTable extends JTable {
	// Serial Version UID.
	private static final long serialVersionUID = -862122948112543163L;

	// NCIEditTab's Tab Indexes:
	private static final int EDIT_TAB_INDEX = 0;

	//private static final int WORKFLOW_TAB_INDEX = 11;

	// Member variables:
	private Logger _logger = Log.getLogger(getClass());

	private XMLToOWLParser _parser;

	private Workflow _workflow;

    private OWLModel _owlModel;

	//private OWLWrapper _wrapper;

	private AssignmentTableModel _model;
    

	private ArrayList<TableColumn> _initialColumns;
	
	private Preferences _preferences = null;
	
    /** Postfix for setting table column visibility preference */
	private final String VISIBLE = "_visible";

    /** Postfix for setting table column position preference */
    private final String POSITION = "_position";

    /**
	 * Instantiates this class.
	 * 
	 * @param kb
	 *            The KnowledgeBase.
	 * @param workflow
	 *            The Workflow.
	 */
	public AssignmentTable(KnowledgeBase kb, Workflow workflow) {
		init(kb, workflow);
        initPreferences();
		initGUI();
	}

	/**
	 * Initializes the member variables.
	 * 
	 * @param kb
	 *            The KnowledgeBase.
	 * @param workflow
	 *            The Workflow.
	 */
	private void init(KnowledgeBase kb, Workflow workflow) {
		_owlModel = (OWLModel) kb;
		//_wrapper = OWLWrapper.getInstance();
		_workflow = workflow;
		_workflow.addWorkflowListener(new WorkflowHandler());
		_parser = new XMLToOWLParser(_owlModel);
	}

	/**
	 * Initializes the table's GUI components.
	 */
	private void initGUI() {
		setModel(_model = newTableModel());
		_initialColumns = TableUtil.getTableColumns(this);
        setColumnWidths();
		configRenderers();
		
		// configEditors();
		hideDefaultHiddenColumns();
        repositionColumns();
		addMouseListener(new TablePopupMenuAdapter(_workflow));
		getTableHeader().addMouseListener(new TableHeaderPopupMenuAdapter());		
		this.getColumnModel().addColumnModelListener(new MyTableColumnModelListener(this));
        TableUtil.createToolTipDismissDelayListener(this);
        
        JTableHeader header = getTableHeader();
        header.setUpdateTableInRealTime(true);
        header.addMouseListener(_model.new ColumnListener(this));
        header.setReorderingAllowed(true);
	}
    
    /**
     * Returns the tooltip text from the mouse event.
     * @param event The MouseEvent.
     * @return the tooltip text from the mouse event.
     */
    public String getToolTipText(MouseEvent event) {
        ToolTipManager.sharedInstance().setDismissDelay(TableUtil.INFINITE_TIME);
        return super.getToolTipText(event);
    }
    
	/**
	 * Called by Workflow tab when closing. This saves out the preferences items
	 */
	public void close() {
        savePreferences();
	}
    
	private class MyTableColumnModelListener implements TableColumnModelListener {
        private JTable table;
    
        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        public MyTableColumnModelListener(JTable table) {
            this.table = table;
        }
    
        public void columnAdded(TableColumnModelEvent e) {
        	//TableColumn col = (TableColumn) e.getSource();
        	//String nam = (String) col.getIdentifier();
        	
        	//String nam = table.getModel().getColumnName(e.getFromIndex());
        	//Col.valueOf(nam).setVisible(true);
        }
    
        public void columnRemoved(TableColumnModelEvent e) {
        	//TableColumn col = (TableColumn) e.getSource();
        	//String nam = (String) col.getIdentifier();
        	//String nam = table.getModel().getColumnName(e.getFromIndex());
        	//Col.valueOf(nam).setVisible(false);            
        }
    
        public void columnMoved(TableColumnModelEvent e) {
        }
    
        public void columnMarginChanged(ChangeEvent e) {
        	ArrayList<TableColumn> cols = TableUtil.getTableColumns(table);
            // set them all as we don't know which one changed
            for (TableColumn col : cols) {
            	// mucking with enum name() method breaks valueOf
            	Col.find((String)col.getIdentifier()).setWidth(col.getWidth());
            }
        }
    
        public void columnSelectionChanged(ListSelectionEvent e) {
            
        }
    }
	
    /**
     * Initializes the table preferences.
     */
	private void initPreferences() {
        if (_preferences == null)
            _preferences = Preferences.userNodeForPackage(AssignmentTable.class);

        for (Col col : Col.values()) {
            String name = col.getName();
            col.setWidth(_preferences.getInt(name, col.getWidth()));
            col.setVisible(_preferences.getBoolean(name + VISIBLE, col
                .isVisible()));
            col.setInitPosition(_preferences.getInt(name + POSITION, -1));
        }
    }

    /**
     * Saves the table preferences to the file system.
     */
    private void savePreferences() {
        for (Col col : Col.values()) {
            String name = col.getName();
            _preferences.putInt(name, col.getWidth());
            _preferences.putBoolean(name + VISIBLE, col.isVisible());
            _preferences.putInt(name + POSITION, -1); // Initializes position
        }
        for (int i = 0; i < getColumnCount(); ++i) {
            String name = getColumnName(i);
            _preferences.putInt(name + POSITION, i);
        }
    }

    /**
     * Repositions the columns based on the previous sessions.
     * These values are stored in the prefernces variable.
     */
    private void repositionColumns() {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        for (Col col : Col.values()) {
            String name = col.getName();
            int position = col.getInitPosition();
            if (position >= 0)
                map.put(new Integer(position), name);
        }

        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            String name = (String) map.get(key);
            int index = TableUtil.getColumnIndex(this, name);
            if (index >= 0)
                moveColumn(index, key);
        }
    }

    /**
     * Sets up the column widths for the table.
     */
    private void setColumnWidths() {
        setAutoResizeMode(AUTO_RESIZE_OFF);
        for (Col col : Col.values()) {
            TableUtil.setColumnWidth(this, col.getColumnIndex(), col.getWidth());
        }
    }
    
	/**
	 * Configures the cell renderers for the table.
	 */
	private void configRenderers() {
		TableUtil.JLabelCenterRenderer center = new TableUtil.JLabelCenterRenderer(true);
		TableUtil.JLabelLeftRenderer left = new TableUtil.JLabelLeftRenderer(true);
        TableUtil.JLabelLeftRenderer leftWrap = new TableUtil.JLabelLeftRenderer(true);
        leftWrap.setWrapToolTip(true);
		TableUtil.DateRenderer date = new TableUtil.DateRenderer(true);
		TableUtil.NotesRenderer notes = new TableUtil.NotesRenderer(true);
		notes.setWrapToolTip(true);
		TableUtil.LastTokenRenderer lastToken = new TableUtil.LastTokenRenderer("/", true);

		getColumn(Col.ID.getName()).setCellRenderer(center);
		getColumn(Col.STATUS.getName()).setCellRenderer(center);
		getColumn(Col.DATE_CREATED.getName()).setCellRenderer(date);
		getColumn(Col.LAST_UPDATED.getName()).setCellRenderer(date);
		getColumn(Col.MANAGER.getName()).setCellRenderer(center);
		getColumn(Col.MODELER.getName()).setCellRenderer(center);
		getColumn(Col.ACTUAL_WORK.getName()).setCellRenderer(leftWrap);
		getColumn(Col.TASK_DESCRIPTION.getName()).setCellRenderer(left);
		getColumn(Col.MODELER_NOTES.getName()).setCellRenderer(notes);
		getColumn(Col.PACKAGE_ID.getName()).setCellRenderer(center);
        getColumn(Col.PACKAGE_NAME.getName()).setCellRenderer(lastToken);
	}

	/**
	 * Hides the default hidden columns in the table.
	 */
	private void hideDefaultHiddenColumns() {
		TableColumnModel model = getColumnModel();
		for (Col col : Col.values())
			if (! col.isVisible())
				model.removeColumn(getColumn(col.getName()));
	}

	/**
	 * Creates a new table model.
	 * 
	 * @return
	 */
	private AssignmentTableModel newTableModel() {
		AssignmentTableModel model = new AssignmentTableModel(_workflow);
		model.addTableModelListener(new TableModelHandler());
		return model;
	}

	/**
	 * Sets (or updates) the list of modelers. This method updates the Modeler's
	 * column cell editor.
	 * 
	 * @param modelers
	 *            The list of modelers.
	 */
	public void setModelers(String[] modelers) {
		TableUtil.getColumn(_initialColumns, Col.MODELER.getName())
				.setCellEditor(new TableUtil.JComboBoxCellEditor(modelers));
	}

	/**
	 * Loads assignments for a specific user.
	 * 
	 * @param user
	 *            The user.
	 */
	public void loadAssignments(WorkflowUser user) {
		_model.load(user);
	}

	/**
	 * Imports assigments from the Semantic Media Wiki.
	 * 
	 * @param tableUsername
	 *            The name of the user whose assignments are currently being
	 *            displayed.
	 * @param loggedInUsername
	 *            The name of the current logged in user.
	 */
	public void importSMW(String tableUsername, String loggedInUsername) {
		try {
			ArrayList<Assignment> assignments = 
			    _workflow.importAssignmentsFromWiki(
			        NCIWorkflowTab.WIKI_URL, NCIWorkflowTab.WIKI_TOP_PAGE,
			        NCIWorkflowTab.WIKI_NS_PREFIX);
			addAssignments(assignments, tableUsername, loggedInUsername);
		} catch (WorkflowException e) {
			MsgDialog.error(this, 
			    "Import Error (URL: " + NCIWorkflowTab.WIKI_URL + ")", 
			    e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Adds assignments if the current table is displaying the current logged-in
	 * users assignments. If not, display a message.
	 * 
	 * @param assignments
	 *            The assignments.
	 * @param tableUsername
	 *            The name of the user whose assignments are currently being
	 *            displayed.
	 * @param loggedInUsername
	 *            The name of the current logged in user.
	 */
	private void addAssignments(ArrayList<Assignment> assignments,
			String tableUsername, String loggedInUsername) {
		Iterator<Assignment> iterator = assignments.iterator();
		ArrayList<Object> idList = new ArrayList<Object>();
		while (iterator.hasNext()) {
			try {
				Assignment a = (Assignment) iterator.next();
				_workflow.storeAssignment(a);
				if (a.getModeler().equals(tableUsername))
					_model.addRow(a);
				idList.add((Object) new Integer(a.getIdentifier()));
			} catch (WorkflowException e) {
				e.printStackTrace();
			}
		}
		if (idList.size() <= 0) {
			MsgDialog.ok(this, "Nothing to import from:\n  "
					+ NCIWorkflowTab.WIKI_URL);
			return;
		}
		MsgDialog.ok(this, StringUtil.formatMessage(
				"The following newly created assignments (IDs)\n"
						+ "are currently assigned to " + loggedInUsername
						+ ".\n\n", idList,
				"\n\nSwitch to this user's assignment table to see them.",
				"    ", 10));
	}

	/**
	 * This class handles updating the table model's data.
	 */
	private class TableModelHandler implements TableModelListener {
		/**
		 * Notifies listeners the cells, rows, or columns that changed.
		 * 
		 * @param event
		 *            The TableModelEvent.
		 */
		public void tableChanged(TableModelEvent event) {
			debugTableModelListener(event);
			updateModel(event);
		}

		/**
		 * Debugs the table model event.
		 * 
		 * @param event
		 *            The TableModelEvent.
		 */
		private void debugTableModelListener(TableModelEvent event) {
			if (true)
				return;
			_logger.log(Level.INFO, "tableChanged" + " type="
					+ TableUtil.convertTableModelEventString(event.getType())
					+ ", firstRow=" + event.getFirstRow() + ", lastRow="
					+ event.getLastRow() + ", col=" + event.getColumn()
					+ ", src=" + event.getSource());
		}

		/**
		 * Updates the modeler cell.
		 * 
		 * @param model
		 *            The TableModel.
		 * @param row
		 *            The row of the cell.
		 * @param col
		 *            The column of the cell.
		 * @return true if updated.
		 */
		private boolean updateModeler(AssignmentTableModel model, int row,
				int col) {
			Assignment a = (Assignment) model.getValueAt(row, 0);
			String prevValue = a.getModeler();
			String newValue = (String) model.getValueAt(row, col);
			if (newValue.equals(prevValue))
				return false;

			a.setModeler(newValue);
			a.setStatus(Assignment.Status.ASSIGNED);
			return true;
		}

		/**
		 * Updates the current model.
		 * 
		 * @param event
		 *            The TableModelEvent.
		 * @return true if updated.
		 */
		private boolean updateModel(TableModelEvent event) {
			if (event.getType() != TableModelEvent.UPDATE)
				return false;

			int c = event.getColumn();
			AssignmentTableModel model = (AssignmentTableModel) event
					.getSource();
			for (int r = event.getFirstRow(); r <= event.getLastRow(); ++r) {
				Assignment a = (Assignment) model.getValueAt(r, 0);
				try {
					boolean updated = false;
					if (c == Col.MODELER.getColumnIndex())
						updated = updateModeler(model, r, c);
					if (updated) {
						_workflow.storeAssignment(a);
					}
				} catch (WorkflowException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	}

	/**
	 * This class handles performing the actions in the table header's popup
	 * menu.
	 */
	private class TableHeaderPopupMenuHandler extends
			PopupMenuUtil.PopupMenuHandler {
		// Serial Version UID
		private static final long serialVersionUID = 5987332447437874393L;

		// Cancel Menu
		private static final String CANCEL_MENU = "Cancel";

		/**
		 * Instantiates this class.
		 */
		public TableHeaderPopupMenuHandler() {
			int n = AssignmentTable.this._model.getColumnCount();
			for (int i = 0; i < n; ++i) {
				String columnName = AssignmentTable.this._model
						.getColumnName(i);

				// Do not display the following column names in menu.
				if (columnName.equals(Col.ASSIGNMENT.getName())
						|| columnName.equals(Col.ID.getName())
						|| columnName.equals(Col.PACKAGE_NAME.getName()))
					continue;

				addCheckBoxMenuItem(columnName, TableUtil.isColumnDisplayed(
						AssignmentTable.this, columnName));
			}
			addSeparator();
			addMenuItem(CANCEL_MENU);
		}

		/**
		 * Performs the action specified by the event.
		 * 
		 * @param event
		 *            The ActionEvent.
		 */
		public void actionPerformed(ActionEvent event) {
			Object obj = event.getSource();
			if (obj instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) obj;
				String text = item.getText();
                Col.find(text).setVisible(item.isSelected());
				if (!item.isSelected()) {
					getColumnModel().removeColumn(getColumn(text));
				} else {
					TableColumn column =
                        TableUtil.getColumn(_initialColumns, text);
					getColumnModel().addColumn(column);
					
				}
			}
		}
	}

	/**
	 * This class handles displaying the table header's popup menu.
	 */
	private class TableHeaderPopupMenuAdapter extends
			PopupMenuUtil.PopupMenuAdapter {
		private TableHeaderPopupMenuHandler _popupMenu = new TableHeaderPopupMenuHandler();

		protected void show(MouseEvent e) {
			_popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * This class handles performing the actions in the table's popup menu.
	 */
	private class TablePopupMenuHandler extends PopupMenuUtil.PopupMenuHandler {
		// Serial Version UID
		private static final long serialVersionUID = 4871347483035624380L;

		// List of menu labels:
		private static final String EDIT_MENU = "Edit Concept";

		private static final String ACCEPT_MENU = "Accept Assignment";

		private static final String MODIFY_MENU = "Modify Assignment";

		private static final String COMPLETE_MENU = "Complete Assignment";

		private static final String REJECT_MENU = "Reject Assignment";

		private static final String DELETE_MENU = "Delete Assignment";

        private static final String CANCEL_MENU = "Cancel";

		// List of menu items:
		private JMenuItem _editMenu;

		private JMenuItem _modifyMenu;

		private JMenuItem _acceptMenu;

		private JMenuItem _completeMenu;

		private JMenuItem _rejectMenu;

		private JMenuItem _deleteMenu;

		// Is Admin flag.
		// Dave, this is no longer needed
		//private boolean _isAdmin = false;

		/**
		 * Constructs this class.
		 * 
		 * @param isAdmin
		 *            true if logged-in user is an admin.
		 */
		public TablePopupMenuHandler() {
			//_isAdmin = isAdmin;
			_editMenu = addMenuItem(EDIT_MENU);
			_acceptMenu = addMenuItem(ACCEPT_MENU);
			_modifyMenu = addMenuItem(MODIFY_MENU);
			_completeMenu = addMenuItem(COMPLETE_MENU);
			addSeparator();
			_rejectMenu = addMenuItem(REJECT_MENU);
			_deleteMenu = addMenuItem(DELETE_MENU);
			addSeparator();
			addMenuItem(CANCEL_MENU);
		}

		/**
		 * Enables and disables certain menu items.
		 * 
		 * @param invoker
		 *            The invoker.
		 */
		private void enableMenu(Component invoker) {
			boolean isSingleSelection = AssignmentTable.this
					.getSelectedRowCount() == 1;
			boolean isMultipleSelected = AssignmentTable.this
					.getSelectedRowCount() > 0;

			if (isMultipleSelected) {
				_editMenu.setEnabled(false);
				_acceptMenu.setEnabled(false);
				_modifyMenu.setEnabled(false);
				_completeMenu.setEnabled(false);
				_rejectMenu.setEnabled(false);
				// currently all you can do with multiple selects is delete if
				// you are an admin
				_deleteMenu.setEnabled(true);
			}

			if (isSingleSelection) {
				Assignment a = (Assignment) ((JTable) invoker).getModel()
						.getValueAt(AssignmentTable.this.getSelectedRow(), 0);

				_editMenu.setEnabled(_workflow.canDo(a,
						Workflow.RequestedAction.EDIT));
				_acceptMenu.setEnabled(_workflow.canDo(a,
						Workflow.RequestedAction.ACCEPT));
				_modifyMenu.setEnabled(_workflow.canDo(a,
						Workflow.RequestedAction.MODIFY));
				_completeMenu.setEnabled(_workflow.canDo(a,
						Workflow.RequestedAction.COMPLETE));
				_rejectMenu.setEnabled(_workflow.canDo(a,
						Workflow.RequestedAction.REJECT));
				_deleteMenu.setEnabled(_workflow.canDo(a,
						Workflow.RequestedAction.DELETE));

				/*
				 * if (_isAdmin) { _editMenu.setEnabled(true);
				 * _acceptMenu.setEnabled(false); _modifyMenu.setEnabled(true);
				 * _completeMenu.setEnabled(false);
				 * _rejectMenu.setEnabled(false); // currently all you can do
				 * with multiple selects is delete _deleteMenu.setEnabled(true); }
				 * else { Assignment.Status status = a.getCurrentStatus();
				 * boolean acceptOrUpdatedOrAssigned = (status ==
				 * Assignment.Status.ASSIGNED || status ==
				 * Assignment.Status.ACCEPTED || status ==
				 * Assignment.Status.UPDATED);
				 * 
				 * _editMenu.setEnabled(acceptOrUpdatedOrAssigned); _acceptMenu
				 * .setEnabled(status == Assignment.Status.ASSIGNED);
				 * _modifyMenu.setEnabled(acceptOrUpdatedOrAssigned);
				 * _completeMenu.setEnabled(acceptOrUpdatedOrAssigned);
				 * _rejectMenu.setEnabled(acceptOrUpdatedOrAssigned); //
				 * modelers can never delete _deleteMenu.setEnabled(false);
				 * 
				 * if (status == Assignment.Status.SUGGESTED)
				 * _modifyMenu.setEnabled(true); }
				 */
			}
		}

		/**
		 * Displays the menu at a specific location.
		 * 
		 * @param invoker
		 *            The invoker.
		 * @param x
		 *            The x position.
		 * @param y
		 *            The y position.
		 */
		public void show(Component invoker, int x, int y) {
			enableMenu(invoker);
			super.show(invoker, x, y);
		}

		private void checkAssignmentAccepted(JMenuItem item, Assignment a)
				throws WorkflowException {
			if ((item == _editMenu) || (item == _modifyMenu)) {
				if (a.getCurrentStatus() == Assignment.Status.ASSIGNED) {
					if (_workflow.getUser().getUserName().equalsIgnoreCase(
							a.getModeler())) {
						//if (!WorkflowUtil.isAdmin(_workflow.getUser()
								//.getUserRole())) {
							a.setStatus(Assignment.Status.ACCEPTED);
							_workflow.storeAssignment(a);
						//}
					}
				}
			}
		}

		/**
		 * Performs the action specified by the event.
		 * 
		 * @param event
		 *            The ActionEvent.
		 */
		public void actionPerformed(ActionEvent event) {
			JMenuItem item = (JMenuItem) event.getSource();
			int[] rows = AssignmentTable.this.getSelectedRows();
			if (rows.length <= 0)
				return;

			try {
				// Currently handling one assignment at a time.
				Assignment a = (Assignment) _model.getValueAt(rows[0], 0);

				checkAssignmentAccepted(item, a);

				if (item == _editMenu)
					editConcept(a);
				else if (item == _modifyMenu)
					modifyAssignment(a);
				else if (item == _deleteMenu)
					_model.removeRows(_workflow, getSelectedRows());
				else if (item == _acceptMenu) {
					a.setStatus(Assignment.Status.ACCEPTED);
					AssignmentTable.this._workflow.storeAssignment(a);
				} else if (item == _completeMenu) {
					a.setStatus(Assignment.Status.COMPLETED);
					AssignmentTable.this._workflow.storeAssignment(a);
				} else if (item == _rejectMenu) {
					NotesDialog dialog = new NotesDialog(UIUtil
							.getFrame(this), a, true);
					//dialog.setVisible(true);
					// a.setStatus(Assignment.Status.REJECTED);
					// AssignmentTable.this._workflow.storeAssignment(a);
				}
			} catch (WorkflowException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * This class handles displaying the table's popup menu.
	 */
	private class TablePopupMenuAdapter extends PopupMenuUtil.PopupMenuAdapter {
		// Is admin flag.
		// Dave, this isn't needed
		// private boolean _isAdmin;

		// The popup menu.
		private TablePopupMenuHandler _popupMenu;

		/**
		 * Constructs this class.
		 * 
		 * @param workflow
		 *            The Workflow.
		 */
		public TablePopupMenuAdapter(Workflow workflow) {
			//boolean isAdmin = WorkflowUtil.isAdmin(workflow.getUser()
					//.getUserRole());
			_popupMenu = new TablePopupMenuHandler();
		}

		/**
		 * Displays the popup menu.
		 * 
		 * @param event
		 *            The MouseEvent.
		 */
		protected void show(MouseEvent event) {
			JTable table = (JTable) event.getComponent();
			if (table.getSelectedRowCount() > 0)
				_popupMenu.show(table, event.getX(), event.getY());
		}
	}

	/**
	 * The NCIEditTab must be displayed in order for an assignment to be edited.
	 * If this tab is set to null, then it displays a warning message.
	 * 
	 * @param tab
	 *            The NCIEditTab.
	 * @param assignment
	 *            The assignment.
	 * @return true if displayed.
	 */
	private boolean canEditAssignment(NCIEditTab tab, Assignment assignment) {
		if (tab != null)
			return true;

		MsgDialog.warning(this, "Can Not Edit Assignment",
				"NCI Editor (NCIEditTab) must be displayed\n"
						+ "to edit an assignment.");
		return false;
	}

	/**
	 * Displays (or edits) the OWLNamedClass in the EditPanel.
	 * 
	 * @param tab
	 *            The NCIEditTab.
	 * @param cls
	 *            The OWLNamedClass.
	 */
	private void editWithEditPanel(NCIEditTab tab, OWLNamedClass cls) {
		EditPanel panel = (EditPanel) tab.getTabbedPane().getComponentAt(
				EDIT_TAB_INDEX);
		tab.setSelectedInstance(cls);
        tab.ensureClassSelected(cls);
		tab.getEditPanel().setSelectedInstance(cls);
		tab.getEditPanel().updateAll();

        tab.getTabbedPane().setSelectedComponent(panel);
		UIUtil.selectTab(tab);
	}
    
	/**
	 * Displays (or edits) the proposed concept class (upperCls) and the
	 * existing class (lowerCls) in the WorkflowPanel.
	 * 
	 * @param tab
	 *            The NCIEditTab.
	 * @param items
	 *            The proposed concept class stored within TreeItems.
	 * @param lowerCls
	 *            The existing class.
	 * @param assignment
	 *            The assignment.
	 */
    private void editWithWorkflowPanel(NCIEditTab tab, TreeItems items,
        OWLNamedClass lowerCls, Assignment assignment) {
        tab.ensureClassSelected(lowerCls);
        WorkflowPanel panel = tab.getWorkflowPanel();
        panel.loadUpperPanel(items);

        if (lowerCls != null)
            panel.loadLowerPanel(lowerCls);
        panel.setupWhileLoadingAssignment(assignment);

        tab.getTabbedPane().setSelectedComponent(panel);
        UIUtil.selectTab(tab);
    }

	/**
	 * Removes any proposed concept loaded in the WorkflowPanel (if any).
	 * 
	 * @param tab
	 *            NCIEditTab.
	 * @return true if concepts are removed.
	 */
	private boolean clearProposedConcept(NCIEditTab tab) {
		WorkflowPanel panel = tab.getWorkflowPanel();
		return panel.clear();
	}

	/**
	 * Displays (or edits) any concepts (that are referenced in the assignment)
	 * in either the EditPanel or WorkflowPanel.
	 * 
	 * @param assignment
	 *            The assignment.
	 */
	private void editConcept(Assignment assignment) {
		NCIEditTab tab = (NCIEditTab) UIUtil.getTab(NCIEditTab.class.getName());
		if (!canEditAssignment(tab, assignment))
			return;
		if (!clearProposedConcept(tab))
			return;

        String content = WorkflowUtil.getActualWorkValue(assignment, 
            WorkflowUtil.ACTUAL_WORK_FIELD.CONTENT);

        if (ClsUtil.isCode(content)) {
            _logger.log(Level.INFO, "Case 1: ActualWork=" + content);
            editWithEditPanel(tab, ClsUtil.getConceptByCode(_owlModel, content));
            return;
        }

        boolean isStructured = assignment.isStructured();
		if (isStructured) {
	        String error = "";
			TreeItems items = _parser.processXml(content);
            String pt = items.elementAt(0).getNameValue();
            XMLToOWLParser.ProposalType proposalType = _parser.getProposalType();
			if (proposalType == XMLToOWLParser.ProposalType.Structured) {
				String code = _parser.getCode();
				_logger.log(Level.INFO, "Cases 2: Structured proposal: " + 
				    pt + " (" + code + ")");
				OWLNamedClass existingClass = ClsUtil.getConceptByCode(_owlModel, code);
				if (existingClass == null) {
					error += "* Could not load existing concept:\n";
					error += "  " + pt + "(" + code + ") does not exist.\n";
				}
				editWithWorkflowPanel(tab, items, existingClass, assignment);
			} else if (proposalType == XMLToOWLParser.ProposalType.NewConcept) {
				_logger.log(Level.INFO,
						"Cases 3: Structured proposal for new concept: " + pt);
				//Note: Retrieves newly created concept code (if any) when the 
				//  user created a concept with "Accept" option.
                String code = WorkflowUtil.getActualWorkValue(assignment, 
                    ACTUAL_WORK_FIELD.NEW_CONCEPT_CODE);
                OWLNamedClass existingClass = ClsUtil.getConceptByCode(_owlModel, code);
                editWithWorkflowPanel(tab, items, existingClass, assignment);
			}
			error += _parser.getWarningMsg();
			if (error.length() > 0)
				MsgDialog.warning(this, "Proposed Concept Warning: " + pt, error);
		} else if (content.startsWith("http")) {
			_logger.log(Level.INFO, "Case 4: ActualWork=" + content);
            
            
            String urlbeg = content.substring(0,content.indexOf("Special:"));
            String urlend = content.substring(content.indexOf("Special:") + 18);
            
            
            try {
                
              JEditorPane htmlPane = createHTMLBrowser(urlbeg + urlend);
              htmlPane.setEditable(false);
              
              JScrollPane pane = new JScrollPane(htmlPane);
              //pane.setPreferredSize(new Dimension(500,600));
              ProtegeUI.getModalDialogFactory().showDialog(UIUtil.getFrame(this), pane, "Unstructured Proposal",
                                                           ModalDialogFactory.MODE_CLOSE);
            } catch(Exception ioe) {
              System.err.println("Error displaying " + urlbeg + urlend);
            }
			//MsgDialog.ok(this, "Display in Browser", content);
		} else {
			MsgDialog.ok(this, "Unknown Case", "Do nothing.");
		}
	}
    
    private JEditorPane createHTMLBrowser(String url) {
        JEditorPane pane = new JEditorPane("text/html","<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><body><p><a href=\"" + url + "\">Click here to view in WIKI</a></p></body></html>") {
            @Override
            public void paint(Graphics g) {
                ComponentUtilities.enableAllAntialiasing(g);
                super.paint(g);
            }
        };
        pane.setEditable(false);
        pane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        
                        BrowserLauncher.openURL(e.getURL().toString());
                    } catch (IOException ex) {
                        Log.getLogger().warning(ex.toString());
                    }                    
                }
            }
        });
        
        return pane;
    }

    

	/**
	 * Displays a dialog that allows the user to modify the assignment fields.
	 * 
	 * @param assignment
	 *            The assignment.
	 */
	private void modifyAssignment(Assignment assignment) {
		EditAssignmentDialog dialog = new EditAssignmentDialog(UIUtil
				.getFrame(this), _workflow, assignment);
		//dialog.setVisible(true);
	}
	
//    /**
//     * Exports package back to the Semantic Media Wiki.
//     */
//    public void exportSMW() {
//        String title = "Export Package";
//        JFrame frame = UIUtil.getFrame(this);
//        if (! isSelectedSamePackage()) {
//            MsgDialog.warning(frame, title, 
//                "All the selected rows must be within the same package.\n" +
//                "Note: You could also select one assignment within the package.");
//            return;
//        }
//        int selectedRow = getSelectedRow();
//        if (selectedRow < 0) {
//            MsgDialog.warning(frame, title, "Must select a row.");
//            return;
//        }
//        Assignment assignment = (Assignment) getModel().getValueAt(
//            selectedRow, 0);
//        if (assignment == null) {
//            MsgDialog.warning(frame, title, "Assignment is null.");
//            return;
//        }
//        exportPackage(assignment);
//    }
    
    /**
     * Exports package back to the Semantic Media Wiki.
     */
    public void exportSMW() {
        JFrame frame = UIUtil.getFrame(this);
        String title = "Export Package";
        String[] pkgs = _model.getPackageNames(true);
        if (pkgs.length <= 0) {
            MsgDialog.warning(frame, title, "No packages to export.");
            return;
        }
        
        String pkg = (String) JOptionPane.showInputDialog(frame,
            "Select a package to export:", title,
            JOptionPane.QUESTION_MESSAGE, null, pkgs, pkgs[0]);
        if (pkg == null || pkg.length() <= 0)
            return;

        String pkgIdText = "pkgId=";
        int i = pkg.indexOf(pkgIdText);
        String pkgIdString = pkg.substring(i + pkgIdText.length());
        pkgIdString = pkgIdString.substring(0, pkgIdString.length()-1);
        int pkgId = Integer.parseInt(pkgIdString);
        
        exportPackage(pkg, pkgId);
    }

//    /**
//     * Exports the current assignment's package to the Wiki.
//     * @param assignment The assignment.
//     */
//    private void exportPackage(Assignment assignment) {
//        String pkgUrl = WorkflowUtil.getActualWorkValue(assignment, 
//            WorkflowUtil.ACTUAL_WORK_FIELD.PACKAGE_URL);
//        pkgUrl = StringUtil.getLastToken(pkgUrl, "/");
//        ExportPackageDialog dialog = new ExportPackageDialog(
//            UIUtil.getFrame(this), _owlModel, pkgUrl,
//            WorkflowUtil.getAssignmentsFromAPackage(
//                _model, assignment.getPackageId()));
//        dialog.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (! e.getActionCommand().equals(CreateUserDialog.APPLY))
//                    return;
//                removeExportedPackage((JDialog) e.getSource());
//            }
//        });
//        dialog.setVisible(true);
//    }
    
    /**
     * Exports the current assignment's package to the Wiki.
     * @param pkgName The package name.
     * @param pkgId The package Id.
     */
    private void exportPackage(String pkgName, int pkgId) {
        final int pid = pkgId;
        ExportPackageDialog dialog = new ExportPackageDialog(
            UIUtil.getFrame(this), _owlModel, pkgName,
            WorkflowUtil.getAssignmentsFromAPackage(
                _model, pkgId));
        dialog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (! e.getActionCommand().equals(CreateUserDialog.APPLY))
                    return;
                removeExportedPackage((JDialog) e.getSource(), pid);
            }
        });
        dialog.setVisible(true);
    }

    /**
     * Removes rows related to a specific package.
     */
    private void removeExportedPackage(JDialog dialog, int pkgId) {
        int rows[] = getRowsFromPackage(pkgId);
        _model.removeRows(_workflow, rows);
        dialog.setVisible(false);
        MsgDialog.ok(dialog, "Export Package", 
            "Package sucessfully exported.\n" +
            "Note: Archived assignments related\n" +
            "    to this package.");
    }
    
    /**
     * Returns a list of rows for a specific packageId.
     * @param packageId The packageId.
     * @return a list of rows for a specific packageId.
     */
    private int[] getRowsFromPackage(Integer packageId) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<_model.getRowCount(); ++i) {
            Integer pkgId = (Integer) _model.getValueAt(
                i, AssignmentTableModel.Col.PACKAGE_ID.getColumnIndex());
            if (pkgId == packageId)
                list.add(i);
        }
        return UIUtil.toInts(list);
    }
    
    /**
     * Returns true if the specified rows are within the same package.
     * @param rows The list of rows.
     * @return true if the specified rows are within the same package.
     */
    public boolean isSamePackage(int rows[]) {
        int pkgId = -1;
        for (int i=0; i<rows.length; ++i) {
            Integer ID = (Integer) _model.getValueAt(
                rows[i], AssignmentTableModel.Col.PACKAGE_ID.getColumnIndex());
            int id = ID.intValue();
            if (pkgId < 0)
                pkgId = id;
            else if (id != pkgId)
                return false;
        }
        return true;
    }
    
    /**
     * Returns true if selected rows are within the same package.
     * @return true if selected rows are within the same package.
     */
    public boolean isSelectedSamePackage() {
        int rows[] = getSelectedRows();
        return isSamePackage(rows);
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
			Assignment a = null;
			Object obj = event.getObject();
			if (obj != null && obj instanceof Assignment)
				a = (Assignment) obj;

			// System.out.println("Debug: type=" + type + ", id=" +
			// (a != null ? a.getIdentifier() : "null"));
			if (type == WorkflowEvent.Type.CREATEASSIGNMENT)
				_model.addRow(a);
			else if (type == WorkflowEvent.Type.DELETEASSIGNMENT)
				_model.removeRowWithID(a.getIdentifier());
			else if (type == WorkflowEvent.Type.STOREASSIGNMENT)
				_model.update(a);
		}
	}
}
