/**
 * 
 */
package gov.nih.nci.protegex.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.panel.SplitPanel;
import edu.stanford.smi.protegex.owl.model.*;

/**
 * @author Bob Dionne
 * 
 */
public class Config {

	Logger logger = Log.getLogger(getClass());

	Vector<String> noneditableClasses = null;

	Vector<String> requiredProperties = null;

	Vector<String> readOnlyProperties = null;

	Vector<String> complexProperties = null;

	Vector<String> nontransferprops = null;

	public Vector<String> getComplexProperties() {
		return complexProperties;
	}

	private String idBy = "code";
	private String useRules = "";

	public void setIdBy(String s) {
		if (s != null) {
			idBy = s;
		}
		if (idBy.equals("code")) {
			wrapper.codeSlotExists = true;
			wrapper.setCodeSlotName(idBy);
			NCIEditTab.CODE = idBy;
		}
	}

	public void setUseRules(String s) {
		if (s != null) {
			useRules = s;
		}
	}

	public String getUseRules() {
		return useRules;
	}

	private HashMap<String, ArrayList<String>> authorities = new HashMap<String, ArrayList<String>>();

	public HashMap<String, ArrayList<String>> getAuthorities() {
		return authorities;
	}

	private String smwBaseUrl = null;

	public String getSmwBaseUrl() {
		return smwBaseUrl;
	}

	private String smwTopLevelPage = null;

	private HashMap<String, CustomizedAnnotationData> customizedAnnotationData_Map = new HashMap<String, CustomizedAnnotationData>();

	public String getSmwTopLevelPage() {
		return smwTopLevelPage;
	}

	public void setSMWString(String bu, String tlp) {
		smwBaseUrl = bu;
		smwTopLevelPage = tlp;
	}

	private Map<String, String> prefix_names = new HashMap<String, String>();

	public void addSMWPrefix(String p_name, String prefix) {
		prefix_names.put(p_name, prefix);

	}

	public String getSMWPrefix(String base) {
		return prefix_names.get(base);
	}

	public void addCAD(String name, CustomizedAnnotationData cad) {

		customizedAnnotationData_Map.put(name, cad);
	}

	public CustomizedAnnotationData getCustomizedAnnotationData(String name) {

		return (CustomizedAnnotationData) customizedAnnotationData_Map
				.get(name);

	}

	public void addAuthority(String a, String u) {
		ArrayList<String> al = authorities.get(a);
		if (al == null) {
			al = new ArrayList<String>();
			authorities.put(a, al);
		}
		al.add(u);
	}

	private OWLWrapper wrapper = null;

	public boolean isReadOnlyProperty(String name) {
		if (readOnlyProperties.contains(name))
			return true;
		return false;
	}

	public Vector<String> getRequiredProperties() {
		return requiredProperties;
	}

	public Vector<String> getReadOnlyProperties() {
		return readOnlyProperties;
	}

	public Vector<String> getNonEditableProperties() {
		return noneditableClasses;
	}

	public Vector<String> getNonTransferProps() {
		return this.nontransferprops;
	}

	private void initProps() {

		readOnlyProperties = new Vector<String>();
		requiredProperties = new Vector<String>();

		noneditableClasses = new Vector<String>();

		complexProperties = new Vector<String>();

		nontransferprops = new Vector<String>();

	}

	private void initReadOnly() {

		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONPARENTCONCEPT);
		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONCHILDCONCEPT);
		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONROLE);
		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONASSOC);
		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONSTATE);
		readOnlyProperties.add(NCIEditTab.CODE);
		readOnlyProperties.add(NCIEditTab.RDFLABEL);
		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONSOURCEROLE);
		readOnlyProperties.add(NCIEditTab.PREDEPRECATIONSOURCEASSOC);
		readOnlyProperties.add(NCIEditTab.MERGETO);
		readOnlyProperties.add(SplitPanel.MERGE_SOURCE);
		readOnlyProperties.add(SplitPanel.MERGE_TARGET);
		readOnlyProperties.add(SplitPanel.SPLIT_FROM);

	}

	public Config(String filename, OWLWrapper wrapper) {
		this.wrapper = wrapper;

		initProps();

		ConfigFileParser cfp = new ConfigFileParser(filename, this, logger);
		try {
			cfp.processDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}

		initReadOnly();

		wrapper
				.init(noneditableClasses, readOnlyProperties,
						requiredProperties);
	}

	public void addRequiredProp(String name) {
		requiredProperties.add(name);
	}

	public void addComplexProperty(String s) {
		complexProperties.add(s);
	}

	public RDFProperty getPropertySlot(String name) {
		return wrapper.getRDFProperty(name);
	}

	public String[] getSupportedLanguages() {
		return wrapper.getSupportedLanguages();
	}

	public void addNonEditableConcept(String con_name) {
		noneditableClasses.add(con_name);
	}

	private HashMap<String, ArrayList<String>> disMenus = new HashMap<String, ArrayList<String>>();

	public HashMap<String, ArrayList<String>> getDisableMenus() {
		return disMenus;
	}

	public void disableMenuItems(String menu, String submenu) {
		ArrayList<String> subs = disMenus.get(menu);
		if (subs == null) {
			subs = new ArrayList<String>();
			disMenus.put(menu, subs);
		}
		subs.add(submenu);
	}

}
