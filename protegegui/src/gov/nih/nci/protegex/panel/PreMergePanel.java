package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.dialog.NoteDialog;
import gov.nih.nci.protegex.edit.DataHandler;
import gov.nih.nci.protegex.edit.NCIEditFilter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.TreePanelReviewAction;
import gov.nih.nci.protegex.tree.TreeItem;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeNode;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.ClsUtil;
import gov.nih.nci.protegex.util.MsgDialog;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;

public class PreMergePanel extends NCIDoublePanel {
	private static final long serialVersionUID = 4368565152182764625L;

	private final String EDITOR_NOTE = NCIEditTab.EDITORIALNOTE;

	private final String DESIGNNOTE = NCIEditTab.SCOPENOTE;

	private final String MERGE_SOURCE = NCIEditTab.MERGERETIRE;

	private final String MERGE_TARGET = NCIEditTab.MERGESURVIVING;

	private final String PREMERGED_CONCEPTS = NCIEditTab.PREMERGED_CONCEPTS;

	private JButton _premergeButton;

	private JButton _unmergeButton;

	private boolean _unmergeButtonPressed;

	private TreePanelReviewAction _upperReviewAction;

	private TreePanelReviewAction _lowerReviewAction;

	private NCIEditFilter _editfilter = null;

	public PreMergePanel(NCIEditTab tab, NCIEditFilter editFilter) {
		super(tab, TreePanel.PanelType.TYPE_PREMERGE);
		_editfilter = editFilter;
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		initObjects();

		LabeledComponent upperLC = new LabeledComponent("Surviving Concept",
				_upperScrollPane);
		LabeledComponent lowerLC = new LabeledComponent("Retiring Concept",
				_lowerScrollPane);

		//JFrame frame = (JFrame) _tab.getTopLevelAncestor();
		_upperReviewAction = new TreePanelReviewAction(getUpperTreePanel());
		upperLC.addHeaderButton(_upperReviewAction);
		_lowerReviewAction = new TreePanelReviewAction(getLowerTreePanel());
		lowerLC.addHeaderButton(_lowerReviewAction);
		JSplitPane mergePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				upperLC, lowerLC);

		mergePane.setDividerLocation(280);
		mergePane.setOneTouchExpandable(true);

		add(mergePane, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(_unmergeButton = createButton("UnMerge", false));
		panel.add(_premergeButton = createButton("PreMerge", false));
		panel.add(_saveButton = createButton("Save", false));
		panel.add(_clearButton = createButton("Clear", false));
		return panel;
	}

	private void initObjects() {
		_upperTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_upperTreePanel, true, true, true);
		_upperScrollPane.setViewportView(_upperTreePanel);

