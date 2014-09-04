package gov.nih.nci.protegex.edit;


import edu.stanford.smi.protege.util.PopupMenuMouseListener;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIRoleGroupTable extends JTable {
	
	public static final long serialVersionUID = 923456027L;

    private NCIRoleGroupTableModel tableModel;
    private JTextField textField;
    OWLModel kb;

    public NCIRoleGroupTable(OWLModel kb, NCIRoleGroupTableModel model) {

        super(model);
        this.tableModel = model;
        this.kb = kb;

        model.setTable(this);

        textField = new JTextField();
        OWLUI.addCopyPastePopup(textField);
        setDefaultEditor(Object.class, new DefaultCellEditor(textField));
        TableColumn groupColumn = getColumnModel().getColumn(0);
        TableColumn roleColumn = getColumnModel().getColumn(1);

        groupColumn.setPreferredWidth(100);
        roleColumn.setPreferredWidth(500);

        

        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        getTableHeader().setReorderingAllowed(false);
        setRowMargin(0);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addMouseListener(new PopupMenuMouseListener(this) {

            protected JPopupMenu getPopupMenu() {
                return null;
            }

            protected void setSelection(JComponent c, int x, int y) {
                int row = y / getRowHeight();
                if (row >= 0 && row < getRowCount()) {
                    getSelectionModel().setSelectionInterval(row, row);
                }
            }
        });


        setRowHeight(new JTextField().getPreferredSize().height);
        setGridColor(Color.LIGHT_GRAY);
        setShowGrid(true);
        setIntercellSpacing(new Dimension(1, 1));

    }


	

    public NCIRoleGroupTableModel getTableModel() {
        return tableModel;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
    	//only the group number is editable in this table
        if (columnIndex == 0)
        	return true;

		return false;
	}

}
