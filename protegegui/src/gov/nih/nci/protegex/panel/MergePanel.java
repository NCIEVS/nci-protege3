package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.Deprecatable;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.edit.DataHandler;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.tree.TreeItem;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreeNode;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.tree.TreePanel.PanelType;
import static gov.nih.nci.protegex.tree.TreePanel.PanelType.*;
import gov.nih.nci.protegex.util.MsgDialog;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;

public class MergePanel extends NCIDoublePanel {

	private Logger logger = Log.getLogger(getClass());

	private static final long serialVersionUID = -2718800361139903876L;

	private final String MERGE_SOURCE = NCIEditTab.MERGERETIRE;

	private final String MERGE_TARGET = NCIEditTab.MERGESURVIVING;

	private final String MERGE_INTO = NCIEditTab.MERGETO;

	private final String EDITOR_NOTE = NCIEditTab.EDITORIALNOTE;

	private final String DESIGNNOTE = NCIEditTab.SCOPENOTE;

	private final String PREMERGED_CONCEPTS = NCIEditTab.PREMERGED_CONCEPTS;

	private final String RETIRED_CONCEPTS = NCIEditTab.RETIRED_CONCEPTS;

	private final String OLD_PARENT = NCIEditTab.PREDEPRECATIONPARENTCONCEPT;

	private final String OLD_CHILD = NCIEditTab.PREDEPRECATIONCHILDCONCEPT;

	private final String SPLIT_FROM = NCIEditTab.SPLITFROM;

	private JButton _mergeButton;

	private JButton _unmergeButton;

	private boolean _unmergeButtonPressed;

	public MergePanel(NCIEditTab tab) {
		super(tab, TreePanel.PanelType.TYPE_MERGE);
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		initObjects();

		LabeledComponent lc_upper = new LabeledComponent("Surviving Concept",
				_upperScrollPane);
		LabeledComponent lc_lower = new LabeledComponent("Retiring Concept",
				_lowerScrollPane);

		JSplitPane mergePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				lc_upper, lc_lower);
		mergePane.setDividerLocation(280);
		mergePane.setOneTouchExpandable(true);

		add(mergePane, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(_unmergeButton = createButton("UnMerge", false));
		panel.add(_mergeButton = createButton("Merge", false));
		panel.add(_saveButton = createButton("Save", false));
		panel.add(_clearButton = createButton("Clear", false));
		return panel;
	}

	private void initObjects() {
		_lowerTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_lowerTreePanel, false, true, true);
		_lowerScrollPane.setViewportView(_lowerTreePanel.getTree());

