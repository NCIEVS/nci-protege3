package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCISelectClsesPanel;
import gov.nih.nci.protegex.util.MsgDialog;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class ModifySuperclassDialog extends JDialog {
	private static final long serialVersionUID = 123456008L;

	JButton okButton, cancelButton;

	JTextField tf, fillerText;

	String separator;

	JButton selButton;

	OWLModel owlModel;

	NCIEditTab tab;

	public boolean cancelBtnPressed;

	JTextField value_field;

	public JCheckBox definingCheckBox;

	boolean defining = false;

	boolean defining_prev = false;

	String name = "";

	private Cls selectedCls = null;

	boolean definingEditable = true;

	boolean modified = false;

	RDFSClass aClass = null;

	boolean isDefinition = false;

	public ModifySuperclassDialog(NCIEditTab tab, String name,
			RDFSClass aClass, boolean defining, boolean definingEditable) {
		super((JFrame) tab.getTopLevelAncestor(), "Modify Named Superclass",
				true);
		this.tab = tab;
		this.owlModel = tab.getOWLModel();
		this.defining = defining;
		this.defining_prev = defining;

		this.aClass = aClass;

		this.name = name;
		this.definingEditable = definingEditable;

		selectedCls = null;
		if (aClass != null) {
			selectedCls = (Cls) aClass;
		}

		String title = "Modify Named Superclass";
		cancelBtnPressed = false;
		JPanel panel = createPanel();

		int r = ProtegeUI.getModalDialogFactory().showDialog(tab, panel, title,
				ModalDialogFactory.MODE_OK_CANCEL);

		if (r == ModalDialogFactory.OPTION_OK) {
			if (selectedCls == null) {
				try {
					MsgDialog.ok(this, "Incomplete data entry.");
					cancelBtnPressed = true;
					return;
				} catch (Exception ex) {
					Log.getLogger().log(Level.WARNING, "Exception caught", ex);
					;
				}
			} else {
				if (name == null) {
					modified = true;
				} else {
					String selectedClsName = tab.getOWLWrapper()
							.getInternalName(selectedCls);
					if (selectedClsName.compareTo(name) != 0) {
						modified = true;
					}
				}

				defining = definingCheckBox.isSelected();

				if (!defining_prev && definingCheckBox.isSelected())
					modified = true;
				if (defining_prev && !definingCheckBox.isSelected())
					modified = true;
				if (!modified) {
					cancelBtnPressed = true;
				}

				isDefinition = defining;
			}
		} else {
			cancelBtnPressed = true;
		}
	}

	public boolean isCancelled() {
		return cancelBtnPressed;
	}

	public JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLocation(450, 300);
		panel.setLayout(new GridLayout(2, 1));

		LabeledComponent lc = null;
		JPanel definingPanel = new JPanel();
		definingPanel.setLayout(new BorderLayout());
		JLabel definingLabel = new JLabel("  Defining");
		definingCheckBox = new JCheckBox();
		definingCheckBox.setSelected(defining);
		definingCheckBox.setEnabled(definingEditable);

		definingPanel.add(definingCheckBox, BorderLayout.WEST);
		definingPanel.add(definingLabel, BorderLayout.CENTER);

		panel.add(definingPanel);

		value_field = new JTextField("");
		value_field.setEditable(false);
		
		String pt = selectedCls != null ? selectedCls.getBrowserText() : "";
		value_field.setText(pt);

		value_field.setPreferredSize(new Dimension(300, 20));
		lc = new LabeledComponent("Select a superclass", value_field);

		Action SelectPropertyValueAction = new AbstractAction(
				"Select a superclass...", OWLIcons.getAddIcon("PrimitiveClass")) {
			private static final long serialVersionUID = 123456009L;

			public void actionPerformed(ActionEvent e) {
				// java.awt.TextField textfield = new TextField();
				String label = "Select a named class";

				
				Collection clses = tab.getOWLWrapper().getSelectableRoots();
				
				
				

				boolean ok = false;

				while (!ok) {

					final NCISelectClsesPanel p = new NCISelectClsesPanel(
							owlModel, clses);
					int result = ModalDialog.showDialog(tab, p, label,
							ModalDialog.MODE_OK_CANCEL);
					if (result == ModalDialogFactory.OPTION_OK) {
						Collection c = p.getSelection();
						if (c != null && c.size() > 0) {
							Iterator it = c.iterator();
							Object obj = it.next();
							String clsName = ((OWLNamedClass) obj).getPrefixedName();
								//tab.getOWLWrapper()
									//.getInternalName((Cls) obj);
							if (clsName.compareTo("owl:Thing") == 0) {

							} else {
								setCls((Cls) obj);
								ok = true;
							}
						}
						
					} else {
						// user cancelled
						ok = true;
					}

				}
			}

		};

		lc.addHeaderButton(SelectPropertyValueAction);
		panel.add(lc);

		return panel;
	}

	public boolean getIsDefining() {
		return isDefinition;// defining;
	}

	public String getName() {
		return name;
	}

	public Cls getCls() {
		return selectedCls;
	}

	private void setCls(Cls cls) {
		selectedCls = cls;
		name = tab.getWrapper().getInternalName(selectedCls);
		value_field.setText(cls.getBrowserText());
	}
}
