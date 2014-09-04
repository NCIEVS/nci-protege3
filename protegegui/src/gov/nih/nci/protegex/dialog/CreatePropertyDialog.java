package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSDatatype;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.edit.NCIAnnotationsTableModel;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIFULLSYNTableModel;
import gov.nih.nci.protegex.util.*;
import gov.nih.nci.protegex.util.MsgDialog;

public class CreatePropertyDialog extends JDialog // implements ActionListener
{
	public static final long serialVersionUID = 123456794L;

	JButton okButton, cancelButton;

	// JTextField tf, fillerText;
	String separator;

	String property_name;

	String property_value;

	JButton selButton;

	KnowledgeBase kb;

	OWLModel owlModel;

	NCIEditTab tab;

	public boolean cancelBtnPressed;

	JTextField name_field;

	JTextArea value_field;

	public CreatePropertyDialog(NCIEditTab tab, String propertyname,
			String propertyvalue) {
		super((JFrame) tab.getTopLevelAncestor(), "Create a Property", true);
		this.tab = tab;
		this.kb = tab.getOWLModel();
		this.owlModel = (OWLModel) kb;

		// this.property_name = "";
		// this.property_value = "";

		this.property_name = propertyname;
		this.property_value = propertyvalue;

		cancelBtnPressed = false;

		JPanel panel = createPanel(propertyname, propertyvalue);

		String title = "Create a Property";
		if (propertyname.compareTo("") != 0) {
			title = "Modify a Property";
		}
		int r = ProtegeUI.getModalDialogFactory().showDialog(tab, panel, title,
				ModalDialogFactory.MODE_OK_CANCEL, value_field);

		if (r == ModalDialogFactory.OPTION_OK) {
			property_name = name_field.getText();
			property_value = value_field.getText();

			property_name.trim();
			property_value.trim();

			if (property_name.compareTo("") == 0
					|| property_value.compareTo("") == 0) {
				try {
                    MsgDialog.ok(this, "Incomplete data entry.");
					cancelBtnPressed = true;
					return;
				} catch (Exception ex) {
					Log.getLogger().log(Level.WARNING, "Exception caught", ex);
				}
			} else {

				if (tab.isReadOnlyProperty(property_name)) {
					Component warning_label = new JLabel(property_name
							+ " is a read-only property.");
					LabeledComponent lc2 = new LabeledComponent("",
							warning_label);
					ProtegeUI.getModalDialogFactory().showDialog(tab, lc2,
							"WARNING", ModalDialogFactory.MODE_CLOSE);
                    cancelBtnPressed = true;

					return;
				}

				property_name = name_field.getText();
				property_value = value_field.getText();
			}
		} else {
			cancelBtnPressed = true;
		}
	}

	public String getPropertyName() {
		return property_name;
	}

	public String getPropertyValue() {
		return property_value;
	}

	public boolean isCancelled() {
		return cancelBtnPressed;
	}	

	public JPanel createPanel() {
		return createPanel("", "");
	}

