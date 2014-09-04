/**
 * 
 */
package gov.nih.nci.protegex.util;

import static gov.nih.nci.protegex.dialog.CustomizedAnnotationData.CompType;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData;
import gov.nih.nci.protegex.edit.EditTabException;
import gov.nih.nci.protegex.workflow.WorkflowException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import static gov.nih.nci.protegex.util.ConfigFileParser.State.*;
import gov.nih.nci.protegex.edit.NCIEditTab;

/**
 * A simple SAX style parser for parsing the config file of NciEditTab There are a few major components to the config
 * file
 * 
 * 
 * @author Bob Dionne
 * 
 */
public class ConfigFileParser {

    /**
     * This enum serves a couple of purposes. It tracks the state of the parser. If an element can only have certain
     * children in the document, the element will belong to those child states parents. As the parser transitions from
     * state to state the transitions can be validated.
     * 
     * It also associates the labels with each state.
     * 
     */
    public enum State {

        EMPTY(new State[] {}, ""), CONFIG(EMPTY, "config"), PANEL(CONFIG, "panel"), PANELNAME(PANEL, "panel-name"), PANELLABEL(
                PANEL, "panel-label"), OWLPROPERTY(PANEL, "owl-property"), NAME(OWLPROPERTY, "name"), TABLE(
                OWLPROPERTY, "table"), FIELD(TABLE, "field"), ELEMENT(FIELD, "element"), LABEL(FIELD, "label"), TYPE(
                FIELD, "type"), ENUM(FIELD, "enumeration"), VALUE(new State[] { ENUM, FIELD }, "value"), ERROR(EMPTY,
                "error"), MENUDISABLE(CONFIG, "menu-disable"), MENUBAR(MENUDISABLE, "menu-bar"), MENUITEM(MENUDISABLE,
                "menu-item"), REQUIREDPROPERTY(CONFIG, "required-property"), PREFLABEL(REQUIREDPROPERTY, "prefLabel"), ALTLABEL(
                REQUIREDPROPERTY, "altLabel"), DEFINITION(REQUIREDPROPERTY, "definition"), SCOPENOTE(REQUIREDPROPERTY,
                "scopeNote"), EDITORIALNOTE(REQUIREDPROPERTY, "editorialNote"), PRERETIRECHILDCONCEPT(REQUIREDPROPERTY,
                "preDeprecationChildConcept"), PRERETIREPARENTCONCEPT(REQUIREDPROPERTY, "preDeprecationParentConcept"), PRERETIREASSOC(
                REQUIREDPROPERTY, "preDeprecationAssoc"), PRERETIRESOURCEASSOC(REQUIREDPROPERTY,
                "preDeprecationSourceAssoc"), PRERETIREROLE(REQUIREDPROPERTY, "preDeprecationRole"), PRERETIRESOURCEROLE(
                REQUIREDPROPERTY, "preDeprecationSourceRole"), PRERETIRESTATE(REQUIREDPROPERTY, "preDeprecationState"), CONCEPTSTATUS(
                REQUIREDPROPERTY, "concept-status"), CURATORIALAUTHORITY(REQUIREDPROPERTY, "curatorial-authority"), MERGERETIRE(
                REQUIREDPROPERTY, "merge-retire"), MERGESURVIVING(REQUIREDPROPERTY, "merge-surviving"), MERGETO(
                REQUIREDPROPERTY, "merge-to"), SPLITFROM(REQUIREDPROPERTY, "split-from"), REQUIREDCONCEPTS(CONFIG,
                "required-concepts"), PREMERGEDIDENTIFIER(REQUIREDCONCEPTS, "premerged-identifier"), PRERETIREDIDENTIFIER(
                REQUIREDCONCEPTS, "preretired-identifier"), DEPRECATEDIDENTIFIER(REQUIREDCONCEPTS,
                "deprecated-identifier"), WORKFLOW(CONFIG, "workflow"), BASEURL(WORKFLOW, "base-url"), TOPLEVEL(
                WORKFLOW, "top-level-page"), PREFIX(
                        WORKFLOW, "prefix"), AUTHORIZATION(CONFIG, "authorization"), ENTRY(AUTHORIZATION, "entry"), AUTHORITY(
                ENTRY, "authority"), USERNAME(ENTRY, "username"), NONTRANSFERPROP(CONFIG,"non-transferable-property"),
                NONTPROPIDENT(NONTRANSFERPROP,"identifier");

