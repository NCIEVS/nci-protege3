package gov.nih.nci.protegex.workflow;

import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.util.WorkflowHelper;
import gov.nih.nci.protegex.util.WorkflowUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This dialog allows the user to prepend a note to the assignment's modeler
 * notes field.
 * 
 * @author David Yee
 */
public class NotesDialog {
    // Serial Version UID
    private static final long serialVersionUID = -6285914043822916321L;

    // Member variables:
    protected DateFormat _formatter = UIUtil.getDateFormatter();

    protected NotesTable _notesTable;

    protected JTextArea _newNotesTA;

    protected Assignment _assignment;

    protected String _action = "Edited";
    
    private boolean rejected = false;

    private JComponent gui = null;
    
    private JFrame parent = null;

    /**
     * Constructs this class.
     * 
     * @param frame
     *            The parent frame.
     * @param assignment
     *            The assignment.
     * @param notes
     *            The default note.
     */
    public NotesDialog(JFrame frame, Assignment assignment, String notes, boolean reject) {

        _assignment = assignment;
        
        parent = frame;
        
        rejected = reject;
        
        if (reject) {
            _action = "Rejected";
        }

        gui = buildGui();

        setInitialValues(notes);

        int r = ProtegeUI.getModalDialogFactory()
                         .showDialog(frame, gui, "Edit Note",
                                     ModalDialogFactory.MODE_OK_CANCEL,
                                     _newNotesTA);
        if (r == ModalDialogFactory.OPTION_OK) {
            this.apply();
        }
    }

    /**
     * Constructs this class.
     * 
     * @param frame
     *            The parent frame.
     * @param assignment
     *            The assignment.
     */
    public NotesDialog(JFrame frame, Assignment assignment, boolean reject) {
        this(frame, assignment, "", reject);
    }

    /**
     * Creates the main panel that contains the GUI components for this dialog.
     */
    protected JComponent buildGui() {
        JPanel panel = new JPanel(new BorderLayout());

        NotesTable table = new NotesTable();
        JScrollPane pane = new JScrollPane(table);
        LabeledComponent lc1 = new LabeledComponent("Log/Activity", pane);
        lc1.setPreferredSize(new Dimension(450, 250));

        _notesTable = table;

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(true);
        pane = new JScrollPane(textArea);
        LabeledComponent lc2 = new LabeledComponent("Notes", pane);
        lc2.setPreferredSize(new Dimension(450, 150));

        _newNotesTA = textArea;

        panel.add(lc1, BorderLayout.NORTH);
        panel.add(lc2, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Sets the initial values to the GUI components.
     */
    private void setInitialValues(String notes) {
        _notesTable.load(_assignment.getModelerNote());
        _newNotesTA.setText(notes.trim());
    }
    
    
    private void notRejectMessage() {
        ProtegeUI.getModalDialogFactory().showErrorMessageDialog(parent, "Note Rejected",
            "You must add a note explaining why this\n" +
            "proposed concept was rejected in order to\n" +
            "officially reject it.");
    }

    


    /**
     * Prepends a note to the assignment's modeler notes field.
     */
    protected void apply() {
        
        if (rejected) {
            String value = _newNotesTA.getText().trim();
            if (value.length() <= 0) {
                notRejectMessage();
                return;
            }

            _assignment.setStatus(Assignment.Status.REJECTED);
            
        }
        
        WorkflowUtil.updateModelersNotes(_assignment, _newNotesTA.getText(),
                                         _action);
        WorkflowHelper.getInstance().storeAssignment(_assignment);
    }
}