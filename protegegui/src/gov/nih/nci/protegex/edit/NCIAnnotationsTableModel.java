package gov.nih.nci.protegex.edit;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;

import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.model.impl.XMLSchemaDatatypes;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.components.triples.TriplesComponent;
import edu.stanford.smi.protegex.owl.ui.metadata.AnnotationsWidgetPlugin;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.util.*;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIAnnotationsTableModel extends AbstractTableModel {

	public static final long serialVersionUID = 9934561210L;
	
	private Logger logger = Log.getLogger(getClass());

	/**
	 * The list of Properties currently displayed
	 */
	private ArrayList<RDFProperty> properties = new ArrayList<RDFProperty>();
	
	public boolean alreadyHas(String propname, String value) {
		for (int i = 0; i < properties.size(); i++) {
			String pn = properties.get(i).getBrowserText();
			if (pn.equalsIgnoreCase(propname)) {
				
				if (this.tablemodel_type == SIMPLE_PROPERTY_MODEL) {
					String ss = (String) values.get(i);
					if (ss.equalsIgnoreCase(value)) {
						return true;
					}
					
				}
				
				if (this.tablemodel_type == COMPLEX_PROPERTY_MODEL) {
					String ss = (String) this.value2xml_array.get(i);
					if (ss.equalsIgnoreCase(value)) {
						return true;
					}
					
				}
				
				if (this.tablemodel_type == OBJECT_PROPERTY_MODEL) {
					OWLNamedClass onc = (OWLNamedClass) values.get(i);
					if (onc.getBrowserText().equalsIgnoreCase(value)) {
						return true;
					}					
					
				} 				
				
			}
		}
		return false;
	}

	/**
	 * The resource being annotated
	 */
	private RDFResource subject;

	private JTable table;

	/**
	 * The initial list of Properties
	 */

	private Vector<String> userDefinedRelevantProperties;

	private Vector<String> excludedProperties;

	private boolean objectValuedOnly;

	private ArrayList<RDFProperty> init_properties = new ArrayList<RDFProperty>();

	private ArrayList<Object> init_values = new ArrayList<Object>();

	private final String SUBPROPERTYOF = "rdfs:subPropertyOf";

	OWLModel owlModel = null;

	int tablemodel_type = 0;

	boolean is_complex;

	boolean hasPropertyColumn = true;

	public static final int COL_DEFINITION_VALUE = 0;

	EditPanel edit_panel = null;

	int type;

	/**
	 * The values currently displayed in each row (either plain values or
	 * RDFObjects)
	 */
	private ArrayList<Object> values = new ArrayList<Object>();

	private ArrayList<String> value2xml_array = new ArrayList<String>();

	public final static int COL_PROPERTY = 0;

	public final static int COL_VALUE = 1;

	public final static int SIMPLE_PROPERTY_MODEL = 1;

	public final static int COMPLEX_PROPERTY_MODEL = 2;

	public final static int OBJECT_PROPERTY_MODEL = 4;

	int qualifier_tblmodel_id = -1;

	public NCIAnnotationsTableModel(OWLModel owlModel) {
		this.owlModel = owlModel;
		this.userDefinedRelevantProperties = new Vector<String>();
		this.excludedProperties = new Vector<String>();
		initialize();
	}

	public void setHasPropertyColumn(boolean hasPropertyColumn) {
		this.hasPropertyColumn = hasPropertyColumn;
	}

	public boolean getHasPropertyColumn() {
		return hasPropertyColumn;
	}

	public boolean isComplex() {
		return is_complex;
	}

	public int get_qualifier_tblmodel_id() {
		return qualifier_tblmodel_id;
	}

	public NCIAnnotationsTableModel(OWLModel owlModel, EditPanel edit_panel,
			RDFResource subject, Vector v, Vector w, boolean objectValueOnly,
			int qualifier_tblmodel_id, int type) {

		this.owlModel = owlModel;
		this.type = type;
		setModelType(type);

		this.edit_panel = edit_panel;
		this.userDefinedRelevantProperties = new Vector<String>();
		this.excludedProperties = new Vector<String>();
		this.objectValuedOnly = objectValueOnly;
		this.subject = subject;
		this.qualifier_tblmodel_id = qualifier_tblmodel_id;

		if (qualifier_tblmodel_id > 0) {
			is_complex = true;
		}

		for (int i = 0; i < v.size(); i++) {
			userDefinedRelevantProperties.add((String) v.elementAt(i));
		}
		for (int i = 0; i < w.size(); i++) {
			excludedProperties.add((String) w.elementAt(i));
		}

		if (subject != null) {

			initialize();
		}
		refill();
	}

	public void setModelType(int type) {
		tablemodel_type = type;
		if (tablemodel_type == COMPLEX_PROPERTY_MODEL) {
			is_complex = true;
		}
	}

	public EditPanel getEditPanel() {
		return edit_panel;
	}

	private void filterAllModelProperties(RDFProperty[] ss, boolean refill) {

		for (int i = 0; i < ss.length; i++) {
			RDFProperty property = ss[i];
			if (property.isAnnotationProperty()
					&& !(property instanceof DefaultOWLObjectProperty)) {

				if (userDefinedRelevantProperties.contains(property
						.getPrefixedName())) {
					if (property.isVisible()) {
						Collection c2 = subject.getPropertyValues(property);

						for (Iterator it = c2.iterator(); it.hasNext();) {
							Object value = it.next();

							addPropValue(property, value, refill);

						}
					}
				} else if (userDefinedRelevantProperties.size() == 0
						&& !excludedProperties
								.contains(property.getPrefixedName())) {
					if (property.getPrefixedName().compareTo(SUBPROPERTYOF) != 0) {
						Collection c2 = subject.getPropertyValues(property);

						if (property.getPrefixedName().indexOf("comment") != -1) {
							if (c2.size() == 0) {
								//addPropValue(property, "", refill);
							}
						}
						for (Iterator it = c2.iterator(); it.hasNext();) {
							Object value = it.next();
							addPropValue(property, value, refill);
						}
					}
				}
			}
		}
	}

	private void addPropValue(RDFProperty property, Object value, boolean refill) {
		if (refill) {
			properties.add(property);
			if (isComplex()) {
				this.values_add(value);
			} else {
				values.add(value);
			}
		} else {
			init_properties.add(property);
			init_values.add(value);
		}
	}

	private void initialize() {
		if (subject == null)
			return;

		edit_panel.enableDelEditHB(type, false);


		if (objectValuedOnly) {
			Vector<RDFProperty> v = getObjectProperties();
			for (int i = 0; i < v.size(); i++) {
				RDFProperty property = (RDFProperty) v.elementAt(i);
				Collection c = subject.getPropertyValues(property);
				for (Iterator it = c.iterator(); it.hasNext();) {
					Object value = it.next();
					this.init_properties.add(property);
					this.init_values.add(value);

				}
			}
			//v.clear();
		} else {

			filterAllModelProperties(getAllModelProperties(), false);

		}

	}

	public int addRow(RDFProperty property) {
		Object value = createDefaultValue(property);
		if (value != null) {
			return addRow(property, value);
		} else {
			return -1;
		}
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

	public void addPropertyValue(RDFProperty property, Object value) {
		int index = getPropertyValueRow(property, value);

		if (index >= 0 && index < getRowCount()) {
			logger.warning("addPropertyValue: property already exists -- nothing done.");
			return;
		}
		if (isComplex()) {
			properties.add(property);
			values_add(value);
		} else {
			properties.add(property);
			values.add(value);
		}

		updateValues();
	}

	public void removePropertyValue(RDFProperty property, Object value) {
		int index = getPropertyValueRow(property, value);
		if (index >= 0 && index < getRowCount()) {
			properties.remove(index);
			if (isComplex()) {
				values_remove(index);
			} else {
				values.remove(index);
			}
			updateValues();
		}
	}

	// ////////////////////////////////////////////////////////////////////
	public int addRow(RDFProperty property, Object value) {
		if (!hasPropertyValue(property, value)) {
			addPropertyValue(property, value);
		}
		int index = getPropertyValueRow(property, value);
		return index;
	}

	private Object createDefaultValue(RDFProperty property) {
		for (Iterator it = TriplesComponent.plugins(); it.hasNext();) {
			AnnotationsWidgetPlugin plugin = (AnnotationsWidgetPlugin) it
					.next();
			if (plugin.canEdit(subject, property, null)) {
				Object defaultValue = plugin.createDefaultValue(
						(RDFResource) subject, property);
				if (defaultValue != null) {
					return defaultValue;
				}
			}
		}
		RDFResource range = property.getRange();
		if (range instanceof RDFSDatatype) {
			return ((RDFSDatatype) range).getDefaultValue();
		} else if (range instanceof OWLDataRange) {
			OWLDataRange dataRange = (OWLDataRange) range;
			RDFList oneOf = dataRange.getOneOf();
			Object firstValue = null;
			if (oneOf != null) {
				Collection values = oneOf.getValues();
				if (!values.isEmpty()) {
					firstValue = values.iterator().next();
				}
			}
			return firstValue;
		} else if (property instanceof OWLObjectProperty) {
			return null;
		} else {
			return "";
		}
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

	private Object createNewValue(String text, String language) {
		Object newValue = null;
		if (language == null || language.trim().length() == 0) {
			newValue = text;
		} else {
			newValue = owlModel.createRDFSLiteralOrString(text, language);
		}
		return newValue;
	}

	public void deleteRow(int rowIndex) {
		RDFProperty property = getPredicate(rowIndex);
		Object value = getValue(rowIndex);

		removePropertyValue(property, value);
	}

	public Class<?> getColumnClass(int column) {
		if (hasPropertyColumn) {
			if (column == COL_PROPERTY) {
				return RDFProperty.class;
			} else if (column == COL_VALUE) {
				return Object.class;
			} else if (hasTypeColumn() && column == COL_VALUE + 1) {
				return RDFResource.class;
			} else {
				return String.class;
			}
		} else
			return String.class;
	}

	public int getColumnCount() {
		if (hasTypeColumn())
			return 4;
		else if (hasPropertyColumn) {
			if (type == OBJECT_PROPERTY_MODEL)
				return 2;
			else
				return 3;
		}

		return 2;
	}

	public String getColumnName(int column) {
		if (hasPropertyColumn) {
			if (column == COL_PROPERTY) {
				return "Property";
			} else if (column == COL_VALUE) {
				return "Value";
			} else if (isTypeColumn(column)) {
				return "Type";
			} else {
				return "Lang";
			}
		} else {
			if (column == 0)
				return "Value";
			else
				return "Lang";
		}
	}

	public Object getDisplayValue(int rowIndex) {
		Object value = values.get(rowIndex);
		if (value instanceof RDFSLiteral) {
			return ((RDFSLiteral) value).getString();
		}
		return value;
	}

	private String getLanguage(int row) {
		Object value = getValue(row);
		if (value instanceof RDFSLiteral) {
			return ((RDFSLiteral) value).getLanguage();
		} else if (value instanceof RDFResource) {
			return null;

		} else if (value instanceof String) {
		    return null;
		} else {
			HashMap<String, String> hmap = ComplexPropertyParser
					.parseXML((String) values_get(row));
			return (String) hmap.get("xml:lang");
		}
	}

	public OWLModel getOWLModel() {
		return owlModel;
	}

	public RDFProperty getPredicate(int rowIndex) {
		return (RDFProperty) properties.get(rowIndex);
	}

	public int getPropertyValueRow(RDFProperty property, Object value) {
		String val = ComplexPropertyParser.getText(value.toString());
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty s = getPredicate(i);
			if (s.equals(property)) {
				Object rowVal = getValue(i);
				if (val == null && rowVal == null)
					return i;

				String rowValStr = rowVal != null ? rowVal.toString() : "";
				if (val != null && val.equals(rowValStr))
					return i;
			}
		}
		return -1;
	}

	private RDFProperty[] sortedAllModelProps = null;

	@SuppressWarnings("deprecation")
	protected RDFProperty[] getAllModelProperties() {
		if (sortedAllModelProps != null) {

		} else {
			OWLModel owlModel = subject.getOWLModel();
			
			Collection coll = owlModel.getRDFProperties();
			
			sortedAllModelProps = new RDFProperty[coll.size()];
			
			coll.toArray(sortedAllModelProps);
			
			
			//sortedAllModelProps = (RDFProperty[]) owlModel.getSystemFrames()
					//.getOwlAnnotationPropertyClass().getInstances().toArray(
							//new RDFProperty[0]);
			Arrays.sort(sortedAllModelProps, new FrameComparator());
			
		}
		
		return sortedAllModelProps;

	}

	public int getRowCount() {
		return properties.size();
	}

	public RDFResource getSubject() {
		return subject;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (hasPropertyColumn) {
			if (columnIndex == COL_PROPERTY) {
				return getPredicate(rowIndex);
			} else if (columnIndex == COL_VALUE) {
				return getDisplayValue(rowIndex);
			} else if (isTypeColumn(columnIndex)) {
				Object value = getValue(rowIndex);
				if (value instanceof RDFResource) {
					return ((RDFResource) value).getRDFType();
				} else if (value instanceof RDFSLiteral) {
					return ((RDFSLiteral) value).getDatatype();
				} else {
					RDFSLiteral literal = DefaultRDFSLiteral.create(
							getOWLModel(), value);
					return literal.getDatatype();
				}
			} else {
				return getLanguage(rowIndex);
			}
		} else {
			if (columnIndex == 0)
				return getDisplayValue(rowIndex);
			else
				return getLanguage(rowIndex);
		}
	}

	public Object getValue(int rowIndex) {
		Object value = values.get(rowIndex);
		return value;
	}

	protected boolean hasTypeColumn() {
		return false;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (type == OBJECT_PROPERTY_MODEL)
			return false;

		if (columnIndex == getColumnCount() - 1)
			return true;
		return false;

	}

	public boolean isDeleteEnabled(int row) {
		RDFProperty predicate = getPredicate(row);
		Object object = getValue(row);
		return edu.stanford.smi.protegex.owl.ui.actions.triple.DeleteTripleAction
				.isSuitable(subject, predicate, object);
	}

	public static boolean isInvalidXMLLiteral(RDFProperty property, Object value) {
		if (XMLSchemaDatatypes.isXMLLiteralSlot(property)
				&& value instanceof String) {
			if (!XMLLiteralType.theXMLLiteralType.isValid((String) value)) {
				ProtegeUI.getModalDialogFactory().showErrorMessageDialog(
						property.getOWLModel(),
						"This value is not a valid XML literal:\n" + value);
				return true;
			}
		}
		return false;
	}

	protected boolean isRelevantProperty(RDFProperty property) {
		return true;
	}

	private boolean isTypeColumn(int column) {
		return hasTypeColumn() && column == COL_VALUE + 1;
	}

	private Vector<RDFProperty> allObjProps = null;

	private Vector<RDFProperty> getObjectProperties() {
		if (allObjProps == null) {
			Collection allProperties = owlModel
					.getVisibleUserDefinedRDFProperties();
			allObjProps = new Vector<RDFProperty>();
			for (Iterator it = allProperties.iterator(); it.hasNext();) {
				RDFProperty property = (RDFProperty) it.next();
				if (property.isAnnotationProperty()
						&& property.hasObjectRange()
						&& property instanceof DefaultOWLObjectProperty
						&& !excludedProperties
								.contains(property.getPrefixedName())) {
					allObjProps.add(property);
				}
			}

			allObjProps.add(owlModel.getOWLDifferentFromProperty());
			allObjProps.add(owlModel.getOWLDisjointWithProperty());
			allObjProps.add(owlModel.getOWLEquivalentPropertyProperty());
			allObjProps.add(owlModel.getOWLSameAsProperty());
			allObjProps.add(owlModel.getRDFSIsDefinedByProperty());

			RDFProperty seeAlsoSlot = owlModel
					.getRDFProperty(RDFSNames.Slot.SEE_ALSO);
			allObjProps.add(seeAlsoSlot);

		}

		return allObjProps;

	}

	private void refill() {
		if (subject == null)
			return;

		// For Assocations and other object valued properties

		if (objectValuedOnly) {

			Vector<RDFProperty> v = getObjectProperties();

			for (int i = 0; i < v.size(); i++) {
				RDFProperty property = (RDFProperty) v.elementAt(i);
				Collection c2 = subject.getPropertyValues(property);


				for (Iterator it = c2.iterator(); it.hasNext();) {
					Object value = it.next();

					this.properties.add(property);
					if (isComplex()) {
						this.values_add(value);

					} else {

						this.values.add(value);
					}
				}
			}
			//v.clear();
		} else {
			filterAllModelProperties(getAllModelProperties(), true);

		}

		if (values.size() > 0) {
			if (table != null) {
				table.getSelectionModel().setSelectionInterval(0, 0);
			}
			if (qualifier_tblmodel_id > 0) {
				edit_panel.updateQualifierTable(qualifier_tblmodel_id);
			}
			edit_panel.enableDelEditHB(type, true);
		}
	}

	private void setDatatype(int row, RDFSDatatype datatype) {
		Object oldValue = getValue(row);
		final String lexicalValue = oldValue.toString();
		RDFSLiteral newValue = datatype.getOWLModel().createRDFSLiteral(
				lexicalValue, datatype);
		RDFProperty predicate = getPredicate(row);

		removePropertyValue(predicate, oldValue);

		addPropertyValue(predicate, newValue);
	}

	private Object setLanguage(int row, String newLanguage) {
		RDFProperty property = getPredicate(row);
		Object text = getDisplayValue(row);

		if (isComplex()) {
			text = value2xml_array.get(row);
		}

		Object newValue = createNewValue(property, text.toString(), newLanguage);
		if (!subject.getPropertyValues(property).contains(newValue)) {
			Object oldValue = null;
			if (isComplex()) {
				oldValue = get_Value(row);
			} else {
				oldValue = getValue(row);
			}
			resetValue(property.getPrefixedName(), oldValue, newValue);
			table.getSelectionModel().setSelectionInterval(row, row);
		}

		return newValue;
	}

	public void setSubject(RDFResource instance) {
		
		this.subject = instance;

		init_properties.clear();
		init_values.clear();

		properties.clear();
		value2xml_array.clear();
		values.clear();


		initialize();
		refill();
		
		fireTableDataChanged();
	}

	public Object setValue(Object aValue, int row) {
		RDFProperty property = getPredicate(row);
		Object oldValue = getValue(row);
		if (oldValue == null || !oldValue.equals(aValue)) {
			try {
				Object newValue;
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
				if (aValue instanceof RDFSLiteral
						&& ((RDFSLiteral) aValue).getLanguage() != null) {
					newValue = aValue;
				} else if (lang != null) {
					newValue = createNewValue(property, str, lang);
				} else if (oldValue instanceof RDFSLiteral) {
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
				return newValue;
			} catch (NumberFormatException ex) {
				logger.warning(ex.getLocalizedMessage());
			}
		}
		return oldValue;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setValueAndGetIt(aValue, rowIndex, columnIndex);
	}

	public Object setValueAndGetIt(Object value, int row, int col) {
		if (col == getColumnCount() - 1)
			return setLanguage(row, (String) value);

		if (col == COL_PROPERTY) {
			return null;
		}
		if (col == COL_VALUE) {
			return setValue(value, row);
		} else if (isTypeColumn(col)) {
			if (value instanceof RDFSDatatype) {
				RDFSDatatype datatype = (RDFSDatatype) value;
				setDatatype(row, datatype);
				return datatype;
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * Removes all rows containing a given property and then re-adds them with
	 * the most recent values.
	 */

	void updateValues() {
		int index = -1;
		if (table != null) {
			index = table.getSelectedRow();
			logger.fine("table != null && index = " + index);
		}
		fireTableDataChanged();
		if (table != null && index >= 0 && index < getRowCount()) {
			table.getSelectionModel().setSelectionInterval(index, index);
		} else if (table != null && getRowCount() > 0) {
			table.getSelectionModel().setSelectionInterval(0, 0);
		}

	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public JTable getTable() {
		return table;
	}

	public Collection getProperties() {
		return properties;
	}

	public Collection getValues() {
		if (isComplex())
			return get_Values();
		return values;
	}

	public ArrayList<RDFProperty> getInitialProperties() {
		return init_properties;
	}

	public Collection getInitialValues() {
		return init_values;
	}

	private String getStringValue(Object obj) {
		if (obj instanceof RDFSLiteral) {
			RDFSLiteral literal = (RDFSLiteral) obj;
			return literal.getString();
		} else {
			return (String) obj;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean resetValue(String prop_name, Object prop_value_obj,
			Object new_prop_value_obj) {
		// String prop_value = (String) prop_value_obj;
		String prop_value = getStringValue(prop_value_obj);
		// String new_prop_value = getStringValue(new_prop_value_obj);

		for (int i = 0; i < properties.size(); i++) {
			RDFProperty property = (RDFProperty) properties.get(i);
			if (!(property instanceof DefaultOWLObjectProperty)) {
				// String name = property.getBrowserText();
				String name = property.getPrefixedName();
				Object value = null;
				if (isComplex()) {
					value = (Object) values_get(i);
				} else {
					value = values.get(i);
				}
				String str_value = getStringValue(value);

				if (name.compareToIgnoreCase(prop_name) == 0
						&& str_value.compareToIgnoreCase(prop_value) == 0) {
					if (isComplex()) {
						String text = ComplexPropertyParser.getText(str_value);
						// Note: The following line did not remove the
						// definition
						// complex property since it is store as XML tag in
						// value2xml_map.
						// Fix: Removing via str_value instead of text variable.
						// value2xml_map.remove(text);
						value2xml_array.remove(str_value);
						text = ComplexPropertyParser
								.getText(getStringValue(new_prop_value_obj));
						// value2xml_map.put(text, new_prop_value_obj);
						value2xml_array.add(i,
								getStringValue(new_prop_value_obj));
						// String language = getLanguage(i);
						// 010307
						String language = "en";
						if (new_prop_value_obj instanceof RDFSLiteral) {
							language = ((RDFSLiteral) new_prop_value_obj)
									.getLanguage();
							if (language == null || language.compareTo("") == 0)
								language = "en";
							Object obj = createNewValue(text, language);
							values.set(i, obj);
						} else {
							values.set(i, text);
						}
					} else {
						values.set(i, new_prop_value_obj);
					}

					// updateValues();

					fireTableDataChanged();
					return true;
				}

			}
		}
		return false;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getObjectValue(Object obj) {
		if (obj == null)
			return null;
		Object newValue;
		String str = obj.toString();
		if (obj instanceof RDFSLiteral) {
			RDFSLiteral oldLiteral = (RDFSLiteral) obj;
			newValue = owlModel
					.createRDFSLiteral(str, oldLiteral.getDatatype());
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

	private void values_add(Object value) {
		// String text = ComplexPropertyParser.getText((String) value);
		String text = ComplexPropertyParser.getText(value.toString());

		value2xml_array.add(value.toString());

		if (value instanceof RDFSLiteral) {
			RDFSLiteral literal = (RDFSLiteral) value;
			String language = literal.getLanguage();
			values.add(createNewValue(text, language));
		} else {
			values.add(text);
		}
	}

	// TODO: Bob, this is flawed, using a hash fails when tables have dup values
	private Object values_get(int i) {
		// 080506
		// String text = (String) values.get(i);

		Object obj = values.get(i);

		// obj = value2xml_map.get(text);
		if (value2xml_array.size() > i) {
			obj = value2xml_array.get(i);
		}
		if (obj == null)
			return null;

		// return getObjectValue(obj);
		return obj;
	}

	public Object get_Value(int i) {
		return values_get(i);
	}

	public ArrayList get_Values() {
		ArrayList<Object> curr_values = new ArrayList<Object>();
		for (int i = 0; i < values.size(); i++) {
			curr_values.add(values_get(i));
		}
		return curr_values;
	}

	private void values_remove(int i) {

		value2xml_array.remove(i);
		values.remove(i);
	}

	public String getPropertyName(int row) {
		RDFProperty property = getPredicate(row);
		// return property.getBrowserText();
		return property.getBrowserText();
	}

	public String getPropertyValue(int row) {
		String text = (String) values.get(row);
		return text;
	}

	public RDFProperty getProperty(int row) {
		RDFProperty property = getPredicate(row);
		// return property.getBrowserText();
		return property;
	}

	public void updateRDFSLabel(String pt) {
		ArrayList<RDFProperty> list = getInitialProperties();
		int n = list.size();
		for (int i = 0; i < n; ++i) {
			RDFProperty property = (RDFProperty) list.get(i);
			String name = property.getBrowserText();
			if (name.equals("rdfs:label")) {
				setValueAt(pt, i, COL_VALUE);
				return;
			}
		}
	}

}
