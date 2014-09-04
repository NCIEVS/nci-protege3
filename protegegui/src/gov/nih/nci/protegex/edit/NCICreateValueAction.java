package gov.nih.nci.protegex.edit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationDialog;
import gov.nih.nci.protegex.util.*;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.panel.*;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class NCICreateValueAction extends ResourceAction {
	private static final long serialVersionUID = 6233742130383434383L;

	private OWLModel kb;

	private NCIEditTab tab;
    
    private EditPanel editPanel = null;

	private Collection properties;

	private NCIAnnotationsTableModel tableModel = null;

	private String property_name = null;

	private String property_value = null;

	private int type;

	private JTextComponent textComponent = null;

	private RDFProperty currSlot = null;

	/**
	 * If true, notifies this action class that it is creating a value for a new
	 * concept. It does not have to check to see if the newly created value
	 * already exists. If it does check, it would inadvertently check the parent
	 * class since it is currently loaded in the EditPanel.
	 */
	private boolean isNewConcept = false;

	private NCICreateValueAction(NCIEditTab tab,EditPanel ep,
			NCIAnnotationsTableModel tableModel, int type) {
		super("Create new annotation value", OWLIcons
				.getCreateIcon(OWLIcons.ANNOTATION));
		this.tab = tab;
        this.editPanel = ep;
		this.type = type;
		this.kb = tab.getOWLModel();
		this.tableModel = tableModel;
	}

	public NCICreateValueAction(NCIEditTab tab, EditPanel ep,
			NCIAnnotationsTableModel tableModel, Collection properties, int type) {
		this(tab, ep, tableModel, type);

		this.properties = properties;
		if (properties == null) {
			this.properties = ClsUtil.getSelectableResources(kb);
		}

	}

	public NCICreateValueAction(NCIEditTab tab, EditPanel ep,
			NCIAnnotationsTableModel tableModel, Collection properties,
			Collection excludedProperties, int type) {
		this(tab, ep, tableModel, type);

		if (properties == null && excludedProperties == null) {
			this.properties = ClsUtil.getSelectableResources(kb);
		} else if (excludedProperties != null) {
			Collection newprops = new ArrayList();
			Collection all_properties = ClsUtil.getSelectableResources(kb);
			Object[] a = all_properties.toArray();
			/*
			 * Warning! p is NCI readOnly means that you cannot add or remove triples of the form <x, p, y>
			 */
			for (int i = 0; i < a.length; i++) {
				RDFProperty property = (RDFProperty) a[i];
				// if (!excludedProperties.contains(property.getBrowserText()))
				if (!excludedProperties.contains(property.getPrefixedName()) && !property.isReadOnly()) {
					newprops.add(property);
				}
			}
			this.properties = newprops;
		}

	}

	public void setTextComponent(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}

	public void actionPerformed(ActionEvent e) {
		if (currSlot != null) {
			selectValue(currSlot);
			return;
		}

		Collection allowedSlots = properties;
		if (allowedSlots == null)
			allowedSlots = new ArrayList(kb.getSlots());
		RDFProperty slot = ProtegeUI.getSelectionDialogFactory()
				.selectProperty(tab, kb, allowedSlots);

		selectValue(slot);
	}

	public void selectPropertyByCode(String code) {
		currSlot = ClsUtil.findRDFProperty(kb, code);
	}

	public void selectPropertyByName(String name) {
		currSlot = null;
		Iterator iterator = properties.iterator();
		while (iterator.hasNext()) {
			RDFProperty slot = (RDFProperty) iterator.next();
			String slotName = slot.getPrefixedName();
			if (!slotName.equals(name))
				continue;
			currSlot = slot;
			return;
		}
	}

	private void selectValue(RDFProperty slot) {
		if (slot == null)
			return;

		boolean badUTF8 = false;
		String name = slot.getPrefixedName();
		String prop_value = "";
		RDFProperty property = null;

		if (tab.isReadOnlyProperty(name)) {
			MsgDialog.warning(tab, name + " is a read-only property.");
			return;
		}

		property = slot;
		property_name = name;

		String[] allowedvalues = tab.getOWLWrapper().getAllowableValues(
				property);
		if (allowedvalues != null) {
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
			int r = ProtegeUI.getModalDialogFactory().showDialog(tab, lc,
					"Add Annotation", ModalDialogFactory.MODE_OK_CANCEL);
			if (r == ModalDialogFactory.OPTION_OK) {

				prop_value = (String) valueList.getSelectedValue();

				System.out.println(prop_value);

				if (!isNewConcept
						&& tableModel.alreadyHas(property_name, prop_value)) {
					// tab.getEditPanel().itemExists(key,
					// TreeItem.TYPE_PROPERTY)) {
					MsgDialog.warning(tab, "Property already exists.");
					property_name = "";
					property_value = "";
					return;
				} else {
					addToTableModel(property, prop_value, badUTF8);
				}
			}
		} else {

			if (name.compareTo("") == 0)
				return;
			property_name = name;

			if (tab.isComplexProp(name)) {
			

				CustomizedAnnotationDialog dialog = new CustomizedAnnotationDialog(
						tab.getEditPanel(), name, "", -1, tableModel);
				prop_value = dialog.getValue();
				property_value = prop_value;

				if (!isNewConcept
						&& tableModel.alreadyHas(name, property_value)) {
					// tab.getEditPanel().itemExists(key,
					// TreeItem.TYPE_PROPERTY)) {
					MsgDialog.warning(tab, "Property already exists.");
					name = "";
					property_value = "";
					return;
				}

			} else {
				boolean ok = false;

				while (!ok) {
					badUTF8 = false;
					final JTextArea textArea = new JTextArea(prop_value.toString());
					textArea.setLineWrap(true);
					textArea.setWrapStyleWord(true);
					textArea.setEnabled(true);

					JScrollPane comp = new JScrollPane(textArea);
					

					String label = tab.getOWLWrapper().getRDFProperty(name)
							.getBrowserText();

					LabeledComponent lc = new LabeledComponent(label, comp);

					lc.setPreferredSize(new Dimension(400, 120));
					comp.addFocusListener(new FocusAdapter() {
						public void focusGained(FocusEvent e) {
							textArea.requestFocus();
							
						}
					});
					int r = ProtegeUI.getModalDialogFactory().showDialog(tab,
							lc, "Add Annotation",
							ModalDialogFactory.MODE_OK_CANCEL, textArea);
					if (r == ModalDialogFactory.OPTION_OK) {

						if (textArea.getText().trim().compareTo("") == 0) {
							MsgDialog.warning(tab,
									"Property value is mandatory.");
							// return;
						} else {
							prop_value = textArea.getText().trim();
							prop_value = StringUtil.cleanString(prop_value,
									false);
							property_value = prop_value;

							// check if property name and value exists:
							if (!isNewConcept
									&& tableModel.alreadyHas(property_name,
											property_value)) {
								// tab.getEditPanel().itemExists(key,
								// TreeItem.TYPE_PROPERTY)) {
								MsgDialog.warning(tab,
										"Property already exists.");
							} else {
								ok = true;
							}

						}
					} else {
						ok = true;
						prop_value = null;
					}
				}
			}
		}
		addToTableModel(property, prop_value, badUTF8);
		updateTextComponent(prop_value, badUTF8);
	}

	private void addToTableModel(RDFProperty property, String prop_value,
			boolean badUTF8) {
		if (tableModel == null || prop_value == null
				|| prop_value.length() <= 0 || badUTF8)
			return;

		tableModel.addRow(property, prop_value);
		JTable table = tableModel.getTable();
		if (table != null) {
			int newrow = tableModel.getRowCount() - 1;
			table.setRowSelectionInterval(newrow, newrow);
		} else {
			System.out.println("table is null");
		}

		editPanel.enableSaveButton(true);
		if (tableModel.getRowCount() == 1)
			tab.getEditPanel().enableDelEditHB(type, true);
	}

	private void updateTextComponent(String prop_value, boolean badUTF8) {
		if (textComponent == null || prop_value == null
				|| prop_value.length() <= 0 || badUTF8)
			return;
		textComponent.setText(prop_value);
	}

	public String getPropertyName() {
		return property_name;
	}

	public String getPropertyValue() {
		return property_value;
	}

	public boolean isSuitable(Component component, RDFResource resource) {
		return resource instanceof RDFProperty && resource.isEditable()
				&& component instanceof JTree;
	}

	/**
	 * Sets the isNewConcept flag.
	 * 
	 * @param isNewConcept
	 *            The flag.
	 */
	public void setIsNewConcept(boolean isNewConcept) {
		this.isNewConcept = isNewConcept;
	}

	/**
	 * Returns true if is this action is handle a new concept.
	 * 
	 * @return true if is this action is handle a new concept.
	 */
	public boolean isNewConcept() {
		return isNewConcept;
	}
}
