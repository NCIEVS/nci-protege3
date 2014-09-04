package gov.nih.nci.protegex.panel;

import static gov.nih.nci.protegex.tree.TreePanel.PanelType.TYPE_UNKNOWN;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.tree.TreePanel.PanelType;
import gov.nih.nci.protegex.util.DropClsListener;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This is an abstract base class for all NCI edit panels that contain two TreePanels.
 * 
 * @author David Yee
 */
public abstract class NCIDoublePanel extends JPanel implements DropClsListener, PanelDirty, ConceptChangedListener {
    // Serial Version UID
    private static final long serialVersionUID = 3838532116523940458L;
    protected static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
    protected static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    protected Logger _logger = Log.getLogger();

    public static enum DropType {
        GOOD, BAD, TRYAGAIN
    };

    // NCIEditTab Constants:
    protected static final String CODE = NCIEditTab.CODE;
    protected static final String FULL_SYN = NCIEditTab.ALTLABEL;
    protected static final String PREFERRED_NAME = NCIEditTab.PREFLABEL;

    // Member variables:
    protected NCIEditTab _tab;
    protected OWLModel _kb;
    protected OWLWrapper _wrapper;
    protected ArrayList<JButton> _buttons = new ArrayList<JButton>();
    protected JButton _saveButton = null;
    protected JButton _clearButton = null;
    protected boolean _isDataModified = false;
    protected PanelType _type = TYPE_UNKNOWN;
    protected PanelType _upperSubtype = TYPE_UNKNOWN;
    protected PanelType _lowerSubtype = TYPE_UNKNOWN;
    protected JScrollPane _lowerScrollPane;
    protected JScrollPane _upperScrollPane;
    protected TreePanel _upperTreePanel;
    protected TreePanel _lowerTreePanel;

    public void conceptChanged(OWLNamedClass cls, String msg) {
        // ProtegeUI.getModalDialogFactory().showMessageDialog(_kb, msg);
    }

    /**
     * Constructs this class.
     * 
     * @param tab The NCIEditTab.
     * @param type The panel type.
     */
    public NCIDoublePanel(NCIEditTab tab, PanelType typ) {
        super(false);
        _tab = tab;
        _kb = _tab.getOWLModel();
        _wrapper = _tab.getOWLWrapper();
        _type = typ;
        _upperSubtype = PanelType.valueOf(_type.name() + "_UPPER");
        _lowerSubtype = PanelType.valueOf(_type.name() + "_LOWER");
        _upperScrollPane = new JScrollPane();
        _lowerScrollPane = new JScrollPane();
    }

    /**
     * Sets up the treePanel.
     * 
     * @param treePanel The treePanel.
     */
    protected void setupTreePanel(TreePanel treePanel, boolean isUpper, boolean isEnabled, boolean canDrop) {
        treePanel.setType(_type);
        treePanel.setSubType(isUpper ? _upperSubtype : _lowerSubtype);
        treePanel.setDoublePanel(this);
        treePanel.setEnabled(isEnabled);
        treePanel.setCanDrop(canDrop);
        treePanel.setDropClsListener(this);
        treePanel.addUpdateActionListener(new TreePanelUpdateActionHandler());
    }

    /**
     * Sets the treePanel concept class.
     */
    public void updateCls(Object treePanel, Cls cls) {
    }

    /**
     * Creates a button with a specific label.
     * 
     * @param label The button label.
     * @param enable If true, enables this button.
     * @return The newly created button.
     */
    protected JButton createButton(String label, boolean enable) {
        JButton button = new JButton(label);
        button.addActionListener(new ButtonHandler());
        button.setEnabled(enable);
        _buttons.add(button);
        return button;
    }

    /**
     * Reset the member variables to their initial values.
     */
    public void reset() {
        enableButtons(false);
        _isDataModified = false;
        if (this._upperTreePanel != null) {
            this._upperTreePanel.reset();
        }
        if (this._lowerTreePanel != null) {
            this._lowerTreePanel.reset();
        }
    }

