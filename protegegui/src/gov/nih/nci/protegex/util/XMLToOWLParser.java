package gov.nih.nci.protegex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.tree.TreeItem;
import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.workflow.util.BaseXmlPullParser;

public class XMLToOWLParser extends BaseXmlPullParser {
	/**
	 * This enum serves a couple of purposes. It tracks the state of the parser.
	 * If an element can only have certain children in the document, the element
	 * will belong to those child states parents. As the parser transitions from
	 * state to state the transitions can be validated.
	 * 
	 * It also associates the labels with each state.
	 */
	private enum State {
		EMPTY(new State[] {}, ""), RDF(EMPTY, "rdf:RDF"), RAW(RDF, "raw"), OWL_CLASS(
				RDF, "owl:Class"), OWL_CLASS_NAME(OWL_CLASS, "owl-class-name"), OWL_CLASS_CODE(
				OWL_CLASS, "owl-class-code"), PROPOSAL_TYPE(OWL_CLASS,
				"proposal-type"), IN_SCHEME(OWL_CLASS, "in-scheme"), SYNONYM(
				OWL_CLASS, "SYNONYM"), FULL_SYN(OWL_CLASS, "FULL_SYN"), FULL_SYN_COMP_TERM(FULL_SYN, "ComplexTerm"),
				FULL_SYN_TERM_CODE(
				FULL_SYN_COMP_TERM, "term-code"), FULL_SYN_TERM_NAME(FULL_SYN_COMP_TERM,
				"term-name"), FULL_SYN_TERM_GROUP(FULL_SYN_COMP_TERM,
				"term-group"), FULL_SYN_TERM_SOURCE(FULL_SYN_COMP_TERM,
				"term-source"), FULL_SYN_SOURCE_CODE(FULL_SYN_COMP_TERM,
				"source-code"),
				ALT_DEFINITION(OWL_CLASS, "ALT_DEFINITION"),
				DEFINITION(OWL_CLASS, "DEFINITION"), DEF_COMP_DEF(new State[] {DEFINITION, ALT_DEFINITION}, "ComplexDefinition"),
				DEF_SOURCE(
				DEF_COMP_DEF, "def-source"), DEF_DEFINITION(DEF_COMP_DEF,
				"def-definition"), DEF_REVIEWER(DEF_COMP_DEF, "Definition_Reviewer_Name"), DEF_ATTRIBUTE(
				DEF_COMP_DEF, "def-attribute"), DEF_DATE(DEF_COMP_DEF, "Definition_Review_Date"),
				PROPERTY(OWL_CLASS,
				"property"), PROPERTY_CODE(PROPERTY, "property-code"), PROPERTY_VALUE(
				PROPERTY, "property-value"), PARENT(OWL_CLASS, "PARENT"), PARENT_NAME(
				PARENT, "parent-name"), PARENT_CODE(PARENT, "parent-code"), ASSOCIATION(
				OWL_CLASS, "ASSOCIATION"), ASSOC_PART(ASSOCIATION, "assoc-part"), ASSOC_PART_CODE(
				ASSOCIATION, "assoc-part-code"), ASSOC_NAME(ASSOCIATION,
				"assoc-name"), ASSOC_NAME_CODE(ASSOCIATION, "assoc-name-code"), ASSOC_QUAL(
				ASSOCIATION, "assoc-qual"), ASSOC_VIEW(ASSOCIATION,
				"assoc-view"), GO_ANNOTATION(OWL_CLASS, "GO_Annotation"), GO_TERM(
				GO_ANNOTATION, "go-term"), GO_SOURCE_DATE(GO_ANNOTATION,
				"go-source-date"), GO_SOURCE(GO_ANNOTATION, "go-source"), GO_EVI(
				GO_ANNOTATION, "go-evi"), GO_ID(GO_ANNOTATION, "go-id"), ;

		/**
		 * parent states are legitimate parent elements in the doc
		 */
		private State _parents[] = new State[] {};

