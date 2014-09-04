package gov.nih.nci.protegex.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.components.annotations.AnnotationsTableCellHolder;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIAnnotationsValueEditor extends AbstractCellEditor implements TableCellEditor {
	
	private static final long serialVersionUID = 441936038070346943L;

    private JTextArea textArea;

    private AnnotationsTableCellHolder multiLineHolder;

    private JTextField textField;

    private AnnotationsTableCellHolder singleLineHolder;

    private JTextComponent textComponent;

    private Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");

    public static final int EDITING_MARGIN = 30;

    int col_num = -1;

    NCIAnnotationsTableModel tableModel = null;

    public NCIAnnotationsValueEditor(OWLModel owlModel, NCIAnnotationsTable t, int col_num) {
        this.col_num = col_num;
        this.tableModel = (NCIAnnotationsTableModel) t.getTableModel();
        textArea = new JTextArea();
        textArea.setRows(2);
        OWLUI.addCopyPastePopup(textArea);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isControlDown()) {
                        stopCellEditing();
                        e.consume();
                    }
                }
            }
        });
        textArea.setFocusable(true);
        JScrollPane sp = new JScrollPane(textArea);
        sp.setBorder(focusBorder);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        multiLineHolder = new AnnotationsTableCellHolder(sp, BorderLayout.CENTER);

        textField = new JTextField();
        singleLineHolder = new AnnotationsTableCellHolder(textField, BorderLayout.CENTER);
    }


    public Object getCellEditorValue() {
        return textComponent.getText().trim();
    }


    public Component getTableCellEditorComponent(final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row,
                                                 final int column) {

        //RDFProperty property = (RDFProperty) table.getValueAt(row, NCIAnnotationsTableModel.COL_DEFINITION_VALUE);
        //if (AnnotationsTable.isMultiLineProperty(property)) {
        if (column == col_num)
        {
            textArea.setText(value != null ? value.toString() : "");
            int rowHeight = getRowHeight(table, row);
            if (table.getRowHeight(row) != rowHeight) {
                table.setRowHeight(row, rowHeight);
            }
            textComponent = textArea;
            focusTextField();
            return multiLineHolder;

		}
        else {
            textField.setText(value != null ? value.toString() : "");
            textComponent = textField;
            focusTextField();
            return singleLineHolder;
        }

    }


    private void focusTextField() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textComponent.requestFocus();
            }
        });
    }


	public boolean isCellEditable(EventObject e) {
		// Only edit cell if the user has double
		// clicked it.
		/*
		if(e instanceof MouseEvent) {
			return ((MouseEvent) e).getClickCount() == 2;
		}
		else {
			return super.isCellEditable(e);
		}
		*/
		return false;
	}

    private int getRowHeight(JTable table, int row)
    {
        Object val = table.getValueAt(row, col_num);
	    if(val == null) {
		    val = "";
	    }
        int preferredHeight = 0;
        View v = textArea.getUI().getRootView(textArea);
        v.setSize(table.getColumnModel().getColumn(col_num).getWidth(), Integer.MAX_VALUE);
        preferredHeight = (int) v.getPreferredSpan(View.Y_AXIS) + EDITING_MARGIN;

        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
        if (sp != null) {
            int maxHeight = sp.getViewport().getViewRect().height;
            if (preferredHeight > maxHeight) {
                preferredHeight = maxHeight;
            }
        }
        return preferredHeight;
    }


}

