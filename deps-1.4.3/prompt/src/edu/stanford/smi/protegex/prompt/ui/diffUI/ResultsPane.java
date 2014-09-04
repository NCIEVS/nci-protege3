 /*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu,
 *                 Michel Klein michel.klein@cs.vu.nl
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRowComparator;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow.Column;
import edu.stanford.smi.protegex.prompt.util.Util;

public class ResultsPane extends SelectableContainer {
    private static final long serialVersionUID = -1772898468838117964L;

    private ResultTable diffTable;
	private DiffTablePanel _diffTablePanel;
    private List<TableRow> _results;
    private JTable _table = ComponentFactory.createTable(null);
    private JSplitPane _contentPane;


    public ResultsPane(ResultTable diffTable, List<TableRow> results) {
    	_results = results;
    	this.diffTable = diffTable;

        _table.setModel (createTableModel ());

        for (Column col : Column.values()) {
            ComponentUtilities.addColumn(_table, col.createRenderer());
        }
        
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setLayout (new BorderLayout ());
        _contentPane = createContentPane();
        add (_contentPane, BorderLayout.CENTER);
    }

	public void reset() {
		_table.setModel (createTableModel ());
		_diffTablePanel.reload();
	}
	
    @SuppressWarnings("unchecked")
    public Collection getSelection () {
      int[] indices = _table.getSelectedRows();
      Collection selection = new ArrayList();
      if (indices == null) return selection;
      TableModel model = _table.getModel();
      for (int i = 0; i < indices.length; i++) {
          selection.add(model.getValueAt(indices[i], Column.values().length));
      }
      return selection;
    }
    
    public void setSelectionFromFrames(Collection<Frame> frames) {
        ListSelectionModel selectionModel = _table.getSelectionModel();
        TableModel  model = _table.getModel();
        selectionModel.clearSelection();
        for (int index = 0; index < _table.getRowCount(); index++) {
            TableRow trow = (TableRow) model.getValueAt(index, Column.values().length);
            Frame f2 = trow.getF2Value();
            if (f2 != null) {
                if (frames.contains(f2)) {
                    selectionModel.addSelectionInterval(index, index);
                }
            }
            else if (frames.contains(trow.getF1Value())) {
                selectionModel.addSelectionInterval(index, index);
            }
        }
    }

    private JSplitPane createContentPane() {
      JSplitPane result = ComponentFactory.createTopBottomSplitPane();
      result.setTopComponent(ComponentFactory.createScrollPane(_table));
	  _diffTablePanel = new DiffTablePanel (null, null, diffTable);
      result.setBottomComponent(_diffTablePanel);
      addMySelectionListener();

      return result;
    }


    private void addMySelectionListener () {
        _table.getSelectionModel().addListSelectionListener(new ListSelectionListener () {
            public void valueChanged(ListSelectionEvent e) {
                int index = _table.getSelectedRow();
                if (index != -1) {
                    TableRow row = getFirstSelection();
                    if (row != null) {
                        PromptTab.getTabComponent().selectArgumentsInTrees (row.createArrayFromEntries());
                        _diffTablePanel.setRow (row, false);
                    }
                }
                notifySelectionListeners();
            }

        });
    }

    private TableModel createTableModel () {
        DefaultTableModel model = new DefaultTableModel() {
            private static final long serialVersionUID = 3318639368823181643L;

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Column col : Column.values()) {
        	String columnName = null;
        	if (col == Column.F1) {
        		columnName = diffTable.getKb1().getName();
        	}
        	else if (col == Column.F2  && diffTable.getKb2() instanceof OWLModel) {
        		columnName = diffTable.getKb2().getName();
        	}
        	if (columnName == null) {
        		columnName = col.getName();
        	}
            model.addColumn(columnName);
        }
        //add an extra invisible column to store the whole TableRow object
        model.addColumn ("no name");
        Iterator<TableRow> i = _results.iterator();
        for (TableRow row : _results) {
            model.addRow (createRow(row));
        }
        return model;

    }
    
    private Object[] createRow(TableRow tableRow) {
        Object [] row = new Object[Column.values().length + 1];
        row[Column.F1.ordinal()] = tableRow.getF1Value();
        row[Column.F2.ordinal()] = tableRow.getF2Value();
        row[Column.RENAMED.ordinal()] = tableRow.getRenameValue();
        row[Column.OPERATION.ordinal()] = tableRow.getOperationValue();
        row[Column.MAP_LEVEL.ordinal()] = (tableRow.getOperationValue() == TableRow.OPERATION_MAP) ? tableRow.getMappingLevel() : "";
        row[Column.RENAME_EXPLANATION.ordinal()] = tableRow.getRenameExplanation();
        row[Column.values().length] = tableRow;
        return row;
    }
    
    public void reshape (int x, int y, int w, int h) {
      super.reshape(x, y, w, h);
      _contentPane.setDividerLocation ((int)(3*h/4));
    }

    public TableRow getFirstSelection () {
        TableModel model = _table.getModel();
        int[] indices = _table.getSelectedRows();
        Object selection = model.getValueAt(indices[0], TableRow.numberOfColumns ());
        if (selection != null)
        	return (TableRow)selection;
        else
        	return null;
    }

    public static class MappingLevelRenderer extends DefaultRenderer {
        public void load(Object value) {
            if (value.equals(TableRow.MAPPING_LEVEL_STRONG_ISOMORPHIC) ||
                    value.equals(TableRow.MAPPING_LEVEL_WEAK_ISOMORPHIC))
                super.load (TableRow.MAPPING_LEVEL_ISOMORPHIC);
            else
                super.load(value);
        }
    }
}
