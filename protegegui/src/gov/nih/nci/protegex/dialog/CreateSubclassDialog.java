package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.edit.NCIEditFilter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCISelectClsesPanel;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.SemanticTypeUtil;
import gov.nih.nci.protegex.util.StringUtil;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class CreateSubclassDialog extends JDialog {
	public static final long serialVersionUID = 223456792L;

	private String concept_name;

	private String preferred_name;

	private String superconcept_name;

	private String definition;

	private KnowledgeBase kb;

	private OWLModel owlModel;

	private NCIEditTab tab;

	private JTextField name_field;

	private JTextField pt_field;

	private JTextArea def_area;

	private JTextField superconcept_field;

	private OWLNamedClass selected_superclass = null;

	private boolean hasName = true;

	public CreateSubclassDialog(NCIEditTab tab) {
		super((JFrame) tab.getTopLevelAncestor(), "Create Subclass", true);
		this.tab = tab;
		this.kb = tab.getOWLModel();
		this.owlModel = (OWLModel) kb;

		this.concept_name = "";
		this.preferred_name = "";
		this.superconcept_name = "";
		// GF#7368
		OWLNamedClass default_parent = (OWLNamedClass) tab
				.getSelectedInstance();
		if (default_parent != null) {
		    if (tab.getOWLWrapper().isRetired(default_parent)) {
		        MsgDialog.warning(tab, "Cannot edit retired concept");
		        return;
		    }
			selected_superclass = default_parent;
			this.superconcept_name = selected_superclass.getBrowserText();
		}

		this.definition = "";
		//this.selected_superclass = null;

		if (tab.byCode()) {
			hasName = false;
		}

		while (true) {
			JPanel panel = createPanel();
			int reply = ProtegeUI.getModalDialogFactory()
					.showDialog(tab, panel, "Create Subclass",
							ModalDialogFactory.MODE_OK_CANCEL, name_field);
			if (reply != ModalDialogFactory.OPTION_OK)
				return;

			if (hasName) {
				concept_name = name_field.getText().trim();
				
			}
			preferred_name = pt_field.getText().trim();
			definition = StringUtil.cleanString(def_area.getText(), false);
			superconcept_name = superconcept_field.getText().trim();

			if (hasName)
				name_field.setText(concept_name);
			pt_field.setText(preferred_name);
			def_area.setText(definition);
			superconcept_field.setText(superconcept_name);

			if (hasName) {
				
				if (concept_name.compareTo("") == 0) {
					MsgDialog.warning(this, "Incomplete data entry",
							"Class name is mandatory.");
					continue;
				} else if (!NCIEditFilter
						.checkXMLNCNameCompliance(concept_name)) {
					MsgDialog.warning(this, "Data entry warning",
							"Class name format is incorrect.");
					continue;
				}
			}

			if (superconcept_name.compareTo("") == 0
					|| superconcept_name.compareTo("owl:Thing") == 0) {
				MsgDialog.warning(this, "Incomplete data entry",
						"Superclass is mandatory.");
				continue;
			}

			if (preferred_name.compareTo("") == 0) {
				MsgDialog.warning(this, "Incomplete data entry",
						"Preferred name is mandatory.");
				continue;
			}
			if (StringUtil.containsConsecutiveSpaces(preferred_name)) {
				MsgDialog.warning(this, "Data entry warning",
						"No consecutive spaces in preferred name is allowed.");
				continue;
			}

			// GF#7047
			if (preferred_name.compareTo("") != 0
					&& !tab.getFilter().checkLeadingAndTrailingSpaces(
							preferred_name)) {
				MsgDialog
						.warning(this, "Data entry warning",
								"No leading or trailing space in preferred name is allowed.");
				continue;
			}

			if (definition.compareTo("") == 0) {
				MsgDialog.ok(this, "Data entry reminder",
						"Reminder -- definition has not been provided.");
				// continue;
			}

			concept_name = name_field.getText().trim();
			if (owlModel.getRDFResource(concept_name) != null) {
				showConceptExistError(concept_name);
				continue;
			}
			


			preferred_name = pt_field.getText().trim();
			definition = StringUtil.escapeXML(def_area.getText().trim());
			superconcept_name = superconcept_field.getText().trim();
			//selected_superclass = owlModel.getOWLNamedClass(superconcept_name);

			OWLNamedClass cls = null;
			if (tab.byCode()) {
				cls = tab.getWrapper().createClsByCode(preferred_name, selected_superclass, definition);
			} else {
				cls = tab.getWrapper().createClsByName(concept_name, preferred_name, selected_superclass, definition);
			}
			if (cls == null) {
				MsgDialog.warning(this, "Unable to create class.");
				continue;
			}

			String value = tab.getOWLWrapper().getPropertyValue(
					selected_superclass, SemanticTypeUtil.SEMANTICTYPE);

			if (value != null) {
				tab.getOWLWrapper().addAnnotationProperty(cls,
						SemanticTypeUtil.SEMANTICTYPE, value);
			}
			// record evs history
			tab.recordHistory(NCIEditTab.EVSHistoryAction.CREATE, cls, "");
			tab.getClassPanel().setSelectedCls(cls);
			break;
		}
	}

	public String getConceptName() {
		return concept_name;
	}

	public String getPreferredName() {
		return preferred_name;
	}

	public String getSuperconceptName() {
		return superconcept_name;
	}

	public Cls getSuperconcept() {
		return selected_superclass;
	}

	public JPanel createPanel() {

		LabeledComponent lc = null;
		JPanel panel = new JPanel();
		panel.setLocation(450, 300);
		panel.setLayout(new BorderLayout());

		superconcept_field = new JTextField(this.superconcept_name);
		superconcept_field.setEditable(false);
		superconcept_field.setPreferredSize(new Dimension(300, 20));
		lc = new LabeledComponent(
				"Click the icon on the right to select a superclass",
				superconcept_field);

		Action createAction = new AbstractAction("Select Superclass", OWLIcons
				.getImageIcon(OWLIcons.RDFS_NAMED_CLASS)) {

			public static final long serialVersionUID = 323456792L;

			public void actionPerformed(ActionEvent e) {
				Collection clses = tab.getOWLWrapper().getSelectableRoots();
				String label = "Select a superclass";
				final NCISelectClsesPanel p = new NCISelectClsesPanel(
						(OWLModel) kb, clses);
				int result = ModalDialog.showDialog(tab, p, label,
						ModalDialog.MODE_OK_CANCEL);
				if (result == ModalDialogFactory.OPTION_OK) {
					Collection c = p.getSelection();
					if (c != null && c.size() > 0) {
						Iterator it = c.iterator();
						Object obj = it.next();
						OWLNamedClass cls = (OWLNamedClass) obj;
						superconcept_field.setText(cls.getBrowserText());
						selected_superclass = cls;
					}
				}

			}
		};

		lc.addHeaderButton(createAction);
		panel.add(lc, BorderLayout.NORTH);

		JPanel middle_panel = new JPanel();
		middle_panel.setLayout(new BorderLayout());

		name_field = new JTextField(this.concept_name);

		if (hasName) {
			name_field = new JTextField(this.concept_name);
			name_field.setEditable(true);
			name_field.setPreferredSize(new Dimension(300, 20));
			lc = new LabeledComponent("Enter class_name", name_field);
			middle_panel.add(lc, BorderLayout.NORTH);
		}

		pt_field = new JTextField(this.preferred_name);
		pt_field.setEditable(true);
		pt_field.setPreferredSize(new Dimension(300, 20));
		lc = new LabeledComponent("Enter preferred name", pt_field);
		middle_panel.add(lc, BorderLayout.CENTER);

		panel.add(middle_panel, BorderLayout.CENTER);
		def_area = new JTextArea(this.definition);
		def_area.setEditable(true);
		boolean wrap = true;
		def_area.setLineWrap(wrap);
		def_area.setWrapStyleWord(wrap);

		Component comp = new JScrollPane(def_area);
		comp.setPreferredSize(new Dimension(400, 120));
		lc = new LabeledComponent("Enter definition", comp);

		panel.add(lc, BorderLayout.SOUTH);

		return panel;
	}

	public void showConceptExistError(String name) {
		MsgDialog.ok(this, "Concept or property or other resource named " + name + " already exists.");
	}
}
