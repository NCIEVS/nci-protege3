package gov.nih.nci.protegex.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import gov.nih.nci.protegex.util.PopupMenuUtil;
import gov.nih.nci.protegex.util.TableColumnResizer;
import gov.nih.nci.protegex.util.TableRowResizer;
import gov.nih.nci.protegex.util.TableUtil;
import gov.nih.nci.protegex.util.WorkflowUtil;
import gov.nih.nci.protegex.workflow.NotesTableModel.Col;

/**
 * This tables contains a list of modeler's notes.
 * 
 * @author David Yee
 */
public class NotesTable extends JTable {
    // Serial Version UID.
    private static final long serialVersionUID = 2353950295097129049L;
    
    //Member Variable(s):
    private NotesTableModel _model;
    private ArrayList<TableColumn> _initialColumns;

    /**
     * Instantiates this class.
     */
    public NotesTable() {
        initGUI();
    }

    /**
	 * Initializes the table's GUI components.
	 */
    private void initGUI() {
        setModel(_model = new NotesTableModel());
        _initialColumns = TableUtil.getTableColumns(this);
        setColumnWidths();
        configRenderers();
        getTableHeader().addMouseListener(new TableHeaderPopupMenuAdapter());
        new TableColumnResizer(this);
        new TableRowResizer(this);
        TableUtil.createToolTipDismissDelayListener(this);
    }
    
    /**
     * Returns the tooltip text from the mouse event.
     * @param event The MouseEvent.
     * @return the tooltip text from the mouse event.
     */
    public String getToolTipText(MouseEvent event) {
        return TableUtil.getToolTipInHTML(this, event);
    }
    
    /**
     * Sets up the column widths for the table.
     */
    private void setColumnWidths() {
        //setAutoResizeMode(AUTO_RESIZE_OFF);
        //TableUtil.setColumnWidth(this, Col.NOTE.getColumnIndex(), 300);
        //TableUtil.setColumnWidth(this, Col.EXTRA.getColumnIndex(), 200);
    }

    /**
     * Configures the cell renderers for the table.
     */
    private void configRenderers() {
        TableUtil.JTextAreaRenderer render = new TableUtil.JTextAreaRenderer();

        getColumn(Col.NOTE.getName()).setCellRenderer(render);
        getColumn(Col.EXTRA.getName()).setCellRenderer(render);
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
            int n = NotesTable.this._model.getColumnCount();
            for (int i = 0; i < n; ++i) {
                String columnName = NotesTable.this._model.getColumnName(i);

                // Do not display the following column names in menu.
                if (columnName.equals(Col.NOTE.getName()))
                    continue;

                addCheckBoxMenuItem(columnName, TableUtil.isColumnDisplayed(
                    NotesTable.this, columnName));
            }
            addSeparator();
            addMenuItem(CANCEL_MENU);
        }

        /**
         * Performs the action specified by the event.
         * @param event The ActionEvent.
         */
        public void actionPerformed(ActionEvent event) {
            Object obj = event.getSource();
            if (obj instanceof JCheckBoxMenuItem) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) obj;
                String text = item.getText();
                if (!item.isSelected()) {
                    getColumnModel().removeColumn(getColumn(text));
                } else {
                    TableColumn column = TableUtil.getColumn(_initialColumns,
                            text);
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
        private TableHeaderPopupMenuHandler _popupMenu =
            new TableHeaderPopupMenuHandler();

        protected void show(MouseEvent e) {
            _popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Loads the table with notes.
     * @param value The string containing the notes.
     */
    public void load(String value) {
        ArrayList<ArrayList<String>> values = WorkflowUtil.parseModelerNotes(value);
        ArrayList<String> notes = values.get(0);
        ArrayList<String> dates = values.get(1);
        ArrayList<String> editBys = values.get(2);
        for (int i=0; i<notes.size(); ++i) {
            String note = notes.get(i);
            String date = dates.get(i);
            String editBy = editBys.get(i);
            addRow(note, date + "\n" + editBy);
        }
    }
    
    /**
     * Adds the values to the row.
     * @param note The note.
     * @param extra The extra.
     * @return true if successful.
     */
    private boolean addRow(String note, String extra) {
        note = note.trim();
        extra = extra.trim();
        if (note.length() <= 0 && extra.length() <= 0)
            return false;

        _model.addRow(new Object[] { note, extra } );
        return true;
    }
}
