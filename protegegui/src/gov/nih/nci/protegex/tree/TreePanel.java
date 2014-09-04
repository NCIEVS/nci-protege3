package gov.nih.nci.protegex.tree;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_ASSOCIATION;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_CONCEPT;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_NOT_SET;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_PARENT;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_PROPERTY;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_RESTRICTION;
import static gov.nih.nci.protegex.tree.TreePanel.PanelType.*;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.TransferableCollection;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
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
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSDatatype;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.action.RoleGroupRestrictionComp;
import gov.nih.nci.protegex.dialog.CreateObjPropertyDialog;
import gov.nih.nci.protegex.dialog.CreatePropertyDialog;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationDialog;
import gov.nih.nci.protegex.dialog.ModifySuperclassDialog;
import gov.nih.nci.protegex.edit.NCIAnnotationsTableModel;
import gov.nih.nci.protegex.edit.NCIConditionsTableModel;
import gov.nih.nci.protegex.edit.NCICreateRolePanel;
import gov.nih.nci.protegex.edit.NCIEditFilter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIFULLSYNTableModel;
import gov.nih.nci.protegex.edit.NCIOWLIcons;
import gov.nih.nci.protegex.edit.NCISelectSuperClsPanel;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.panel.ConceptChangedListener;
import gov.nih.nci.protegex.panel.NCIDoublePanel;
import gov.nih.nci.protegex.util.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class TreePanel extends JPanel implements DropTargetListener,
		ConceptChangedListener {
	public static final long serialVersionUID = 123456791L;

	private Logger logger = Log.getLogger(getClass());

	public static final String UPDATED = "Updated";

	public static final String ACCEPTED = "Accepted";

	private boolean enableAcceptAction = false;

	// (1) need to store which tab this tree panel belongs
	// (2) perform pre-condition checking on drop event
	// for example, for a concept to be retired, the concept must have already
	// been flagged for retirement through preretire action, reject drop if
	// needed.
	//

	public static enum PanelType {
		TYPE_UNKNOWN, TYPE_SPLIT, TYPE_SPLIT_LOWER, TYPE_SPLIT_UPPER, TYPE_PREMERGE, TYPE_PREMERGE_UPPER, TYPE_PREMERGE_LOWER, TYPE_MERGE, TYPE_MERGE_UPPER, TYPE_MERGE_LOWER, TYPE_PRERETIRE, TYPE_RETIRE, TYPE_CLONE, TYPE_CLONE_UPPER, TYPE_CLONE_LOWER, TYPE_DELETE, TYPE_WORKFLOW, TYPE_WORKFLOW_UPPER, TYPE_WORKFLOW_LOWER
	}

	Vector<String> associationNames = null;

	Instance selectedInstance;

	protected TreePath m_clickedPath;

	protected JTree edit_tree;

	// JScrollPane treeView;

	int selected_tab;

	TreeNode root_node;

	DropTarget dt;

	protected DefaultTreeModel model;

	public OWLNamedClass droppedCls = null;

	// Rename
	JDialog renamePopUpDialogBox;

	JButton renameCancelButton;

	// TreePanel thisTree;
	// KnowledgeBase kb;
	OWLModel kb;

	private TreeItems currState = new TreeItems();

	private TreeItems initialState = null;

	// 042106
	private TreeItems item_vec = new TreeItems();

	NCIEditTab tab;

	String code = null;

	String name = null;

	String newname = null;

	private boolean canDrop;

	public PanelType type = PanelType.TYPE_UNKNOWN;

	public PanelType subtype;

	private static final String SUBCLASSOF = NCIEditTab.SUBCLASSOF;

	HashSet hset;

	OWLWrapper wrapper;

	Vector nonEditableProperties;

	private Vector inboundRoles;

	private Vector inboundAssocs;

	private Vector<Cls> oldSubclasses;

	private Vector<String> old_child_vec = null;

	private Vector<String> old_source_role_vec = null;

	private Vector<String> old_source_assoc_vec = null;

	public boolean isNew;

	protected PopupTrigger popupTrigger = null;

	private DropClsListener dropClsListener;

	private boolean displayWorkflowMenu = true;

	private NCIDoublePanel _doublePanel = null;

	private Stack<RDFSClass> tmp_restrs = new Stack<RDFSClass>();

	public void initTmpRestrs() {
		tmp_restrs = new Stack<RDFSClass>();

	}

	public boolean isDisplayWorkflowMenu() {

		return displayWorkflowMenu;
	}

	public void setDisplayWorkflowMenu(boolean display) {
		displayWorkflowMenu = display;
	}

	private ArrayList<ActionListener> _updateListeners = new ArrayList<ActionListener>();

	public Cls getInstance() {
		return (Cls) selectedInstance;
	}

	public void conceptChanged(OWLNamedClass cls, String msg) {
		// handled by the parent DoublePanel
		// ProtegeUI.getModalDialogFactory().showMessageDialog(kb, msg);
	}

	public void reset() {
		while (!tmp_restrs.empty()) {
			RDFSClass rc = tmp_restrs.pop();
			rc.delete();
		}

	}

	private TreePanel(NCIEditTab tab, Instance selectedInstance) {
		this.kb = tab.getOWLModel();
		this.wrapper = tab.getOWLWrapper();

		init();
		this.item_vec.clear();
		this.selectedInstance = selectedInstance;
		this.tab = tab;

	}

	// Merge: lowerTreePanel_Merge = new TreePanel(this, selectedInstance,
	// selected_tab, dmtn, null);
	// lowerTreePanel_Premerge = new TreePanel(this, selectedInstance, dmtn,
	// null);

	public TreePanel(NCIEditTab tab, Instance selectedInstance, TreeNode dmtn,
			String code) {
		this(tab, selectedInstance);

		this.root_node = dmtn;

		currState.clear();
		currState.add((TreeItem) dmtn.getUserObject());

		TreeItem tree_item = (TreeItem) dmtn.getUserObject();
		newname = tree_item.getName();
		name = tree_item.getName();

		model = new DefaultTreeModel(dmtn);
		initialize(dmtn);

		initialState = getCurrentState(true);

		setupDrop();

		hset = getOWLAnnotationProperties();

		this.type = PanelType.TYPE_UNKNOWN;

		setCode(wrapper.getCode(wrapper.getCls(name)));

		old_child_vec = new Vector<String>();
		old_source_role_vec = new Vector<String>();
		old_source_assoc_vec = new Vector<String>();

	}

	// Split: lowerTreePanel_Split = new TreePanel(this, selectedInstance,
	// selected_tab, root, code, v);

	public TreePanel(NCIEditTab tab, Instance selectedInstance, TreeNode dmtn,
			String code, TreeItems treeitems) {
		this(tab, selectedInstance);

		this.root_node = dmtn;

		this.code = code;

		currState.clear();
		currState.add((TreeItem) dmtn.getUserObject());

		TreeItem tree_item = (TreeItem) dmtn.getUserObject();
		newname = tree_item.getName();
		name = tree_item.getName();
		tree_item.setType(TYPE_CONCEPT);

		// 041906
		edit_tree = new JTree(dmtn);

		// 010807
		TreeRenderer renderer = new TreeRenderer();
		edit_tree.setCellRenderer(renderer);
		NCITreeCellEditor editor = new NCITreeCellEditor(edit_tree, renderer);
		edit_tree.setCellEditor(editor);

		model = (DefaultTreeModel) edit_tree.getModel();

		populate(treeitems);

		initialState = getCurrentState(true);

		setupDrop();

		hset = getOWLAnnotationProperties();
		this.type = TYPE_UNKNOWN;

		old_child_vec = new Vector<String>();
		old_source_role_vec = new Vector<String>();
		old_source_assoc_vec = new Vector<String>();

		// initialize(null);
	}

	public TreePanel(NCIEditTab tab, Instance selectedInstance, OWLModel kb) {
		this(tab, selectedInstance);

		initialize(null);
		setupDrop();

		// this.kb = kb;
		hset = getOWLAnnotationProperties();

		// thisTree = this;

		currState.clear();

		this.type = TYPE_UNKNOWN;

		if (selectedInstance != null) {
			String instance_name = tab.getWrapper().getInternalName(
					(Cls) selectedInstance);

			setName(instance_name);

			// setName(selectedInstance.getBrowserText());
			setCode(wrapper.getCode((Cls) selectedInstance));

		}

		old_child_vec = new Vector<String>();
		old_source_role_vec = new Vector<String>();
		old_source_assoc_vec = new Vector<String>();

	}

	public void setIsNew(boolean b) {
		isNew = b;
	}

	public boolean getIsNew() {
		return isNew;
	}

	public boolean containsCls() {
		return droppedCls != null;
	}

	public void add_old_child(String sub) {
		old_child_vec.add(sub);
	}

	public void add_old_source_role(String role) {
		old_source_role_vec.add(role);
	}

	public void add_old_source_assoc(String role) {
		old_source_assoc_vec.add(role);
	}

	private void init() {
		associationNames = new Vector<String>();
		associationNames.add(RDFSNames.Slot.IS_DEFINED_BY);
		associationNames.add(RDFSNames.Slot.SEE_ALSO);
		associationNames.add(OWLNames.Slot.SAME_AS);
		associationNames.add(kb.getOWLDifferentFromProperty().getBrowserText());
		associationNames.add(kb.getOWLEquivalentPropertyProperty()
				.getBrowserText());
		associationNames.add(kb.getOWLDisjointWithProperty().getBrowserText());

		nonEditableProperties = wrapper.getReadOnlyProperties();

		this.inboundRoles = new Vector();
		this.oldSubclasses = new Vector<Cls>();

		this.isNew = false;
	}

	private boolean isNonEditable(String prop_name) {
		if (nonEditableProperties.contains(prop_name)) {
			return true;
		}
		return false;
	}

	private void setCode(String code) {
		if (!code.equals("0"))
			this.code = code;
		else
			this.code = "";
	}

	public void setName(String name) {
		this.name = name;
	}

	// TODO: Move thes next two into OWLWrapper
	private boolean canRetire(OWLNamedClass retireCls) {
		OWLModel okb = (OWLModel) tab.getOWLModel();
		OWLNamedClass retire_root = okb
				.getOWLNamedClass(NCIEditTab.PRERETIRED_CONCEPTS);

		String assignedName = tab.getOWLWrapper().getInternalName(retireCls);

		if (retire_root != null) {
			if (retireCls.isSubclassOf(retire_root)) {
				return true;
			} else if (assignedName.compareTo(NCIEditTab.PRERETIRED_CONCEPTS) == 0) {
				return true;
			}
		}
		return false;
	}

	public void setType(PanelType typ) {
		type = typ;
	}

	public void setSubType(PanelType sub) {
		subtype = sub;
	}

	public OWLNamedClass getDroppedCls() {
		return droppedCls;
	}

	public void setDroppedCls(OWLNamedClass cls, String code) {
		tmp_restrs = new Stack<RDFSClass>();
		droppedCls = cls;
		if (cls != null) {
			setCode(code);
			createTree(cls);
			return;
		}
		selectedInstance = null;
		edit_tree.setModel(null);
		setCode("");
	}

	public void setDroppedCls(OWLNamedClass cls) {
		setDroppedCls(cls, cls != null ? wrapper.getCode(cls) : "");
	}

	public TreeNode getRootNode() {
		return root_node;
	}

	public void initRootNode() {
		root_node = null;
	}

	public TreeItems getInitialState() {
		if (initialState == null)
			return currState;
		return initialState;
	}

	public TreeItems getCurrentState() {
		return getCurrentState(false);
	}

	// DYEE: Possibly can be moved into TreeItems.
	private TreeItems getCurrentState(boolean cloneItems) {
		TreeItems v = new TreeItems();

		if (root_node == null) {
			logger
					.warning("TreePanel getFinalState WARNING: root_node == null");
			return v;
		} else if (root_node.getUserObject() == null) {
			return v;
		}

		TreeItem item = (TreeItem) root_node.getUserObject();
		v.add(item);

		Enumeration enumeration = root_node.children();

		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			item = (TreeItem) dmtn.getUserObject();

			if (cloneItems)
				v.add(item.cloneTreeItem(true));
			else
				v.add(item);
		}

		return v;
	}

	private boolean isUserDefinedRDFProperty(String name) {
		Collection allProperties = kb.getVisibleUserDefinedRDFProperties();
		if (allProperties == null || allProperties.isEmpty())
			return false;

		for (Iterator it = allProperties.iterator(); it.hasNext();) {
			RDFProperty property = (RDFProperty) it.next();
			if (property.isAnnotationProperty() && property.hasObjectRange()
					&& property instanceof DefaultOWLObjectProperty) {
				// if (property.getBrowserText().compareTo(name) == 0)
				if (property.getPrefixedName().compareTo(name) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	private void initialize(TreeNode dmtn) {
		canDrop = true;
		if (dmtn != null) {
			root_node = dmtn;
			createNodes(root_node);

		} else if (selectedInstance != null) {
			root_node = dmtn;
			createNodes(root_node);
		}

		edit_tree = new JTree(root_node);

		model = new DefaultTreeModel(root_node);

		edit_tree.setEditable(true);

		edit_tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		TreeRenderer renderer = new TreeRenderer();

		// renderer.setLeafIcon(new ImageIcon("images/middle.gif"));
		edit_tree.setCellRenderer(renderer);

		setLayout(new BorderLayout());
		// add(treeView, BorderLayout.CENTER);
		add(edit_tree, BorderLayout.CENTER);
	}

	private void populate(TreeItems treeitems) {
		canDrop = true;

		edit_tree.setEditable(true);
		edit_tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		TreeRenderer renderer = new TreeRenderer();

		edit_tree.setCellRenderer(renderer);

		createNodes(treeitems);

		// treeView = new JScrollPane(edit_tree);

		setLayout(new BorderLayout());
		// add(treeView, BorderLayout.CENTER);
		add(edit_tree, BorderLayout.CENTER);
		// addPopup(edit_tree, new PopupTrigger());
	}

	private void createNodes(TreeItems treeitems) {
		// if (selectedInstance == null) {
		// System.out.println("WARNING: selectedInstance == null");
		// return;
		// }

		currState.clear();

		currState.add((TreeItem) root_node.getUserObject());

		if (root_node == null) {
			System.out.println("WARNING: root_node is null.");
			return;
		}
		for (int i = 0; i < treeitems.size(); i++) {
			TreeItem item = (TreeItem) treeitems.elementAt(i);
			if (item.getType() == TYPE_PROPERTY) {
				addProperty(item.getName(), item.getValue(), null);
			} else if (item.getType() == TYPE_RESTRICTION) {
				addRestriction(item.getCls(), item.getIsDefining());

			} else if (item.getType() == TYPE_PARENT) {
				addParent((Cls) item.getCls(), item.getIsDefining());
			} else if (item.getType() == TYPE_ASSOCIATION) {
				addAssociation(item.getProperty(), item.getCls(), item
						.getName(), item.getValue());
			}
		}
	}

	public Vector getDirectSuperClses() {
		if (root_node == null) {
			return null;
		}

		Vector<RDFSClass> v = new Vector<RDFSClass>();
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PARENT) {
				v.add(item.getCls());
			}
		}

		return v;
	}

	private void createNodes(TreeNode top) {
		if (selectedInstance == null) {
			System.out.println("WARNING: selectedInstance == null");
			return;
		}

		currState.clear();
		currState.add((TreeItem) top.getUserObject());
		Cls cls = (Cls) selectedInstance;

		try {
			getSlots(top, cls);
			// getParents(top, (OWLNamedClass) cls);
			getRoles(top, (OWLNamedClass) cls);
			TreeItem[] items = item_vec.sort();

			for (int i = 0; i < items.length; i++) {
				TreeItem item = (TreeItem) items[i];

				// 061306
				// TreeNode childnode = new TreeNode(item, -1);
				TreeNode childnode = new TreeNode(item, item.getType());

				root_node.add(childnode);
			}

			initialState = getCurrentState(true);

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

	private void getSlots(TreeNode dmtn, Cls cls) {
		Collection ownslots = ((OWLNamedClass) cls).getRDFProperties();
		if (ownslots != null) {
			Iterator j = ownslots.iterator();
			while (j.hasNext()) {
				RDFProperty slot = (RDFProperty) j.next();
				// guoi
				Collection slotColl = ((OWLNamedClass) cls)
						.getPropertyValues(slot);

				if (slotColl == null || slotColl.isEmpty())
					continue;
				if (slot.getNamespacePrefix().equalsIgnoreCase("protege")) {
					continue;
				}
				String slotName = slot.getBrowserText();

				if (slotName.equals(Model.Slot.NAME)) {
					continue;
				} else if (slotName.equals(Model.Slot.DIRECT_SUBCLASSES))
					continue;
				else if (slotName.equals(Model.Slot.DIRECT_INSTANCES))
					continue;
				else {
					getOwnslot(slotName, slotColl, slot);
				}
			}
		}
	}

	public Vector getRestrictions() {
		if (root_node == null) {
			return null;
		}

		Vector<RDFSClass> v = new Vector<RDFSClass>();
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_RESTRICTION) {
				v.add(item.getCls());
			}
		}

		return v;
	}

	public Vector getAssociations() {
		if (root_node == null) {
			return null;
		}

		Vector<RDFSClass> v = new Vector<RDFSClass>();
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_ASSOCIATION) {
				v.add(item.getCls());
			}
		}

		return v;
	}

	private static HashSet<String> owlAnnProps = null;

	private HashSet<String> getOWLAnnotationProperties() {
		if (owlAnnProps != null) {
			// nop
		} else {
			HashSet<String> hset = new HashSet<String>();

			if (tab == null) {
				return hset;
			}

			if (kb == null) {
				kb = tab.getOWLModel();
			}

			Collection annotationSlots = kb.getOWLAnnotationProperties();
			for (Iterator iterator = annotationSlots.iterator(); iterator
					.hasNext();) {
				RDFProperty slot = (RDFProperty) iterator.next();
				String slotName = slot.getBrowserText();
				hset.add(slotName);
			}
			owlAnnProps = hset;
		}
		return owlAnnProps;
	}

	private String getStringLanguage(Object obj) {
		if (obj instanceof RDFSLiteral) {
			RDFSLiteral literal = (RDFSLiteral) obj;
			return literal.getLanguage();
		}
		// guoi
		/*
		 * else { return (String) obj; }
		 */
		return "";
	}

	// private void getOwnslot(TreeNode dmtn, String slotname, Collection
	// values)
	private void getOwnslot(String slotname, Collection values, RDFProperty slot) {
		if (hset == null) {
			// System.out.println("WARNING: hset == null in getOwnslot");
			hset = getOWLAnnotationProperties();
			if (hset == null) {
				return;
			}
		}
		if (!hset.contains(slotname)) {
			if (slotname.compareTo(":DIRECT-SUPERCLASSES") != 0
					&& slotname.compareTo(":DIRECT-TYPE") != 0
					&& slotname.compareTo(":ROLE") != 0
					&& slotname.compareTo(SUBCLASSOF) != 0
					&& slotname.compareTo("owl:equivalentClass") != 0) {
				if (values == null)
					return;
				Iterator i = values.iterator();
				while (i.hasNext()) {
					Object obj = i.next();
					TreeItem item = new TreeItem();

					if (obj instanceof OWLNamedClass) {
						OWLNamedClass cls = (OWLNamedClass) obj;
						String internalName = wrapper.getInternalName(cls);
						if (internalName.compareTo("owl:Class") != 0) {
							item.setType(TYPE_ASSOCIATION);
							item.setName(slot.getPrefixedName());
							item.setProperty(slot);
							item.setValue(internalName);
							item.setNameValue(slotname, cls.getBrowserText());
							item.setCls(cls);
							currState.add(item);
							item_vec.add(item);
						}
					}
				}
				return;
			}
		}

		if (values != null) {
			Iterator i = values.iterator();
			while (i.hasNext()) {
				Object obj = i.next();
				if (hset.contains(slotname)) {
					ValueType type = getObjectValueType(obj);

					String entry = convertObjecttoString(obj, type);

					try {
						TreeItem item = new TreeItem();
						if (slotname.compareTo(SUBCLASSOF) == 0) {
							item.setType(TYPE_PARENT);

							item.setCls((RDFSClass) obj);
						}

						else if (associationNames.contains(slotname)
								|| isUserDefinedRDFProperty(slotname)) {
							item.setType(TYPE_ASSOCIATION);

							// 111706

							item.setCls((RDFSClass) obj);
							String targetname = wrapper
									.getInternalName((OWLNamedClass) obj);
							item.setValue(targetname);
							item.setNameValue(slotname, targetname);

						}

						else {
							item.setType(TYPE_PROPERTY);
						}

						item.setName(slot.getPrefixedName());

						if (slotname.compareToIgnoreCase(NCIEditTab.CODE) == 0) {
							if (code == null) {
								item.setValue(entry);
								item.setNameValue(slotname, entry);

								code = entry;
							} else {
								item.setValue(code);
								item.setNameValue(slotname, code);
							}
						}

						// 111706
						else if (item.getType() == TYPE_PROPERTY) {
							// properties

							// String newEntry = ComplexPropertyParser
							// .pipeDelim2XML(entry);
							// newEntry = ComplexPropertyParser
							// .reformatComplexProperty(slotname, newEntry);
							item.setValue(entry);
							item.setLanguage(getStringLanguage(obj));
							item.setNameValue(slotname, entry);

						}

						item.setProperty(slot);
						currState.add(item);
						item_vec.add(item);

					} catch (Exception e) {
						Log.getLogger().log(Level.WARNING, "Exception caught",
								e);
					}

				}

			}
		}
	}

	protected String convertObjecttoString(Object obj, ValueType type) {
		if (type.equals(ValueType.STRING))
			return obj.toString();
		else if (type.equals(ValueType.FLOAT))
			return obj.toString();
		else if (type.equals(ValueType.INTEGER))
			return obj.toString();
		else if (type.equals(ValueType.ANY))
			return obj.toString();
		else if (type.equals(ValueType.BOOLEAN))
			return obj.toString();
		else if (type.equals(ValueType.CLS)) {
			Cls cls = (Cls) obj;
			// ORIG: return cls.getName();
			return cls.getBrowserText();
		} else if (type.equals(ValueType.INSTANCE)) {
			Instance inst = (Instance) obj;
			return inst.getName();
		} else if (type.equals(ValueType.SYMBOL))
			return obj.toString();
		else
			return "Unknown";
	}

	protected ValueType getObjectValueType(Object obj) {
		if (obj instanceof Boolean)
			return ValueType.BOOLEAN;
		else if (obj instanceof Cls)
			return ValueType.CLS;
		else if (obj instanceof Float)
			return ValueType.FLOAT;
		else if (obj instanceof Instance)
			return ValueType.INSTANCE;
		else if (obj instanceof Integer)
			return ValueType.INTEGER;
		else if (obj instanceof String)
			return ValueType.STRING;
		else
			return ValueType.ANY;
	}

	public void dragEnter(DropTargetDragEvent event) {
	}

	public void dragOver(DropTargetDragEvent event) {
	}

	public void dropActionChanged(DropTargetDragEvent event) {
	}

	public void dragExit(DropTargetEvent event) {
	}

	public JTree getTree() {
		return edit_tree;
	}

	public void createTree(Cls cls) {
		selectedInstance = cls;
		String value = "";
		String restriction = "";
		String modifier = "";
		int cardinality = -1;

		setName(wrapper.getInternalName(cls));
		TreeItem item = new TreeItem(TYPE_CONCEPT, this.name, value,
				restriction, modifier, cardinality);
		item.setNameValue(this.name);
		item.setCls((RDFSClass) cls);

		// 061306
		// root_node = new TreeNode(item);
		root_node = new TreeNode(item, TYPE_NOT_SET);

		edit_tree = new JTree(root_node);
		edit_tree.setEditable(true);

		model = new DefaultTreeModel(root_node);

		currState.clear();
		currState.add(item);

		// 042506
		item_vec.clear();
		try {
			getSlots(root_node, cls);
			// getParents(root_node, (OWLNamedClass) cls);
			getRoles(root_node, (OWLNamedClass) cls);
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
		createSortedNodes(root_node);

		initialState = getCurrentState(true);
	}

	public void createSortedNodes(TreeNode root_node) {
		TreeItem[] items = item_vec.sort();
		for (int i = 0; i < items.length; i++) {
			TreeItem item = (TreeItem) items[i];
			TreeNode childnode = new TreeNode(item, item.getType());
			root_node.add(childnode);
		}

		edit_tree = new JTree(root_node);
		model = (DefaultTreeModel) edit_tree.getModel();
		edit_tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		if (popupWindowEnabled) {
			this.enablePopUpWindow();
		}

		TreeRenderer renderer = new TreeRenderer();
		edit_tree.setCellRenderer(renderer);

		// treeView.setViewportView(edit_tree);
		setView(edit_tree);

		setupDrop();
		// addPopup(edit_tree, new PopupTrigger());
	}

	public boolean isPartOfDoublePanel() {
		return this._doublePanel != null;
	}

	public void setView(JTree edit_tree) {

		tab.getScrollPane(type, subtype).setViewportView(edit_tree);
	}

	public void setCanDrop(boolean canDrop) {
		this.canDrop = canDrop;
	}

	public void drop(DropTargetDropEvent e) {
		if (!canDrop)
			return;

		if (subtype == TYPE_CLONE_LOWER && _doublePanel.getNumClses() == 0) {
			_doublePanel.getUpperTreePanel().drop(e);
			return;
		}

		if (wrapper == null) {
			wrapper = tab.getOWLWrapper();
		}

		boolean succeeded = false;
		DataFlavor flavor = TransferableCollection.getCollectionFlavor();

		// JTree tree = getTree(e);
		JTree tree = (JTree) e.getDropTargetContext().getComponent();

		if (e.isDataFlavorSupported(flavor)) {
			try {
				int action = e.getDropAction();
				Collection sources = (Collection) (e.getTransferable()
						.getTransferData(flavor));
				Iterator i = sources.iterator();
				while (i.hasNext()) {
					Object source = i.next();

					if (source instanceof Cls) {
						droppedCls = (OWLNamedClass) source;

						if (wrapper.isRetired(droppedCls)) {
							String msg = "ERROR: Concept "
									+ droppedCls.getBrowserText()
									+ " is not editable.";
							showMessage(msg);
							return;
						} else if (droppedCls.equals(kb.getOWLThingClass())) {
							String msg = "ERROR: Concept "
									+ droppedCls.getBrowserText()
									+ " is not editable.";
							showMessage(msg);
							return;
						}

						String assignedName = wrapper
								.getInternalName(droppedCls);

						setName(assignedName);

						tab.addToListenedToClses((OWLNamedClass) droppedCls,
								this);

						if (wrapper.isNonEditable((OWLNamedClass) droppedCls)) {
							String msg = "ERROR: Concept "
									+ droppedCls.getBrowserText()
									+ " is not editable.";
							showMessage(msg);
							return;
						}
						// Perform pre-condition checking

						if (type == TYPE_SPLIT || type == TYPE_CLONE) {
							if (droppedCls.isSubclassOf(kb.getOWLThingClass())) {
								return;
							}

						}

						else if (type == TYPE_PREMERGE) {
							if (droppedCls.isSubclassOf(kb.getOWLThingClass())) {
								return;
							}

							NCIDoublePanel.DropType typ = _doublePanel
									.populate(droppedCls, subtype);

							if (typ == NCIDoublePanel.DropType.TRYAGAIN) {
								if (subtype == TYPE_PREMERGE_LOWER) {
									_doublePanel.getUpperTreePanel().drop(e);
									return;

								}
								if (subtype == TYPE_PREMERGE_UPPER) {
									_doublePanel.getLowerTreePanel().drop(e);
									return;

								} else {
									return;
								}

							} else if (typ == NCIDoublePanel.DropType.BAD) {
								return;
							}
						}

						else if (type == TYPE_MERGE) {

							NCIDoublePanel.DropType typ = _doublePanel
									.populate(droppedCls, subtype);

							if (typ == NCIDoublePanel.DropType.TRYAGAIN) {
								if (subtype == TYPE_MERGE_LOWER) {
									_doublePanel.getUpperTreePanel().drop(e);
									return;

								}
								if (subtype == TYPE_MERGE_UPPER) {
									_doublePanel.getLowerTreePanel().drop(e);
									return;

								} else {
									return;
								}

							} else if (typ == NCIDoublePanel.DropType.BAD) {
								return;
							}

						}

						else if (type == TYPE_PRERETIRE) {
							OWLNamedClass retireCls = (OWLNamedClass) droppedCls;

							if (canRetire(retireCls)) {
								String text = retireCls.getBrowserText()
										+ " has already been flagged for retirement.";
								showMessage(text);

								return;
							}

							inboundRoles = wrapper
									.getRestrictionSources(retireCls);

							inboundAssocs = wrapper
									.getInverseAssocStrings(retireCls);

							Collection c = wrapper
									.getNamedSubclasses(retireCls);
							for (Iterator it = c.iterator(); it.hasNext();) {
								Cls cls = (Cls) it.next();
								oldSubclasses.add(cls);
							}

							tab.getPreRetirePanel().setDroppedCls(droppedCls);

							tab.getPreRetirePanel().enableButton(
									"unretireButton_Preretire", false);
							tab.getPreRetirePanel().enableButton(
									"preretireButton_Preretire", true);
							tab.getPreRetirePanel().enableButton(
									"saveButton_Preretire", false);
							tab.getPreRetirePanel().enableButton(
									"clearButton_Preretire", true);

						}

						else if (type == TYPE_RETIRE) {

							if (!canRetire(droppedCls)) {

								String text = droppedCls.getBrowserText()
										+ " has not been flagged for retirement.";

								JComponent c = ComponentFactory
										.createLabel(text);
								ModalDialog.showDialog(tab, c, "Message",
										ModalDialog.MODE_CLOSE);

								return;

							}
							// tab.setDroppedCls(droppedCls);

							tab.getRetirePanel().enableButton(
									"unretireButton_Retire", true);
							tab.getRetirePanel().enableButton(
									"retireButton_Retire", true);
							tab.getRetirePanel().enableButton(
									"saveButton_Retire", false);
							tab.getRetirePanel().enableButton(
									"clearButton_Retire", true);
						}

						if (_doublePanel != null)
							_doublePanel.setupWhileLoadingConcept(this);
						createTree((Cls) droppedCls);

						// KLO 052406
						setCanDrop(false);

					}
				}
				e.acceptDrop(action);
			} catch (Exception ex) {
				Log.getLogger().warning(Log.toString(ex));
			}
		} else {
			e.rejectDrop();
			Log.getLogger().warning("unsupported flavor: " + e);
		}
		e.dropComplete(succeeded);
		clearDropSelection(tree);

		setupDrop();
		// addPopup(edit_tree, new PopupTrigger());
		if (dropClsListener != null)
			dropClsListener.updateCls(this, droppedCls);
	}

	public void setDropClsListener(DropClsListener listener) {
		dropClsListener = listener;
	}

	private void showMessage(String text) {
		JComponent c = ComponentFactory.createLabel(text);
		ModalDialog.showDialog(tab, c, "Message", ModalDialog.MODE_CLOSE);
	}

	private void clearDropSelection(JTree tree) {
		tree.putClientProperty(DefaultRenderer.DROP_TARGET, null);
		tree.putClientProperty(DefaultRenderer.DROP_TARGET_AREA, null);
	}

	public void setupDrop() {
		dt = new DropTarget(edit_tree, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}

	public TreeNode addProperty(String name, String value, Cls cls) {

		TreeItem item = new TreeItem();
		item.setType(TYPE_PROPERTY);
		item.setName(name);
		item.setValue(value);

		RDFProperty po = wrapper.getRDFProperty(name);
		item.setNameValue(po.getBrowserText(), value);

		if (!tab.getFilter().canUpdate(NCIEditFilter.UpdateAction.ADD, item,
				currState)) {
			tab.showError(getDisplayName());
			return null;
		}

		if (name.compareTo(NCIEditTab.PREDEPRECATIONPARENTCONCEPT) == 0
				|| name.compareTo(NCIEditTab.PREDEPRECATIONCHILDCONCEPT) == 0) {
			if (cls == null) {
				System.out.println("WARNING TreePanel:");
				System.out.println("  * addProperty name: " + name);
				System.out.println("  * addProperty value: " + value);
				System.out.println("  * addProperty cls: null");
			}
		}

		if (cls != null) {
			item.setCls((RDFSClass) cls);
		}

		if (root_node == null) {
			System.out.println("root is null");

		}
		return addObject(root_node, item, true);
	}

	public TreeNode addParent(Cls cls) {
		return addParent(cls, false);
	}

	public TreeNode addParent(Cls cls, boolean isDefining) {
		TreeItem item = new TreeItem();
		item.setType(TYPE_PARENT);
		item.setName(SUBCLASSOF);

		String value = wrapper.getInternalName(cls);
		item.setValue(value);
		if (isDefining) {
			item.setNameValue("owl:equivalentClass", value);
		} else {
			item.setNameValue(SUBCLASSOF, value);
		}

		item.setCls((RDFSClass) cls);
		item.setIsDefining(isDefining);

		if (root_node == null) {
			System.out.println("root is null");

		}

		return addObject(root_node, item, true);
	}

	/**
	 * public TreeNode addRestriction(RDFSClass aClass) { TreeItem item = new
	 * TreeItem(); item.setType(TYPE_RESTRICTION);
	 * 
	 * item.setCls(aClass); item.setNameValue(aClass.getBrowserText());
	 * 
	 * return addObject(root_node, item, true); }
	 **/

	public TreeNode addRestriction(RDFSClass aClass, boolean isDefining) {

		TreeItem item = new TreeItem();
		item.setType(TYPE_RESTRICTION);

		item.setCls(aClass.createClone());
		item.setNameValue(aClass.getBrowserText());

		item.setIsDefining(isDefining);

		return addObject(root_node, item, true);
	}

	private int findNodePosition(TreeNode parent, TreeItem child) {
		if (parent.getChildCount() == 0)
			return 0;

		String key_1 = child.getKey();
		TreeItems items = new TreeItems();
		Enumeration enumeration = parent.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			items.add(item);
		}

		TreeItem[] sortedItems = items.sort();
		for (int i = 0; i < sortedItems.length; i++) {
			TreeItem item = (TreeItem) sortedItems[i];
			String key_2 = item.getKey();
			if (key_2.compareTo(key_1) > 0)
				return i;
		}
		return parent.getChildCount();
	}

	private TreeNode addObject(TreeNode parent, TreeItem child,
			boolean shouldBeVisible) {

		if (parent == null) {
			System.out.println("WARNING addObject -- parent = null.");
		}

		// 061306
		// TreeNode childNode = new TreeNode(child);
		TreeNode childNode = new TreeNode(child, child.getType());

		model.insertNodeInto(childNode, parent,
		// parent.getChildCount());
				findNodePosition(parent, child));

		model.reload();

		if (shouldBeVisible) {
			edit_tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		currState.add(child);
		return childNode;
	}

	/**
	 * private void removeObject(TreeNode node) {
	 * model.removeNodeFromParent(node); model.reload(); }
	 */

	public boolean deleteProperty(String name, String value) {
		if (root_node == null) {
			logger.warning("WARNING unable to delete property " + name + " ( "
					+ value + " ) " + " -- root node is null.");
			return false;

		}
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getName().compareTo(name) == 0
					&& item.getValue().compareTo(value) == 0) {
				model.removeNodeFromParent(dmtn);
				model.reload();

				return true;
			}
		}
		return false;
	}

	public boolean deleteProperty(String name, String value, Cls cls) {
		// TreeNode selectedNode = null;
		if (cls == null) {
			return deleteProperty(name, value);
		}

		if (root_node == null) {
			logger.warning("WARNING unable to delete property " + name + " ( "
					+ value + " ) " + " -- root node is null.");
			return false;

		}

		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getName().compareTo(name) == 0
					&& wrapper.getInternalName(item.getCls()).compareTo(
							wrapper.getInternalName(cls)) == 0) {
				model.removeNodeFromParent(dmtn);
				model.reload();

				return true;
			}
		}

		return false;
	}

	public boolean deleteProperty(String name, Cls cls) {
		if (root_node == null) {
			logger.warning("WARNING deleteProperty unable to delete property ");
			return false;

		}

		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getName().compareTo(name) == 0
					&& wrapper.getInternalName(item.getCls()).compareTo(
							wrapper.getInternalName(cls)) == 0) {
				model.removeNodeFromParent(dmtn);
				model.reload();

				return true;
			}
		}
		return false;
	}

	public boolean deleteProperties(String name) {
		// TreeNode selectedNode = null;
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getName().compareTo(name) == 0
					&& item.getType() == TYPE_PROPERTY) {
				model.removeNodeFromParent(dmtn);
				model.reload();
			}
		}
		return true;
	}

	public String getPropertyValue(String name) {
		if (root_node == null) {
			return null;
		}

		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PROPERTY) {
				if (item.getName().compareTo(name) == 0) {
					return item.getValue();
				}
			}
		}
		return null;
	}

	public boolean isSubclassOf(String value) {
		if (root_node == null) {
			logger.warning("WARNING: isSubclasOf root == null");
		}

		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PARENT) {
				if (item.getValue().compareTo(value) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public Vector<String> getPropertyValues(String name) {
		if (root_node == null) {
			logger.warning("WARNING getPropertyValues root == null");
		}

		Vector<String> v = new Vector<String>();
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PROPERTY) {
				if (item.getName().compareTo(name) == 0) {
					v.add(item.getValue());
				}
			}
		}
		return v;
	}

	public Vector<RDFSClass> getPropertyObjectValues(String name) {
		if (root_node == null) {
			logger.warning("WARNING getPropertyValues root == null");
		}

		Vector<RDFSClass> v = new Vector<RDFSClass>();
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PROPERTY) {
				if (item.getName().compareTo(name) == 0) {

					if (item.getCls() == null) {
						if (name
								.compareTo(NCIEditTab.PREDEPRECATIONPARENTCONCEPT) == 0
								|| name
										.compareTo(NCIEditTab.PREDEPRECATIONCHILDCONCEPT) == 0) {
							item.setCls((RDFSClass) kb.getRDFSNamedClass(item
									.getValue()));

						}

					}

					v.add(item.getCls());
				}
			}
		}
		return v;
	}

	public boolean deleteProperty(String name) {
		// TreeNode selectedNode = null;
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getName().compareTo(name) == 0) {
				model.removeNodeFromParent(dmtn);
				model.reload();
				return true;
			}
		}

		return false;
	}

	public boolean deleteParent(String value) {
		return deleteProperty(SUBCLASSOF, value);
	}

	public boolean deleteParent(Cls cls) {
		return deleteProperty(SUBCLASSOF, wrapper.getInternalName(cls), cls);
	}

	public boolean deleteRestriction(String name, String value, String modifier) {
		// TreeNode selectedNode = null;
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_RESTRICTION) {
				if (item.getName().compareTo(name) == 0
						&& item.getValue().compareTo(value) == 0
						&& item.getModifier().compareTo(modifier) == 0) {
					model.removeNodeFromParent(dmtn);
					model.reload();
				}
			}
		}
		return true;
	}

	public String getCode() {
		if (tab.byCode()) {
			return ((OWLNamedClass) this.selectedInstance).getPrefixedName();
		} else {
			return code;
		}
	}

	public String getDisplayName() {
		TreeNode node = getRootNode();
		if (node == null) {
			return null;
		}

		TreeItem item = (TreeItem) node.getUserObject();
		return item.getNameValue();
	}

	/**
	 * Returns the loaded local name (as opposed to its display name).
	 * 
	 * @return the loaded local name.
	 */
	public String getLocalName() {
		TreeNode node = getRootNode();
		if (node == null)
			return null;

		TreeItem item = (TreeItem) node.getUserObject();
		String name = item.getName();
		if (name != null && name.length() > 0)
			return name;
		return wrapper.getInternalName(item.getCls());
	}

	public Vector getInboundRoles() {
		return inboundRoles;
	}

	public Vector getInboundAssocs() {
		return inboundAssocs;
	}

	public Vector getOldSubclasses() {
		return oldSubclasses;
	}

	class PopupTrigger extends MouseAdapter {
		protected JPopupMenu m_popup = new JPopupMenu();

		private ExpandCollapsePropertyAction m_action = new ExpandCollapsePropertyAction();

		protected void buildPopupMenu(MouseEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();
			TreeItem item = (TreeItem) selectedNode.getUserObject();

			edit_tree.remove(m_popup);
			m_popup = new JPopupMenu();

			if (item.getType() == TYPE_CONCEPT) {
				m_popup.add(m_action);
				if (TreePanel.this.isEnabled()) {
					m_popup.addSeparator();
					m_popup.add(new AddPropertyAction());
					m_popup.addSeparator();
					m_popup.add(new CreateARoleRestrictionAction());
					m_popup.addSeparator();
					m_popup.add(new AddAssociationAction());
					m_popup.addSeparator();
					m_popup.add(new AddParentAction());
					m_popup.add(new CreateARoleGroupAction());
				}
				if (enableAcceptAction) {
					m_popup.add(new AcceptAction(enableAcceptAction));
				}
			} else if (item.getType() == TYPE_PROPERTY) {
				if (TreePanel.this.isEnabled()) {
					m_popup.add(new DeletePropertyAction());
					m_popup.addSeparator();
					m_popup.add(new ModifyPropertyAction());
				}
			} else if (item.getType() == TYPE_ASSOCIATION) {
				if (TreePanel.this.isEnabled()) {
					m_popup.add(new DeletePropertyAction());
					m_popup.addSeparator();
					m_popup.add(new ModifyAssociationAction());
				}
			} else if (item.getType() == TYPE_RESTRICTION) {
				if (TreePanel.this.isEnabled()) {
					m_popup.add(new DeletePropertyAction());
					m_popup.addSeparator();
					m_popup.add(new ModifyARestrictionAction());
				}
			} else if (item.getType() == TYPE_PARENT) {
				if (TreePanel.this.isEnabled()) {
					m_popup.add(new DeletePropertyAction());
					m_popup.add(new ModifyParentAction());
				}
			}

			int n = m_popup.getComponentCount();
			if (n >= 5) {
				// this means we're on the concept, can't copy pase
			} else {
				if (n > 0)
					m_popup.addSeparator();

				m_popup.add(new CopyPropertyAction());
				if (TreePanel.this.isEnabled()) {
					// m_popup.addSeparator();
					m_popup.add(new PastePropertyAction());
				}
			}

			if (displayWorkflowMenu) {
				String code = wrapper
						.getCode((Cls) TreePanel.this.selectedInstance);
				m_popup.add(WorkflowUtil.createAssignmentAction(
						"Create Workflow Task", code, name + " (" + code
								+ ") needs updating."));
				edit_tree.add(m_popup);
			}
		}

		private void checkEvent(MouseEvent e) {
			if (!e.isPopupTrigger())
				return;

			int x = e.getX();
			int y = e.getY();
			TreePath path = edit_tree.getPathForLocation(x, y);
			if (path != null) {
				edit_tree.setSelectionPath(path);
				buildPopupMenu(e);

				if (edit_tree.isExpanded(path)) {
					m_action.putValue(Action.NAME, "Collapse");
				} else {
					m_action.putValue(Action.NAME, "Expand");
				}

				m_popup.show(edit_tree, x, y);
				m_clickedPath = path;
			}
		}

		public void mouseClicked(MouseEvent event) {
			checkEvent(event);
		}

		public void mousePressed(MouseEvent event) {
			checkEvent(event);
		}

		public void mouseReleased(MouseEvent e) {
			checkEvent(e);
		}
	}

	protected boolean popupWindowEnabled = false;

	public void enablePopUpWindow() {

		popupTrigger = new PopupTrigger();

		popupWindowEnabled = true;

		edit_tree.addMouseListener(popupTrigger);

	}

	public void disablePopUpWindow() {
		edit_tree.removeMouseListener(popupTrigger);
		popupWindowEnabled = false;
	}

	private boolean modifyRestriction(TreeItem item) // RDFSClass aClass,
	// boolean defining)
	{
		OWLClass aClass_old = (OWLClass) item.getCls();// aClass;

		boolean defining = item.getIsDefining();

		if (aClass_old != null) {
			logger.fine("TreePanel modifyRestriction : "
					+ aClass_old.getBrowserText());
		}

		if (aClass_old instanceof OWLUnionClass
				|| aClass_old instanceof OWLIntersectionClass) {
			RoleGroupRestrictionComp rrc = new RoleGroupRestrictionComp(tab,
					kb, wrapper, (OWLNamedClass) getDroppedCls(), aClass_old,
					defining);

			if (rrc.getRestriction() != null) {
				item.setCls(rrc.getRestriction().createClone());
				tmp_restrs.push(item.getCls());
				item.setNameValue(rrc.getRestriction().getBrowserText());
				item.setIsDefining(rrc.getIsDefining());

				return true;
			} else {
				return false;
			}
		} else {
			OWLRestriction r = (OWLRestriction) aClass_old;
			RDFProperty p = r.getOnProperty();

			String fillerText = r.getFillerText();
			String prop_modifier = wrapper.getRestrictionType(r);

			String label = wrapper.restrictionType2Label(prop_modifier);
			edu.stanford.smi.protege.model.Cls metaCls = wrapper
					.getMetaClsByName(label);

			NCICreateRolePanel dlg = new NCICreateRolePanel(this, metaCls, p,
					fillerText, (RDFSClass) tab.getSelectedCls(), r, true);
			dlg.setIsDefining(defining);
			final String modify_label = "Modify a Restriction";
			int ans = ModalDialog.showDialog(tab, dlg, modify_label,
					ModalDialog.MODE_OK_CANCEL);
			if (ans == ModalDialog.OPTION_OK) {
				r = dlg.getResult();
				if (r != null) {
					// item.setCls(r.createClone());
					item.setCls(r);
					tmp_restrs.push(item.getCls());
					item.setNameValue(r.getBrowserText());
					item.setIsDefining(dlg.getIsDefining());
					return true;
				}
				return false;
			} else {
				return false;
			}

		}

	}

	private void getRoles(TreeNode dmtn, OWLNamedClass cls) {

		HashSet<String> owlEquivClassSet = new HashSet<String>();

		HashSet<String> hset = new HashSet<String>();
		TreeItems v = getDefinitionItems(cls,
				NCIConditionsTableModel.SET_SUPERCLASS);
		for (int i = 0; i < v.size(); i++) {
			TreeItem item = (TreeItem) v.elementAt(i);
			String internalName = item.getCls().getBrowserText();

			// String value = SUBCLASSOF + ": " + internalName;
			String value = item.getName() + ": " + internalName;

			owlEquivClassSet.add(internalName);

			if (!hset.contains(value)) {
				// 121406
				item.setType(TYPE_PARENT);

				item.setNameValue(value);
				item.setIsDefining(true);
				currState.add(item);
				item_vec.add(item);
				hset.add(value);
			}
		}
		v = getDefinitionItems(cls, NCIConditionsTableModel.SET_RESTRICTION);
		for (int i = 0; i < v.size(); i++) {
			TreeItem item = (TreeItem) v.elementAt(i);
			String value = item.getCls().getBrowserText();
			String mod = "";
			if (item.getCls() instanceof OWLSomeValuesFrom) {
				mod = "some";
			} else {
				mod = "only";
			}
			if (!hset.contains(value)) {
				// 121406
				item.setType(TYPE_RESTRICTION);

				item.setNameValue(value);
				item.setIsDefining(true);
				item.setModifier(mod);
				currState.add(item);
				item_vec.add(item);
				hset.add(value);
			}
		}

		v = getDirectSuperclassItems(cls,
				NCIConditionsTableModel.SET_SUPERCLASS);
		for (int i = 0; i < v.size(); i++) {
			TreeItem item = (TreeItem) v.elementAt(i);
			String internalName = item.getCls().getBrowserText();
			String value = SUBCLASSOF + ": " + item.getCls().getBrowserText();
			if (!hset.contains(value)
					&& !owlEquivClassSet.contains(internalName)) {
				// 121406
				item.setType(TYPE_PARENT);

				item.setNameValue(value);
				item.setIsDefining(false);
				currState.add(item);
				item_vec.add(item);
				hset.add(value);
			}
		}

		v = getDirectSuperclassItems(cls,
				NCIConditionsTableModel.SET_RESTRICTION);
		for (int i = 0; i < v.size(); i++) {
			TreeItem item = (TreeItem) v.elementAt(i);
			String value = item.getCls().getBrowserText();
			String mod = "";
			if (item.getCls() instanceof OWLSomeValuesFrom) {
				mod = "some";
			} else {
				mod = "only";
			}
			if (!hset.contains(value)) {
				// 121406
				item.setType(TYPE_RESTRICTION);

				item.setNameValue(value);
				item.setIsDefining(false);
				item.setModifier(mod);
				currState.add(item);
				item_vec.add(item);
				hset.add(value);
			}
		}

	}

	public boolean deleteRestriction(RDFSClass r) {
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_RESTRICTION) {
				if (item.toString().compareTo(r.getBrowserText()) == 0) {
					model.removeNodeFromParent(dmtn);
					model.reload();
				}
			}
		}
		return true;
	}

	public boolean deleteAssociation(RDFSClass r) {
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_ASSOCIATION) {
				if (item.getCls().getBrowserText()
						.compareTo(r.getBrowserText()) == 0) {
					model.removeNodeFromParent(dmtn);
					model.reload();
				}
			}
		}
		return true;
	}

	private String[] getAllowableValues(RDFProperty property) {
		boolean rangeDefined = property.isRangeDefined();
		if (!rangeDefined)
			return null;
		boolean includingSuperproperties = false;
		Vector<String> v = new Vector<String>();
		Collection c = property.getRanges(includingSuperproperties);
		if (c == null)
			return null;
		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			Object value = (Object) iterator.next();

			if (value instanceof DefaultRDFSDatatype) {
				return null;
			} else {
				Collection values = ((OWLDataRange) value).getOneOf()
						.getValues();
				if (values == null)
					return null;
				if (values.size() > 0) {
					for (Iterator iterator2 = values.iterator(); iterator2
							.hasNext();) {
						Object value2 = (Object) iterator2.next();
						v.add(value2.toString());
					}
				}
			}
		}
		Object[] objs = v.toArray();
		String[] allowedvalues = new String[objs.length];

		for (int i = 0; i < objs.length; i++) {
			allowedvalues[i] = (String) objs[i];
		}
		return allowedvalues;
	}

	public TreeNode addAssociation(Slot slot, Cls cls, String name, String value) {
		if (value.compareTo("owl:Class") == 0)
			return null;

		TreeItem item = new TreeItem();
		item.setType(TYPE_ASSOCIATION);
		item.setName(name);

		RDFProperty aProperty = (RDFProperty) slot;
		item.setProperty(aProperty);

		RDFSClass aClass = (RDFSClass) cls;
		item.setCls(aClass);

		item.setValue(value);
		item.setNameValue(name, value);

		return addObject(root_node, item, true);
	}

	public int removeEquivalentSuperclasses() {
		if (root_node == null) {
			System.out
					.println("WARNING: removeEquivalentSuperclasses  root_node == null");
			root_node = getRootNode();
		}

		int n = 0;
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PARENT && item.getIsDefining())
			// if (item.getName().compareTo("owl:equivalentClass") == 0)
			{
				String value = wrapper.getInternalName(item.getCls());
				item.setNameValue(SUBCLASSOF, value);
				// model.removeNodeFromParent(dmtn);
				// model.reload();
				item.setName(SUBCLASSOF);
				item.setIsDefining(false);
				n++;
			}
		}
		return n;
	}

	public Vector<RDFSClass> getEquivalentSuperclasses() {
		Vector<RDFSClass> v = new Vector<RDFSClass>();

		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PARENT && item.getIsDefining()) {
				v.add(item.getCls());
			}
		}
		return v;
	}

	private int getSuperclassCount() {
		int n = 0;
		Enumeration enumeration = root_node.children();
		while (enumeration.hasMoreElements()) {
			TreeNode dmtn = (TreeNode) enumeration.nextElement();
			TreeItem item = (TreeItem) dmtn.getUserObject();
			if (item.getType() == TYPE_PARENT) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Adds the update listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void addUpdateActionListener(ActionListener listener) {
		_updateListeners.add(listener);
	}

	/**
	 * Removes the update listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void removeUpdateActionListener(ActionListener listener) {
		_updateListeners.remove(listener);
	}

	/**
	 * Fires an update event.
	 * 
	 * @param event
	 *            The action event.
	 */
	protected void fireUpdateEvent(ActionEvent event) {
		for (ActionListener listener : _updateListeners)
			listener.actionPerformed(event);
	}

	/**
	 * Fires an update event with a specific text message.
	 * 
	 * @param text
	 *            The text message.
	 */
	protected void fireUpdateEvent(String text) {
		fireUpdateEvent(new ActionEvent(this, 0, text));
	}

	/**
	 * Sets the NCIDoublePanel.
	 * 
	 * @param panel
	 *            The NCIDoublePanel.
	 */
	public void setDoublePanel(NCIDoublePanel panel) {
		_doublePanel = panel;
	}

	/**
	 * Expand and collapses the tree in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class ExpandCollapsePropertyAction extends AbstractAction {
		private static final long serialVersionUID = 6319054364742750938L;

		public void actionPerformed(ActionEvent e) {
			if (m_clickedPath == null)
				return;
			if (edit_tree.isExpanded(m_clickedPath))
				edit_tree.collapsePath(m_clickedPath);
			else
				edit_tree.expandPath(m_clickedPath);
		}
	}

	/**
	 * Deletes a property in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class DeletePropertyAction extends AbstractAction {
		private static final long serialVersionUID = 6344855743546945457L;

		public DeletePropertyAction() {
			super("Delete");
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();
			TreeItem item = (TreeItem) selectedNode.getUserObject();

			if (!tab.getFilter().canUpdate(NCIEditFilter.UpdateAction.DELETE,
					item, currState)) {
				tab.showError(getDisplayName());
				return;
			}

			if (item.getType() == TYPE_PROPERTY
					|| item.getType() == TYPE_ASSOCIATION) {
				String prop_name = item.getName();

				if (wrapper == null) {
					wrapper = tab.getOWLWrapper();
				}

				if (wrapper.isReadOnlyProperty(prop_name)) {
					MsgDialog.warning(tab, "Cannot delete " + prop_name
							+ "; it is read only.");
					return;
				}
			} else if (item.getType() == TYPE_PARENT) {
				int n = getSuperclassCount();
				if (n == 1) {
					MsgDialog.warning(tab,
							"Cannot delete the last named superclass.");
					return;
				}
			}

			if (selectedNode.getParent() != null) {
				model.removeNodeFromParent(selectedNode);
				model.reload();
				edit_tree.repaint();
			} else {
				System.out.println("WARNING: selectedNode.getParent() == null");
			}
			fireUpdateEvent(UPDATED);
		}
	}

	/**
	 * Modifies a property in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class ModifyPropertyAction extends AbstractAction {
		private static final long serialVersionUID = -5016246082490044575L;

		public ModifyPropertyAction() {
			super("Modify Property");
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();
			TreeItem item = (TreeItem) selectedNode.getUserObject();

			if (item.getType() != TYPE_PROPERTY) {
				return;
			}

			if (!tab.getFilter().canUpdate(NCIEditFilter.UpdateAction.MODIFY,
					item, currState)) {
				tab.showError(getDisplayName());
				return;
			}

			String prop_name = item.getName();
			String prop_value = item.getValue();

			if (wrapper == null) {
				wrapper = tab.getOWLWrapper();
			}

			if (wrapper.isReadOnlyProperty(prop_name)) {
				MsgDialog.warning(tab, "Cannot modify " + prop_name
						+ "; it is read only.");
				return;
			}

			if (isNonEditable(prop_name)) {
				MsgDialog.warning(tab, "Cannot add " + prop_name
						+ "; it is created by computer.");
				return;
			}

			if (prop_name.equals(NCIEditTab.ALTLABEL)) {
				NCIFULLSYNTableModel tableModel = new NCIFULLSYNTableModel(tab
						.getEditPanel());
				CustomizedAnnotationDialog dialog = new CustomizedAnnotationDialog(
						tab.getEditPanel(), prop_name, prop_value, -1,
						tableModel);

				if (!dialog.cancelBtnPressed) {
					String newValue = dialog.getValue();
					String ptname = ComplexPropertyParser
							.getPtNciTermName(newValue);
					if (ptname != null) {
						if (!newValue.equals(item.getValue())) {
							int ans = MsgDialog
									.yesOrNo(tab,
											"WARNING: Preferred Name changed, continue?");
							if (ans == MsgDialog.YES_OPTION) {

							} else {
								return;
							}

						}
					}

					item.setValue(newValue);
					item.setNameValue(prop_name, item.getValue());

					selectedNode.setUserObject((Object) item);

					if (ptname != null) {
						if (tab.useNCIRules()) {
							currState.synchronizePreferredName(ptname);
						}
					}
					model.reload();
					fireUpdateEvent(UPDATED);
				}
				return;
			}

			if (tab.isComplexProp(prop_name)) {

				CustomizedAnnotationDialog dialog = new CustomizedAnnotationDialog(
						tab.getEditPanel(), prop_name, prop_value, -1,
						new NCIAnnotationsTableModel(kb));

				if (!dialog.cancelBtnPressed) {
					String newValue = dialog.getValue().trim();
					item.setValue(newValue);
					item.setNameValue(prop_name, item.getValue());

					selectedNode.setUserObject((Object) item);
					model.reload();
					fireUpdateEvent(UPDATED);
				}
				return;
			}

			RDFProperty property = kb.getRDFProperty(prop_name);
			String[] allowedvalues = getAllowableValues(property);
			if (allowedvalues != null) {
				JList valueList;
				valueList = ComponentFactory.createList(null);
				valueList.getSelectionModel().setSelectionMode(
						ListSelectionModel.SINGLE_SELECTION);

				Arrays.sort(allowedvalues);
				java.util.List list = Arrays.asList(allowedvalues);
				valueList.setListData(list.toArray());
				// valueList.setSelectedIndex(0);
				JScrollPane comp = new JScrollPane(valueList);

				valueList.setSelectedValue(prop_value, true);

				String label = property.getBrowserText();
				LabeledComponent lc = new LabeledComponent(label, comp);

				lc.setPreferredSize(new Dimension(400, 120));
				int r = ProtegeUI.getModalDialogFactory().showDialog(tab, lc,
						"Add Annotation", ModalDialogFactory.MODE_OK_CANCEL);
				if (r == ModalDialogFactory.OPTION_OK) {

					String newValue = (String) valueList.getSelectedValue();
					// TODO: Bob fix another UTF8
					if (newValue.compareTo("") == 0) {
						MsgDialog.warning(tab, "Property value is mandatory.");
					} else {
						item.setValue(newValue);
						item.setNameValue(prop_name, item.getValue());
						selectedNode.setUserObject((Object) item);
						model.reload();
						fireUpdateEvent(UPDATED);
					}
				}
				return;
			}

			boolean ok = false;
			String newValue = prop_value;
			while (!ok) {

				JTextArea textArea = new JTextArea();
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				textArea.setText(newValue);

				Component comp = new JScrollPane(textArea);
				LabeledComponent lc = new LabeledComponent(prop_name, comp);

				lc.setPreferredSize(new Dimension(400, 120));
				int r = ProtegeUI.getModalDialogFactory().showDialog(
						tab.getEditPanel(), lc, "Edit Annotation",
						ModalDialogFactory.MODE_OK_CANCEL, textArea);
				if (r == ModalDialogFactory.OPTION_OK) {
					newValue = textArea.getText();
					newValue = StringUtil.cleanString(newValue.trim(), false);

					if (newValue.compareTo("") == 0) {
						MsgDialog.warning(tab, "Property value is mandatory.");
					} else {
						if (!newValue.equals(item.getValue())) {
							if (prop_name.equals("Preferred_Name")) {
								int ans = MsgDialog
										.yesOrNo(tab,
												"WARNING: Preferred Name changed, continue?");
								if (ans == MsgDialog.YES_OPTION) {

								} else {
									return;
								}
							}

						}
						item.setValue(newValue);
						item.setNameValue(prop_name, item.getValue());
						selectedNode.setUserObject((Object) item);
						if (prop_name.equals("Preferred_Name")) {

							if (tab.useNCIRules()) {
								currState
										.synchronizePreferredNameFullSyn(newValue);
							}
						}
						model.reload();
						ok = true;
						fireUpdateEvent(UPDATED);
					}
				} else {
					ok = true;
				}
			}
		}
	}

	/**
	 * Modifies a restriction in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class ModifyARestrictionAction extends AbstractAction {
		private static final long serialVersionUID = -4047406783349007881L;

		public ModifyARestrictionAction() {
			super("Modify a restriction ...", NCIOWLIcons.getImageIcon("edit"));
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();
			TreeItem item = (TreeItem) selectedNode.getUserObject();

			if (item.getType() != TYPE_RESTRICTION) {
				return;
			}

			boolean retval = modifyRestriction(item);
			if (retval) {
				selectedNode.setUserObject((Object) item);

				model.reload();
				edit_tree.repaint();
				fireUpdateEvent(UPDATED);
			}
		}
	}

	/**
	 * Adds a property in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class AddPropertyAction extends AbstractAction {
		private static final long serialVersionUID = 5345526405119907045L;

		public AddPropertyAction() {
			super("Add Property");
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();

			if (selectedNode != null) {
				// String currValue = (String)
				// selectedNode.getUserObject().toString();

				boolean ok = false;
				String prop_name = "";
				String prop_value = "";
				while (!ok) {
					boolean badUTF8 = false;

					CreatePropertyDialog dlg = new CreatePropertyDialog(tab,
							prop_name, prop_value);

					if (dlg.cancelBtnPressed) {
						ok = true;
					} else {
						prop_name = dlg.getPropertyName();
						prop_value = StringUtil.cleanString(dlg
								.getPropertyValue().trim(), false);

						for (int i = 0; i < prop_value.length(); i++) {
							if (prop_value.codePointAt(i) < 32) {
								badUTF8 = true;
							}
						}

						if (badUTF8) {
							MsgDialog
									.warning(tab,
											"Property value cannot include UTF8 chars below 32.");
						} else if (prop_name.compareTo("") != 0
								&& prop_value.compareTo("") != 0) {
							if (prop_name.equals(NCIEditTab.ALTLABEL)) {
								String ptnam = ComplexPropertyParser
										.getPtNciTermName(prop_value);
								if (ptnam != null) {
									int ans = MsgDialog
											.yesOrNo(tab,
													"WARNING: Preferred Name changed, continue?");
									if (ans == MsgDialog.YES_OPTION) {

									} else {
										return;
									}
									if (tab.useNCIRules()) {
										currState
												.synchronizePreferredName(ptnam);
									}
								}
							}
							if (addProperty(prop_name, prop_value, null) != null) {
								edit_tree.repaint();
								fireUpdateEvent(UPDATED);
								ok = true;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a restriction in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class CreateARoleGroupAction extends AbstractAction {
		private static final long serialVersionUID = 1362163995326516988L;

		public CreateARoleGroupAction() {
			super("Create a restriction group ...", OWLIcons
					.getCreateIcon(OWLIcons.OWL_RESTRICTION));
		}

		public void actionPerformed(ActionEvent e) {
			RoleGroupRestrictionComp rrc = new RoleGroupRestrictionComp(tab,
					kb, wrapper, null, null, false);

			if (rrc.getRestriction() != null) {
				addRestriction(rrc.getRestriction(), rrc.getIsDefining());
				tmp_restrs.push(rrc.getRestriction());
				edit_tree.repaint();
				fireUpdateEvent(UPDATED);

			}

		}
	}

	private class CreateARoleRestrictionAction extends AbstractAction {
		private static final long serialVersionUID = 1362163995326516988L;

		public CreateARoleRestrictionAction() {
			super("Create a restriction ...", OWLIcons
					.getCreateIcon(OWLIcons.OWL_RESTRICTION));
		}

		public void actionPerformed(ActionEvent e) {

			OWLRestriction r = null;

			final String create_label = "Create a Restriction";

			NCICreateRolePanel dlg = new NCICreateRolePanel(
					tab,
					kb
							.getRDFSNamedClass(OWLNames.Cls.SOME_VALUES_FROM_RESTRICTION),
					null, null, (RDFSClass) tab.getSelectedCls(), null, true);

			int ans = ModalDialog.showDialog(tab, dlg, create_label,
					ModalDialog.MODE_OK_CANCEL);
			if (ans == ModalDialog.OPTION_OK) {
				r = dlg.getResult();
				if (r != null) {
					addRestriction(r, dlg.getIsDefining());
					tmp_restrs.push(r);
					edit_tree.repaint();
					fireUpdateEvent(UPDATED);

				}
			}
		}
	}

	/**
	 * Adds an association in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class AddAssociationAction extends AbstractAction {
		private static final long serialVersionUID = -463498328111664561L;

		public AddAssociationAction() {
			super("Add Association");
		}

		public void actionPerformed(ActionEvent e) {
			String title = "Add an Object-Valued Property";
			CreateObjPropertyDialog dlg = new CreateObjPropertyDialog(tab,
					title, null, null);
			if (!dlg.isCancelled()) {
				RDFProperty property = dlg.getSelectedProperty();
				Cls cls = dlg.getSelectedCls();

				// 010407
				boolean invaldSelection = wrapper
						.isPremerged((OWLNamedClass) cls);
				if (invaldSelection) {
					MsgDialog.error(tab, "Cannot select a pre-merged concept.");
					return;
				}
				invaldSelection = wrapper.isPreretired((OWLNamedClass) cls);
				if (invaldSelection) {
					MsgDialog
							.error(tab, "Cannot select a pre-retired concept.");
					return;
				}
				invaldSelection = wrapper.isRetired((OWLNamedClass) cls);
				if (invaldSelection) {
					MsgDialog.error(tab, "Cannot select a retired concept.");
					return;
				}

				if (property != null && cls != null) {
					String propName = property.getBrowserText();
					String clsName = wrapper.getInternalName(cls);

					addAssociation((Slot) property, cls, propName, clsName);
					edit_tree.repaint();
					fireUpdateEvent(UPDATED);
				}
			}
		}

	}

	/**
	 * Modifies an association in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class ModifyAssociationAction extends AbstractAction {
		private static final long serialVersionUID = 4267295746741448342L;

		public ModifyAssociationAction() {
			super("Modify Association");
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();
			TreeItem item = (TreeItem) selectedNode.getUserObject();

			if (item.getType() != TYPE_ASSOCIATION) {
				return;
			}

			RDFProperty property = item.getProperty();
			Cls cls = item.getCls();

			CreateObjPropertyDialog dlg = new CreateObjPropertyDialog(tab,
					"Modify Association", property, cls);
			if (!dlg.isCancelled()) {
				property = dlg.getSelectedProperty();
				cls = dlg.getSelectedCls();

				// 010407
				boolean invaldSelection = wrapper
						.isPremerged((OWLNamedClass) cls);
				if (invaldSelection) {
					MsgDialog.error(tab, "Cannot select a pre-merged concept.");
					return;
				}
				invaldSelection = wrapper.isPreretired((OWLNamedClass) cls);
				if (invaldSelection) {
					MsgDialog
							.error(tab, "Cannot select a pre-retired concept.");
					return;
				}
				invaldSelection = wrapper.isRetired((OWLNamedClass) cls);
				if (invaldSelection) {
					MsgDialog.error(tab, "Cannot select a retired concept.");
					return;
				}

				if (property != null && cls != null) {
					String internalName = wrapper.getInternalName(cls);
					item.setValue(internalName);
					String propName = property.getBrowserText();
					String clsName = cls.getBrowserText();
					item.setNameValue(propName, clsName);
					item.setProperty(property);
					item.setCls((RDFSClass) cls);

					edit_tree.repaint();
					fireUpdateEvent(UPDATED);
				}
			}
		}

	}

	private boolean isValidSelection(OWLNamedClass ocl) {
		if (wrapper.isPremerged(ocl) || wrapper.isPreretired(ocl)
				|| wrapper.isRetired(ocl)) {
			try {
				MsgDialog
						.ok(tab,
								"Invalid entry, class cannot be premerged, preretired, or retired.");
				return false;
			} catch (Exception ex) {
				Log.getLogger().log(Level.WARNING, "Exception caught", ex);
				return false;
			}
		}
		return true;

	}

	private class ModifyParentAction extends AbstractAction {
		private static final long serialVersionUID = -43839162989022290L;

		public ModifyParentAction() {
			super("Modify Parent");
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();
			TreeItem item = (TreeItem) selectedNode.getUserObject();

			boolean defining = item.getIsDefining();

			boolean definingEditable = true;

			ModifySuperclassDialog dlg = new ModifySuperclassDialog(tab, item
					.getCls().getBrowserText(), item.getCls(), defining,
					definingEditable);
			if (!dlg.cancelBtnPressed) {
				defining = false;
				if (dlg.definingCheckBox.isSelected()) {
					defining = true;
				}

				if ((dlg.getCls() != null)
						&& isValidSelection((OWLNamedClass) dlg.getCls())) {

					RDFSClass aClass = (RDFSClass) dlg.getCls();

					item.setCls(aClass);
					item.setIsDefining(dlg.getIsDefining());
					item.setNameValue(item.getName(), aClass.getPrefixedName());

					selectedNode.setUserObject((Object) item);

					model.reload();
					edit_tree.repaint();
					fireUpdateEvent(UPDATED);

				} else {

				}
			}

		}

	}

	private class AddParentAction extends AbstractAction {
		private static final long serialVersionUID = -4383916332989022290L;

		public AddParentAction() {
			super("Add Parent");
		}

		public void actionPerformed(ActionEvent e) {
			// java.awt.TextField textfield = new TextField();
			String label = "Select a class.";

			Cls cls = null;

			final NCISelectSuperClsPanel p = new NCISelectSuperClsPanel(kb);
			int result = ModalDialog.showDialog(tab, p, label,
					ModalDialog.MODE_OK_CANCEL);
			if (result == ModalDialogFactory.OPTION_OK) {
				Collection c = p.getSelection();
				if (c != null && c.size() > 0) {
					Iterator it = c.iterator();
					Object obj = it.next();
					cls = (Cls) obj;
				}
			}

			// TODO: Tim/Tania, when we use
			// DisplayUtilities.pickConcreteCls.. the search icon doesn't work
			// cls = DisplayUtilities.pickConcreteCls(textfield, kb, clses);
			if (cls == null) {
				System.out.println("cls is null");
			} else {

				if (isValidSelection((OWLNamedClass) cls)) {
					addParent(cls, p.isDefining());
					edit_tree.repaint();
					fireUpdateEvent(UPDATED);
				}
			}
		}
	}

	private TreeItems getDefinitionItems(Cls hostClass, int set_type) {
		TreeItems v = new TreeItems();
		HashSet<String> hset = new HashSet<String>();

		for (Iterator it = ((RDFSClass) hostClass).getSuperclasses(false)
				.iterator(); it.hasNext();) {
			Cls superCls = (Cls) it.next();

			if (superCls instanceof RDFSClass
					&& ((RDFSClass) superCls).getSuperclasses(false).contains(
							hostClass)) {
				RDFSClass equivalentClass = (RDFSClass) superCls;
				if (equivalentClass instanceof OWLIntersectionClass) {
					OWLIntersectionClass intersectionCls = (OWLIntersectionClass) equivalentClass;
					// 121406 KLO
					if (set_type == NCIConditionsTableModel.SET_RESTRICTION
							&& OWLWrapper
									.containsSimpleRestrictionsOnly(intersectionCls)) {
						Collection operands = intersectionCls.getOperands();
						for (Iterator oit = operands.iterator(); oit.hasNext();) {
							RDFSClass operand = (RDFSClass) oit.next();
							String value = operand.getBrowserText();
							if (!hset.contains(value)) {
								TreeItem item = new TreeItem();
								item.setType(TYPE_RESTRICTION);
								item.setNameValue(value);
								item.setCls((RDFSClass) operand);
								item.setIsDefining(true);
								v.add(item);
								hset.add(value);
							}
						}
					} else {
						Collection operands = ((OWLIntersectionClass) equivalentClass)
								.getOperands();
						for (Iterator oit = operands.iterator(); oit.hasNext();) {
							RDFSClass operand = (RDFSClass) oit.next();

							if (set_type == NCIConditionsTableModel.SET_RESTRICTION
									&& operand instanceof OWLAnonymousClass) {
								String value = operand.getBrowserText();
								if (!hset.contains(value)) {
									TreeItem item = new TreeItem();
									item.setType(TYPE_RESTRICTION);
									item.setNameValue(operand.getBrowserText());
									item.setCls((RDFSClass) operand);
									item.setIsDefining(true);
									v.add(item);
									hset.add(value);
								}

							} else if (set_type == NCIConditionsTableModel.SET_SUPERCLASS
									&& operand instanceof OWLNamedClass) {
								String value = SUBCLASSOF + ": "
										+ operand.getBrowserText();
								if (!hset.contains(value)) {
									TreeItem item = new TreeItem();
									item.setType(TYPE_PARENT);
									item.setName(SUBCLASSOF);

									item.setValue(wrapper
											.getInternalName(operand));
									item.setNameValue(value);
									item.setIsDefining(true);
									item.setCls((RDFSClass) operand);
									v.add(item);
									hset.add(value);
								}
							}
							// hset.add(edit_panel.getNCIEditTab().getBrowserText(operand));
						}
					}

				}

				else if (equivalentClass instanceof OWLUnionClass) {
					if (set_type == NCIConditionsTableModel.SET_RESTRICTION
							&& equivalentClass instanceof OWLAnonymousClass) {
						String value = equivalentClass.getBrowserText();
						if (!hset.contains(value)) {
							TreeItem item = new TreeItem();
							item.setType(TYPE_RESTRICTION);
							item.setNameValue(equivalentClass.getBrowserText());
							item.setCls((RDFSClass) equivalentClass);
							item.setIsDefining(true);
							v.add(item);
							hset.add(value);
						}

					} else if (set_type == NCIConditionsTableModel.SET_SUPERCLASS
							&& equivalentClass instanceof OWLNamedClass) {
						String value = SUBCLASSOF + ": "
								+ equivalentClass.getBrowserText();
						if (!hset.contains(value)) {
							TreeItem item = new TreeItem();
							item.setType(TYPE_PARENT);
							item.setName(SUBCLASSOF);

							item.setValue(wrapper
									.getInternalName(equivalentClass));
							item.setNameValue(value);
							item.setIsDefining(true);
							item.setCls((RDFSClass) equivalentClass);
							v.add(item);
							hset.add(value);
						}
					}
				} else // None of the above
				{
					if (set_type == NCIConditionsTableModel.SET_RESTRICTION
							&& equivalentClass instanceof OWLAnonymousClass) {
						String value = equivalentClass.getBrowserText();
						if (!hset.contains(value)) {
							TreeItem item = new TreeItem();
							item.setType(TYPE_RESTRICTION);
							item.setNameValue(equivalentClass.getBrowserText());
							item.setCls((RDFSClass) equivalentClass);
							item.setIsDefining(true);
							v.add(item);
							hset.add(value);
						}

					} else if (set_type == NCIConditionsTableModel.SET_SUPERCLASS
							&& equivalentClass instanceof OWLNamedClass) {
						String value = SUBCLASSOF + ": "
								+ equivalentClass.getBrowserText();
						if (!hset.contains(value)) {
							TreeItem item = new TreeItem();
							item.setType(TYPE_PARENT);
							item.setName(SUBCLASSOF);

							item.setValue(wrapper
									.getInternalName(equivalentClass));
							item.setNameValue(value);
							item.setIsDefining(true);
							item.setCls((RDFSClass) equivalentClass);
							v.add(item);
							hset.add(value);
						}
					}
				}
			}
		}
		return v;
	}

	private TreeItems getDirectSuperclassItems(Cls hostClass, int set_type) {
		TreeItems v = new TreeItems();
		HashSet<String> hset = new HashSet<String>();
		if (set_type == NCIConditionsTableModel.SET_SUPERCLASS) {

			for (Iterator it = ((OWLNamedClass) hostClass).getSuperclasses(
					false).iterator(); it.hasNext();) {
				Cls superCls = (Cls) it.next();
				if (superCls instanceof RDFSNamedClass) {
					RDFSClass aClass = (RDFSClass) superCls;
					if (aClass instanceof OWLNamedClass) {
						String value = SUBCLASSOF + ": "
								+ superCls.getBrowserText();
						if (!hset.contains(value)) {
							TreeItem item = new TreeItem();
							item.setType(TYPE_PARENT);
							item.setName(SUBCLASSOF);

							item.setValue(((OWLNamedClass) superCls)
									.getPrefixedName());
							item.setNameValue(value);
							item.setCls((RDFSClass) superCls);
							v.add(item);
							hset.add(value);
						}
					}

				}
			}
		}

		else if (set_type == NCIConditionsTableModel.SET_RESTRICTION) {
			// for (Iterator it = hostClass.getSuperclasses().iterator();
			// it.hasNext();)
			for (Iterator it = hostClass.getDirectSuperclasses().iterator(); it
					.hasNext();) {
				Cls superCls = (Cls) it.next();
				if (superCls instanceof OWLAnonymousClass) {
					RDFSClass r = (RDFSClass) superCls;

					// if (aClass instanceof OWLAnonymousClass && !(aClass
					// instanceof OWLIntersectionClass))
					if (r instanceof OWLAnonymousClass) {
						// need to exclude equivalent classes
						boolean isDefinition = wrapper.containsDefinition(
								(OWLNamedClass) hostClass, r);
						if (!isDefinition) {
							String value = r.getBrowserText();
							if (!hset.contains(value)) {
								TreeItem item = new TreeItem();
								item.setType(TYPE_RESTRICTION);
								item.setNameValue(r.getBrowserText());
								item.setCls((RDFSClass) r);
								v.add(item);
								hset.add(value);
							}
						}
					}
				}
			}
		}
		return v;
	}

	/**
	 * Copies a property in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class CopyPropertyAction extends AbstractAction {
		private static final long serialVersionUID = 7663041115225134711L;

		public CopyPropertyAction() {
			super("Copy");
		}

		public void actionPerformed(ActionEvent e) {
			TreeNode selectedNode = (TreeNode) edit_tree
					.getLastSelectedPathComponent();

			TreeItem item = (TreeItem) selectedNode.getUserObject();
			// [#6179] User is not prevented from duplicating rdfs:label
			// in the Copy Tab
			if (item.getType() == TYPE_PROPERTY) {
				String prop_name = item.getName();
				if (prop_name.compareTo("rdfs:label") == 0) {
					MsgDialog.error(tab, "Cannot copy " + prop_name + ".");
					return;
				}

				Boolean value = wrapper.isReadOnlyProperty(prop_name);
				if (Boolean.TRUE.equals(value)) {
					MsgDialog.error(tab, "Cannot copy " + prop_name + ".");
					return;
				}
			}
			// TreeItem ni = item.cloneTreeItem(false);
			// ni.setCls(item.getCls().createClone());
			tab.setCopyItem(item);
		}
	}

	/**
	 * Pastes a property in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class PastePropertyAction extends AbstractAction {
		private static final long serialVersionUID = -1950010156858621717L;

		public PastePropertyAction() {
			super("Paste");
		}

		public void actionPerformed(ActionEvent e) {
			TreeItem copied_item = tab.getCopyItem();
			addTreeItem(copied_item);
			fireUpdateEvent(UPDATED);
		}
	}

	/**
	 * Accepts a concept in the TreePanel.
	 * 
	 * @author David Yee
	 */
	private class AcceptAction extends AbstractAction {
		private static final long serialVersionUID = -5236277809547212932L;

		public AcceptAction(boolean enabled) {
			super("Accept");
			setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			fireUpdateEvent(ACCEPTED);
		}
	}

	public void enableAcceptAction(boolean enable) {
		enableAcceptAction = enable;
	}

	private void addTreeItem(TreeItem item) {
		if (item == null)
			return;

		if (item.getType() == TYPE_PROPERTY)
			addProperty(item.getName(), item.getValue(), null);
		else if (item.getType() == TYPE_RESTRICTION) {

			addRestriction(item.getCls(), item.getIsDefining());
		}
		if (item.getType() == TYPE_PARENT)
			addParent((Cls) item.getCls());

		edit_tree.repaint();
	}

	public void refresh() {
		edit_tree.repaint();
		fireUpdateEvent(UPDATED);
	}

}
