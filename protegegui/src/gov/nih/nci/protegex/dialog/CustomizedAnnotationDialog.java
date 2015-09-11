package gov.nih.nci.protegex.dialog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protege.util.LabeledComponent;

import edu.stanford.smi.protegex.owl.model.OWLModel;

import edu.stanford.smi.protegex.owl.model.*;
import gov.nih.nci.protegex.edit.NCIAnnotationsTableModel;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIFULLSYNTableModel;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.util.StringUtil;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class CustomizedAnnotationDialog {

    private EditPanel edit_panel;

	private String newValue = null;

	public boolean cancelBtnPressed = false;

	private OWLWrapper wrapper = null;

	private OWLModel owlModel = null;

	private Dimension dimension = new Dimension(500, 300);

	private String title = null;

	private CustomizedAnnotationData cad = null;

	private CustomizedAnnotationDialog(EditPanel edit_panel) {
		this.edit_panel = edit_panel;
		cancelBtnPressed = false;
		this.newValue = null;

		wrapper = edit_panel.getOWLWrapper();
		owlModel = edit_panel.getOWLModel();
	}

	public CustomizedAnnotationDialog(EditPanel edit_panel,
			String annotation_name, Object annotation_value, int row,
			AbstractTableModel model) {

		this(edit_panel);

		String label = "";
		JPanel panel = null;
		Component comp = null;
		LabeledComponent lc = null;

		boolean isLiteral = false;
		String language = null;

		String anno_value = "";

		if (annotation_value instanceof RDFSLiteral) {
			RDFSLiteral value = (RDFSLiteral) annotation_value;
			anno_value = value.getString();
			isLiteral = true;
			language = value.getLanguage();
		} else {
			anno_value = (String) annotation_value;
		}

		cad = this.getCustomizedAnnotationData(annotation_name);
		cad.setAnnotationName(annotation_name);

		boolean ok = false;
		// original value must be maintained for final update to database, 
		// needs before and after current_anno_value will hold the value
		// for each pass through the loop where the use might need to edit
		String current_anno_value = anno_value;
		
		CustomizedAnnotationConstructor dlg_constructor = 
		    new CustomizedAnnotationConstructor(edit_panel, cad);

		while (!ok) {

			panel = dlg_constructor.createPanel(current_anno_value);

			comp = new JScrollPane(panel);
			label = wrapper.getRDFProperty(annotation_name).getBrowserText();
			// label = wrapper.getRDFProperty(annotation_name).getLocalName();

			lc = new LabeledComponent(label, comp);
			lc.setPreferredSize(dimension);

			// String ext_annotation_name = edit_panel.getNCIEditTab()
			// .getExternalName(annotation_name);
			title = "Edit " + annotation_name + " Annotation Property";

			if (anno_value == null || anno_value.compareTo("") == 0) {
				title = "Create " + annotation_name + " Annotation Property";
			}

			int r = ProtegeUI.getModalDialogFactory().showDialog(edit_panel,
					lc, title, ModalDialogFactory.MODE_OK_CANCEL, dlg_constructor.getFocus());
			if (r == ModalDialogFactory.OPTION_OK) {

				String def_field_value = StringUtil.cleanString(
				    dlg_constructor.getValue(0), false);
				boolean fieldBlank = false;

				if (def_field_value.compareTo("") == 0) {
					fieldBlank = true;
					Component warning_label = new JLabel(
							"Property value is mandatory.");
					LabeledComponent lc2 = new LabeledComponent("",
							warning_label);
					ProtegeUI.getModalDialogFactory().showDialog(edit_panel,
							lc2, "WARNING", ModalDialogFactory.MODE_CLOSE);
				}

				if (fieldBlank) {

					current_anno_value = dlg_constructor.getValue();
					

					// return;
				} else {
					ok = true;
				}

				if (ok) {

					newValue = dlg_constructor.getValue();


					if (row >= 0) {
						if (isLiteral) {
							RDFProperty property = (RDFProperty) owlModel
									.getRDFProperty(annotation_name);
							Object new_value = wrapper.createNewValue(property,
									newValue, language);

							if (model instanceof NCIAnnotationsTableModel) {

								((NCIAnnotationsTableModel) model).resetValue(
										annotation_name, annotation_value,
										(Object) new_value);
							} else {
								((NCIFULLSYNTableModel) model).resetValue(
										annotation_name, annotation_value,
										(Object) new_value);

							}
						} else {
							if (model instanceof NCIAnnotationsTableModel) {

								((NCIAnnotationsTableModel) model).resetValue(
										annotation_name, (Object) anno_value,
										(Object) newValue);
							} else {
								((NCIFULLSYNTableModel) model).resetValue(
										annotation_name, (Object) anno_value,
										(Object) newValue);

							}

						}
						model.fireTableDataChanged();
						edit_panel.enableSaveButton(true, false);
					}

				}
			} else {
				newValue = anno_value;
				cancelBtnPressed = true;
				//cad.setDefaultValuesData(default_value_data);
				ok = true;
			}
		}

	}

	public CustomizedAnnotationDialog(EditPanel edit_panel,
			String annotation_name, Object annotation_value, int row,
			NCIFULLSYNTableModel model) {
		this(edit_panel, annotation_name, annotation_value, row,
				(AbstractTableModel) model);
	}

	public CustomizedAnnotationDialog(EditPanel edit_panel,
			String annotation_name, Object annotation_value, int row,
			NCIAnnotationsTableModel model) {
		this(edit_panel, annotation_name, annotation_value, row,
				(AbstractTableModel) model);
	}

	public String getValue() {
		return newValue;
	}

	private CustomizedAnnotationData getCustomizedAnnotationData(
			String annotation_name) {
		/**
		String ns = null;
		if (annotation_name.compareTo(NCIEditTab.DEFINITION) == 0
				|| annotation_name.compareTo(NCIEditTab.ALT_DEFINITION) == 0
				|| annotation_name.compareTo(NCIEditTab.LONG_DEFINITION) == 0
				|| annotation_name.compareTo(NCIEditTab.ALT_LONG_DEFINITION) == 0) {
			ns = NCIEditTab.DEFINITION;

		} else if (annotation_name.compareTo(NCIEditTab.GO_ANNOTATION) == 0) {

			ns = NCIEditTab.GO_ANNOTATION;

		} else if (annotation_name.compareTo(NCIEditTab.ALTLABEL) == 0) {

			ns = NCIEditTab.ALTLABEL;
		}
		**/

		
		return NCIEditTab.getCustomizedAnnotationData(annotation_name);
	}

}
