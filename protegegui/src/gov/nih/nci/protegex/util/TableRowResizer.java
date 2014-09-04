package gov.nih.nci.protegex.util;

import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * Allows the user to resize a specific row in a table.
 * @author David Yee
 */
public class TableRowResizer extends MouseInputAdapter {
    // Resize cursor
    private static final Cursor RESIZE_CURSOR = 
        Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);

    // Member variables:
    private int _mouseYOffset;
    private int _resizingRow;
    private Cursor _cursor = RESIZE_CURSOR;
    private JTable _table;

    /**
     * Instantiates this class.
     * @param table The JTable.
     */
    public TableRowResizer(JTable table) {
        _table = table;
        table.addMouseListener(this);
        table.addMouseMotionListener(this);
    }

    /**
     * Returns the row index specific by the point.
     * @param point The point.
     * @return the row index specific by the point.
     */
    private int getResizingRow(Point point) {
        return getResizingRow(point, _table.rowAtPoint(point));
    }

    /**
     * Returns the row index specific by the point.
     * @param point The point.
     * @param row The table row.
     * @return the row index specific by the point.
     */
    private int getResizingRow(Point point, int row) {
        if (row == -1)
            return -1;

        int col = _table.columnAtPoint(point);
        if (col == -1)
            return -1;

        Rectangle r = _table.getCellRect(row, col, true);
        r.grow(0, -3);
        if (r.contains(point))
            return -1;

        int midPoint = r.y + r.height / 2;
        int rowIndex = (point.y < midPoint) ? row - 1 : row;
        return rowIndex;
    }

    /**
     * Swaps the cursor.
     */
    private void swapCursor() {
        Cursor prevCursor = _table.getCursor();
        _table.setCursor(_cursor);
        _cursor = prevCursor;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e The MouseEvent.
     */
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        _resizingRow = getResizingRow(p);
        _mouseYOffset = p.y - _table.getRowHeight(_resizingRow);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * @param e The MouseEvent.
     */
    public void mouseMoved(MouseEvent e) {
        if ((getResizingRow(e.getPoint()) >= 0) != 
            (_table.getCursor() == RESIZE_CURSOR)) {
            swapCursor();
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * @param e The MouseEvent.
     */
    public void mouseDragged(MouseEvent e) {
        if (_resizingRow < 0)
            return;

        int mouseY = e.getY();
        int newHeight = mouseY - _mouseYOffset;
        if (newHeight > 0)
            _table.setRowHeight(_resizingRow, newHeight);
    }
}
