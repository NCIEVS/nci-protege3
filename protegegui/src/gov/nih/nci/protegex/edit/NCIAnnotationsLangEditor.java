package gov.nih.nci.protegex.edit;

import edu.stanford.smi.protegex.owl.ui.components.annotations.*;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.components.ComponentUtil;
import gov.nih.nci.protegex.panel.EditPanel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIAnnotationsLangEditor extends AbstractCellEditor implements
		TableCellEditor {

	private static final long serialVersionUID = 441936038070346942L;

	private AnnotationsTableCellHolder langHolder;

	private JComboBox comboBox;

	EditPanel edit_panel = null;

	public NCIAnnotationsLangEditor(OWLModel model, EditPanel edit_panel,
			JTable table) {
		comboBox = ComponentUtil.createLangCellEditor(model, table);
		JPanel holderPanel = new JPanel(new BorderLayout());
		holderPanel.add(comboBox, BorderLayout.NORTH);
		holderPanel.setOpaque(false);
		holderPanel.setBorder(UIManager
				.getBorder("Table.focusCellHighlightBorder"));
		langHolder = new AnnotationsTableCellHolder(holderPanel,
				BorderLayout.CENTER);
		langHolder.setOpaque(false);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
			}
		});
		this.edit_panel = edit_panel;
	}

	public Object getCellEditorValue() {
		if (comboBox.getSelectedItem() != null) {
			return comboBox.getSelectedItem().toString();
		} else {
			return null;
		}
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		comboBox.setSelectedItem(value);
		edit_panel.enableSaveButton(true);
		return langHolder;
	}
}