	public JPanel createPanel(String name, String value) {
		JPanel panel = new JPanel();
		panel.setLocation(450, 300);
		// panel.setLayout(new GridLayout(2, 1));
		panel.setLayout(new BorderLayout());

		LabeledComponent lc = null;

		name_field = new JTextField(name);
		name_field.setEditable(false);
		name_field.setPreferredSize(new Dimension(300, 20));

		lc = new LabeledComponent("Select a property", name_field);

		value_field = new JTextArea(value);
		value_field.setEditable(true);
		value_field.setLineWrap(true);
		value_field.setWrapStyleWord(true);
		if (name.compareTo("") != 0) {
		value_field.setEnabled(true);
		} else {
			value_field.setEnabled(false);
			
		}

		// final JButton propertyValueBtn = null;

		Action SelectPropertyAction = new AbstractAction(
				"Select a property ...",
				OWLIcons
						.getCreatePropertyIcon(OWLIcons.OWL_OBJECT_ANNOTATION_PROPERTY)) {
			public static final long serialVersionUID = 123456795L;

			public void actionPerformed(ActionEvent e) {
				

				Collection allowedSlots = ClsUtil.getSelectableResources(owlModel);
				
				// TODO: for 1.3 replace the call to DisplayUtilities with the line below
				RDFProperty slot = ProtegeUI.getSelectionDialogFactory().selectProperty(tab, owlModel, allowedSlots);
				//RDFProperty slot = (RDFProperty) DisplayUtilities.pickSlot(textfield, allowedSlots,
						//"Select a property");
				if (slot == null) {
					System.out.println("slot is null");
				} else {
					value_field.setEnabled(true);
					String selected_prop_name = slot.getLocalName();
					name_field.setText(selected_prop_name);
					value_field.setText("");
                    value_field.requestFocus();

                    if (tab.isComplexProp(selected_prop_name)) {
						value_field.setEditable(false);
					} else {
						RDFProperty property = owlModel.getRDFProperty(selected_prop_name);
						String[] allowedvalues = tab.getOWLWrapper().getAllowableValues(property);
						if (allowedvalues != null) {
							value_field.setEditable(false);
						} else {
							value_field.setEditable(true);
						}
					}
				}
			}

		};

		lc.addHeaderButton(SelectPropertyAction);
		panel.add(lc, BorderLayout.NORTH);

		JScrollPane comp = new JScrollPane(value_field);

		LabeledComponent lc2 = new LabeledComponent("Enter a property value",
				comp);
		lc2.setPreferredSize(new Dimension(400, 120));

		Action SelectPropertyValueAction = new AbstractAction(
				"Specify a property value...", OWLIcons
						.getCreateIcon(OWLIcons.ANNOTATION)) {
			public static final long serialVersionUID = 123456796L;

			public void actionPerformed(ActionEvent e) {
				String prop_name = name_field.getText();

				if (tab.isReadOnlyProperty(prop_name)) {
					Component warning_label = new JLabel(prop_name
							+ " is a read-only property.");
					LabeledComponent lc2 = new LabeledComponent("",
							warning_label);
					ProtegeUI.getModalDialogFactory().showDialog(tab, lc2,
							"WARNING", ModalDialogFactory.MODE_CLOSE);
					name_field.setText("");
					value_field.setText("");
					value_field.setEnabled(false);
					return;
				}

				value_field.setEnabled(true);
				
				RDFProperty property = (RDFProperty) owlModel.getRDFProperty(prop_name);
				String[] allowedvalues = tab.getOWLWrapper().getAllowableValues(property);
				if (allowedvalues != null) {
					value_field.setEditable(false);
					JList valueList;
					valueList = ComponentFactory.createList(null);
					valueList.getSelectionModel().setSelectionMode(
							ListSelectionModel.SINGLE_SELECTION);

					Arrays.sort(allowedvalues);
					java.util.List list = Arrays.asList(allowedvalues);
					valueList.setListData(list.toArray());
					valueList.setSelectedIndex(0);
					JScrollPane comp = new JScrollPane(valueList);

					String label = property.getBrowserText();
					LabeledComponent lc = new LabeledComponent(label, comp);

					lc.setPreferredSize(new Dimension(400, 120));
					int r = ProtegeUI.getModalDialogFactory().showDialog(tab,
							lc, "Add Annotation",
							ModalDialogFactory.MODE_OK_CANCEL);
					if (r == ModalDialogFactory.OPTION_OK) {

						String prop_value = (String) valueList
								.getSelectedValue();
						value_field.setText(prop_value);
					}
				}

				else {

					if (tab.isComplexProp(prop_name)) {

						CustomizedAnnotationDialog dialog = new CustomizedAnnotationDialog(
								tab.getEditPanel(), prop_name, "", -1,
								new NCIAnnotationsTableModel(owlModel));

						if (!dialog.cancelBtnPressed) {
							value_field.setText(dialog.getValue());
						}
					}

					else if (prop_name.equals(NCIEditTab.ALTLABEL)) {

						// if
						// (prop_name.equals(tab.getSlotName(NCIEditTab.ALTLABEL))
						// ||
						// prop_name.equals(tab.getSlotName(NCIEditTab.DEFINITION))){

						CustomizedAnnotationDialog dialog = new CustomizedAnnotationDialog(
								tab.getEditPanel(), prop_name, "", -1,
								new NCIFULLSYNTableModel(tab.getEditPanel()));

						if (!dialog.cancelBtnPressed) {
							value_field.setText(dialog.getValue());
						}

					} else {

						value_field.setEditable(true);
						JTextArea textArea = new JTextArea("");

						textArea.setLineWrap(true);
						textArea.setWrapStyleWord(true);

						Component comp = new JScrollPane(textArea);
						LabeledComponent lc3 = new LabeledComponent(prop_name,
								comp);

						lc3.setPreferredSize(new Dimension(400, 120));
						int r = ProtegeUI.getModalDialogFactory().showDialog(
								tab.getEditPanel(), lc3, "Edit Annotation",
								ModalDialogFactory.MODE_OK_CANCEL);
						if (r == ModalDialogFactory.OPTION_OK) {
							String newValue = textArea.getText();

							if (newValue.compareTo("") == 0) {
								Component warning_label = new JLabel(
										"Property value is mandatory.");
								LabeledComponent lc4 = new LabeledComponent("",
										warning_label);
								ProtegeUI.getModalDialogFactory()
										.showDialog(tab.getEditPanel(), lc4,
												"WARNING",
												ModalDialogFactory.MODE_CLOSE);
							} else {
								value_field.setText(newValue);
							}
						}
					}
				}
			}
		};

		lc2.addHeaderButton(SelectPropertyValueAction);
		panel.add(lc2, BorderLayout.CENTER);

		return panel;
	}

	
}
