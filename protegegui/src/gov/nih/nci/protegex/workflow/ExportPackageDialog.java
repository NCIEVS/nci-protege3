package gov.nih.nci.protegex.workflow;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.owlexport.GenerateWikiText;
import gov.nih.nci.protegex.util.ClsUtil;
import gov.nih.nci.protegex.util.DialogHelper;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.OKApplyCancelDialog;
import gov.nih.nci.protegex.util.OKSuppressDialog;
import gov.nih.nci.protegex.util.PopupMenuUtil;
import gov.nih.nci.protegex.util.StringUtil;
import gov.nih.nci.protegex.util.TableUtil;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowUtil;
import gov.nih.nci.protegex.util.XMLToOWLParser;
import gov.nih.nci.protegex.util.YesNoDialog;
import gov.nih.nci.protegex.util.WorkflowUtil.ACTUAL_WORK_FIELD;
import gov.nih.nci.protegex.workflow.wiki.BiomedGTWiki;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * This dialog allows the user to update package and proposals statuses.
 *
 * @author David Yee
 */
public class ExportPackageDialog extends OKApplyCancelDialog {
    // Serial Version UID
    private static final long serialVersionUID = -4508463196331854572L;
    private static DialogHelper _dialogHelper = new DialogHelper(750, 200);

    // Member variables:
    private Logger _logger = Log.getLogger(getClass());
    private OWLModel _owlModel;
    private JPanel _mainPanel;
    private ProposalTableModel _tableModel;
    private JTable _table;
    private ArrayList<TableColumn> _initialColumns;
    private JLabel _packageStatusL;
    private JComboBox _packageStatusCB;
    
    /**
     * Constructs this class.
     * @param frame The parent frame.
     * @param owlModel The OWLModel.
     * @param packageName The name of the package for the assignments.
     * @param assignment the selected assignment.
     */
    public ExportPackageDialog(JFrame frame, OWLModel owlModel, 
        String packageName, ArrayList<Assignment> assignments) {
        super(frame);
        _owlModel = owlModel;
        _dialogHelper.init(this, true);

        updateMainComponent(assignments);
        if (setDialogEditable())
            setTitle("Export Package: " + packageName);
        else setTitle("Read Only: Export Package: " + packageName);
    }

    /**
     * Sets all the editable components editable if all the assignment
     * statuses are either marked as either completed or rejected.
     * @return true if editable.
     */
    private boolean setDialogEditable() {
        int n = _tableModel.getRowCount();
        boolean isAllCompleted = _tableModel.isAllCompleted();
        //isAllCompleted = true; //DEBUG
        setEditable(isAllCompleted);
        if (isAllCompleted && n >= 0)
            return true;

        if (_suppressMessagePackageNotExportable)
            return false;
        OKSuppressDialog dialog = new OKSuppressDialog(
            UIUtil.getFrame(getParent()), "Export Package: Not Exportable", 
            "All the assignment statuses within a package must be either\n" +
            Assignment.Status.COMPLETED.getName() + " or " + 
            Assignment.Status.REJECTED.getName() + 
            " to be export back to SMW.\n\n" +
            "Note: The following Export Package dialog is read-only.",
            true, 400, 150);
        dialog.setVisible(true);
        setSuppressMessagePackageNotExportable(dialog.isSuppressFutureMessages());
        return false;
    }

    /**
     * Sets the size of this dialog.
     * @param dimension The size.
     */
    public void setSize(Dimension dimension) {
        super.setSize(_dialogHelper.getUpdatedSize(dimension));
    }
    
    /**
     * Sets the location of this dialog.
     * @param location The location.
     */
    public void setLocation(Point location) {
        super.setLocation(_dialogHelper.getUpdatedLocation(location));
    }
    
    /**
     * Cleans up this dialog.
     */
    public void dispose() {
        _dialogHelper.dispose();
        super.dispose();
    }
    
    /**
     * Creates the main panel that contains the GUI components for this dialog.
     */
    protected JComponent newMainComponent() {
        return _mainPanel = new JPanel(new BorderLayout());
    }
    
    /**
     * Updates the main panel.
     * @param assignments The assignments loaded in the proposal table.
     */
    private void updateMainComponent(ArrayList<Assignment> assignments) {
        JPanel panel = _mainPanel;
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(newProposalTable(assignments));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        _buttonPanel.add(newPackageStatusPanel(), 0);
        _buttonPanel.add(new JLabel("    "), 1);
    }
    
