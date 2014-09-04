/**
 * 
 */
package gov.nih.nci.protegex.action;

import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.edit.NCICreateRolePanel;
import gov.nih.nci.protegex.edit.NCIDeleteRoleAction;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIOWLIcons;
import gov.nih.nci.protegex.edit.NCIRoleGroupEditorPanel;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.MsgDialog;

import java.awt.event.ActionEvent;
import java.util.Stack;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * @author bitdiddle
 *
 */
public class RoleGroupRestrictionComp {

	private static final long serialVersionUID = 1362163995326516988L;
	
	private NCIEditTab tab = null;
	private OWLModel kb = null;
	private OWLWrapper wrapper = null;
	private OWLNamedClass owl_ncls = null;
	private OWLClass owl_cls = null;
	private LabeledComponent lc = null;
	
	private RDFSClass rest = null;
	private boolean isDefining = false;
	
	private Stack<OWLRestriction> tmp_restrictions = new Stack<OWLRestriction>();
	
	public RDFSClass getRestriction() {
		return rest;
		
	}
	
	public boolean getIsDefining() {
		return isDefining;
		
	}

	public RoleGroupRestrictionComp(NCIEditTab t, OWLModel k, OWLWrapper w, OWLNamedClass nc, OWLClass c, boolean isdef) {
		tab = t;
		kb = k;
		wrapper = w;
		owl_ncls = nc;
		owl_cls = c;
		isDefining = isdef;
		
		runDialog();
		
	}
	
	private void runDialog() {
		
		boolean cancelBtnPressed = false;
		boolean dataEntryComplete = false;

		while (!cancelBtnPressed || !dataEntryComplete) {
			final NCIRoleGroupEditorPanel rolegroupPanel = new NCIRoleGroupEditorPanel(
					tab, kb, owl_ncls, -1, owl_cls, isDefining);

			String label = "Roles (Simple Restrictions)";
			lc = new LabeledComponent(label,
					rolegroupPanel);

			final String create_label = "Create a Restriction";
			Action createRestrictionAction_1 = new AbstractAction(
					"Create a role (simple restriction)...", OWLIcons
							.getCreateIcon(OWLIcons.OWL_RESTRICTION)) {
				private static final long serialVersionUID = -2650563044906021580L;

				public void actionPerformed(ActionEvent e) {
					OWLRestriction r = null;
					boolean completed = false;

					while (!completed) {
						NCICreateRolePanel dlg = new NCICreateRolePanel(
								tab,
								kb.getRDFSNamedClass(OWLNames.Cls.SOME_VALUES_FROM_RESTRICTION),
								null, null, (RDFSClass) tab.getSelectedCls(), null, false);

						int ans = ModalDialog.showDialog(tab, dlg,
								create_label, ModalDialog.MODE_OK_CANCEL);
						if (ans == ModalDialog.OPTION_OK) {
							r = dlg.getResult();
							if (r != null) {
								rolegroupPanel.getRoleGroupTableModel()
										.addRow(r);
								// push onto tmp stack in case use cancels main dialog
								tmp_restrictions.push(r);
								completed = true;
								break;
							} else {
								try {
									MsgDialog.ok(tab,
											"Incomplete data entry.");
									completed = false;
								} catch (Exception ex) {
									Log.getLogger().log(Level.WARNING,
											"Exception caught", ex);
								}
							}
						} else {
							completed = true;
						}
					}
				}
			};
			lc.addHeaderButton(createRestrictionAction_1);

			// ////////////////////////////////////////////////////////////////////////////////////////////////
			final String modify_label = "Modify a Restriction";
			Action modifyRestrictionAction_1 = new AbstractAction(
					"Modify a role (simple restriction)...", NCIOWLIcons
							.getImageIcon("edit")) {
				private static final long serialVersionUID = 3235873697675544483L;

				public void actionPerformed(ActionEvent e) {
					OWLRestriction r = null;
					boolean completed = false;

					int selIndex = rolegroupPanel.getRoleGroupTable()
							.getSelectedRow();
					if (selIndex == -1)
						return;

					r = rolegroupPanel.getRoleGroupTableModel().getRole(
							selIndex);

					RDFProperty p = r.getOnProperty();

					String fillerText = r.getFillerText();
					String prop_modifier = wrapper.getRestrictionType(r);

					String label = wrapper
							.restrictionType2Label(prop_modifier);
					edu.stanford.smi.protege.model.Cls metaCls = wrapper
							.getMetaClsByName(label);

					rolegroupPanel.getRoleGroupTableModel()
							.getRoleGroupNumber(selIndex);

					while (!completed) {
						NCICreateRolePanel dlg = new NCICreateRolePanel(
								rolegroupPanel, metaCls, p, fillerText,
								(RDFSClass) tab.getSelectedCls(), r, false);

						int ans = ModalDialog.showDialog(tab, dlg,
								modify_label, ModalDialog.MODE_OK_CANCEL);
						if (ans == ModalDialog.OPTION_OK) {
							r = dlg.getResult();
							if (r != null) {
								//System.out.println(r.getBrowserText());
								// rolegroupPanel.getRoleGroupTableModel().addRow(r);

								rolegroupPanel.getRoleGroupTableModel()
										.updateRestriction(r, selIndex);

								completed = true;
								break;
							} else {
								try {
									MsgDialog.ok(tab,
											"Incomplete data entry.");
									completed = false;
								} catch (Exception ex) {
									Log.getLogger().log(Level.WARNING,
											"Exception caught", ex);
								}
							}
						} else {
							completed = true;
						}
					}
				}
			};

			lc.addHeaderSeparator();
			lc.addHeaderButton(modifyRestrictionAction_1);

			// ////////////////////////////////////////////////////////////////////////////////////////////////

			lc.addHeaderSeparator();
			lc.addHeaderButton(new NCIDeleteRoleAction(rolegroupPanel, tab,
					rolegroupPanel.getRoleGroupTable()));
			// String label = "Create Restriction";

			int result = ModalDialog.showDialog(tab, lc, create_label,
					ModalDialog.MODE_OK_CANCEL);

			if (result == ModalDialog.OPTION_OK) {
				rest = rolegroupPanel.getRolegroups();
				if (rest == null) {
					try {
						MsgDialog.ok(tab, "Incomplete or invalid rolegroup. A rolegroup must have a minimum of four entries.");
						dataEntryComplete = false;
					} catch (Exception ex) {
						Log.getLogger().log(Level.WARNING,
								"Exception caught", ex);
					}
				} else {
					// Process data
					//HashMap hmap = rolegroupPanel.getRoleGroupTableModel()
							//.getRolegroups();
					//rest = rolegroupPanel
							//.roleGroupHash2RDFSClass(hmap);
					isDefining = rolegroupPanel.getIsDefining();

					
					break;
				}
			} else {
			    /**
			    if (owl_cls == null) {
			    rolegroupPanel.getRoleGroupTableModel().cleanOutModel();
			    }
			    **/
			    while (!tmp_restrictions.empty()) {
			        OWLRestriction tr = tmp_restrictions.pop();
			        tr.delete();
			           
			        
			    }
				break;
			}
		}
		
	}
	

	

}
