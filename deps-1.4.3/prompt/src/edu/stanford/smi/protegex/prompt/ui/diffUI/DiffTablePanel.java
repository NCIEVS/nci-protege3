/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Sandhya Kunnatur kunnatur@stanford.edu
* 		   Abhita Chugh abhita@stanford.edu
*/
package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTable;
import edu.stanford.smi.protege.widget.AbstractSlotWidget;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protegex.changes.ui.ChangeAnnotateWindow;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.promptDiff.users.ChangeManagement;
import edu.stanford.smi.protegex.prompt.ui.FrameInMergingRenderer;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.prompt.util.Util;
import edu.stanford.smi.protegex.server_changes.prompt.AuthorManagement;

public class DiffTablePanel extends SelectableContainer {
    private static Logger log = Log.getLogger(DiffTablePanel.class);

	private SelectableTable _table;
	private TableRow _row;
	private ArrayList<FrameDifferenceElement> _displayedDiffs;
	private DiffTreeView _treeView;
	private DiffClsesPanel _clsesPanel;
	private JLabel _authorLabel = new JLabel();
	private AbstractSlotWidget _selectedWidget;
	private boolean _customised;
	private boolean isOwl;

	private static String columnNames[] = { "Operation", "Slot", "Facet", "Old Value", "New Value" };
	private static String owlColumnNames[] = { "Operation", "Property", "Restriction", "Old Value", "New Value" };

	private final String fontName = _authorLabel.getFont().getFontName();
	private final String fontFamily = _authorLabel.getFont().getFamily();
	private final String CHANGED_BY_BEGIN = "<html><font face = \"" + fontName + "\" family = \"" + fontFamily + "\"><b> Changed by: </b>";
	private final String CHANGED_BY_END = "</font></html>";

	public DiffTablePanel(DiffTreeView treeView, DiffClsesPanel clsesPanel, ResultTable diffTable) {
		super();
		setLayout(new BorderLayout());
		isOwl = PromptTab.kbInOWL();
		_table = createTable(isOwl);
		_treeView = treeView;
		_clsesPanel = clsesPanel;

		JPanel authors = createAuthorsPanel();
		add(authors, BorderLayout.NORTH);

		LabeledComponent diffElements = createTableComponent();
		add(diffElements, BorderLayout.CENTER);
		setSelectable(_table);

	}

	private JPanel createAuthorsPanel() {
		JPanel authors = new JPanel();
		authors.setLayout(new BorderLayout());
		_authorLabel.setText(CHANGED_BY_BEGIN + CHANGED_BY_END);
		authors.add(_authorLabel, BorderLayout.CENTER);

		return authors;
	}

	private LabeledComponent createTableComponent() {
		LabeledComponent c = new LabeledComponent("Differences", ComponentFactory.createScrollPane(_table));
		addHeaderButtons(c);
		return c;
	}

	private SelectableTable createTable(boolean isOwl) {
		SelectableTable table = new SelectableTable();
		table.setModel(createTableModel(isOwl));

		TableColumnModel columnModel = table.getColumnModel();

		columnModel.getColumn(0).setCellRenderer(new DefaultRenderer());
		columnModel.getColumn(1).setCellRenderer(new SlotColumnRenderer());
		columnModel.getColumn(2).setCellRenderer(new FrameInMergingRenderer());
		columnModel.getColumn(3).setCellRenderer(new FrameInMergingRenderer());
		columnModel.getColumn(4).setCellRenderer(new FrameInMergingRenderer());

		return table;
	}

	private TableModel createTableModel(boolean isOwl) {
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		String[] tableColumnNames;

		if (isOwl) {
			tableColumnNames = owlColumnNames;
		} else {
			tableColumnNames = columnNames;
		}

		for (String tableColumnName : tableColumnNames) {
			model.addColumn(tableColumnName);
		}

		return model;
	}

	private void addHeaderButtons(LabeledComponent labeledComponent) {
		labeledComponent.addHeaderButton(getAcceptDiffAction("Accept Diff"));
		labeledComponent.addHeaderButton(getRejectDiffAction("Reject Diff"));
		labeledComponent.addHeaderButton(getCompareDiffAction("Compare Changes"));
		labeledComponent.addHeaderButton(getViewAnnotateAction("View Annotations"));
	}

	public void setRow(TableRow row, boolean customised) {
		clearSelection();
		clearModelRows((DefaultTableModel) _table.getModel());
		_row = row;
		_customised = customised;
		setValues();
	}

