/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 * 				Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.actionLists.ActionArgs;
import edu.stanford.smi.protegex.prompt.event.PromptEvent;
import edu.stanford.smi.protegex.prompt.event.PromptListener;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.operation.DeepCopyClsOperation;
import edu.stanford.smi.protegex.prompt.operation.DeepCopyFrameOperation;
import edu.stanford.smi.protegex.prompt.operation.KeepClsOperation;
import edu.stanford.smi.protegex.prompt.operation.KeepFrameOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeClsesOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeSlotsOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.SelectionListener;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;

public class ActionListPane extends JPanel {
	private Collection _actionList;

	private JTable _table;

	static final private String PARAMETERS_PREFIX = "params = {";
	static final private String PARAMETER_SUFFIX = "}";
	static final private String DEEP_PARAMETER = "deep";
	static final private String WITH_SUBCLASSES_PARAMETER = "subs";
	static final private String WITH_INSTANCES_PARAMETER = "inst";

	private int[] _columnSizes_merging = new int[] { 50, 230, 230, 150 };

	private String[] _columnNames_merging = new String[] { "Name", "Arg1", "Arg2", "Params" };

	private int[] _columnSizes_extracting = new int[] { 80, 400, 180 };

	private String[] _columnNames_extracting = new String[] { "Name", "Arg", "Params" };

	private int[] _columnSizes;

	private String[] _columnNames;

	// the previously selected suggestion row
	private int previouslySelectedRow = -1;

	// flag to determine if the UI is being drawn for the first time
	private boolean firstShown = true;

	private String searchText = "";

	/** Used to store selection listeners for this action pane, these are added by UI plugins */
	private LinkedList<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();

	ActionListPane(Collection actionList, javax.swing.Action action) {
		if (PromptTab.merging() || PromptTab.mapping()) {
			initialize(actionList, _columnSizes_merging, _columnNames_merging, action);
		} else {
			initialize(actionList, _columnSizes_extracting, _columnNames_extracting, action);
		}
	}

	ActionListPane(Collection actionList, int[] columnSizes, String[] columnNames, javax.swing.Action action) {
		initialize(actionList, columnSizes, columnNames, action);
	}

