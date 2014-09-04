/**
 * 
 */
package gov.nih.nci.protegex.workflow;


import java.io.IOException;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * The main job of this class is to parse an XML file that contains a work
 * assignment and construct the assignment object.
 * 
 * It makes use of the <a
 * href="http://www.extreme.indiana.edu/xgws/xsoap/xpp/">IU XPP Pull Parser</a>
 * and an interesting way of encoding some of the parsing state in an enum
 * constant for better error processing and modularity
 * 
 * 
 * @see gov.nih.nci.protegex.workflow.Assignment
 * @see gov.nih.nci.protegex.workflow.Workflow
 * 
 * @author Flora B. Workflow
 * 
 */
public class AssignmentParser {

	/**
	 * This enum serves a couple of purposes. It tracks the state of the parser.
	 * If an element can only have certain children in the document, the element
	 * will belong to those child states parents. As the parser transitions from
	 * state to state the transitions can be validated.
	 * 
	 * It also associates the labels with each state.
	 * 
	 */
	private enum State {

		EMPTY(new State[] {}, ""), 
		    ASSIGNMENT(EMPTY, "Assignment"), 
		        IDENTIFIER(ASSIGNMENT, "identifier"), 
		        DATECREATED(ASSIGNMENT, "dateCreated"), 
		        MANAGERID(ASSIGNMENT, "managerId"), 
		        MODELERID(ASSIGNMENT, "modelerId"), 
		        CURRENTSTATUS(ASSIGNMENT, "currentStatus"), 
		        LASTUPDATED(ASSIGNMENT, "lastUpdated"), 
		        WORKSOURCE(ASSIGNMENT, "workSource"), 
		        THEACTUALWORK(ASSIGNMENT, "theActualWork"), 
		        TASKDESCRIPTION(ASSIGNMENT, "taskDescription"), 
		        MODELERNOTE(ASSIGNMENT, "modelerNote"),
                PACKAGEID(ASSIGNMENT, "packageId"),
		        ;

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
		 * Checks whether it is valid to transition to the specified state from
		 * this state.
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

		private static void registerState(State state) {
			tagLookup.put(state.name, state);
		}

	} // end enum
	
	private enum TagType {DATE, INT, TEXT}

	private Logger log = null;

	private boolean tracing = false;

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
	 * This parser produce one Assignment
	 */
	private Assignment assignment = new Assignment();

	/**
	 * dateFormatter is used to parse strings into dates, and also to produce
	 * the strings from dates during serialization
	 * 
	 * @see Assignment#toXML()
	 */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

	/**
	 * 
	 * @return the Assignment object after the parse is complete
	 */
	public Assignment getAssignment() {
		return assignment;
	}