		/**
		 * tag name
		 */
		private String _name = null;

		/**
		 * A convenience constructor as often states have a single parent
		 * 
		 * @param parent
		 * @param name
		 */
		State(State parent, String name) {
			this._parents = new State[] { parent };
			this._name = name;
		}

		/**
		 * Constructor for State with a multiple parent states.
		 * 
		 * @param parents
		 * @param name
		 */
		State(State[] parents, String name) {
			this._parents = parents;
			this._name = name;
		}

		/**
		 * Checks whether it is valid to transition to the specified state from
		 * this state.
		 * 
		 * @param toState
		 * @return true if valid, false otherwise.
		 */
		public boolean checkValid(State toState) {
			if (this.equals(toState))
				return true;

			for (int i = 0; i < toState._parents.length; i++)
				if (toState._parents[i].equals(this))
					return true;
			return false;
		}

		/**
		 * Returns the tag name associated with this State.
		 * 
		 * @return the tag name associated with this State.
		 */
		public String getTagName() {
			return _name;
		}

		private static Map<String, State> tagLookup = new HashMap<String, State>();

		/*
		 * This static code executes after the enums have been constructed, and
		 * only once.
		 * 
		 * Because of order of execution when initializing an enum, you can't
		 * call static functions in an enum constructor. (They are constructed
		 * before static initialization).
		 * 
		 * Instead, we use a static initializer to populate the lookup hashmap
		 * after all the enums are constructed.
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

		/**
		 * Registers the state.
		 * 
		 * @param state
		 *            The state.
		 */
		private static void registerState(State state) {
			tagLookup.put(state._name, state);
		}
	} // end enum

	/** List of proposal types handled by the Wiki. */
	public static enum ProposalType {
		Unknown(), Structured(), Unstructured(), NewConcept();

		/**
		 * Returns the corresponding proposal type from the text value.
		 * 
		 * @param type
		 *            Type as a string.
		 * @return the corresponding proposal type from the text value.
		 */
		public static ProposalType getType(String type) {
			type = type.toUpperCase();
			if (type.equals("STRUCTURED"))
				return Structured;
			if (type.equals("UNSTRUCTURED"))
				return Unstructured;
			if (type.equals("NEWCONCEPT"))
				return NewConcept;
			return Unknown;
		}
	}

	/**
	 * OWLModel is the remote model, passed in to the constructor. This is used
	 * to look up existing concepts.
	 */
	private static String[] _prefixes = null;

	private OWLModel _model = null;

	private OWLWrapper _wrapper = null;

	private State _parserState = State.EMPTY;

	private Stack<State> _stateStack = new Stack<State>();

	private FullSyn _fullSyn = null;

	private Definition _definition = null;

	private Property _property = null;

	private Association _association = null;

	private GO_Annotation _goAnnotation = null;

	private Stack<String> _ignoreTags = new Stack<String>();

	private TreeItems _items = null;

	private ConceptInfo _info = null;

	private ProposalType _proposalType = ProposalType.Unknown;

	/**
	 * Returns the code property for cls.
	 * 
	 * @return the code property for cls.
	 */
	public String getCode() {
		return _info.code;
	}

	/**
	 * Returns the class name;
	 * 
	 * @return the class name;
	 */
	public String getClassName() {
		return _info.name;
	}

	/**
	 * Instantiates this class.
	 * 
	 * @param model
	 *            The OWLModel.
	 */
	public XMLToOWLParser(OWLModel model) {
		_model = model;
		_wrapper = OWLWrapper.createInstance(_model);
		_prefixes = getPrefixes(_model);
	}

	/**
	 * Returns a list of prefixes.
	 * 
	 * @param model
	 *            The OWLModel.
	 * @return a list of prefixes.
	 */
	private static String[] getPrefixes(OWLModel model) {
		if (_prefixes != null)
			return _prefixes;

		NamespaceManager nsMgr = model.getNamespaceManager();
		Collection<String> prefixes = nsMgr.getPrefixes();
		String[] list = prefixes.toArray(new String[0]);
		_prefixes = list;
		return list;
	}