	private void initialize(Collection actionList, int[] columnSizes, String[] columnNames, javax.swing.Action action) {
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				// if this panel is being displayed for the first time, select
				// the first suggestion
				if (firstShown) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (_actionList.size() != 0) {
								_table.setRowSelectionInterval(0, 0);
							}
						}
					});
					firstShown = false;
				}
			}
		});
		
		_actionList = actionList;
		_columnNames = columnNames;
		createTable(columnSizes, columnNames, action);
		setLayout(new BorderLayout());
		add(ComponentFactory.createScrollPane(_table), BorderLayout.CENTER);
	}

	private void createTable(int[] columnSizes, String[] columnNames, javax.swing.Action action) {
		_table = ComponentFactory.createTable(action);
		_table.setModel(createTableModel(columnNames));

		_table.getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTableHeader header = _table.getTableHeader();
				int column = header.columnAtPoint(e.getPoint());

				((ActionTableModel) _table.getModel()).setSortIndex(column);
				_table.setColumnSelectionInterval(0, _columnNames.length-1);
			}
		});

		_table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				_table.setColumnSelectionInterval(0, _columnNames.length-1);
			}
		});

		if (columnSizes.length >= 1) {
			addColumn(columnSizes[0], new ActionNameRenderer());
		}
		for (int i = 0; i < columnSizes.length - 2; i++) {
			addColumn(columnSizes[i + 1], new ActionArgsRenderer(i));
		}

		if (columnSizes.length >= 2) {
			addColumn(columnSizes[columnSizes.length - 1], new ActionParametersRenderer());
		}
		_table.setColumnSelectionAllowed(true);
		_table.setColumnSelectionInterval(0, columnNames.length - 1);
	}

	private void addColumn(int width, TableCellRenderer renderer) {
		if (_table.getColumnCount() < _columnNames.length) {
			_table.addColumn(new TableColumn(_table.getColumnCount(), width, renderer, null));
		}
	}

	private TableModel createTableModel() {
		return createTableModel(_columnNames);
	}

	private TableModel createTableModel(String[] columnNames) {
		return new ActionTableModel(columnNames);
			}

	public void addActionListSelectionListener(ListSelectionListener listener) {
		_table.getSelectionModel().addListSelectionListener(listener);

		final ListSelectionListener listenerToCall = listener;

		// call the selection listener event after everything is drawn
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (_table.getSelectionModel() != null && _actionList.size() > 0) {
					listenerToCall.valueChanged(null);
				}
			}
		});
	}

	public Collection getSelection() {
		return ComponentUtilities.getSelection(_table);
	}

	public int getSelectedIndex() {
		return _table.getSelectedRow();
	}

	public int getNumberOfRows() {
		return _table.getRowCount();
	}

	public Action getFirstSelection() {
		Collection selection = ComponentUtilities.getSelection(_table);
		if (selection == null || selection.size() == 0) {
			return null;
		} else {
			return (Action) CollectionUtilities.getFirstItem(selection);
		}
	}

	public void setSelectedRow(int index) {
		_table.getSelectionModel().setSelectionInterval(index, index);
	}

	/**
	 * Allows this list pane to have a selection listener extension point for plugins.
	 * 
	 * @param selectionListener
	 */
	public void addSelectionListener(edu.stanford.smi.protegex.prompt.plugin.SelectionListener selectionListener) {
		selectionListeners.add(selectionListener);
	}

	public void addSelectionListener() {
		_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int index = _table.getSelectedRow();
				if (index != -1) {
					final Action a = getFirstSelection();
					Object[] args = a.getArgs().toArray();

					if (selectionListeners.size() == 0) {
						PromptTab.getTabComponent().selectArgumentsInTrees(args);
					}

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// call selection performed for this event
							PluginManager.getInstance().fireSelectionPerformed(selectionListeners, a);
						}
					});

					previouslySelectedRow = index;
				}
			}
		});
	}

	public Collection getActionList() {
		return _actionList;
	}

	public void filterOperations(Collection itemsToFilter, String searchText) {
		this.searchText = searchText;
		if (itemsToFilter != null) {
			String[] searchTextElements = searchText.toLowerCase().split(" ");
			List<Operation> matchingOperations = new ArrayList<Operation>();
			for (String search : searchTextElements) {
				if(search.startsWith("*")) {
					search = search.substring(1);
				}
				for (Object o : itemsToFilter) {
					Operation operation = (Operation) o;
					for (int i = 0; i < operation.getArgs().size(); i++) {
						Frame frame = (Frame) operation.getArgs().getArg(i);
						if (frame.getBrowserText().toLowerCase().contains(search)) {
							matchingOperations.add(operation);
							break;
						}
					}
				}
			}
			changeList(matchingOperations, false);
		}
	}

	private void changeList(Collection actionList, boolean applyFilter) {
		_actionList = actionList;
		if (applyFilter) {
			filterOperations(_actionList, searchText);
			return;
		}

		ActionTableModel tableModel = (ActionTableModel) _table.getModel();
		int sortColumn = tableModel.getSortColumn();
		boolean ascending = false;
		if(sortColumn >= 0) ascending = tableModel.ascending[sortColumn];
		
		TableModel model = createTableModel();
		_table.getSelectionModel().clearSelection();
		_table.setModel(model);
		if (model.getRowCount() > 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// call selection performed for this event
					_table.getSelectionModel().setSelectionInterval(0, 0);
				}
			});
		}
		
		if(sortColumn >= 0) {
			((ActionTableModel) _table.getModel()).ascending[sortColumn] = !ascending;
			((ActionTableModel) _table.getModel()).setSortIndex(sortColumn);
		}
		
		_table.setColumnSelectionInterval(0, _columnNames.length - 1);
	}

	public void changeList(Collection actionList) {
		changeList(actionList, true);
	}

	public JTable getSuggestionTable() {
		return _table;
	}

	public String toString() {
		return "ActionListPane";
	}

	// Renderers for column elements
	private class ActionNameRenderer extends ActionRenderer {
		public void load(Object o) {
			if (!(o instanceof Action)) {
				return;
			}
			Action action = (Action) o;
			setMainString(action.getShortName());
			if (action.getFromUser()) {
				Font boldFont = new Font(getFont().getName(), Font.BOLD, getFont().getSize());
				setTextFont(boldFont);
			} else {
				setTextFont(getFont());
			}
		}
	}

	private class ActionArgsRenderer extends ActionRenderer {
		int _index;

		ActionArgsRenderer(int i) {
			super();
			_index = i;
		}

		public void load(Object o) {
			if (!(o instanceof Action)) {
				return;
			}
			Action action = (Action) o;
			ActionArgs args = action.getShortArgs();
			if (args != null && args.size() > _index) {
				setArgs(new Object[] { args.getArg(_index) });
			} else {
				setArgs(null);
			}
		}
	}

	private class ActionParametersRenderer extends ActionRenderer {
		public void load(Object o) {
			if (!(o instanceof Action)) {
				return;
			}
			Action action = (Action) o;
			boolean deep = isDeep(action);
			boolean shallow = isShallow(action);
			boolean withSubclasses = withSubclasses(action);
			boolean withInstances = withInstances(action);
			setMainString(generateString(deep, shallow, withSubclasses, withInstances));
		}

		private String generateString(boolean deep, boolean shallow, boolean withSubclasses, boolean withInstances) {
			if (!deep && !withSubclasses && !withInstances) {
				return "";
			}
			String result = PARAMETERS_PREFIX;
			if (deep) {
				result += DEEP_PARAMETER;
			}
			if (withSubclasses) {
				if (result == PARAMETERS_PREFIX) {
					result += WITH_SUBCLASSES_PARAMETER;
				} else {
					result += ", " + WITH_SUBCLASSES_PARAMETER;
				}
			}
			if (withInstances) {
				if (result == PARAMETERS_PREFIX) {
					result += WITH_INSTANCES_PARAMETER;
				} else {
					result += ", " + WITH_INSTANCES_PARAMETER;
				}
			}
			return result + PARAMETER_SUFFIX;
		}

		private boolean isDeep(Action action) {
			return action instanceof DeepCopyFrameOperation || (action instanceof MergeFramesOperation && ((MergeFramesOperation) action).deepCopy());
		}

		private boolean isShallow(Action action) {
			if (action instanceof KeepFrameOperation || action instanceof MergeFramesOperation) {
				return true;
			}
			return false;
		}

		private boolean withSubclasses(Action action) {
			return (action instanceof DeepCopyClsOperation || action instanceof KeepClsOperation || action instanceof MergeClsesOperation) && ((Operation) action).copySubclasses();
		}

		private boolean withInstances(Action action) {
			return (action instanceof DeepCopyClsOperation || action instanceof KeepClsOperation || action instanceof MergeClsesOperation) && ((Operation) action).copyInstances();
		}
	}

	/**
	 * Table model for the action items. This supports sorting for each column.
	 * 
	 * @author seanf
	 */
	class ActionTableModel extends DefaultTableModel {
		private int sortColumn = -1;
		private boolean[] ascending;
		private String[] columnNames;

		public ActionTableModel(String[] columnNames) {
			this.columnNames = columnNames;
			for (int c = 0; c < columnNames.length; c++) {
				addColumn(columnNames[c]);
			}
			ascending = new boolean[columnNames.length];
		}
		
		public int getSortColumn() {
			return sortColumn;
		}

		public void setSortIndex(int column) {
			if(column >= 0 && column < columnNames.length) {
				int prevColumn = sortColumn;
				sortColumn = column;
	
				if (prevColumn >= 0) {
					_table.getColumnModel().getColumn(prevColumn).setHeaderValue(getColumnName(prevColumn));
				}
	
				ascending[sortColumn] = !ascending[sortColumn];
	
				Collections.sort((List) _actionList, new Comparator() {
					public int compare(Object o1, Object o2) {
						if (o1 instanceof Action && o2 instanceof Action) {
							Action action1 = (Action) o1;
							Action action2 = (Action) o2;
	
							if (ascending[sortColumn]) {
								return compareAction(action1, action2);
							} else {
								return compareAction(action2, action1);
							}
	
						}
						return -1;
					}
				});
	
				_table.getColumnModel().getColumn(sortColumn).setHeaderValue(getColumnName(sortColumn));
	
				fireTableDataChanged();
			}
		}

		public String getColumnName(int column) {
			if (sortColumn == column) {
				return columnNames[column] + " " + ((ascending[column]) ? "(asc)" : "(desc)");
			}
			return columnNames[column];
		}

		public int getRowCount() {
			return ((List) _actionList).size();
		}

		public Object getValueAt(int row, int column) {
			Action action = (Action) ((List) _actionList).get(row);
			return action;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		private int compareAction(Action action1, Action action2) {
			if (sortColumn == 0) {
				return action1.getShortName().compareToIgnoreCase(action2.getShortName());
			} else if (sortColumn == 1) {
				Frame frame1 = (Frame)action1.getArgs().getArg(0);
				Frame frame2 = (Frame)action2.getArgs().getArg(0);
				return frame1.getBrowserText().compareToIgnoreCase(frame2.getBrowserText());
			} else if (sortColumn == 2) {
				Frame frame1 = (Frame)action1.getArgs().getArg(1);
				Frame frame2 = (Frame)action2.getArgs().getArg(1);
				return frame1.getBrowserText().compareToIgnoreCase(frame2.getBrowserText());
			}
			return -1;
		}
	}
}
