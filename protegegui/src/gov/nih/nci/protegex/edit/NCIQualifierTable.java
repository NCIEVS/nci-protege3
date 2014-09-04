package gov.nih.nci.protegex.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashSet;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import edu.stanford.smi.protegex.owl.model.RDFSNames;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIQualifierTable extends JTable {
	
	private static final long serialVersionUID = 441936038070346948L;

    private static HashSet<String> multiLineProperties = new HashSet<String>();

    static {
        multiLineProperties.add(RDFSNames.Slot.COMMENT);
    }

    private NCIQualifierTableModel tableModel;
    //private JTextField textField;

    public NCIQualifierTable(NCIQualifierTableModel model) {

        super(model);
        this.tableModel = model;

        model.setTable(this);

        TableColumn nameColumn = getColumnModel().getColumn(NCIQualifierTableModel.COL_NAME);
        TableColumn valueColumn = getColumnModel().getColumn(NCIQualifierTableModel.COL_VALUE);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

        valueColumn.setCellRenderer(renderer);
        valueColumn.setPreferredWidth(700);

        nameColumn.setCellRenderer(renderer);
        nameColumn.setPreferredWidth(200);

        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        getTableHeader().setReorderingAllowed(false);
        setRowMargin(0);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setRowHeight(new JTextField().getPreferredSize().height);
        setGridColor(Color.LIGHT_GRAY);
        setShowGrid(true);
        setIntercellSpacing(new Dimension(1, 1));

    }


    public NCIQualifierTableModel getTableModel() {
        return tableModel;
    }


}
