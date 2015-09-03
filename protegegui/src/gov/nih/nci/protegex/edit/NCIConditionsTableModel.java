package gov.nih.nci.protegex.edit;

import java.awt.Component;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.JLabel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.impl.AbstractRDFSClass;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.conditions.ConditionsTableItem;
import edu.stanford.smi.protegex.owl.ui.conditions.ConditionsTableModel;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.util.MsgDialog;

/**
 * The TableModel used by the AssertedConditionsWidget.
 * 
 * TODO: refactor to extend the ConditionsTableModel in OWL
 * 
 * @author NGIT
 */
public class NCIConditionsTableModel extends ConditionsTableModel {

	public static final long serialVersionUID = 123456011L;

	private Logger logger = Log.getLogger(getClass());

	public static final int SET_RESTRICTION = 1;

	public static final int SET_SUPERCLASS = 2;

	int set_type = 0;

	private Stack<RDFSClass> tmp_clses = new Stack<RDFSClass>();

	public void cleanOutTemps() {
		while (!tmp_clses.empty()) {
			RDFSClass rc = tmp_clses.pop();
			if (rc != null) {
				rc.delete();
			}
		}
	}

	/**
	 * One Item object for each row
	 */
	private java.util.List<NCIConditionsTableItem> init_items = new ArrayList<NCIConditionsTableItem>();

	/**
	 * Needed to select the most recently edited row after closing the
	 * expression editor
	 */
	public Cls previouslyEditedCls;

	public EditPanel edit_panel;

	public NCIConditionsTableModel(OWLNamedClass hostCls,
			Slot superclassesSlot, int type, EditPanel edit_panel) {
		super(superclassesSlot);
		this.edit_panel = edit_panel;
		this.set_type = type;
		setCls(hostCls);
	}

	public int addEmptyRow(int selectedRow) {

		NCIConditionsTableItem item = NCIConditionsTableItem
				.createNew(getType(selectedRow));
		int index = selectedRow + 1;
		addItem(index, item);
		fireTableRowsInserted(index, index);
		return index;

	}

	/**
	 * Adds a given Cls to the class conditions specified by a given row.
	 * 
	 * @param aClass
	 *            the aClassass to add
	 * @param selectedRow
	 *            the row to add to
	 * @return true if the row has been added
	 */
	public boolean addRow(RDFSClass aClass, int selectedRow) {

		if (aClass == null) {
			return false;
		}

		String browserText = edit_panel.getNCIEditTab().getOWLWrapper()
				.getInternalName(aClass);
		if (isDefinition(selectedRow)) {
			boolean retval = addRowAllowMove(aClass, selectedRow);
			return retval;
		}

		else if (!((AbstractRDFSClass) hostClass)
				.hasPropertyValueWithBrowserText(((KnowledgeBase) owlModel)
						.getSlot(Model.Slot.DIRECT_SUPERCLASSES), browserText)) {
			boolean retval = addRowAllowMove(aClass, selectedRow);
			return retval;

		} else {
			boolean retval = addRowAllowMove(aClass, selectedRow);
			return retval;
		}
	}

	public boolean addRow(RDFSClass aClass, int selectedRow, boolean isDefining) {
		if (!(aClass instanceof OWLNamedClass)) {
			tmp_clses.push(aClass);
		}
		return addRowAllowMove(aClass, selectedRow, isDefining);
	}

	public boolean addRowAllowMove(RDFSClass aClass, int selectedRow) {
		return addRowAllowMove(aClass, selectedRow, false);
	}

	// check
	public boolean addRowAllowMove(RDFSClass aClass, int selectedRow,
			boolean isDefining) {
		if (aClass.equals(hostClass)) {
			logger.warning("Invalid input -- restriction not added.");
			return false;
		}

		if (isDefining) {
			if (aClass instanceof OWLNamedClass) {
				addDefiningSuperclass(aClass, SET_SUPERCLASS);
			} else {
				addDefiningRestriction(aClass);
			}
		} else {
			addSuperclass(aClass);

		}

		fireTableDataChanged();
		return true;
	}

	public boolean isSeperator(int index) {
		NCIConditionsTableItem item = (NCIConditionsTableItem) items.get(index);
		if (item.isSeparator()) {
			return true;
		}
		return false;
	}

