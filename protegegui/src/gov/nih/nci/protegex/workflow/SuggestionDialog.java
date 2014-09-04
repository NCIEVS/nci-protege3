package gov.nih.nci.protegex.workflow;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

import java.util.*;

import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.util.*;

/**
 * This dialog allows the user to create an assignment.
 * 
 * @author David Yee
 */
public class SuggestionDialog {
    // Serial Version UID
    // private static final long serialVersionUID = -3539225298333506323L;
    // private static DialogHelper _dialogHelper = new DialogHelper(500, 275);

    // Member variables:
    private JTextField tf;

    private String tdesc = null;

    private JTextArea textArea;    

    private JFrame fr = null;

    private ArrayList<String> codes = null;
    private ArrayList<String> names = null;

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
    public SuggestionDialog(String taskDescription, ArrayList<String> ccodes, ArrayList<String> cnames) {

        codes = ccodes;
        names = cnames;
        tdesc = taskDescription;
        init();

    }

    /**
     * Creates the main panel that contains the GUI components for this dialog.
     */
    private void init() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setLocation(450, 300);

        tf = new JTextField();
        tf.setPreferredSize(new Dimension(300, 20));
        tf.setText(tdesc);
        tf.setEditable(false);
        LabeledComponent ltf = new LabeledComponent("Task Description:", tf);

        JComboBox ucb = null;
        LabeledComponent las = null;

        if (WorkflowHelper.getInstance().isCurrentUserAdmin()) {
            // user is admin, add assignee combobox
            String[] names = WorkflowUtil
                                         .getUsers(
                                                   WorkflowHelper.getInstance()
                                                                 .getAllUsers(),
                                                   true);
            ucb = new JComboBox();
            UIUtil.addItems(ucb, false, names);

            las = new LabeledComponent("Assignee:", ucb);

        }

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(true);
        JScrollPane notes = new JScrollPane(textArea);
        LabeledComponent lcn = new LabeledComponent("Notes:", notes);
        notes.setPreferredSize(new Dimension(400, 200));

        panel.add(ltf, BorderLayout.NORTH);
        if (ucb != null) {
            panel.add(las, BorderLayout.CENTER);
            panel.add(lcn, BorderLayout.SOUTH);
        } else {
            panel.add(lcn, BorderLayout.CENTER);
        }

        int r = ProtegeUI.getModalDialogFactory()
                         .showDialog(fr, panel, "Assignment",
                                     ModalDialogFactory.MODE_OK_CANCEL, textArea);
        if (r == ModalDialogFactory.OPTION_OK) {
        	if (ucb != null) {
        		apply(ucb.getSelectedItem());
        		
        	} else {
        		apply(NCIEditTab.getUserName());
        		
        	}
            
        }

    }

    /**
     * Creates an assignment.
     */
    private boolean apply(Object selectedUser) {
    	String user = WorkflowUtil.removeMarker((String) selectedUser);
        String note = textArea.getText().trim();
        if (note.length() <= 0) {
            MsgDialog.warning(fr, "Assignment Not Created",
                              "You must add a note to create an assignment.");
            return false;
        }
        
        for (int i = 0; i < codes.size(); i++) {
            String cname = names.get(i);
            String code = codes.get(i);
            String td = cname + " (" + code + ") needs updating.";
            WorkflowHelper.getInstance()
            .createDefaultTaskAssignment(user, code,
                                         td,
                                         note);
            
        }

       String msg = "Created assignment.";
       if (codes.size() > 1) {
           msg = "Created " + codes.size() + " assignments.";
       }
           
        MsgDialog.ok(fr, msg);
        return true;
    }
}
