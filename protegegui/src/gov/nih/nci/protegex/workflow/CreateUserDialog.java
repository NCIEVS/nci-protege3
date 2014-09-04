package gov.nih.nci.protegex.workflow;

import edu.stanford.smi.protege.model.KnowledgeBase;
import gov.nih.nci.protegex.util.DialogHelper;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.OKApplyCancelDialog;
import gov.nih.nci.protegex.util.WorkflowUtil;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This dialog creates a workflow user with a specific role.
 *
 * @author David Yee
 */
public class CreateUserDialog extends OKApplyCancelDialog {
    // Serial Version UID
    private static final long serialVersionUID = -3539225298333560233L;
    private static DialogHelper _dialogHelper = new DialogHelper(250, 120);
    
    // Member variables:
    private KnowledgeBase _kb;
    private Workflow _workflow;
    private JTextField _userNameTF;
    private JComboBox _roleCB;
    private WorkflowUser _user;

    /**
     * Constructs this class.
     * @param frame The parent frame.
     * @param kb The KnowledgeBase.
     * @param workflow The Workflow.
     */
    public CreateUserDialog(JFrame frame, KnowledgeBase kb, Workflow workflow) {
        super(frame);
        setTitle("Create Workflow User");
        showApplyButton(true);
        _dialogHelper.init(this, true);
        _kb = kb;
        _workflow = workflow;
    }

    /**
     * Sets the size of this dialog.
     * @param dimension The size.
     */
    public void setSize(Dimension dimension) {
        super.setSize(_dialogHelper.getUpdatedSize(dimension));
    }
    
    /**
     * Sets the location of this dialog.
     * @param location The location.
     */
    public void setLocation(Point location) {
        super.setLocation(_dialogHelper.getUpdatedLocation(location));
    }
    
    /**
     * Cleans up this dialog.
     */
    public void dispose() {
        _dialogHelper.dispose();
        super.dispose();
    }

    /**
     * Creates the main panel that contains the GUI components for this dialog.
     */
    protected JComponent newMainComponent() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,5,0,5);

        c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
        add(panel, new JLabel("User: "), c);

        c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
        _userNameTF = (JTextField) add(panel, new JTextField(), c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.0;
        add(panel, new JLabel("Role: "), c);

        c.gridx = 1; c.gridy = 2; c.weightx = 1.0;
        _roleCB = (JComboBox) add(panel,
                new JComboBox(WorkflowUser.Type.values()), c);

        c.gridx = 1; c.gridy = 3;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        add(panel, new JLabel(), c);

        reset();
        return panel;
    }

    /**
     * Resets the values of the GUI components.
     */
    private void reset() {
        _userNameTF.setText("");
        _roleCB.setSelectedItem(WorkflowUser.Type.MODELER);
    }

    /**
     * Checks the various values to determine if there are any errors.
     * @return an error message if there is a problem else a blank string.
     */
    private String checkValues() {
        String userName = getUserName();
        if (userName.length() <= 0)
            return "Must enter a user name.";
        if (! WorkflowUtil.userMPExists(_kb, userName)) {
            return "User \"" + userName + "\" is not a valid Protege user.";
        }
        if (WorkflowUtil.userExists(_workflow, userName))
            return "Workflow user " + userName + " already exists.";
        return "";
    }

    /**
     * Displays an error dialog if there are any errors.
     * @return true if there are errors.
     */
    private boolean displayErrors() {
        String errors = checkValues();
        if (errors.length() <= 0)
            return false;

        MsgDialog.warning(this, "Workflow User Not Created", errors);
        return true;
    }

    /**
     * Creates a user with a specific role.
     * @param userName The name of the user.
     * @param role The assigned role.
     */
    private void createUser(String userName, WorkflowUser.Type role) {
        WorkflowUtil.modifyMPUser(_kb, userName, role);
        _user = WorkflowUtil.createUser(_workflow, userName, role);
        fireEvent(new ActionEvent(this, 0, APPLY));
    }

    /**
     * Creates a workflow user with a specific role.
     */
    protected boolean apply() {
        _user = null;
        if (displayErrors())
            return false;

        String userName = getUserName();
        createUser(userName, getRole());
        MsgDialog.ok(this, "Workflow User " + userName + " created.");
        reset();
        return true;
    }

    /**
     * Returns the created workflow user name.
     * @return the created workflow user name.
     */
    private String getUserName() {
        return _userNameTF.getText().trim();
    }

    /**
     * Returns the created workflow user's role.
     * @return the created workflow user's role.
     */
    private WorkflowUser.Type getRole() {
        return (WorkflowUser.Type) _roleCB.getSelectedItem();
    }

    /**
     * Returns the created workflow user.
     * @return the created workflow user.
     */
    public WorkflowUser getUser() {
        return _user;
    }
}