	public void deleteRow(int index) {
		if (index < 0 && index >= items.size())
			return;

		NCIConditionsTableItem tableItem = (NCIConditionsTableItem) items
				.get(index);
		if (tableItem.isSeparator()) {
			logger.warning("Cannot delete a separator ");
			return;
		} else if (tableItem.isInherited()) {
			Component warning_label = new JLabel(
					"Cannot delete a inherited restriction.");
			LabeledComponent lc2 = new LabeledComponent("", warning_label);
			ProtegeUI.getModalDialogFactory().showDialog(this.edit_panel, lc2,
					"WARNING", ModalDialogFactory.MODE_CLOSE);
			return;
		} else {

			items.remove(index);
			fireTableDataChanged();
		}
	}

	private void downCastItems() {

		ArrayList<ConditionsTableItem> newitems = new ArrayList<ConditionsTableItem>();

		for (ConditionsTableItem cdi : items) {
			if (cdi instanceof NCIConditionsTableItem) {
				newitems.add((NCIConditionsTableItem) cdi);
			} else {
				newitems.add(NCIConditionsTableItem.createInherited(cdi
						.getCls(), cdi.getOriginCls()));
			}
		}
		items = newitems;
	}

	protected void fillItems() {
		Collection<Instance> coveredClses = new HashSet<Instance>();
		final int classificationStatus = getEditedCls()
				.getClassificationStatus();

		if (superclassesSlot.getName().equals(Model.Slot.DIRECT_SUPERCLASSES)
				|| classificationStatus != OWLNames.CLASSIFICATION_STATUS_UNDEFINED) {

			fillDefinitionItems(coveredClses);
			fillDirectSuperclassItems(coveredClses);

			if (set_type == SET_RESTRICTION) {
				// the super class may not populate inherited items, depending
				// on the configuration of the properties
				super.fillInheritedItems(coveredClses);
				downCastItems();

			}

			sortItems();
		}

		if (items.size() > 2) // two separators (necessary and sufficient,
		// necessary)
		{
			edit_panel.enableDelEditHB(6, true);
			edit_panel.enableDelEditHB(7, true);
		} else {
			edit_panel.enableDelEditHB(6, false);
			edit_panel.enableDelEditHB(7, false);
		}

	}

	public NCIConditionsTableItem getItem(int rowIndex) {
		return (NCIConditionsTableItem) items.get(rowIndex);
	}

	/**
	 * Checks whether the "add named class" function is enabled for a given row.
	 * 
	 * @param rowIndex
	 *            the index of the row where a named class shall be added
	 * @return true if a named class could be added
	 */

	public boolean isAddEnabledAt(int rowIndex) {
		return true;
	}

	// Implements TableModel
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void removeEmptyRow() {
		super.removeEmptyRow();
		updateLocalIndices();
	}

	/**
	 * Sorts the items according to their <CODE>compareTo</CODE> method.
	 * 
	 * @see NCIConditionsTableItem#compareTo
	 */
	private void sortItems() {
		Collections.sort(items);
		updateLocalIndices();
	}