    /**
     * Enables or disables all the buttons.
     * 
     * @param enable if true enables all buttons else disables them.
     */
    private void enableButtons(boolean enable) {
        Iterator<JButton> iterator = _buttons.iterator();
        while (iterator.hasNext())
            iterator.next().setEnabled(false);
    }

    /**
     * Sets up this panel while a concept is loading.
     * 
     * @param treePanel The corresponding TreePanel.
     */
    public void setupWhileLoadingConcept(TreePanel treePanel) {
    }

    /**
     * This class handles performing the actions specific to button events.
     */
    private class ButtonHandler implements ActionListener {
        /**
         * Performs the action specified by the event.
         * 
         * @param event The ActionEvent.
         */
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            if (source instanceof JButton)
                performAction((JButton) source);
        }
    }

    /**
     * Perform the action associated with a button
     * 
     * @param button The button.
     */
    protected void performAction(JButton button) {
        if (button == _clearButton)
            clear();
        else if (button == _saveButton)
            save();

    }

    /**
     * Clears the concepts from the upper and lower panels.
     * 
     * @return true if concepts are cleared.
     */
    public boolean clear() {
        try {
            if (_isDataModified && _tab.checkNoSavedContinueMsg())
                return false;
            reset();
            return true;
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "Exception caught", ex);
            return false;
        }
    }

    /**
     * Saves the concepts loaded in the panels.
     */
    protected void save() {
    }

    /**
     * Returns the upper TreePanel.
     * 
     * @return the upper TreePanel.
     */
    public TreePanel getUpperTreePanel() {
        return _upperTreePanel;
    }

    /**
     * Returns the lower TreePanel.
     * 
     * @return the lower TreePanel.
     */
    public TreePanel getLowerTreePanel() {
        return _lowerTreePanel;
    }

    /**
     * Returns the total number of concepts loaded.
     * 
     * @return the total number of concepts loaded.
     */
    public int getNumClses() {
        return 0;
    }

    /**
     * Returns the specified scroll pane (either the upper or lower).
     * 
     * @param type The scroll pane's type.
     * @param subtype The scroll pane's subtype.
     * @return the specified scroll pane.
     */
    public JScrollPane getScrollPane(PanelType type, PanelType subtype) {
        return null;
    }

    /**
     * Populates the panel with concept class.
     * 
     * @param cls Concept class.
     * @param subtype Panel subtype.
     * @return true if populated.
     */
    public DropType populate(OWLNamedClass cls, PanelType subtype) {
        return DropType.BAD;
    }

    public boolean isDirty() {
        return (isEnabled() && isDataModified());
    }

    /**
     * Returns true if a concept in either panel is modified.
     * 
     * @return true if a concept in either panel is modified.
     */
    public boolean isDataModified() {
        return _isDataModified;
    }

    /**
     * Sets the flag that determines whether a concept has been modified.
     * 
     * @param value The modified value.
     */
    public void setDataModified(boolean value) {
        _isDataModified = value;
    }

    /**
     * This method is called when the treePanel has changed.
     * 
     * @param action The action that has taken place.
     */
    protected void treePanelChanged(String action) {
        if (action.equals(TreePanel.UPDATED)) {
            _isDataModified = true;
            _saveButton.setEnabled(true);
        }
    }

    public JScrollPane getLowerPanel() {
        return _lowerScrollPane;
    }

    /**
     * This class handles performing actions specific to TreePanel.
     */
    protected class TreePanelUpdateActionHandler implements ActionListener {
        /**
         * Performs the action specified by the event.
         * 
         * @param event The ActionEvent.
         */
        public void actionPerformed(ActionEvent event) {
            String action = event.getActionCommand();
            NCIDoublePanel.this.treePanelChanged(action);
        }
    }
}
