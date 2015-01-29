/**
 *
 */
package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.dialog.*;
import gov.nih.nci.protegex.edit.DataHandler;
import gov.nih.nci.protegex.edit.NCIEditFilter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.TreePanelReviewAction;
import gov.nih.nci.protegex.tree.TreeItem;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreeNode;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.tree.TreePanel.PanelType;
import static gov.nih.nci.protegex.tree.TreePanel.PanelType.*;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.StringUtil;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ClonePanel extends NCIDoublePanel {
	private static final long serialVersionUID = -2657346249351475064L;

	private JButton _cloneButton;

	

	private JSplitPane _splitPane;

	private boolean _clonedCls;

	private TreePanelReviewAction _upperReviewAction;

	private TreePanelReviewAction _lowerReviewAction;

	public ClonePanel(NCIEditTab tab) {
		super(tab, TYPE_CLONE);
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

		//JFrame frame = (JFrame) _tab.getTopLevelAncestor();
		_upperReviewAction = new TreePanelReviewAction(getUpperTreePanel());
		upperLC.addHeaderButton(_upperReviewAction);
		_lowerReviewAction = new TreePanelReviewAction(getLowerTreePanel());
		lowerLC.addHeaderButton(_lowerReviewAction);

		_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperLC, lowerLC);
		_splitPane.setDividerLocation(280);
		_splitPane.setOneTouchExpandable(true);

		add(_splitPane, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(_cloneButton = createButton("Clone", false));
		panel.add(_saveButton = createButton("Save", false));
		panel.add(_clearButton = createButton("Clear", false));
		return panel;
	}

	private void initObjects() {
		_upperTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_upperTreePanel, true, true, true);
		_upperTreePanel.enablePopUpWindow();
		_upperScrollPane.setViewportView(_upperTreePanel);

		_lowerTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_lowerTreePanel, false, true, true);
		_lowerTreePanel.enablePopUpWindow();
		_lowerScrollPane.setViewportView(_lowerTreePanel);
	}

	public void reset() {
		super.reset();
		initObjects();

		_tab.clearListenedToClses();
		_upperReviewAction.setTreePanel(null);
		_lowerReviewAction.setTreePanel(null);
	}

	private void cloneConcept() {
		try {

			String label = "";
			String pt = "";
			String def = "";
			boolean cancelled = false;
			boolean hasError = true;

			while (!cancelled) {
				NewClsDialog dlg = new NewClsDialog(_tab, true);
				dlg.init(label, pt, def);
				cancelled = dlg.cancelButtonPressed;

				hasError = false;
				if (cancelled)
					break;
				label = dlg.getLabel();
				pt = dlg.getPT().trim();
				def = dlg.getDefinition().trim();

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
				String str_def = StringUtil.escapeXML(def);
				boolean canClone = populate(label, pt, def);
				if (!canClone) {
					MsgDialog.ok(this, "Unable to create a new concept.");
					_cloneButton.setEnabled(false);
					return;
				}
				_clonedCls = true;
			}

		} catch (Exception ex) {
			_logger.log(Level.WARNING, "Exception caught", ex);
			OWLUI.handleError(_kb, ex);
		}
	}

	protected void save() {
        String upperClsName = _upperTreePanel.getLocalName();
        String lowerClsName = _lowerTreePanel.getLocalName();
        Cls fromCls = _wrapper.getCls(upperClsName);
		Cls toCls = _wrapper.getCls(lowerClsName);
		_tab.removeFromListenedToClses((OWLNamedClass) fromCls, this);
		_tab.removeFromListenedToClses((OWLNamedClass) toCls, this);

		boolean concept1_exists = false;
		if (_upperTreePanel.droppedCls != null
				&& upperClsName.compareTo("") != 0) {
			concept1_exists = true;
		}

		boolean concept2_exists = false;
		if (_lowerTreePanel.getRootNode() != null) {
			concept2_exists = true;
		}

		if (concept1_exists && !concept2_exists) {
			DataHandler.Status status = _tab.getDataHandler().canSaveData(
					_upperTreePanel, fromCls);
			if (status != DataHandler.Status.SUCCESSFUL) {
				if (status == DataHandler.Status.FAILURE)
					_tab.showError(_upperTreePanel);
				_tab.addToListenedToClses((OWLNamedClass) fromCls, this);

				return;
			} else {
				try {
					_kb.beginTransaction("Saving top pane concept "
							+ upperClsName, fromCls.getName());
					if (_tab.saveConcept(_upperTreePanel) != DataHandler.Status.SUCCESSFUL) {
						_kb.rollbackTransaction();
					} else {
					    //Hack: There seems to be a timing issue when
					    //  synchronizing the preferred name in FullSyn, 
					    //  Preferred Name, and rdfs:label properties.
					    //  The following sleep statement prevents duplicated
					    //  rdfs:labels from appearing.
					    Thread.sleep(300);
						_kb.commitTransaction();
					}
				} catch (Exception ex) {
					_kb.rollbackTransaction();
					OWLUI.handleError(_kb, ex);

					_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
					_tab.addToListenedToClses((OWLNamedClass) toCls, this);
					return;

				}
			}

			_upperTreePanel.setDroppedCls((OWLNamedClass) fromCls);
			_saveButton.setEnabled(false);
			_isDataModified = false;
			MsgDialog.ok(this, "Concept " + _upperTreePanel.getDisplayName()
					+ " saved successful.");
		} else if (concept1_exists && concept2_exists) {
			DataHandler.Status status = _tab.getDataHandler().canSaveData(
					_upperTreePanel, fromCls);
			if (status != DataHandler.Status.SUCCESSFUL) {
				if (status == DataHandler.Status.FAILURE)
					_tab.showError(_upperTreePanel);
				_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
				_tab.addToListenedToClses((OWLNamedClass) toCls, this);
				return;
			}

			if (_clonedCls)
				status = _tab.getDataHandler().canCreateData(lowerClsName,
						_lowerTreePanel.getCurrentState());
			else
				status = _tab.getDataHandler().canSaveData(_lowerTreePanel,
						toCls);

			if (status != DataHandler.Status.SUCCESSFUL) {
				if (status == DataHandler.Status.FAILURE)
					_tab.showError(_lowerTreePanel.getDisplayName());
				_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
				_tab.addToListenedToClses((OWLNamedClass) toCls, this);
				return;
			} else {

				try {
					_kb.beginTransaction("Create a copy (clone) of "
							+ upperClsName, fromCls.getName());

					if (_tab.saveConcept(_upperTreePanel) != DataHandler.Status.SUCCESSFUL) {
						_kb.rollbackTransaction();
						return;
					}
					if (_tab.saveConcept(_lowerTreePanel, lowerClsName) != 
					        DataHandler.Status.SUCCESSFUL) {
						_kb.rollbackTransaction();
						return;
					}

					_kb.commitTransaction();

				} catch (Exception ex) {
					_kb.rollbackTransaction();
					OWLUI.handleError(_kb, ex);

					_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
					_tab.addToListenedToClses((OWLNamedClass) toCls, this);
					return;
				}

			}

			_upperTreePanel.setDroppedCls((OWLNamedClass) fromCls);
			if (toCls == null)
				toCls = _wrapper.getCls(lowerClsName);
			_lowerTreePanel.setDroppedCls((OWLNamedClass) toCls);
			_saveButton.setEnabled(false);
			_isDataModified = false;
			MsgDialog.ok(this, "Concepts saved successful.");
		}

		// 050906
		_clonedCls = false;

		_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
		_tab.addToListenedToClses((OWLNamedClass) toCls, this);
	}

	/**
	 * Sets up this panel while a concept is loading.
	 * 
	 * @param treePanel
	 *            The corresponding TreePanel.
	 */
	public void setupWhileLoadingConcept(TreePanel treePanel) {
		_cloneButton.setEnabled(getNumClses() == 1);
		_clearButton.setEnabled(true);
	}

	protected void performAction(JButton button) {
		super.performAction(button);
		if (button == _cloneButton)
			cloneConcept();
	}

	private boolean populate(String name, String pt, String def) {
	    
	    if (def.equalsIgnoreCase("")) {
	        MsgDialog.ok(this, "Data entry reminder",
            "Reminder -- definition has not been provided.");
	    }

		String code = _tab.getWrapper().getCode();
		if (_tab.byCode()) {
			if (code == null || code.compareTo("0") == 0)
				return false;
		}

		// get final state from upperTreePanel_Split
		TreeItems items = _upperTreePanel.getCurrentState();

		items = items.convertNCIPT2NCISY();
		
		// why are we doing this???????
		//items = items.clone();

		// GF1179
		if (items == null)
			return false;

		TreeItem item = new TreeItem();
		item.setType(TYPE_CONCEPT);

		if (_wrapper.codeSlotExists) {
			item.setName(name);
			item.setNameValue(name);
		} else {
	        item.setName(code);
	        item.setNameValue(code);
		}

		TreeItems w = new TreeItems();
		

        

		HashSet<String> hset = new HashSet<String>();

		for (int i = 0; i < items.size(); i++) {
			TreeItem treeitem = (TreeItem) items.elementAt(i);
			if (treeitem.getName().compareTo(CODE) == 0) {
				TreeItem clone = treeitem.cloneTreeItem(true);
				clone.setValue(code);
				clone.setNameValue(treeitem.getDisplayName(), code);
				w.add(clone);
			} else if (treeitem.getName().compareTo(NCIEditTab.DEFINITION) == 0) {
				
				
				
			} else if (treeitem.getName().compareTo(PREFERRED_NAME) == 0) {
			    
				
			} else if (treeitem.getName().compareTo(FULL_SYN) == 0) {
				String t = treeitem.getValue();
				String hash_value = treeitem.getName() + ": " + t;
				if (!hset.contains(hash_value)) {
					TreeItem clone = treeitem.cloneTreeItem(true);
					clone.setValue(t);
					clone.setNameValue(treeitem.getDisplayName(), t);
					w.add(clone);
					hset.add(hash_value);
				}
			}

			else if (treeitem.getName().compareTo("rdfs:label") == 0) {
				//w.add(treeitem.cloneTreeItem(true));
			} else if (_tab.getNonTransferProps().contains(treeitem.getName())) {
				
			} else if (treeitem.getCls() instanceof RDFSClass) {
			    treeitem.setCls(treeitem.getCls().createClone());
			    w.add(treeitem.cloneTreeItem(false));
			} else {
				w.add(treeitem.cloneTreeItem(true));
			}
		}
		hset.clear();

		TreeNode root = new TreeNode(item, TYPE_NOT_SET);
		_lowerTreePanel = new TreePanel(_tab, _tab.getSelectedInstance(),
				root, code, w);
		_lowerTreePanel.enablePopUpWindow();
		setupTreePanel(_lowerTreePanel, false, true, true);
		_tab.addToListenedToClses((OWLNamedClass) _tab.getSelectedInstance(), this);

		_lowerTreePanel.addProperty("rdfs:label", pt, null);
		_lowerTreePanel.addProperty(PREFERRED_NAME, pt, null);
		if (!def.equalsIgnoreCase("")) {
		    String ns = _wrapper.formatDEFINITION("NCI", def);
		    _lowerTreePanel.addProperty(NCIEditTab.DEFINITION, ns, null);
		}
		   

		String prop_name = NCIEditTab.ALTLABEL;
		
		ArrayList<String> vals = new ArrayList<String>();
		vals.add(pt);
		vals.add("PT");
		vals.add("NCI");
		vals.add("");
		vals.add("en");
		String prop_value = NCIEditTab.getSerializedCustomizedAnnotationData(prop_name, 
				vals);
		

		if (prop_name != null) {
			_lowerTreePanel.addProperty(prop_name, prop_value, null);
		}

		// need to copy subclasses when the Save button is pressed
		// subclasses are not visible

		_upperTreePanel.setCanDrop(false);
		_lowerScrollPane.setViewportView(_lowerTreePanel);

		_cloneButton.setEnabled(false);
		_saveButton.setEnabled(true);
		_isDataModified = true;
		_clearButton.setEnabled(true);

		_lowerReviewAction.setTreePanel(_lowerTreePanel);
		return true;
	}

	public JScrollPane getScrollPane(PanelType type, PanelType subtype) {
		if (type == TYPE_CLONE) {
			int num = getNumClses();
			if (num == 1)
				return _upperScrollPane;
			else if (num == 2) {
				if (subtype == TYPE_CLONE_LOWER)
					return _lowerScrollPane;
				else
					return _upperScrollPane;
			}
		}
		return null;
	}

	public int getNumClses() {
		int num = 0;
		if (_upperTreePanel.containsCls())
			num++;
		if (_lowerTreePanel.containsCls())
			num++;
		return num;
	}

	public void updateCls(Object obj, Cls cls) {
		if (obj == _upperTreePanel)
			_upperReviewAction.setTreePanel(_upperTreePanel);
		else if (obj == _lowerTreePanel)
			_lowerReviewAction.setTreePanel(_lowerTreePanel);
	}
}
