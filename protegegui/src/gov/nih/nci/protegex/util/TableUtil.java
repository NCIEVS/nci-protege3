package gov.nih.nci.protegex.util;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Contains utility methods for the JTable GUI component.
 *
 * @author David Yee
 */
public class TableUtil {
    public static final int WRAP_MAX_CHAR = 150;
    
    /**
     * Returns the table column using the column name.
     *
     * @param table The table.
     * @param columnName The column name.
     * @return The table column.
     */
    public static TableColumn getColumn(JTable table, String columnName) {
        int i = table.getColumnModel().getColumnIndex(columnName);
        return table.getColumnModel().getColumn(i);
    }

    /**
     * Converts TableModelEvent type from int to a string.
     *
     * @param event TableModelEvent.
     * @return The string representing event.
     */
    public static String convertTableModelEventString(int event) {
        switch (event) {
        case TableModelEvent.UPDATE:
            return "Update";
        case TableModelEvent.INSERT:
            return "Insert";
        case TableModelEvent.DELETE:
            return "Delete;";
        default:
            return "Unknown";
        }
    }

    /**
     * Generic JComboBox cell editor.
     */
    public static class JComboBoxCellEditor extends AbstractCellEditor
            implements TableCellEditor, ItemListener {
        // Serial Version UID
        private static final long serialVersionUID = 3843885311784101550L;

        //Member variables:
        protected JComboBox _comboBox;

        /**
         * Constructs this class.
         * @param items The items for the comboBox.
         */
        public JComboBoxCellEditor(Object[] items) {
            _comboBox = new JComboBox(new DefaultComboBoxModel(items));
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
     * Generic label renderer.
     */
    public static class JLabelRenderer extends DefaultTableCellRenderer {
        // Serial Version UID
        private static final long serialVersionUID = -6130673113787369006L;

        // Member variables:
        protected boolean _showToolTip = false;
        protected boolean _wrapToolTip = false;

        /**
         * Constructs this class.
         * @param alignment The text alignment.
         * @param showToolTip if true, shows tooltips.
         */
        public JLabelRenderer(int alignment, boolean showToolTip) {
            setHorizontalAlignment(alignment);
            _showToolTip = showToolTip;
        }
        
        /**
         * Shows or hides tooltip.
         * @param show true if show.
         */
        public void setShowToolTip(boolean show) {
        	_showToolTip = show;
        }
        
        /**
         * Returns true if tooltips are displayed.
         * @return true if tooltips are displayed.
         */
        public boolean isShowToolTip() {
            return _showToolTip;
        }
        
        /**
         * Wraps or unwraps tooltip.
         * @param wrap true if wrap.
         */
        public void setWrapToolTip(boolean wrap) {
        	_wrapToolTip = wrap;
        }
        
        /**
         * Returns true if tooltips are wrapped.
         * @return true if tooltips are wrapped.
         */
        public boolean isWrapToolTip() {
            return _wrapToolTip;
        }
        
        /**
         * Sets the tooltip.
         * @param value The tooltip value.
         */
        public void setToolTipText(String value) {
        	if (!_showToolTip || value == null || value.trim().length() <= 0) {
                super.setToolTipText(null);
                return;
            }
            String text = value.trim();
            text = StringUtil.wrap(WRAP_MAX_CHAR, text);
            if (_wrapToolTip)
                text = StringUtil.convertToHTML(text);
            if (text.length() <= 0)
                super.setToolTipText(null);
            else super.setToolTipText(text);
        }

        /**
         * Returns the cell renderer component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param hasFocus Has focus flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            setToolTipText(value.toString());
            return super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, col);
        }
    }

    /**
     * Left justified label renderer.
     */
    public static class JLabelLeftRenderer extends JLabelRenderer {
        // Serial Version UID
        private static final long serialVersionUID = 1172803873204697034L;

        /**
         * Constructs this class.
         * @param showToolTip if true, shows tooltips.
         */
        public JLabelLeftRenderer(boolean showToolTip) {
            super(JLabel.LEFT, showToolTip);
        }
    }
    
    /**
     * Center justified label renderer.
     */
    public static class JLabelCenterRenderer extends JLabelRenderer {
        // Serial Version UID
        private static final long serialVersionUID = 1172803873204697034L;

        /**
         * Constructs this class.
         * @param showToolTip if true, shows tooltips.
         */
        public JLabelCenterRenderer(boolean showToolTip) {
            super(JLabel.CENTER, showToolTip);
        }
    }

    /**
     * Right justified label renderer.
     */
    public static class JLabelRightRenderer extends JLabelRenderer {
        // Serial Version UID
        private static final long serialVersionUID = 6298227367917626075L;

        /**
         * Constructs this class.
         * @param showToolTip if true, shows tooltips.
         */
        public JLabelRightRenderer(boolean showToolTip) {
            super(JLabel.RIGHT, showToolTip);
        }
    }

    /**
     * Left justified label renderer.
     */
    public static class NotesRenderer extends JLabelRenderer {
        // Serial Version UID
        private static final long serialVersionUID = 1172803873204697034L;

        /**
         * Constructs this class.
         * @param showToolTip if true, shows tooltips.
         */
        public NotesRenderer(boolean showToolTip) {
            super(JLabel.LEFT, showToolTip);
        }

        /**
         * Returns the cell renderer component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param hasFocus Has focus flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            
            String text = WorkflowUtil.getLatestNote(value.toString());
            setToolTipText(text);
            return super.getTableCellRendererComponent(table, text,
                    isSelected, hasFocus, row, col);
        }
    }
    
    /**
     * Left justified label renderer for the last token.
     */
    public static class LastTokenRenderer extends JLabelRenderer {
        // Serial Version UID
        private static final long serialVersionUID = 5216073164908478376L;

        private String _delimiter = "/";
        
        /**
         * Constructs this class.
         * @param delimiter The delimiter.
         * @param showToolTip if true, shows tooltips.
         */
        public LastTokenRenderer(String delimiter, boolean showToolTip) {
            super(JLabel.LEFT, showToolTip);
            _delimiter = delimiter; 
        }

        /**
         * Returns the cell renderer component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param hasFocus Has focus flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            
            String text = (String) value;
            text = StringUtil.getLastToken(text.trim(), _delimiter);
            setToolTipText(text);
            return super.getTableCellRendererComponent(table, text,
                    isSelected, hasFocus, row, col);
        }
    }

    /**
     * Date Renderer.
     */
    public static class DateRenderer extends JLabelCenterRenderer {
        // Serial Version UID
        private static final long serialVersionUID = 2110067732971350483L;

        // Member variables:
        private DateFormat _formatter = UIUtil.getDateFormatter();

        /**
         * Constructs this class.
         * @param showToolTip if true, shows tooltips.
         */
        public DateRenderer(boolean showToolTip) {
            super(showToolTip);
        }

        /**
         * Returns the cell renderer component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param hasFocus Has focus flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            value = _formatter.format(value);
            return super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, col);
        }
    }

    /**
     * Generic TextArea renderer.
     */
    public static class JTextAreaRenderer extends JTextArea
        implements TableCellRenderer {
        private static final long serialVersionUID = 7531491527315576877L;

        /**
         * Constructs this class.
         */
        public JTextAreaRenderer()
        {
            setLineWrap(true);
        }
        
        /**
         * Returns the cell renderer component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param hasFocus Has focus flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {

            String text = (String) value;
            if (text == null)
                text = "";
            setText(text.trim());

            FontMetrics fm = getFontMetrics(getFont());
            int lines = getLineCount();
            int heightOfMessage = lines * fm.getHeight() + 4;
            
            int rowHeight = table.getRowHeight(row);
            if (heightOfMessage > rowHeight)
                table.setRowHeight(row, heightOfMessage);
            return this;
        }
    }

    /**
     * Generic boolean checkbox renderer.
     */
    public static class BooleanCheckBoxRenderer extends JCheckBox 
        implements TableCellRenderer
    {
        // Serial Version UID
        private static final long serialVersionUID = 2084942166873757864L;
        
        // Member variables:
        protected boolean _showToolTip = false;

        /**
         * Constructs this class.
         * @param showToolTip if true, shows tooltips.
         */
        public BooleanCheckBoxRenderer(boolean showToolTip) {
            setHorizontalAlignment(SwingConstants.CENTER);
            _showToolTip = showToolTip;
        }
        
        /**
         * Shows or hides tooltip.
         * @param show true if show.
         */
        public void setShowToolTip(boolean show) {
            _showToolTip = show;
        }
        
        /**
         * Returns true if tooltips are displayed.
         * @return true if tooltips are displayed.
         */
        public boolean isShowToolTip() {
            return _showToolTip;
        }

        /**
         * Returns the cell renderer component.
         * @param table The table.
         * @param value The cell value.
         * @param isSelected Is selected flag.
         * @param hasFocus Has focus flag.
         * @param row The cell's row.
         * @param col The cell's column.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            Boolean val = (Boolean) value;
            setSelected(val.booleanValue());
            setToolTipText(val.toString());
            return this;
        }
    }
    
    /**
     * Generic boolean checkbox editor.
     */
    public static class BooleanCheckBoxCellEditor extends AbstractCellEditor
            implements TableCellEditor, ItemListener {
        // Serial Version UID
        private static final long serialVersionUID = 8558945406457670322L;
        
        //Member variables:
        protected JCheckBox _checkBox = new JCheckBox();

        /**
         * Constructs this class.
         * @param items The items for the comboBox.
         */
        public BooleanCheckBoxCellEditor() {
            _checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            _checkBox.addItemListener(this);
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
            Boolean val = (Boolean) value;
            _checkBox.setForeground(table.getSelectionForeground());
            _checkBox.setBackground(table.getSelectionBackground());
            _checkBox.setSelected(val.booleanValue());
            return _checkBox;
        }

        /**
         * Returns the cell value.
         * @return the cell value.
         */
        public Object getCellEditorValue() {
            return _checkBox.isSelected() ? Boolean.TRUE : Boolean.FALSE;
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
     * Returns true if column with columnName is displayed within the table.
     *
     * @param table The table.
     * @param columnName The columnName.
     * @return true if column with columnName is displayed within the table.
     */
    public static boolean isColumnDisplayed(JTable table, String columnName) {
        ArrayList<TableColumn> cols = getTableColumns(table);
        for (TableColumn tc : cols) {
            if (((String) tc.getHeaderValue()).equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns The array list of table columns.
     *
     * @param table The table.
     * @return The array list of table columns.
     */
    public static ArrayList<TableColumn> getTableColumns(JTable table) {
        ArrayList<TableColumn> list = new ArrayList<TableColumn>();
        TableColumnModel model = table.getColumnModel();
        Enumeration<TableColumn> enumeration = model.getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn column = enumeration.nextElement();
            list.add(column);
            
        }
        return list;
    }

    /**
     * Returns the table column from the list of table columns.
     *
     * @param list The list of TableColumns.
     * @param columnName The column name.
     * @return the table column from the list of table columns.
     */
    public static TableColumn getColumn(ArrayList<TableColumn> list,
            String columnName) {
        Iterator<TableColumn> iterator = list.iterator();
        while (iterator.hasNext()) {
            TableColumn column = iterator.next();
            if (column.getHeaderValue().equals(columnName))
                return column;
        }
        return null;
    }
    
    /**
     * Sets the column width for a specific column.
     * @param table The table.
     * @param column The column.
     * @param width The width.
     */
    public static void setColumnWidth(JTable table, int column, int width) {
        table.getColumnModel().getColumn(column).setPreferredWidth(width);
    }
    
    /**
     * Creates a listener to restore the dismiss delay value for the
     *   the table tooltip.
     * @param table The table.
     */
    public static void createToolTipDismissDelayListener(JTable table) {
        final int oldDelay = ToolTipManager.sharedInstance().getDismissDelay();
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setDismissDelay(oldDelay);
            }
        });
        table.setToolTipText(""); // Dummy to initialize the mechanism
    }
    
    /** Tooltip infinite time value */
    public static final int INFINITE_TIME = 0x7fffffff;
    
    /**
     * Returns the tooltip value in HTML format.
     * @param table The table.
     * @param event The MouseEvent.
     * @return the tooltip value in HTML format.
     */
    public static String getToolTipInHTML(JTable table, MouseEvent event) {
		ToolTipManager.sharedInstance().setDismissDelay(INFINITE_TIME);

		TableModel model = table.getModel();
		int rowCount = model.getRowCount();
		int colCount = model.getColumnCount();
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < colCount; c++) {
				Rectangle rect = table.getCellRect(r, c, false);
				if (rect.contains(event.getPoint())) {
					String value = model.getValueAt(r, c).toString();
                    if (value.length() <= 0)
                        return null;
                    value = StringUtil.wrap(WRAP_MAX_CHAR, value);
					return StringUtil.convertToHTML(value);
				}
			}
		}
		return null;
	}
    
    /**
     * Returns the column index for a specific column within the table.
     * @param table The JTable.
     * @param columnName The column name.
     * @return the column index for a specific column.
     */
    public static int getColumnIndex(JTable table, String columnName) {
        for (int i=0; i<table.getColumnCount(); ++i)
            if (table.getColumnName(i).equals(columnName))
                return i;
        return -1;
    }
}
