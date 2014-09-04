package gov.nih.nci.protegex.tree;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItem.TreeItemType;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.edit.Property;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Contains a list of TreeItems.
 * 
 * @author David Yee
 */
public class TreeItems implements Cloneable {
	
	private Logger logger = Log.getLogger(getClass());
	// Member Variables:
	private Vector<TreeItem> _vector = new Vector<TreeItem>();

	private OWLWrapper _wrapper = OWLWrapper.getInstance();

	/**
	 * Constructs this class
	 */
	public TreeItems() {
	}

	/**
	 * Adds a TreeItem to the list.
	 * 
	 * @param item
	 *            The TreeItem.
	 */
	public void add(TreeItem item) {
		if (item == null)
			return;
		_vector.add(item);
	}

	/**
	 * Clears all the TreeItems.
	 */
	public void clear() {
		_vector.clear();
	}

	/**
	 * Returns the number of TreeItems stored.
	 * 
	 * @return the number of TreeItems stored.
	 */
	public int size() {
		return _vector.size();
	}

	/**
	 * Returns the ItemItem at the specified index position.
	 * 
	 * @param index
	 *            The index position.
	 * @return the ItemItem at the specified index position.
	 */
	public TreeItem elementAt(int index) {
		return _vector.elementAt(index);
	}

	/**
	 * Inserts a TreeItem at the specified index position.
	 * 
	 * @param item
	 *            the TreeItem.
	 * @param index
	 *            The index position.
	 */
	public void insertElementAt(TreeItem item, int index) {
		_vector.insertElementAt(item, index);
	}

	/**
	 * Removes the TreeItem at the specified index position.
	 * 
	 * @param index
	 *            The index position.
	 * @return the ItemItem at the specified index position.
	 */
	public TreeItem remove(int index) {
		if (index < 0)
			return null;
		return _vector.remove(index);
	}

	/**
	 * Returns an array list of TreeItems.
	 * 
	 * @return an array list of TreeItems.
	 */
	public TreeItem[] toArray() {
		return _vector.toArray(new TreeItem[_vector.size()]);
	}

	public TreeItem[] toArray(int size) {
		return _vector.toArray(new TreeItem[size]);
	}

	/**
	 * Returns the iterator.
	 * 
	 * @return the iterator.
	 */
	public Iterator<TreeItem> iterator() {
		return _vector.iterator();
	}

