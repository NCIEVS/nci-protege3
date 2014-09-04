package gov.nih.nci.protegex.edit;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIFULLSYNTable extends JTable {
	public static final long serialVersionUID = 123456000L;

	private NCIFULLSYNTableModel tableModel;

	public NCIFULLSYNTable(Project project, NCIFULLSYNTableModel model,
			final String partialActionName) {

		super(model);
		this.tableModel = model;

		model.setTable(this);

		JTextField textField = new JTextField();
		OWLUI.addCopyPastePopup(textField);
		setDefaultEditor(Object.class, new DefaultCellEditor(textField));

		TableColumn termnameColumn = getColumnModel().getColumn(
				NCIFULLSYNTableModel.TERM_NAME);
		TableColumn termgroupColumn = getColumnModel().getColumn(
				NCIFULLSYNTableModel.TERM_GROUP);
		TableColumn termsourceColumn = getColumnModel().getColumn(
				NCIFULLSYNTableModel.TERM_SOURCE);
		TableColumn sourcecodeColumn = getColumnModel().getColumn(
				NCIFULLSYNTableModel.TERM_SOURCE_CODE);
		TableColumn langColumn = getColumnModel().getColumn(
				NCIFULLSYNTableModel.LANGUAGE);

		NCIFULLSYNValueRenderer renderer = new NCIFULLSYNValueRenderer();

		termnameColumn.setCellRenderer(renderer);
		termnameColumn.setPreferredWidth(300);

		termgroupColumn.setPreferredWidth(60);
		termgroupColumn.setCellRenderer(renderer);

		termsourceColumn.setCellRenderer(renderer);
		termsourceColumn.setPreferredWidth(60);

		sourcecodeColumn.setCellRenderer(renderer);
		sourcecodeColumn.setPreferredWidth(80);

		langColumn.setCellRenderer(renderer);

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

	public NCIFULLSYNTableModel getTableModel() {
		return tableModel;
	}
}