		_upperTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_upperTreePanel, true, true, true);
		_upperScrollPane.setViewportView(_upperTreePanel.getTree());

		_unmergeButtonPressed = false;
	}

	public void reset() {
		super.reset();
		initObjects();
		_tab.clearListenedToClses();
	}

	private void merge() {
		try {
			// _kb.beginTransaction("test if this fixes refresh issues");
			String upperClsName = _upperTreePanel.getLocalName();
			String lowerClsName = _lowerTreePanel.getLocalName();
			setCursor(WAIT_CURSOR);

			Cls retiringCls = _wrapper.getCls(lowerClsName);
			Cls survivalCls = _wrapper.getCls(upperClsName);

			JTree edit_tree_1 = _lowerTreePanel.getTree();
			JTree edit_tree_2 = _upperTreePanel.getTree();

			String prop_name = MERGE_SOURCE;
			_upperTreePanel.deleteProperty(prop_name);
			edit_tree_2.repaint();

			prop_name = NCIEditTab.MERGESURVIVING;
			_lowerTreePanel.deleteProperty(prop_name);
			edit_tree_1.repaint();

			String prop_value = NCIEditTab.PREMERGED_CONCEPTS;
			_lowerTreePanel.deleteParent(prop_value);
			edit_tree_1.repaint();

			Vector<String> exclusion = new Vector<String>();
			exclusion.add(NCIEditTab.CODE);
			for (String prop : _tab.getNonTransferProps()) {
				exclusion.add(prop);
			}

			exclusion.add("ID");
			exclusion.add(NCIEditTab.PREFLABEL);
			exclusion.add(NCIEditTab.EDITORIALNOTE);
			exclusion.add(NCIEditTab.SCOPENOTE);
			exclusion.add(NCIEditTab.PREDEPRECATIONSOURCEROLE);
			exclusion.add(NCIEditTab.PREDEPRECATIONROLE);
			exclusion.add(NCIEditTab.PREDEPRECATIONSOURCEASSOC);
			exclusion.add(NCIEditTab.PREDEPRECATIONASSOC);

			exclusion.add(NCIEditTab.PREDEPRECATIONPARENTCONCEPT);
			exclusion.add("rdfs:label");
			exclusion.add("hasType");

			// 082706
			String split_from_id = _lowerTreePanel.getPropertyValue(SPLIT_FROM);
			if (split_from_id != null) {
				if (split_from_id.compareTo(upperClsName) == 0) {
					exclusion.add(SPLIT_FROM);
				} else if (split_from_id.compareTo(_upperTreePanel.getCode()) == 0) {
					exclusion.add(SPLIT_FROM);
				}
			}

			String merge_into_id = _lowerTreePanel.getPropertyValue(MERGE_INTO);
			if (merge_into_id != null) {
				if (merge_into_id.compareTo(upperClsName) == 0) {
					exclusion.add(MERGE_INTO);
				} else if (merge_into_id.compareTo(_upperTreePanel.getCode()) == 0) {
					exclusion.add(MERGE_INTO);
				}
			}

			TreeItems survival_state = _upperTreePanel.getCurrentState();
			TreeItems retiring_state = _lowerTreePanel.getCurrentState();

			// 090806
			// change NCI/PT to NCI/SY in retiring concept
			retiring_state = retiring_state.convertNCIPT2NCISY();

			TreeItems diffs = survival_state.getDiffsByType(retiring_state,
					TYPE_PROPERTY, exclusion);

			for (int i = 0; i < diffs.size(); i++) {
				TreeItem item = (TreeItem) diffs.elementAt(i);
				_upperTreePanel.addProperty(item.getName(), item.getValue(),
						null);
			}

			// modify editor_note and designnote:
			String old_editor_note = _lowerTreePanel
					.getPropertyValue(EDITOR_NOTE);
			if (old_editor_note != null && old_editor_note.indexOf("|") != -1) {
				String editor_note = old_editor_note.substring(old_editor_note
						.indexOf("|") + 1);
				_lowerTreePanel.deleteProperty(EDITOR_NOTE, old_editor_note,
						null);
				_lowerTreePanel.addProperty(EDITOR_NOTE, editor_note, null);
			}

			String old_designnote = _lowerTreePanel
					.getPropertyValue(DESIGNNOTE);
			if (old_designnote != null && old_designnote.indexOf("|") != -1) {
				String designnote = old_designnote.substring(old_designnote
						.indexOf("|") + 1);
				_lowerTreePanel
						.deleteProperty(DESIGNNOTE, old_designnote, null);
				_lowerTreePanel.addProperty(DESIGNNOTE, designnote, null);
				edit_tree_1.repaint();
			}

			// 082706
			Collection subs = retiringCls.getDirectSubclasses();
			Object[] obs_2 = subs.toArray();
			for (int j = 0; j < obs_2.length; j++) {
				if (obs_2[j] instanceof OWLNamedClass) {
					OWLNamedClass owlcls_2 = (OWLNamedClass) obs_2[j];
					_lowerTreePanel.addProperty(OLD_CHILD, _wrapper
							.getInternalName(owlcls_2), owlcls_2);
					edit_tree_1.repaint();
				}
			}

			/*
			 * (2) all nonredundant roles from concept two
			 */
			survival_state = _upperTreePanel.getCurrentState();
			retiring_state = _lowerTreePanel.getCurrentState();
			diffs = survival_state.getDiffsByType(retiring_state,
					TYPE_RESTRICTION, new Vector<String>());
			for (int i = 0; i < diffs.size(); i++) {
				TreeItem item = (TreeItem) diffs.elementAt(i);
				// need to be cloned??? 040707

				_upperTreePanel.addRestriction(item.getCls().createClone(),
						item.getIsDefining());
			}

			/*
			 * (2) all nonredundant associations from concept two
			 */
			survival_state = _upperTreePanel.getCurrentState();
			retiring_state = _lowerTreePanel.getCurrentState();
			diffs = survival_state.getDiffsByType(retiring_state,
					TYPE_ASSOCIATION, new Vector<String>());
			for (int i = 0; i < diffs.size(); i++) {
				TreeItem item = (TreeItem) diffs.elementAt(i);
				// need to be cloned??? 040707

				_upperTreePanel.addAssociation(item.getProperty(), item
						.getCls(), item.getName(), item.getValue());
			}

			/*
			 * (3) all nonredundant parents from concept two
			 */
			survival_state = _upperTreePanel.getCurrentState();
			retiring_state = _lowerTreePanel.getCurrentState();
			diffs = survival_state.getDiffsByType(retiring_state, TYPE_PARENT,
					new Vector<String>());
			for (int i = 0; i < diffs.size(); i++) {
				TreeItem item = (TreeItem) diffs.elementAt(i);

				// TreeNode dmtn =
				// upperTreePanel_Merge.addParent(item.getValue());

				String sup_name = _wrapper.getInternalName((Cls) item.getCls());
				String survivalCls_name = _wrapper.getInternalName(survivalCls);
				if (sup_name.compareTo(survivalCls_name) != 0) {
					// KLO, 040907
					// TreeNode dmtn = upperTreePanel_Merge.addParent((Cls)
					// item.getCls());
					_upperTreePanel.addParent((Cls) item.getCls(), item
							.getIsDefining());
				}
			}

			/*
			 * (4) all nonredundant children from concept two // Note: can store
			 * children and inbound roles of the retiring concept // in local
			 * variables of the corresponding TreePanel (i.e.,
			 * lowerTreePanel_Merge)
			 * 
			 * 
			 * ii) On nonredundant entities: It is expected that a number of
			 * entities (parents, roles, properties) will be present in both
			 * concepts. Do not add a specific entity if concept one already has
			 * it.
			 * 
			 * (1) When assessing whether a property, role value, so on, is the
			 * same in both concepts, conduct the string comparison in a
			 * case?sensitive manner. iii) Concept one also becomes the target
			 * of any referencing concepts targeting concept two (see below) (1)
			 * Concepts that refer to concept two include children in addition
			 * to role sources. iv) The concept name, code, and ID of concept
			 * two are ignored. v) In the history table, there will be a "merge"
			 * entry for concept one, referencing itself as a descendant in the
			 * merge action.
			 * 
			 * 
			 * d) Merge actions on concept two (retirement actions). i) All the
			 * properties are retained ii) Roles are stored in OLD_ROLE property
			 * and deleted (1) OLD_ROLE value contains the role name and the
			 * role value, delimited by pipes (a) The role value within OLD_ROLE
			 * is the concept name of the target concept (2) The roles are
			 * deleted after conversion to the OLD_ROLE property iii) Parents
			 * are stored in OLD_PARENT property and deleted (1) The concept
			 * name of the parent concept is stored as the value of OLD_PARENT
			 * (2) The reference to the parent concept(s) is(are) deleted (3) A
			 * new parent concept, "Retired Concept" from the Retired_Kind
			 * hierarchy is added. iv) Store the kind name in OLD_KIND property
			 * (1) The kind name string is stored as a value of OLD_KIND (2) Set
			 * the kind to Retired_Kind v) Children concepts are stored in
			 * OLD_CHILD property; children are remapped to concept one. (1)
			 * Children concept names are stored as values of OLD_CHILD, one
			 * property per child (2) The childrent concepts are retrieved and
			 * parent references to concept two are deleted (3) Concept one is
			 * added as a parent of each child vi) Referring role sources
			 * targeting concept two are remapped to concept one. (1) Concepts
			 * that refer to concept two via roles are retrieved (2) The roles
			 * referring to concept two are identified (3) New roles are created
			 * that refer to concept one instead (4) The roles referring to
			 * concept two are finally deleted vii) In the history table, the
			 * entry for concept two will show a "merge" action with a reference
			 * to concept one, and a "retire" action with no reference code. (1)
			 * i.e. conceptcode action editdate referencecode other columns
			 * concept two merge timestamp concept one concept two retire
			 * timestamp null
			 */

			// remove Merge_Target property; remove Merged_<kind>_Concepts
			// superconcept
			// remove Editor_Note, DesignNote
			_tab.updateNoteProperties(_lowerTreePanel, (RDFSClass) retiringCls);
			_tab
					.attributes2Properties(_lowerTreePanel,
							(RDFSClass) retiringCls);

			prop_value = RETIRED_CONCEPTS;

			Cls retiredCls = _wrapper.getCls(prop_value);

			_lowerTreePanel.addParent(retiredCls);
			edit_tree_1.repaint();

			String conceptStatus = _lowerTreePanel
					.getPropertyValue(NCIEditTab.CONCEPTSTATUS);
			if (conceptStatus != null) {
				_lowerTreePanel.deleteProperty(NCIEditTab.CONCEPTSTATUS,
						conceptStatus, null);
			}
			_lowerTreePanel.addProperty(NCIEditTab.CONCEPTSTATUS,
					"Retired_Concept", null);
			_lowerTreePanel.refresh();
			// edit_tree_1.repaint();

			_mergeButton.setEnabled(false);
			_unmergeButton.setEnabled(false);
			_saveButton.setEnabled(true);
			// _lowerTreePanel.enablePopUpWindow();
			_upperTreePanel.enablePopUpWindow();
			_isDataModified = true;
			_clearButton.setEnabled(true);

		} catch (Exception ex) {
			// _kb.rollbackTransaction();
			_logger.warning("Exception caught" + ex.toString());
			OWLUI.handleError(_kb, ex);
		}
		setCursor(DEFAULT_CURSOR);
	}

	private void unmerge() {
		setCursor(WAIT_CURSOR);
		JTree edit_tree = _lowerTreePanel.getTree();
		JTree edit_tree2 = _upperTreePanel.getTree();
		// NOT_NEEDED: _lowerTreePanel.getName();
		_tab.deleteNoteProperties(_lowerTreePanel);

		// NOT_NEEDED: _upperTreePanel.getTree();
		// NOT_NEEDED: _upperTreePanel.getName();

		String prop_name = MERGE_TARGET;
		_lowerTreePanel.deleteProperty(prop_name);

		String prop_value = PREMERGED_CONCEPTS;
		_lowerTreePanel.deleteParent(prop_value);

		prop_name = MERGE_SOURCE;
		prop_value = _upperTreePanel.getPropertyValue(prop_name);
		_upperTreePanel.deleteProperty(prop_name, prop_value, null);

		// NOT_NEEDED: prop_value = _tab.getClsName(PREMERGED_CONCEPTS);
		// NOT_NEEDED: _lowerTreePanel.deleteParent(prop_value);

		Vector<RDFSClass> sups = _lowerTreePanel
				.getPropertyObjectValues(OLD_PARENT);
		for (int i = 0; i < sups.size(); i++) {
			Cls superCls = (Cls) sups.elementAt(i);
			// TreeNode child_node4 = lowerTreePanel_Merge.addParent(sup);
			_lowerTreePanel.addParent(superCls);

			_lowerTreePanel.deleteProperty(OLD_PARENT, _wrapper
					.getInternalName(superCls), superCls);
		}

		edit_tree2.repaint();

		edit_tree.repaint();

		_unmergeButton.setEnabled(false);
		_mergeButton.setEnabled(false);

		_lowerTreePanel.enablePopUpWindow();
		_upperTreePanel.enablePopUpWindow();

		_saveButton.setEnabled(true);
		_isDataModified = true;
		_clearButton.setEnabled(true);

		_unmergeButtonPressed = true;
		setCursor(DEFAULT_CURSOR);
	}

	protected void save() {
		long beg = System.currentTimeMillis();
		String upperClsName = _upperTreePanel.getLocalName();
		String lowerClsName = _lowerTreePanel.getLocalName();
		Cls toCls = _wrapper.getCls(upperClsName);
		Cls fromCls = _wrapper.getCls(lowerClsName);

		_tab.removeFromListenedToClses((OWLNamedClass) fromCls, this);
		_tab.removeFromListenedToClses((OWLNamedClass) toCls, this);

		DataHandler.Status status = _tab.getDataHandler().canSaveData(
				_lowerTreePanel, toCls);
		if (status != DataHandler.Status.SUCCESSFUL) {
			setCursor(DEFAULT_CURSOR);
			if (status == DataHandler.Status.FAILURE) {
				String action = _unmergeButtonPressed ? "unmerge" : "merge";
				MsgDialog.ok(this, "Unable to " + action + " "
						+ _lowerTreePanel.getDisplayName() + " and "
						+ _upperTreePanel.getDisplayName() + ".");
			}
			_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
			_tab.addToListenedToClses((OWLNamedClass) toCls, this);
			return;
		}
		status = _tab.getDataHandler().canSaveData(_upperTreePanel, fromCls);
		if (status != DataHandler.Status.SUCCESSFUL) {
			setCursor(DEFAULT_CURSOR);
			if (status == DataHandler.Status.FAILURE) {
				String action = _unmergeButtonPressed ? "unmerge" : "merge";
				MsgDialog.ok(this, "Unable to " + action + " "
						+ _lowerTreePanel.getDisplayName() + " and "
						+ _upperTreePanel.getDisplayName() + ".");
			}
			_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
			_tab.addToListenedToClses((OWLNamedClass) toCls, this);
			return;
		} else {
			try {
				logger.fine("Ok checking two concepts can be saved took "
						+ (System.currentTimeMillis() - beg));
				_kb.beginTransaction("Merge " + lowerClsName + " into "
						+ upperClsName, fromCls.getName());
				setCursor(WAIT_CURSOR);

				if ((_tab.saveConcept(_lowerTreePanel) != DataHandler.Status.SUCCESSFUL)
						|| (_tab.saveConcept(_upperTreePanel) != DataHandler.Status.SUCCESSFUL)) {
					_kb.rollbackTransaction();
					_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
					_tab.addToListenedToClses((OWLNamedClass) toCls, this);
					return;

				}

				logger.fine("Ok saving both concepts took "
						+ (System.currentTimeMillis() - beg));

				if (!_unmergeButtonPressed) {
					String id_toCls = _wrapper.getCode(toCls);
					if (id_toCls == null || id_toCls.compareTo("0") == 0) {
						id_toCls = _wrapper.getInternalName(toCls);
					}

					boolean retval = _wrapper.addAnnotationProperty(fromCls
							.getName(), MERGE_INTO, id_toCls);
					if (!retval) {
						_tab
								.addToListenedToClses((OWLNamedClass) fromCls,
										this);
						_tab.addToListenedToClses((OWLNamedClass) toCls, this);
						_kb.rollbackTransaction();
						return;
					}

					// transfer subconcepts of the retiring concept to
					// suviving concept
					// fromCls: survival
					// toCls: retiring

					_wrapper.removeDirectSuperclasses((OWLNamedClass) fromCls);
					_wrapper.addDirectSubclasses((OWLNamedClass) fromCls,
							(OWLNamedClass) toCls);
					_wrapper.removeDirectSubclasses((OWLNamedClass) fromCls);

					// redirect inbound roles from the retiring concept to
					// the surviving concept
					_wrapper.redirectReferringClasses(fromCls, toCls);

					RDFSNamedClass cls = _kb.getRDFSNamedClass(lowerClsName);
					Deprecatable deprecatedCls = (Deprecatable) cls;
					deprecatedCls.setDeprecated(true);

					_wrapper.removeDirectSuperclasses((OWLNamedClass) fromCls);

				}

				_kb.commitTransaction();
				logger
						.fine("Ok adding annotation bits and changing parentage took "
								+ (System.currentTimeMillis() - beg));

				String id_toCls = _wrapper.getCode(toCls);
				_tab.recordHistory(NCIEditTab.EVSHistoryAction.MERGE,
						(OWLNamedClass) fromCls, id_toCls);
				_tab.recordHistory(NCIEditTab.EVSHistoryAction.MERGE,
						(OWLNamedClass) toCls, id_toCls);
				_tab.recordHistory(NCIEditTab.EVSHistoryAction.RETIRE,
						(OWLNamedClass) fromCls, "");
				logger.fine("Ok recording history took "
						+ (System.currentTimeMillis() - beg));

			} catch (Exception ex) {
				_kb.rollbackTransaction();
				OWLUI.handleError(_kb, ex);
				_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
				_tab.addToListenedToClses((OWLNamedClass) toCls, this);
				return;
			}

			setCursor(DEFAULT_CURSOR);

			_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
			_tab.addToListenedToClses((OWLNamedClass) toCls, this);

			if (_unmergeButtonPressed) {
				MsgDialog.ok(this, "Concepts "
						+ _lowerTreePanel.getDisplayName() + " and "
						+ _upperTreePanel.getDisplayName()
						+ " are unmerged successfully.");
				_unmergeButtonPressed = false;

			} else {
				MsgDialog.ok(this, "Concepts "
						+ _lowerTreePanel.getDisplayName() + " and "
						+ _upperTreePanel.getDisplayName()
						+ " are merged successfully.");

			}
			reset();
			logger.fine("Ok final reset took "
					+ (System.currentTimeMillis() - beg));
		}
	}

	protected void performAction(JButton button) {
		super.performAction(button);
		if (button == _mergeButton)
			merge();
		else if (button == _unmergeButton)
			unmerge();
	}

	private boolean populate(String name) {
		if (name == null || name.length() <= 0)
			return false;

		// TreeItem item = new TreeItem();
		// item.setType(TYPE_CONCEPT);
		// item.setName(name);

		// TreeNode dmtn = new TreeNode(item, TYPE_NOT_SET);
		Instance selectedInstance = (Instance) _kb.getRDFSNamedClass(name);
		OWLNamedClass cls = (OWLNamedClass) selectedInstance;
		// item.setCls(cls);
		// item.setNameValue(cls.getBrowserText());

		if (_wrapper.hasAnnotationProperty(cls, MERGE_TARGET)) {
			_lowerTreePanel.createTree(cls);
			// new TreePanel(_tab, selectedInstance, dmtn, null);
			// setupTreePanel(_lowerTreePanel, false, true, true);
			// _lowerScrollPane = new JScrollPane();
			// _lowerScrollPane.setViewportView(_lowerTreePanel.getTree());
		} else if (_wrapper.hasAnnotationProperty(
				(OWLNamedClass) selectedInstance, MERGE_SOURCE)) {
			// _upperScrollPane.remove(_upperTreePanel.getTree());
			_upperTreePanel.createTree(cls);
			// setupTreePanel(_upperTreePanel, true, true, true);
			// _upperScrollPane = new JScrollPane();

			// _upperScrollPane.setViewportView(_upperTreePanel.getTree());

		} else {
			return false;
		}

		_saveButton.setEnabled(false);
		_isDataModified = false;
		_unmergeButton.setEnabled(true);
		_mergeButton.setEnabled(true);
		_clearButton.setEnabled(true);

		_unmergeButtonPressed = false;

		_lowerTreePanel.setCanDrop(false);
		_upperTreePanel.setCanDrop(false);

		_tab.addToListenedToClses((OWLNamedClass) selectedInstance, this);
		return true;
	}

	/**
	 * Populates the panel with concept class.
	 * 
	 * @param cls
	 *            Concept class.
	 * @param subtype
	 *            Panel subtype.
	 * @return true if populated.
	 */
	public DropType populate(OWLNamedClass cls, PanelType subtype) {
		String title = "Unable to load";
		if (!(_wrapper.hasAnnotationProperty(cls, MERGE_SOURCE) || _wrapper
				.hasAnnotationProperty(cls, MERGE_TARGET))) {
			MsgDialog.warning(_tab, title, " concept has not been premerged.");
			return DropType.BAD;

		} else {
			if (_wrapper.hasAnnotationProperty(cls, MERGE_SOURCE)) {
				if (subtype == _lowerSubtype) {
					// MsgDialog.warning(_tab, title, MERGE_TARGET
					// + " cannot be identified.");
					return DropType.TRYAGAIN;
				}
				if (!populate(_wrapper.getPropertyValue(cls, MERGE_SOURCE))) {
					MsgDialog.warning(_tab, title,
							"Unable to load pre-merged concepts.");
					return DropType.BAD;
				}
			} else if (_wrapper.hasAnnotationProperty(cls, MERGE_TARGET)) {
				if (subtype == _upperSubtype) {
					// MsgDialog.warning(_tab, title, MERGE_SOURCE
					// + " cannot be identified.");
					return DropType.TRYAGAIN;
				}
				if (!populate(_wrapper.getPropertyValue(cls, MERGE_TARGET))) {
					MsgDialog.warning(_tab, title,
							"Unable to load pre-merged concepts.");
					return DropType.BAD;
				}
			}
			return DropType.GOOD;
		}
	}

	public JScrollPane getScrollPane(PanelType type, PanelType subtype) {
		if (type == TYPE_MERGE) {
			if (subtype == TYPE_MERGE_LOWER)
				return _lowerScrollPane;
			else
				return _upperScrollPane;
		}
		return null;
	}
}