	private void setValues() {

		DefaultTableModel model = (DefaultTableModel) _table.getModel();
		if (_row == null) {
			return;
		}

		setAuthors();
		setDiffs(model);
	}

	private void setAuthors() {
	    boolean oldName = _row.getOperationValue().equals(TableRow.OPERATION_DELETE);
		Frame changedConcept = oldName ? _row.getF1Value() : _row.getF2Value();
		String authors;
        AuthorManagement authorManagement = PromptTab.getPromptDiff().getAuthorManagement();
		if (changedConcept != null && authorManagement != null) {
		    Collection<String> users = authorManagement.getEditorsByFrameName(changedConcept.getName(), oldName);
		    authors = Util.createAuthorString(users);
		} else {
			authors = "";
		}
		String authorLabel = CHANGED_BY_BEGIN + authors + CHANGED_BY_END;
		_authorLabel.setText(authorLabel);
		_authorLabel.repaint();
	}

	private void setDiffs(DefaultTableModel model) {
		Collection allDiffs = _row.getOperationExplanation();
		_displayedDiffs = new ArrayList<FrameDifferenceElement>();

		int row = 0;
		Iterator i = allDiffs.iterator();
		while (i.hasNext()) {
			FrameDifferenceElement d = (FrameDifferenceElement) i.next();

			if (displayDiff(d)) {
				Object slot = d.getSlotValue();
				if (slot == null) {
					slot = "";
				}
				if (PromptTab.kbInOWL() && d.getRelationshipToFrame().equals(FrameDifferenceElement.RESTRICTION)) {
					Object o1 = d.getO1Value();
					if (o1 != null && o1 instanceof Cls) {
						slot = OWLUtil.getPropertyForRestriction((Cls) o1);
					}
				}

				Object facet = d.getFacetValue();
				if (facet == null) {
					facet = "";
				}

				String changeDescription;
				if (PromptTab.kbInOWL()) {
					changeDescription = OWLUtil.getChangeDescription(ProjectsAndKnowledgeBases.getTargetKnowledgeBase(), _row, d);
				} else {
					changeDescription = d.getChangeDescription();
				}

				model.addRow(new Object[] { changeDescription, d, facet, d.getO1Value(), d.getO2Value() });
				_displayedDiffs.add(d);
				row++;
			}
		}
		_table.setModel(model);
	}

	private boolean displayDiff(FrameDifferenceElement diff) {
		if (diff.getChangeLevel() != FrameDifferenceElement.LEVEL_DIRECT) {
			return false;
		}

		String relationship = diff.getRelationshipToFrame();
		if (relationship == FrameDifferenceElement.FACET || relationship == FrameDifferenceElement.FACET_VALUE || relationship == FrameDifferenceElement.OWN_SLOT
				|| relationship == FrameDifferenceElement.OWN_SLOT_VALUE || relationship == FrameDifferenceElement.TYPE || relationship == FrameDifferenceElement.META_CLASS
				|| relationship == FrameDifferenceElement.META_SLOT || relationship == FrameDifferenceElement.RESTRICTION || relationship == FrameDifferenceElement.PRIMITIVE_TO_DEFINED
				|| relationship == FrameDifferenceElement.DEFINED_TO_PRIMITIVE || relationship == FrameDifferenceElement.SUPERCLASS) {

			return true;

		} else if (!_customised && diff.getRelationshipToFrame() == FrameDifferenceElement.TEMPLATE_SLOT) {
			return true;
		}
		return false;
	}

