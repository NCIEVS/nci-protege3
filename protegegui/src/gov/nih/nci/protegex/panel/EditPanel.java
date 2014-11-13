package gov.nih.nci.protegex.panel;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_ASSOCIATION;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_CONCEPT;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_PARENT;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_PROPERTY;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_RESTRICTION;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.owltable.DeleteRowAction;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.EditTypeFormAction;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.InstanceNameComponent;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.action.RoleGroupRestrictionComp;
import gov.nih.nci.protegex.dialog.CreateObjPropertyDialog;
import gov.nih.nci.protegex.dialog.CreateSubclassDialog;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationDialog;
import gov.nih.nci.protegex.dialog.ModifySuperclassDialog;
import gov.nih.nci.protegex.dialog.ReviewClassDialog;
import gov.nih.nci.protegex.edit.DataHandler;
import gov.nih.nci.protegex.edit.NCIAnnotationsTable;
import gov.nih.nci.protegex.edit.NCIAnnotationsTableModel;
import gov.nih.nci.protegex.edit.NCIConditionsTable;
import gov.nih.nci.protegex.edit.NCIConditionsTableItem;
import gov.nih.nci.protegex.edit.NCIConditionsTableModel;
import gov.nih.nci.protegex.edit.NCICreateDEFINITIONAction;
import gov.nih.nci.protegex.edit.NCICreateFULLSYNAction;
import gov.nih.nci.protegex.edit.NCICreateRolePanel;
import gov.nih.nci.protegex.edit.NCICreateValueAction;
import gov.nih.nci.protegex.edit.NCIDeletePropertyAction;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIFULLSYNTable;
import gov.nih.nci.protegex.edit.NCIFULLSYNTableModel;
import gov.nih.nci.protegex.edit.NCIOWLIcons;
import gov.nih.nci.protegex.edit.NCIQualifierTable;
import gov.nih.nci.protegex.edit.NCIQualifierTableModel;
import gov.nih.nci.protegex.edit.NCISelectSuperClsPanel;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.tree.TreeItem;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class EditPanel extends JPanel implements ActionListener, PanelDirty,
		ConceptChangedListener {
	public static final long serialVersionUID = 123456010L;

	private Logger logger = Log.getLogger(getClass());

	private EditTypeFormAction editTypeFormAction = null;

	private static final String SUBCLASSOF = NCIEditTab.SUBCLASSOF;

	private Instance selectedInstance;

	private OWLModel kb;

	private NCIEditTab tab;

	private String code = null;

	private OWLWrapper wrapper;

	public boolean isNew;

	private Instance instance = null;

	private InstanceNameComponent name_panel = null;

	private JButton reviewButton_Modify, saveButton_Modify,
			cancelButton_Modify, newButton_Modify, deleteButton_Modify;

	private JLabel codeLabel = null;

	private JTabbedPane tabbedPane_Modify = null;

	private NCIFULLSYNTableModel full_syn_mod = null;

	private NCIFULLSYNTable full_syn_table = null;

	private NCIAnnotationsTableModel def_mod, simple_prop_mod, obj_prop_mod,
			complex_prop_mod;

	private NCIAnnotationsTable def_table, simple_prop_table, obj_prop_table,
			complex_prop_table;

	private NCIQualifierTableModel def_qual_mod, complex_qual_mod;

	private NCIQualifierTable def_qual_table, complex_qual_table;

	private NCIConditionsTableModel role_mod, sups_mod;

	private NCIConditionsTable role_table, sups_table;

	private JPanel modify_panel = null;

	private boolean newBtn = true;

	private boolean isModified = false;

	private JLabel ptLabel;

	private JTextField ptTextField;

	// 6: restriction
	// 7: named superclass
	private JButton hb_add_1, hb_add_2, hb_add_3, hb_add_4, hb_add_5,
			hb_add_6s, hb_add_6, hb_add_7;

	private JButton hb_delete_1, hb_delete_2, hb_delete_3, hb_delete_4,
			hb_delete_5, hb_delete_6, hb_delete_7;

	private JButton hb_edit_1, hb_edit_2, hb_edit_3, hb_edit_4, hb_edit_5,
			hb_edit_6, hb_edit_7;

	private OWLNamedClass focusCls = null;

	private String initial_preferred_name = null;

	private boolean hasDEFINITION = true;

	private LabeledComponent topComponent = null;

	private String AdvancedQuery = "";

	public void conceptChanged(OWLNamedClass cls, String msg) {

		ProtegeUI.getModalDialogFactory().showMessageDialog(kb, msg);
		if (tab != null) {
			getCancelButton().setEnabled(true);
		}
	}

	public EditPanel(NCIEditTab tab, Instance selectedInstance, OWLModel kb,
			String advancedQuery, boolean newBtn) {
		super();
		this.newBtn = newBtn;
		this.tab = tab;
		this.wrapper = tab.getWrapper();
		this.kb = kb;
		this.instance = selectedInstance;
		this.AdvancedQuery = advancedQuery;

		if (selectedInstance != null
				&& selectedInstance instanceof OWLNamedClass) {
			focusCls = (OWLNamedClass) selectedInstance;
		}

		initialize();
	}

	public OWLNamedClass getFocusClass() {
		return focusCls;
	}

	public void setFocusClass(OWLNamedClass cls) {
		focusCls = cls;

	}

	public NCIEditTab getNCIEditTab() {
		return tab;
	}

	public OWLWrapper getOWLWrapper() {
		return wrapper;
	}

	public void setAdvancedQuery(String AdvancedQuery) {
		this.AdvancedQuery = AdvancedQuery;
	}

	public void setSelectedInstance(Instance instance) {
		selectedInstance = instance;
	}

	public void updateAll() {
		updateAll(false);
	}

	private void updateAll(boolean no_focus) {
		this.isModified = false;
		if (wrapper == null) {
			System.out.println("WARNING: wrapper == null");
			return;
		}
		code = "";

		Instance selectedInstance = getSelectedInstance();
		if (selectedInstance == null) {
			selectedInstance = focusCls;

		}

		name_panel = new InstanceNameComponent();
		// 120606
		if (AdvancedQuery.equals("AdvancedQuery")) {
			selectedInstance = focusCls;

		}

		String preferred_name = "";
		if (selectedInstance != null
				&& selectedInstance instanceof OWLNamedClass) {
			try {
				instance = selectedInstance;
				focusCls = (OWLNamedClass) selectedInstance;
			} catch (Exception ex) {
			}

			setInstance(selectedInstance);

			// name_panel.setInstance(selectedInstance);
			code = wrapper.getCode((Cls) selectedInstance);

			preferred_name = wrapper.getPropertyValue((Cls) selectedInstance,
					NCIEditTab.PREFLABEL);
			/**
			 * preferred_name = getObjectValue(wrapper.getSlotValue( (Cls)
			 * selectedInstance, getSlotName(NCIEditTab.PREFLABEL)));
			 */

			if (code != null) {
				code = wrapper.getPlainString(code);
				if (code.compareTo("0") != 0) {
					codeLabel.setText("   code: " + code + "   ");
				} else {
					codeLabel.setText("");
				}
			} else {
				codeLabel.setText("");
			}

			// 091406
			Cls cls = (Cls) selectedInstance;

			if (topComponent != null && cls != null) {
				String id = wrapper.getInternalName(cls);
				
				JPanel foo = new JPanel();
				foo.setLayout(new BorderLayout());
				foo.add(new JLabel("Class: "),BorderLayout.WEST);
				JTextField bar = new JTextField(id);
				bar.setEditable(false);
				bar.setBorder(null);
				foo.add(bar,BorderLayout.CENTER);
				
				topComponent.setHeaderComponent(foo);
				//topComponent.repaint();
				tab.repaint();
				//topComponent.setHeaderLabel("Class:  " + id);
			}

			if (no_focus) {
				topComponent.setHeaderComponent(null);
				topComponent.setHeaderLabel("Class:  ");
				
			}

			if (preferred_name != null) {
				ptTextField.setText(getObjectValue(preferred_name));
				initial_preferred_name = preferred_name;
			} else {
				ptTextField.setText("");
				initial_preferred_name = null;
			}

			ptTextField.setEditable(true);
			if (!wrapper.isEditable(focusCls)) {
				ptTextField.setEditable(false);
			}

			RDFProperty prop = wrapper.getRDFProperty(NCIEditTab.PREFLABEL);
			if (prop == null) {
				ptTextField.setEnabled(false);
				ptTextField.setEditable(false);
			}

			def_qual_mod.initialize();
			complex_qual_mod.initialize();

			full_syn_mod.setSubject((RDFResource) instance);

			// 080506
			if (hasDEFINITION) {
				def_mod.setSubject((RDFResource) instance);

				if (def_mod.getRowCount() > 0) {
					def_table.setRowSelectionInterval(0, 0);
					updateQualifierTable(def_mod.get_qualifier_tblmodel_id());
				}
			}

			simple_prop_mod.setSubject((RDFResource) instance);
			obj_prop_mod.setSubject((RDFResource) instance);
			complex_prop_mod.setSubject((RDFResource) instance);

			role_mod.setCls((OWLNamedClass) instance);
			sups_mod.setCls((OWLNamedClass) instance);

			newButton_Modify.setEnabled(true);
			reviewButton_Modify.setEnabled(true);
			saveButton_Modify.setEnabled(false);

			setDeleteButton();

			cancelButton_Modify.setEnabled(false);
		} else {
			codeLabel.setText("");
		}
	}

	public OWLModel getOWLModel() {
		return kb;
	}

	private Project getProject() {
		return getOWLModel().getProject();
	}

	private Instance getSelectedInstance() {
		return tab.getSelectedInstance();
	}

	private void modifyProperty(NCIAnnotationsTable table,
			NCIAnnotationsTableModel model) {

		int selIndex = table.getSelectedRow();
		if (selIndex == -1) {
			if (model.getRowCount() > 0) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						NOITEMSEL);
			}
			return;
		}

		if (selIndex >= 0 && selIndex < model.getRowCount()) {

			String propertyname = model.getPredicate(selIndex).getPrefixedName();

			if (wrapper.isReadOnlyProperty(propertyname)) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						propertyname + " is read only.");
				return;
			}

			table.setRowSelectionInterval(selIndex, selIndex);
			viewValue(table, model);

			table.setRowSelectionInterval(selIndex, selIndex);
			updateQualifierTable(model.get_qualifier_tblmodel_id());

		}
	}

	private void modifyProperty(NCIAnnotationsTable table,
			NCIAnnotationsTableModel model,
			NCIQualifierTableModel qualifier_tblmodel) {
		int selIndex = table.getSelectedRow();

		if (selIndex == -1) {
			if (model.getRowCount() > 0) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						NOITEMSEL);
			}
			return;
		}

		if (selIndex >= 0 && selIndex < model.getRowCount()) {

			table.setRowSelectionInterval(selIndex, selIndex);
			viewValue(table, model);

			table.setRowSelectionInterval(selIndex, selIndex);
			updateQualifierTable(model.get_qualifier_tblmodel_id());

		}
	}

	public void updateQualifierTable(int model_id) {
		if (model_id == 1) {
			updateQualifierTable(def_table, def_mod, def_qual_mod);
		} else if (model_id == 2) {
			updateQualifierTable(complex_prop_table, complex_prop_mod,
					complex_qual_mod);
		}
	}

	public void updateQualifierTable(NCIAnnotationsTable table,
			NCIAnnotationsTableModel model,
			NCIQualifierTableModel qualifier_tblmodel) {

		if (table == null || model == null || qualifier_tblmodel == null)
			return;

		int row = table.getSelectedRow();
		if (model.getRowCount() > 0 && row == -1) {
			table.setRowSelectionInterval(0, 0);
			row = 0;
		}

		if (row >= 0 && row < model.getRowCount()) {
			Object value = model.getValue(row);
			if (model.isComplex()) {
				value = model.get_Value(row);
			}

			if (value instanceof RDFResource) {
				// ProtegeUI.show((RDFResource) value);
			} else {
				RDFProperty property = model.getPredicate(row);

				String name = property.getPrefixedName();
				if (tab.isComplexProp(name)) {

					HashMap<String, String> hmap = null;
					if (value instanceof RDFSLiteral) {
						RDFSLiteral literal = (RDFSLiteral) value;
						hmap = ComplexPropertyParser.parseXML((String) literal
								.getString());
					} else {
						hmap = ComplexPropertyParser.parseXML((String) value);
					}
					// this little dance accounts for our config currently only
					// having
					// two type of these ocmplex beasts
					String cadcompname = name;
					
					qualifier_tblmodel.setData(hmap, NCIEditTab
							.getCustomizedAnnotationData(cadcompname)
							.getCadCompsNames());
					qualifier_tblmodel.fireTableDataChanged();
				}
			}
		}
	}
	
	private void resetState() {
		role_mod.cleanOutTemps();
		sups_mod.cleanOutTemps();
		updateAll();
		reviewButton_Modify.setEnabled(true);
		saveButton_Modify.setEnabled(false);
		cancelButton_Modify.setEnabled(false);
	}

	private void convertPrimitivesToDefinitions() {
		boolean ok = role_mod.togglePrimitiveDefiningState(true) &&
		sups_mod.togglePrimitiveDefiningState(true);
		
		if (ok) {
		
		
		this.enableSaveButton(true);
		} else {
			resetState();
		}

	}

	private void convertDefinitionsToPrimitives() {
		boolean ok = role_mod.togglePrimitiveDefiningState(false) &&
		sups_mod.togglePrimitiveDefiningState(false);
		
		if (ok) {
		this.enableSaveButton(true);
		} else {
			resetState();
		}

	}

	private void modifyRestriction(NCIConditionsTable table,
			NCIConditionsTableModel model) {
		int selIndex = table.getSelectedRow();

		if (selIndex < 0) {
			if (model.getRowCount() > 0) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						NOITEMSEL);
			}
			return;
		}

		if (selIndex >= 0 && selIndex < model.getRowCount()) {

			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) model
					.getItem(selIndex);

			if (tableItem.isSeparator())
				return;

			boolean defining = tableItem.isDefinition();

			if (tableItem.isInherited()) {
				try {
					MsgDialog.ok(tab, "Cannot modify inherited restriction.");
					return;
				} catch (Exception ex) {
					Log.getLogger().log(Level.WARNING, "Exception caught", ex);
				}
			}
			OWLClass aClass_old = (OWLClass) tableItem.getCls();

			if (aClass_old instanceof OWLUnionClass
					|| aClass_old instanceof OWLIntersectionClass) {

				RoleGroupRestrictionComp rrc = new RoleGroupRestrictionComp(
						tab, kb, wrapper, focusCls, aClass_old, defining);

				if (rrc.getRestriction() != null) {

					if ((rrc.getIsDefining() == defining)
							&& model.alreayHasItem(rrc.getRestriction())) {
						// rrc.getRestriction().delete();
						// model.deleteRow(selIndex);

					} else {

						RDFSClass aClass = rrc.getRestriction().createClone();
						model.modifyRow(selIndex, aClass, rrc.getIsDefining());
						enableSaveButton(true);
					}

				}
			} else {
				OWLRestriction r = (OWLRestriction) aClass_old;
				RDFProperty p = r.getOnProperty();

				String fillerText = r.getFillerText();
				String prop_modifier = wrapper.getRestrictionType(r);

				String label = wrapper.restrictionType2Label(prop_modifier);
				edu.stanford.smi.protege.model.Cls metaCls = wrapper
						.getMetaClsByName(label);

				NCICreateRolePanel dlg = new NCICreateRolePanel(this, metaCls,
						p, fillerText, (RDFSClass) this.focusCls, r, true);
				dlg.setIsDefining(defining);
				final String modify_label = "Modify a Restriction";
				int ans = ModalDialog.showDialog(tab, dlg, modify_label,
						ModalDialog.MODE_OK_CANCEL);
				if (ans == ModalDialog.OPTION_OK) {
					r = dlg.getResult();
					if (r != null) {
						model.modifyRow(selIndex, r, dlg.getIsDefining());
						enableSaveButton(true);
					}
				}

			}

		}
	}

	private void modifySuperclass(NCIConditionsTable table,
			NCIConditionsTableModel model) {
		int selIndex = table.getSelectedRow();

		int num_rows = model.getRowCount();
		if (selIndex < 0) {
			if (num_rows > 0) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						NOITEMSEL);
			}
			return;
		}

		if (selIndex >= 0 && selIndex < model.getRowCount()) {
			String clsname = model.getTableItemName(selIndex);
			if (clsname != null && !wrapper.isEditable(clsname)) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						clsname + " is read only.");
				return;
			}

			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) model
					.getItem(selIndex);

			if (tableItem.isInherited()) {
				try {
					MsgDialog.ok(tab, "Cannot modify inherited restriction.");
					return;
				} catch (Exception ex) {
					Log.getLogger().log(Level.WARNING, "Exception caught", ex);
				}
			}

			String name = wrapper.getInternalName(tableItem.getCls());// .getBrowserText();

			boolean defining = tableItem.getIsDefining();

			boolean definingEditable = true;

			ModifySuperclassDialog dlg = new ModifySuperclassDialog(tab, name,
					tableItem.getCls(), defining, definingEditable);
			if (!dlg.cancelBtnPressed) {
				defining = false;
				if (dlg.definingCheckBox.isSelected()) {
					defining = true;
				}

				if (dlg.getCls() != null) {
					String clsName = wrapper.getInternalName(dlg.getCls());
					OWLNamedClass ocl = (OWLNamedClass) dlg.getCls();
					if (wrapper.isPremerged(ocl) ||
							wrapper.isPreretired(ocl) ||
							wrapper.isRetired(ocl) ||
							clsName.compareTo("owl:Thing") == 0) {
						ProtegeUI.getModalDialogFactory()
								.showErrorMessageDialog(tab,
										"Cannot select " + clsName + ".");
						return;
					}

					RDFSClass aClass = (RDFSClass) dlg.getCls();

					model.modifyRow(selIndex, aClass, dlg.getIsDefining());

					saveButton_Modify.setEnabled(true);
					cancelButton_Modify.setEnabled(true);

				} else {
					try {
						MsgDialog.ok(tab, "Incomplete data entry.");
						return;
					} catch (Exception ex) {
						Log.getLogger().log(Level.WARNING, "Exception caught",
								ex);
					}

				}
			}
		}
	}

	private void viewValue(NCIAnnotationsTable table,
			NCIAnnotationsTableModel model) {
		final int row = table.getSelectedRow();
		viewValue(row, table, model);
	}

	private String[] getAllowableValues(RDFProperty property) {
		try {
			boolean rangeDefined = property.isRangeDefined();
			if (!rangeDefined)
				return null;
			boolean includingSuperproperties = false;
			Vector<String> v = new Vector<String>();
			Collection c = property.getRanges(includingSuperproperties);
			for (Iterator iterator = c.iterator(); iterator.hasNext();) {
				Object value = (Object) iterator.next();
				Collection values = ((OWLDataRange) value).getOneOf()
						.getValues();
				if (values.size() > 0) {
					for (Iterator iterator2 = values.iterator(); iterator2
							.hasNext();) {
						Object value2 = (Object) iterator2.next();
						v.add(value2.toString());
					}
				}
			}
			Object[] objs = v.toArray();
			String[] allowedvalues = new String[objs.length];

			for (int i = 0; i < objs.length; i++) {
				allowedvalues[i] = (String) objs[i];
			}
			return allowedvalues;
		} catch (Exception e) {
			return null;
		}
	}

	// TODO: this is largely overlap with customized dialogs
	private void viewValue(final int row, NCIAnnotationsTable table,
			NCIAnnotationsTableModel model) {

		Object value = model.getValue(row);
		if (model.isComplex()) {
			value = model.get_Value(row);
		}

		if (value instanceof RDFResource) {
			ProtegeUI.show((RDFResource) value);
		}

		else {

			RDFProperty property = model.getPredicate(row);
			// RDFResource subject = model.getSubject();
			String name = property.getPrefixedName();

			if (tab.isComplexProp(name)) {
			

				new CustomizedAnnotationDialog(this, name, value, row, model);

			} else {

				boolean ok = false;
				// keep this over each iteration to not lose user state
				JTextArea textArea = null;
				String newValue = null;
				while (!ok) {
					LabeledComponent lc = null;

					JList valueList = null;
					String[] allowedvalues = getAllowableValues(property);
					if (allowedvalues != null) {
						valueList = ComponentFactory.createList(null);
						valueList.getSelectionModel().setSelectionMode(
								ListSelectionModel.SINGLE_SELECTION);

						Arrays.sort(allowedvalues);
						java.util.List list = Arrays.asList(allowedvalues);
						valueList.setListData(list.toArray());

						// valueList.setSelectedIndex(0);
						String propertyvalue = model.getPropertyValue(row);
						boolean shouldScroll = true;
						valueList.setSelectedValue((Object) propertyvalue,
								shouldScroll);
						int selIndex = valueList.getSelectedIndex();
						valueList.ensureIndexIsVisible(selIndex);
						// boolean isAdjusting = true;
						valueList.setSelectedIndex(selIndex);

						JScrollPane comp = new JScrollPane(valueList);

						String label = property.getBrowserText();
						lc = new LabeledComponent(label, comp);

						lc.setPreferredSize(new Dimension(400, 120));
					} else {
						if (newValue != null) {
							textArea = new JTextArea(newValue.toString());
						} else {
							textArea = new JTextArea(value.toString());
						}
						textArea.setLineWrap(true);
						textArea.setWrapStyleWord(true);

						Component comp = new JScrollPane(textArea);
						lc = new LabeledComponent(property.getBrowserText(),
								comp);
					}

					lc.setPreferredSize(new Dimension(400, 120));
					int r = ProtegeUI.getModalDialogFactory().showDialog(this,
							lc, "Edit Annotation",
							ModalDialogFactory.MODE_OK_CANCEL);
					if (r == ModalDialogFactory.OPTION_OK) {

						if (allowedvalues != null) {
							newValue = (String) valueList.getSelectedValue();
						} else {
							newValue = textArea.getText();
						}

						newValue = StringUtil.cleanString(newValue, false);

						if (newValue.compareTo("") == 0) {
							Component warning_label = new JLabel(
									"Property value is mandatory.");
							LabeledComponent lc2 = new LabeledComponent("",
									warning_label);
							ProtegeUI.getModalDialogFactory().showDialog(this,
									lc2, "WARNING",
									ModalDialogFactory.MODE_CLOSE);
						} else {
							table.getSelectionModel().setSelectionInterval(row,
									row);
							model.resetValue(name, value, newValue);
							// model.setValue(newValue, row);
							enableSaveButton(true);

							ok = true;
						}
					} else {
						// user cancelled
						ok = true;
					}
				}
			}
		}
	}

	private void editFULLSYNTable(NCIFULLSYNTable table,
			NCIFULLSYNTableModel model) {
		int selIndex = table.getSelectedRow();
		if (selIndex < 0) {
			if (model.getRowCount() > 0) {
				ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
						NOITEMSEL);
			}
			return;
		}

		if (selIndex >= 0 && selIndex < model.getRowCount()) {
			editFULLSYN(selIndex, model);
		}
	}

	private void editFULLSYN(final int row, NCIFULLSYNTableModel model) {

		Object value = model.getValue(row);
		if (value instanceof RDFResource) {
			ProtegeUI.show((RDFResource) value);
		} else {
			RDFProperty property = model.getPredicate(row);

			String name = property.getPrefixedName();

			new CustomizedAnnotationDialog(this, name, value, row, model);
		}
	}
	
	int sortCol = 0;
	boolean isSortAsc = true;
	
	class ColumnListener extends MouseAdapter {
		protected JTable table;

		public ColumnListener(JTable t) {
			table = t;
		}

		public void mouseClicked(MouseEvent e) {
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex)
					.getModelIndex();

			if (modelIndex < 0)
				return;
			if (sortCol == modelIndex)
				isSortAsc = !isSortAsc;
			else
				sortCol = modelIndex;

			table.getTableHeader().repaint();
			
			
	        

			Collections.sort(full_syn_mod.getSortVector(), new MyComparator(isSortAsc, sortCol));
			
			//table.tableChanged(new TableModelEvent(NCIFULLSYNTableModel.this));
			table.repaint();
		}
	}
	
	class MyComparator implements Comparator {
		protected boolean isSortAsc;

		protected int col;

		public MyComparator(boolean sortAsc, int sortCol) {
			isSortAsc = sortAsc;
			col = sortCol;
		}

		private int compareObjs(Object o1, Object o2) {
			return -1;
		}

		public int compare(Object o1, Object o2) {
			if (!(o1 instanceof Vector) || !(o2 instanceof Vector))
				return 0;
			Vector s1 = (Vector) o1;
			Vector s2 = (Vector) o2;
			int result = 0;
			result = compareObjs(s1.get(col), s2.get(col));
			if (!isSortAsc)
				result = -result;
			return result;
		}

		public boolean equals(Object obj) {
			if (obj instanceof MyComparator) {
				MyComparator compObj = (MyComparator) obj;
				return ((compObj.isSortAsc == isSortAsc) && (compObj.col == col));
			}
			return false;
		}
	}



	private JPanel createClassTabbedPane() {
		JPanel panel = new JPanel(false);
		ImageIcon icon = new ImageIcon("images/middle.gif");

		tabbedPane_Modify = new JTabbedPane();

		JPanel panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout());

		tabbedPane_Modify.addTab("Basic Data", icon, panel_1, "");

		// ----------------------------------------------- ALTLABEL

		RDFResource subject = (RDFResource) getSelectedInstance();

		// 120606
		if (AdvancedQuery.equals("AdvancedQuery")) {
			subject = (RDFResource) focusCls;
		}

		Vector<String> userDefinedRelevantProperties = new Vector<String>();

		Collection<Slot> allowed_properties = new ArrayList<Slot>();
		RDFProperty full_syn_slot = tab.getPropertySlot(NCIEditTab.ALTLABEL);
		if (full_syn_slot != null) {
			allowed_properties.add(full_syn_slot);
			userDefinedRelevantProperties.add(full_syn_slot.getPrefixedName());
		} else {
			System.out
					.println("WARNING: Cannot find ALTLABEL property -- allowed_properties is empty");

		}

		full_syn_mod = new NCIFULLSYNTableModel(this, subject,
				userDefinedRelevantProperties);
		full_syn_table = new NCIFULLSYNTable(getProject(), full_syn_mod, "");
		
		full_syn_table.getTableHeader().addMouseListener(new ColumnListener(full_syn_table));

		String table_name = NCIEditTab
				.getCustomizedAnnotationData(NCIEditTab.ALTLABEL)
				.getPanelLabel();

		LabeledComponent lc1_1 = new LabeledComponent(table_name,
				new JScrollPane(full_syn_table));

		Action modifyFULLSYNAction = new AbstractAction("Edit full synonym",
				NCIOWLIcons.getImageIcon("edit")) {
			public static final long serialVersionUID = 123456013L;

			public void actionPerformed(ActionEvent e) {
				editFULLSYNTable(full_syn_table, full_syn_mod);
			}
		};

		hb_add_1 = lc1_1.addHeaderButton(new NCICreateFULLSYNAction(tab,
				full_syn_mod, allowed_properties));
		hb_edit_1 = lc1_1.addHeaderButton(modifyFULLSYNAction);
		hb_delete_1 = lc1_1.addHeaderButton(new NCIDeletePropertyAction(tab,
				this, full_syn_table));

		// ----------------------------------------------- DEFINITION

		String[] columnNames = new String[2];
		columnNames[0] = "Name";
		columnNames[1] = "Value";

		def_qual_mod = new NCIQualifierTableModel(columnNames);
		def_qual_table = new NCIQualifierTable(def_qual_mod);

		userDefinedRelevantProperties = new Vector<String>();

		allowed_properties = new ArrayList<Slot>();

		hasDEFINITION = false;
		RDFProperty definitionSlot = tab.getPropertySlot(NCIEditTab.DEFINITION);
		if (definitionSlot != null) {
			userDefinedRelevantProperties.add(NCIEditTab.DEFINITION);
			hasDEFINITION = true;
			allowed_properties.add(definitionSlot);

		} else {
			System.out.println("WARNING: Cannot find definition Slot.");

		}

		def_mod = new NCIAnnotationsTableModel(kb, this, subject,
				userDefinedRelevantProperties, new Vector<String>(), false, 1,
				2);
		def_mod.setModelType(NCIAnnotationsTableModel.COMPLEX_PROPERTY_MODEL);
		def_mod.setHasPropertyColumn(false);

		def_table = new NCIAnnotationsTable(def_mod, "");
		def_mod.setTable(def_table);

		LabeledComponent lc7 = new LabeledComponent("Qualifiers",
				new JScrollPane(def_qual_table));

		def_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					updateQualifierTable(def_table, def_mod, def_qual_mod);
				}

			}
		});

		def_table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						updateQualifierTable(def_table, def_mod, def_qual_mod);
					}
				});

		table_name = tab.getCustomizedAnnotationData(NCIEditTab.DEFINITION)
				.getPanelLabel();
		LabeledComponent lc1_2 = new LabeledComponent(table_name,
				new JScrollPane(def_table));

		Action modifyDEFINITIONAction = new AbstractAction("Edit definition",
				NCIOWLIcons.getImageIcon("edit")) {
			public static final long serialVersionUID = 123456014L;

			public void actionPerformed(ActionEvent e) {
				modifyProperty(def_table, def_mod, def_qual_mod);
			}
		};

		hb_add_2 = lc1_2.addHeaderButton(new NCICreateDEFINITIONAction(tab,
				def_mod, allowed_properties, 2));

		hb_edit_2 = lc1_2.addHeaderButton(modifyDEFINITIONAction);
		hb_delete_2 = lc1_2.addHeaderButton(new NCIDeletePropertyAction(tab,
				this, def_table, def_qual_mod, 2));

		JSplitPane splitPane_7 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				lc1_2, lc7);
		splitPane_7.setDividerLocation(200); // 150
		splitPane_7.setOneTouchExpandable(true);

		JSplitPane splitPane_1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				lc1_1, splitPane_7);
		splitPane_1.setOneTouchExpandable(true);
		splitPane_1.setDividerLocation(180);

		panel_1.add(splitPane_1, BorderLayout.CENTER);

		// //////////////////////////////////////////////////////////////////////////////////////////
		// relations
		// //////////////////////////////////////////////////////////////////////////////////////////

		JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BorderLayout());
		tabbedPane_Modify.addTab("Relations", icon, panel_2, "");

		Slot slot = kb.getSlot(Model.Slot.DIRECT_SUPERCLASSES);

		// /////////////
		// Restrictions
		// /////////////

		role_mod = new NCIConditionsTableModel((OWLNamedClass) subject, slot,
				NCIConditionsTableModel.SET_RESTRICTION, this);
		role_table = new NCIConditionsTable(getOWLModel(), role_mod);

		final String create_label = "Create a Restriction";
		Action createSingleRestrictionAction_1 = new AbstractAction(
				"Create a role (simple restriction)...", OWLIcons
						.getImageIcon(OWLIcons.OWL_RESTRICTION)) {
			private static final long serialVersionUID = -2650563044906021580L;

			public void actionPerformed(ActionEvent e) {
				OWLRestriction r = null;

				NCICreateRolePanel dlg = new NCICreateRolePanel(
						tab,
						kb
								.getRDFSNamedClass(OWLNames.Cls.SOME_VALUES_FROM_RESTRICTION),
						null, null, (RDFSClass) tab.getSelectedCls(), null,
						true);

				int ans = ModalDialog.showDialog(tab, dlg, create_label,
						ModalDialog.MODE_OK_CANCEL);
				if (ans == ModalDialog.OPTION_OK) {
					r = dlg.getResult();
					if ((r != null) && !role_mod.alreayHasItem(r))  {
						int row = -1;
						boolean isDedefining = dlg.getIsDefining();

						if (isDedefining) {
							row = 1;
						} else {
							row = role_mod.findFirstRowInNecessaryBlock();
						}
						role_mod.addRow(r, row, isDedefining);
						enableSaveButton(true);

					}
				}

			}
		};

		Action createRestrictionAction_1 = new AbstractAction(
				"Add a restriction/group", OWLIcons
						.getCreateIcon(OWLIcons.OWL_RESTRICTION)) {
			public static final long serialVersionUID = 123456015L;

			// OWLRestriction restriction = null;

			public void actionPerformed(ActionEvent e) {

				RoleGroupRestrictionComp rrc = new RoleGroupRestrictionComp(
						tab, kb, wrapper, focusCls, null, false);

				if (rrc.getRestriction() != null) {

					if (!role_mod.alreayHasItem(rrc.getRestriction())) {
						int row = -1;
						boolean isDedefining = rrc.getIsDefining();

						if (isDedefining) {
							row = 1;
						} else {
							row = role_mod.findFirstRowInNecessaryBlock();
						}

						role_mod
								.addRow(rrc.getRestriction(), row, isDedefining);
						enableSaveButton(true);
					}

				}

			}
		};

		Action modifyRestrictionAction_1 = new AbstractAction(
				"Edit a restriction/group", NCIOWLIcons.getImageIcon("edit")) {
			public static final long serialVersionUID = 123456018L;

			public void actionPerformed(ActionEvent e) {
				modifyRestriction(role_table, role_mod);
			}
		};

		DeleteRowAction deleteAction_1 = new DeleteRowAction(role_table) {
			public static final long serialVersionUID = 123456019L;

			public void actionPerformed(ActionEvent e) {

				int rowIndex = role_table.getSelectedRow();
				if (rowIndex == -1) {
					if (role_mod.getRowCount() > 0) {
						ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
								NOITEMSEL);
					}
					return;
				}

				if (role_mod.isSeparator(rowIndex))
					return;

				boolean bool_val = confirmDeleteAction("restriction");
				if (!bool_val)
					return;

				role_mod.deleteRow(rowIndex);
				enableSaveButton(true);

				// 062906
				if (role_mod.getRowCount() == 2)
					enableDelEditHB(6, false);
			}
		};

		LabeledComponent lc2_1 = new LabeledComponent("Restrictions/Groups",
				new JScrollPane(role_table));
		lc2_1.addHeaderSeparator();
		// lc2_1.addHeaderButton(createExpressionAction_1);
		hb_add_6s = lc2_1.addHeaderButton(createSingleRestrictionAction_1);
		hb_add_6 = lc2_1.addHeaderButton(createRestrictionAction_1);
		hb_edit_6 = lc2_1.addHeaderButton(modifyRestrictionAction_1);
		hb_delete_6 = lc2_1.addHeaderButton(deleteAction_1);

		// //////////////
		// Superconcepts
		// //////////////

		sups_mod = new NCIConditionsTableModel((OWLNamedClass) subject, slot,
				NCIConditionsTableModel.SET_SUPERCLASS, this);
		sups_table = new NCIConditionsTable(getOWLModel(), sups_mod);

		Action addNamedClassAction_2 = new AbstractAction("Add parent class",
				OWLIcons.getAddIcon("PrimitiveClass")) {
			public static final long serialVersionUID = 123456020L;

			public void actionPerformed(ActionEvent e) {
				// java.awt.TextField textfield = new TextField();
				String label = "Select a class.";
				Collection c;
				final NCISelectSuperClsPanel p = new NCISelectSuperClsPanel(kb);
				int result = ModalDialog.showDialog(tab, p, label,
						ModalDialog.MODE_OK_CANCEL);
				boolean isDefining = false;

				if (result == ModalDialog.OPTION_OK) {
					c = p.getSelection();
					isDefining = p.isDefining();
				} else {
					// c = Collections.EMPTY_LIST;
					return;
				}

				Cls cls = (Cls) CollectionUtilities.getFirstItem(c);
				if (cls == null
						|| ((OWLNamedClass) cls).getPrefixedName().compareTo(
								"owl:Thing") == 0) {
					try {
						MsgDialog.ok(tab, "No class is selected.");
						return;
					} catch (Exception ex) {
						Log.getLogger().log(Level.WARNING, "Exception caught",
								ex);
					}
				} else {

					// String prop_value = cls.getBrowserText();
					String prop_value = wrapper.getInternalName(cls);// .getBrowserText();

					// 013107
					boolean invaldSelection = wrapper
							.isPremerged((OWLNamedClass) cls);
					if (invaldSelection) {
						ProtegeUI.getModalDialogFactory()
								.showErrorMessageDialog(tab,
										"Cannot select a pre-merged class.");
						return;
					}
					invaldSelection = wrapper.isPreretired((OWLNamedClass) cls);
					if (invaldSelection) {
						ProtegeUI.getModalDialogFactory()
								.showErrorMessageDialog(tab,
										"Cannot select a pre-retired class.");
						return;
					}
					invaldSelection = wrapper.isRetired((OWLNamedClass) cls);
					if (invaldSelection) {
						ProtegeUI.getModalDialogFactory()
								.showErrorMessageDialog(tab,
										"Cannot select a retired class.");
						return;
					}

					else {
						RDFSClass value = (RDFSClass) cls;
						OWLNamedClass owl_cls = (OWLNamedClass) cls;

						if (prop_value.compareTo(wrapper
								.getInternalName(focusCls)) == 0)

						{
							ProtegeUI
									.getModalDialogFactory()
									.showErrorMessageDialog(sups_table,
											"Cannot select " + prop_value + ".");
							return;
						} else if (owl_cls.hasSuperclass(focusCls)) {
							ProtegeUI
									.getModalDialogFactory()
									.showErrorMessageDialog(sups_table,
											"Cannot select " + prop_value + ".");
							return;
						}
						int row = 1;

						if (sups_mod.alreayHasItem(owl_cls)) {
							Component warning_label = new JLabel(
									"Superclass already exists.");
							LabeledComponent lc2 = new LabeledComponent("",
									warning_label);
							ProtegeUI.getModalDialogFactory().showDialog(tab,
									lc2, "WARNING",
									ModalDialogFactory.MODE_CLOSE);
							return;
						}

						if (isDefining) {
							boolean canAdd = sups_mod.addRow(owl_cls, row,
									isDefining);
							if (canAdd) {
								enableDelEditHB(7, true);
								enableSaveButton(true);
							}
						} else {
							row = sups_mod.getNamedSuperClassCount();
							boolean canAdd = sups_mod.addRow(value, row);
							if (canAdd) {
								sups_table.getSelectionModel()
										.setSelectionInterval(row, row);
								sups_table.scrollRectToVisible(sups_table
										.getCellRect(row, 0, true));
								enableDelEditHB(7, true);
								enableSaveButton(true);
							}
						}

					}
				}
			}
		};

		Action modifySuperclassAction_1 = new AbstractAction(
				"Edit parent class", NCIOWLIcons.getImageIcon("edit")) {
			public static final long serialVersionUID = 123456021L;

			public void actionPerformed(ActionEvent e) {
				int rowIndex = sups_table.getSelectedRow();
				if (rowIndex == -1) {
					if (sups_mod.getRowCount() > 0) {
						ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
								NOITEMSEL);
					}
					return;
				}
				if (sups_mod.isSeparator(rowIndex)) {
					return;
				}

				modifySuperclass(sups_table, sups_mod);
			}
		};

		DeleteRowAction deleteAction_2 = new DeleteRowAction(sups_table) {
			public static final long serialVersionUID = 123456022L;

			public void actionPerformed(ActionEvent e) {
				int rowIndex = sups_table.getSelectedRow();
				if (rowIndex == -1) {
					if (sups_mod.getRowCount() > 0) {
						ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
								NOITEMSEL);
					}
					return;
				}

				if (sups_mod.isSeparator(rowIndex)) {
					return;
				}

				String clsname = sups_mod.getTableItemName(rowIndex);
				if (clsname != null && !wrapper.isEditable(clsname)) {
					ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
							clsname + " is read only.");
					return;
				} else {
					String owlThing = kb.getOWLThingClass().getBrowserText();

					if (clsname.compareTo(owlThing) == 0) {
						ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
								"Cannot delete " + clsname + ".");
						return;
					}
				}

				int knt = sups_mod.getStrictNamedSuperClassCount();
				if (knt == 1) {
					ProtegeUI.getModalDialogFactory().showMessageDialog(
							kb,
							"Cannot delete the last superclass " + clsname
									+ ".");
					return;
				}

				boolean bool_val = confirmDeleteAction("superclass");
				if (!bool_val)
					return;

				// KLO
				// cond_model_2.deleteRow(rowIndex, true);
				sups_mod.deleteRow(rowIndex);
				enableSaveButton(true);
				if (sups_mod.getRowCount() == 0) {
					enableDelEditHB(7, false);
				}
			}
		};

		LabeledComponent lc2_2 = new LabeledComponent("Parent Class",
				new JScrollPane(sups_table));
		lc2_2.addHeaderSeparator();
		hb_add_7 = lc2_2.addHeaderButton(addNamedClassAction_2);
		hb_edit_7 = lc2_2.addHeaderButton(modifySuperclassAction_1);
		hb_delete_7 = lc2_2.addHeaderButton(deleteAction_2);

		// /////////////////////////////
		// Associations
		// /////////////////////////////

		Vector<String> exclusion = new Vector<String>();
		exclusion.add("ID");
		obj_prop_mod = new NCIAnnotationsTableModel(kb, this,
				(OWLNamedClass) subject, new Vector<String>(), exclusion, true,
				-1, 4);
		obj_prop_mod
				.setModelType(NCIAnnotationsTableModel.OBJECT_PROPERTY_MODEL);
		obj_prop_table = new NCIAnnotationsTable(obj_prop_mod, "");

		Action addNamedClassAction_3 = new AbstractAction(
				"Add existing resource as value...",
				OWLIcons
						.getCreatePropertyIcon(OWLIcons.OWL_OBJECT_ANNOTATION_PROPERTY))

		{
			public static final long serialVersionUID = 123456023L;

			public void actionPerformed(ActionEvent e) {
				String title = "Add an Object-Valued Property";
				CreateObjPropertyDialog dlg = new CreateObjPropertyDialog(tab,
						title, null, null);
				if (!dlg.isCancelled()) {
					RDFProperty property = dlg.getSelectedProperty();
					Cls cls = dlg.getSelectedCls();

					if (property != null && cls != null) {
						OWLNamedClass value = (OWLNamedClass) cls;
						obj_prop_mod.addRow(property, value);

						enableSaveButton(true);
						enableDelEditHB(4, true);
					}
				}
			}

		};

		Action modifyAssociationAction = new AbstractAction(
				"Modify existing object valued property...", NCIOWLIcons
						.getImageIcon("edit"))

		{
			public static final long serialVersionUID = 123456024L;

			public void actionPerformed(ActionEvent e) {
				int rowIndex = obj_prop_table.getSelectedRow();
				if (rowIndex < 0) {
					if (obj_prop_mod.getRowCount() > 0) {
						ProtegeUI.getModalDialogFactory().showMessageDialog(kb,
								NOITEMSEL);
					}
					return;
				}

				Object selected_value = obj_prop_mod.getValue(rowIndex);
				OWLNamedClass owl_cls = (OWLNamedClass) selected_value;
				RDFProperty selected_property = obj_prop_mod
						.getPredicate(rowIndex);

				String title = "Edit an Object-Valued Property";
				CreateObjPropertyDialog dlg = new CreateObjPropertyDialog(tab,
						title, selected_property, owl_cls);

				if (!dlg.isCancelled()) {
					RDFProperty property = dlg.getSelectedProperty();
					Cls cls = dlg.getSelectedCls();
					if (property != null && cls != null) {
						obj_prop_mod.deleteRow(rowIndex);
						obj_prop_mod.addRow(property, cls);
						enableSaveButton(true);
					}
				}
			}

		};

		LabeledComponent lc2_3 = new LabeledComponent("Associations",
				new JScrollPane(obj_prop_table));
		lc2_3.addHeaderSeparator();
		hb_add_4 = lc2_3.addHeaderButton(addNamedClassAction_3);
		hb_edit_4 = lc2_3.addHeaderButton(modifyAssociationAction);
		hb_delete_4 = lc2_3.addHeaderButton(new NCIDeletePropertyAction(tab,
				this, obj_prop_table, 4));

		JSplitPane splitPane_3_1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				lc2_1, lc2_2);
		splitPane_3_1.setOneTouchExpandable(true);
		splitPane_3_1.setDividerLocation(150);

		JSplitPane splitPane_3_2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				splitPane_3_1, lc2_3);
		splitPane_3_2.setDividerLocation(300);
		splitPane_3_2.setOneTouchExpandable(true);

		panel_2.add(splitPane_3_2, BorderLayout.CENTER);

		// ///////////////////////////////////////////////////////////////////////////////////////////
		// Property Tab

		// Simple properties

		JPanel panel_3 = new JPanel();
		panel_3.setLayout(new BorderLayout());
		tabbedPane_Modify.addTab("Properties", icon, panel_3, "");

		// ----------------------------------------------- Properties
		userDefinedRelevantProperties = new Vector<String>();

		Vector<String> excludedProperties = new Vector<String>();
		excludedProperties.add(NCIEditTab.PREFLABEL);

		for (String s : tab.getComplexProperties()) {
			excludedProperties.add(s);
		}

		excludedProperties.add(NCIEditTab.CODE);
		excludedProperties.add("Model.Slot.DIRECT_SUBCLASSES");
		excludedProperties.add("Model.Slot.DIRECT_INSTANCES");
		excludedProperties.add(SUBCLASSOF);
		excludedProperties.add("ID");

		Collection c = getExcludedProperties();

		for (Iterator it = c.iterator(); it.hasNext();) {
			RDFProperty property = (RDFProperty) it.next();
			// excludedProperties.add(property.getBrowserText());
			excludedProperties.add(property.getBrowserText());// BrowserText());
		}

		simple_prop_mod = new NCIAnnotationsTableModel(kb, this, subject,
				userDefinedRelevantProperties, excludedProperties, false, -1, 3);

		simple_prop_mod
				.setModelType(NCIAnnotationsTableModel.SIMPLE_PROPERTY_MODEL);

		simple_prop_table = new NCIAnnotationsTable(simple_prop_mod, "");

		Action modifySimplePropertyAction = new AbstractAction(
				"Modify Simple Property", NCIOWLIcons.getImageIcon("edit")) {
			public static final long serialVersionUID = 123456025L;

			public void actionPerformed(ActionEvent e) {
				modifyProperty(simple_prop_table, simple_prop_mod);
			}
		};

		LabeledComponent lc3 = new LabeledComponent("Simple Properties",
				new JScrollPane(simple_prop_table));
		hb_add_3 = lc3.addHeaderButton(new NCICreateValueAction(tab, this,
				simple_prop_mod, null, excludedProperties, 3));
		hb_edit_3 = lc3.addHeaderButton(modifySimplePropertyAction);
		hb_delete_3 = lc3.addHeaderButton(new NCIDeletePropertyAction(tab,
				this, simple_prop_table, 3));

		// ///////////////////////////////////////////////////////////////////

		// Complex properties

		userDefinedRelevantProperties = new Vector<String>();
		Vector<String> pnams = tab.getComplexProperties();
		
		// TODO: remove this and replace with notion of complex - required
		for (String ps : pnams) {
			if (!tab.isRequiredProperty(ps)) {
				userDefinedRelevantProperties.add(ps);				
			}			
		}

		excludedProperties = new Vector<String>();
		complex_prop_mod = new NCIAnnotationsTableModel(kb, this, subject,
				userDefinedRelevantProperties, excludedProperties, false, 2, 5);
		complex_prop_mod
				.setModelType(NCIAnnotationsTableModel.COMPLEX_PROPERTY_MODEL);

		complex_prop_table = new NCIAnnotationsTable(complex_prop_mod, "");
		complex_prop_mod.setTable(complex_prop_table);

		complex_qual_mod = new NCIQualifierTableModel(columnNames);
		complex_qual_table = new NCIQualifierTable(complex_qual_mod);

		complex_prop_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					updateQualifierTable(complex_prop_table, complex_prop_mod,
							complex_qual_mod);
				}
			}
		});

		complex_prop_table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						updateQualifierTable(complex_prop_table,
								complex_prop_mod, complex_qual_mod);
					}
				});

		// 071906 qualifier scroll bar fix
		table_name = "Complex Properties";
		if (tab.getComplexProperties().size() > 0) {
			Vector<String> pns = tab.getComplexProperties();
			for (String p : pns) {
				if (!tab.isRequiredProperty(p)) {
					table_name = tab.getCustomizedAnnotationData(p).getPanelLabel();
					break;
				}
			}			
		}
		LabeledComponent lc4 = new LabeledComponent(table_name,
				new JScrollPane(complex_prop_table));

		allowed_properties = new ArrayList<Slot>();

		Vector<String> cprops = tab.getComplexProperties();
		for (String pn : cprops) {
			if (!tab.isRequiredProperty(pn)) {
			RDFProperty p = wrapper.getRDFProperty(pn);
			if (p != null) {
				allowed_properties.add(p);
			}
			}
		}

		Action modifyComplexPropertyAction = new AbstractAction(
				"Edit property", NCIOWLIcons.getImageIcon("edit")) {
			public static final long serialVersionUID = 123456026L;

			public void actionPerformed(ActionEvent e) {
				modifyProperty(complex_prop_table, complex_prop_mod);
			}
		};

		hb_add_5 = lc4.addHeaderButton(new NCICreateValueAction(tab, this,
				complex_prop_mod, allowed_properties, 5));
		hb_edit_5 = lc4.addHeaderButton(modifyComplexPropertyAction);
		hb_delete_5 = lc4.addHeaderButton(new NCIDeletePropertyAction(tab,
				this, complex_prop_table, complex_qual_mod, 5));

		LabeledComponent lc5 = new LabeledComponent("Qualifiers",
				new JScrollPane(complex_qual_table));

		JSplitPane splitPane_6 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lc4,
				lc5);
		splitPane_6.setDividerLocation(150);
		splitPane_6.setOneTouchExpandable(true);

		JSplitPane splitPane_5 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lc3,
				splitPane_6);

		splitPane_5.setDividerLocation(200); // 150
		splitPane_5.setOneTouchExpandable(true);

		panel_3.add(splitPane_5, BorderLayout.CENTER);

		tabbedPane_Modify.addTab("Properties", icon, panel_3, "");

		panel.setLayout(new GridLayout(1, 1));
		panel.add(tabbedPane_Modify);

		tabbedPane_Modify.setSelectedIndex(0);

		return panel;

	}

	public InstanceNameComponent getNamePanel() {
		return name_panel;
	}

	public void setInstance(Instance instance) {
		name_panel.setInstance(instance);
	}

	public void reInitialize() {
		initialize();
	}

	private class ConvertToDefinedClass extends AbstractAction {

		public ConvertToDefinedClass() {
			super("Convert to Defined Class", OWLIcons
					.getImageIcon(OWLIcons.DEFINED_OWL_CLASS));

		}

		public void actionPerformed(ActionEvent e) {
			convertPrimitivesToDefinitions();

		}
	}

	private class ConvertToPrimitiveClass extends AbstractAction {

		public ConvertToPrimitiveClass() {
			super("Convert to Primitive Class", OWLIcons
					.getImageIcon(OWLIcons.PRIMITIVE_OWL_CLASS));

		}

		public void actionPerformed(ActionEvent e) {
			convertDefinitionsToPrimitives();

		}
	}

	private void initialize() {

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(500, 500));

		JPanel labelPanel = new JPanel(false);
		labelPanel.setLayout(new BorderLayout());

		name_panel = new InstanceNameComponent();

		name_panel.setEditable(false);

		saveButton_Modify = new JButton("Save");
		deleteButton_Modify = new JButton("Delete");
		cancelButton_Modify = new JButton("Cancel");

		ptTextField = new JTextField();
		ptTextField.setPreferredSize(new Dimension(300, 22));
		// ptTextField.setEditable(false);

		ptTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_V) {
					if (e.getModifiers() > 0) {
						enableSaveButton(true);

					}
				}

			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
				if (e.getModifiers() > 0) {

				} else {
					enableSaveButton(true);
				}

			}
		});

		codeLabel = new JLabel("");
		selectedInstance = getSelectedInstance();
		// 120606
		if (AdvancedQuery.equals("AdvancedQuery")) {
			selectedInstance = focusCls;
		}

		code = null;
		String preferred_name = null;
		if (selectedInstance != null
				&& selectedInstance instanceof OWLNamedClass) {
			// editTypeAction = new EditTypeAction((RDFResource)
			// selectedInstance);
			this.setInstance(selectedInstance);
			// name_panel.setInstance(selectedInstance);

			code = wrapper.getCode((Cls) selectedInstance);
			Cls cls = (Cls) selectedInstance;
			String cls_name = wrapper.getInternalName(cls);

			// if (code != null &&
			// code.compareTo(selectedInstance.getBrowserText()) != 0)
			if (code != null && code.compareTo(cls_name) != 0) {
				codeLabel.setText("   code: " + code + "   ");
			} else {
				codeLabel.setText("   code: " + "   ");
			}

			preferred_name = wrapper.getPropertyValue((Cls) selectedInstance,
					NCIEditTab.PREFLABEL);

			if (preferred_name != null) {
				this.initial_preferred_name = preferred_name;
				ptTextField.setText(getObjectValue(preferred_name));
			} else {

				preferred_name = "";
				ptTextField.setText("");

			}

			if (!AdvancedQuery.equals("AdvancedQuery")) {
				ptTextField.setEditable(true);
			}
			if (!wrapper.isEditable(focusCls)) {
				ptTextField.setEditable(false);
			}

			// String internalName = wrapper.getInternalName((Cls)
			// selectedInstance);

			if (!AdvancedQuery.equals("AdvancedQuery")) {
				tab
						.addToListenedToClses((OWLNamedClass) selectedInstance,
								this);
			}
		} else {
			// RDFResource resource = (RDFResource) wrapper.getCls("owl:Thing");
			// editTypeAction = new EditTypeAction(resource);
			// editTypeFormAction = new EditTypeFormAction(resource);
		}

		JPanel ptPanel = new JPanel();
		ptPanel.setLayout(new BorderLayout());
		ptLabel = new JLabel("  Preferred Name: ");
		ptTextField.setPreferredSize(new Dimension(300, 22));
		ptPanel.add(ptLabel, BorderLayout.WEST);
		ptPanel.add(ptTextField, BorderLayout.CENTER);

		RDFProperty prop = wrapper.getRDFProperty(NCIEditTab.PREFLABEL);
		if (prop == null) {
			ptTextField.setEnabled(false);
			ptTextField.setEditable(false);
		}

		RDFProperty cprop = wrapper.getRDFProperty(wrapper.getCodeSlotName());
		if (cprop != null) {
			ptPanel.add(codeLabel, BorderLayout.EAST);
		}

		labelPanel.add(ptPanel, BorderLayout.CENTER);

		topComponent = new LabeledComponent("",labelPanel);
		
		
		
		
		
		if (topComponent != null && selectedInstance != null) {
			String id = wrapper.getInternalName((OWLNamedClass)selectedInstance);
			
			JPanel foo = new JPanel();
			foo.setLayout(new BorderLayout());
			foo.add(new JLabel("Class: "),BorderLayout.WEST);
			JTextField bar = new JTextField(id);
			bar.setEditable(false);
			bar.setBorder(null);
			foo.add(bar,BorderLayout.CENTER);
			
			topComponent.setHeaderComponent(foo);
			//topComponent.repaint();
			tab.repaint();
			//topComponent.setHeaderLabel("Class:  " + id);
		}
	    
		


		topComponent.addHeaderButton(new ConvertToDefinedClass());
		topComponent.addHeaderButton(new ConvertToPrimitiveClass());

		if (!AdvancedQuery.equals("AdvancedQuery")) {

			if (tab.getProject().isMultiUserClient()) {
				// noop
			} else {

				if (editTypeFormAction == null) {
					RDFResource resource = (RDFResource) wrapper
							.getCls("owl:Thing");
					editTypeFormAction = new EditTypeFormAction(resource);
				}

				topComponent.addHeaderButton(editTypeFormAction);
			}

		}

		this.add(topComponent, BorderLayout.NORTH);

		modify_panel = createClassTabbedPane();

		this.add(modify_panel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		if (newBtn) {
			newButton_Modify = new JButton("New");
			newButton_Modify.addActionListener(this);
		}

		reviewButton_Modify = new JButton("Review");
		reviewButton_Modify.addActionListener(this);

		// saveButton_Modify = new JButton("Save");
		saveButton_Modify.addActionListener(this);

		deleteButton_Modify.addActionListener(this);

		// cancelButton_Modify = new JButton("Cancel");
		cancelButton_Modify.addActionListener(this);

		if (newBtn) {
			buttonPanel.add(newButton_Modify);
		}

		if (tab.isActionAllowed(NCIEditTab.EDIT_RETIRED)) {
			buttonPanel.add(deleteButton_Modify);
		}

		if (selectedInstance != null
				&& selectedInstance instanceof OWLNamedClass) {
			OWLNamedClass retire_root = kb
					.getOWLNamedClass(NCIEditTab.RETIRED_CONCEPTS);
			OWLNamedClass curr_cls = (OWLNamedClass) selectedInstance;
			boolean isretired = curr_cls.isSubclassOf(retire_root);
			if (!isretired) {
				deleteButton_Modify.setEnabled(false);
			}
		} else {
			deleteButton_Modify.setEnabled(false);
		}

		buttonPanel.add(reviewButton_Modify);
		buttonPanel.add(saveButton_Modify);
		buttonPanel.add(cancelButton_Modify);

		this.add(buttonPanel, BorderLayout.SOUTH);

		if (newButton_Modify != null) {
			newButton_Modify.setEnabled(true);
		}
		reviewButton_Modify.setEnabled(true);
		saveButton_Modify.setEnabled(false);

		this.setDeleteButton();

		// 090606
		/*
		 * if (preferred_name == null || preferred_name.compareTo("") == 0) {
		 * saveButton_Modify.setEnabled(true); }
		 */
		cancelButton_Modify.setEnabled(false);
		isModified = false;

		this.setVisible(true);
	}

	public JButton getReviewButton() {
		return reviewButton_Modify;
	}

	public JButton getCancelButton() {	
		return cancelButton_Modify;
	}

	/**
	 * Synchronizing the preferred name value that exists in various location.
	 * 
	 * @return true if synchronized.
	 */
	private boolean synchronizePreferredName() {
		if (!tab.useNCIRules()) {
			// ignore synchornization when not using NCI rules
			return true;
		}
		String pnTF = getPreferredName(); // From JTextField
		String pnTM = full_syn_mod.getPtNciTermName(); // From
		// NCIFULLSYNTableModel

		if (pnTF.equals(initial_preferred_name)
				&& pnTM.equals(initial_preferred_name)) {
			return true;
		}
		
		if (pnTF.equalsIgnoreCase("")) {
			MsgDialog.ok(this, "Preferred Name is required");
			return false;
		}

		int ans = MsgDialog.yesOrNo(this,
				"WARNING: Preferred Name has changed, continue?");

		if (ans == MsgDialog.YES_OPTION) {
			// ok don't do anything

		} else {
			return false;
		}

		// Note: If the preferred names are both different in the JTextField
		// and table model, the JTextField has precedence.
		if (!pnTF.equals(initial_preferred_name)) {
			int i = full_syn_mod.getPtNciIndex();
			full_syn_mod.setValueAt(pnTF, i, NCIFULLSYNTableModel.TERM_NAME);
			simple_prop_mod.updateRDFSLabel(pnTF);
			return true;
		} else if (!pnTM.equalsIgnoreCase("")) {
		ptTextField.setText(pnTM);
		simple_prop_mod.updateRDFSLabel(pnTM);
		return true;
		} else {
			MsgDialog.ok(this,
			"WARNING: PT FULL SYN is required");
			return false;
		}
	}

	public void actionPerformed(ActionEvent event) {
		Object action = event.getSource();
		if (action == reviewButton_Modify) {
			if (focusCls != null) {
				new ReviewClassDialog(tab, this, (OWLNamedClass) focusCls);
			}
		}

		else if (action == newButton_Modify) {

			if (isSaveButtonEnabled()) {
				try {
					int ans = MsgDialog.yesOrNo(this,
							"No data will be saved. Do you want to continue?");
					if (ans == MsgDialog.NO_OPTION)
						return;
				} catch (Exception ex) {
					logger.warning("Exception caught "
							+ ex.getLocalizedMessage());
				}
			}

			tab.removeFromListenedToClses((OWLNamedClass) tab.getSelectedCls(),
					this);

			new CreateSubclassDialog(tab);
			reviewButton_Modify.setEnabled(true);
		}

		else if (action == saveButton_Modify) {

			if (!tab.isActionAllowed(NCIEditTab.EDIT_BASIC))

			{
				MsgDialog.ok(this, "User does not have write privilege.");
				saveButton_Modify.setEnabled(false);
				return;
			}

			if (!synchronizePreferredName()) {
				return;
			}

			OWLNamedClass curr_cls = (OWLNamedClass) instance;
			tab.removeFromListenedToClses(curr_cls, this);

			logger.fine("Starting entire save ...");
			long t0 = System.currentTimeMillis();

			saveButton_Modify.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			DataHandler.Status status = tab.getDataHandler().canSaveData(this,
					(Cls) instance);
			if (status != DataHandler.Status.SUCCESSFUL) {
				if (status == DataHandler.Status.FAILURE) {
					tab.showError(instance.getBrowserText());
					logger.warning("ERROR: Unable to save "
							+ curr_cls.getBrowserText());
				}
				saveButton_Modify.setEnabled(true);
				cancelButton_Modify.setEnabled(true);
				saveButton_Modify.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				tab.addToListenedToClses((OWLNamedClass) curr_cls, this);
				// updateAll();

				return;
			} else {
				try {
					kb.beginTransaction("Save class in EditPanel "
							+ curr_cls.getBrowserText(), curr_cls.getName());

					if (tab.getDataHandler().processData((Cls) instance, this) != DataHandler.Status.SUCCESSFUL) {
						kb.rollbackTransaction();
						saveButton_Modify.setEnabled(true);
						cancelButton_Modify.setEnabled(true);
						saveButton_Modify.setCursor(new Cursor(
								Cursor.DEFAULT_CURSOR));
						tab
								.addToListenedToClses((OWLNamedClass) curr_cls,
										this);

						return;
					}

					kb.commitTransaction();

					// tab.recordHistory(NCIEditTab.EVSHistoryAction.MODIFY,
					// curr_cls, "");
					//
				} catch (Exception ex) {
					kb.rollbackTransaction();
					OWLUI.handleError(kb, ex);
					saveButton_Modify.setEnabled(true);
					cancelButton_Modify.setEnabled(true);
					saveButton_Modify.setCursor(new Cursor(
							Cursor.DEFAULT_CURSOR));
					tab.addToListenedToClses((OWLNamedClass) curr_cls, this);

					return;

				}

				updateAll();

				logger.fine("\t finished. Time = "
						+ (System.currentTimeMillis() - t0));

				saveButton_Modify.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

				enableSaveButton(false);
				reviewButton_Modify.setEnabled(true);

				initial_preferred_name = getObjectValue(ptTextField.getText()
						.trim());
				tab.addToListenedToClses((OWLNamedClass) curr_cls, this);
				MsgDialog.ok(this, "Class saved successfully.");
			}
			
			tab.ensureClassSelected(curr_cls);

			//updateAll();

		}

		else if (action == deleteButton_Modify) {
			OWLNamedClass retire_root = kb
					.getOWLNamedClass(NCIEditTab.RETIRED_CONCEPTS);
			OWLNamedClass curr_cls = (OWLNamedClass) instance;

			boolean isretired = curr_cls.isSubclassOf(retire_root);

			if (retire_root == null || !isretired) {
				deleteButton_Modify
						.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				String text = curr_cls.getBrowserText()
						+ " has not been retired yet.";
				ModalDialog.showMessageDialog(null, text);
				logger.warning(text);

				return;
			}

			boolean confirmDeletion = confirmDeleteClassAction(curr_cls
					.getBrowserText());

			if (!confirmDeletion)
				return;

			deleteButton_Modify.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			tab.removeFromListenedToClses(curr_cls, this);
			String deletedClsName = curr_cls.getBrowserText();

			try {
				kb.beginTransaction("Deleting class "
						+ curr_cls.getBrowserText(), curr_cls.getName());

				// this.tab.owlModel.deleteCls(curr_cls);
				curr_cls.delete();

				kb.commitTransaction();
			} catch (Exception e) {
				logger.warning("transaction rollback");
				kb.rollbackTransaction();
				OWLUI.handleError(kb, e);
				deleteButton_Modify
						.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			}

			reviewButton_Modify.setEnabled(false);
			deleteButton_Modify.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			String text = deletedClsName + " has been deleted.";
			ModalDialog.showMessageDialog(null, text);
			deleteButton_Modify.setEnabled(false);

			tab.initializeEditPanel();

		}

		else if (action == cancelButton_Modify) {
			int option = MsgDialog.yesOrNo(null,
					"No data will be saved. Do you want to continue?");
			if (option == MsgDialog.YES_OPTION) {
				this.role_mod.cleanOutTemps();
				this.sups_mod.cleanOutTemps();
				updateAll();
				reviewButton_Modify.setEnabled(true);
				saveButton_Modify.setEnabled(false);
				cancelButton_Modify.setEnabled(false);
			}
		}
	}

	public String getPreferredName() {
		return ptTextField.getText().trim();
	}

	public String getUserName() {
		return tab.getUserName();
	}

	public void enableSaveButton(boolean state) {

		if (reviewButton_Modify != null) {
			reviewButton_Modify.setEnabled(state);
		}
		saveButton_Modify.setEnabled(state);
		cancelButton_Modify.setEnabled(state);
		isModified = state;
	}

	public boolean isSaveButtonEnabled() {
		return saveButton_Modify.isEnabled();
	}

	public boolean dataModified() {
		return isModified;
	}

	public boolean isDirty() {
		return dataModified();
	}

	public void reset() {
		setInstance(tab.getSelectedInstance());
		updateAll();
	}

	// ////////////////////////////////////////////////////////
	// Review
	// ////////////////////////////////////////////////////////

	// public NCIAnnotationsTableModel get_synonym_tablemodel()
	public NCIFULLSYNTableModel get_synonym_tablemodel() {
		return full_syn_mod;
	}

	public NCIAnnotationsTableModel get_definition_tablemodel() {
		return def_mod;
	}

	public NCIAnnotationsTableModel get_property_tablemodel() {
		return simple_prop_mod;
	}

	public NCIAnnotationsTableModel get_association_tablemodel() {
		return obj_prop_mod;
	}

	public NCIAnnotationsTableModel get_complex_property_tablemodel() {
		return complex_prop_mod;
	}

	public NCIConditionsTableModel get_restriction_tablemodel() {
		return role_mod;
	}

	public NCIConditionsTableModel get_superconcept_tablemodel() {
		return sups_mod;
	}

	public NCIQualifierTableModel get_qualifier_tablemodel_1() {
		return def_qual_mod;
	}

	public NCIQualifierTableModel get_qualifier_tablemodel() {
		return complex_qual_mod;
	}

	// ////////////////////////////////////////////////////////
	// Save
	// ////////////////////////////////////////////////////////

	// DYEE: TreeItems possibly
	public TreeItems getInitialState() {
		// Concept
		TreeItems v = new TreeItems();
		// String name = instance.getBrowserText();
		Cls cls = (Cls) instance;
		String name = wrapper.getInternalName(cls);// instance.getBrowserText();

		String value = "";
		OWLNamedClass owlCls = null;
		String restriction = "";
		String modifier = "";
		int cardinality = -1;

		TreeItem item = new TreeItem(TYPE_CONCEPT, name, value, restriction,
				modifier, cardinality);
		v.add(item);

		// preferred_name
		if (initial_preferred_name != null) {
			item = new TreeItem(TYPE_PROPERTY, NCIEditTab.PREFLABEL,
					getObjectValue(initial_preferred_name), restriction,
					modifier, cardinality);
			RDFProperty property = wrapper.getRDFProperty(NCIEditTab.PREFLABEL);
			item.setProperty(property);
			v.add(item);
		}

		// Synonym and PT Properties
		ArrayList properties = (ArrayList) get_synonym_tablemodel()
				.getInitialProperties();
		ArrayList values = (ArrayList) get_synonym_tablemodel()
				.getInitialValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);

			// value = getObjectValue(values.get(i));

			item = new TreeItem();
			name = property.getPrefixedName();
			item.setType(TYPE_PROPERTY);
			// item.setName(property.getBrowserText());
			item.setLanguage(StringUtil.getLanguage(values.get(i)));

			item.setName(name);
			item.setValue(value);
			item.setNameValue(property.getBrowserText(), values.get(i));
			item.setProperty(property);
			v.add(item);
		}

		// Definition Properties
		properties = (ArrayList) get_definition_tablemodel()
				.getInitialProperties();
		values = (ArrayList) get_definition_tablemodel().getInitialValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			// value = (String) values.get(i);
			// value = getObjectValue(values.get(i));
			// name = property.getBrowserText();
			name = property.getPrefixedName();// BrowserText();
			item = new TreeItem();
			item.setType(TYPE_PROPERTY);
			item.setLanguage(StringUtil.getLanguage(values.get(i)));

			item.setName(name);
			item.setValue(value);
			item.setNameValue(property.getBrowserText(), values.get(i));
			item.setProperty(property);
			v.add(item);
		}

		// Other Properties
		properties = (ArrayList) get_property_tablemodel()
				.getInitialProperties();
		values = (ArrayList) get_property_tablemodel().getInitialValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			if (!(property instanceof DefaultOWLObjectProperty)) {
				// value = (String) values.get(i);
				// value = getObjectValue(values.get(i));
				// name = property.getBrowserText();
				name = property.getPrefixedName();
				item = new TreeItem();
				item.setType(TYPE_PROPERTY);
				item.setLanguage(StringUtil.getLanguage(values.get(i)));

				item.setName(name);
				item.setValue(value);
				item.setNameValue(property.getBrowserText(), values.get(i));
				item.setProperty(property);
				v.add(item);
			}
		}

		properties = (ArrayList) get_complex_property_tablemodel()
				.getInitialProperties();
		values = (ArrayList) get_complex_property_tablemodel()
				.getInitialValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			if (!(property instanceof DefaultOWLObjectProperty)) {
				// value = (String) values.get(i);
				// value = getObjectValue(values.get(i));
				// name = property.getBrowserText();
				name = property.getPrefixedName();
				item = new TreeItem();
				item.setType(TYPE_PROPERTY);
				item.setName(name);
				item.setLanguage(StringUtil.getLanguage(values.get(i)));

				item.setValue(value);
				item.setNameValue(property.getBrowserText(), values.get(i));
				item.setProperty(property);
				v.add(item);
			}
		}

		// Associations
		properties = (ArrayList) get_association_tablemodel()
				.getInitialProperties();
		values = (ArrayList) get_association_tablemodel().getInitialValues();

		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			owlCls = (OWLNamedClass) values.get(i);

			// value = owlCls.getBrowserText();
			value = wrapper.getInternalName(owlCls);
			// name = property.getBrowserText();
			name = property.getPrefixedName();
			item = new TreeItem();
			item.setType(TYPE_ASSOCIATION);
			item.setName(name);
			item.setValue(value);
			item.setNameValue(property.getBrowserText(), value);
			item.setCls(owlCls);
			item.setProperty(property);
			item.setIsDefining(false);
			item.setProperty(property);
			v.add(item);
		}

		// Superconcepts
		ArrayList items = (ArrayList) get_superconcept_tablemodel()
				.getInitialItems();
		for (int i = 0; i < items.size(); i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) items
					.get(i);
			if (!tableItem.isSeparator()) {
				if (tableItem.getCls() instanceof OWLNamedClass) {
					// value = tableItem.getDisplayText();

					// added by tt
					value = wrapper.getInternalName(tableItem.getCls());
					// end added by tt

					item = new TreeItem();
					item.setType(TYPE_PARENT);
					item.setName(SUBCLASSOF);
					item.setValue(value);
					item.setNameValue(SUBCLASSOF, value);

					item.setIsDefining(tableItem.getIsDefining());
					item.setCls(tableItem.getCls());

					v.add(item);
				}
			}
		}

		// Restrictions
		items = (ArrayList) get_restriction_tablemodel().getInitialItems();
		for (int i = 0; i < items.size(); i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) items
					.get(i);
			// 101906, KLO
			if (!tableItem.isSeparator() && !tableItem.isInherited())
			// if (!tableItem.isSeparator())
			{
				RDFSClass aClass = tableItem.getCls();
				if (!(aClass instanceof OWLNamedClass)) {
					item = new TreeItem();
					item.setType(TYPE_RESTRICTION);
					item.setNameValue(aClass.getBrowserText());
					item.setIsDefining(tableItem.getIsDefining());
					if (aClass instanceof OWLAllValuesFrom) {
						item.setModifier("all");
					} else if (aClass instanceof OWLSomeValuesFrom) {
						item.setModifier("some");
					}
					item.setCls(tableItem.getCls());
					v.add(item);
				}

			}
		}
		return v;
	}

	// DYEE: TreeItems possibly
	public TreeItems getFinalState() {
		// Concept
		TreeItems v = new TreeItems();
		Cls cls = (Cls) instance;
		String name = wrapper.getInternalName(cls);

		String value = "";
		OWLNamedClass owlCls = null;
		String restriction = "";
		String modifier = "";
		int cardinality = -1;

		TreeItem item = new TreeItem(TYPE_CONCEPT, name, value, restriction,
				modifier, cardinality);
		v.add(item);

		// preferred_name
		String pt = ptTextField.getText().trim();
		pt = NCIEditTab.getPlainString(pt);

		item = new TreeItem(TYPE_PROPERTY, NCIEditTab.PREFLABEL,
				getObjectValue(pt), restriction, modifier, cardinality);
		RDFProperty prop = wrapper.getRDFProperty(NCIEditTab.PREFLABEL);
		item.setProperty(prop);
		v.add(item);

		// Synonym and PT Properties
		ArrayList properties = (ArrayList) get_synonym_tablemodel()
				.getProperties();
		ArrayList values = (ArrayList) get_synonym_tablemodel().getValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			// value = getObjectValue(values.get(i));
			name = property.getPrefixedName();
			item = new TreeItem();
			item.setLanguage(StringUtil.getLanguage(values.get(i)));
			item.setType(TYPE_PROPERTY);
			item.setName(name);

			item.setValue(NCIEditTab.getPlainString(value));
			item.setNameValue(property.getBrowserText(), values.get(i));
			item.setProperty(property);
			v.add(item);
		}

		// Definition Properties
		properties = (ArrayList) get_definition_tablemodel().getProperties();
		values = (ArrayList) get_definition_tablemodel().getValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			// value = getObjectValue(values.get(i));
			name = property.getPrefixedName();

			item = new TreeItem();
			item.setLanguage(StringUtil.getLanguage(values.get(i)));
			item.setType(TYPE_PROPERTY);
			item.setName(name);
			item.setValue(value);
			item.setNameValue(property.getBrowserText(), values.get(i));
			item.setProperty(property);
			v.add(item);
		}

		// Other Properties
		properties = (ArrayList) get_property_tablemodel().getProperties();
		values = (ArrayList) get_property_tablemodel().getValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			if (!(property instanceof DefaultOWLObjectProperty)) {
				// value = getObjectValue(values.get(i));
				name = property.getPrefixedName();

				item = new TreeItem();
				item.setLanguage(StringUtil.getLanguage(values.get(i)));
				item.setType(TYPE_PROPERTY);
				item.setName(name);
				item.setValue(value);
				item.setNameValue(property.getBrowserText(), values.get(i));
				item.setProperty(property);
				v.add(item);
			}
		}

		properties = (ArrayList) get_complex_property_tablemodel()
				.getProperties();
		values = (ArrayList) get_complex_property_tablemodel().getValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			if (!(property instanceof DefaultOWLObjectProperty)) {
				// value = getObjectValue(values.get(i));
				name = property.getPrefixedName();

				item = new TreeItem();
				item.setLanguage(StringUtil.getLanguage(values.get(i)));
				item.setType(TYPE_PROPERTY);
				item.setName(name);
				item.setValue(value);
				item.setNameValue(property.getBrowserText(), values.get(i));
				item.setProperty(property);
				v.add(item);
			}
		}

		// Associations
		properties = (ArrayList) get_association_tablemodel().getProperties();

		values = (ArrayList) get_association_tablemodel().getValues();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);

			// owlCls = (OWLNamedClass) values.get(i);

			// value = owlCls.getBrowserText();

			// value = wrapper.getInternalName(owlCls);//.getBrowserText();

			owlCls = (OWLNamedClass) values.get(i);
			value = owlCls.getPrefixedName();

			// name = property.getBrowserText();
			name = property.getPrefixedName();

			item = new TreeItem();
			item.setType(TYPE_ASSOCIATION);
			item.setName(name);
			item.setValue(value);
			item.setNameValue(property.getBrowserText(), value);

			item.setProperty(property);

			item.setCls(owlCls);

			v.add(item);
		}

		// Superconcepts
		ArrayList items = (ArrayList) get_superconcept_tablemodel().getItems();
		for (int i = 0; i < items.size(); i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) items
					.get(i);
			if (!tableItem.isSeparator()) {
				// value = tableItem.getDisplayText();
				if (tableItem.getCls() instanceof OWLNamedClass) {
					value = wrapper.getInternalName(tableItem.getCls());
					item = new TreeItem();
					item.setType(TYPE_PARENT);
					item.setName(SUBCLASSOF);

					item.setValue(value);
					item.setNameValue(SUBCLASSOF, value);

					item.setCls(tableItem.getCls());

					item.setIsDefining(tableItem.getIsDefining());
					item.setCls(tableItem.getCls());

					v.add(item);
				}
			}
		}

		// Restrictions
		items = (ArrayList) get_restriction_tablemodel().getItems();

		for (int i = 0; i < items.size(); i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) items
					.get(i);
			// 101906, KLO
			if (!tableItem.isSeparator() && !tableItem.isInherited())
			// if (!tableItem.isSeparator())
			{
				RDFSClass aClass = tableItem.getCls();
				if (!(aClass instanceof OWLNamedClass)) {
					item = new TreeItem();
					item.setType(TYPE_RESTRICTION);
					item.setNameValue(aClass.getBrowserText());
					item.setIsDefining(tableItem.getIsDefining());

					if (aClass instanceof OWLAllValuesFrom) {
						item.setModifier("all");
					} else if (aClass instanceof OWLSomeValuesFrom) {
						item.setModifier("some");
					}

					// item.setModifier(aClass.)

					if (!tableItem.getIsNew()
							&& !(aClass instanceof OWLNamedClass)) {
						item.setCls(aClass);
					} else {
						item.setCls(tableItem.getCls());
					}
					v.add(item);
				}
			}
		}
		return v;
	}

	/*
	 * static final int TYPE_PROPERTY = 1; static final int TYPE_RESTRICTION =
	 * 3; static final int TYPE_PARENT = 2; static final int TYPE_ASSOCIATION =
	 * 4;
	 * 
	 * public static String toNameValue(String name, Object value) { return name
	 * + ": " + getObjectValue(value); }
	 */

	private String object2String(Object obj) {
		if (obj == null)
			return null;
		Object newValue;
		String str = obj.toString();

		if (obj instanceof RDFSLiteral) {
			newValue = (RDFSLiteral) obj;

			RDFSLiteral literal = (RDFSLiteral) obj;
			if (literal.getLanguage() == null
					|| literal.getLanguage().compareTo("") == 0) {
				str = str.substring(2, str.length());
			} else if (str.length() > 6)// if (str.indexOf("~#") != -1)
			{
				str = str.substring(5, str.length());
			}

		} else if (obj instanceof Boolean) {
			newValue = Boolean.valueOf(str.equals("true"));
		} else if (obj instanceof Float) {
			newValue = Float.valueOf(str);
		} else if (obj instanceof Integer) {
			newValue = Integer.valueOf(str);
		} else {

			newValue = NCIEditTab.getPlainString(str);
		}

		return newValue.toString();
	}

	public String toNameValue(String name, Object value) {
		return name + ": " + object2String(value);
	}

	private Collection getExcludedProperties() {
		Collection<RDFProperty> properties = new ArrayList<RDFProperty>();
		Collection allowedProperties = kb.getRDFProperties();
		for (Iterator it = allowedProperties.iterator(); it.hasNext();) {
			RDFProperty property = (RDFProperty) it.next();
			if (property.isVisible() && property.hasObjectRange()
					&& !property.isSystem()) {
				properties.add(property);
			}
		}
		properties.add(kb.getOWLDisjointWithProperty());
		properties.add(kb.getOWLDifferentFromProperty());
		properties.add(kb.getOWLEquivalentPropertyProperty());
		properties.add(kb.getOWLSameAsProperty());
		properties.add(kb.getRDFProperty(RDFSNames.Slot.IS_DEFINED_BY));
		properties.add(kb.getRDFProperty(RDFSNames.Slot.SEE_ALSO));
		return properties;
	}

	private String getObjectValue(Object obj) {
		if (obj == null)
			return null;
		Object newValue;
		String str = obj.toString();

		if (str.indexOf("~#") != -1) {
			str = str.substring(5, str.length());
		}

		if (obj instanceof RDFSLiteral) {
			RDFSLiteral oldLiteral = (RDFSLiteral) obj;
			newValue = getOWLModel().createRDFSLiteral(str,
					oldLiteral.getDatatype());
		} else if (obj instanceof Boolean) {
			newValue = Boolean.valueOf(str.equals("true"));
		} else if (obj instanceof Float) {
			newValue = Float.valueOf(str);
		} else if (obj instanceof Integer) {
			newValue = Integer.valueOf(str);
		} else {
			newValue = str;
		}

		return newValue.toString();
	}

	public boolean confirmDeleteAction(String name) {
		return ProtegeUI
				.getModalDialogFactory()
				.showConfirmDialog(
						kb,
						"The selected class "
								+ name
								+ " will be deleted. Are you sure you want to delete it?",
						"Confirm Delete");
	}

	public boolean confirmDeleteClassAction(String name) {
		return ProtegeUI
				.getModalDialogFactory()
				.showConfirmDialog(
						kb,
						"The selected class "
								+ name
								+ " will be permanently deleted. Are you sure you want to delete it?",
						"Confirm Delete");
	}

	public void enableDelEditHB(int i, boolean b) {
		if (i == 1) {
			if (hb_delete_1 == null)
				return;
			hb_delete_1.setEnabled(b);
			hb_edit_1.setEnabled(b);
		} else if (i == 2) {
			if (hb_delete_2 == null)
				return;
			hb_delete_2.setEnabled(b);
			hb_edit_2.setEnabled(b);
		} else if (i == 3) {
			if (hb_delete_3 == null)
				return;
			hb_delete_3.setEnabled(b);
			hb_edit_3.setEnabled(b);
		} else if (i == 4) {
			if (hb_delete_4 == null)
				return;
			hb_delete_4.setEnabled(b);
			hb_edit_4.setEnabled(b);
		} else if (i == 5) {
			if (hb_delete_5 == null)
				return;
			hb_delete_5.setEnabled(b);
			hb_edit_5.setEnabled(b);
		} else if (i == 6) {
			if (hb_delete_6 == null)
				return;
			hb_delete_6.setEnabled(b);
			hb_edit_6.setEnabled(b);
		} else if (i == 7) {
			if (hb_delete_7 == null)
				return;
			hb_delete_7.setEnabled(b);
			hb_edit_7.setEnabled(b);
		}
	}

	public void disableAll() {
		// this.ptTextField.setEnabled(false);
		boolean b = false;
		hb_add_1.setEnabled(b);
		hb_add_2.setEnabled(b);
		hb_add_3.setEnabled(b);
		hb_add_4.setEnabled(b);
		hb_add_5.setEnabled(b);
		hb_add_6.setEnabled(b);
		hb_add_6s.setEnabled(b);
		hb_add_7.setEnabled(b);

		hb_delete_1.setEnabled(b);
		hb_edit_1.setEnabled(b);
		hb_delete_2.setEnabled(b);
		hb_edit_2.setEnabled(b);
		hb_delete_3.setEnabled(b);
		hb_edit_3.setEnabled(b);
		hb_delete_4.setEnabled(b);
		hb_edit_4.setEnabled(b);
		hb_delete_5.setEnabled(b);
		hb_edit_5.setEnabled(b);
		hb_delete_6.setEnabled(b);
		hb_edit_6.setEnabled(b);
		hb_delete_7.setEnabled(b);
		hb_edit_7.setEnabled(b);

		if (newButton_Modify != null) {
			newButton_Modify.setEnabled(b);
		}
		deleteButton_Modify.setEnabled(b);
	}

	public void enableAddButtons() {
		enableAddButtons(true);
	}

	public void enableAddButtons(boolean b) {
		hb_add_1.setEnabled(b);
		hb_add_2.setEnabled(b);
		hb_add_3.setEnabled(b);
		hb_add_4.setEnabled(b);
		hb_add_5.setEnabled(b);
		hb_add_6.setEnabled(b);
		hb_add_6s.setEnabled(b);
		hb_add_7.setEnabled(b);
	}

	private void setDeleteButton() {

		if ((tab.isActionAllowed(NCIEditTab.EDIT_RETIRED) || tab
				.isActionAllowed(NCIEditTab.DELETE))
				&& !isSystemCls()) {
			deleteButton_Modify.setEnabled(true);
		}

		else {
			deleteButton_Modify.setEnabled(false);
		}

	}

	// 101306
	private boolean isSystemCls() {
		Instance selectedInstance = getSelectedInstance();

		// 120606
		if (AdvancedQuery.equals("AdvancedQuery")) {
			selectedInstance = focusCls;
		}

		if (selectedInstance == null)
			return true;

		Cls cls = (Cls) selectedInstance;
		if (wrapper.getInternalName(cls).compareTo("owl:Thing") == 0
				|| wrapper.getInternalName(cls).compareTo("rdfs:Class") == 0
				|| wrapper.getInternalName(cls).compareTo("rdf:Property") == 0) {
			return true;
		}

		return false;
	}

	private final String NOITEMSEL = "No item is selected.";
}
