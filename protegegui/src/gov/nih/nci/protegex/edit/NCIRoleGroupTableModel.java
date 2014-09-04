package gov.nih.nci.protegex.edit;

import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIRoleGroupTableModel extends AbstractTableModel {

	public static final long serialVersionUID = 923456026L;

	/**
	 * The resource being annotated
	 */
	private HashMap<Integer, Vector<OWLRestriction>> rolegroups = new HashMap<Integer, Vector<OWLRestriction>>();

	private HashMap<Integer, RoleGroupElement> values = new HashMap<Integer, RoleGroupElement>();

	private JTable table;

	private OWLClass role_group;

	public NCIRoleGroupTableModel(OWLClass r0) {
		this.role_group = r0;
		initialize();
	}

	private void initialize() {
		populateRoleGroupTable((OWLClass) role_group);
	}

	public int getColumnCount() {
		return 2;
	}

	public int addRow(OWLRestriction restriction) {
		return addRow(restriction, 0);
	}

	public int addRow(OWLRestriction restriction, int groupId) {
		if (!hasRoleGroupValue(restriction, new Integer(groupId))) {
			addRoleGroupValue(restriction, new Integer(groupId));

			RoleGroupElement element = new RoleGroupElement(groupId,
					restriction);
			values.put(values.size(), element);

			updateValues();
		}
		return values.size();
	}

	public void addRoleGroupValue(OWLRestriction restriction, Integer groupId) {
		if (restriction == null)
			return;
		if (hasRoleGroupValue(restriction, groupId))
			return;

		Vector<OWLRestriction> roles = new Vector<OWLRestriction>();
		if (rolegroups.containsKey(groupId)) {
			roles = rolegroups.get(groupId);
		}

		roles.add(restriction);

		rolegroups.put(groupId, roles);
	}

	private boolean hasRoleGroupValue(OWLRestriction restriction,
			Integer groupId) {
		if (restriction == null)
			return false;

		if (rolegroups.containsKey(groupId)) {
			Vector roles = (Vector) rolegroups.get(groupId);
			if (roles.contains(restriction))
				return true;
		}

		return false;
	}

	private String getRoleValue(OWLRestriction r) {
		return r.getBrowserText();
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setValueAndGetIt(aValue, rowIndex, columnIndex);
	}

	public Object setValueAndGetIt(Object value, int row, int col) {
		if (col == 0)
			return setGroupNumberAndGetIt(value, row);

		return null;
		
	}

	private Object setGroupNumberAndGetIt(Object value, int row) {
		RoleGroupElement element = (RoleGroupElement) values.get(row);
		int oldValue = element.getRoleGroupNumber();
		OWLRestriction r = element.getRestriction();
		element.setRoleGroupNumber(Integer.parseInt((String) value));
		values.put(row, element);

		Vector<OWLRestriction> v1 = rolegroups.get(oldValue);
		v1.remove(r);
		rolegroups.put(oldValue, v1);

		Vector<OWLRestriction> v2 = new Vector<OWLRestriction>();
		int newKey = Integer.parseInt((String) value);
		if (rolegroups.containsKey(newKey)) {
			v2 = rolegroups.get(newKey);
		}
		v2.add(r);
		rolegroups.put(newKey, v2);

		return value;
	}

	public int getRestrictionValueRow(OWLRestriction restriction, int groupId) {
		return -1;
	}

	public String getColumnName(int column) {
		if (column == 0)
			return "Group No.";
		else
			return "Restriction";
	}

	public void deleteRow(int rowIndex) {

		RoleGroupElement element = (RoleGroupElement) values.get(rowIndex);
		for (int i = rowIndex; i < values.size() - 1; i++) {
			values.put(i, values.get(i + 1));
		}
		values.remove(values.size() - 1);

		int groupnumber = element.getRoleGroupNumber();
		OWLRestriction r = element.getRestriction();

		Vector v = (Vector) rolegroups.get(groupnumber);
		v.remove(r);

		if (v.size() > 0) {
			rolegroups.put(groupnumber, v);
		} else {
			rolegroups.remove(groupnumber);
		}
        
		updateValues();
	}

	public void updateRestriction(OWLRestriction r, int index) {
		RoleGroupElement element = (RoleGroupElement) values.get(index);
		int rolegroup_number = element.getRoleGroupNumber();
		OWLRestriction old_restriction = element.getRestriction();
		Vector<OWLRestriction> v = rolegroups.get(rolegroup_number);
		v.remove(old_restriction);
		v.add(r);
		rolegroups.put(rolegroup_number, v);

		element.setRestriction(r);
		updateValues();
	}

	void updateValues() {
		int index = -1;
		if (table != null)
			index = table.getSelectedRow();

		fireTableDataChanged();
		if (table != null && index >= 0 && index < getRowCount()) {
			table.getSelectionModel().setSelectionInterval(index, index);
		} else if (table != null && getRowCount() > 0) {
			table.getSelectionModel().setSelectionInterval(0, 0);
		}
	}

	public int getRowCount() {
		return values.size();
	}
	
	/**
	public void cleanOutModel() {
	    for (int i = 0;i < getRowCount();i++) {
	        OWLRestriction r = getRole(i);
	        r.delete();
	        
	    }
	}
	**/

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return getRoleGroupNumber(rowIndex);
		} else {
			OWLRestriction r = getRole(rowIndex);
			return getRoleValue(r);
		}
	}

	public OWLRestriction getRole(int rowIndex) {
		RoleGroupElement element = (RoleGroupElement) values.get(rowIndex);
		return element.getRestriction();
	}

	public int getRoleGroupNumber(int rowIndex) {
		RoleGroupElement element = (RoleGroupElement) values.get(rowIndex);
		return element.getRoleGroupNumber();
	}

	public void setTable(JTable table) {
		this.table = table;
		this.table.setFont(new Font("Times New Roman", Font.PLAIN, 12));
	}

	public JTable getTable() {
		return table;
	}

	public void dumpRoleGroup(HashMap rolegroup_hmap) {
		System.out.println("Role groups...");

		Iterator keys = rolegroup_hmap.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			Vector v = (Vector) rolegroup_hmap.get(key);
			for (int i = 0; i < v.size(); i++) {
				OWLRestriction r = (OWLRestriction) v.elementAt(i);
				System.out.println("\t" + key + ": " + r.getBrowserText());
			}
		}
	}

	public HashMap getRolegroups() {
		return rolegroups;
	}

	public HashMap getValues() {
		return values;
	}

	private void populateRoleGroupTable(OWLClass owlcls) {
		if (owlcls == null) {
			return;
		}
		int group_number = 0;

		// if it's a form of intersection class
		if (owlcls instanceof OWLIntersectionClass) {

			//System.out
					//.println("NCIRoleGroupTableModel populateRoleGroupTable ... owlcls instanceof OWLIntersectionClass ");

			OWLIntersectionClass intersectionCls = (OWLIntersectionClass) owlcls;
			Collection c = intersectionCls.getOperands();
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Cls r = (Cls) it.next();

				if (r instanceof OWLIntersectionClass) {
					// not supported.
					//System.out
							//.println("populateRoleGroupTable OWLIntersectionClass NOT SUPPORTED."
									//+ r.getBrowserText());
				} else if (r instanceof OWLUnionClass) {

					//System.out
							//.println("populateRoleGroupTable -- r instanceof OWLUnionClass");

					//System.out.println("populateRoleGroupTable OWLUnionClass "
							//+ r.getBrowserText());

					OWLUnionClass unionCls = (OWLUnionClass) r;

						Collection c2 = unionCls.getOperands();
					Iterator it2 = c2.iterator();
					while (it2.hasNext()) {
						Cls cls = (Cls) it2.next();
						OWLIntersectionClass intersection_cls = (OWLIntersectionClass) cls;
						Collection c3 = intersection_cls.getOperands();

						group_number++;

						Iterator it3 = c3.iterator();
						while (it3.hasNext()) {
							Cls cls3 = (Cls) it3.next();
							OWLRestriction r3 = (OWLRestriction) cls3;
							addRow(r3, group_number);
						}
					}
				} else {

					//System.out
							//.println("populateRoleGroupTable r NOT instanceof OWLUnionClass addRow "
									//+ r.getBrowserText());

					addRow((OWLRestriction) r, 0);
				}

			}

		}
		// if it has more than one role groups
		else if (owlcls instanceof OWLUnionClass) {
			// each operand is a role group
			OWLUnionClass unionCls = (OWLUnionClass) owlcls;

			Collection c2 = unionCls.getOperands();
			Iterator it2 = c2.iterator();
			while (it2.hasNext()) {
				Cls cls = (Cls) it2.next();
				OWLIntersectionClass intersection_cls = (OWLIntersectionClass) cls;

				Collection c3 = intersection_cls.getOperands();
				group_number++;

				Iterator it3 = c3.iterator();
				while (it3.hasNext()) {
					Cls cls3 = (Cls) it3.next();
					OWLRestriction r3 = (OWLRestriction) cls3;

					//System.out
							//.println("populateRoleGroupTable -- a simple restriction Group number "
									//+ group_number);

					addRow(r3, group_number);
				}
			}
		} else // if it's a simple restriction
		{

			//System.out
					//.println("populateRoleGroupTable -- a simple restriction Group number 0 ");

			OWLRestriction r = (OWLRestriction) owlcls;
			addRow(r, 0);
		}

	}

}
