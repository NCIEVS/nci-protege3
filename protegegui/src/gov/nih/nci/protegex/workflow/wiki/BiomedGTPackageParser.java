package gov.nih.nci.protegex.workflow.wiki;

import gov.nih.nci.protegex.workflow.util.BaseXmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BiomedGTPackageParser extends BaseXmlPullParser {
    /**
     * This enum serves a couple of purposes. It tracks the state of the parser.
     * If an element can only have certain children in the document, the element
     * will belong to those child states parents. As the parser transitions from
     * state to state the transitions can be validated.
     * 
     * It also associates the labels with each state.
     */
    private enum State {
        EMPTY(new State[] {}, ""), 
          EXPORT_LIST(EMPTY, "export-list"),
            WORKFLOW(EXPORT_LIST, "workflow"),
              WF_NAME(WORKFLOW, "wf-name"),
              WF_URL(WORKFLOW, "wf-url"),
        ;
        
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
         * @param parent
         * @param name
         */
        State(State parent, String name) {
            this._parents = new State[] { parent };
            this._name = name;
        }

        /**
         * Constructor for State with a multiple parent states.
         * @param parents
         * @param name
         */
        State(State[] parents, String name) {
            this._parents = parents;
            this._name = name;
        }

        /**
         * Checks whether it is valid to transition to the specified
         * state from this state.
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
         * @param state The state.
         */
        private static void registerState(State state) {
            tagLookup.put(state._name, state);
        }
    } // end enum
    
    // Member Variables:
    private State _parserState = State.EMPTY;
    private Stack<State> _stateStack = new Stack<State>();
    private ArrayList<String> _urls = null;
    private ArrayList<String> _packageNames = null;
    private Stack<String> _ignoreTags = new Stack<String>();
    
    /**
     * Returns the parser state as a string.
     * Note: Used by the base class because each specific parser contains
     *   a different State enum.
     */
    protected String getParserStateString() {
        return _parserState.toString();
    }
    
    /**
     * Returns the parser state's tag name.
     * Note: Used by the base class because each specific parser contains
     *   a different State enum.
     */
    protected String getParserStateTagName() {
        return _parserState.getTagName();
    }
    
    /**
     * Pushes the current state to the parser stack.
     * Note: Used by the base class because each specific parser contains
     *   a different State enum.
     * @return true if successful.
     * @throws XmlPullParserException
     * @throws Exception
     */
    protected boolean pushCurrentStateToStack() 
            throws XmlPullParserException, Exception{
        String tagName = _parser.getName();
        State newState = State.lookupStateForTag(tagName);

        if ((newState != null) && (_parserState.checkValid(newState))) {
            _stateStack.push(_parserState);
            _parserState = newState;
            return true;
        } else {
            warning("Ignoring Invalid XML Tag: " + tagName + 
                " " + getParserPosition());
            _ignoreTags.add(tagName);
            return false;
        }
    }

    /**
     * Pops the current state from the stack. 
     * Note: Used by the base class because each specific parser contains
     *   a different State enum.
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
     * Process the XML string.
     * @param xmlPacket The XML string.
     * @return the ArrayList of package URLs.
     */
    public ArrayList<String> processXml(String xmlPacket) {
        _urls = new ArrayList<String>();
        _packageNames = new ArrayList<String>();
        _parser = getParser(xmlPacket);
        _parserState = State.EMPTY;
        _ignoreTags.clear();
        processDocument();
        return _urls;
    }
    
    /**
     * Processes the text value.
     * @throws XmlPullParserException
     * @throws Exception
     */
    protected void processText()
        throws XmlPullParserException, Exception {
        if (false) debugParser();
        if (_parser.getEventType() != XmlPullParser.TEXT)
            return;
        
        String text = _parser.getText().trim();
        if (text.length() <= 0)
            return;
        
        int size = _ignoreTags.size();
        if (size > 0) {
            String tag = _ignoreTags.get(size - 1);
            warning("Ignoring(" + tag + ") value: " + text);
            return;
        }
        
        //debug("processText: " + _parserState.getTagName() + "[" + text + "]");
        switch (_parserState) {
        case WF_NAME:
            _packageNames.add(text);
            break;
        case WF_URL:
            _urls.add(text);
            break;
        }
    }
    
    public ArrayList<String> getPackageNames() {
        return _packageNames;
    }
}