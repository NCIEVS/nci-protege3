package gov.nih.nci.protegex.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.ui.ResourceRenderer;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIAnnotationsTable extends JTable {
	
	private static final long serialVersionUID = 441936038070346944L;

    private static HashSet<String> multiLineProperties = new HashSet<String>();

    static {
        multiLineProperties.add(RDFSNames.Slot.COMMENT);
        multiLineProperties.add("rdfs:comment");
    }

    private NCIAnnotationsTableModel tableModel;
    private JTextField textField;
    private JComboBox typeComboBox;

    TableColumn valueColumn;

    public NCIAnnotationsTable(NCIAnnotationsTableModel model,
                  final String partialActionName) {

        super(model);
        this.tableModel = model;

        model.setTable(this);

        textField = new JTextField();
        OWLUI.addCopyPastePopup(textField);
        setDefaultEditor(Object.class, new DefaultCellEditor(textField));
        valueColumn = getColumnModel().getColumn(tableModel.getColumnCount() - 2);
        TableColumn languageColumn = getColumnModel().getColumn(tableModel.getColumnCount() - 1);

        NCIAnnotationsValueRenderer renderer = new NCIAnnotationsValueRenderer(model);

        valueColumn.setCellRenderer(renderer);
        valueColumn.setPreferredWidth(700);

        if (model.getHasPropertyColumn()){
        	TableColumn propertyColumn = getColumnModel().getColumn(NCIAnnotationsTableModel.COL_PROPERTY);
        	propertyColumn.setCellRenderer(renderer);
        	propertyColumn.setPreferredWidth(200);
		}

        languageColumn.setCellRenderer(renderer);
        
        if (tableModel.hasTypeColumn()) {
            TableColumn typeColumn = getColumnModel().getColumn(tableModel.getColumnCount() - 2);
            typeColumn.setCellRenderer(new ResourceRenderer());
            Vector datatypes = new Vector();
            typeComboBox = new JComboBox(datatypes);
            typeComboBox.setRenderer(new ResourceRenderer());
            typeColumn.setCellEditor(new DefaultCellEditor(typeComboBox));
        }

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


    public void setMultilineEditor()
    {
		int col_num = tableModel.getColumnCount() - 2;
        valueColumn.setCellEditor(new NCIAnnotationsValueEditor(tableModel.getOWLModel(), this, col_num));
    }


    public NCIAnnotationsTableModel getTableModel() {
        return tableModel;
    }


    public static Collection getMultiLineProperties() {
        return Collections.unmodifiableCollection(multiLineProperties);
    }


    public static boolean isMultiLineProperty(RDFProperty property) {
        return multiLineProperties.contains(property.getName());
    }

    public static boolean isMultiLineProperty(String propertyName) {
        return multiLineProperties.contains(propertyName);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
		return false;
	}

}
