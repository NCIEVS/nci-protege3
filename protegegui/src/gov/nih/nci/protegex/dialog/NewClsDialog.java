package gov.nih.nci.protegex.dialog;

import java.awt.*;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import edu.stanford.smi.protege.util.LabeledComponent;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.util.StringUtil;

public class NewClsDialog extends JDialog implements ActionListener {
	public static final long serialVersionUID = 123456799L;

	JButton okButton, cancelButton;

	JTextField name_field;

	JTextField pt_field;

	private boolean inc_definition = false;

	private JTextArea defArea;

	private String definition = "";

	NCIEditTab tab;

	private String label;

	private String pt;

	public boolean cancelButtonPressed = false;

	public NewClsDialog(NCIEditTab tab, boolean include_def) {
		super((JFrame) tab.getTopLevelAncestor(), "Enter Class Identifiers",
				true);
		this.name_field = new JTextField("");
		this.name_field.setPreferredSize(new Dimension(300, 20));

		this.pt_field = new JTextField("");
		this.pt_field.setPreferredSize(new Dimension(300, 20));

		inc_definition = include_def;

		if (include_def) {
			defArea = new JTextArea("");
			defArea.setEditable(true);
			defArea.setLineWrap(true);
			defArea.setWrapStyleWord(true);
		}

		this.tab = tab;
		this.label = "";
	}

	public String getLabel() {
		return label;
	}

	public String getPT() {
		return pt;
	}

	public String getDefinition() {
		return definition;
	}

	public void setTab(NCIEditTab tab) {
		this.tab = tab;
	}

	public void init() {
		init("", "", "");
	}

	public void init(String init_name, String init_pt, String def) {
		Container contain = this.getContentPane();
		
		// this.setLocation(480,360);

		this.setLocation(450, 300);
		this.setLayout(new BorderLayout());
		// this.setLocation(300,200);

		LabeledComponent lc = null;
		contain.setLayout(new BorderLayout());

		name_field = new JTextField(init_name);

		if (!tab.byCode()) {
			name_field.setEditable(true);
			name_field.setPreferredSize(new Dimension(300, 20));
			lc = new LabeledComponent("Enter class name", name_field);
			contain.add(lc, BorderLayout.NORTH);
		}
		
		//if (tab.useNCIRules()) {

		pt_field = new JTextField(init_pt);
		pt_field.setEditable(true);
		pt_field.setPreferredSize(new Dimension(300, 20));
		lc = new LabeledComponent("Enter preferred name", pt_field);
		//}

		if (inc_definition) {

			JPanel middle_panel = new JPanel();
			middle_panel.setLayout(new BorderLayout());

			//if (tab.useNCIRules()) {
			middle_panel.add(lc, BorderLayout.NORTH);
			//}

			Component comp = new JScrollPane(defArea);
			defArea.setText(def);
			comp.setPreferredSize(new Dimension(400, 120));
			lc = new LabeledComponent("Enter Definition", comp);

			middle_panel.add(lc, BorderLayout.CENTER);

			contain.add(middle_panel, BorderLayout.CENTER);
		} else {
			//if (tab.useNCIRules()) {
			contain.add(lc, BorderLayout.CENTER);
			//}
		}

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		contain.add(buttonPanel, BorderLayout.SOUTH);

		pack();
		
		
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		cancelButtonPressed = false;
		Object action = event.getSource();
		if (action == okButton) {

			//if (tab.useNCIRules()) {
			pt = pt_field.getText();
			pt = pt.trim();
			pt_field.setText(pt);
			//}

			if (!tab.byCode()) {
				label = name_field.getText();
				label = label.trim();
				name_field.setText(label);
			} else {
				if (tab.useNCIRules()) {
				label = pt;
				}
			}

			if (inc_definition) {
				definition = StringUtil.cleanString(defArea.getText(), false);
			}

			dispose();

		} else if (action == cancelButton) {
			label = "";
			pt = "";
			cancelButtonPressed = true;
			dispose();
		}
	}

}
