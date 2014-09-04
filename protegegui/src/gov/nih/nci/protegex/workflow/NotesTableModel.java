package gov.nih.nci.protegex.workflow;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

/**
 * This tables model contains the notes found in the assignment.
 *
 * @author David Yee
 */
public class NotesTableModel extends DefaultTableModel {
    // Serial Version UID.
    private static final long serialVersionUID = 6048091477521533993L;

    public enum Col {
        NOTE("Note", 0, 20), EXTRA("Date/Author", 1, 40);
    
        // Name member variable and methods
        private String _name = null;
        public String getName() { return _name; }

        // Width member variable and methods
        private int _width = -1;
        public int getWidth() { return _width; }
        public void setWidth(int i) { if (i > 0) { _width = i; } }

        // Column index member variable and methods
        private int _columnIndex = -1;
        public int getColumnIndex() {return _columnIndex;}      

        // IsVisible member variable and methods
        private boolean _isVisible = true;
        public boolean isVisible() { return _isVisible; }
        public void setVisible(boolean visible) { _isVisible = visible; }
        
        /**
         * Returns the column by the column name.
         * @param name The column name.
         * @return the column by the column name.
         */
        public static Col findCol(String name) {
            for (Col col : values())
                if (col.getName().equalsIgnoreCase(name))
                    return col;
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
            // Extra.setVisible(false);
        }    
    }

    /**
     * Constructs this class.
     * @param workflow The Workflow.
     */
    public NotesTableModel() {
        super(new Object[0][], getColumnNames());
    }

    /**
     * Returns the list of column names.
     * @return the list of column names.
     */
    private static String[] getColumnNames() {
        ArrayList<String> list = new ArrayList<String>();
        for (Col col : Col.values())
            list.add(col.getName());
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns true if the cell located at (row, column) is editable.
     * @param row The row location.
     * @param column The column location.
     * @return true if the cell located at (row, column) is editable.
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