	/**
	 * Process the XML string contain concept class information.
	 * 
	 * @param xmlPacket
	 *            The XML string.
	 * @return the TreeItems containing the concept properties.
	 */
	public TreeItems processXml(String xmlPacket) {
		init(xmlPacket);
		processDocument();
		return _items;
	}

	/**
	 * Initializes local variables.
	 * 
	 * @param xmlPacket
	 *            The XML String.
	 */
	private void init(String xmlPacket) {
		_xmlPacket = xmlPacket;
		_parser = getParser(xmlPacket);
		_parserState = State.EMPTY;
		_items = new TreeItems();
		_warning.delete(0, _warning.length());
		_ignoreTags.clear();
		_info = new ConceptInfo();
	}

	/**
	 * Returns the parser state as a string. Note: Used by the base class
	 * because each specific parser contains a different State enum.
	 */
	protected String getParserStateString() {
		return _parserState.toString();
	}

	/**
	 * Returns the parser state's tag name. Note: Used by the base class because
	 * each specific parser contains a different State enum.
	 */
	protected String getParserStateTagName() {
		return _parserState.getTagName();
	}

	/**
	 * Pushes the current state to the parser stack. Note: Used by the base
	 * class because each specific parser contains a different State enum.
	 * 
	 * @return true if successful.
	 * @throws XmlPullParserException
	 * @throws Exception
	 */
	protected boolean pushCurrentStateToStack() throws XmlPullParserException,
			Exception {
		String tagName = _parser.getName();
		State newState = State.lookupStateForTag(tagName);

		if ((newState != null) && (_parserState.checkValid(newState))) {
			_stateStack.push(_parserState);
			_parserState = newState;
			return true;
		} else {
			log("Ignoring Invalid XML Tag: " + tagName + " "
					+ getParserPosition());
			_ignoreTags.add(tagName);
			return false;
		}
	}

	/**
	 * Pops the current state from the stack. Note: Used by the base class
	 * because each specific parser contains a different State enum.
	 * 
	 * @return true if successful.
	 */
	protected boolean popStateFromStack() {
		if (_ignoreTags.size() > 0) {
			_ignoreTags.pop();
			return false;
		}
		_parserState = _stateStack.pop();
		return true;
	}

	/**
	 * Process the start element.
	 * 
	 * @throws XmlPullParserException
	 * @throws Exception
	 */
	public void processStartElement() throws XmlPullParserException, Exception {
		pushCurrentStateToStack();
		if (_ignoreTags.size() > 0)
			return;

		switch (_parserState) {
		case FULL_SYN:
			_fullSyn = new FullSyn();
			break;
		case DEFINITION:
		case ALT_DEFINITION:
			_definition = new Definition();
			break;
		case PROPERTY:
			_property = new Property();
			break;
		case ASSOCIATION:
			_association = new Association();
			break;
		case GO_ANNOTATION:
			_goAnnotation = new GO_Annotation();
		}
	}

	/**
	 * Processes the end element.
	 * 
	 * @throws XmlPullParserException
	 * @throws Exception
	 */
	public void processEndElement() throws XmlPullParserException, Exception {
		if (_ignoreTags.size() > 0) {
			popStateFromStack();
			return;
		}

		try {
			switch (_parserState) {
			case FULL_SYN:
				addProperty(_parserState.getTagName(), _fullSyn);
				_fullSyn = null;
				break;
			case DEFINITION:
			case ALT_DEFINITION:
				addProperty(_parserState.getTagName(), _definition);
				_definition = null;
				break;
			case PROPERTY:
				addProperty(_property);
				_property = null;
				break;
			case ASSOCIATION:
				addAssociationOrRestriction(_association);
				_association = null;
				break;
			case GO_ANNOTATION:
				addProperty(_parserState.getTagName(), _goAnnotation);
				_goAnnotation = null;
				break;
			case OWL_CLASS:
				createConceptItem();
				break;
			}
		} catch (Exception e) {
			warningMsg("* " + e.getClass().getSimpleName() + " in "
					+ getClass().getSimpleName());
			warningMsg("  " + e.getMessage());
		}
		popStateFromStack();
	}