    /**
     * The table model that holds a list of proposals from the wiki.
     * @author David Yee
     *
     */
    private static class ProposalTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 3027124972338064517L;

        public enum Col {
            ASSIGNMENT("ASSIGNMENT", 0, 20), 
            ID("ID", 1, 40),
            STATUS("Status", 2, 100), 
            MODELER("Modeler", 3, 150),
            TASK_DESCRIPTION("Task Description", 4, 250),
            PROPOSAL_STATUS("Proposal Status", 5, 100),
            SEND_NOTES("Send Notes", 6, 75),
            ;
        
            // Name member variable and methods
            private String _name = null;
            public String getName() { return _name; }

            // Width member variable and methods
            private int _width = -1;
            public int getWidth() { return _width; }
            public void setWidth(int i) { if (i > 0) { _width = i; } }
            
            // Column index member variable and methods
            private int _columnIndex = -1;
            public int getColumnIndex() { return _columnIndex; }    
            
            // IsVisible member variable and methods
            private boolean _isVisible = true;
            public boolean isVisible() { return _isVisible; }
            public void setVisible(boolean visible) { _isVisible = visible; }
            
            // Column's initial position member variable and methods
            private int _initPosition = -1;
            public int getInitPosition() { return _initPosition; }
            public void setInitPosition(int position) { _initPosition = position; }
            
            /**
             * Returns the column by the column name.
             * @param name The column name.
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
             * @param name The column name.
             * @param index The column index.
             * @param width The column width.
             */
            Col(String name, int index, int width) {
                _name = name;
                _columnIndex = index;
                _width = width;
            }

            static {
                ASSIGNMENT.setVisible(false);
            }

            /**
             * Returns the list of column names.
             * @return the list of column names.
             */
            private static String[] getNames() {
                ArrayList<String> list = new ArrayList<String>();
                for (Col col : values())
                    list.add(col.getName());
                return list.toArray(new String[list.size()]);
            }
        }
        
        // Member Variable(s):
        private boolean _editable = false;
        private boolean _allCompleted = true;
        
        /**
         * Constructs this class.
         * @param assignments The list of assignments to be loaded.
         */
        public ProposalTableModel(ArrayList<Assignment> assignments) {
            super(new Object[0][], Col.getNames());
            _allCompleted = true;
            for (Assignment a : assignments) {
                if (! a.isStructured())
                    continue;
                addRow(newRow(a));
                Assignment.Status status = a.getCurrentStatus();
                _allCompleted = _allCompleted && (
                    status == Assignment.Status.COMPLETED ||
                    status == Assignment.Status.REJECTED);
            }
        }
        
        /**
         * Sets certain columns to be editable.
         * @param editable if true, sets the certain columns editable.
         */
        public void setEditable(boolean editable) {
            _editable = editable;
        }
        
        /**
         * Returns true if certain columns are editable.
         * @return true if certain columns are editable.
         */
        public boolean isEditable() {
            return _editable;
        }
        
        /**
         * Returns true if all assignment statuses are completed and rejected.
         * @return true if all assignment statuses are completed and rejected.
         */
        public boolean isAllCompleted() {
            return _allCompleted;
        }

        public boolean isCellEditable(int row, int column) {
            return _editable && (
                column == Col.PROPOSAL_STATUS._columnIndex ||
                column == Col.SEND_NOTES._columnIndex);
        }
        