		_lowerTreePanel = new TreePanel(_tab, null, _kb);
		setupTreePanel(_lowerTreePanel, false, true, true);
		_lowerScrollPane.setViewportView(_lowerTreePanel);
	}

	public void reset() {
		super.reset();
		initObjects();
		_tab.clearListenedToClses();
		_upperReviewAction.setTreePanel(null);
		_lowerReviewAction.setTreePanel(null);
	}

	private void premerge() {
		try {
			setCursor(WAIT_CURSOR);
			String retiringClsName = _lowerTreePanel.getLocalName();
			String survivingClsName = _upperTreePanel.getDisplayName();
			String survivingClsCode = _upperTreePanel.getCode();
			if (retiringClsName == null || retiringClsName.compareTo("") == 0) {
				setCursor(DEFAULT_CURSOR);
				return;
			}
			if (survivingClsName == null || survivingClsName.compareTo("") == 0) {
				setCursor(DEFAULT_CURSOR);
				return;
			}
			if (survivingClsName.compareTo(retiringClsName) == 0) {
				setCursor(DEFAULT_CURSOR);
				MsgDialog.ok(this, "Cannot merge a concept with itself.");
				return;
			}

			String editornote = "";
			String designnote = "";

			// GF#7520
			editornote = "Merge into " + survivingClsName + "(" + survivingClsCode + ")";
			
			if (_tab.getUserName() != null) {
			    editornote += ", " + _tab.getUserName();
			}
					
			designnote = "See '" + survivingClsName + "(" + survivingClsCode + ")" + "'";

			String prefix = "premerge_annotation";

			NoteDialog dlg = new NoteDialog(_tab, editornote, designnote,
					prefix);

			editornote = dlg.getEditorNote();
			designnote = dlg.getDesignNote();

			if (_lowerTreePanel == null) {
				System.out.println("WARNING: lowerTreePanel_Premerge == null");
			} else if (dlg.OKBtnPressed()) {
				JTree edit_tree = _lowerTreePanel.getTree();

				String prop_name = EDITOR_NOTE;
				_lowerTreePanel.addProperty(prop_name, editornote, null);
				prop_name = DESIGNNOTE;
				_lowerTreePanel.addProperty(prop_name, designnote, null);

				JTree edit_tree2 = _upperTreePanel.getTree();

				prop_name = MERGE_TARGET;
				_lowerTreePanel.addProperty(prop_name, survivingClsName, null);

				String prop_value = PREMERGED_CONCEPTS;
				Cls premergedCls = _wrapper.getCls(prop_value);

				_lowerTreePanel.addParent(premergedCls, false);

				prop_name = MERGE_SOURCE;
				_upperTreePanel.addProperty(prop_name, retiringClsName, null);

				edit_tree.repaint();
				edit_tree2.repaint();

				_saveButton.setEnabled(true);
				_isDataModified = true;
				_premergeButton.setEnabled(false);
				_unmergeButton.setEnabled(true);
				
				_lowerTreePanel.enablePopUpWindow();
				_upperTreePanel.enablePopUpWindow();

				_unmergeButtonPressed = false;
				// _lowerReviewAction.setEnabled(false);
			}
		} catch (Exception ex) {
			_logger.warning("Exception caught" + ex.toString());
			OWLUI.handleError(_kb, ex);
		}
		setCursor(DEFAULT_CURSOR);
	}

	private void unmerge() {
		try {
			setCursor(WAIT_CURSOR);
			if (_lowerTreePanel == null) {
				System.out.println("WARNING: lowerTreePanel_Premerge == null");
			} else {
				JTree edit_tree = _lowerTreePanel.getTree();

				_tab.deleteNoteProperties(_lowerTreePanel);

				JTree edit_tree2 = _upperTreePanel.getTree();

				String prop_name = MERGE_TARGET;
				_lowerTreePanel.deleteProperty(prop_name);

				String prop_value = PREMERGED_CONCEPTS;
				_lowerTreePanel.deleteParent(prop_value);

				prop_name = MERGE_SOURCE;
				prop_value = _upperTreePanel.getPropertyValue(prop_name);
				_upperTreePanel.deleteProperty(prop_name, prop_value, null);

				edit_tree.repaint();
				edit_tree2.repaint();

				_saveButton.setEnabled(true);
				_isDataModified = true;
				_premergeButton.setEnabled(true);
				_unmergeButton.setEnabled(false);

				_unmergeButtonPressed = true;
				// _lowerReviewAction.setCls(_lowerTreePanel.droppedCls);
			}
		} catch (Exception ex) {
			_logger.warning("Exception caught" + ex.toString());
			OWLUI.handleError(_kb, ex);
		}
		setCursor(DEFAULT_CURSOR);
	}

	protected void save() {
        String upperClsName = _upperTreePanel.getLocalName();
        String lowerClsName = _lowerTreePanel.getLocalName();
		Cls fromCls = _wrapper.getCls(upperClsName);
		Cls toCls = _wrapper.getCls(lowerClsName);
		

		setCursor(WAIT_CURSOR);
		DataHandler.Status status = _tab.getDataHandler().canSaveData(
				_lowerTreePanel, toCls);
		if (status != DataHandler.Status.SUCCESSFUL) {
			setCursor(DEFAULT_CURSOR);
			if (status == DataHandler.Status.FAILURE) {
				MsgDialog.ok(this, "Unable to flag "
						+ _lowerTreePanel.getDisplayName() + " and "
						+ _upperTreePanel.getDisplayName() + " for merge -- "
						+ _editfilter.getErrorMessage());
			}
			
			return;
		}
		status = _tab.getDataHandler().canSaveData(_upperTreePanel, fromCls);
		if (status != DataHandler.Status.SUCCESSFUL) {
			setCursor(DEFAULT_CURSOR);
			if (status == DataHandler.Status.FAILURE) {
				MsgDialog.ok(this, "Unable to flag "
						+ _lowerTreePanel.getDisplayName() + " and "
						+ _upperTreePanel.getDisplayName() + " for merge -- "
						+ _editfilter.getErrorMessage());
			}
			
			
			return;
		} else {

			try {
				_tab.removeFromListenedToClses((OWLNamedClass) fromCls, this);
				_tab.removeFromListenedToClses((OWLNamedClass) toCls, this);
				_kb.beginTransaction("Premerge " + upperClsName
						+ " and " + lowerClsName, fromCls.getName());
				if ((_tab.saveConcept(_lowerTreePanel) != DataHandler.Status.SUCCESSFUL)
						|| (_tab.saveConcept(_upperTreePanel) != DataHandler.Status.SUCCESSFUL)) {
					_kb.rollbackTransaction();					
					return;

				}

				_kb.commitTransaction();
				

			} catch (Exception ex) {
				_kb.rollbackTransaction();
				OWLUI.handleError(_kb, ex);				
				return;
			} finally {
				_tab.addToListenedToClses((OWLNamedClass) fromCls, this);
				_tab.addToListenedToClses((OWLNamedClass) toCls, this);
				
			}

			setCursor(DEFAULT_CURSOR);
			
			_tab.ensureClassSelected(fromCls);
			if (_unmergeButtonPressed) {
				MsgDialog.ok(this,
						"Merge flags have been removed from concepts "
								+ _lowerTreePanel.getDisplayName() + " and "
								+ _upperTreePanel.getDisplayName());
			} else {
				MsgDialog.ok(this, "Concepts " + _lowerTreePanel.getDisplayName()
						+ " and " + _upperTreePanel.getDisplayName()
						+ " have been flagged for merge.");
			}
			reset();
		}

	}

	/**
	 * Sets up this panel while a concept is loading.
	 * 
	 * @param treePanel
	 *            The corresponding TreePanel.
	 */
	public void setupWhileLoadingConcept(TreePanel treePanel) {
		if (_upperTreePanel.containsCls() && _lowerTreePanel.containsCls())
			_premergeButton.setEnabled(true);
		_clearButton.setEnabled(true);
	}

	protected void performAction(JButton button) {
		super.performAction(button);
		if (button == _premergeButton)
			premerge();
		else if (button == _unmergeButton)
			unmerge();
	}

	private void displaySwapMessage() {
		Cls upperCls = _upperTreePanel.getDroppedCls();
		Cls lowerCls = _lowerTreePanel.getDroppedCls();
		if (upperCls == null || lowerCls == null)
			return;

		int upperNumericCode = ClsUtil.getNumericCode(_wrapper, upperCls);
		int lowerNumericCode = ClsUtil.getNumericCode(_wrapper, lowerCls);
		if (upperNumericCode <= lowerNumericCode)
			return;

		int ans = MsgDialog.yesOrNo(_tab,
				"The retiring concept is older than the surviving concept."
						+ "\nDo you want to swap them?");
		if (ans == MsgDialog.YES_OPTION)
			swapConcepts();
	}

	private void swapConcepts() {
		OWLNamedClass upperCls = _upperTreePanel.getDroppedCls();
		OWLNamedClass lowerCls = _lowerTreePanel.getDroppedCls();

		if (upperCls == null || lowerCls == null)
			return;

		String upperCode = _upperTreePanel.getCode();
		String lowerCode = _lowerTreePanel.getCode();

		_upperTreePanel.setDroppedCls(lowerCls, lowerCode);
		_lowerTreePanel.setDroppedCls(upperCls, upperCode);
	}

	private boolean populate(String name) {
		if (name == null || name.length() <= 0)
			return false;

		String slotname_src = MERGE_SOURCE;
		String slotname_target = MERGE_TARGET;

		//TreeItem item = new TreeItem();
		//item.setType(TYPE_CONCEPT);
		//item.setName(name);

		//TreeNode dmtn = new TreeNode(item, TYPE_NOT_SET);
		Instance selectedInstance = (Instance) _kb.getRDFSNamedClass(name);
		OWLNamedClass cls = (OWLNamedClass) selectedInstance;
		//item.setCls(cls);
		//item.setNameValue(cls.getBrowserText());

		if (_wrapper
				.hasAnnotationProperty(cls, slotname_target)) {
			_lowerTreePanel.createTree(cls);
			_lowerTreePanel.setCanDrop(false);
            //= new TreePanel(_tab, selectedInstance, dmtn,
				//	null);
			//setupTreePanel(_lowerTreePanel, false, true, false);
			//_lowerScrollPane.setViewportView(_lowerTreePanel);
		} else if (_wrapper.hasAnnotationProperty(cls,
				slotname_src)) {
			_upperTreePanel.createTree(cls);
			_upperTreePanel.setCanDrop(false);
            //= new TreePanel(_tab, selectedInstance, dmtn,
				//	null);
			//setupTreePanel(_upperTreePanel, true, true, false);
			//_upperScrollPane.setViewportView(_upperTreePanel);
		}
		_unmergeButton.setEnabled(true);
		_premergeButton.setEnabled(false);
		_saveButton.setEnabled(false);
		_isDataModified = false;
		_clearButton.setEnabled(true);
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
	public DropType populate(OWLNamedClass cls, TreePanel.PanelType subtype) {
        String title = "Unable to load";
		if (_wrapper.hasAnnotationProperty(cls, MERGE_SOURCE)) {
			if (subtype == _lowerSubtype) {
				//MsgDialog.warning(_tab, title,
				    //MERGE_TARGET + " cannot be identified.");
				return DropType.TRYAGAIN;
			}
			if (!populate(_wrapper.getPropertyValue(cls, MERGE_SOURCE))) {
				MsgDialog.warning(_tab, title,
						"Unable to load pre-merged concepts.");
				return DropType.BAD;
			}
		} else if (_wrapper.hasAnnotationProperty(cls, MERGE_TARGET)) {
			if (subtype == _upperSubtype) {
				//MsgDialog.warning(_tab, title,
				        //MERGE_SOURCE + " cannot be identified.");
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

	public JScrollPane getScrollPane(TreePanel.PanelType type, TreePanel.PanelType subtype) {
		if (type == TreePanel.PanelType.TYPE_PREMERGE) {
			if (subtype == TreePanel.PanelType.TYPE_PREMERGE_LOWER)
				return _lowerScrollPane;
			else
				return _upperScrollPane;
		}
		return null;
	}

	public void updateCls(Object obj, Cls cls) {
		if (obj == _upperTreePanel)
			_upperReviewAction.setTreePanel(_upperTreePanel);
		else if (obj == _lowerTreePanel)
			_lowerReviewAction.setTreePanel(_lowerTreePanel);
		displaySwapMessage();
	}
}