	/**
	 * Adds a property to the newly created OWLNamedClass.
	 * 
	 * @param name
	 *            The property name.
	 * @param value
	 *            The property value.
	 */
	private void addProperty(String name, String value) {
		TreeItem item = createPropertyItem(_wrapper, name, value);
		if (item != null) {
		_items.add(item);
		}
	}

	/**
	 * Adds a property to the newly created OWLNamedClass.
	 * 
	 * @param name
	 *            The property name.
	 * @param xmlClass
	 *            The xmlClass that contains the property values.
	 */
	private void addProperty(String name, XmlClass xmlClass) {
		TreeItem item = createPropertyItem(_wrapper, name, xmlClass.value());
		_items.add(item);
	}

	/**
	 * Adds a property to the newly created OWLNamedClass.
	 * 
	 * @param property
	 *            The property.
	 */
	private void addProperty(Property property) {
		RDFProperty prop = ClsUtil.findRDFProperty(_model, property.code);
		if (prop == null)
			prop = _wrapper.getRDFProperty(property.code);
		if (prop == null) {
			warningMsg("* Can not find RDFProperty for property: "
					+ property.code + ".");
			return;
		}
		TreeItem item = createPropertyItem(prop, property.value);
		_items.add(item);
		storePreferredName(prop, property.value);
	}

	/**
	 * Stores Preferred Name value.
	 * 
	 * @param property
	 *            The RDFProperty.
	 * @param value
	 *            The value.
	 */
	private void storePreferredName(RDFProperty property, String value) {
		String propName = property.getBrowserText();
		if (!propName.equals(NCIEditTab.PREFLABEL))
			return;
		_info.pt = value;
	}

	/**
	 * Subclasses the newly created OWLNamedClass to a specific parent.
	 * 
	 * @param parentName
	 *            The parent name.
	 * @throws Exception
	 */
	private void subClass(String parentName) {
		_info.parentName = parentName;
	}

	/**
	 * Subclasses the newly created OWLNamedClass to a specific parent.
	 * 
	 * @param parentCode
	 *            The parent code.
	 */
	private void subClassByCode(String parentCode) {
		try {
			TreeItem item = createSubclassItemByCode(_wrapper, parentCode);
			_items.add(item);
		} catch (Exception e) {
			warningMsg("* " + e.getMessage());
		}
	}

	/**
	 * Retrieves a concept by code. If it fails, it retrieves it by name.
	 * 
	 * @param code
	 *            The concept code.
	 * @param name
	 *            The concept name.
	 * @return the concept.
	 */
	private OWLNamedClass getCls(String code, String name) {
		OWLNamedClass cls = ClsUtil.getConceptByCode(_model, code);
		if (cls == null)
			cls = _wrapper.getOWLNamedClass(name);
		return cls;
	}

	/**
	 * Returns true if RDFProperty is an association.
	 * 
	 * @param property
	 *            The RDFProperty.
	 * @return true if RDFProperty is an association.
	 */
	private boolean isAssociation(RDFProperty property) {
		return property.isAnnotationProperty() && property.hasObjectRange()
				&& property instanceof DefaultOWLObjectProperty;
	}

