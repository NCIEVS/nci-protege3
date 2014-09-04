package gov.nih.nci.protegex.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class NCIRoleGroupEditorPanel extends JComponent {

	public static final long serialVersionUID = 923456027L;

	private OWLModel owlModel;

	private RDFSClass targetClass = null;

	RDFProperty property;

	edu.stanford.smi.protege.model.Cls metaCls;

	boolean definingEditable = true;

	NCIEditTab tab;

	OWLWrapper wrapper = null;

	public boolean cancelBtnPressed = false;

	JCheckBox definingCheckBox;

	private boolean defining = false;

	// RDFSClass aClass;
	OWLClass role_group;
	
	HashMap orig_rgrps = null;

	Slot slot;


	NCIRoleGroupTableModel rolegroup_model;

	NCIRoleGroupTable rolegroup_table;


	OWLNamedClass focusedCls;

	int selIndex = -1;

	public NCIRoleGroupEditorPanel(NCIEditTab tab, OWLModel owlModel,
			OWLNamedClass focusedCls, int selIndex, OWLClass r0,
			boolean defining)

	{
		this.metaCls = null;
		this.owlModel = owlModel;
		this.tab = tab;
		this.focusedCls = focusedCls;
		this.selIndex = selIndex;
		this.defining = defining;

		this.role_group = r0;
		

		initialize();
	}

	public NCIRoleGroupTable getRoleGroupTable() {
		return rolegroup_table;
	}

	public NCIRoleGroupTableModel getRoleGroupTableModel() {
		return rolegroup_model;
	}

	private void initialize() {
		setLocation(260, 200);
		setPreferredSize(new Dimension(500, 300));

		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel definingPanel = new JPanel();
		definingPanel.setLayout(new BorderLayout());

		JLabel definingLabel = new JLabel("  Defining");
		definingCheckBox = new JCheckBox();
		definingCheckBox.setEnabled(definingEditable);

		definingCheckBox.setSelected(defining);
		definingPanel.add(definingCheckBox, BorderLayout.WEST);
		definingPanel.add(definingLabel, BorderLayout.CENTER);

		rolegroup_model = new NCIRoleGroupTableModel(role_group);
		
		this.orig_rgrps = roleGroupCopy(rolegroup_model.getRolegroups());

		rolegroup_table = new NCIRoleGroupTable(owlModel, rolegroup_model);

		rolegroup_model.setTable(rolegroup_table);

		JScrollPane scrollPane = new JScrollPane(rolegroup_table);

		panel.add(BorderLayout.CENTER, scrollPane);
		panel.add(BorderLayout.SOUTH, definingPanel);

		add(panel, BorderLayout.CENTER);

	}
	
	private HashMap<Integer, Vector<OWLRestriction>> roleGroupCopy(HashMap<Integer, Vector<OWLRestriction>> rg) {
	    HashMap<Integer, Vector<OWLRestriction>> res = new HashMap<Integer, Vector<OWLRestriction>>();
	    Iterator it = rg.keySet().iterator();
	    while (it.hasNext()) {
	        Integer key = (Integer) it.next();
	        res.put(key, new Vector<OWLRestriction>(rg.get(key)));
	    }
	    return res;
	}

	private boolean vectorsEqual(Vector v1, Vector v2) {
		if (v1.size() != v2.size()) {
			return false;
		}
		for (int i = 0; i < v1.size(); i++) {
			OWLRestriction r1 = (OWLRestriction) v1.elementAt(i);
			boolean found = false;
			for (int j = 0; j < v2.size(); j++) {
				OWLRestriction r2 = (OWLRestriction) v2.elementAt(j);
				if (r1.equalsStructurally(r2)) {
					found = true;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;

	}

	private Integer existsInMap(Vector tval, HashMap map) {

		Iterator keys = map.keySet().iterator();
		while (keys.hasNext()) {
			Integer gno = (Integer) keys.next();
			if (gno.intValue() != 0) {
				Vector val = (Vector) map.get(gno);
				if (val == tval) {
					// vector is always eq to itself
				} else {
					if (vectorsEqual(val, tval)) {
						return gno;

					}
				}
			}
		}
		return null;

	}

	private HashMap removeDups(HashMap map) {
		HashMap res = new HashMap();

		Iterator keys = map.keySet().iterator();
		while (keys.hasNext()) {
			Integer gno = (Integer) keys.next();
			if (gno.intValue() != 0) {
				Vector val = (Vector) map.get(gno);

				Integer ano = existsInMap(val, map);
				if (ano != null) {
					if (res.containsKey(ano)) {
                        // already in the map
					} else {
						res.put(gno, val);
					}
				} else {
				    res.put(gno, val);
                }
			} else {
				// it's zero just put it back
				res.put(gno, map.get(gno));
			}
		}

		return res;
	}
	
	private Iterator sort(Set<Integer> keys) {
	    
	    TreeSet<Integer> ts = new TreeSet<Integer>(keys);
	    return ts.iterator();
	    
	}

	private RDFSClass roleGroupHash2RDFSClass(HashMap rolegroup_hmap) {
		if (rolegroup_hmap == null)
			return null;
		Iterator keys = null;

		Vector<Integer> key_vec = new Vector<Integer>();

		// check for and remove empty groups
		keys = rolegroup_hmap.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			Vector v = (Vector) rolegroup_hmap.get(key);
			if (v.size() == 0) {
				key_vec.add((Integer) key);
			}
		}

		for (int i = 0; i < key_vec.size(); i++) {
			Integer key_int = (Integer) key_vec.elementAt(i);
			rolegroup_hmap.remove((Object) key_int);
		}

		rolegroup_hmap = removeDups(rolegroup_hmap);

		if (isSimpleRestriction(rolegroup_hmap)) {
			OWLRestriction r = getSimpleRestriction(rolegroup_hmap);
			return (RDFSClass) r;
		}

		try {
			owlModel.beginTransaction("Created local role group",
					targetClass == null ? null : targetClass.getName());

			int n = rolegroup_hmap.keySet().size();
			// one role group --> intersection class
			if (n == 1) {
				OWLIntersectionClass intersectionClass = owlModel
						.createOWLIntersectionClass();
				keys = rolegroup_hmap.keySet().iterator();
				while (keys.hasNext()) {
					Object key = keys.next();
					Vector v = (Vector) rolegroup_hmap.get(key);
					for (int i = 0; i < v.size(); i++) {
						OWLRestriction r = (OWLRestriction) v.elementAt(i);
						intersectionClass.addOperand(r);
					}

					owlModel.commitTransaction();
					return intersectionClass;
				}
			}

			if (!containsSimpleRestriction(rolegroup_hmap)) {
				OWLUnionClass unionClass = owlModel.createOWLUnionClass();
				keys = sort(rolegroup_hmap.keySet());
				while (keys.hasNext()) {
					Object key = keys.next();
					Integer groupNo = (Integer) key;
					if (groupNo.intValue() != 0) {
						OWLIntersectionClass intersectionClass = owlModel
								.createOWLIntersectionClass();
						Vector v = (Vector) rolegroup_hmap.get(key);
						for (int i = 0; i < v.size(); i++) {
							OWLRestriction r = (OWLRestriction) v.elementAt(i);
							intersectionClass.addOperand(r);
						}
						unionClass.addOperand(intersectionClass);
					}
				}

				owlModel.commitTransaction();
				return unionClass;
			}

			OWLIntersectionClass intersectionClass = owlModel
					.createOWLIntersectionClass();

			// simple restrictions:
			keys = rolegroup_hmap.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Integer groupNo = (Integer) key;
				if (groupNo.intValue() == 0) {
					Vector v = (Vector) rolegroup_hmap.get(key);
					for (int i = 0; i < v.size(); i++) {
						OWLRestriction r = (OWLRestriction) v.elementAt(i);
						intersectionClass.addOperand(r);
					}
				} else {
					Vector v = (Vector) rolegroup_hmap.get(key);
					if (v.size() == 1) {
						OWLRestriction r = (OWLRestriction) v.elementAt(0);
						intersectionClass.addOperand(r);
					}
				}
			}

			// count the number of true role groups
			int m = 0;
			keys = rolegroup_hmap.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Integer groupNo = (Integer) key;
				if (groupNo.intValue() != 0) {
					Vector v = (Vector) rolegroup_hmap.get(key);
					if (v.size() > 1) {
						m++;
					}
				}
			}

			if (m == 0) {
				owlModel.commitTransaction();
				return (RDFSClass) intersectionClass;
			}

			if (m == 1) {
				keys = rolegroup_hmap.keySet().iterator();
				while (keys.hasNext()) {
					Object key = keys.next();
					Integer groupNo = (Integer) key;
					if (groupNo.intValue() != 0) {
						Vector v = (Vector) rolegroup_hmap.get(key);
						if (v.size() > 1) {
							for (int i = 0; i < v.size(); i++) {
								OWLRestriction r = (OWLRestriction) v
										.elementAt(i);
								intersectionClass.addOperand(r);
							}

							owlModel.commitTransaction();
							return (RDFSClass) intersectionClass;
						}
					}
				}
			}

			// multiple role groups, need a union class
			OWLUnionClass unionClass = owlModel.createOWLUnionClass();
			keys = rolegroup_hmap.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Integer groupNo = (Integer) key;
				if (groupNo.intValue() != 0) {
					Vector v = (Vector) rolegroup_hmap.get(key);
					if (v.size() > 1) {
						OWLIntersectionClass intersectionCls = owlModel
								.createOWLIntersectionClass();
						for (int i = 0; i < v.size(); i++) {
							OWLRestriction r = (OWLRestriction) v.elementAt(i);
							intersectionCls.addOperand(r);
						}
						unionClass.addOperand(intersectionCls);
					}
				}
			}
			intersectionClass.addOperand(unionClass);

			owlModel.commitTransaction();
			return (RDFSClass) intersectionClass;
		} catch (Exception e) {
			owlModel.rollbackTransaction();
			OWLUI.handleError(owlModel, e);
		}

		return null;

	}

	public RDFSClass getRolegroups() {

		HashMap rolegroup_hmap = rolegroup_model.getRolegroups();
		if (rolegroup_hmap.keySet().size() < 2)
			return null;
		// if this was an edit and nothing has changed return the original and don't create a new one
		if ((role_group != null) && roleGroupsEqual(rolegroup_hmap, this.orig_rgrps)) {
		    return this.role_group;
		} else {
		return roleGroupHash2RDFSClass(rolegroup_hmap);
		}
	}
	
	private boolean vecsEqual(Vector<OWLRestriction> v1, Vector<OWLRestriction> v2) {
	    if (v1.size() != v2.size()) {
	        return false;
	    }
	    for (int i = 0; i < v1.size(); i++) {
	        OWLRestriction r1 = v1.elementAt(i);
	        boolean found = false;
	        for (int j = 0; j < v2.size(); j++) {
	            OWLRestriction r2 = v2.elementAt(j);
	            if (r1.equalsStructurally(r2)) {
	                found = true;
	                break;
	            }
	        }
	        if (!found) {
	            return false;
	        }
	    }
	    return true;
	    
	}
	
	private boolean roleGroupsEqual(HashMap<Integer, Vector<OWLRestriction>> rg1, HashMap<Integer, Vector<OWLRestriction>> rg2) {
	    if (rg1.size() != rg2.size()) {
	        return false;
	    }
	    Iterator it1 = rg1.keySet().iterator();
	    while (it1.hasNext()) {
	        Integer key = (Integer) it1.next();
	        Vector<OWLRestriction> vals1 = rg1.get(key);
	        
	        boolean found = false;
	        Iterator it2 = rg2.keySet().iterator();
	        while(it2.hasNext()) {
	            Integer key2 = (Integer) it2.next();
	            Vector<OWLRestriction> vals2 = rg2.get(key2);
	            if (vecsEqual(vals1, vals2)) {
	                found = true;
	                break;
	            }	            
	        }
	        if (!found) {
	            return false;
	        }        
	    }
	    return true;
	}

	public boolean getIsDefining() {
		return definingCheckBox.isSelected();
	}

	public NCIEditTab getNCIEditTab() {
		return tab;
	}

	private boolean isSimpleRestriction(HashMap rolegroup_hmap) {
		if (rolegroup_hmap.keySet().size() != 1) {
			return false;
		}
		Iterator keys = rolegroup_hmap.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			Vector v = (Vector) rolegroup_hmap.get(key);
			if (v.size() != 1)
				return false;
		}
		return true;
	}

	private OWLRestriction getSimpleRestriction(HashMap rolegroup_hmap) {
		Iterator keys = rolegroup_hmap.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			Vector v = (Vector) rolegroup_hmap.get(key);
			return (OWLRestriction) v.elementAt(0);
		}
		return null;
	}

	// 091806
	private boolean containsSimpleRestriction(HashMap rolegroup_hmap) {
		Integer zero = new Integer(0);
		Vector v = (Vector) rolegroup_hmap.get(zero);
		if (rolegroup_hmap.containsKey(zero) && v.size() > 0) {
			return true;
		}
		return false;
	}
}
