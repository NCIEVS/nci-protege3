package gov.nih.nci.protegex.tree;

import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import gov.nih.nci.protegex.edit.NCIEditTab;

import edu.stanford.smi.protege.util.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class TreeItem {

	public static enum TreeItemType {
		TYPE_NOT_SET, TYPE_CONCEPT, TYPE_PROPERTY, TYPE_PARENT, TYPE_RESTRICTION, TYPE_ASSOCIATION
	}

	private Logger logger = Log.getLogger(getClass());

	private TreeItemType _type = TYPE_NOT_SET;

	private String _name = "";

	private String _value = "";

	private String _restriction = "";

	private String _modifier = "";

	private int _cardinality = -1; // cardinality constraint filler

	private String _nameValue = ""; // cardinality constraint filler

	private String _language;

	private boolean _isDefining = false;

	private RDFSClass _class = null;

	private RDFProperty _property = null;

	public TreeItem() {
		debug("----------");
		debug("Method: Default Constructor");
	}

	public TreeItem(TreeItemType type, String name, String value,
			String restriction, String modifier, int cardinality) {
		debug("----------");
		debug("Method: Constructor with arguments");
		setType(type);
		setName(name);
		setValue(value);
		setRestriction(restriction);
		setModifier(modifier);
		setCardinality(cardinality);
		setNameValue(name, value);
		setIsDefining(false);
		setCls(null);
		setProperty(null);
	}

	private void debug(String text) {
		logger.log(Level.FINE, "TreeItem: " + text);
	}

	public RDFSClass getCls() {
		return _class;
	}

	public void setCls(RDFSClass aClass) {
		_class = aClass;

		debug("class: " + _class);
	}

	public RDFProperty getProperty() {
		return _property;
	}

	public void setProperty(RDFProperty aProperty) {
		_property = aProperty;
		debug("property: " + _property);
	}

	public void setLanguage(String language) {
		_language = language;
		debug("language: " + _language);
	}

	public String getLanguage() {
		return _language;
	}

	public void setIsDefining(boolean defining) {
		_isDefining = defining;
	}

	public boolean getIsDefining() {
		return _isDefining;
	}

	public void setType(TreeItemType type) {
		_type = type;
		debug("type: " + type.toString());
	}

	public TreeItemType getType() {
		return _type;
	}

	public void setName(String name) {
		_name = name;
		debug("name: " + _name);
	}

	public String getName() {
		return _name;
	}
	
	
	public String getDisplayName() {
		if (this._property != null) {
			return _property.getBrowserText();
		}
		if (this._class != null) {
			return _class.getBrowserText();
		}
		return this._nameValue.substring(0, _nameValue.indexOf(":"));
	}
	

	public void setValue(String value) {
		_value = value;
		debug("value: " + _value);
	}

	public String getValue() {
		return _value;
	}

	private String valueToString(Object obj) {
		if (obj == null)
			return "";
		if (obj instanceof String)
			return (String) obj;

		Object newValue;
		String str = obj.toString();

		if (obj instanceof RDFSLiteral) {
			newValue = (RDFSLiteral) obj;

			RDFSLiteral literal = (RDFSLiteral) obj;
			if (literal.getLanguage() == null
					|| literal.getLanguage().compareTo("") == 0) {
				str = str.substring(2, str.length());
			} else if (str.length() > 6) { // if (str.indexOf("~#") != -1)
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

	public void setRestriction(String restriction) {
		_restriction = restriction;
		debug("restriction: " + _restriction);
	}

	public String getRestriction() {
		return _restriction;
	}

	public void setModifier(String modifier) {
		_modifier = modifier;
		debug("modifier: " + _modifier);
	}

	public String getModifier() {
		return _modifier;
	}

	public void setCardinality(int cardinality) {
		_cardinality = cardinality;
		debug("cardinality: " + _cardinality);
	}

	public int getCardinality() {
		return _cardinality;
	}

	public void setNameValue(String nameValue) {
		_nameValue = nameValue;
		debug("nameValue: " + _nameValue);
	}

	public void setNameValue(String name, Object value) {
		String s = valueToString(value);

		if (_value == null || _value.length() <= 0)
			setValue(s);
		
		//setName(name);

		_nameValue = name + ": " + s;
		debug("nameValue[:]: " + _nameValue);
	}

	public String getNameValue() {
		return _nameValue;
	}

	public String toString() {
		return _nameValue;
	}

	/**
	 * Returns the key. Checks the type restriction first.
	 * 
	 * @return the key.
	 */
	public String getKey_withRestriction() {
		if (getType() != TYPE_RESTRICTION)
			return toString();
		String key = getCls().getBrowserText();
		if (getIsDefining())
			key = key + "(defining)";
		return key;
	}

	/**
	 * Returns the key.
	 * 
	 * @return the key.
	 */
	public String getKey() {
		TreeItemType type = getType();
		String name = getName();
		if (name.compareTo("rdfs:label") == 0 || name.compareTo("code") == 0
				|| name.compareTo("hasType") == 0) {
			type = TYPE_CONCEPT;
		}

		String key = "" + type + toString();
		return key.toUpperCase();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof TreeItem))
			return false;

		TreeItem other = (TreeItem) obj;
		return (_type == other._type && namesEqual(_name, other._name)
				&& _value.equals(other._value)
				&& _restriction.equalsIgnoreCase(other._restriction)
				&& _modifier.equalsIgnoreCase(other._modifier)
				&& _cardinality == other._cardinality
				&& namesEqual(_nameValue,other._nameValue)); 
				//&& _isDefining == other._isDefining);
	}
	
	private boolean namesEqual(String s1, String s2) {
		if (s1.equalsIgnoreCase(s2)) {
			return true;
		}
		if ((s1.equalsIgnoreCase("owl:subClassOf") && s2.equalsIgnoreCase("owl:equivalentClass")) ||
				(s2.equalsIgnoreCase("owl:subClassOf") && s1.equalsIgnoreCase("owl:equivalentClass"))) {
			return true;
		} else {
			return false;
		}
	}
	
	

	public TreeItem cloneTreeItem(boolean cloneDefining) {
		debug("----------");
		debug("Method: cloneTreeItem");
		TreeItem newitem = new TreeItem();
		newitem.setType(_type);
		newitem.setName(_name);
		newitem.setValue(_value);
		newitem.setRestriction(_restriction);
		newitem.setProperty(_property);
		newitem.setModifier(_modifier);
		newitem.setCardinality(_cardinality);
		newitem.setNameValue(_nameValue);

		// when cloning a restriction always make roles primitive
		if (cloneDefining) {
			newitem.setIsDefining(_isDefining);
		} else {
			newitem.setIsDefining(false);
		}
		newitem.setLanguage(_language);
		newitem.setCls(_class);
        

		return newitem;
	}
}
