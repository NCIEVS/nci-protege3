package com.clarkparsia.protege.reasoner;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.CellConstraints;

import com.clarkparsia.dig20.exceptions.ErrorResponseException;
import com.clarkparsia.dig20.responses.ErrorResponse;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;

import java.net.ConnectException;
import java.util.logging.Logger;
import java.util.logging.Level;

import edu.stanford.smi.protege.util.WaitCursor;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.inference.ui.icons.InferenceIcons;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Title: ReasonerPreferencesDialog<br/>
 * Description: Dialog for manipulating the preferences for the C&P reasoner.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Apr 15, 2009 1:59:01 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ReasonerPreferencesDialog extends JDialog {
	private static Logger LOGGER = Log.getLogger(ReasonerPreferencesDialog.class);

	// consts for UI labels, these ought to be in a ResourceBundle
    private static final String LABEL_OK = "Ok";
    private static final String LABEL_REASONER_PREFERENCES = "Reasoner Details";
    private static final String LABEL_TRACK_CHANGES = "Synchronize Changes with Reasoner";
    private static final String LABEL_SYNCH_ON_QUERY = "Synchronize on query";
    private static final String LABEL_SYNCH_REALTIME = "Synchronize in real-time";
	private static final String LABEL_REASONER_STATUS = "Reasoner Status";
	private static final String LABEL_REASONER_BEHAVIOR = "Reasoner Behavior";

	/**
	 * Track changes checkbox
	 */
	private JCheckBox mTrackChanges;
	
    private JRadioButton mSynchRealTime;
    private JRadioButton mSynchOnQuery;

	/**
	 * The current project
	 */
    private Project mProject;

	/**
	 * Create a new ReasonerPreferencesDialog
	 * @param theProject the project whose preferences should be edited by this dialog
	 */
    public ReasonerPreferencesDialog(Project theProject) {
        super((JFrame)null, LABEL_REASONER_PREFERENCES, true);

        mProject = theProject;

        initGUI();

        pack();

        ComponentUtilities.center(this);
    }

	/**
	 * Initialize the GUI components
	 */
    private void initGUI() {
        mTrackChanges = new JCheckBox();

        mTrackChanges.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                rehup();

                if (PluginUtilities.isPluginAvailable(CustomReasonerProjectPlugin.class.getName())) {
                    CustomReasonerProjectPlugin.trackChanges(ProjectManager.getProjectManager().getCurrentProject(),
                                                             mTrackChanges.isSelected());
                }
            }
        });

        mSynchOnQuery = new JRadioButton(LABEL_SYNCH_ON_QUERY);
        mSynchRealTime = new JRadioButton(LABEL_SYNCH_REALTIME);

        ButtonGroup aGroup = new ButtonGroup();
        aGroup.add(mSynchOnQuery);
        aGroup.add(mSynchRealTime);

        Object aValue = mProject.getClientInformation(CustomReasonerProjectPlugin.KEY_SYNCH);
        if (aValue != null && aValue.equals(CustomReasonerProjectPlugin.VALUE_REALTIME)) {
            mSynchRealTime.setSelected(true);
        }
        else if (aValue != null && aValue.equals(CustomReasonerProjectPlugin.VALUE_QUERY)) {
            mSynchOnQuery.setSelected(true);
        }

        Boolean aTrack = (Boolean) mProject.getClientInformation(CustomReasonerProjectPlugin.KEY_TRACK_CHANGES);
        mTrackChanges.setSelected(aTrack != null && aTrack);

        mTrackChanges.setEnabled(!mProject.isMultiUserClient() && !mProject.isMultiUserServer() &&
                                 PluginUtilities.isPluginAvailable(CustomReasonerProjectPlugin.class.getName()));

		FormLayout aLayout = new FormLayout("left:pref:grow(.2), 10dlu, left:max(200dlu;pref):grow(.8)");

        DefaultFormBuilder aBuilder = new DefaultFormBuilder(aLayout);

        aBuilder.setDefaultDialogBorder();

        aBuilder.setRow(1);
        aBuilder.setColumn(1);

        aBuilder.appendRow(new RowSpec("top:pref:none"));

        CellConstraints c = new CellConstraints();

		aBuilder.addSeparator("Details");

		aBuilder.appendRow(new RowSpec("10dlu"));
		aBuilder.appendRow(new RowSpec("top:min(125dlu;pref):none"));
		aBuilder.nextRow(2);

		aBuilder.add(new JLabel(LABEL_REASONER_STATUS),
					 c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 1, 1, "right, top"));

		aBuilder.add(new ReasonerStatusComponent(),
					 c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1, "fill, fill"));

		aBuilder.appendRow(new RowSpec("10dlu"));
		aBuilder.appendRow(new RowSpec("top:pref:none"));
		aBuilder.nextRow(2);
		
		aBuilder.addSeparator("Preferences");

		aBuilder.appendRow(new RowSpec("10dlu"));
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(2);

        aBuilder.add(new JLabel(LABEL_TRACK_CHANGES),
                     c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 1, 1, "right, center"));

        aBuilder.add(mTrackChanges,
                     c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));

        aBuilder.appendRow(new RowSpec("10dlu"));
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(2);

        aBuilder.add(new JLabel(LABEL_REASONER_BEHAVIOR),
                     c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 1, 1, "right, center"));

        aBuilder.add(mSynchOnQuery,
                     c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));

        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(1);

        aBuilder.add(mSynchRealTime,
                     c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));

        aBuilder.appendRow(new RowSpec("top:10dlu:grow"));
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(2);

        JButton aOkBtn = new JButton(new OkAction());

        ButtonBarBuilder aBtnBuilder = new ButtonBarBuilder();

        aBtnBuilder.addFixed(aOkBtn);

        aBuilder.add(aBtnBuilder.getPanel(),
                     c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 3, 1, "center, bottom"));

        setContentPane(aBuilder.getPanel());

        rehup();
    }

	/**
	 * Updates the enabled status of the synch reasoner radio buttons
	 */
    private void rehup() {
        mSynchOnQuery.setEnabled(mTrackChanges.isEnabled() && mTrackChanges.isSelected() && !mProject.isMultiUserClient());
        mSynchRealTime.setEnabled(mTrackChanges.isEnabled() && mTrackChanges.isSelected() && !mProject.isMultiUserClient());
    }

	/**
	 * Action implementation for the Ok button
	 */
	private class OkAction extends AbstractAction {

		/**
		 * Create a new OkAction
		 */
		public OkAction() {
			super(LABEL_OK);
		}

		/**
		 * Performs the Ok operation when the dialog is closed.  The preferences are saved to the project and the dialog hidden.
		 * @inheritDoc
		 */
		public void actionPerformed(ActionEvent theEvent) {
			WaitCursor aCursor = new WaitCursor(null);

			aCursor.show();

			if (mSynchOnQuery.isSelected()) {
				mProject.setClientInformation(CustomReasonerProjectPlugin.KEY_SYNCH, CustomReasonerProjectPlugin.VALUE_QUERY);
			}
			else {//if (mSynchRealTime.isSelected()) {
				mProject.setClientInformation(CustomReasonerProjectPlugin.KEY_SYNCH, CustomReasonerProjectPlugin.VALUE_REALTIME);
			}

			mProject.setClientInformation(CustomReasonerProjectPlugin.KEY_TRACK_CHANGES, mTrackChanges.isSelected());

			dispose();
			setVisible(false);

			aCursor.hide();
		}
	}

	private String getBadStatusMessage(Exception theError) {
		String aStatusMsg = "There was an error while checking the reasoner status.  " +
							"The error message was: " + theError.getMessage();

		if (theError instanceof ConnectException ||
			theError.getCause() instanceof ConnectException ||
			theError.getMessage().toLowerCase().indexOf("connection refused") != -1) {
			// lets special case when the connection is refused, which means that either the reasoner is down,
			// or they have the wrong URL.

			aStatusMsg = "Status check failed, unable to establish a connection to the reasoner.  Please verify the the reasoner URL " +
						 "is correct, and that the reasoner is running.";
		}
		else if (theError instanceof ErrorResponseException ||
				 theError.getCause() instanceof ErrorResponseException) {
			ErrorResponseException ex = (ErrorResponseException) (theError instanceof ErrorResponseException ? theError : theError.getCause());

			if (ex.getCode() == ErrorResponse.ERROR_CODE_UNDEFINED_KB ||
				theError.getMessage().toLowerCase().indexOf("does not match server kb id") != -1) {
				aStatusMsg = "Status check failed, the current project KB Id does not match the KB Id in the reasoner.";
			}
			else {
				aStatusMsg = "Status check failed with content: " + ex.getContent();
			}
		}

		return aStatusMsg;
	}

	private class UpdateStatusAction extends AbstractAction {
		private ReasonerStatusComponent mComp;

		public UpdateStatusAction(ReasonerStatusComponent theComp) {
			super("Update");

			mComp = theComp;
		}

		public void actionPerformed(ActionEvent theEvent) {
			OWLModel aModel = (OWLModel) mProject.getKnowledgeBase();

			ProtegeReasoner aReasoner = ReasonerManager.getInstance().getProtegeReasoner(aModel);

			WaitCursor aCursor = new WaitCursor(null);

			try {
				aCursor.show();

				aReasoner.isSubsumedBy(aModel.getOWLThingClass(), aModel.getOWLNothing());
				mComp.updateStatus(Status.Ok, "Reasoner status check successful!");
			}
			catch (ProtegeReasonerException e) {
				mComp.updateStatus(Status.Fail, getBadStatusMessage(e));
				LOGGER.log(Level.INFO, "Exception while checking reasoner status", e);
			}
			catch (RuntimeException e) {
				mComp.updateStatus(Status.Fail, getBadStatusMessage(e));
				LOGGER.log(Level.INFO, "Exception while checking reasoner status", e);
			}
			finally {
				aCursor.hide();
			}
		}
	}

	private enum Status {
		Unknown, Ok, Fail
	}

	private class ReasonerStatusComponent extends JPanel {

		private Status mStatus = Status.Unknown;
		private String mStatusMsg;

		private DefaultTreeModel mTreeModel;
		private JTree mTree;

		public ReasonerStatusComponent() {
			initGUI();
		}

		private void initGUI() {
			mTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(""));

			mTree = new JTree();

			mTree.setRootVisible(false);
			mTree.setModel(mTreeModel);
			mTree.setCellRenderer(new NodeRenderer());
			setLayout(new GridBagLayout());
			add(new JScrollPane(mTree),
				new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));

			add(new JButton(new UpdateStatusAction(this)),
							new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,10,0,0), 0, 0));

			updateTree();
		}

		private void updateTree() {
			String aStatusLabel = "Unknown";
			switch (mStatus) {
				case Ok:
					aStatusLabel = "Ok";
					break;
				case Fail:
					aStatusLabel = "Failed";
					break;
			}

			DefaultMutableTreeNode aNewRoot = new DefaultMutableTreeNode("");

			DefaultMutableTreeNode aStatusNode = new DefaultMutableTreeNode("Status: " + aStatusLabel);

			if (mStatusMsg != null && mStatusMsg.length() > 0) {
				aStatusNode.add(new DefaultMutableTreeNode(mStatusMsg));
			}

			aNewRoot.add(aStatusNode);

			mTreeModel.setRoot(aNewRoot);

			for (int i = 0; i < mTree.getRowCount(); i++) {
				mTree.expandRow(i);
			}
		}

		public void updateStatus(Status theStatus, String theDetails) {
			mStatus = theStatus;
			mStatusMsg = theDetails;

			updateGUI();
		}

		private void updateGUI() {
			updateTree();
		}
	}

	private class NodeRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree theTree, Object theValue, boolean theSelection,
												boolean theExpanded, boolean theLeaf, int theRow, boolean theHasFocus) {

			super.getTreeCellRendererComponent(theTree, theValue, theSelection, theExpanded, theLeaf, theRow, theHasFocus);

			TreeNode aNode = (DefaultMutableTreeNode) theValue;

			if (aNode.getParent() != null && aNode.getParent().equals(theTree.getModel().getRoot())) {
				setIcon(InferenceIcons.getReasonerInspectorTreeIcon());
			}
			else {
				setIcon(getDefaultLeafIcon());
			}

			return this;
		}
	}
}
