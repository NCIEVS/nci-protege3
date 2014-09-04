package gov.nih.nci.protegex.edit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

import edu.stanford.smi.protegex.owl.ui.components.annotations.AnnotationsTableCellHolder;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIFULLSYNValueRenderer implements TableCellRenderer {

    private JTextArea textArea;

    private JLabel label;

    private JLabel langLabel;

    public static final int EXTRA_SPACING = 4;

    

    //private AnnotationsTableCellHolder plainTextPropertyValHolder;

    private AnnotationsTableCellHolder langHolder;

	private AnnotationsTableCellHolder multiLineHolder;
	private JTextComponent textComponent;
	private JTextArea textArea_group, textArea_src, textArea_code;
	private AnnotationsTableCellHolder plainTextPropertyValHolder_group;
	private AnnotationsTableCellHolder plainTextPropertyValHolder_src;
	private AnnotationsTableCellHolder plainTextPropertyValHolder_code;

    public NCIFULLSYNValueRenderer() {
        super();
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFocusable(true);
        textArea.setOpaque(false);
        //plainTextPropertyValHolder = new AnnotationsTableCellHolder(textArea, BorderLayout.CENTER);
        label = new JLabel();
        label.setOpaque(false);
	    if(System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
		    // Adjust border on label due to some silly windows 'feature'
		    label.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
	    }
	    
        langLabel = new JLabel();
        langHolder = new AnnotationsTableCellHolder(langLabel, BorderLayout.NORTH);

		multiLineHolder = new AnnotationsTableCellHolder(textArea, BorderLayout.CENTER);

        textArea_group = new JTextArea();
        textArea_group.setWrapStyleWord(true);
        textArea_group.setLineWrap(true);
        textArea_group.setFocusable(true);
        textArea_group.setOpaque(false);
        plainTextPropertyValHolder_group = new AnnotationsTableCellHolder(textArea_group, BorderLayout.CENTER);

        textArea_src = new JTextArea();
        textArea_src.setWrapStyleWord(true);
        textArea_src.setLineWrap(true);
        textArea_src.setFocusable(true);
        textArea_src.setOpaque(false);
        plainTextPropertyValHolder_src = new AnnotationsTableCellHolder(textArea_src, BorderLayout.CENTER);

        textArea_code = new JTextArea();
        textArea_code.setWrapStyleWord(true);
        textArea_code.setLineWrap(true);
        textArea_code.setFocusable(true);
        textArea_code.setOpaque(false);
        plainTextPropertyValHolder_code = new AnnotationsTableCellHolder(textArea_code, BorderLayout.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object o,
                                                   boolean selected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col) {
        // Ensure that the row is the correct height.  We want to adjust the
	    // row height if it hasn't been adjusted by the cell editor, so check
	    // to ensure that the value of the property isn't being edited.
        if ((table.getEditingRow() == row && table.getEditingColumn() == NCIFULLSYNTableModel.TERM_NAME) == false) {
            int rowHeight = getRowHeight(table, row);
            if (table.getRowHeight(row) != rowHeight) {
                table.setRowHeight(row, rowHeight);
            }
        }

        if (col == NCIFULLSYNTableModel.LANGUAGE) {
            langLabel.setText(o != null ? o.toString() : "");
            langHolder.setColors(selected, hasFocus);
            return langHolder;
		}else if (col == NCIFULLSYNTableModel.TERM_NAME) {
			return getValueCellHolder(table, o, selected, hasFocus, row, col);
		}else if (col == NCIFULLSYNTableModel.TERM_GROUP) {
			textArea_group.setText(o != null ? o.toString() : "");
			plainTextPropertyValHolder_group.setColors(selected, hasFocus);
			return plainTextPropertyValHolder_group;
		}else if (col == NCIFULLSYNTableModel.TERM_SOURCE) {
			textArea_src.setText(o != null ? o.toString() : "");
			plainTextPropertyValHolder_src.setColors(selected, hasFocus);
			return plainTextPropertyValHolder_src;
		}//else if (col == NCIFULLSYNTableModel.TERM_SOURCE_CODE) {

		textArea_code.setText(o != null ? o.toString() : "");
		plainTextPropertyValHolder_code.setColors(selected, hasFocus);
		return plainTextPropertyValHolder_code;
		//}

		/*textArea.setText(o != null ? o.toString() : "");
		plainTextPropertyValHolder.setColors(selected, hasFocus);
		return plainTextPropertyValHolder;*/
    }

    private AnnotationsTableCellHolder getValueCellHolder(JTable table,
                                                   Object o,
                                                   boolean selected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col)
	{
		textArea.setText(o != null ? o.toString() : "");
		int rowHeight = getRowHeight(table, row);
		if (table.getRowHeight(row) != rowHeight) {
			table.setRowHeight(row, rowHeight);
		}
		textComponent = textArea;
		focusTextField();
		multiLineHolder.setColors(selected, hasFocus);
		return multiLineHolder;
	}


	private void focusTextField() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textComponent.requestFocus();
			}
		});
	}

    


    private int getRowHeight(JTable table, int row) {
		Object val = table.getValueAt(row, 0);
		if (val instanceof String) {
			String text = val.toString();
			textArea.setText(text);

			View v = textArea.getUI().getRootView(textArea);
			v.setSize(table.getColumnModel().getColumn(0).getWidth(), Integer.MAX_VALUE);
			int height = (int) v.getPreferredSpan(View.Y_AXIS) + 4;
			if (height < table.getRowHeight()) {
				height = table.getRowHeight();
			}
			return height;
		}
		return table.getRowHeight();
    }


}

