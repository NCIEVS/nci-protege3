/**
 *
 */
package gov.nih.nci.protegex.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.widget.TabWidget;

/**
 * Contains utility methods for the user interface.
 *
 * @author David Yee
 */
public class UIUtil {
    /**
     * Makes all the components within a panel the same size.
     * @param panel The panel.
     */
    public static void componentsSameHeight(JPanel panel) {
        Component[] components = panel.getComponents();
        int maxH = 0;
        for (int i=0; i<components.length; ++i) {
            int h = components[i].getPreferredSize().height;
            maxH = maxH > h ? maxH : h;
        }
        for (int i=0; i<components.length; ++i) {
            int w = components[i].getPreferredSize().width;
            components[i].setPreferredSize(new Dimension(w, maxH));
        }
    }

    /**
     * Sorts a list.
     * @param list The list.
     * @return The sorted list.
     */
    public static ArrayList<Object> sort(ArrayList<Object> list) {
        Object[] objs = list.toArray();
        Arrays.sort(objs);
        return new ArrayList<Object>(Arrays.asList(objs));
    }

    /**
     * Returns the tabbed index for a the corresponding tab name. 
     * @param tabName The tab name.
     * @return the tabbed index for a the corresponding tab name.
     */
    public static int getTabbedIndex(String tabName) {
        ProjectManager manager = ProjectManager.getProjectManager();
        ProjectView view = manager.getCurrentProjectView();
        Collection list = view.getTabs();
        Iterator iterator = list.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            TabWidget tab = (TabWidget) iterator.next();
            String name = tab.getName();
            if (name.equals(tabName))
                return i;
            ++i;
        }
        return -1;
    }

    /**
     * Returns the JTabbedPane from the current project view. 
     * @return the JTabbedPane from the current project view.
     */
    public static JTabbedPane getTabbedPane() {
        ProjectManager manager = ProjectManager.getProjectManager();
        ProjectView view = manager.getCurrentProjectView();
        return view.getTabbedPane();
    }

    /**
     * Sets the tab title specified by the tab index.
     * @param index The tab index.
     * @param title The title.
     */
    public static void setTabTitleAt(int index, String title) {
        JTabbedPane pane = getTabbedPane();
        if (pane == null)
            return;
        pane.setTitleAt(index, title);
    }
    
    /**
     * Sets the tab title specified by the tab name.
     * @param tabName The tab name.
     * @param title The title.
     */
    public static void setTabTitle(String tabName, String title) {
        int i = getTabbedIndex(tabName);
        if (i < 0)
            return;
        setTabTitleAt(i, title);
    }
    
    public static String getTabTitle(String tabName) {
    	
    	int i = getTabbedIndex(tabName);
        if (i < 0) {
            return "none";
        } else {
        	JTabbedPane pane = getTabbedPane();
            if (pane == null)
                return "none";
            return pane.getTitleAt(i);
        }
    	
    }
    
    /**
     * Returns a protege main tab by its class name.
     * @param className The class name.
     * @return The specified tab widget.
     */
    public static TabWidget getTab(String className) {
        ProjectManager manager = ProjectManager.getProjectManager();
        ProjectView view = manager.getCurrentProjectView();
        return view.getTabByClassName(className);
    }

    /**
     * Selects and displays a specified protege main tab.
     * @param tab The tab.
     */
    public static void selectTab(TabWidget tab) {
        ProjectManager manager = ProjectManager.getProjectManager();
        ProjectView view = manager.getCurrentProjectView();
        if (tab != null)
            view.setSelectedTab(tab);
    }
    
    /**
     * Returns true if the specified tab has focus.
     * @param tabName The tab name.
     * @return true if the specified tab has focus.
     */
    public static boolean tabHasFocus(String tabName) {
        int desiredIndex = UIUtil.getTabbedIndex(tabName);
        int currIndex = UIUtil.getTabbedPane().getSelectedIndex();
        return currIndex == desiredIndex;
    }

    /**
     * Prints the list of active main tabs.
     * @param logger The logger.
     */
    public static void debugTabs(Logger logger) {
        ProjectManager manager = ProjectManager.getProjectManager();
        ProjectView view = manager.getCurrentProjectView();
        Collection list = view.getTabs();
        Iterator iterator = list.iterator();
        logger.log(Level.INFO, "List of tabs:");
        int i = 0;
        while (iterator.hasNext()) {
            TabWidget tab = (TabWidget) iterator.next();
            logger.log(Level.INFO, "  " + i + ": Name = " + tab.getName());
            logger.log(Level.INFO, "  " + i + ": Label = " + tab.getLabel());
            ++i;
        }
    }

    /**
     * Returns a string that contains a list of selected rows from a table.
     * @param table The table.
     * @return The string.
     */
    public static String getSelectedRowsString(JTable table) {
        StringBuffer buffer = new StringBuffer();
        int[] rows = table.getSelectedRows();
        for (int i=0; i<rows.length; ++i) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(rows[i]);
        }
        return buffer.toString();
    }

    /**
     * Adds items into JComboBox.
     * @param comboBox JComboBox.
     * @param removeAll If true, removes all items before adding new ones.
     * @param items The list of items.
     */
    public static void addItems(JComboBox comboBox, boolean removeAll,
            Object[] items) {
        if (removeAll)
            comboBox.removeAllItems();
        for (int i=0; i<items.length; ++i)
            comboBox.addItem(items[i]);
    }

    /**
     * Returns the frame for a container.
     * @param container The container.
     * @return The frame.
     */
    public static JFrame getFrame(Container container) {
        if (container == null)
            return null;
        if (container instanceof JFrame)
            return (JFrame) container;
        return getFrame(container.getParent());
    }
    
    /**
     * Centers a dialog with respect to its parent.
     * @param dialog The dialog.
     */
    public static void centerWithRespectToParent(JDialog dialog) {
        Container parent = dialog.getParent();
        Point topLeft = parent.getLocationOnScreen();
        Dimension parentSize = parent.getSize();
        Dimension dialogSize = dialog.getSize();

        int x = topLeft.x, y = topLeft.y;
        if (parentSize.width > dialogSize.width) 
            x = ((parentSize.width - dialogSize.width)/2) + topLeft.x;
        if (parentSize.height > dialogSize.height) 
            y = ((parentSize.height - dialogSize.height)/2) + topLeft.y;
        dialog.setLocation (x, y);
    }
    
    /**
     * Centers a dialog with respect to the screen.
     * @param dialog The dialog.
     */
    public static void center(JDialog dialog) {
        Dimension size = dialog.getToolkit().getScreenSize();
        Rectangle bounds = dialog.getBounds();
        dialog.setLocation((size.width - bounds.width) / 2,
            (size.height - bounds.height) / 2);
    }

    /**
     * Returns the date formatter for this project.
     * @return the date formatter for this project.
     */
    public static DateFormat getDateFormatter() {
        return new SimpleDateFormat("E MM/dd/yyyy HH:mm:ss z");
    }
    
    /**
     * Returns true if the array contains a specific value.
     * @param array The array.
     * @param value The value.
     * @return true if array contains a specific value.
     */
    public static boolean contains(int[] array, int value) {
        for (int i=0; i<array.length; ++i)
            if (array[i] == value)
                return true;
        return false;
    }
    
    /**
     * Converts a list of integers to an array of ints.
     * @param list The list of integers.
     * @return the array of ints.
     */
    public static int[] toInts(ArrayList<Integer> list) {
        int n = list.size();
        int[] array = new int[n];
        for (int i=0; i<n; ++i)
            array[i] = list.get(i).intValue();
        return array;
    }
}