	private void updateLocalIndices() {
		int row = 0;
		for (Iterator it = items.iterator(); it.hasNext(); row++) {
			NCIConditionsTableItem item = (NCIConditionsTableItem) it.next();
			if (item.isSeparator()) {
				updateLocalIndices(row + 1);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	private void updateLocalIndices(int startRow) {
		for (int i = startRow; i < items.size() && !getItem(i).isSeparator(); i++) {
			getItem(i).setLocalIndex(i - startRow);
		}
	}

	private void fillInitItems() {
		init_items.clear();
		if (hostClass == null)
			return;
		for (Iterator it = items.iterator(); it.hasNext();) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) it
					.next();

			if (!tableItem.isSeparator()) {
				if (tableItem.getCls() != null) {
					init_items.add(tableItem.createClone());
				}
			}
		}
	}

	public void setCls(OWLNamedClass cls) {

		tmp_clses = new Stack<RDFSClass>();

		items.clear();

		hostClass = cls;
		if (hostClass != null) {

			fillItems();
			fireTableDataChanged();

			fillInitItems();
		}
	}

	public Collection getItems() {
		ArrayList<NCIConditionsTableItem> a = new ArrayList<NCIConditionsTableItem>();
		for (Iterator it = items.iterator(); it.hasNext();) {
			NCIConditionsTableItem item = (NCIConditionsTableItem) it.next();
			if (!item.isSeparator()) {
				a.add(item);
			}
		}
		return a;
	}

	public boolean togglePrimitiveDefiningState(boolean defining) {
		try {
			boolean done = false;
			while (!done) {
				done = true;
				for (int i = 0; i < items.size(); i++) {

					NCIConditionsTableItem ti = (NCIConditionsTableItem) items
							.get(i);
					if (ti.isSeparator() || ti.isInherited()) {

					} else {
						RDFSClass rcl = ti.getCls();
						if (ti.isDefining()) {
							if (defining) {

							} else {
								if (rcl instanceof OWLRestriction) {
									rcl = ((OWLRestriction) rcl).createClone();
								}
								if (rcl instanceof OWLUnionClass) {
									rcl = ((OWLUnionClass) rcl).createClone();
								}
								if (rcl instanceof OWLIntersectionClass) {
									
									rcl = ((OWLIntersectionClass) rcl)
											.createClone();
									
								}
								modifyRow(i, rcl, false);
								done = false;
								break;

							}

						} else {
							if (defining) {
								if (rcl instanceof OWLRestriction) {
									rcl = ((OWLRestriction) rcl).createClone();
								}
								if (rcl instanceof OWLUnionClass) {
									rcl = ((OWLUnionClass) rcl).createClone();
								}
								if (rcl instanceof OWLIntersectionClass) {
									rcl = ((OWLIntersectionClass) rcl)
											.createClone();
								}

								modifyRow(i, rcl, true);
								done = false;
								break;

							} else {

							}

						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			Log.getLogger()
					.warning(
							"Error trying to convert state of class: "
									+ e.getMessage());
			MsgDialog.error(edit_panel, "Convert Class Error", 
					"Unable to toggle state of class due to underlying exception: " + e.getMessage());
			return false;

		}
	}

	public boolean alreayHasItem(RDFSClass c) {
		for (int i = 0; i < items.size(); i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) items
					.get(i);
			if (!tableItem.isSeparator()) {
				// value = tableItem.getDisplayText();
				if (tableItem.getCls() instanceof RDFSClass) {
					if (tableItem.getCls().equalsStructurally(c)) {
						return true;
					}

				}
			}
		}
		return false;
	}

	public Collection getInitialItems() {
		return init_items;
	}

	public int getNamedSuperClassCount() {
		if (hostClass == null)
			return -1;
		if (items == null || items.size() == 0)
			return 0;

		int count = 0;
		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];
			if (tableItem.getType() == TYPE_SUPERCLASS) {
				RDFSClass sup = (RDFSClass) tableItem.getCls();
				if (sup instanceof OWLNamedClass)// && !tableItem.isDefining)
				// // testing
				{
					count++;
				}
			}
		}
		return count;
	}

	public int getRestrictionCount() {
		if (hostClass == null)
			return -1;
		if (items == null || items.size() == 0)
			return 0;

		int count = 0;
		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];
			if (tableItem.getType() == TYPE_SUPERCLASS) {
				RDFSClass sup = (RDFSClass) tableItem.getCls();
				if (sup instanceof OWLAnonymousClass) {
					count++;
				}
			}
		}
		return count;
	}

