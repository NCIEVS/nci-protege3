/**
 *
 */
package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.edit.DataHandler;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIReviewAction;
import gov.nih.nci.protegex.tree.TreeItem;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreeNode;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.TreeUtil;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowHelper;
import gov.nih.nci.protegex.util.WorkflowUtil;
import gov.nih.nci.protegex.workflow.Assignment;
import gov.nih.nci.protegex.workflow.NotesDialog;


import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class WorkflowPanel extends NCIDoublePanel {
    // Serial Version UID
    private static final long serialVersionUID = 2740886780492083985L;

    // Member variables:
    private JButton _notesButton;
    private JButton _rejectButton;
    private JButton _diffButton;
    private TreePanel _upperTreePanel;
    private TreePanel _lowerTreePanel;
    private JScrollPane _upperScrollPane;
    private JScrollPane _lowerScrollPane;
    private NCIReviewAction _upperReviewAction;
    private NCIReviewAction _lowerReviewAction;
    private Assignment _assignment;
    private JSplitPane splitPane = null;

    /**
     * Constructs this class.
     * @param tab The NCIEditTab.
     */
    public WorkflowPanel(NCIEditTab tab) {
        super(tab, TreePanel.PanelType.TYPE_WORKFLOW);
        init();
    }

    /**
     * Initializes the GUI components.
     */
    private void init() {
        setLayout(new BorderLayout());

        _upperScrollPane = new JScrollPane();
        _lowerScrollPane = new JScrollPane();

        initObjects();

        LabeledComponent upperLC = new LabeledComponent("Proposed Concept",
                _upperScrollPane);
        LabeledComponent lowerLC = new LabeledComponent("Existing Concept",
                _lowerScrollPane);

        _upperReviewAction = new NCIReviewAction(_tab);
        upperLC.addHeaderButton(_upperReviewAction);
        _lowerReviewAction = new NCIReviewAction(_tab);
        lowerLC.addHeaderButton(_lowerReviewAction);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperLC,
                lowerLC);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(280);

        add(splitPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the button panel.
     * @return The newly created button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.add(_saveButton = createButton("Save", false));
        panel.add(_clearButton = createButton("Clear", false));
        panel.add(_notesButton = createButton("Notes", false));
        panel.add(_rejectButton = createButton("Reject", false));
        panel.add(_diffButton = createButton("Diff", true));
        _diffButton.setVisible(false);
        return panel;
    }

    /**
     * Initializes the member variables.
     */
    private void initObjects() {
        _upperTreePanel = new TreePanel(_tab, null, _kb);
        setupTreePanel(_upperTreePanel, true, false, false);
        _upperTreePanel.enablePopUpWindow();
        _upperTreePanel.setDisplayWorkflowMenu(false);
        _upperScrollPane.setViewportView(_upperTreePanel);

        _lowerTreePanel = new TreePanel(_tab, null, _kb);
        setupTreePanel(_lowerTreePanel, false, true, false);
        _lowerTreePanel.enablePopUpWindow();
        _lowerTreePanel.setDisplayWorkflowMenu(false);
        _lowerScrollPane.setViewportView(_lowerTreePanel);
    }

    /**
     * Reset the member variables to their initial values.
     */
    public void reset() {
        super.reset();

        _upperTreePanel.setCanDrop(false);
        _upperTreePanel.setDroppedCls(null);
        _lowerTreePanel.setCanDrop(false);
        _lowerTreePanel.setDroppedCls(null);

        _tab.clearListenedToClses();
        _upperReviewAction.setCls(null);
        _lowerReviewAction.setCls(null);
    }

    /**
     * Saves the existing concept located in the lower panel.
     */
    protected void save() {
        String name = _lowerTreePanel.getLocalName();
        Cls cls = _wrapper.getCls(name);
        if (cls == null) {
            // This case occurs when the user "Accepts" the proposed concept
            // and the newly created "existing" concept had problems being
            // saved in the server (database).
            saveFromScratch();
            return;
        }

        

        try {
        	_tab.removeFromListenedToClses((OWLNamedClass) cls,this);
            DataHandler.Status status = _tab.getDataHandler().canSaveData(
                _lowerTreePanel, cls);
            if (status != DataHandler.Status.SUCCESSFUL) {
                if (status == DataHandler.Status.FAILURE)
                    _tab.showError(_lowerTreePanel.getDisplayName());
                
                return;
            }
            
            _kb.beginTransaction("Update existing concept "
                    + _lowerTreePanel.getLocalName(), cls.getName());

            status = _tab.saveConcept(_lowerTreePanel);
            if (status != DataHandler.Status.SUCCESSFUL) {
                if (status == DataHandler.Status.FAILURE)
                    _tab.showError(_lowerTreePanel.getDisplayName());
                ;
                _kb.rollbackTransaction();
                return;
            }

            MsgDialog.ok(this, "Existing Concept saved successful.");
            _kb.commitTransaction();

            if (cls == null)
                cls = _wrapper.getCls(_lowerTreePanel.getLocalName());
            _lowerTreePanel.setDroppedCls((OWLNamedClass) cls);
            showDiffs();
            _saveButton.setEnabled(false);
            _isDataModified = false;
            WorkflowUtil.updateModelersNotes(
                                             _assignment, "class saved", "Edited");
            WorkflowHelper.getInstance().storeAssignment(_assignment);
        } catch (Exception ex) {
            _kb.rollbackTransaction();
            OWLUI.handleError(_kb, ex);
        } finally {
        	_tab.addToListenedToClses((OWLNamedClass) cls, this);
        }
        
    }

    /**
     * Displays notes dialog for user to enter modeler's notes.
     */
    private void displayNotesDialog() {
        NotesDialog dialog = new NotesDialog(UIUtil.getFrame(this), _assignment, false);
        //dialog.setVisible(true);
    }

    /**
     * Displays the rejection dialog to allow the user to reject the
     * proposed concept.
     */
    private void rejectConcept() {
        if (_assignment.getCurrentStatus() == Assignment.Status.COMPLETED) {
            MsgDialog.ok(UIUtil.getFrame(this), "Can Not Reject",
                "Assignment " + _assignment.getIdentifier() + " is completed.");
            return;
        }

        NotesDialog dialog = new NotesDialog(
            UIUtil.getFrame(this), _assignment, true);
        //dialog.setVisible(true);
    }

    /**
     * Shows the differences between the concepts loaded in the upper and
     * lower panels.
     */
    private void showDiffs() {
        TreeItems upperState = _upperTreePanel.getCurrentState();
        TreeItems lowerState = _lowerTreePanel.getCurrentState();
        
        if (upperState == null || upperState.size() <= 0 ||
            lowerState == null || lowerState.size() <= 0)
            return;
        
        Vector<TreeItems> results = TreeItems.diffData(
            upperState, lowerState);

        TreeItems inserted = results.get(0);
        TreeItems deleted = results.get(1);
        adjustDiff(inserted, deleted);
        
        TreeUtil.setTreeDiffState(_upperTreePanel.getTree(), deleted,
                TreeNode.DiffState.Inserted);
        TreeUtil.setTreeDiffState(_lowerTreePanel.getTree(), inserted,
                TreeNode.DiffState.Deleted);
    }
    
    /**
     * If the root TreeItem for both the inserted and deleted TreeItems
     * contains the same preferred name, then delete them both from these 
     * lists.  This prevents these root TreeItem-s to show up as being
     * different because their getName method returns different values
     * (concept internal name) when comparing them with the TreeItem.equals
     * method.
     * @param inserted The inserted TreeItems from a diff.
     * @param deleted The deleted TreeItems from a diff.
     */
    private void adjustDiff(TreeItems inserted, TreeItems deleted) {
        if (inserted.size() <= 0 || deleted.size() <= 0)
            return;
        if (inserted.elementAt(0).getType() != TYPE_CONCEPT ||
            deleted.elementAt(0).getType() != TYPE_CONCEPT)
            return;
        
        String insertedPT = inserted.elementAt(0).getNameValue();
        String deletedPT = deleted.elementAt(0).getNameValue();
        if (insertedPT.equals(deletedPT) && insertedPT.length() > 0) {
            inserted.remove(0);
            deleted.remove(0);
        }
    }

    /**
     * Perform the action associated with a button
     * @param button The button.
     */
    protected void performAction(JButton button) {
        super.performAction(button);
        if (button == _notesButton)
            displayNotesDialog();
        else if (button == _rejectButton)
            rejectConcept();
        else if (button == _diffButton)
            showDiffs();
    }

    /**
     * Returns the specified scroll pane (either the upper or lower).
     * @param type The scroll pane's type.
     * @param subtype The scroll pane's subtype.
     * @return the specified scroll pane.
     */
    public JScrollPane getScrollPane(TreePanel.PanelType type, TreePanel.PanelType subtype) {
        if (type == TreePanel.PanelType.TYPE_WORKFLOW) {
            if (subtype == TreePanel.PanelType.TYPE_WORKFLOW_UPPER)
                return _upperScrollPane;
            if (subtype == TreePanel.PanelType.TYPE_WORKFLOW_LOWER)
                return _lowerScrollPane;
        }
        return null;
    }

    /**
     * Updates the concept class located in the specified TreePanel.
     */
    public void updateCls(Object treePanel, Cls cls) {
        if (treePanel == _upperTreePanel)
            _upperReviewAction.setCls((OWLNamedClass) cls);
        else if (treePanel == _lowerTreePanel)
            _lowerReviewAction.setCls((OWLNamedClass) cls);
    }

    /**
     * Loads the temporary concept class (via TreeItems) to the upper panel.
     * @param items The TreeItems.
     */
    public void loadUpperPanel(TreeItems items) {
        if (items.size() <= 0 ||
            items.elementAt(0).getType() != TYPE_CONCEPT)
            return;
        
        TreeItem item = items.remove(0);
        TreeNode root = new TreeNode(item, TYPE_NOT_SET);
        
        _upperTreePanel = new TreePanel(_tab, null, root, "0", items);
        _upperTreePanel.setDisplayWorkflowMenu(false);
        setupTreePanel(_upperTreePanel, true, false, false);
        _upperScrollPane.setViewportView(_upperTreePanel);
        _upperTreePanel.enablePopUpWindow();
        _upperTreePanel.enableAcceptAction(true);
    }

    /**
     * Loads the concept class (OWLNamedClass) to the lower panel.
     * @param cls The OWLNamedClass.
     */
    public void loadLowerPanel(OWLNamedClass cls) {
        _lowerTreePanel.setDroppedCls(cls);
        updateCls(_lowerTreePanel, cls);

        //Note: When the lowerPanel is loaded with a concept, we need to
        //  disable the upperPanel's Accept menu option.  The Accept option
        //  allows the modeler to create a new concept to be loaded in the
        //  the lowerPanel.
        boolean enableAcceptAction = cls == null;
        _upperTreePanel.enableAcceptAction(enableAcceptAction);
        showDiffs();
    }
    
    /**
     * Sets up this panel while a concept is loading.
     * @param treePanel The corresponding TreePanel.
     */
    public void setupWhileLoadingConcept(TreePanel treePanel) {
        if (_lowerTreePanel.getDroppedCls() == null)
            return;
        _clearButton.setEnabled(true);
    }
    
    /**
     * Sets up this panel while a concept is loading.
     * @param assignment The Assignment.
     */
    public void setupWhileLoadingAssignment(Assignment assignment) {
        _assignment = assignment;
        _notesButton.setEnabled(assignment != null);
        _rejectButton.setEnabled(!WorkflowUtil.isAdmin(WorkflowHelper.getInstance().getCurrentUser().getUserRole()));
        setupWhileLoadingConcept(null);
    }

    /**
     * Accepts the proposed concept (loaded in the upper panel) so that
     * an existing concept could be created and loaded in the lower panel.
     * Note: This method takes the proposed concept and clones it to create
     * an existing concept.
     */
    private void acceptProposedConcept() {
        TreeNode node = _upperTreePanel.getRootNode();
        TreeItem item = (TreeItem) node.getUserObject();
        String pt = item.getNameValue();
        String name = WorkflowUtil.getUniqueConceptName(_wrapper, pt);

        TreeItems items = _upperTreePanel.getCurrentState();
        OWLNamedClass newCls = items.createCls(name, pt);
        String newCode = _wrapper.getCode(newCls);
        
        WorkflowUtil.updateActualWorkValue(_assignment, 
            WorkflowUtil.ACTUAL_WORK_FIELD.NEW_CONCEPT_CODE, newCode);

        _lowerTreePanel.setDroppedCls(newCls);
        _upperTreePanel.enableAcceptAction(false);
        this.showDiffs();
    }

    /**
     * Saves the concept loaded in the lowerTreePanel.  This is a new
     * concept that has not been stored in the server.
     */
    private void saveFromScratch() {
        Cls fromCls = _wrapper.getCls(_upperTreePanel.getLocalName());
        Cls toCls = _wrapper.getCls(_lowerTreePanel.getLocalName());
        

        DataHandler.Status status = _tab.getDataHandler().canSaveData(
            _lowerTreePanel, toCls);
        if (status != DataHandler.Status.SUCCESSFUL) {
            if (status == DataHandler.Status.FAILURE)
                _tab.showError(_lowerTreePanel.getDisplayName());
           
            return;
        }
        
        _tab.removeFromListenedToClses((OWLNamedClass) fromCls, this);
        _tab.removeFromListenedToClses((OWLNamedClass) toCls, this);

        _kb.beginTransaction("Create a copy (clone) of "
                + _upperTreePanel.getLocalName(), fromCls.getName());
        
        status = _tab.saveConcept(_lowerTreePanel,
                _lowerTreePanel.getLocalName());
        if (status != DataHandler.Status.SUCCESSFUL) {
            if (status == DataHandler.Status.FAILURE)
                _tab.showError(_lowerTreePanel.getDisplayName());
            
            _kb.rollbackTransaction();
        } else {
            MsgDialog.ok(this, "Concepts saved successful.");
            _kb.commitTransaction();

            _upperTreePanel.setDroppedCls((OWLNamedClass) fromCls);
            if (toCls == null)
                toCls = _wrapper.getCls(_lowerTreePanel.getLocalName());
            _lowerTreePanel.setDroppedCls((OWLNamedClass) toCls);
        }
        
        _tab.addToListenedToClses((OWLNamedClass) fromCls, this);
        _tab.addToListenedToClses((OWLNamedClass) toCls, this);

        showDiffs();
        _upperTreePanel.enableAcceptAction(false);
        _saveButton.setEnabled(false);
        _isDataModified = false;
    }

    protected void treePanelChanged(String action) {
        if (action.equals(TreePanel.UPDATED)) {
            showDiffs();
        } else if (action.equals(TreePanel.ACCEPTED)) {
            acceptProposedConcept();
        }
        super.treePanelChanged(action);
    }
}