	/**
	 * construct an Assignment parser, using the passed in Logger for errors
	 * @param parser an XmlPullParser
	 * @param l a Logger
	 */
	public AssignmentParser(XmlPullParser parser, Logger l) {
		this.parser = parser;
		log = l;
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
	public void processDocument() throws XmlPullParserException, IOException,
			WorkflowException {

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
	 * consolidates error processing into a single message. Take the message add
	 * the parser line number and the current state. Log the message and throw
	 * it in an InfluenceException
	 * 
	 * @param message
	 * @throws WorkflowException
	 */
	private void processError(String message) throws WorkflowException {
		String s = message + getParserPosition() + ".\n"
				+ "Current parser state: " + parserState.toString();
		error(s);
		throw new WorkflowException(s);

	}

	public void processStartElement() throws WorkflowException,
			XmlPullParserException {

		String tagName = parser.getName();

		State newState = State.lookupStateForTag(tagName);

		if ((newState != null) && (parserState.checkValid(newState))) {
			stateStack.push(parserState);
			parserState = newState;
		} else {
			// invalid transition
			String message = "Got illegal state transition in parser, attempt to transition to \n"
					+ newState.toString();
			this.processError(message);
		}

		// dispatch to handling method
		dispatch();
	}

	public void processEndElement() throws XmlPullParserException,
			WorkflowException {
		dispatch();
		parserState = stateStack.pop();
	}

	public void processText() throws XmlPullParserException, WorkflowException {
		dispatch();
	}

	private void dispatch() throws XmlPullParserException, WorkflowException {

		if (tracing) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					|| parser.getEventType() == XmlPullParser.END_TAG) {
				trace("Dispatching " + evtToName(parser.getEventType())
						+ " to state " + parserState.toString() + " (tag: "
						+ parser.getName() + ")");
			} else {
				trace("Dispatching " + evtToName(parser.getEventType())
						+ " to state " + parserState.toString());
			}
		}

		switch (parserState) {
		case ASSIGNMENT:
			processAssignment();
			break;

		case IDENTIFIER:
			checkAndPull(TagType.INT);
			break;
			
		case DATECREATED:
			checkAndPull(TagType.DATE);
			break;

		case MANAGERID:
			checkAndPull(TagType.TEXT);
			break;

		case MODELERID:
			checkAndPull(TagType.TEXT);
			break;

		case CURRENTSTATUS:
			checkAndPull(TagType.TEXT);
			break;

		case LASTUPDATED:
			checkAndPull(TagType.DATE);
			break;

		case WORKSOURCE:
			checkAndPull(TagType.TEXT);
			break;

		case THEACTUALWORK:
			checkAndPull(TagType.TEXT);
			break;

		case TASKDESCRIPTION:
			checkAndPull(TagType.TEXT);
			break;

		case MODELERNOTE:
			checkAndPull(TagType.TEXT);
			break;

        case PACKAGEID:
            checkAndPull(TagType.INT);
            break;

        default:
			break;
		}
	}
	
	/**
	 * At the beginning of parse create an Assignment object
	 * 
	 * @throws XmlPullParserException
	 * @throws WorkflowException
	 */
	private void processAssignment() throws XmlPullParserException,
			WorkflowException {

		switch (parser.getEventType()) {
		case XmlPullParser.START_TAG:
			assignment = new Assignment();
			break;
		case XmlPullParser.END_TAG:

		}
	}

	
	/**
	 * several tags share the same basic processing, whether they are
	 * date fields, text, or int. When the parser event is TEXT the text is
	 * retrieved and parsed into a Date object, int field or left alone
	 * if the tag type is text. It then sets the correct field in the assignment
	 * object based on the current parser state
	 * 
	 * @param tt a tag type, one of TEXT, INT or DATE
	 * @throws XmlPullParserException
	 * @throws WorkflowException
	 */	
	private void checkAndPull(TagType tt) throws XmlPullParserException,
		WorkflowException {
		
		if (parser.getEventType() == XmlPullParser.TEXT) {
			String val = parser.getText();
			Date d = null;
			int i = -1;
			
			// determine tye of field and parse out text into it.
			switch (tt) {
			case DATE:
				try {
					d = dateFormatter.parse(val);
				} catch (ParseException e) {
					processError("Can't parse date field");
					}
				break;
				
			case INT:
				i = Integer.parseInt(val);
				break;
				
				default:
					break;
			}
			
			switch (parserState) {
			
			case IDENTIFIER:
				assignment.setIdentifier(i);
				break;

			case DATECREATED:
				assignment.setDateCreated(d);
				break;

			case MANAGERID:
				assignment.setManager(val);
				break;

			case MODELERID:
				assignment.setModeler(val);
				break;

			case CURRENTSTATUS:
				assignment.setStatus(Assignment.Status.getStatus(val));
				break;

			case LASTUPDATED:
				assignment.setLastUpdated(d);
				break;

			case WORKSOURCE:
				assignment.setWorkSource(val);
				break;

			case THEACTUALWORK:
				assignment.setTheActualWork(val);
				break;

			case TASKDESCRIPTION:
				assignment.setTaskDescription(val);
				break;

			case MODELERNOTE:
				assignment.setModelerNote(val);
				break;

            case PACKAGEID:
                assignment.setPackageId(i);
                break;

            default:
				break;
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
		return "(line: " + parser.getLineNumber() + " col: "
				+ parser.getColumnNumber() + ")";
	}

}
