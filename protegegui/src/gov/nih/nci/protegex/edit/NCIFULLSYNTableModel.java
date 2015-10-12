package gov.nih.nci.protegex.edit;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.util.ComplexPropertyParser;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import java.util.*;
import java.net.URI;

public class NCIFULLSYNTableModel extends AbstractTableModel {

	public static final long serialVersionUID = 123456901L;

	private ArrayList<RDFProperty> init_properties = new ArrayList<RDFProperty>();

	private ArrayList<Object> init_values = new ArrayList<Object>();

	private RDFResource subject;

	private ArrayList<RDFProperty> properties = new ArrayList<RDFProperty>();

	private ArrayList<Object> values = new ArrayList<Object>();
	
	public boolean alreadyHas(String propname, String value) {
		for (int i = 0; i < properties.size(); i++) {
			String pn = properties.get(i).getBrowserText();
			if (pn.equalsIgnoreCase(propname)) {
				String ss = values.get(i).toString();
				if (ss.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	private JTable table;

	public final static int TERM_NAME = 0;

	public final static int TERM_GROUP = 1;

	public final static int TERM_SOURCE = 2;

	public final static int TERM_SOURCE_CODE = 3;

	public final static int LANGUAGE = 4;

	private final String SUBPROPERTYOF = "rdfs:subPropertyOf";

	private Vector<String> userDefinedRelevantProperties, excludedProperties;

	private String[] columnNames = { "Term Name", "Term Group", "Term Source",
			"Source Code", "Lang" };

	private EditPanel editpanel;

	public NCIFULLSYNTableModel(EditPanel editpanel) {
		this.editpanel = editpanel;
		userDefinedRelevantProperties = new Vector<String>();
		excludedProperties = new Vector<String>();
		initialize();
	}

	public NCIFULLSYNTableModel(EditPanel editpanel, RDFResource subject,
			Vector<String> v) {

		this.editpanel = editpanel;
		userDefinedRelevantProperties = new Vector<String>();
		excludedProperties = new Vector<String>();
		this.subject = subject;

		for (int i = 0; i < v.size(); i++) {
			userDefinedRelevantProperties.add(v.elementAt(i));
		}

		if (subject != null) {

			initialize();
			refill();
		}
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return properties.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValue(int rowIndex) {
		if (!values.isEmpty()) {
			return values.get(rowIndex);
		} else {
			return "";
		}
	}

	public Object getValueAt(int row, int col) {

		Object obj = getValue(row);
		if (obj instanceof RDFSLiteral) {
			RDFSLiteral value = (RDFSLiteral) obj;
			return getFullSynValue(value.getString(), col);
		} else
			return getFullSynValue((String) obj, col);
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	public Class<?> getColumnClass(int c) {
		//return String.class;
		if (c == 4)
			return String.class;

		return getValueAt(0, c).getClass();
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		// return true;
		// if (col == NCIFULLSYNTableModel.LANGUAGE)
		// return true;
		return false;
	}

	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	public void setValueAt(Object aValue, int row, int col) {
		if (row < 0 || col < 0)
			return;
		setValueAndGetIt(aValue, row, col);
		fireTableCellUpdated(row, col);
		editpanel.enableSaveButton(true, false);
	}

	private Object setLanguage(int row, String newLanguage) {

		RDFProperty property = getPredicate(row);
		String text = (String) getDisplayValue(row);
		Object newValue = createNewValue(property, text, newLanguage);
		if (!subject.getPropertyValues(property).contains(newValue)) {
			Object oldValue = getValue(row);

			resetValue(property.getPrefixedName(), oldValue, newValue);
		}

		return newValue;
	}

	private Object createNewValue(RDFProperty property, String text,
			String language) {
		Object newValue = null;
		if (language == null || language.trim().length() == 0) {
			newValue = text;
		} else {
			newValue = property.getOWLModel().createRDFSLiteralOrString(text,
					language);
		}
		return newValue;
	}

	public Object getDisplayValue(int rowIndex) {
		Object value = values.get(rowIndex);
		if (value instanceof RDFSLiteral) {
			return ((RDFSLiteral) value).getString();
		}
		return value;
	}

	public Object setValueAndGetIt(Object value, int row, int col) {
		if (col == LANGUAGE)
			return setLanguage(row, (String) value);
		
		String oldValue = "";
		Object o1 = getValue(row);
		if (o1 instanceof String) {
			oldValue = (String) o1;
		} else if (o1 instanceof RDFSLiteral) {
			oldValue = ((RDFSLiteral) o1).getBrowserText();
		}

		
		oldValue = ComplexPropertyParser.pipeDelim2XML(oldValue);
		HashMap<String, String> hmap = ComplexPropertyParser.parseXML(oldValue);

		String newValue = null;
		if (col == TERM_NAME) {
			newValue = ComplexPropertyParser.replaceFullSynValue(hmap,
					"term-name", (String) value);
			if (newValue == null)
				return oldValue;
			return setValue(newValue, row, col);
		}
		if (col == TERM_GROUP)
			newValue = ComplexPropertyParser.replaceFullSynValue(hmap,
					"term-group", (String) value);
		else if (col == TERM_SOURCE)
			newValue = ComplexPropertyParser.replaceFullSynValue(hmap,
					"term-source", (String) value);
		else if (col == TERM_SOURCE_CODE)
			newValue = ComplexPropertyParser.replaceFullSynValue(hmap,
					"source-code", (String) value);

		if (newValue == null)
			return "";
		return setValue(newValue, row, col);
	}

	public String getLanguage(int row) {
		Object value = getValue(row);
		if (value instanceof RDFSLiteral) {
			return ((RDFSLiteral) value).getLanguage();
		} else {
			HashMap<String, String> hmap = ComplexPropertyParser
					.parseXML((String) value);
			return (String) hmap.get("xml:lang");
		}
	}

	private Object createNewValue(String text, String language) {
		Object newValue = null;
		if (language == null || language.trim().length() == 0) {
			newValue = text;
		} else {
			newValue = subject.getOWLModel().createRDFSLiteralOrString(text,
					language);
		}
		return newValue;
	}

	public Object setValue(Object aValue, int row, int col) {
		RDFProperty property = getPredicate(row);
		Object oldValue = getValue(row);
		if (oldValue == null || !oldValue.equals(aValue)) {
			// RDFResource range = property.getRange();
			try {
				Object newValue = null;
				String str = aValue.toString();
				OWLModel owlModel = property.getOWLModel();
				if (owlModel.getOWLOntologyProperties().contains(property)) {
					String message = str + " is not a valid URI.";
					try {
						new URI(str);
					} catch (Exception ex) {
						ProtegeUI.getModalDialogFactory()
								.showErrorMessageDialog(owlModel, message);
						return oldValue;
					}
					if (!str.startsWith("http://") && !str.startsWith("file:")) {
						ProtegeUI.getModalDialogFactory()
								.showErrorMessageDialog(owlModel, message);
						return oldValue;
					}
				}

				String lang = getLanguage(row);
				// ?????
				if (aValue instanceof RDFSLiteral
						&& ((RDFSLiteral) aValue).getLanguage() != null) {
					newValue = aValue;
				} else if (lang != null) {
					newValue = createNewValue(str, lang);
					return newValue;
				}

				else if (oldValue instanceof RDFSLiteral) {
					RDFSLiteral oldLiteral = (RDFSLiteral) oldValue;
					newValue = getOWLModel().createRDFSLiteral(str,
							oldLiteral.getDatatype());
				} else if (oldValue instanceof Boolean) {
					newValue = Boolean.valueOf(str.equals("true"));
				} else if (oldValue instanceof Float) {
					newValue = Float.valueOf(str);
				} else if (oldValue instanceof Integer) {
					newValue = Integer.valueOf(str);
				} else {
					newValue = str;
				}

				if (!subject.getPropertyValues(property).contains(newValue)) {
					removePropertyValue(property, oldValue);
					addPropertyValue(property, newValue);
				}
				// Note: The following line is not necessary since this method
				// is already removing and adding property value from the
				// statements above.
				// values.set(row, aValue);
				return newValue;
			} catch (NumberFormatException ex) {
				// Ignore illegal number format
			}
		}
		return oldValue;
	}

	public void setSubject(RDFResource instance) {

		subject = instance;

		init_properties.clear();
		init_values.clear();

		properties.clear();
		values.clear();

		if (instance != null) {

			initialize();
			refill();
		}
		fireTableDataChanged();
		// isModified = false;
	}

	private void initialize() {
		if (subject == null)
			return;
		selectFullSynsFromAllModelProps(getAllModelProperties(), false);
		editpanel.enableDelEditHB(1, false);
	}

	private void refill() {
		if (subject == null)
			return;
		selectFullSynsFromAllModelProps(getAllModelProperties(), true);
		if (values.size() > 0) {
			editpanel.enableDelEditHB(1, true);
		}
	}

	public Collection getProperties() {
		return properties;
	}
	
	public Collection getValues() {
		return values;
	}

	private RDFProperty[] allModelProps = null;

	protected RDFProperty[] getAllModelProperties() {
		if (allModelProps != null) {

		} else {
			OWLModel owlModel = subject.getOWLModel();
			allModelProps = ((Collection<? extends RDFProperty>) owlModel
					.getRDFProperties()).toArray(new RDFProperty[0]);
		}

		return allModelProps;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public String getFullSynValue(String value, int column) {
		if (value == null || value.equals(""))
			return "";

		value = ComplexPropertyParser.pipeDelim2XML(value);
		HashMap<String, String> hmap = ComplexPropertyParser.parseXML(value);
		String s = "";
		if (column == TERM_NAME)
			s = hmap.get("term-name");
		else if (column == TERM_GROUP)
			s = hmap.get("term-group");
		else if (column == TERM_SOURCE)
			s = hmap.get("term-source");
		else if (column == TERM_SOURCE_CODE)
			s = hmap.get("source-code");
		else if (column == LANGUAGE)
			s = hmap.get("xml:lang");
		return s != null ? s : "";
	}

	public RDFProperty getPredicate(int rowIndex) {
		return (RDFProperty) properties.get(rowIndex);
	}

	public OWLModel getOWLModel() {
		return subject.getOWLModel();
	}

	public void removePropertyValue(RDFProperty property, Object value) {
		int index = getPropertyValueRow(property, value);
		if (index >= 0 && index < getRowCount()) {
			properties.remove(index);
			values.remove(index);
		}
	}

	public int getPropertyValueRow(RDFProperty property, Object value) {
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty s = getPredicate(i);
			if (s.equals(property)) {
				Object ob = getValue(i);
				String tableStr;
				if (ob instanceof RDFSLiteral)
					tableStr = ((RDFSLiteral) ob).getString();
				else
					tableStr = (String) ob;

				if (value == null && ob == null) {
					return i;
				} else if (value != null && value.toString().equals(tableStr)) {
					return i;
				}
			}
		}
		return -1;
	}

	void updateValues() {
		int index = -1;
		if (table != null) {
			index = table.getSelectedRow();
		}
		properties.clear();
		values.clear();

		refill();
		fireTableDataChanged();
		if (table != null && index >= 0 && index < getRowCount()) {
			table.getSelectionModel().setSelectionInterval(index, index);
		} else if (table == null) {
			System.out.println("table == null");
		}
	}

	private String getStringValue(Object obj) {
		if (obj instanceof RDFSLiteral) {
			RDFSLiteral literal = (RDFSLiteral) obj;
			return literal.getString().trim();
		} else {
			return ((String) obj).trim();
		}
	}

	private void selectFullSynsFromAllModelProps(RDFProperty[] ss,
			boolean refill) {

		// Arrays.sort(ss, new FrameComparator());
		for (int i = 0; i < ss.length; i++) {
			RDFProperty property = ss[i];
			if (property.isAnnotationProperty()
					&& !(property instanceof DefaultOWLObjectProperty)) {
				if (userDefinedRelevantProperties.contains(property
						.getPrefixedName())) {
					if (property.isVisible()) {
						if (property.getPrefixedName().compareTo(
								NCIEditTab.ALTLABEL) == 0) {
							Collection values = subject
									.getPropertyValues(property);
							for (Iterator it = values.iterator(); it.hasNext();) {
								Object value = it.next();
								// this.init_properties.add(property);
								// this.init_values.add(value);
								String str_value = getStringValue(value);
								String s = ComplexPropertyParser
										.pipeDelim2XML(str_value);

								HashMap<String, String> hmap = ComplexPropertyParser
										.parseXML(s);
								if (hmap != null
										&& hmap.containsKey("term-name")) {
									addPropValue(property, value, refill);

								}

							}
						}
					}
				} else if (userDefinedRelevantProperties.size() == 0
						&& !excludedProperties
								.contains(property.getPrefixedName())) {
					if (property.isVisible()
							&& property.getPrefixedName().compareTo(SUBPROPERTYOF) != 0) {
						Collection values = subject.getPropertyValues(property);
						for (Iterator it = values.iterator(); it.hasNext();) {
							Object value = it.next();
							String s = ComplexPropertyParser
									.pipeDelim2XML(value.toString());
							HashMap<String, String> hmap = ComplexPropertyParser
									.parseXML(s);
							if (hmap != null && hmap.containsKey("term-name")) {

								addPropValue(property, value, refill);

							}
						}

					}
				}
			}
		}

	}

	private void addPropValue(RDFProperty property, Object value, boolean refill) {

		if (refill) {
			properties.add(property);
			values.add(value);

		} else {
			init_properties.add(property);
			init_values.add(value);
		}
	}

	public RDFResource getSubject() {
		return subject;
	}

	public Collection getInitialProperties() {
		return init_properties;
	}

	public Collection getInitialValues() {
		return init_values;
	}

	private boolean hasPropertyValue(RDFProperty property, Object value) {
		if (property == null || value == null)
			return false;

		int index = getPropertyValueRow(property, value);
		if (index >= 0 && index < getRowCount()) {
			return true;
		}
		return false;
	}

	public void deleteRow(int index) {
		if (index >= 0 && index < getRowCount()) {
			properties.remove(index);
			values.remove(index);
			fireTableDataChanged();
			editpanel.enableSaveButton(true, false);
		}
	}

	public int addRow(RDFProperty property, Object value) {
		if (!hasPropertyValue(property, value)) {
			addPropertyValue(property, value);
			// isModified = true;
			editpanel.enableSaveButton(true, false);
		}

		int index = getPropertyValueRow(property, value);
		return index;
	}

	public void addPropertyValue(RDFProperty property, Object value) {

		int index = getPropertyValueRow(property, value);
		if (index >= 0 && index < getRowCount()) {
			return;
		}
		properties.add(property);
		values.add(value);
		fireTableDataChanged();
		// isModified = true;
		// updateValues();
	}

	public boolean resetValue(String prop_name, Object prop_value_obj,
			Object new_prop_value_obj) {

		String prop_value = getStringValue(prop_value_obj);
		// String new_prop_value = getStringValue(new_prop_value_obj);

		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			// String name = property.getBrowserText();
			String name = property.getPrefixedName();

			String value = null;// (String) values.get(i);
			Object obj = values.get(i);

			if (obj instanceof RDFSLiteral) {
				RDFSLiteral obj_literal = (RDFSLiteral) obj;
				value = obj_literal.getString();
			} else {
				value = (String) obj;
			}

			if (name.compareToIgnoreCase(prop_name) == 0
					&& value.compareTo(prop_value) == 0) {
				values.set(i, new_prop_value_obj);
				fireTableDataChanged();
				// isModified = true;
				editpanel.enableSaveButton(true, false);
				return true;
			}
		}
		return false;
	}

	public EditPanel getEditPanel() {
		return editpanel;
	}

	public int getPtNciIndex() {
		int n = getRowCount();
		for (int i = 0; i < n; ++i) {
			String group = (String) getValueAt(i, TERM_GROUP);
			if (!(group.equals("PT") || group.equals("HD") || group.equals("AQ")))
				continue;
			String source = (String) getValueAt(i, TERM_SOURCE);
			if (!source.equals("NCI"))
				continue;
			return i;
		}
		return -1;
	}

	public String getPtNciTermName() {
		int i = getPtNciIndex();
		if (i < 0)
			return "";
		return (String) getValueAt(i, TERM_NAME);
	}
}