	public int findFirstRowInNecessaryBlock() {
		if (hostClass == null)
			return -1;
		if (items == null || items.size() == 0)
			return 0;
		int numSeparators = 0;

		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];
			if (tableItem.isSeparator()) {
				numSeparators++;
				if (numSeparators == 2) {

					return i + 1;
				}
			}
		}
		return objs.length;
	}

	public int getDefinitionCount() {
		if (hostClass == null)
			return -1;
		if (items == null || items.size() == 0)
			return 0;
		int numSeparators = 0;
		int count = 0;
		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];
			if (tableItem.isSeparator()) {
				numSeparators++;
				if (numSeparators == 2)
					return count;
			} else {
				count++;
			}
		}
		return count;
	}

	public boolean addEquivalentClass(RDFSClass aClass) {
		if (hostClass == null || aClass == null)
			return false;
		if (items == null || items.size() == 0)
			return false;

		NCIConditionsTableItem item = NCIConditionsTableItem.create(aClass, 0);
		item.setIsDefining(true);

		items.add(item);

		sortItems();

		java.util.List<NCIConditionsTableItem> separators = new ArrayList<NCIConditionsTableItem>();

		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];
			if (tableItem.isSeparator()) {
				separators.add(tableItem);
			}
		}
		fireTableDataChanged();

		return true;
	}

	public boolean addDefiningRestriction(RDFSClass aClass) {
		if (hostClass == null || aClass == null)
			return false;
		if (items == null || items.size() == 0)
			return false;

		RDFSClass newCls = null;
		if (aClass != null) {

			newCls = aClass;
		}

		NCIConditionsTableItem item = NCIConditionsTableItem.create(newCls, 0);

		item.setIsDefining(true);
		items.add(1, item);
		fireTableDataChanged();
		return true;
	}

	private boolean addDefiningSuperclass(RDFSClass aClass, int type) {
		if (hostClass == null || aClass == null)
			return false;
		if (items == null || items.size() == 0)
			return false;

		NCIConditionsTableItem item = NCIConditionsTableItem.create(aClass, -1);
		item.setIsDefining(true);

		// item.setDefinition();
		items.add(1, item);

		fireTableDataChanged();
		// dumpItems();

		return true;
	}

	public String getRestrictionModifier(OWLWrapper wrapper,
			NCIConditionsTableItem item) {
		if (!(item.getCls() instanceof OWLRestriction))
			return null;
		OWLRestriction cls = (OWLRestriction) item.getCls();
		return wrapper.getRestrictionType(cls);
	}

	public String getRestrictionName(OWLWrapper wrapper,
			NCIConditionsTableItem item) {
		if (!(item.getCls() instanceof OWLRestriction))
			return null;
		OWLRestriction cls = (OWLRestriction) item.getCls();
		RDFProperty property = (RDFProperty) cls.getOnProperty();
		return property.getBrowserText();
	}

	public String getRestrictionValue(OWLWrapper wrapper,
			NCIConditionsTableItem item) {
		if (!(item.getCls() instanceof OWLRestriction))
			return null;
		OWLRestriction cls = (OWLRestriction) item.getCls();
		return cls.getFillerText();
	}

	public boolean itemInSet(NCIConditionsTableItem item, java.util.List list) {
		if (list.isEmpty())
			return false;
		for (int i = 0; i < list.size(); i++) {
			NCIConditionsTableItem nextItem = (NCIConditionsTableItem) list
					.get(i);
			if (sameItem(item, nextItem))
				return true;
		}
		return false;
	}

	public boolean sameItem(NCIConditionsTableItem item_1,
			NCIConditionsTableItem item_2) {
		// aClass

		String str1 = edit_panel.getNCIEditTab()
				.getBrowserText(item_1.getCls());
		String str2 = edit_panel.getNCIEditTab()
				.getBrowserText(item_2.getCls());
		if (str1.compareTo(str2) != 0)
			return false;

		// if
		// (item_1.getCls().getBrowserText().compareTo(item_2.getCls().getBrowserText())
		// != 0) return false;

		// boolean isDefining
		if (item_1.isDefining() && !item_2.isDefining())
			return false;
		if (!item_1.isDefining() && item_2.isDefining())
			return false;

		// int type
		if (item_1.getType() != item_2.getType())
			return false;

		// OWLIntersectionClass definition;
		if (item_1.getDefinition() == null && item_2.getDefinition() != null)
			return false;
		if (item_1.getDefinition() != null && item_2.getDefinition() == null)
			return false;
		if (item_1.getDefinition().getBrowserText().compareTo(
				item_2.getDefinition().getBrowserText()) != 0)
			return false;

		return true;
	}

	/*
	 * private boolean addSuperclass(RDFSClass aClass, int type) { if (hostClass
	 * == null || aClass == null) return false; if (items == null ||
	 * items.size() == 0) return false;
	 * 
	 * NCIConditionsTableItem item = NCIConditionsTableItem.create(aClass, -1);
	 * int index = findFirstRowInNecessaryBlock();
	 * 
	 * items.add(index, item); return true; }
	 */
	private boolean addSuperclass(RDFSClass aClass) {
		if (hostClass == null || aClass == null)
			return false;
		if (items == null || items.size() == 0)
			return false;

		NCIConditionsTableItem item = NCIConditionsTableItem.create(aClass, -1);
		int index = findFirstRowInNecessaryBlock();

		items.add(index, item);
		return true;

	}

	public int replaceNamedSuperClass(String oldClsName, String newClsName,
			RDFSClass aClass) {
		if (newClsName == null)
			return 0;
		if (items == null || items.size() == 0)
			return 0;

		int count = 0;
		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];
			if (tableItem.getType() == TYPE_SUPERCLASS) {
				RDFSClass sup = (RDFSClass) tableItem.getCls();

				if (sup == null) {
					System.out.println("WARNING: sup == null");
				}

				if (sup instanceof OWLNamedClass)// && !tableItem.isDefining)
				// // testing
				{

					if (sup.getBrowserText().equals(oldClsName)) {
						// tableItem.setCls((RDFSClass) newCls);

						tableItem.setCls((RDFSClass) aClass);
						count++;
					}

				}
			}
		}

		if (count > 0) {
			fireTableDataChanged();
		}
		return count;
	}

	public String getTableItemName(int row) {
		RDFSClass cls = (RDFSClass) getClass(row);
		if (cls == null)
			return null;
		return cls.getBrowserText();
	}

	public void updateTableItem(int selIndex, RDFSClass aClass) {
		NCIConditionsTableItem tableItem = (NCIConditionsTableItem) this
				.getItem(selIndex);
		tableItem.setCls(aClass);
		fireTableDataChanged();

	}

	public void updateTableItem(int selIndex, RDFSClass aClass,
			boolean isDefining) {
		NCIConditionsTableItem tableItem = (NCIConditionsTableItem) this
				.getItem(selIndex);
		tableItem.setCls(aClass);
		tableItem.setIsDefining(isDefining);
		fireTableDataChanged();

	}

	public boolean containsDefinition(OWLNamedClass hostClass, RDFSClass aClass) {
		if (aClass == null)
			return false;

		RDFSClass definition = null;
		Collection c = hostClass.getEquivalentClasses();

		if (c.size() == 0)
			return false;

		Iterator it = c.iterator();
		while (it.hasNext()) {
			definition = (RDFSClass) it.next();
			break;
		}

		if (definition.getBrowserText().compareTo(aClass.getBrowserText()) == 0) {
			return true;
		}
		if (definition instanceof OWLIntersectionClass) {
			OWLIntersectionClass intersectionCls = (OWLIntersectionClass) definition;
			if (intersectionCls.hasOperandWithBrowserText(aClass
					.getBrowserText()))
				return true;
		}
		return false;
	}

	private void fillDirectSuperclassItems(Collection coveredClses) {
		items.add(NCIConditionsTableItem.createSeparator(TYPE_SUPERCLASS));
		if (set_type == SET_SUPERCLASS) {
			for (Iterator it = ((Cls) hostClass).getDirectOwnSlotValues(
					superclassesSlot).iterator(); it.hasNext();) {
				Cls superCls = (Cls) it.next();
				if (superCls instanceof OWLNamedClass
						&& !coveredClses.contains(superCls)) {

					if (!containsDefinition(hostClass, (RDFSClass) superCls)) {
						RDFSClass aClass = (RDFSClass) superCls;
						coveredClses.add(aClass);
						NCIConditionsTableItem item = NCIConditionsTableItem
								.create(aClass, TYPE_SUPERCLASS);
						item.setCls(aClass);
						item.setIsDefining(false);
						items.add(NCIConditionsTableItem.create(aClass,
								TYPE_SUPERCLASS));

					}
				}
			}

		} else if (set_type == SET_RESTRICTION) {
			for (Iterator it = hostClass.getSuperclasses(false).iterator(); it
					.hasNext();) {
				Cls superCls = (Cls) it.next();
				if (superCls instanceof OWLAnonymousClass
						&& !coveredClses.contains(superCls)) {
					if (!containsDefinition(hostClass, (RDFSClass) superCls)) {
						RDFSClass aClass = (RDFSClass) superCls;
						coveredClses.add(aClass);
						NCIConditionsTableItem item = NCIConditionsTableItem
								.create(aClass, TYPE_SUPERCLASS);
						item.setCls(aClass);
						item.setIsDefining(false);
						items.add(NCIConditionsTableItem.create(aClass,
								TYPE_SUPERCLASS));
					}
				}
			}
		}
	}

	private void fillDefinitionItems(Collection<Instance> coveredClses) {
		List<NCIConditionsTableItem> separators = new ArrayList<NCIConditionsTableItem>();
		Slot slot = ((KnowledgeBase) hostClass.getOWLModel())
				.getSlot(Model.Slot.DIRECT_SUPERCLASSES);

		int index = TYPE_DEFINITION_BASE;
		NCIConditionsTableItem separator0 = NCIConditionsTableItem
				.createSeparator(index);
		items.add(separator0);
		separators.add(separator0);

		boolean first = true;
		for (Iterator it = ((Cls) hostClass).getDirectOwnSlotValues(slot)
				.iterator(); it.hasNext();) {
			Cls superCls = (Cls) it.next();
			if (superCls instanceof RDFSClass
					&& superCls.getDirectOwnSlotValues(slot)
							.contains(hostClass)) {

				if (!first) {
					NCIConditionsTableItem separator = NCIConditionsTableItem
							.createSeparator(index);

					items.add(separator);
					separators.add(separator);
				}

				first = false;
				RDFSClass equivalentClass = (RDFSClass) superCls;
				coveredClses.add(equivalentClass);

				if (equivalentClass instanceof OWLIntersectionClass) {
					OWLIntersectionClass intersectionCls = (OWLIntersectionClass) equivalentClass;

					Collection operands = ((OWLIntersectionClass) equivalentClass)
							.getOperands();
					for (Iterator oit = operands.iterator(); oit.hasNext();) {
						RDFSClass operand = (RDFSClass) oit.next();
						if (set_type == SET_RESTRICTION
								&& operand instanceof OWLAnonymousClass) {
							coveredClses.add(operand);
							NCIConditionsTableItem item = NCIConditionsTableItem
									.createSufficient(operand, index,
											intersectionCls);
							item.setIsDefining(true);
							items.add(item);

						}

						else if (set_type == SET_SUPERCLASS
								&& operand instanceof OWLNamedClass) {
							coveredClses.add(operand);
							NCIConditionsTableItem item = NCIConditionsTableItem
									.createSufficient(operand, index,
											intersectionCls);
							item.setIsDefining(true);
							items.add(item);
						}
					}
				}

				else {

					if (set_type == SET_RESTRICTION
							&& equivalentClass instanceof OWLAnonymousClass) {
						NCIConditionsTableItem item = NCIConditionsTableItem
								.create(equivalentClass, index);
						item.setIsDefining(true);
						items.add(item);
					} else if (set_type == SET_SUPERCLASS
							&& equivalentClass instanceof OWLNamedClass) {
						NCIConditionsTableItem item = NCIConditionsTableItem
								.create(equivalentClass, index);
						item.setIsDefining(true);
						items.add(item);
					}
				}
				index++;
			}
		}
	}

	public int getStrictNamedSuperClassCount() {
		if (set_type != SET_SUPERCLASS) {
			return 0;
		}
		if (hostClass == null)
			return -1;

		if (items == null || items.size() == 0) {
			return 0;
		}

		int count = 0;
		Object[] objs = items.toArray();
		for (int i = 0; i < objs.length; i++) {
			NCIConditionsTableItem tableItem = (NCIConditionsTableItem) objs[i];

			RDFSClass sup = (RDFSClass) tableItem.getCls();
			// if (sup instanceof OWLNamedClass && !tableItem.getIsDefining())
			// // testing
			if (sup instanceof OWLNamedClass) // testing
			{
				// System.out.println(sup.getBrowserText());
				count++;
			}
		}
		return count;
	}

	public void modifyRow(int index, RDFSClass aClass, boolean isDefining) {
		deleteRow(index);
		if (isDefining) {
			if (aClass instanceof OWLNamedClass) {
				addDefiningSuperclass(aClass, SET_SUPERCLASS);
			} else {
				addDefiningRestriction(aClass);
				
			}
		} else {
			addSuperclass(aClass);
		}
		if (!(aClass instanceof OWLNamedClass)) {

			tmp_clses.push(aClass);

		}

		fireTableDataChanged();
	}

}
