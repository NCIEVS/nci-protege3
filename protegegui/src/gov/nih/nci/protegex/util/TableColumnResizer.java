package gov.nih.nci.protegex.util;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * Allows the user to resize a specific column in a table.
 * @author David Yee
 */
public class TableColumnResizer extends MouseInputAdapter {
    // Resize cursor
    private static final Cursor RESIZE_CURSOR = 
        Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

    // Member variables:
    private int _mouseXOffset;
    private Cursor _cursor = RESIZE_CURSOR;
    private JTable _table;

    /**
     * Instantiates this class.
     * @param table The JTable.
     */
    public TableColumnResizer(JTable table) {
        this._table = table;
        table.addMouseListener(this);
        table.addMouseMotionListener(this);
    }

    /**
     * Returns true if column can be resized.
     * @param column The TableColumn.
     * @return true if column can be resized.
     */
    private boolean canResize(TableColumn column) {
        return column != null && _table.getTableHeader().getResizingAllowed()
                && column.getResizable();
    }

    /**
     * Returns the column index specific by the point.
     * @param point The point.
     * @return the column index specific by the point.
     */
    private TableColumn getResizingColumn(Point point) {
        return getResizingColumn(point, _table.columnAtPoint(point));
    }

    /**
     * Returns the column index specific by the point.
     * @param point The point.
     * @param column The table column.
     * @return the column index specific by the point.
     */
    private TableColumn getResizingColumn(Point point, int column) {
        if (column == -1)
            return null;

        int row = _table.rowAtPoint(point);
        if (row == -1)
            return null;

        Rectangle r = _table.getCellRect(row, column, true);
        r.grow(-3, 0);
        if (r.contains(point))
            return null;

        int midPoint = r.x + r.width / 2;
        int columnIndex;
        if (_table.getTableHeader().getComponentOrientation().isLeftToRight())
            columnIndex = (point.x < midPoint) ? column - 1 : column;
        else columnIndex = (point.x < midPoint) ? column : column - 1;

        if (columnIndex == -1)
            return null;

        return _table.getTableHeader().getColumnModel().getColumn(columnIndex);
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
        _table.getTableHeader().setDraggedColumn(null);
        _table.getTableHeader().setResizingColumn(null);
        _table.getTableHeader().setDraggedDistance(0);

        // First find which header cell was selected 
        Point p = e.getPoint();
        int index = _table.columnAtPoint(p);
        if (index == -1)
            return;

        // The last 3 pixels + 3 pixels of next column are for resizing 
        TableColumn resizingColumn = getResizingColumn(p, index);
        if (!canResize(resizingColumn))
            return;

        _table.getTableHeader().setResizingColumn(resizingColumn);
        if (_table.getTableHeader().getComponentOrientation().isLeftToRight())
            _mouseXOffset = p.x - resizingColumn.getWidth();
        else _mouseXOffset = p.x + resizingColumn.getWidth();
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * @param e The MouseEvent.
     */
    public void mouseMoved(MouseEvent e) {
        if (canResize(getResizingColumn(e.getPoint())) != 
            (_table.getCursor() == RESIZE_CURSOR)) {
            swapCursor();
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * @param e The MouseEvent.
     */
    public void mouseDragged(MouseEvent e) {
        JTableHeader header = _table.getTableHeader();
        TableColumn resizingColumn = header.getResizingColumn();
        if (resizingColumn == null)
            return;

        boolean headerLeftToRight =
            header.getComponentOrientation().isLeftToRight();
        int mouseX = e.getX();
        int oldWidth = resizingColumn.getWidth();
        int newWidth;
        if (headerLeftToRight)
            newWidth = mouseX - _mouseXOffset;
        else newWidth = _mouseXOffset - mouseX;
        resizingColumn.setWidth(newWidth);

        Container container;
        if ((_table.getTableHeader().getParent() == null) ||
            ((container = _table.getTableHeader().getParent().getParent()) == null) || 
            !(container instanceof JScrollPane))
            return;

        if (container.getComponentOrientation().isLeftToRight() || 
            headerLeftToRight)
            return;

        if (_table != null)
            return;

        JViewport viewport = ((JScrollPane) container)
                .getViewport();
        int viewportWidth = viewport.getWidth();
        int diff = newWidth - oldWidth;
        int newHeaderWidth = _table.getWidth() + diff;

        // Resizes the table
        Dimension tableSize = _table.getSize();
        tableSize.width += diff;
        _table.setSize(tableSize);

        // If this table is in AUTO_RESIZE_OFF mode and has a horizontal 
        //   scrollbar, we need to update the view's position.
        if ((newHeaderWidth >= viewportWidth)
                && (_table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)) {
            Point p = viewport.getViewPosition();
            p.x = Math.max(0, Math.min(newHeaderWidth
                    - viewportWidth, p.x + diff));
            viewport.setViewPosition(p);

            // Update the original X offset value.
            _mouseXOffset += diff;
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e The MouseEvent.
     */
    public void mouseReleased(MouseEvent e) {
        _table.getTableHeader().setResizingColumn(null);
        _table.getTableHeader().setDraggedColumn(null);
    }
}
