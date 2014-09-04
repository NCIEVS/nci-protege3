package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.dialog.NewClsDialog;
import gov.nih.nci.protegex.edit.DataHandler;
import gov.nih.nci.protegex.edit.NCIEditFilter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.TreePanelReviewAction;
import gov.nih.nci.protegex.tree.TreeItem;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreeNode;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.MsgDialog;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class SplitPanel extends NCIDoublePanel {
	private static final long serialVersionUID = 4107742881535484142L;

	public static final String MERGE_SOURCE = NCIEditTab.MERGERETIRE;

	public static final String MERGE_TARGET = NCIEditTab.MERGESURVIVING;

	public static final String SPLIT_FROM = NCIEditTab.SPLITFROM;

	private JButton _splitButton;

	private TreePanelReviewAction _upperReviewAction;

	private TreePanelReviewAction _lowerReviewAction;

	private JSplitPane splitPane = null;

	public SplitPanel(NCIEditTab tab) {
		super(tab, TreePanel.PanelType.TYPE_SPLIT);

		init();
		initObjects();
	}

	private void init() {
		setLayout(new BorderLayout());

		initObjects();

		LabeledComponent upperLC = new LabeledComponent("Existing Concept",
				_upperScrollPane);
		LabeledComponent lowerLC = new LabeledComponent("New Concept",
				_lowerScrollPane);

		// JFrame frame = (JFrame) _tab.getTopLevelAncestor();
		_upperReviewAction = new TreePanelReviewAction(getUpperTreePanel());
		upperLC.addHeaderButton(_upperReviewAction);
		_lowerReviewAction = new TreePanelReviewAction(getLowerTreePanel());
		lowerLC.addHeaderButton(_lowerReviewAction);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperLC, lowerLC);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(280);

		add(splitPane, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(_splitButton = createButton("Split", false));
		panel.add(_saveButton = createButton("Save", false));
		panel.add(_clearButton = createButton("Clear", false));
		return panel;
	}

	private void initObjects() {
		_upperTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_upperTreePanel, true, true, true);
		_upperScrollPane.setViewportView(_upperTreePanel);

		_lowerTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_lowerTreePanel, false, true, false);
		_lowerScrollPane.setViewportView(_lowerTreePanel);
	}

	public void reset() {
		super.reset();
		initObjects();

		_tab.clearListenedToClses();
		_upperReviewAction.setTreePanel(null);
		_lowerReviewAction.setTreePanel(null);
	}

	private void split() {
		try {
			setCursor(WAIT_CURSOR);
			OWLNamedClass fromCls = (OWLNamedClass) _wrapper
					.getCls(_upperTreePanel.getLocalName());
			if (_wrapper.hasAnnotationProperty(fromCls, MERGE_SOURCE)
					|| _wrapper.hasAnnotationProperty(fromCls, MERGE_TARGET)) {
				setCursor(DEFAULT_CURSOR);
				MsgDialog.ok(this, "Cannot split " + fromCls.getName()
						+ " -- concept has been flagged for merge.");
				return;
			}

			String label = "";
			String pt = "";
			boolean cancelled = false;
			boolean hasError = true;
			while (!cancelled) {
				NewClsDialog dlg = new NewClsDialog(_tab, false);
				dlg.init(label, pt, "");
				cancelled = dlg.cancelButtonPressed;

				hasError = false;
				if (cancelled)
					break;
				label = dlg.getLabel();
				pt = dlg.getPT();

				if (label.compareTo("") == 0 || pt.compareTo("") == 0) {
					_tab.showDataError("Please complete data entry.");
					hasError = true;
					cancelled = false;
				} else if (_kb.getRDFSNamedClass(label) != null) {
					_tab.showConceptExistError(label);
					hasError = true;
					cancelled = false;
				} else {
					// check if the name is valid:
					boolean nameValid = NCIEditFilter
							.checkXMLNCNameCompliance(label);
					if (!nameValid) {
						String text = "Class name format is incorrect -- "
								+ label;
						ModalDialog.showMessageDialog(null, text);
						hasError = true;
						cancelled = false;
					} else {
						break;
					}
				}
			}
			if (!cancelled && !hasError) {
				setCursor(DEFAULT_CURSOR);
				boolean canSplit = populate(label, pt);
				if (!canSplit) {
					MsgDialog.ok(this, "Unable to create a new concept.");
					_splitButton.setEnabled(false);
				}
			}
		} catch (Exception ex) {
			_logger.log(Level.WARNING, "Exception caught", ex);
			OWLUI.handleError(_kb, ex);
		}
		setCursor(DEFAULT_CURSOR);
	}

	protected void save() {
		long beg = System.currentTimeMillis();
		String upperClsName = _upperTreePanel.getLocalName();
		String lowerClsName = _lowerTreePanel.getLocalName();
		if (lowerClsName == null) {
			MsgDialog.warning(this, "You must split the existing concept\n"
					+ "before you can save.");
			return;
		}
		Cls fromCls = _wrapper.getCls(upperClsName);
		RDFSNamedClass toCls = _kb.getRDFSNamedClass(lowerClsName);

		// Cls toCls = wrapper.getCls(lowerTreePanel_Split.getName());

		setCursor(WAIT_CURSOR);
		DataHandler.Status status1 = _tab.getDataHandler().canSaveData(
				_upperTreePanel, fromCls);
		if (status1 != DataHandler.Status.SUCCESSFUL) {
			setCursor(DEFAULT_CURSOR);
			if (status1 == DataHandler.Status.FAILURE)
				_tab.showError(_upperTreePanel);

			return;
		}

		DataHandler.Status status2 = _tab.getDataHandler().canCreateData(
				lowerClsName, _lowerTreePanel.getCurrentState());

		if (status2 != DataHandler.Status.SUCCESSFUL) {
			setCursor(DEFAULT_CURSOR);
			if (status2 == DataHandler.Status.FAILURE)
				_tab.showError(_lowerTreePanel.getDisplayName());

			return;
		}
		System.out.println("time spent checking data -- "
				+ (System.currentTimeMillis() - beg));
		try {

			String id_fromCls = _wrapper.getCode(fromCls);

			if (id_fromCls == null || id_fromCls.compareTo("0") == 0) {
				id_fromCls = _wrapper.getInternalName(fromCls);
			}

			_tab.removeFromListenedToClses((OWLNamedClass) fromCls, this);
			_tab.removeFromListenedToClses((OWLNamedClass) toCls, this);

			_kb.beginTransaction("Save in split panel. Split class "
					+ upperClsName, fromCls.getName());

			if ((_tab.saveConcept(_upperTreePanel) != DataHandler.Status.SUCCESSFUL)
					|| (_tab.saveConcept(_lowerTreePanel, lowerClsName) != DataHandler.Status.SUCCESSFUL)) {
				_kb.rollbackTransaction();

				return;
			}

			System.out.println("time spent saving upper and lower panels -- "
					+ (System.currentTimeMillis() - beg));

			toCls = _kb.getRDFSNamedClass(lowerClsName);
			String id_toCls = _wrapper.getCode(toCls);

			boolean retval = _wrapper.addAnnotationProperty(lowerClsName,
					SPLIT_FROM, id_fromCls);
			if (!retval) {
				setCursor(DEFAULT_CURSOR);

				_kb.rollbackTransaction();
				return;
			}

			Collection subs = fromCls.getDirectSubclasses();
			for (Iterator iterator = subs.iterator(); iterator.hasNext();) {
				RDFSClass sub = (RDFSClass) iterator.next();
				if (sub instanceof OWLNamedClass) {
					sub.addSuperclass(toCls);
				}
			}
			System.out
					.println("time spent adding annotations and fixing children -- "
							+ (System.currentTimeMillis() - beg));

			_kb.commitTransaction();
			_tab.recordHistory(NCIEditTab.EVSHistoryAction.SPLIT,
					(OWLNamedClass) fromCls, id_fromCls);
			_tab.recordHistory(NCIEditTab.EVSHistoryAction.SPLIT,
					(OWLNamedClass) fromCls, id_toCls);
		} catch (Exception ex) {
			_kb.rollbackTransaction();
			OWLUI.handleError(_kb, ex);

			return;
		} finally {
			_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
			_tab.addToListenedToClses((OWLNamedClass) toCls, this);
		}

		setCursor(DEFAULT_CURSOR);
		MsgDialog.ok(this, "Concept split successful.");

		// 051007
		this._lowerTreePanel.initTmpRestrs();
		this._upperTreePanel.initTmpRestrs();
		reset();

		setCursor(DEFAULT_CURSOR);

		_tab.ensureClassSelected(fromCls);
	}

	/**
	 * Sets up this panel while a concept is loading.
	 * 
	 * @param treePanel
	 *            The corresponding TreePanel.
	 */
	public void setupWhileLoadingConcept(TreePanel treePanel) {
		_splitButton.setEnabled(true);
		_clearButton.setEnabled(true);
	}

	protected void performAction(JButton button) {
		super.performAction(button);
		if (button == _splitButton)
			split();
	}

	private boolean populate(String name, String pt) {
		String code = _tab.getWrapper().getCode();
		if (_tab.byCode()) {
			if (code == null || code.compareTo("0") == 0)
				return false;
		}

		// By name: use name as identifier
		// By code: use code as identifier
		TreeItem item = new TreeItem();
		item.setType(TYPE_CONCEPT);
		if (!_wrapper.byCode()) {
            item.setName(name);
            item.setNameValue(name);
		} else {
	        item.setName(code);
	        item.setNameValue(pt);
		}

		TreeItems currState = _upperTreePanel.getCurrentState();

		currState = currState.convertNCIPT2NCISY();
		// finalItems = cloneStateVector(finalItems);

		TreeItems newItems = new TreeItems();

		HashSet<String> hset = new HashSet<String>();

		for (int i = 0; i < currState.size(); i++) {
			TreeItem treeitem = (TreeItem) currState.elementAt(i);
			if (treeitem.getName().compareTo(CODE) == 0) {
				TreeItem clone = treeitem.cloneTreeItem(true);
				clone.setValue(code);
				clone.setNameValue(treeitem.getDisplayName(), code);
				newItems.add(clone);
			} else if (treeitem.getName().compareTo(PREFERRED_NAME) == 0) {
				String prop_name = FULL_SYN;
				if (prop_name != null) {
					String prop_value = treeitem.getValue();
					
					ArrayList<String> vals = new ArrayList<String>();
					vals.add(prop_value);
					vals.add("SY");
					vals.add("NCI");
					vals.add("");
					vals.add("en");
					String newvalue = _tab.getSerializedCustomizedAnnotationData(NCIEditTab.ALTLABEL, 
							vals);
					
					String hash_value = prop_name + ": " + newvalue;

					if (!hset.contains(hash_value)) {
						TreeItem clone = treeitem.cloneTreeItem(true);
						//clone.setName(prop_name);
						clone.setValue(newvalue);
						clone.setNameValue(treeitem.getDisplayName(), newvalue);
						newItems.add(clone);
						hset.add(hash_value);
					}
				}
			} else if (treeitem.getName().compareTo(FULL_SYN) == 0) {
				String t = treeitem.getValue();
				String hash_value = treeitem.getName() + ": " + t;
				if (!hset.contains(hash_value)) {
					TreeItem clone = treeitem.cloneTreeItem(true);
					clone.setValue(t);
					clone.setNameValue(treeitem.getDisplayName(), t);
					newItems.add(clone);
					hset.add(hash_value);
				}
			}
			else if (treeitem.getCls() instanceof RDFSClass) {
			    RDFSClass tc = treeitem.getCls();
			    RDFSClass nc = tc.createClone();
                treeitem.setCls(nc);
                //tc.delete();
                newItems.add(treeitem.cloneTreeItem(false));
			}
			else if (_tab.getNonTransferProps().contains(treeitem.getName())) {
				
			} else if (treeitem.getName().compareTo("rdfs:label") != 0) {
				newItems.add(treeitem.cloneTreeItem(true));
			}
		}
		hset.clear();

		TreeNode root = new TreeNode(item, TYPE_NOT_SET);

		_lowerTreePanel = new TreePanel(_tab, _tab.getSelectedInstance(), root,
				code, newItems);
		setupTreePanel(_lowerTreePanel, false, true, false);
		_tab.addToListenedToClses((OWLNamedClass) _tab.getSelectedInstance(), this);

		_lowerTreePanel.addProperty("rdfs:label", pt, null);
		_lowerTreePanel.addProperty(PREFERRED_NAME, pt, null);

		String prop_name = NCIEditTab.ALTLABEL;
		
		
		ArrayList<String> vals = new ArrayList<String>();
		vals.add(pt);
		vals.add("PT");
		vals.add("NCI");
		vals.add("");
		vals.add("en");
		String prop_value = _tab.getSerializedCustomizedAnnotationData(NCIEditTab.ALTLABEL, 
				vals);

		if (prop_name != null) {
			_lowerTreePanel.addProperty(prop_name, prop_value, null);
		}

		// need to copy subclasses when the Save button is pressed
		// subclasses are not visible

		_lowerTreePanel.setCanDrop(false);
		_upperTreePanel.setCanDrop(false);
		_lowerScrollPane.setViewportView(_lowerTreePanel);

		_splitButton.setEnabled(false);
		
		_lowerTreePanel.enablePopUpWindow();
		_upperTreePanel.enablePopUpWindow();
		
		_saveButton.setEnabled(true);
		_isDataModified = true;
		_clearButton.setEnabled(true);

		_lowerReviewAction.setTreePanel(_lowerTreePanel);
		return true;
	}

	public JScrollPane getScrollPane(TreePanel.PanelType type,
			TreePanel.PanelType subtype) {
		if (type == TreePanel.PanelType.TYPE_SPLIT)
			return _upperScrollPane;
		return null;
	}

	public void updateCls(Object obj, Cls cls) {
		if (obj == _upperTreePanel)
			_upperReviewAction.setTreePanel(_upperTreePanel);
		else if (obj == _lowerTreePanel)
			_lowerReviewAction.setTreePanel(_lowerTreePanel);
	}
}