        /**
         * parent states are legitimate parent elements in the doc
         */
        State parents[] = new State[] {};

        /**
         * tag name
         */
        String name = null;

        /**
         * A convenience constructor as often states have a single parent
         * 
         * @param parent
         * @param name
         */
        State(State parent, String name) {
            this.parents = new State[] { parent };
            this.name = name;
        }

        /**
         * Constructor for State with a multiple parent states.
         * 
         * @param parents
         * @param name
         */
        State(State[] parents, String name) {
            this.parents = parents;
            this.name = name;
        }

        public void setParents(State[] parents) {
            this.parents = parents;
        }

        /**
         * Checks whether it is valid to transition to the specified state from this state.
         * 
         * @param toState
         * @return true if valid, false otherwise.
         */
        public boolean checkValid(State toState) {
            if (this.equals(toState))
                return true;

            for (int i = 0; i < toState.parents.length; i++) {
                if (toState.parents[i].equals(this)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * 
         * @return the tag name associated with this State
         */
        public String getTagName() {
            return name;
        }

        static Map<String, State> tagLookup = new HashMap<String, State>();

        /*
         * This static code executes after the enums have been constructed, and only once.
         * 
         * Because of order of execution when initializing an enum, you can't call static functions in an enum
         * constructor. (They are constructed before static initialization).
         * 
         * Instead, we use a static initializer to populate the lookup hashmap after all the enums are constructed.
         */
        static {
            for (State state : State.values()) {
                registerState(state);
            }
        }

        /**
         * Maps a tag name to a State
         * 
         * @param tagName
         * @return the ParserState for that tag.
         */
        public static State lookupStateForTag(String tagName) {
            return tagLookup.get(tagName);
        }

        private static void registerState(State state) {
            tagLookup.put(state.name, state);
        }

    } // end enum

    private Logger log = null;

    private boolean tracing = false;

    private Config config = null;

    /**
     * XML parser doing the actual work.
     */
    XmlPullParser parser = null;

    /**
     * Internal parser state variable.
     */
    State parserState = State.EMPTY;

    /**
     * State stack.
     */
    Stack<State> stateStack = new Stack<State>();

    /**
     * construct a ConfigFileParser parser, using the passed in Logger for errors
     * 
     * @param parser an XmlPullParser
     * @param l a Logger
     */
    public ConfigFileParser(XmlPullParser parser, Logger l) {
        this.parser = parser;
        log = l;
    }

    private void initParser(String fname) {

        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new FileInputStream(fname), null);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ConfigFileParser(String fname, Config cfg, Logger l) {
        log = l;
        config = cfg;
        initParser(fname);

    }

    private void error(String msg) {
        log.log(Level.SEVERE, msg);
    }

    private void trace(String msg) {
        log.log(Level.INFO, msg);
    }

    /**
     * perform the actual parse of the document
     * 
     * @throws XmlPullParserException
     * @throws IOException
     * @throws WorkflowException
     */
    public void processDocument() throws XmlPullParserException, IOException, EditTabException {

        int eventType = parser.getEventType();
        do {
            switch (eventType) {

            case XmlPullParser.START_DOCUMENT:
                // debug("Start document");
                break;
            case XmlPullParser.END_DOCUMENT:
                // debug("End document");
                break;
            case XmlPullParser.START_TAG:
                processStartElement();
                break;
            case XmlPullParser.END_TAG:
                processEndElement();
                break;
            case XmlPullParser.TEXT:
                processText();
                break;
            }

            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

    }

    /**
     * consolidates error processing into a single message. Take the message add the parser line number and the current
     * state. Log the message and throw it in an InfluenceException
     * 
     * @param message
     * @throws EditTabException
     */
    private void processError(String message) throws EditTabException {
        String s = message + getParserPosition() + ".\n" + "Current parser state: " + parserState.toString();
        error(s);
        throw new EditTabException(s);

    }

    public void processStartElement() throws EditTabException, XmlPullParserException {

        String tagName = parser.getName();

        State newState = State.lookupStateForTag(tagName);

        if ((newState != null) && (parserState.checkValid(newState))) {
            stateStack.push(parserState);
            parserState = newState;
        } else {
            // invalid transition
            String message = null;
            if (newState == null) {
                message = "Couldn't find a valid state";

            } else {
                message = "Got illegal state transition in parser, attempt to transition to \n" + newState.toString();
            }
            this.processError(message);
        }

        // dispatch to handling method
        dispatch();
    }

    public void processEndElement() throws XmlPullParserException, EditTabException {
        dispatch();
        parserState = stateStack.pop();
    }

    public void processText() throws XmlPullParserException, EditTabException {
        dispatch();
    }

    private void dispatch() throws XmlPullParserException, EditTabException {

        if (tracing) {
            if (parser.getEventType() == XmlPullParser.START_TAG || parser.getEventType() == XmlPullParser.END_TAG) {
                trace("Dispatching " + evtToName(parser.getEventType()) + " to state " + parserState.toString()
                        + " (tag: " + parser.getName() + ")");
            } else {
                trace("Dispatching " + evtToName(parser.getEventType()) + " to state " + parserState.toString());
            }
        }
        
        
		if (parserState == CONFIG) {

			if (parser.getEventType() == XmlPullParser.START_TAG) {
				
				
				
				config.setIdBy(parser.getAttributeValue(null, "idBy"));
				
				
				config.setUseRules(parser.getAttributeValue(null,"useRules"));

			}
		}
		

        State parent = parserState.parents[0];

        if (parent.equals(REQUIREDPROPERTY)) {
            processRequiredProperty();
            return;

        } else if (parent.equals(REQUIREDCONCEPTS)) {
            this.processRequiredConcepts();
            return;

        } else if (parent.equals(WORKFLOW)) {
            processWorkflow();
            return;

        } else if (parent.equals(ENTRY)) {
            processAuthorizationEntry();
            return;
        } else if (parent.equals(NONTRANSFERPROP)) {
        	processNonTransferProp();
        	return;
        }

        switch (parserState) {
        case PANELLABEL:
        case NAME:
        case LABEL:
        case TYPE:
        case ELEMENT:
        case ENUM:
        case VALUE:
        case OWLPROPERTY:
        case FIELD:
            processPanel();
            break;

        case MENUDISABLE:
        case MENUBAR:
        case MENUITEM:
            processMenuDisable();
            break;

        default:
            break;
        }
    }

    /**
     * At the beginning of parse create an panel object
     * 
     * @throws XmlPullParserException
     * @throws EditTabException
     */
    private void processPanel() throws XmlPullParserException, EditTabException {
        if (parserState.equals(NAME)) {
        	if (parser.getEventType() == XmlPullParser.START_TAG) {
        		root_tag = parser.getAttributeValue(0);
        	}
        }
        
        if (parser.getEventType() == XmlPullParser.TEXT) {
            if (parserState.equals(NAME)) {
            	String pn = parser.getText();
                initAnnData(pn, panel_label, root_tag);
                config.addComplexProperty(pn);

            }
            if (parserState.equals(PANELLABEL)) {
                panel_label = parser.getText();
            }
            if (parserState.equals(ELEMENT)) {
                // ignore language element for now
                // TODO: Bob talk to Gilberto
                currComp = currCad.createComponent(parser.getText());
                currComp.setIsRequired(currCompRequired);
                // reset
                currCompRequired = false;

            }
            if (parserState.equals(TYPE)) {
                String ts = parser.getText();
                CompType tv = null;
                if (ts.equals("text-line")) {
                    tv = CompType.JTEXTFIELD;
                } else if (ts.equals("text-paragraph")) {
                    tv = CompType.JTEXTAREA;
                }

                currComp.setType(tv);
                // addFunkyValue(tv, Funky.COMPTYPE);

            }

            if (parserState.equals(LABEL)) {

                String ts = parser.getText();
                currComp.setLabel(ts);

            }

            if (parserState.equals(ENUM)) {
                if (parser.getText().equals("$language")) {
                    String[] ls = config.getSupportedLanguages();
                    for (int i = 0; i < ls.length; i++) {
                        currComp.addComboBoxValue(ls[i]);
                    }

                }
            }

            if (parserState.equals(VALUE)) {
                String ts = parser.getText();
                if (ts.equals("$date")) {
                    currComp.setType(CompType.DATE);
                    // addFunkyValue("Date", Funky.COMPTYPE);
                } else if (ts.equals("$username")) {
                    currComp.setType(CompType.USER);
                    // addFunkyValue("User", Funky.COMPTYPE);

                } else if (inEnum) {

                    currComp.addComboBoxValue(ts);
                    // addFunkyValue(ts, Funky.COMBODAT);

                }
            }
        } else if (parser.getEventType() == XmlPullParser.END_TAG) {
            if (parserState.equals(ENUM)) {
                inEnum = false;
            } else if (parserState.equals(OWLPROPERTY)) {

                /**
                 * trimCads();
                 * 
                 * CustomizedAnnotationData cad = new CustomizedAnnotationData( annotation_name, tagname_data,
                 * component_type_data, combobox_data, default_value_data); config.addCAD(annotation_name, cad);
                 */

            } else if (parserState.equals(FIELD)) {

            }
        } else if (parser.getEventType() == XmlPullParser.START_TAG) {
            if (parserState.equals(ENUM)) {
                inEnum = true;
                currComp.setType(CompType.COMBOBOX);
                if (parser.getAttributeCount() > 0) {
                    if (parser.getAttributeName(0).equals("default")) {
                        currComp.setDefaultValue(parser.getAttributeValue(0));
                    }
                }
            } else if (parserState.equals(ELEMENT)) {
            	if (parser.getAttributeCount() > 0) {
                    if (parser.getAttributeName(0).equals("required")) {
                        currCompRequired = parser.getAttributeValue(0).equalsIgnoreCase("true");
                        	
                        
                    }
                }
            	
            }

        }

    }

    private boolean inEnum = false;

    private void initAnnData(String name, String panelLabel, String root_tag) {
        
        if (name.indexOf(":") > 0) {
            NCIEditTab.fixComplexName(name);
            
        }
        
        
        currCad = new CustomizedAnnotationData(name, panelLabel);
        currCad.setRootTag(root_tag);
        config.addCAD(name, currCad);
        

    }

    CustomizedAnnotationData currCad = null;

    CustomizedAnnotationData.CadComp currComp = null;
    private boolean currCompRequired = false;

    private String base_url = null;

    private String top_level_page = null;
    
    private String panel_label = null;
    private String root_tag = null;
    
    private String prefix_name = null;

    private void processWorkflow() throws XmlPullParserException, EditTabException {
    	
    	if (parser.getEventType() == XmlPullParser.START_TAG) {
    		if (parserState == PREFIX) {
    			prefix_name = parser.getAttributeValue(0);
    		}
    	}

        if (parser.getEventType() == XmlPullParser.TEXT) {
            if (parserState.equals(BASEURL)) {
                base_url = parser.getText();
            } else if (parserState.equals(TOPLEVEL)) {
                top_level_page = parser.getText();
                config.setSMWString(base_url, top_level_page);
                
            } else if (parserState.equals(PREFIX)) {
            	config.addSMWPrefix(prefix_name, parser.getText());
            	
            	
            }
        }

    }

    private String authority = null;

    private String username = null;

    private void processAuthorizationEntry() throws XmlPullParserException, EditTabException {

        if (parser.getEventType() == XmlPullParser.TEXT) {
            if (parserState.equals(AUTHORITY)) {

                authority = parser.getText();

            } else if (parserState.equals(USERNAME)) {

                username = parser.getText();
                config.addAuthority(authority, username);
                // TODO: need to take into account the curatorial authority
                // attribute

            }
        }

    }
    
    private void processNonTransferProp() throws XmlPullParserException, EditTabException {
    	if (parser.getEventType() == XmlPullParser.TEXT) {
            config.getNonTransferProps().add(parser.getText());
        }
    	
    }

    private String prop_name = null;

    //private String code = null;

    private void processRequiredProperty() throws XmlPullParserException, EditTabException {

        
        if (parser.getEventType() == XmlPullParser.TEXT) {
            prop_name = parser.getText();
            config.addRequiredProp(prop_name);

        } else if (parser.getEventType() == XmlPullParser.END_TAG) {
        	String s = prop_name;
        	
            switch (parserState) {
            
            case PREFLABEL:
                NCIEditTab.PREFLABEL = s;
                break;
            case ALTLABEL:
                NCIEditTab.ALTLABEL = s;
                break;
            case DEFINITION:
                NCIEditTab.DEFINITION = s;
                break;
            //case ALT_DEFINITION:
               // NCIEditTab.ALT_DEFINITION = s;
                //break;                
            case SCOPENOTE:
                NCIEditTab.SCOPENOTE = s;
                break;
            case EDITORIALNOTE:
                NCIEditTab.EDITORIALNOTE = s;
                break;
            case PRERETIRECHILDCONCEPT:
                NCIEditTab.PREDEPRECATIONCHILDCONCEPT = s;
                break;
            case PRERETIREPARENTCONCEPT:
                NCIEditTab.PREDEPRECATIONPARENTCONCEPT = s;
                break;
            case PRERETIREROLE:
                NCIEditTab.PREDEPRECATIONROLE = s;
                break;
            case PRERETIRESOURCEROLE:
                NCIEditTab.PREDEPRECATIONSOURCEROLE = s;
                break;
            case PRERETIREASSOC:
                NCIEditTab.PREDEPRECATIONASSOC = s;
                break;
            case PRERETIRESOURCEASSOC:
                NCIEditTab.PREDEPRECATIONSOURCEASSOC = s;
                break;
            case PRERETIRESTATE:
                NCIEditTab.PREDEPRECATIONSTATE = s;
                break;
            case CONCEPTSTATUS:
                NCIEditTab.CONCEPTSTATUS = s;
                break;
            case CURATORIALAUTHORITY:
                NCIEditTab.CURATORIALAUTHORITY = s;
                break;
            case MERGERETIRE:
                NCIEditTab.MERGERETIRE = s;
                break;
            case MERGESURVIVING:
                NCIEditTab.MERGESURVIVING = s;
                break;
            case MERGETO:
                NCIEditTab.MERGETO = s;
                break;
            case SPLITFROM:
                NCIEditTab.SPLITFROM = s;
                break;

            }
        }

    }

    private String con_name = null;

    private void processRequiredConcepts() throws XmlPullParserException, EditTabException {

        if (parser.getEventType() == XmlPullParser.TEXT) {
            con_name = parser.getText();
            config.addNonEditableConcept(con_name);
        }

        switch (parserState) {

        case PREMERGEDIDENTIFIER:
            NCIEditTab.PREMERGED_CONCEPTS = con_name;
            break;
        case PRERETIREDIDENTIFIER:
            NCIEditTab.PRERETIRED_CONCEPTS = con_name;
            break;
        case DEPRECATEDIDENTIFIER:
            NCIEditTab.RETIRED_CONCEPTS = con_name;
            break;

        }

    }

    private String menubar = null;

    private String menuitem = null;

    private void processMenuDisable() throws XmlPullParserException, EditTabException {
        if (parser.getEventType() == XmlPullParser.TEXT) {
            if (parserState.equals(MENUBAR)) {
                menubar = parser.getText();
            } else if (parserState.equals(MENUITEM)) {
                menuitem = parser.getText();
                config.disableMenuItems(menubar, menuitem);
            }
        }

    }

    private String evtToName(int eventType) {

        if (eventType == XmlPullParser.START_DOCUMENT) {
            return "START_DOCUMENT";
        } else if (eventType == XmlPullParser.END_DOCUMENT) {
            return "END_DOCUMENT";
        } else if (eventType == XmlPullParser.START_TAG) {
            return "START_TAG";
        } else if (eventType == XmlPullParser.END_TAG) {
            return "END_TAG";
        } else if (eventType == XmlPullParser.TEXT) {
            return "TEXT";
        }

        return "UNKNOWN_EVENT";
    }

    private final String getParserPosition() {
        return "(line: " + parser.getLineNumber() + " col: " + parser.getColumnNumber() + ")";
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        XmlPullParser parser = null;

        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new FileInputStream(args[0]), null);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigFileParser cfParser = new ConfigFileParser(parser, Logger.getAnonymousLogger());
        try {
            cfParser.processDocument();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO Auto-generated method stub

    }

}
