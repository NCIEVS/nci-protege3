package gov.nih.nci.protegex.dialog;

import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;

import javax.swing.*;

import gov.nih.nci.protegex.dialog.CustomizedAnnotationData.CadComp;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData.CompType;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class CustomizedAnnotationConstructor {
    private EditPanel edit_panel;

    private JComponent[] components;
    
    private JComponent focusComp = null;
    
    public JComponent getFocus() {
        return focusComp;
    }

    private int numfields;

    private ArrayList<CadComp> cadComps = null;
    
    private CustomizedAnnotationData cad = null;

	public CustomizedAnnotationConstructor(EditPanel edit_panel,
			CustomizedAnnotationData cad) {
		this.edit_panel = edit_panel;
		this.cad = cad;
		this.cadComps = cad.getCadComps();
		this.numfields = cadComps.size();
	}
	
	public JPanel createPanelFromStrings(String[] values) {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel desc_panel = new JPanel();
		desc_panel.setLayout(new BorderLayout());

		components = new JComponent[numfields];
		JLabel[] labels = new JLabel[numfields];

		for (int i = 0; i < numfields; i++) {
			labels[i] = new JLabel(cadComps.get(i).label);
		}
		// TODO: Bob figure out how to get the property value in there on repeat
		// displays
		JTextArea c0 = new JTextArea("");
		boolean wrap = true;
		c0.setLineWrap(wrap);
		c0.setWrapStyleWord(wrap);
        
        focusComp = c0;

		if (values[0] != null) {
			c0.setText(values[0]);
		} else {
			// TODO: Bob This is a very ugly hack, default values are used each
			// time through
			// when the user needs to fix the data for whatever reason.
			// Originally the default
			// values are intended to be the defaults for creating a new
			// property. This will need
			// to be redesigned
			c0.setText((String) cadComps.get(0).defaultValue);
		}
		components[0] = (JComponent) c0;
        JScrollPane sp = new JScrollPane(c0);
        sp.setPreferredSize(new Dimension(250, 150));
        
		desc_panel.add(labels[0], BorderLayout.NORTH);
		desc_panel.add(sp, BorderLayout.CENTER);

		for (int i = 1; i < numfields; i++) {
			// labels[i] = new JLabel(component_names[i]);
			CompType type = cadComps.get(i).type;
			switch (type) {
			case JTEXTAREA:
				if (values[i] != null) {
					JTextArea ct = new JTextArea(values[i]);
					components[i] = (JComponent) ct;
				} else {
					components[i] = new JTextArea("");
				}
				break;
			case JTEXTFIELD:
				if (values[i] != null) {
					JTextField cp = new JTextField(values[i]);
					components[i] = (JComponent) cp;
				} else {
					JTextField cp = new JTextField("");
					components[i] = (JComponent) cp;
					String default_value = cadComps.get(i).defaultValue;
					if (default_value != null) {
						cp.setText(default_value);
					}
				}
				break;
			case DATE:
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
				String mydate = sdf.format(new Date());
				JTextField c = new JTextField(mydate);
				c.setEditable(false);
				components[i] = (JComponent) c;
				break;

			case USER:

				String username = edit_panel.getUserName();
				JTextField cu = new JTextField(username);
				cu.setEditable(false);
				components[i] = (JComponent) cu;
				break;

			case COMBOBOX:

				ArrayList<String> cbv = cadComps.get(i).comboBoxVals;
				String[] groups = new String[cbv.size()];
				cbv.toArray(groups);

				JComboBox cb = new JComboBox(groups);
				if (values[i] != null) {
					boolean found = false;
					for (int k = 0; k < groups.length; k++) {
						if (groups[k].equalsIgnoreCase(values[i])) {
							found = true;
							break;
						}
					}
					if (!found) {
						cb.addItem(values[i]);
					}
					cb.setSelectedItem(values[i]);
				} else {
					String selection = cadComps.get(i).defaultValue;
					cb.setSelectedItem(selection);
				}
				components[i] = (JComponent) cb;
				break;

			default:
				break;
			}

		}

		JPanel other_panel = new JPanel();
		other_panel.setLayout(new GridLayout(numfields - 1, 2));

		for (int j = 1; j < numfields; j++) {
			other_panel.add(labels[j]);
			other_panel.add(components[j]);
		}

		panel.add(desc_panel, BorderLayout.NORTH);
		panel.add(other_panel, BorderLayout.CENTER);

		return panel;
		
	}

	public JPanel createPanel(String value) {
		String[] values = new String[numfields];

		HashMap<String, String> hmap = new HashMap<String, String>();
		if (value != null && value.compareTo("") != 0) {
			//value = ComplexPropertyParser.pipeDelim2XML(value);
			hmap = ComplexPropertyParser.parseXML(value);
		}

		if (!hmap.isEmpty()) {
			int i = 0;
			for (CadComp cn : cadComps) {
				values[i++] = (String) hmap.get(cn.name);
			}
		}
		return createPanelFromStrings(values);
	}

	public String getValue(int icomponent) {
		String value = "";
		JComponent component = components[icomponent];
		if (component instanceof JTextArea) {
			JTextArea c = (JTextArea) component;
			value = StringUtil.cleanString(c.getText(), false);
		} else if (component instanceof JTextField) {
			JTextField c = (JTextField) component;
			value = StringUtil.cleanString(c.getText(), false);
		} else if (component instanceof JComboBox) {
			JComboBox c = (JComboBox) component;
			value = (String) c.getSelectedItem();
		}
		return value;
	}

	public String getValue() {
		ArrayList<String> gui_vals = new ArrayList<String>();
		for (int i = 0; i < components.length; i++) {
			gui_vals.add(getValue(i));
		}
		return cad.formatValues(gui_vals);
			
		
	}
}