	/**
	 * Adds an association or restriction to the newly created OWLNamedClass.
	 * 
	 * @param association
	 *            The association.
	 * @throws Exception
	 */
	private void addAssociationOrRestriction(Association association)
			throws Exception {
		// Note: Currently, the Wiki handles both associations and
		// restrictions as associations. For restrictions, the
		// Wiki passes the property code, but it does not for
		// associations.

		// Note: Assuming association if there is no property code.
		String partCode = association.partCode;
        

		RDFProperty prop = ClsUtil.findRDFProperty(_model, partCode);
		if (prop == null) {
			warningMsg("Can not find property for " + partCode + ".");
			return;
		}

		if (isAssociation(prop))
			addAssociation(association, prop);
		else
			addRestriction(association, prop);
	}

	/**
	 * Adds an association to the newly created OWLNamedClass.
	 * 
	 * @param association
	 *            The association.
	 * @throws Exception
	 */
	private void addAssociation(Association association, RDFProperty prop) throws Exception {
		OWLNamedClass filler = getCls(association.nameCode, association.name);
		if (filler == null) {
			warningMsg("* Could not add association: " + association.part);
			warningMsg("  Concept " + association.name
					+ " could not be retrieved.");
			return;
		}

        

		TreeItem item = createAssociationItem(prop, filler);
		_items.add(item);
	}

	/**
	 * Adds a restriction to the newly created OWLNamedClass.
	 * 
	 * @param association
	 *            The association.
	 * @throws Exception
	 */
	private void addRestriction(Association association, RDFProperty prop) throws Exception {
		OWLNamedClass filler = getCls(association.nameCode, association.name);
		if (filler == null) {
			warningMsg("* Could not add restriction: " + association.toString());
			warningMsg("  Concept " + association.name
					+ " could not be retrieved.");
			return;
		}

		
		String metaClsName = association.qual;
		String propName = prop.getName();
		String fillerName = filler.getPrefixedName();

		TreeItem item = createRestrictionItem(_wrapper, metaClsName, propName,
				fillerName);
		_items.add(item);
	}

	/**
	 * Returns the proposal type.
	 * 
	 * @return the proposal type.
	 */
	public ProposalType getProposalType() {
		return _proposalType;
	}

	/**
	 * Processes the text value.
	 * 
	 * @throws XmlPullParserException
	 * @throws Exception
	 */
	public void processText() throws XmlPullParserException, Exception {
		if (false)
			debugParser();
		if (_parser.getEventType() != XmlPullParser.TEXT)
			return;

		String text = _parser.getText().trim();
		if (text.length() <= 0)
			return;

		int size = _ignoreTags.size();
		if (size > 0) {
			String tag = _ignoreTags.get(size - 1);
			log("Ignoring(" + tag + ") value: " + text);
			return;
		}
		switch (_parserState) {
		case OWL_CLASS_NAME:
			_info.name = text;
			break;
		case OWL_CLASS_CODE:
			storeCode(text);
			break;
		case PROPOSAL_TYPE:
			_proposalType = ProposalType.getType(text);
			break;
		case IN_SCHEME:
			break;
		case SYNONYM:
			addProperty("Synonym", text);
			break;
		case FULL_SYN_TERM_CODE:
			_fullSyn.code = text;
			break;
		case FULL_SYN_TERM_NAME:
			_fullSyn.termName = text;
			break;
		case FULL_SYN_TERM_GROUP:
			_fullSyn.termGroup = text;
			break;
		case FULL_SYN_TERM_SOURCE:
			_fullSyn.termSource = text;
			break;
		case FULL_SYN_SOURCE_CODE:
			_fullSyn.sourceCode = text;
			break;
		case DEF_SOURCE:
			_definition.source = text;
			break;
		case DEF_DEFINITION:
			_definition.definition = text;
			break;
		case DEF_REVIEWER:
			_definition.reviewerName = text;
			break;
		case DEF_ATTRIBUTE:
			_definition.attribute = text;
			break;
		case DEF_DATE:
			_definition.reviewDate = text;
			break;
		
		case PROPERTY_CODE:
			_property.code = text;
			break;
		case PROPERTY_VALUE:
			_property.value = text;
			break;
		case PARENT_NAME:
			subClass(text);
			break;
		case PARENT_CODE:
			subClassByCode(text);
			break;
		case ASSOC_PART:
			_association.part = text;
			break;
		case ASSOC_PART_CODE:
			_association.partCode = text;
			break;
		case ASSOC_NAME:
			_association.name = text;
			break;
		case ASSOC_NAME_CODE:
			_association.nameCode = text;
			break;
		case ASSOC_QUAL:
			_association.qual = text;
			break;
		case ASSOC_VIEW:
			_association.view = text;
			break;
		case GO_TERM:
			_goAnnotation.go_term = text;
			break;
		case GO_SOURCE_DATE:
			_goAnnotation.go_source_date = text;
			break;
		case GO_SOURCE:
			_goAnnotation.go_source = text;
			break;
		case GO_EVI:
			_goAnnotation.go_evi = text;
			break;
		case GO_ID:
			_goAnnotation.go_id = text;
			break;
		default:
			warning("processText: Ignoring " + _parserState.getTagName());
		}
	}