	/**
	 * Clones this object.
	 * 
	 * @return the cloned object.
	 */
	public TreeItems clone() {
		TreeItems clone = new TreeItems();
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			TreeItem cloneItem = item.cloneTreeItem(true);
			if (cloneItem == null)
				return null;
			clone.add(cloneItem);
		}
		return clone;
	}

	/**
	 * Prints a FYI (or info) text message.
	 * 
	 * @param text
	 *            The text string.
	 */
	public void fyi(String text) {
		logger.fine(text);
	}

	/**
	 * Prints a warning text message.
	 * 
	 * @param text
	 *            The text string.
	 */
	private void warning(String text) {
		logger.warning(text);
	}

	/**
	 * Prints a debug text message.
	 * 
	 * @param text
	 *            The text string.
	 */
	private void debug(String text) {
		logger.finer(text);
	}

	/**
	 * Prints the TreeItems values.
	 */
	public void fyi() {
		Iterator<TreeItem> iterator = this.iterator();
		while (iterator.hasNext())
			fyi("  " + iterator.next().toString());
	}

	/**
	 * Returns true if specified string value exists.
	 * 
	 * @param value
	 *            The string value.
	 * @return true if specified string value exists.
	 */
	public boolean contains(String value) {
		Iterator<TreeItem> iterator = this.iterator();
		while (iterator.hasNext()) {
			TreeItem item = (TreeItem) iterator.next();
			if (item.toString().equals(value))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if item name exists.
	 * 
	 * @param type
	 *            The type.
	 * @param name
	 *            The item's name.
	 * @return true if item name exists.
	 */
	public boolean containsName(TreeItemType type, String name) {
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			if (item.getType() == type && item.getName().compareTo(name) == 0)
				return true;
		}
		return false;
	}

	/**
	 * Returns true if item value exists.
	 * 
	 * @param type
	 *            The type.
	 * @param value
	 *            The item's value.
	 * @return true if item value exists.
	 */
	public boolean containsValue(TreeItemType type, String value) {
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			if (item.getType() == type && item.getValue().compareTo(value) == 0)
				return true;
		}
		return false;
	}

	/**
	 * Returns the concept class name.
	 * 
	 * @return the concept class name.
	 */
	public String getClsName() {
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			if (item.getType() == TYPE_CONCEPT) {
				return item.getName();
			}
		}
		return null;
	}

	/**
	 * Returns converted complex property with NCI|PT to NCI|SY.
	 * 
	 * @return converted complex property with NCI|PT to NCI|SY.
	 */
	public TreeItems convertNCIPT2NCISY() {
		TreeItems convertedItems = new TreeItems();

		String prop_name = NCIEditTab.ALTLABEL;
		for (int i = 0; i < this.size(); i++) {
			TreeItem item0 = (TreeItem) this.elementAt(i);
			TreeItem item = item0.cloneTreeItem(false);

			if (item.getName().compareTo(prop_name) == 0) {
				HashMap<String, String> hmap = ComplexPropertyParser
						.parseXML(ComplexPropertyParser.pipeDelim2XML(item
								.getValue()));
				String term_name = (String) hmap.get("term-name");
				String term_group = (String) hmap.get("term-group");
				String term_source = (String) hmap.get("term-source");
				String source_code = (String) hmap.get("source-code");

				if (term_source == null)
					term_source = "NCI";
				if (term_group == null)
					term_group = "SY";
				// TODO: Dave, this really messes up split
				if (term_source.compareTo("NCI") == 0
						&& isEquivalentToPT(term_group))
					term_group = "SY";
				
				ArrayList<String> vals = new ArrayList<String>();
				vals.add(term_name);
				vals.add(term_group);
				vals.add(term_source);
				vals.add(source_code);
				vals.add("en");
				String value = NCIEditTab.getSerializedCustomizedAnnotationData(prop_name,vals); 

				
				item.setValue(value);
				item.setNameValue(item0.getDisplayName(), value);
			}
			if (!convertedItems.contains(item.toString()))
				convertedItems.add(item);
		}
		return convertedItems;
	}

	/**
	 * Returns true if termType is either PT, HD, or AQ.
	 * 
	 * @param termType
	 *            The term type.
	 * @return true if termType is either PT, HD, or AQ.
	 */
	private boolean isEquivalentToPT(String termType) {
		return termType.equals("PT") || termType.equals("HD")
				|| termType.equals("AQ");
	}

	/**
	 * Returns items by matching type.
	 * 
	 * @param type
	 *            The type.
	 * @return items by matching type.
	 */
	public TreeItems getByType(TreeItemType type) {
		TreeItems items = new TreeItems();
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			if (item.getType() == type)
				items.add(item);
		}
		return items;
	}

	/**
	 * Returns items by matching type except those from the exclusion list.
	 * 
	 * @param type
	 *            The type.
	 * @param exclusion
	 *            The exclusion list.
	 * @return items by matching type except those from the exclusion list.
	 */
	public TreeItems getByType(TreeItemType type, Vector<String> exclusion) {
		TreeItems items = new TreeItems();
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = (TreeItem) this.elementAt(i);
			if (item.getType() == type && !exclusion.contains(item.getName()))
				items.add(item);
		}
		return items;
	}

	/**
	 * Returns the state map.
	 * 
	 * @return the state map.
	 */
	public HashMap<String, TreeItem> getStateMap() {
		HashMap<String, TreeItem> hashmap = new HashMap<String, TreeItem>();
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = (TreeItem) this.elementAt(i);
			String key = getItemKey(item);
			if (!hashmap.containsKey(key))
				hashmap.put(key, item);
		}
		return hashmap;
	}

	/**
	 * Returns the item's key.
	 * 
	 * @param item
	 *            The TreeItem.
	 * @return the item's key.
	 */
	private String getItemKey(TreeItem item) {
		if (item.getIsDefining())
			return item.toString() + "(defining)";
		return item.toString();
	}

	/**
	 * Returns a list of differences.
	 * 
	 * @param other
	 *            The other TreeItems.
	 * @return a list of differences.
	 */
	public TreeItems getDiffs(TreeItems other) {

		

		Iterator<TreeItem> other_items = other.iterator();

		TreeItems result = new TreeItems();

		while (other_items.hasNext()) {
			TreeItem item = other_items.next();
			if (_vector.contains(item)) {

			} else {
				result.add(item);
			}

		}

		return result;

	}

	public TreeItems getMods(TreeItems other) {

		Iterator<TreeItem> items = _vector.iterator();

		TreeItems result = new TreeItems();

		while (items.hasNext()) {
			TreeItem item = items.next();

			Iterator<TreeItem> other_items = other.iterator();
			while (other_items.hasNext()) {
				TreeItem oitem = (TreeItem) other_items.next();
				if (item.equals(oitem)) {
					// check if defining change
					if (item.getIsDefining() != oitem.getIsDefining()) {
						result.add(item);
					}
				}
			}

		}

		return result;

	}

	/**
	 * Returns a list of differences by type.
	 * 
	 * @param other
	 *            The other TreeItems.
	 * @param type
	 *            The type.
	 * @return a list of differences by type.
	 */
	public TreeItems getDiffsByType(TreeItems other, TreeItemType type) {
		TreeItems thisList = this.getByType(type);
		TreeItems otherList = other.getByType(type);
		TreeItems diffs = thisList.getDiffs(otherList);
		return diffs;
	}

	/**
	 * Returns a list of differences by type except those from the exclusion
	 * list.
	 * 
	 * @param other
	 *            The other TreeItems.
	 * @param type
	 *            The type.
	 * @param exclusion
	 *            The exclusion list.
	 * @return a list of differences by type except those from the exclusion
	 *         list.
	 */
	public TreeItems getDiffsByType(TreeItems other, TreeItemType type,
			Vector<String> exclusion) {
		TreeItems thisList = this.getByType(type);
		TreeItems otherList = other.getByType(type, exclusion);
		TreeItems diffs = thisList.getDiffs(otherList);
		return diffs;
	}

	/**
	 * Diffs the final states of the concepts loaded in two TreePanels.
	 * 
	 * @param panel1
	 *            First TreePanel.
	 * @param panel2
	 *            Second TreePanel.
	 * @return A vector containing two vectors. The first vector is the inserted
	 *         items. The second contains the deleted items.
	 */
	public static Vector<TreeItems> diffData(TreeItems finalState1,
			TreeItems finalState2) {

		TreeItems inserted = finalState1.getDiffs(finalState2);
		TreeItems deleted = finalState2.getDiffs(finalState1);

		Vector<TreeItems> results = new Vector<TreeItems>();
		results.add(inserted);
		results.add(deleted);
		return results;
	}

	/**
	 * Deletes property items to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @return true if successful.
	 */
	

	/**
	 * Adds property items to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @return true if successful.
	 */
	public boolean addItems(OWLWrapper wrapper, Cls cls) {
		if (cls == null)
			return false;
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			boolean retval = addItem(wrapper, cls, item.cloneTreeItem(false));
			if (!retval)
				return false;
		}
		return true;
	}

	/**
	 * Adds property item to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @param item
	 *            The property item.
	 * @return true if successful.
	 */
	private boolean addItem(OWLWrapper wrapper, Cls cls, TreeItem item) {
		if (cls == null || item == null)
			return false;

		if (item.getType() == TYPE_PROPERTY) {
			wrapper.addAnnotationProperty((OWLNamedClass) cls, item.getName(),
					item.getValue());
		}

		else if (item.getType() == TYPE_RESTRICTION) {
		    RDFSClass rdfsCls = (RDFSClass) item.getCls();


            if (item.getIsDefining()) {
                return wrapper.addEquivalentDefinition((OWLNamedClass) cls, rdfsCls);

            } else {
                return wrapper.addRestriction((OWLNamedClass) cls, rdfsCls);
            }
            
		}

		else if (item.getType() == TYPE_ASSOCIATION) {
			boolean retval = wrapper.addObjectProperty((OWLNamedClass) cls,
					item.getName(), item.getValue());

			if (!retval) {
				fyi("WARNING: Error encountered while adding association to "
						+ cls.getBrowserText());
				return false;
			}
		}

		else if (item.getType() == TYPE_PARENT) {
			boolean retval = false;

			OWLNamedClass supCls = (OWLNamedClass) item.getCls();

			if (item.getIsDefining()) {
				retval = wrapper.addEquivalentClass((OWLNamedClass) cls,
						(RDFSClass) supCls);
			} else {
				retval = wrapper.addDirectSuperclass(cls, supCls);
			}

			if (!retval) {
				fyi("WARNING: Error encountered while adding parent concept to "
						+ cls.getBrowserText());
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds property items to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @return true if successful.
	 */
	public boolean addInsertedItems(OWLWrapper wrapper, OWLNamedClass cls) {
		fyi("TreeItems.addInsertedItems");
		boolean retVal = true;
		for (Iterator<TreeItem> iter = this.iterator(); iter.hasNext();) {
			TreeItem item = (TreeItem) iter.next();

			// Note: Skips creating a new RDFLABEL (rdfs:label) property
			// because PREFLABEL (Preferred_Name) creates one.
			if (item.getName().equals(NCIEditTab.RDFLABEL)
					&& containsName(TYPE_PROPERTY, NCIEditTab.PREFLABEL)) {
				fyi("  * item [Skipped]: " + item.getNameValue());
				continue;
			}

			fyi("  * item: " + item.getNameValue());
			boolean success = addInsertedItem(wrapper, cls, item);
			if (!success)
				warning("Errors adding " + item);
			retVal = retVal & success;
		}
		return retVal;
	}

	/**
	 * Adds property item to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @param item
	 *            The property item.
	 * @return true if successful.
	 */
	private boolean addInsertedItem(OWLWrapper wrapper, OWLNamedClass cls,
			TreeItem item) {
		if (cls == null || item == null)
			return false;

		if (item.getType() == TYPE_PROPERTY) {
			if (item.getName().equalsIgnoreCase(NCIEditTab.PREFLABEL)) {
				// if preferred name changes then automatically change rdfs
				// label provide we are using nci rules
				if (NCIEditTab.config.getUseRules().equalsIgnoreCase("nci")) {
				wrapper.addAnnotationProperty(cls, NCIEditTab.RDFLABEL, item
						.getValue());
				}
			}
			wrapper.addAnnotationProperty(cls, item.getName(), item.getValue());
			return true;
		}

		if (item.getType() == TYPE_RESTRICTION) {
			RDFSClass rdfsCls = (RDFSClass) item.getCls();

			// rdfsCls = rdfsCls.createClone();
			

			if (item.getIsDefining()) {
				return wrapper.addEquivalentDefinition(cls, rdfsCls);

			} else {
				return wrapper.addRestriction(cls, rdfsCls);
			}
		}

		if (item.getType() == TYPE_ASSOCIATION) {
			return wrapper.addObjectProperty(cls, item.getName(), item
					.getValue());
		}

		if (item.getType() == TYPE_PARENT) {
			RDFSClass supCls = (RDFSClass) item.getCls();

			supCls = supCls.createClone();

			if (item.getIsDefining()) {
				return wrapper.addEquivalentDefinition((OWLNamedClass) cls,
						(RDFSClass) supCls);

			} else {

				return wrapper.addDirectSuperclass(cls, supCls);
			}
		}
		return false;
	}

	/**
	 * Deletes property items to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @return true if successful.
	 */
	public boolean removeDeletedItems(OWLWrapper wrapper, OWLNamedClass cls) {
		fyi("TreeItems.removeDeletedItems");
		boolean retVal = true;
		for (Iterator<TreeItem> iter = this.iterator(); iter.hasNext();) {
			TreeItem item = (TreeItem) iter.next();
			fyi("  * item: " + item.getNameValue());
			boolean success = removeDeletedItem(wrapper, cls, item);
			if (!success)
				warning("Errors removing " + item);
			retVal = retVal & success;
		}
		return retVal;
	}

	/**
	 * Delete property item to the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @param item
	 *            The property item.
	 * @return true if successful.
	 */
	private boolean removeDeletedItem(OWLWrapper wrapper, OWLNamedClass cls,
			TreeItem item) {
		if (cls == null || item == null)
			return false;

		if (item.getType() == TYPE_PROPERTY) {
			// delete property
			String propertyname = item.getName();
			String propertyvalue = item.getValue();
			if (wrapper.removeAnnotationProperty(cls, propertyname,
					propertyvalue))
				return true;

			// Note: Some associations are TYPE_PROPERTY while others are
			// TYPE_ASSOCIATION. For association, we need to retrieve
			// the internal name of the property object and not its
			// browsertext that is stored within item.getValue().
			RDFProperty property = item.getProperty();
			Object res = cls.getPropertyValue(property);
			propertyvalue = wrapper.getObjectValue(res);
			return wrapper.removeAnnotationProperty(cls, propertyname,
					propertyvalue);
		}

		else if (item.getType() == TYPE_RESTRICTION) {
			if (item.getIsDefining())
				return wrapper
						.removeEquivalentDefinitionNew(cls, item.getCls());
			else
				wrapper.removeRestriction((OWLNamedClass) cls, item.getCls());
		}

		else if (item.getType() == TYPE_ASSOCIATION) {

			//String assocname = item.getProperty().getName();
			String assocname = item.getProperty().getPrefixedName();
			String assocvalue = item.getValue();
			return wrapper.removeObjectProperty(cls, assocname, assocvalue);
		}

		else if (item.getType() == TYPE_PARENT) {
			if (item.getIsDefining()) {
				// KLO, 040407
				// return wrapper.removeEquivalentClass(cls, item.getCls());
				return wrapper.removeEquivalentDefinitionNew(cls, item.getCls());
			} else {
				return wrapper.removeDirectSuperclass(cls, item.getCls());
			}
		}

		return true;
	}

	/**
	 * Returns a list of properties.
	 * 
	 * @return a list of properties.
	 */
	public Vector<Property> getProperties() {
		Vector<Property> properties = new Vector<Property>();
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			if (item.getType() == TYPE_PROPERTY) {
				Property p = new Property(item.getName(), item.getValue());
				properties.add(p);
			}
		}
		return properties;
	}

	/**
	 * Returns a list of property values corresponding the the property name.
	 * 
	 * @param propertyName
	 *            The property name.
	 * @return a list of property values corresponding the the property name.
	 */
	public Vector<String> getPropertyValues(String propertyName) {
		Vector<String> list = new Vector<String>();
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = this.elementAt(i);
			if (item.getType() == TYPE_PROPERTY
					&& item.getName().compareTo(propertyName) == 0)
				list.add(item.getValue());
		}
		return list;
	}

	/**
	 * Debugs the state of the object.
	 * 
	 * @param label
	 *            Displays a label for this output. If null or blank, label will
	 *            not be displayed.
	 */
	public void debugState(String label) {
		if (label != null && label.length() > 0)
			debug("TreeItems: " + label);
		for (int i = 0; i < this.size(); i++) {
			TreeItem item = (TreeItem) this.elementAt(i);
			debug("  * Type: " + item.getType());
			debug("  * Name: " + item.getName());
			debug("  * Value: " + item.getValue());
			debug("  * toString: " + item.toString());

			if (item.getCls() != null) {
				debug("  * aClass: " + item.getCls().getBrowserText());
				debug("  * defining: " + item.getIsDefining());
			}

			if (item.getModifier().compareTo("") != 0)
				debug("  * Modifier: " + item.getModifier());
			debug("");
		}
	}

	/**
	 * Sorts the items.
	 * 
	 * @return an array of sorted items.
	 */
	public TreeItem[] sort() {
		TreeItem[] a = this.toArray();
		if (a == null || a.length == 0)
			return a;

		boolean done = false;
		while (!done) {
			done = true;
			for (int i = 0; i < a.length - 1; i++) {
				TreeItem item1 = (TreeItem) a[i];
				TreeItem item2 = (TreeItem) a[i + 1];
				String key_1 = item1.getKey();
				String key_2 = item2.getKey();

				if (key_1.compareTo(key_2) > 0) {
					done = false;
					TreeItem tmp = a[i];
					a[i] = a[i + 1];
					a[i + 1] = tmp;
				}
			}
		}
		return a;
	}

	public void synchronizePreferredName(String pt) {
		
		

		Iterator<TreeItem> iterator = this.iterator();
		while (iterator.hasNext()) {
			TreeItem item = iterator.next();
			String name = item.getName();
			String value = item.getValue();
			if (value.equals(pt))
				continue;
			if (item.getType() == TYPE_CONCEPT) {
				item.setNameValue(pt);
			}
			if (name.equals("rdfs:label") || name.equals(NCIEditTab.PREFLABEL)) {
				item.setValue(pt);
				item.setNameValue(name, pt);
			}
			
		}
	}
	
	public void synchronizePreferredNameFullSyn(String pt) {

		Iterator<TreeItem> iterator = this.iterator();
		while (iterator.hasNext()) {
			TreeItem item = iterator.next();
			String name = item.getName();
			String value = item.getValue();
			if (value.equals(pt))
				continue;
			if (item.getType() == TYPE_CONCEPT) {
				item.setNameValue(pt);
			}
			if (name.equals("rdfs:label")) {
				item.setValue(pt);
				item.setNameValue(name, pt);
			}
			
			if (name.equals(NCIEditTab.ALTLABEL)) {
				String term_name = ComplexPropertyParser
						.getPtNciTermName(value);
				if (term_name == null || term_name.equals(pt))
					continue;
				HashMap<String, String> hm = ComplexPropertyParser
						.parseXML(value);
				String newValue = ComplexPropertyParser.replaceFullSynValue(hm,
						"term-name", pt);
				item.setValue(newValue);
				item.setNameValue(name, newValue);
				//break;
			}
			
		}
	}

	/**
	 * Finds and returns a list of TreeItems with the specified property name.
	 * 
	 * @param name
	 *            The TreeItem's property name.
	 * @return a list of TreeItems with the specified property name.
	 */
	public TreeItems find(String name) {
		TreeItems items = new TreeItems();
		Iterator<TreeItem> iterator = iterator();
		while (iterator.hasNext()) {
			TreeItem item = iterator.next();
			String itemName = item.getName();
			if (itemName.equals(name))
				items.add(item);
		}
		return items;
	}

	/**
	 * Returns the index of the FULL_SYN/PT/NCI TreeItem.
	 * 
	 * @return the index of the /PT/NCI TreeItem.
	 */
	public int findIndex_FullSynPtNci() {
		int n = size();
		for (int i = 0; i < n; ++i) {
			TreeItem item = elementAt(i);
			String name = item.getName();
			if (!name.equals(NCIEditTab.ALTLABEL)) // FULL_SYN
				continue;
			String value = item.getValue();
			if (value.contains("<term-group>PT</term-group>")
					&& value.contains("<term-source>NCI</term-source"))
				return i;
		}
		return -1;
	}

	/**
	 * Removes the list of TreeItems with the specified property name.
	 * 
	 * @param name
	 *            The TreeItem's property name.
	 * @return the list of TreeItems with the specified property name.
	 */
	public TreeItems remove(String name) {
		TreeItems items = new TreeItems();
		int n = size();
		for (int i = n - 1; i >= 0; --i) {
			TreeItem item = elementAt(i);
			String itemName = item.getName();
			if (itemName.equals(name)) {
				items.insertElementAt(item, 0);
				remove(i);
			}
		}
		return items;
	}

	/**
	 * Creates a concept with the list of stored properties in the vector.
	 * 
	 * @param name
	 *            The concept name.
	 * @param pt
	 *            The preferred name.
	 * @param properties
	 *            The list of properties within TreeItems.
	 * @return the newly created concept.
	 */
	public OWLNamedClass createCls(String name, String pt) {
		TreeItems clone = clone();
		clone.remove(NCIEditTab.PREFLABEL);
		clone.remove(clone.findIndex_FullSynPtNci());
		TreeItems parentItems = clone.remove(NCIEditTab.SUBCLASSOF);

		OWLNamedClass newCls = null;
		int n = parentItems.size();
		for (int i = 0; i < n; ++i) {
			TreeItem item = parentItems.elementAt(i);
			RDFSClass parentCls = item.getCls();
			String parentName = parentCls.getPrefixedName();
			if (i == 0)
				newCls = _wrapper.createCls(name, pt, parentName);
			else
				newCls.addSuperclass(parentCls);
		}

		if (newCls == null)
			return null;
		clone.addInsertedItems(_wrapper, newCls);
		return newCls;
	}
}
