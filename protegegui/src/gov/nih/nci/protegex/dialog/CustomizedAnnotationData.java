package gov.nih.nci.protegex.dialog;

import gov.nih.nci.protegex.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class CustomizedAnnotationData {
	String title;
	String key;
	String annotation_name;
	String root_tag;
	String panelLabel;
	
	public void setRootTag(String s) {
		root_tag = s;
	}
	
	public String getRootTag() {
		return root_tag;
	}

	public String getPanelLabel() {
		return panelLabel;
	}

	private int hasLangChoice() {
		for (int i = 0; i < cadCompsArray.size(); i++) {
			if (cadCompsArray.get(i).name.equalsIgnoreCase("xml:lang")) {
				return i;
			}
		}
		return -1;
	}

	public String formatValues(ArrayList<String> gui_vals) {
		int idx = hasLangChoice();
		String useLang = null;
		if (idx > -1) {
			if (gui_vals.get(idx).equalsIgnoreCase("en")) {
				
			} else {
				useLang = gui_vals.get(idx);

			}

		}
		StringBuffer buff = new StringBuffer();
		String beg = "<ncicp:" + root_tag + " xmlns:ncicp=\"http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#\">";
		//String beg = "<ncicp:" + root_tag + ">";
		String end = "</ncicp:" + root_tag + ">";
		buff.append(beg);
		
		for (int i = 0; i < gui_vals.size(); i++) {
			String vs = gui_vals.get(i);
			if (vs == null)
				continue;
			String key = "ncicp:" + (String) cadCompsArray.get(i).name;
			if ((i == 0) && (useLang != null)) {
				StringUtil.createXmlTag(buff, true, key, vs, "xml:lang",
						useLang);
				// check if default, then ignore, ow add as attribute
			} else if (cadCompsArray.get(i).name.equalsIgnoreCase("xml:lang")) {
				// nop already handled case

			} else {
				StringUtil.createXmlTag(buff, true, key, vs);
			}

		}
		buff.append(end);
		
		return buff.toString();
	}

	public CustomizedAnnotationData(String name, String panelLabel) {

		annotation_name = name;
		this.panelLabel = panelLabel;
	}

	public void setAnnotationName(String annotation_name) {
		this.annotation_name = annotation_name;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setKey(String key) {
		this.key = key;
	}

	// new approach
	public static enum CompType {
		JTEXTAREA("JTextArea"), JTEXTFIELD("JTextField"), DATE("Date"), COMBOBOX(
				"JComboBox"), USER("Usr");

		String name = null;

		private CompType(String s) {
			name = s;
		}

	};

	public class CadComp {

		String name = null;
		String label = null;
		CompType type = null;
		ArrayList<String> comboBoxVals = null;
		String defaultValue = null;
		boolean required = false;
		
		public boolean isRequired() {return required;}
		
		public void setIsRequired(boolean b) {required = b;}

		public boolean isValid(String val) {
			if (type == CompType.COMBOBOX) {
				for (String s : comboBoxVals) {
					if (val.compareTo(s) == 0) {
						return true;
					}
				}
				return false;
			} else if (type == CompType.JTEXTAREA || type == CompType.JTEXTFIELD) {
				String scrub = StringUtil.cleanString(val, false);
				if (scrub.compareTo("") == 0) {
					return false;
				} else {
					return true;
				}
			}
			return true;
		}

		public CadComp(String s) {
			name = s;
			label = s;
		}

		public CadComp(String s, String l, CompType t, ArrayList<String> cbv,
				String d) {
			this(s);
			label = l;
			type = t;
			comboBoxVals = cbv;
			defaultValue = d;
		}

		public String getName() {
			return name;
		}

		public void setName(String s) {
			name = s;
		}

		public void setType(CompType t) {
			type = t;
		}

		public void setLabel(String l) {
			label = l;
		}

		public void addComboBoxValue(String vs) {
			if (comboBoxVals == null) {
				comboBoxVals = new ArrayList<String>();
			}
			comboBoxVals.add(vs);
		}

		public void setDefaultValue(String def) {
			defaultValue = def;
		}
	}

	private HashMap<String, CadComp> cadComps = new HashMap<String, CadComp>();
	private ArrayList<CadComp> cadCompsArray = new ArrayList<CadComp>();

	public CadComp createComponent(String tagName) {
		if (cadComps.get(tagName) != null) {
			// do nothing
		} else {
			CadComp cc = new CadComp(tagName);
			cadComps.put(tagName, cc);
			cadCompsArray.add(cc);

		}
		return cadComps.get(tagName);
	}

	public ArrayList<CadComp> getCadComps() {
		return cadCompsArray;
	}

	public ArrayList<String> getCadCompsNames() {
		ArrayList<String> res = new ArrayList<String>(cadCompsArray.size());
		for (CadComp cmp : cadCompsArray) {
			res.add(cmp.name);

		}
		return res;
	}

}