	private void clearModelRows(DefaultTableModel model) {
		int rowCount = model.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			model.removeRow(0);
		}
	}

	public void reload() {
		clearModelRows((DefaultTableModel) _table.getModel());
		setValues();
	}

	public FrameDifferenceElement getDiffElementAt(int row) {
		return _displayedDiffs.get(row);
	}

	private void selectWidget(Slot slot) {
	    if (_treeView == null) {
	        return;
	    }
		ClsWidget clsWidget = _treeView.getCurrentClsWidget();
		if (clsWidget == null) {
			return;
		}
		clearSelectedWidget();

		ResultTable diffTable = _treeView.getDiffTable();
		Slot slotMap = (Slot) Util.getMap(slot, diffTable);

		AbstractSlotWidget slotWidget = (AbstractSlotWidget) clsWidget.getSlotWidget(slot);
		if (slotWidget == null) {
			slotWidget = (AbstractSlotWidget) clsWidget.getSlotWidget(slotMap);
		}

		if (slotWidget != null) {
			_selectedWidget = slotWidget;
			slotWidget.setSelectedBorder();
		}
	}

	public void clearSelectedWidget() {
		if (_selectedWidget != null) {
			_selectedWidget.setNormalBorder();
			_selectedWidget = null;
		}
	}

	private Action getAcceptDiffAction(String prompt) {
		Action action = new AllowableAction(prompt, "Accept Slot/Facet value change", ComponentUtilities.loadImageIcon(TabComponent.class, "images/OK.gif"), _table) {
			public void actionPerformed(ActionEvent evt) {
			    if (log.isLoggable(Level.FINE)) {
			        log.fine("accept diffrow");
			    }
				AcceptorRejector acceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
				Instance selectedInstance = (Instance) _row.getF2Value();
				Cls selectedCls = null;

				if (selectedInstance instanceof Cls) {
					selectedCls = (Cls) _row.getF2Value();
				}
				int[] selectedRows = _table.getSelectedRows();
				for (int selectedRow : selectedRows) {
					FrameDifferenceElement diffEl = getDiffElementAt(selectedRow);
					String dslot = null;
					if (diffEl.getSlotValue() != null) {
						dslot = diffEl.getSlotValue().getName();
					}
					String dfacet = null;
					if (diffEl.getFacetValue() != null) {
						dfacet = diffEl.getFacetValue().getName();
					}
					String o1Name = null;
					if (diffEl.getO1Value() != null && diffEl.refersToFrame()) {
						o1Name = ((Frame) diffEl.getO1Value()).getName();
					}
					String o2Name = null;
					if (diffEl.getO2Value() != null && diffEl.refersToFrame()) {
						o2Name = ((Frame) diffEl.getO2Value()).getName();
					}
					if (selectedCls != null) {
						acceptorRejector.acceptChange(selectedCls, diffEl);

						if (diffEl.refersToFrame()) {
							ProjectsAndKnowledgeBases.getAcceptChangesKb().addChangeToKB(AcceptChangesKnowledgeBase.ACCEPT_CHANGE_IN_CLS, selectedCls.getName(), null, dslot, dfacet, o1Name, o2Name,
									true);
						} else {
							ProjectsAndKnowledgeBases.getAcceptChangesKb().addChangeToKB(AcceptChangesKnowledgeBase.ACCEPT_CHANGE_IN_CLS, selectedCls.getName(), null, dslot, dfacet,
									diffEl.getO1Value(), diffEl.getO2Value(), false);
						}
					} else {
						acceptorRejector.acceptChangeInInstance(selectedInstance.getDirectType(), selectedInstance, diffEl);
						if (diffEl.refersToFrame()) {
							ProjectsAndKnowledgeBases.getAcceptChangesKb().addChangeToKB(AcceptChangesKnowledgeBase.ACCEPT_CHANGE_IN_INSTANCE, selectedInstance.getDirectType().getName(),
									selectedInstance.getName(), dslot, dfacet, o1Name, o2Name, true);
						} else {
							ProjectsAndKnowledgeBases.getAcceptChangesKb().addChangeToKB(AcceptChangesKnowledgeBase.ACCEPT_CHANGE_IN_INSTANCE, selectedInstance.getDirectType().getName(),
									selectedInstance.getName(), dslot, dfacet, diffEl.getO1Value(), diffEl.getO2Value(), false);
						}

					}
				}
				((DiffTabComponent) PromptTab.getTabComponent()).reset();
			}

			@Override
			public void onSelectionChange() {
				int selectedRow = _table.getSelectedRow();

				clearSelectedWidget();
				if (_treeView != null) {
					_treeView.clearSelectedTemplateSlot();
				}
				if (selectedRow == -1) {
					setAllowed(false);
					return;
				}

				FrameDifferenceElement diffEl = getDiffElementAt(selectedRow);
				Assert.assertNotNull("Diff Element should not be NULL", diffEl);
				boolean allowed = diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT;
				setAllowed(allowed);

				if (diffEl.getRelationshipToFrame() == FrameDifferenceElement.OWN_SLOT_VALUE) {
					Slot slot = diffEl.getSlotValue();
					if (slot != null) {
						selectWidget(slot);
					}
				} else if (diffEl.getRelationshipToFrame() == FrameDifferenceElement.FACET_VALUE || diffEl.getRelationshipToFrame() == FrameDifferenceElement.FACET) {
					Slot slot = diffEl.getSlotValue();
					_treeView.selectTemplateSlot(slot);
				}
			}
		};
		return action;
	}

	private Action getViewAnnotateAction(String prompt) {

		Action action = new AllowableAction(prompt, "View Annotations", ComponentUtilities.loadImageIcon(TabComponent.class, "images/class_note.gif"), null) {

			public void actionPerformed(ActionEvent evt) {

				Cls selectedCls = (Cls) _row.getF2Value();
				ChangeAnnotateWindow ca = new ChangeAnnotateWindow(ChangeManagement.getInstance().getChangesKb(), selectedCls.getName(), false);

				ca.show();

			}

		};
		return action;
	}

	private Action getCompareDiffAction(String prompt) {

		Action action = new AllowableAction(prompt, "Compare Changes", ComponentUtilities.loadImageIcon(TabComponent.class, "images/compare.gif"), null) {

			public void actionPerformed(ActionEvent evt) {

				Instance selectedInstance = (Instance) _row.getF2Value();
				DiffCompareDialog _compareDialog = new DiffCompareDialog(PromptTab.getMainWindow(), selectedInstance, _treeView, _clsesPanel);

			}

		};
		return action;
	}

	private Action getRejectDiffAction(String prompt) {
		Action action = new AllowableAction(prompt, "Reject Slot/Facet value change", ComponentUtilities.loadImageIcon(TabComponent.class, "images/Cancel.gif"), _table) {
			public void actionPerformed(ActionEvent evt) {
			    if (log.isLoggable(Level.FINE)) {
			        log.fine("Reject diffrow");
			    }

				AcceptorRejector acceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
				Instance selectedInstance = (Instance) _row.getF2Value();
				Cls selectedCls = null;

				if (selectedInstance instanceof Cls) {
					selectedCls = (Cls) _row.getF2Value();
				}
				int[] selectedRows = _table.getSelectedRows();
				for (int selectedRow : selectedRows) {
					FrameDifferenceElement diffEl = getDiffElementAt(selectedRow);
					if (selectedCls != null) {
						acceptorRejector.rejectChange(selectedCls, diffEl);
					} else {
						acceptorRejector.rejectChangeInInstance(selectedInstance.getDirectType(), selectedInstance, diffEl);
					}
				}
				((DiffTabComponent) PromptTab.getTabComponent()).reset();
			}

			@Override
			public void onSelectionChange() {
				int selectedRow = _table.getSelectedRow();

				if (selectedRow == -1) {
					setAllowed(false);
					return;
				}

				FrameDifferenceElement diffEl = getDiffElementAt(selectedRow);
				Assert.assertNotNull("Diff Element should not be NULL", diffEl);
				boolean allowed = diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT;
				setAllowed(allowed);
			}
		};
		return action;
	}

	public class SlotColumnRenderer extends FrameInMergingRenderer {

		FrameInMergingRenderer frameInMergingRenderer = new FrameInMergingRenderer();

		@Override
		public void load(Object value) {

			if (value == null) {
				return;
			}

			if (!(value instanceof FrameDifferenceElement)) {
				super.load(value);
				return;
			}

			FrameDifferenceElement diff = (FrameDifferenceElement) value;
			Slot slot = diff.getSlotValue();
			boolean loaded = false;

			String relationship = diff.getRelationshipToFrame();
			if (relationship == FrameDifferenceElement.OWN_SLOT || relationship == FrameDifferenceElement.OWN_SLOT_VALUE) {

				ClsWidget clsWidget = _treeView == null ? null : _treeView.getCurrentClsWidget();
				if (clsWidget != null) {
					SlotWidget slotWidget = clsWidget.getSlotWidget(slot);
					Slot slotMap = (Slot) Util.getMap(slot, _treeView.getDiffTable());
					if (slotWidget == null && slotMap != null) {
						slotWidget = clsWidget.getSlotWidget(slotMap);
					}

					if (slotWidget != null) {
						ensureIconFlagsLoaded();
						setMainIcon(slot.getIcon());
						setMainText(slotWidget.getLabel());
						loaded = true;
					}
				}
			}

			if (!loaded && slot != null) {
				super.load(slot);
			}
		}

	}
}
