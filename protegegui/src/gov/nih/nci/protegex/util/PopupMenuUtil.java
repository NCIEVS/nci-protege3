package gov.nih.nci.protegex.util;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * Contains generic classes to help create and display popup menus.
 *
 * @author David Yee
 */
public class PopupMenuUtil {
    /**
     * Generic class to help create popup menus.
     */
    public static class PopupMenuHandler extends JPopupMenu
            implements ActionListener {
        // Serial Version UID
        private static final long serialVersionUID = 3343244355159229505L;

        /**
         * Adds a menu item to the popup menu.
         * @param label The label of the menu item.
         * @return The created menu item.
         */
        protected JMenuItem addMenuItem(String label) {
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(this);
            add(item);
            return item;
        }

        /**
         * Adds a checkbox menu item to the popup menu.
         * @param label The label of the menu item.
         * @param selected if true, selects this menu item.
         * @return The created menu item.
         */
        protected JCheckBoxMenuItem addCheckBoxMenuItem(String label,
                boolean selected) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
            item.setSelected(selected);
            item.addActionListener(this);
            add(item);
            return item;
        }

        /**
         * Performs the action specified by the event.
         * @param event The ActionEvent.
         */
        public void actionPerformed(ActionEvent event) {
        }
    }

    /**
     * Generic class to help display popup menus.
     */
    public static class PopupMenuAdapter extends MouseAdapter {
        /**
         * Handles mouse press events.
         * @param event The MouseEvent.
         */
        public void mousePressed(MouseEvent event) {
            showPopup(event);
        }

        /**
         * Handles mouse release events.
         * @param event The MouseEvent.
         */
        public void mouseReleased(MouseEvent event) {
            showPopup(event);
        }

        /**
         * Displays the popup menu.
         * @param event The MouseEvent.
         */
        private void showPopup(MouseEvent event) {
            if (! event.isPopupTrigger())
                return;
            
            highlightWithRightMouse(event);
            show(event);
        }
        
        /**
         * Highlights the current row when the user right-mouse on a
         * specific row.  Note: If a row or a group of rows were already
         * highlighted and the user right-mouse on top of them, then the
         * selected rows are not changed.  However, if the user right-mouse
         * on a non-selected row, the current row is selected instead.
         * @param event The MouseEvent.
         */
        private void highlightWithRightMouse(MouseEvent event) {
            Object source = event.getSource();
            if (event.getButton() == MouseEvent.BUTTON3 &&
                source instanceof JTable) {
                JTable table = (JTable) source;
                Point point = event.getPoint();
                int row = table.rowAtPoint(point);
                int col = table.columnAtPoint(point);

                if (row >= 0 && col >= 0 &&
                    ! UIUtil.contains(table.getSelectedRows(), row))
                    table.setRowSelectionInterval(row, row);
            }
        }

        /**
         * Shows the popup menu.  Note: The subclass should override this method.
         * @param event The MouseEvent.
         */
        protected void show(MouseEvent event) {
        }
    }
}
