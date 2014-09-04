/**
 * 
 */
package gov.nih.nci.protegex.panel;

import static gov.nih.nci.protegex.tree.TreePanel.PanelType.TYPE_RETIRE;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.edit.*;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author bitdiddle
 * 
 */
public class RetiredPanel extends JPanel implements ActionListener, PanelDirty,
		ConceptChangedListener {

	public static final long serialVersionUID = 123455792L;

	private Logger logger = Log.getLogger(getClass());

	private NCIEditTab tab = null;
	private OWLWrapper wrapper = null;

	private JButton saveButton_Retire, clearButton_Retire, retireButton_Retire,
			unretireButton_Retire;

	private TreePanel upperTreePanel_Retire = null;
	private JScrollPane retireUpperPane = null;

	public JScrollPane getScrollPane() {
		return this.retireUpperPane;
	}

	private boolean unretireButtonPressed;

	public void conceptChanged(OWLNamedClass cls, String msg) {
		ProtegeUI.getModalDialogFactory().showMessageDialog(
				this.tab.getOWLModel(), msg);

	}

	public RetiredPanel(NCIEditTab t) {
		super(false);
		tab = t;
		wrapper = tab.getOWLWrapper();
		init();
	}

	private void init() {

		setLayout(new BorderLayout());

		retireUpperPane = new JScrollPane();
		upperTreePanel_Retire = new TreePanel(tab, null, tab.getOWLModel());
		upperTreePanel_Retire.setType(TYPE_RETIRE);
		retireUpperPane.setViewportView(upperTreePanel_Retire);

		add(retireUpperPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		unretireButton_Retire = new JButton("Unretire");
		unretireButton_Retire.addActionListener(this);

		retireButton_Retire = new JButton("Retire");
		retireButton_Retire.addActionListener(this);

		saveButton_Retire = new JButton("Save");
		saveButton_Retire.addActionListener(this);

		clearButton_Retire = new JButton("Clear");
		clearButton_Retire.addActionListener(this);

		buttonPanel.add(unretireButton_Retire);
		buttonPanel.add(retireButton_Retire);
		buttonPanel.add(saveButton_Retire);
		buttonPanel.add(clearButton_Retire);

		unretireButton_Retire.setEnabled(false);
		retireButton_Retire.setEnabled(false);
		saveButton_Retire.setEnabled(false);
		clearButton_Retire.setEnabled(false);

		unretireButtonPressed = false;

		add(buttonPanel, BorderLayout.SOUTH);
	}

	public boolean isDirty() {
		return this.saveButton_Retire.isEnabled();
	}

	public void reset() {
		this.resetRetirePanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == retireButton_Retire) {
			try {
				retireButton_Retire.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				upperTreePanel_Retire.getTree();
				Cls retiringCls = tab.getWrapper().getCls(
						upperTreePanel_Retire.getLocalName());

				tab.updateNoteProperties(upperTreePanel_Retire,
						(RDFSClass) retiringCls);
				tab.attributes2Properties(upperTreePanel_Retire,
						(RDFSClass) retiringCls);

				// TreeNode child_node = null;
				Vector roles = upperTreePanel_Retire
						.getPropertyValues(NCIEditTab.PREDEPRECATIONSOURCEROLE);
				for (int i = 0; i < roles.size(); i++) {
					String role = (String) roles.elementAt(i);
					upperTreePanel_Retire.deleteProperty(
							NCIEditTab.PREDEPRECATIONSOURCEROLE, role, null);

				}

				// TreeNode child_node = null;
				Vector assocs = upperTreePanel_Retire
						.getPropertyValues(NCIEditTab.PREDEPRECATIONSOURCEASSOC);
				for (int i = 0; i < assocs.size(); i++) {
					String assoc = (String) assocs.elementAt(i);
					upperTreePanel_Retire.deleteProperty(
							NCIEditTab.PREDEPRECATIONSOURCEASSOC, assoc, null);

				}

				String conceptStatus = upperTreePanel_Retire
						.getPropertyValue(NCIEditTab.CONCEPTSTATUS);
				if (conceptStatus != null) {
					upperTreePanel_Retire.deleteProperty(
							NCIEditTab.CONCEPTSTATUS, conceptStatus, null);
				}
				upperTreePanel_Retire.addProperty(NCIEditTab.CONCEPTSTATUS,
						"Retired_Concept", null);

				String prop_value = NCIEditTab.PRERETIRED_CONCEPTS;
				upperTreePanel_Retire.deleteParent(prop_value);

				prop_value = NCIEditTab.RETIRED_CONCEPTS;

				// 082706
				// Cls retiredClses = wrapper.getCls(RETIRED_CONCEPTS);

				Cls retiredClses = tab.getWrapper().getCls(prop_value);
				upperTreePanel_Retire.addParent(retiredClses);

				// upperTreePanel_Retire.enablePopUpWindow();
				unretireButton_Retire.setEnabled(false);
				retireButton_Retire.setEnabled(false);
				saveButton_Retire.setEnabled(true);
				clearButton_Retire.setEnabled(true);

				unretireButtonPressed = false;

			} catch (Exception ex) {
				// Log.getLogger().log(Level.WARNING, "Exception caught", ex);
				OWLUI.handleError(tab.getOWLModel(), ex);
			}
			retireButton_Retire.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		else if (e.getSource() == unretireButton_Retire) {
			try {
				unretireButton_Retire.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				upperTreePanel_Retire.getTree();
				Cls retiringCls = tab.getWrapper().getCls(
						upperTreePanel_Retire.getLocalName());

				ArrayList<String> old_parents = wrapper.getPropertyValues(
						(OWLNamedClass) retiringCls,
						tab.PREDEPRECATIONPARENTCONCEPT);
				boolean ok = true;
				for (String par : old_parents) {
					OWLNamedClass op = (OWLNamedClass) wrapper.getCls(par);
					if (wrapper.isPreretired(op)) {
						ok = false;
					}
				}
				if (!ok) {
					ProtegeUI
							.getModalDialogFactory()
							.showDialog(
									tab,
									new JLabel(
											"Can't unretire class whose parent is preretired"),
									"WARNING", ModalDialogFactory.MODE_CLOSE);

				} else {
					tab.deleteNoteProperties(upperTreePanel_Retire);

					String prop_value = NCIEditTab.PRERETIRED_CONCEPTS;
					upperTreePanel_Retire.deleteParent(prop_value);

					prop_value = NCIEditTab.RETIRED_CONCEPTS;
					upperTreePanel_Retire.deleteParent(prop_value);

					properties2Attributes(upperTreePanel_Retire,
							(RDFSClass) retiringCls);

					unretireButton_Retire.setEnabled(false);
					retireButton_Retire.setEnabled(false);
					saveButton_Retire.setEnabled(true);
					clearButton_Retire.setEnabled(true);

					unretireButtonPressed = true;
				}

			} catch (Exception ex) {
				logger.warning("Exception caught" + ex.toString());
				OWLUI.handleError(tab.getOWLModel(), ex);
			}
			unretireButton_Retire.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		else if (e.getSource() == saveButton_Retire) {
			// Retire ListenedToClses
			String fromClsName = upperTreePanel_Retire.getLocalName();
			Cls fromCls = tab.getWrapper().getCls(fromClsName);

			saveButton_Retire.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			DataHandler.Status status = tab.getDataHandler().canSaveData(
					upperTreePanel_Retire, fromCls);
			if (status != DataHandler.Status.SUCCESSFUL) {
				if (status == DataHandler.Status.FAILURE) {
					tab.showError(upperTreePanel_Retire.getDisplayName());
					logger.info("Save is incomplete.");
				}
			} else {
				try {
					tab
							.removeFromListenedToClses((OWLNamedClass) fromCls,
									this);
					tab.getOWLModel().beginTransaction("Retire " + fromClsName,
							fromCls.getName());

					if (tab.saveConcept(upperTreePanel_Retire) == DataHandler.Status.SUCCESSFUL) {
						tab.getOWLModel().commitTransaction();

					} else {
						tab.getOWLModel().rollbackTransaction();
					}
				} catch (Exception ex) {
					tab.getOWLModel().rollbackTransaction();
					OWLUI.handleError(tab.getOWLModel(), ex);
				}

				OWLModel model = tab.getOWLModel();
				RDFProperty opp = model
						.getRDFProperty(NCIEditTab.PREDEPRECATIONPARENTCONCEPT);
				Collection oldParents = ((OWLNamedClass) fromCls)
						.getPropertyValues(opp);

				Iterator it = oldParents.iterator();
				while (it.hasNext()) {
					String conName = (String) it.next();
					String refcode = wrapper.getCode(wrapper.getCls(conName));
					tab.recordHistory(NCIEditTab.EVSHistoryAction.RETIRE,
							(OWLNamedClass) fromCls, refcode);

				}

				String name = upperTreePanel_Retire.getDisplayName();

				saveButton_Retire.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				if (unretireButtonPressed) {
					MsgDialog.ok(this, name + " has been unretired.");
				} else {
					MsgDialog.ok(this, name + " has been retired.");
					RDFSNamedClass cls = tab.getOWLModel().getRDFSNamedClass(
							fromClsName);
					Deprecatable deprecatedCls = (Deprecatable) cls;
					deprecatedCls.setDeprecated(true);
				}
				tab.addToListenedToClses((OWLNamedClass) fromCls, this);

				resetRetirePanel();

			}

			saveButton_Retire.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			//tab.ensureClassSelected(fromCls);
			tab.ensureClassSelected((OWLNamedClass) wrapper.getSelectableRoots()
					.iterator().next());
		} else if (e.getSource() == clearButton_Retire) {
			try {
				if (tab.checkNoSavedContinueMsg())
					return;
				resetRetirePanel();
				// resetAll();
			} catch (Exception ex) {
				Log.getLogger().log(Level.WARNING, "Exception caught", ex);
			}
		}
		// TODO Auto-generated method stub

	}

	private void resetRetirePanel() {

		upperTreePanel_Retire = new TreePanel(tab, null, tab.getOWLModel());
		upperTreePanel_Retire.setType(TYPE_RETIRE);
		retireUpperPane.setViewportView(upperTreePanel_Retire);

		retireButton_Retire.setEnabled(false);
		saveButton_Retire.setEnabled(false);
		clearButton_Retire.setEnabled(false);
		unretireButtonPressed = false;
		tab.clearListenedToClses();
	}

	public void enableButton(String buttonLabel, boolean state) {
		if (buttonLabel.compareTo("unretireButton_Retire") == 0)
			this.unretireButton_Retire.setEnabled(state);
		else if (buttonLabel.compareTo("clearButton_Retire") == 0)
			this.clearButton_Retire.setEnabled(state);
		else if (buttonLabel.compareTo("retireButton_Retire") == 0)
			this.retireButton_Retire.setEnabled(state);
		else if (buttonLabel.compareTo("saveButton_Retire") == 0)
			this.saveButton_Retire.setEnabled(state);

	}

	private void properties2Attributes(TreePanel panel, RDFSClass retiringCls) {
		// parent --> OLD_PARENT

		Vector sups = panel
				.getPropertyObjectValues(NCIEditTab.PREDEPRECATIONPARENTCONCEPT);
		for (int i = 0; i < sups.size(); i++) {
			Cls superCls = (Cls) sups.elementAt(i);
			if (superCls instanceof OWLNamedClass) {
				panel.addParent(superCls);
				panel.deleteProperty(NCIEditTab.PREDEPRECATIONPARENTCONCEPT,
						wrapper.getInternalName(superCls), superCls);
			}
		}

		// children --> OLD_CHILD
		String retiringClsName = wrapper.getInternalName(retiringCls);

		// 082606
		Vector subs = panel
				.getPropertyObjectValues(NCIEditTab.PREDEPRECATIONCHILDCONCEPT);
		for (int i = 0; i < subs.size(); i++) {
			Cls subCls = (Cls) subs.elementAt(i);
			if (subCls instanceof OWLNamedClass) {
				panel.deleteProperty(NCIEditTab.PREDEPRECATIONCHILDCONCEPT,
						wrapper.getInternalName(subCls), subCls);
				wrapper.addDirectSuperclass(subCls, retiringCls);
			}
		}

		// children --> OLD_SOURCE_ROLE
		Vector roles = panel
				.getPropertyValues(NCIEditTab.PREDEPRECATIONSOURCEROLE);
		for (int i = 0; i < roles.size(); i++) {
			String role = (String) roles.elementAt(i);
			panel.deleteProperty(NCIEditTab.PREDEPRECATIONSOURCEROLE, role,
					null);

			// ???
			// need to move to save button

			StringTokenizer st = new StringTokenizer(role, "|");
			String name = st.nextToken();
			String value = st.nextToken();
			String modifier = st.nextToken();

			// recover the inbound role
			OWLNamedClass owl_cls = wrapper.getOWLNamedClass(value);
			wrapper.addRestriction(owl_cls, name, retiringClsName, modifier);

		}

		// children --> OLD_SOURCE_ROLE
		Vector assocs = panel
				.getPropertyValues(NCIEditTab.PREDEPRECATIONSOURCEASSOC);
		for (int i = 0; i < assocs.size(); i++) {
			String assoc = (String) assocs.elementAt(i);
			panel.deleteProperty(NCIEditTab.PREDEPRECATIONSOURCEASSOC, assoc,
					null);

			// ???
			// need to move to save button

			StringTokenizer st = new StringTokenizer(assoc, "|");
			String name = st.nextToken();
			String value = st.nextToken();

			// recover the inbound role
			OWLNamedClass owl_cls = wrapper.getOWLNamedClass(value);
			wrapper.addObjectProperty(owl_cls, name, retiringClsName);

		}

		// roles --> OLD_ROLE
		roles = panel.getPropertyValues(NCIEditTab.PREDEPRECATIONROLE);
		for (int i = 0; i < roles.size(); i++) {
			String s = (String) roles.elementAt(i);
			/*
			 * StringTokenizer st = new StringTokenizer(s, "|"); String name =
			 * st.nextToken(); String value = st.nextToken(); String modifier =
			 * st.nextToken(); TreeNode node = panel.addRestriction( name,
			 * value, modifier);
			 */

			RDFSClass aClass = wrapper.parsableText2RDFSClass(s);
			boolean isDefining = false;

			panel.addRestriction(aClass, isDefining);

			// TreeNode node = panel.addRestriction(item.getCls(),
			// item.getIsDefining());

			panel.deleteProperty(NCIEditTab.PREDEPRECATIONROLE, s, null);
		}

		// OLD_STATE
		String oldState = panel
				.getPropertyValue(NCIEditTab.PREDEPRECATIONSTATE);
		if (oldState != null) {
			panel
					.deleteProperty(NCIEditTab.PREDEPRECATIONSTATE, oldState,
							null);
			panel.deleteProperty("hasType", "defined", null);
			panel.addProperty("hasType", "primitive", null);

		}

		// roles --> OLD_SOURCE_ROLE
		roles = panel.getPropertyValues(NCIEditTab.PREDEPRECATIONSOURCEROLE);
		for (int i = 0; i < roles.size(); i++) {
			String s = (String) roles.elementAt(i);
			panel.deleteProperty(NCIEditTab.PREDEPRECATIONSOURCEROLE, s, null);
		}
	}

}
