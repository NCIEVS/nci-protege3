package gov.nih.nci.protegex.edit;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_ASSOCIATION;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_PARENT;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_PROPERTY;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_RESTRICTION;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData.CadComp;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData.CompType;
import gov.nih.nci.protegex.tree.TreeItem;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreeItem.TreeItemType;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.SemanticTypeUtil;
import gov.nih.nci.protegex.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class NCIEditFilter {
    public enum UpdateAction {
        ADD, MODIFY, DELETE;
        public String toLowerCase() {
            return toString().toLowerCase();
        }
    };

    private static final String DEF_CURATOR = "Def_Curator";

    private String failure = "";

    

    // 020206
    private final String DEFAULT_TERMTYPE = "SY";

    private final String DEFAULT_SOURCE = "NCI";

    private TreeItems initial_state;

    private TreeItems curr_state;

    private NCIEditTab tab;

    private HashMap<String, ArrayList<String>> authorities = null;

    public NCIEditFilter(NCIEditTab tab, HashMap<String, ArrayList<String>> auth) {
        
        this.tab = tab;

        

        initial_state = new TreeItems();
        curr_state = new TreeItems();

        

        this.authorities = auth;

       
    }

    public void setInitialState(TreeItems items) {
        initial_state = items;
    }

    public void setCurrState(TreeItems items) {
        curr_state = items;
    }

    public void setErrorMessage(String s) {
        failure = s;
    }

    public String getErrorMessage() {
        return failure;
    }

    // DYEE: TreeItems
    public boolean isRetired(TreeItems items) {
        return items.containsValue(TYPE_PARENT, NCIEditTab.RETIRED_CONCEPTS);
    }

    // DYEE: TreeItems
    private boolean checkRetired() {
        if (isRetired(initial_state)) {
            setErrorMessage("Cannot edit a retired concept.");
            return false;
        }
        return true;
    }

    private boolean checkConceptName() {
        String name_1 = initial_state.getClsName();
        String name_2 = curr_state.getClsName();
        if (name_1.compareTo(name_2) != 0) {
            setErrorMessage("Cannot edit concept name.");
            return false;
        }
        return true;
    }

    private boolean checkPreferredName() {
        String pt = "";
        String prefLabel = NCIEditTab.PREFLABEL;
        if (initial_state.containsName(TYPE_PROPERTY, prefLabel) && !curr_state.containsName(TYPE_PROPERTY, prefLabel)) {
            setErrorMessage("Missing preferred name.");
            return false;
        }

        int num_pts = 0;
        for (int i = 0; i < curr_state.size(); i++) {
            TreeItem item = (TreeItem) curr_state.elementAt(i);
            if (item.getType() == TYPE_PROPERTY) {
                if (item.getName().compareTo(prefLabel) == 0) {
                    num_pts++;
                    pt = item.getValue();
                    pt = pt.trim();
                }
            }
        }

        if (num_pts == 0) {
            setErrorMessage("Missing preferred name.");
            return false;
        } else if (num_pts > 1) {
            setErrorMessage("Cannot have multiple preferred names.");
            return false;
        } else if (pt.compareTo("") == 0) {
            setErrorMessage("Preferred name has not been assigned.");
            return false;

        }

        return true;
    }

    // DYEE: TreeItems
    public boolean checkDuplicateValues(TreeItems items, TreeItemType type) {
        HashSet<String> hset = new HashSet<String>();
        for (int i = 0; i < items.size(); i++) {
            TreeItem item = (TreeItem) items.elementAt(i);
            if (item.getType() == type) {
                String key = item.getKey_withRestriction();

                // if (!hset.contains(item.toString()))
                if (!hset.contains(key)) {
                    // hset.add(item.toString());
                    hset.add(key);
                } else {
                    hset.clear();
                    if (type == TYPE_PROPERTY) {
                        setErrorMessage("Duplicate property detected (name: " + item.getName() + ", " + "value: "
                                + item.getValue() + ")");
                        return false;
                    }
                    /*
                     * else if (type == TYPE_RESTRICTION) { setErrorMessage("Duplicate restriction detected (" +
                     * item.toString() + ")"); return false; }
                     */
                    else if (type == TYPE_PARENT) {
                        setErrorMessage("Duplicate superclass detected (" + item.toString() + ")");
                        return false;
                    } else if (type == TYPE_ASSOCIATION) {
                        setErrorMessage("Duplicate association detected (" + item.toString() + ")");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // DYEE: TreeItems
    public boolean checkRestrictionValues(TreeItems items) {
        // create a lits to track necessary and sufficient conditions
        ArrayList<TreeItem> nands = new ArrayList<TreeItem>();
        for (int i = 0; i < items.size(); i++) {
            TreeItem item = (TreeItem) items.elementAt(i);
            if (item.getIsDefining()) {
                nands.add(item);
            }
            if (item.getType() == TYPE_RESTRICTION) {

                if (item.getValue().compareTo(NCIEditTab.RETIRED_CONCEPTS) == 0
                        || item.getValue().compareTo(NCIEditTab.PRERETIRED_CONCEPTS) == 0
                        || item.getValue().compareTo(NCIEditTab.PREMERGED_CONCEPTS) == 0) {
                    setErrorMessage("Invalid filler value found in the restriction\n" + item.toString());
                    return false;
                }
            }
        }

        // check for sole necessary and sufficient condition
        // there must be at least 2 N&S conditions and at least one of them is a SOME OR
        // at least one of them is a N&S parent
        if (nands.size() > 0) {
        if (nands.size() < 2) {
        	setErrorMessage("There must be at least two Necessary and Sufficient conditions\n");
            return false;
            

        } else {
        	boolean isDefiningParent = false;
        	boolean isSomeRestriction = false;
        	for (int j = 0; j < nands.size();j++) {
        		TreeItem it = nands.get(j);
        		if (it.getType() == TYPE_RESTRICTION && it.getIsDefining()) {
        			if (it.getModifier().equalsIgnoreCase("some")) {
        				isSomeRestriction = true;
        			}
        		} else if (it.getType() == TYPE_PARENT && it.getIsDefining()) {
        			isDefiningParent = true;
        			
        		}
        	}
        	if (isDefiningParent || isSomeRestriction) {
        		//ok        		
        	} else {
        		setErrorMessage("To use \"only\" in N&S restrictions, at least one \"some\" restriction or one defining superclass must also be present.\n");
                return false;
        	}
        }
        }
        return true;
    }

    public boolean run() {
        if (!runCheckProperties())
            return false;

        // 041207 switch order
        if (tab.useNCIRules()) {
        if (!checkPreferredName())
            return false;
        if (!checkFullSyn())
            return false;
        }

        if (!checkRestrictionValues(curr_state))
            return false;
        if (!checkRetired())
            return false;
        if (!checkConceptName())
            return false;
        if (!checkDuplicateValues(curr_state, TYPE_RESTRICTION))
            return false;

        return true;
    }

    /**
     * 1. Each concept can have at most one Def_Curator property. 2. If a concept has a Def_Curator property, then only
     * authorized editors can make change to the NCI's DEFINITION property. 3. The names (schema name) of the users who
     * are authorized to make changes to the DEFINITION property will be stored in the server. 4. DEFINITION and
     * ALT_DEFINITION properties length should not exceed 1024. 5. Date of review time-stamped by the filter (applicable
     * to both definition properties.) 6. A concept can have multiple DEFINITION and LONG_DEFINITION
     */

    // Need to parse a XML-string to a vector of NameAndValue pairs
    private Qualifier[] getQualifiers(Property property) {
        Vector<Qualifier> qualifier_vec = new Vector<Qualifier>();
        String prop_value = property.getValue();
        // if (isComplexProperty(prop_value))
        {
            // def-source
            // def-definition

            prop_value = ComplexPropertyParser.pipeDelim2XML(prop_value);
            HashMap<String, String> name2val_hashmap = ComplexPropertyParser.parseXML(prop_value);
            Iterator<String> iter = name2val_hashmap.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = (String) name2val_hashmap.get(key);
                Qualifier q = new Qualifier(key, value);
                qualifier_vec.add(q);
            }
        }

        Qualifier[] qualifiers;
        qualifiers = new Qualifier[qualifier_vec.size()];

        for (int i = 0; i < qualifier_vec.size(); i++)
            qualifiers[i] = (Qualifier) qualifier_vec.elementAt(i);

        return qualifiers;
    }

    private boolean runCheckProperties() {
        HashSet<String> prop_set = new HashSet<String>();
        for (int i = 0; i < curr_state.size(); i++) {
            TreeItem item = (TreeItem) curr_state.elementAt(i);
            if (item.getType() == TYPE_PROPERTY) {
                String s = item.toString();
                if (!prop_set.contains(s)) {
                    prop_set.add(s);
                    Property prop = new Property(item.getName(), item.getValue());

                    // check non-printable characters
                    if (!checkProperty(prop, false)) {
                        return false;
                    }
                } else {
                    failure = "Cannot have duplicate properties " + s + ".";
                    System.out.println(failure);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isEquivalentToPT(String termtype) {
        if (termtype.equals("PT") || termtype.equals("HD") || termtype.equals("AQ"))
            return true;
        return false;
    }

    public boolean checkBatchProperty(String name, String value) {
        Property prop = new Property(name, value);
        if (checkProperty(prop, true)) {
            if (prop.getName().equalsIgnoreCase(SemanticTypeUtil.SEMANTICTYPE)) {
                return checkSemanticTypeValue(prop.getValue());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    public boolean checkBatchPropertyNotFullSynPT(String name, String value) {
        if (name.compareTo(NCIEditTab.ALTLABEL) != 0) {
            return true;
        } else {
            // ok, we have a full syn check
            String tn = ComplexPropertyParser.getPtNciTermName(value);
            if (tn != null) {
                // this is a PT/NCI full syn
                failure = "cannot add NCI/PT FullSyn to existing class";
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean checkSemanticTypeValue(String value) {
        String[] values = tab.getOWLWrapper().getAllowableValues(
                tab.getOWLModel().getRDFProperty(SemanticTypeUtil.SEMANTICTYPE));
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(value)) {
                return true;
            }
        }
        failure = "semantic type value: " + value + " is not valid";
        return false;

    }

    public boolean checkProperty(Property prop, boolean batch) {
        if (prop == null)
            return true;

        String prop_name = prop.getName();
        String value = prop.getValue();
        String warning = "Invalid " + prop_name + ": " + value + ". ";
        if (checkComplexProperty(prop_name, value, batch, warning)) {
            return checkPropertyValue(warning, value, false);
        } else {
            //failure = warning;
            return false;
        }
    }

    private boolean checkComplexProperty(String p_name, String value, boolean batch, String warning) {

        if (tab.isComplexProp(p_name)) {
        	CustomizedAnnotationData cad = NCIEditTab.getCustomizedAnnotationData(p_name);
        	return validateComplexProperty(cad, value, warning);

        } else {
        	if (batch) {
        		// check for allowed values
        		String[] allowedvalues = tab.getOWLWrapper().getAllowableValues(
        				tab.getOWLWrapper().getRDFProperty(p_name));
        		if (allowedvalues != null) {
        			if (tab.getOWLWrapper().isValidValue(allowedvalues, value)) {
        				return true;
        			} else {
        				failure = "Invalid property value";
        				return false;
        			}
        		} else {
        			return true;
        		}
        	}

            return true;
        }
    }
    
    private boolean validateComplexProperty(CustomizedAnnotationData cad, String value, String warning) {
        
        try {
        
        HashMap<String, String> hmap = ComplexPropertyParser.parseXML(value);
        
        if (!hmap.isEmpty()) {
            
            for (CadComp cn : cad.getCadComps()) {
                String vs = hmap.get(cn.getName());
                if (vs != null) {
                    if (cn.isValid(vs)) {
                        
                    } else {
                    	failure = warning + "\n " + cn.getName() + " is invalid";
                        return false;
                    }
                } else if (cn.isRequired()) {
                	failure = warning + "\n " + cn.getName() + ", a required element is missing";
                	return false;
                }
                
            }
            return true;
                
            
        } else {
        	failure = warning + "\n " + cad.getRootTag() + ", unable to parse complex property value, possible escaping issue or otherwise not well-formed xml";
            return false;
        }
        
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 1.More than one NCI/PT in ALTLABEL (Note: A concept does not need to have a NCI PT in ALTLABEL) 2.NCI/PT entry in
     * the ALTLABEL property does not match with the Preferred_Name field 3.Allow full_syns with no qualifiers to pass
     * through 4.One full_syn must be either HD or PT with either no source or NCI source
     */
    // private boolean checkFullSyn(Property[] props){
    private boolean checkFullSyn() {
        Vector<Property> properties = curr_state.getProperties();
        Property[] props = new Property[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            props[i] = (Property) properties.elementAt(i);
        }

        HashSet<String> fullsyn_set = new HashSet<String>();
        String source, termtype;
        String pt;

        source = termtype = pt = null;

        int pt_knt = 0;

        for (int i = 0; i < props.length; i++) {
            if (props[i].getName().compareTo(NCIEditTab.ALTLABEL) == 0) {
                Qualifier[] qualifiers = getQualifiers(props[i]);

                int num_source = 0;
                int num_type = 0;
                int num_code = 0;

                String term_name = null;

                for (int k = 0; k < qualifiers.length; k++) {
                    if (qualifiers[k].getName().equals("term-source")) {
                        source = qualifiers[k].getValue();
                        num_source++;
                    } else if (qualifiers[k].getName().equals("term-group")) {
                        termtype = qualifiers[k].getValue();
                        num_type++;
                    } else if (qualifiers[k].getName().equals("source-code")) {
                        num_code++;
                    } else if (qualifiers[k].getName().equals("term-name"))
                        term_name = qualifiers[k].getValue();
                }

                // 020206
                if (num_type == 0) {
                    termtype = DEFAULT_TERMTYPE;
                    num_type++;
                }
                if (num_source == 0) {
                    source = DEFAULT_SOURCE;
                    num_source++;
                }

                if (num_source > 1 || num_type > 1 || num_code > 1) {
                    failure = "ALTLABEL property format is incorrect: "
                            + "can have exactly one term-source and one term-group and " + "one optional source-code.";
                    return false;
                }

                if (isEquivalentToPT(termtype) && source.equals("NCI")) {
                    pt_knt++;

                    fullsyn_set.add(term_name);

                    if (pt_knt > 1) {
                        failure = "Cannot have more than one NCI PT (or its equivalent) ALTLABEL properties. ";
                        return false;
                    }
                }

            } else if (props[i].getName().equals(NCIEditTab.PREFLABEL)) {
                pt = props[i].getValue();
            }
        }

        if (fullsyn_set.size() != 0 && !fullsyn_set.contains(pt)) {
            failure = "NCI/" + termtype + " ALTLABEL property name does not match " + "with Preferred_Name.";
            return false;
        }

        if (pt_knt == 0) {
            failure = "Must have at least one NCI PT (or its equivalent) ALTLABEL properties. ";
            return false;
        }

        return true;
    }

    /*******************************************************************************************************************
     * for the DEFINITION property, only one space between tokens except when preceeded by period (.), exclamation mark
     * (!), or question mark (?), in which case two spaces are allowed.
     */
    public Vector<String> parseString(String xml) {
        Vector<String> v = new Vector<String>();
        StringTokenizer st = new StringTokenizer(xml, "|");

        while (st.hasMoreTokens())
            v.add(st.nextToken());

        return v;
    }

    public boolean checkPropertyValue(String warning, String value, boolean checknonprintable) {
        int len = value.length();
        if (len == 0)
            return true;

        char prev = value.charAt(0);
        int trimlen = value.trim().length();
        if (trimlen == 0)
            return true;

        boolean warningOff = false;
        int count = 0;

        if (warning == null)
            warning = "";

        if (trimlen != len) {
            failure = warning + "No leading or trailing space is allowed.";
            return false;
        }

        for (int i = 1; i < len; i++) {
            char curr = value.charAt(i);
            int charVal = (int) curr;

            // 020606
            // if ((charVal < 32) || (charVal >= 127)) {
            if (checknonprintable) {
                if (charVal < 32) {
                    failure = "Cannot have nonprintable character: " + curr;
                    return false;
                }
            }

            if ((prev == ' ') && (curr == ' ')) {
                count++;
                if (count > 1) {
                    failure = warning + "Too many spaces between tokens.";
                    return false;
                } else if (!warningOff && count == 1) {
                    failure = warning + "Too many spaces between tokens.";
                    return false;
                }
            }

            if (StringUtil.isPunctuation(curr)) {
                warningOff = true;
                count = 0;
            }
            prev = curr;
        }
        return true;
    }

    // Concept name is not editable
    // Both before and after images of concept name must meet
    // checkXMLNCNameCompliance.

    public boolean runCheckXMLNCNameCompliance(String name) {
        String warning = "Invalid concept name " + name;
        return checkXMLNCNameCompliance(warning, name);
    }

	public static boolean checkXMLNCNameCompliance(String value) {
		byte ptext[] = value.getBytes();
		try {
			value = new String(ptext, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (value == null)
			return false;
		if (value.length() == 0)
			return false;

		// Leading character must be a letter or '_'
		char c = value.charAt(0);

		if (!(Character.isLetter(c) || c == '_')) {
			return false;
		}

		// All characters must be a letter, a digit, or in {'.', '-', '_'}
		// NCICB disallows period.
		for (int i = 1; i < value.length(); i++) {
			c = value.charAt(i);
			if (!(Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_')) {
				return false;
			}
		}
		return true;
	}

    private boolean checkXMLNCNameCompliance(String warning, String value) {
        // Leading character must be a letter or '_'
        char c = value.charAt(0);

        if (!(Character.isLetter(c) || c == '_')) {
            failure = warning + "\n Concept name must start with a letter or '_'";
            return false;
        }

        // All characters must be a letter, a digit, or in {'.', '-', '_'}
        // NCICB disallows period.
        for (int i = 1; i < value.length(); i++) {
            c = value.charAt(i);
            if (!(Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_')) {
                failure = warning + "\n Cannot have character: '" + c + "'";
                return false;
            }
        }
        return true;
    }

    // 010606
    public boolean hasAuthorization(String curator, String user) {
        System.out.println("Checking authorization ...");
        System.out.println("curator: " + curator);
        System.out.println("user: " + user);

        if (authorities.containsKey(curator)) {

            for (String s : authorities.get(curator)) {
                if (s.equalsIgnoreCase(user)) {
                    return true;
                }
            }
            return false;

        } else {

            return false;
        }
    }

    public boolean canUpdate(UpdateAction action, TreeItem item, TreeItems initialState) {
        if (item.getType() == TYPE_PROPERTY) {
            if ((item.getName().compareTo(NCIEditTab.DEFINITION) == 0)
                    || (item.getName().compareTo(NCIEditTab.CURATORIALAUTHORITY) == 0)) {
                Vector<String> list = initialState.getPropertyValues(DEF_CURATOR);
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        String curator = list.elementAt(i);
                        System.out.println("Curator: " + curator);
                        String user = tab.getUserName();
                        boolean retval = hasAuthorization(curator, user);
                        if (retval) {
                            System.out.println("User " + user + " can " + action.toLowerCase()
                                    + " the following definition:" + "\n  * " + item.getNameValue());
                        } else {
                            String nv = item.getNameValue();
                            if (nv.length() > 132)
                                nv = nv.substring(0, 131) + " ...";
                            failure = "User " + user + " does not have authority to " + action.toLowerCase()
                                    + " the following definition:" + "\n  * " + nv;
                            System.out.println(failure);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean canAdd(TreeItems items, TreeItems initialState) {
        for (int i = 0; i < items.size(); i++) {
            TreeItem item = (TreeItem) items.elementAt(i);
            boolean retval = canUpdate(UpdateAction.ADD, item, initialState);
            if (!retval) {
                return false;
            }
        }
        return true;
    }

    public boolean canDelete(TreeItems items, TreeItems initialState) {
        for (int i = 0; i < items.size(); i++) {
            TreeItem item = (TreeItem) items.elementAt(i);
            boolean retval = canUpdate(UpdateAction.DELETE, item, initialState);
            if (!retval) {
                return false;
            }
        }
        return true;
    }

    // GF#7047
    public boolean checkLeadingAndTrailingSpaces(String value) {
        int len = value.length();
        if (len == 0)
            return true;
        int trimlen = value.trim().length();
        if (trimlen != len) {
            // failure = warning
            // + "No leading or trailing space is allowed.";
            return false;
        }
        return true;
    }
}
