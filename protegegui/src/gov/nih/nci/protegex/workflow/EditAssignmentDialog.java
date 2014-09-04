package gov.nih.nci.protegex.workflow;

//import gov.nih.nci.protegex.util.DialogHelper;
import gov.nih.nci.protegex.util.OKApplyCancelDialog;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowHelper;
import gov.nih.nci.protegex.util.WorkflowUtil;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.*;

import java.awt.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;

/**
 * This dialog allows the user to edit and update the fields in an assignment.
 * 
 * @author David Yee
 */
public class EditAssignmentDialog {
    // Serial Version UID
    private static final long serialVersionUID = -3539225298333560233L;

    // private static DialogHelper _dialogHelper = new DialogHelper(625, 475);

    // Member variables:
    private Workflow _workflow;

    private Assignment _assignment;

    private JLabel _managerL;

    private JComboBox _modelerCB;

    private JLabel _statusL;

    private JLabel _taskDescriptionL;

    private NotesTable _notesTable;

    private JTextArea _newNotesTA;

    private boolean _isAdmin = false;

    private JComponent gui = null;

    /**
     * Constructs this class.
     * 
     * @param frame
     *            The parent frame.
     * @param workflow
     *            The Workflow.
     * @param assignment
     *            The assignment.
     */
    public EditAssignmentDialog(JFrame frame, Workflow workflow,
                                 Assignment assignment) {

        _workflow = workflow;
        _assignment = assignment;

        // setTitle("Assignment " + _assignment.getIdentifier());
        // _dialogHelper.init(this, true);

        gui = initGui();
        init();
        
        setInitialValues();
        setEditable();
        int r = ProtegeUI.getModalDialogFactory()
                         .showDialog(frame, gui, "Assignment",
                                     ModalDialogFactory.MODE_OK_CANCEL,
                                     _newNotesTA);
        if (r == ModalDialogFactory.OPTION_OK) {
            this.updateAssignment();
        }
    }

    /**
     * Initializes GUI components.
     */
    private void init() {
        try {
            _isAdmin = WorkflowUtil.isAdmin(_workflow.getUser().getUserRole());
            if (_isAdmin) {
                ArrayList<WorkflowUser> users = _workflow.getAllUsers();
                UIUtil.addItems(_modelerCB, true, WorkflowUtil.getUsers(users,
                                                                        false));
            } else {
                _modelerCB.addItem(_workflow.getUser().getUserName());
            }
        } catch (WorkflowException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the main panel that contains the GUI components for this dialog.
     */
    protected JComponent initGui() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JPanel topbot = new JPanel(new BorderLayout());

        _managerL = new JLabel();
        LabeledComponent lc1 = new LabeledComponent("Manager ", _managerL);

        _modelerCB = new JComboBox();
        LabeledComponent lc2 = new LabeledComponent("Modeler ", _modelerCB);

        _statusL = new JLabel();
        LabeledComponent lc3 = new LabeledComponent("Status ", _statusL);
        
        _taskDescriptionL = new JLabel();
        LabeledComponent lc4 = new LabeledComponent("Task Description ",
                                                    _taskDescriptionL);
        
        topbot.add(lc3, BorderLayout.NORTH);
        topbot.add(lc4, BorderLayout.CENTER);
        

        top.add(lc1, BorderLayout.NORTH);
        top.add(lc2, BorderLayout.CENTER);
        top.add(topbot, BorderLayout.SOUTH);

        

        NotesTable table = new NotesTable();
        //table.setMinimumSize(new Dimension(200,100));
        JScrollPane np = new JScrollPane(table);
        

        LabeledComponent lc5 = new LabeledComponent("Log/Activity", np);
        lc5.setPreferredSize(new Dimension(450,250));

        _notesTable = table;

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(true);
        JScrollPane ta = new JScrollPane(textArea);
        

        LabeledComponent lc6 = new LabeledComponent("Notes",
                                                    ta);
        lc6.setPreferredSize(new Dimension(450,150));
        

        _newNotesTA = textArea;
        
        //JPanel mid = new JPanel(new BorderLayout());
        //mid.add(lc5, BorderLayout.NORTH);
        //mid.add(lc6, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(lc5, BorderLayout.CENTER);
        panel.add(lc6, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Sets the initial values to the GUI components.
     */
    private void setInitialValues() {
        _statusL.setText(_assignment.getCurrentStatus().toString());
        _managerL.setText(_assignment.getManager());
        _modelerCB.setSelectedItem(_assignment.getModeler());
        _taskDescriptionL.setText(_assignment.getTaskDescription());
        _notesTable.load(_assignment.getModelerNote());
    }

    /**
     * Sets certain GUI components editable.
     */
    private void setEditable() {
        _modelerCB
                  .setEnabled(AssignmentTableModel
                                                  .isModelerFieldEditable(
                                                                          _assignment,
                                                                          _isAdmin));
    }

    /**
     * Updates the assignment with the values specified in the GUI components.
     */
    private void updateAssignment() {
        String prevModeler = _assignment.getModeler();
        String currModeler = (String) _modelerCB.getSelectedItem();

        _assignment.setModeler(currModeler);
        _assignment.setTaskDescription(_taskDescriptionL.getText().trim());
        WorkflowUtil.updateModelersNotes(_assignment, _newNotesTA.getText(),
                                         "Edited");

        if (!currModeler.equals(prevModeler))
            _assignment.setStatus(Assignment.Status.ASSIGNED);
        WorkflowHelper.getInstance().storeAssignment(_assignment);
    }

}