	/**
	 * Creates a concept TreeItem and inserts it as the first element of the
	 * TreeItems list.
	 */
	private void createConceptItem() {
		TreeItem item = null;
		if (_proposalType == ProposalType.Structured)
			item = createConceptItemByCode(_info.code, _info.pt);
		else if (_proposalType == ProposalType.NewConcept)
			item = createConceptItemByName(_info.name, _info.pt);

		if (item == null)
			return;
		_items.insertElementAt(item, 0);
	}

	/**
	 * Stores the code property.
	 * 
	 * @param code
	 *            The code.
	 */
	private void storeCode(String code) {
		_info.code = code;
		TreeItem item = createCodeItem(code);
		_items.add(item);
	}

	private class ConceptInfo {
		String code = "";

		String name = "";

		String pt = "";

		String parentName = "";
	}

	/**
	 * Base class that helps format to an XML string.
	 * 
	 * @author yeed
	 */
	private abstract class XmlClass {
		public abstract String value();

		public String toString() {
			return value();
		}
	}

	private class FullSyn extends XmlClass {
		public String code = "";

		public String termName = "";

		public String termGroup = "";

		public String termSource = "";

		public String sourceCode = "";

		public String value() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("<ComplexTerm>");
			StringUtil.createXmlTag(buffer, true, "term-name", termName);
			StringUtil.createXmlTag(buffer, true, "term-group", termGroup);
			StringUtil.createXmlTag(buffer, true, "term-source", termSource);
			StringUtil.createXmlTag(buffer, true, "source-code", sourceCode);
			buffer.append("</ComplexTerm>");
			return buffer.toString();
		}
	}

	private class Definition extends XmlClass {
		public String definition = "";

		public String reviewDate = "";

		public String source = "";

		public String reviewerName = "";

		public String attribute = "";

		public String value() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("<ComplexDefinition>");
			StringUtil.createXmlTag(buffer, true, "def-definition", definition);
			StringUtil.createXmlTag(buffer, true, "Definition_Review_Date",
					reviewDate);
			StringUtil.createXmlTag(buffer, true, "def-source", source);
			StringUtil.createXmlTag(buffer, true, "Definition_Reviewer_Name",
					reviewerName);
			StringUtil.createXmlTag(buffer, true, "attr", attribute);
			buffer.append("</ComplexDefinition>");
			return buffer.toString();
		}
	}

	private class Property extends XmlClass {
		public String code = "";

		public String value = "";

		public String value() {
			return value;
		}
	}

	private class Association extends XmlClass {
		public String part = "";

		public String partCode = "";

		public String name = "";

		public String nameCode = "";

		public String qual = "";

		public String view = "";

		public String value() {
			return "";
		}

		public String toString() {
			String qualStr = qual + (qual.length() > 0 ? " " : "");
			return part + " (" + partCode + ") " + qualStr + name + " ("
					+ nameCode + ")";
		}
	}

	private class GO_Annotation extends XmlClass {
		public String go_term = "";

		public String go_source_date = "";

		public String go_source = "";

		public String go_evi = "";

		public String go_id = "";

		public String value() {
			StringBuffer buffer = new StringBuffer();
			StringUtil.createXmlTag(buffer, true, "go-term", go_term);
			StringUtil
					.createXmlTag(buffer, true, "source_date", go_source_date);
			StringUtil.createXmlTag(buffer, true, "go-source", go_source);
			StringUtil.createXmlTag(buffer, true, "go-evi", go_evi);
			StringUtil.createXmlTag(buffer, true, "go-id", go_id);
			return buffer.toString();
		}
	}

	/**
	 * Stores a concept name and preferred name within a TreeItem.
	 * 
	 * @param name
	 *            The concept name.
	 * @param pt
	 *            The concept preferred name.
	 * @return the created TreeItem.
	 */
	private TreeItem createConceptItemByName(String name, String pt) {
		OWLNamedClass cls = _wrapper.getOWLNamedClass(name);
		if (cls == null)
			return createConceptItem(name, pt);
		return createConceptItem(cls);
	}

	/**
	 * Stores a concept name and preferred name within a TreeItem.
	 * 
	 * @param code
	 *            The concept code.
	 * @param pt
	 *            The concept preferred name.
	 * @return the created TreeItem.
	 */
	private TreeItem createConceptItemByCode(String code, String pt) {
		OWLNamedClass cls = ClsUtil.getConceptByCode(_model, code);
		if (cls == null) {
			warningMsg("* Concept " + code + " does not exist.");
			return createConceptItem(code, pt);
		}
		return createConceptItem(cls);
	}

	/**
	 * Stores a concept name and preferred name within a TreeItem.
	 * 
	 * @param cls
	 *            The OWLNamedClass.
	 * @return the created TreeItem.
	 */
	private TreeItem createConceptItem(OWLNamedClass cls) {
		TreeItem item = new TreeItem();
		item.setType(TYPE_CONCEPT);
		item.setName(cls.getPrefixedName());
		item.setNameValue(cls.getBrowserText());
		// item.setCls(cls);
		return item;
	}

	/**
	 * Stores a concept name and preferred name within a TreeItem.
	 * 
	 * @param clsName
	 *            The class name.
	 * @param pt
	 *            The preferred name.
	 * @return the created TreeItem.
	 */
	private TreeItem createConceptItem(String clsName, String pt) {
		TreeItem item = new TreeItem();
		item.setType(TYPE_CONCEPT);
		item.setName(clsName);
		item.setNameValue(pt);
		return item;
	}

	/**
	 * Stores the concept code within a TreeItem.
	 * 
	 * @param code
	 *            The code.
	 * @return the concept code within a TreeItem.
	 */
	private TreeItem createCodeItem(String code) {
		if (code == null || code.length() <= 0)
			return null;

		TreeItem item = new TreeItem();
		item.setType(TYPE_PROPERTY);
		item.setName(NCIEditTab.CODE);
		item.setNameValue(NCIEditTab.CODE, code);
		return item;
	}

	/**
	 * Stores subclass property within a TreeItem.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param parentCode
	 *            The parent code.
	 * @return the created TreeItem.
	 * @throws Exception
	 */
	private TreeItem createSubclassItemByCode(OWLWrapper wrapper,
			String parentCode) throws Exception {

		OWLNamedClass parent = null;
		if (parentCode.equalsIgnoreCase("Thing")) {
			parent = _model.getOWLThingClass();
		} else {
			parent = ClsUtil.getConceptByCode(_model, parentCode);
		}
		if (parent == null) {
			String parentName = _info.parentName;
			if (parentName == null || parentName.length() <= 0)
				throw new Exception("Parent code " + parentCode
						+ " does not exist.");
			else
				throw new Exception("Parent " + parentName + " (" + parentCode
						+ ")" + " does not exist.");
		}
		return createSubclassItem(wrapper, parent);
	}

	/**
	 * Stores subclass property within a TreeItem.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param parent
	 *            The parent concept.
	 * @return
	 */
	private TreeItem createSubclassItem(OWLWrapper wrapper, OWLNamedClass parent) {
		TreeItem item = new TreeItem();
		item.setType(TYPE_PARENT);
		item.setName(NCIEditTab.SUBCLASSOF);
		item.setValue(parent.getPrefixedName());
		item.setNameValue(NCIEditTab.SUBCLASSOF, parent.getBrowserText());
		item.setCls(parent);
		return item;
	}

	/**
	 * Stores a concept property within a TreeItem.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param propertyName
	 *            The property name.
	 * @param value
	 *            The value.
	 * @return the created TreeItem.
	 */
	private TreeItem createPropertyItem(OWLWrapper wrapper,
			String propertyName, String value) {
		RDFProperty property = wrapper.getRDFProperty(propertyName);
		if (property != null) {
		return createPropertyItem(property, value);
		} else {
			return null;
		}
	}

	/**
	 * Stores a concept property within a TreeItem.
	 * 
	 * @param property
	 *            The RDFProperty.
	 * @param value
	 *            The value.
	 * @return the created TreeItem.
	 */
	private TreeItem createPropertyItem(RDFProperty property, String value) {
		if (value == null || value.length() <= 0)
			return null;

		TreeItem item = new TreeItem();
		item.setType(TYPE_PROPERTY);
		item.setProperty(property);
		String propName = property.getBrowserText();
		item.setName(propName);
		item.setNameValue(propName, value);
		return item;
	}

	/**
	 * Stores a concept restriction within a TreeItem.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param metaClsName
	 *            The meta class name.
	 * @param propertyName
	 *            The property name.
	 * @param fillerName
	 *            The filler name.
	 * @return the created TreeItem.
	 * @throws Exception
	 */
	private TreeItem createRestrictionItem(OWLWrapper wrapper,
			String metaClsName, String propertyName, String fillerName)
			throws Exception {

		// Note: Default metaClsName to "some" when this value is not
		// specified from the Wiki.
		if (metaClsName.length() <= 0)
			metaClsName = "some";

		OWLRestriction restriction = wrapper.createOWLRestriction(metaClsName,
				propertyName, fillerName);

		if (restriction == null) {
			warningMsg("* Could not add restriction: " + propertyName + " "
					+ metaClsName + " " + fillerName);
			return null;
		}

		return createRestrictionItem(restriction);
	}

	/**
	 * Stores a concept restriction within a
	 * 
	 * @param restriction
	 *            The OWLRestriction.
	 * @return the created TreeItem.
	 * @throws Exception
	 */
	private TreeItem createRestrictionItem(OWLRestriction restriction)
			throws Exception {
		TreeItem item = new TreeItem();
		item.setType(TYPE_RESTRICTION);
		item.setCls(restriction);
		String value = restriction.getBrowserText();
		item.setName(value);
		item.setNameValue(value);
		return item;
	}

	/**
	 * Stores a concept association within a TreeItem.
	 * 
	 * @param property
	 *            The RDFProperty.
	 * @param cls
	 *            The OWLNamedClass.
	 * @return the created TreeItem.
	 */
	private TreeItem createAssociationItem(RDFProperty property,
			OWLNamedClass cls) {
		String name = property.getBrowserText();

		TreeItem item = new TreeItem();
		item.setType(TYPE_ASSOCIATION);
		item.setName(name);
		item.setValue(cls.getPrefixedName());
		item.setNameValue(name, cls.getBrowserText());
		item.setProperty(property);
		// item.setCls(cls);
		return item;
	}
}