        /**
         * Returns the assignments as an array of objects.
         * @param assignment The assignment.
         * @return the assignments as an array of objects.
         */
        private Object[] newRow(Assignment assignment)
        {
            return new Object[] {
                assignment,
                assignment.getIdentifier(),
                assignment.getCurrentStatus(),
                assignment.getModeler(),
                assignment.getTaskDescription(),
                BiomedGTWiki.ProposalStatus.IN_PROGRESS,
                Boolean.TRUE
            };
        }
    }
    
    /**
     * Returns a new proposal table.
     * @param assignments The assignments loaded to this table.
     * @return a new proposal table.
     */
    private JTable newProposalTable(ArrayList<Assignment> assignments) {
        _tableModel = new ProposalTableModel(assignments);
        JTable table = new JTable(_tableModel);

        // Configures column size
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (ProposalTableModel.Col col : ProposalTableModel.Col.values())
            TableUtil.setColumnWidth(table, col.getColumnIndex(), col.getWidth());
        
        // Hides specific columns
        TableColumnModel model = table.getColumnModel();
        for (ProposalTableModel.Col col : ProposalTableModel.Col.values())
            if (! col.isVisible())
                model.removeColumn(table.getColumn(col.getName()));

        // Sets cell renderers
        TableUtil.JLabelLeftRenderer leftR = new TableUtil.JLabelLeftRenderer(true);
        TableUtil.JLabelCenterRenderer centerR = new TableUtil.JLabelCenterRenderer(true);
        TableUtil.BooleanCheckBoxRenderer boolR = new TableUtil.BooleanCheckBoxRenderer(true);
        TableUtil.BooleanCheckBoxCellEditor boolE = new TableUtil.BooleanCheckBoxCellEditor();
        
        table.getColumn(ProposalTableModel.Col.ID.getName()).setCellRenderer(centerR);
        table.getColumn(ProposalTableModel.Col.STATUS.getName()).setCellRenderer(centerR);
        table.getColumn(ProposalTableModel.Col.MODELER.getName()).setCellRenderer(centerR);
        table.getColumn(ProposalTableModel.Col.TASK_DESCRIPTION.getName()).setCellRenderer(leftR);
        table.getColumn(ProposalTableModel.Col.PROPOSAL_STATUS.getName()).setCellRenderer(centerR);
        table.getColumn(ProposalTableModel.Col.SEND_NOTES.getName()).setCellRenderer(boolR);
        table.getColumn(ProposalTableModel.Col.SEND_NOTES.getName()).setCellEditor(boolE);
        
        // Sets cell editors:
        table.getColumn(ProposalTableModel.Col.PROPOSAL_STATUS.getName())
            .setCellEditor(new ProposalStatusComboBoxCellEditor());
        
        _table = table;
        _initialColumns = TableUtil.getTableColumns(_table);
        _table.getTableHeader().addMouseListener(new TableHeaderPopupMenuAdapter());
        return table;
    }
    
    /**
     * Generic JComboBox cell editor.
     */
    private static class ProposalStatusComboBoxCellEditor extends AbstractCellEditor
            implements TableCellEditor, ItemListener {
        // Serial Version UID
        private static final long serialVersionUID = 41902307747006399L;

        //Member variables:
        protected JComboBox _comboBox;

        /**
         * Constructs this class.
         */
        public ProposalStatusComboBoxCellEditor() {
            _comboBox = new JComboBox(BiomedGTWiki.ProposalStatus.getList());
            _comboBox.addItemListener(this);
        }

        /**
         * Returns the cell editor component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int col) {
            _comboBox.setSelectedItem(value);
            return _comboBox;
        }

        /**
         * Returns the cell value.
         * @return the cell value.
         */
        public Object getCellEditorValue() {
            return _comboBox.getSelectedItem();
        }

        /**
         * Handles item state changes.
         * @param event The ItemEvent.
         */
        public void itemStateChanged(ItemEvent event) {
            fireEditingStopped();
        }
    }
    
    /**
     * Returns a new package status panel.
     * @return a new package status panel.
     */
    private JPanel newPackageStatusPanel() {
        _packageStatusL = new JLabel("Package Status:");
        _packageStatusCB = new JComboBox(BiomedGTWiki.PackageStatus.getList());
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(_packageStatusL, BorderLayout.WEST);
        panel.add(_packageStatusCB, BorderLayout.CENTER);
        return panel;
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
            int n = _tableModel.getColumnCount();
            for (int i = 0; i < n; ++i) {
                String columnName = _tableModel.getColumnName(i);

                // Do not display the following column names in menu.
                if (columnName.equals(ProposalTableModel.Col.ASSIGNMENT.getName())
                        || columnName.equals(ProposalTableModel.Col.ID.getName()))
                    continue;

                addCheckBoxMenuItem(columnName, TableUtil.isColumnDisplayed(
                        _table, columnName));
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
                ProposalTableModel.Col.find(text).setVisible(item.isSelected());
                if (!item.isSelected()) {
                    _table.getColumnModel().removeColumn(_table.getColumn(text));
                } else {
                    TableColumn column =
                        TableUtil.getColumn(_initialColumns, text);
                    _table.getColumnModel().addColumn(column);
                    
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
     * Updates proposals statuses and overall package status on the Wiki side.
     */
    protected boolean apply() {
        try {
            if (! canExport()) //DEBUG
                return false;
            if (! canArchiveRows()) //DEBUG
                return false;
            
            int n = _tableModel.getRowCount();
            for (int i=0; i<n; ++i) {
                exportWikiProposal(i);   //DEBUG
                exportProposalStatus(i); //DEBUG
            }
            exportModelerNotes();  //DEBUG
            exportPackageStatus(); //DEBUG
            return super.apply();  //DEBUG
        } catch (Exception e) {
            MsgDialog.error(this, "Export Package: Error", e.getMessage());
            return false;
        }
    }
    
    /**
     * Returns true if all the statuses are no longer set to IN_PROGRESS.
     * @return true if all the statuses are no longer set to IN_PROGRESS.
     */
    private boolean canExport() {
        int n = _tableModel.getRowCount();
        boolean propStatusOK = true;
        for (int i=0; i<n && propStatusOK; ++i) {
            BiomedGTWiki.ProposalStatus propStatus = 
                (BiomedGTWiki.ProposalStatus) _tableModel.getValueAt(
                    i, ProposalTableModel.Col.PROPOSAL_STATUS.getColumnIndex());
            propStatusOK &= propStatus != BiomedGTWiki.ProposalStatus.IN_PROGRESS;
        }
        BiomedGTWiki.PackageStatus pkgStatus = 
            (BiomedGTWiki.PackageStatus) _packageStatusCB.getSelectedItem();
        boolean pkgStatusOK = pkgStatus != BiomedGTWiki.PackageStatus.IN_PROGRESS;

        if (propStatusOK && pkgStatusOK)
            return true;
        
        MsgDialog.warning(this, "Export Package: Update Statuses",
            "In order to export, you must update the following\n" + 
            "statuses so they are no longer set to IN_PROGRESS:\n" +
            "    * All individual proposal statuses\n" +
            "    * Overall package status");
        return false;
    }
    
    /**
     * Returns true if the user allows Protege to archive assignments
     * during a successful export.
     * @return true if assignments can be archived.
     */
    private boolean canArchiveRows() {
        if (_suppressMessageCanArchiveRows)
            return true;
        
        YesNoDialog dialog = new YesNoDialog(this, "Export Package: Archive Assignments", 
            "If export is successful, the assignments located within this\n" + 
            "dialog will be archived (or removed) from the assignment table.\n\n",
            true, 400, 125);
        dialog.setButtonLabel("Continue Export?");
        dialog.defaultNoButton();
        YesNoDialog.ActionHandler handler = new YesNoDialog.ActionHandler();
        dialog.addActionListener(handler);
        dialog.setVisible(true);
        setSuppressMessageCanArchiveRows(dialog.isSuppressFutureMessages());
        return handler.isYes();
    }

    /**
     * Sets this dialog editable.
     * @param editable If true, sets this dialog editable.
     */
    private void setEditable(boolean editable) {
        _tableModel.setEditable(editable);
        _packageStatusL.setEnabled(editable);
        _packageStatusCB.setEnabled(editable);
        _okB.setEnabled(editable);
        _applyB.setEnabled(editable);
    }
    
    /**
     * Exports and updates the proposal located in the Wiki.
     * @param row The TableModel row.
     * @throws Exception
     */
    private void exportWikiProposal(int row) throws Exception {
        Assignment a = (Assignment) _tableModel.getValueAt(
            row, ProposalTableModel.Col.ASSIGNMENT.getColumnIndex());
        if (a.getCurrentStatus() == Assignment.Status.REJECTED)
            return;
        
        String content = WorkflowUtil.getActualWorkValue(a, 
            WorkflowUtil.ACTUAL_WORK_FIELD.CONTENT);
        if (ClsUtil.isCode(content) && ! a.isStructured())
            return;
        
        String propCode = "";
        OWLNamedClass existingClass = null;
        XMLToOWLParser _parser = new XMLToOWLParser(_owlModel);
        _parser.processXml(content);
        XMLToOWLParser.ProposalType propType = _parser.getProposalType();
        if (propType == XMLToOWLParser.ProposalType.Structured) {
            propCode = _parser.getCode();
            existingClass = ClsUtil.getConceptByCode(_owlModel, propCode);
        } else if (propType == XMLToOWLParser.ProposalType.NewConcept) {
            propCode = WorkflowUtil.getActualWorkValue(a, 
                ACTUAL_WORK_FIELD.NEW_CONCEPT_CODE);
            if (propCode.length() <= 0)
                throw new Exception(
                    "New proposed concept was never created.");
            existingClass = ClsUtil.getConceptByCode(_owlModel, propCode);
        } else {
            return;
        }
        
        if (existingClass == null)
            throw new Exception(
                "Can not find concept with code (" + propCode + ")\n" +
                "Can not export proposal changes to the Wiki.");
        
        String propUrl = WorkflowUtil.getActualWorkValue(a, 
            WorkflowUtil.ACTUAL_WORK_FIELD.PROPOSAL_URL);
        exportWikiProposal(propUrl, existingClass);
    }
    
    /**
     * Returns the wiki text for a concept.
     * @param owlModel The OWLModel.
     * @param cls The concept.
     * @return the wiki text for a concept.
     */
    public static String getWikiText(OWLModel owlModel, OWLClass cls)
            throws Exception {
        String ns = cls.getNamespace();
        String nsPrefix = owlModel.getNamespaceManager().getPrefix(ns);
        nsPrefix = BiomedGTWiki.adjustNSPrefix(nsPrefix);
            
        //TODO: Figure out how to get these constant values:
        gov.nih.nci.owlexport.common.Constants.NAMESPACE = nsPrefix;
        gov.nih.nci.owlexport.common.Constants.OID = 
            "2.16.840.1.113883.3.26.1.3";
        
        String xmlPacket = 
            GenerateWikiText.writeOutObjectToString(owlModel, cls);
        return xmlPacket; 
    }

    /**
     * Export and updates the proposal located in the Wiki.
     * @param proposalUrl The proposal URL.
     * @param cls The concept.
     * @throws Exception
     */
    private void exportWikiProposal(String proposalUrl, OWLClass cls) 
            throws Exception {
        String xmlPacket = getWikiText(_owlModel, cls);
        _logger.log(Level.INFO, xmlPacket);
//        MsgDialog.ok(this, "Wiki Text: " + cls.getBrowserText(), //DEBUG
//            proposalUrl + "\n" + xmlPacket);

        BiomedGTWiki wiki = new BiomedGTWiki(BiomedGTWiki.getHostUrl(proposalUrl));
        wiki.updateArticle(proposalUrl, xmlPacket);
    }
    
    /**
     * Exports the package status located in the Wiki.
     * @throws Exception
     */
    private void exportPackageStatus() throws Exception {
        if (_tableModel.getRowCount() <= 0)
            return;

        //Note: Assumes all proposal are from the same package.
        Assignment a = (Assignment) _tableModel.getValueAt(
            0, ProposalTableModel.Col.ASSIGNMENT.getColumnIndex());
        String url = WorkflowUtil.getActualWorkValue(a, 
            WorkflowUtil.ACTUAL_WORK_FIELD.PACKAGE_URL);
        BiomedGTWiki.PackageStatus status = (BiomedGTWiki.PackageStatus) 
            _packageStatusCB.getSelectedItem();
        BiomedGTWiki wiki = new BiomedGTWiki(BiomedGTWiki.getHostUrl(url));
        wiki.updatePackageStatus(url, status);
    }

    /**
     * Exports the proposal status (in a specific row) located in the Wiki.
     * @param row The row containing the proposal.
     * @throws Exception
     */
    private void exportProposalStatus(int row) throws Exception {
        Assignment a = (Assignment) _tableModel.getValueAt(
            row, ProposalTableModel.Col.ASSIGNMENT.getColumnIndex());
        String url = WorkflowUtil.getActualWorkValue(a, 
            WorkflowUtil.ACTUAL_WORK_FIELD.PROPOSAL_URL);
        BiomedGTWiki.ProposalStatus status = 
            (BiomedGTWiki.ProposalStatus) _tableModel.getValueAt(
                row, ProposalTableModel.Col.PROPOSAL_STATUS.getColumnIndex());
        BiomedGTWiki wiki = new BiomedGTWiki(BiomedGTWiki.getHostUrl(url));
        wiki.updateProposalStatus(url, status);
    }
    
    /**
     * Exports all the modeler notes.
     * @throws Exception
     */
    private void exportModelerNotes() throws Exception {
        int n = _tableModel.getRowCount();
        if (n <= 0)
            return;

        boolean hasNotes = false;
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffer.append("<package-notes>\n");
        for (int i=0; i<n; ++i)
            hasNotes |= getModelerNotes(buffer, i);
        if (! hasNotes)
            return;
        buffer.append("</package-notes>\n");
        exportModelerNotes(buffer.toString());
    }
    
    /**
     * Updates the modeler's notes.
     * @param row
     * @return true if there are some notes.
     * @throws Exception
     */
    private boolean getModelerNotes(StringBuffer buffer, int row) throws Exception {
        Boolean isSendNotes = (Boolean) _tableModel.getValueAt(
            row, ProposalTableModel.Col.SEND_NOTES.getColumnIndex());
        if (! isSendNotes)
            return false;
        
        Assignment a = (Assignment) _tableModel.getValueAt(
            row, ProposalTableModel.Col.ASSIGNMENT.getColumnIndex());
        String propUrl = WorkflowUtil.getActualWorkValue(a, 
            WorkflowUtil.ACTUAL_WORK_FIELD.PROPOSAL_URL);
        String propName = StringUtil.getLastToken(propUrl, "/");
        String modelerNote = a.getModelerNote();

        ArrayList<ArrayList<String>> values = WorkflowUtil.parseModelerNotes(modelerNote);
        ArrayList<String> notes = values.get(0);
        ArrayList<String> dates = values.get(1);
        ArrayList<String> editBys = values.get(2);

        int n = notes.size();
        if (n <= 0)
            return false;

        buffer.append("<proposal-notes>\n");
        StringUtil.createXmlTag(buffer, true, "prop-name", propName);
        buffer.append("\n");
        for (int i=0; i<n; ++i) {
            buffer.append("<note>\n");
            StringUtil.createXmlTag(buffer, true, "date", 
                StringUtil.valueAfter(":", dates.get(i)));
            buffer.append("\n");
            StringUtil.createXmlTag(buffer, true, "editor", 
                StringUtil.valueAfter(":", editBys.get(i)));
            buffer.append("\n");
            StringUtil.createXmlTag(buffer, false, "text", 
                "<![CDATA[" + notes.get(i) + "]]>");
            buffer.append("\n");
            buffer.append("</note>\n");
        }
        buffer.append("</proposal-notes>\n");
        return true;
    }
    
    /**
     * Exports the modeler notes.
     * @param notes The modeler notes in XML format.
     * @throws Exception
     */
    private void exportModelerNotes(String notes) throws Exception {
        Assignment a = (Assignment) _tableModel.getValueAt(
            0, ProposalTableModel.Col.ASSIGNMENT.getColumnIndex());
        String pkgUrl = WorkflowUtil.getActualWorkValue(a, 
            WorkflowUtil.ACTUAL_WORK_FIELD.PACKAGE_URL);

        //MsgDialog.ok(this, notes); //DEBUG
        BiomedGTWiki wiki = new BiomedGTWiki(BiomedGTWiki.getHostUrl(pkgUrl));
        wiki.updateModelerNotes(pkgUrl, notes); //DEBUG
    }
    
    // Member Variable(s):
    private static Preferences _preferences = 
        Preferences.userNodeForPackage(ExportPackageDialog.class);
    private static final String SUPPRESS_MESSAGE_PACKAGE_NOT_EXPORTABLE = 
        "supress_message_package_not_exportable"; 
    private static boolean _suppressMessagePackageNotExportable = 
        _preferences.getBoolean(SUPPRESS_MESSAGE_PACKAGE_NOT_EXPORTABLE, false);
    private static final String SUPPRESS_MESSAGE_CAN_ARCHIVE_ROWS = 
        "supress_message_package_can_archive_rows"; 
    private static boolean _suppressMessageCanArchiveRows = 
        _preferences.getBoolean(SUPPRESS_MESSAGE_CAN_ARCHIVE_ROWS, false);
    
    /**
     * Sets the suppress message flag for package not exportable.
     * @param suppress if true, suppress the message.
     */
    public static void setSuppressMessagePackageNotExportable(boolean suppress) {
        _suppressMessagePackageNotExportable = suppress;
        _preferences.putBoolean(SUPPRESS_MESSAGE_PACKAGE_NOT_EXPORTABLE, 
            _suppressMessagePackageNotExportable);
    }
    
    /**
     * Returns true if suppress package not exportable message is turned on. 
     * @return true if suppress package not exportable message is turned on.
     */
    public static boolean isSuppressMessagePackageNotExportable() {
        return _suppressMessagePackageNotExportable;
    }

    /**
     * Sets the suppress message flag for can archive rows.
     * @param suppress if true, suppress the message.
     */
    public static void setSuppressMessageCanArchiveRows(boolean suppress) {
        _suppressMessageCanArchiveRows = suppress;
        _preferences.putBoolean(SUPPRESS_MESSAGE_CAN_ARCHIVE_ROWS, 
            _suppressMessageCanArchiveRows);
    }
    
    /**
     * Returns true if suppress can archive rows message is turned on. 
     * @return true if suppress can archive rows message is turned on.
     */
    public static boolean isSuppressMessageCanArchiveRows() {
        return _suppressMessageCanArchiveRows;
    }
}
