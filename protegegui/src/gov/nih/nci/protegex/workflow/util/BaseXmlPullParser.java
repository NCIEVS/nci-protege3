package gov.nih.nci.protegex.workflow.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import edu.stanford.smi.protege.util.Log;

public abstract class BaseXmlPullParser {
    
    protected XmlPullParser _parser = null;
    protected Logger _logger = Log.getLogger(getClass());
    protected StringBuffer _warning = new StringBuffer();
    protected String _xmlPacket = "";

    /**
     * Returns a new instance of the parser.
     * @param text The string to be parsed.
     * @return the parser.
     */
    protected XmlPullParser getParser(String xmlPacket) {
        try {
            _xmlPacket = xmlPacket;
            _warning.delete(0, _warning.length());

            XmlPullParser parser;
            parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(xmlPacket));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            return parser;
        } catch (XmlPullParserException e) {
            error("Error while creating new parser: " + e.toString());
            return null;
        }
    }
    
    /**
     * Displays a log statement.
     * Note: This log message will not be suppressed when the 
     * protege.system.level is set to SEVERE in the logging.properties
     * file. 
     * @param text The text.
     */
    protected void log(String text) {
        _logger.log(Level.INFO, text);
    }

    /**
     * Displays a debug statement.
     * @param text The text.
     */
    protected void debug(String text) {
        _logger.info(text);
    }
    
    /**
     * Displays a warning statement.
     * @param text The text.
     */
    protected void warning(String text) {
        _logger.warning(text);
    }
    
    /**
     * Displays an error statement.
     * @param text The text.
     */
    protected void error(String text) {
        _logger.severe(text);
    }
    
    /**
     * Appends warning messages to a buffer.  This message can be displayed 
     * (within a dialog) when the parsing is completed.
     * @param msg
     */
    protected void warningMsg(String msg) {
        log(msg);
        _warning.append(msg + "\n");
    }
    
    /**
     * Returns warning messages.  If there are no messages, this method returns
     * a blank string.  This message is used after the parsing is completed.
     * @return error messages.
     */
    public String getWarningMsg() {
        return _warning.toString();
    }

    /**
     * Returns the URL without the ending slash or backslash (if any). 
     * @param url The URL.
     * @return the URL without the ending slash or backslash (if any). 
     */
    public static String stripSlashfromURL(String url) {
        String newURL = url.trim();
        int n = newURL.length();
        char lastChar = newURL.charAt(n-1);
        if (lastChar == '/' || lastChar == '\\')
            newURL = newURL.substring(0, n-1);
        return newURL;
    }

    /**
     * Performs the actual parse of the document
     * 
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void processDocument() {
        try {
            int eventType = _parser.getEventType();
            do {
                //debug("processDocument: " + eventToString(eventType));
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
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
                eventType = _parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
        } catch (Exception e) {
            error("Error while parsing " + e.toString());
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the current parser position.
     * @return the current parser position.
     */
    protected final String getParserPosition() {
        return "(line: " + _parser.getLineNumber() + " column: "
                + _parser.getColumnNumber() + ")";
    }
    
    /**
     * Returns the parser state as a string.
     */
    protected abstract String getParserStateString();
    
    /**
     * Returns the parser state's tag name.
     */
    protected abstract String getParserStateTagName();
    
    /**
     * Consolidates error processing into a single message. Take the
     * message add the parser line number and the current state.
     * Log the message and throw it in an InfluenceException.
     * 
     * @param text The text message.
     * @throws Exception
     */
    protected void processError(String text) throws Exception {
        String s = text +
            "\n  " + "Current parser state: " + getParserStateString();
        throw new Exception(s);
    }
    
    /**
     * Pushes the current state to the parser stack.
     * @return true if successful.
     * @throws XmlPullParserException
     * @throws Exception
     */
    protected abstract boolean pushCurrentStateToStack()
            throws XmlPullParserException, Exception;
    
    /**
     * Pops current state from parser stack.
     * @return true if successful.
     */
    protected abstract boolean popStateFromStack();
    
    /**
     * Process the start element.
     * @throws XmlPullParserException
     * @throws Exception
     */
    protected void processStartElement() 
        throws XmlPullParserException, Exception {
        pushCurrentStateToStack();
    }
    
    /**
     * Processes the end element.
     * @throws XmlPullParserException
     * @throws Exception
     */
    protected void processEndElement()
        throws XmlPullParserException, Exception {
        popStateFromStack();
    }
    
    /**
     * Processes the text value.
     * @throws XmlPullParserException
     * @throws Exception
     */
    protected void processText()
        throws XmlPullParserException, Exception {
        if (_parser.getEventType() != XmlPullParser.TEXT)
            return;

        String text = _parser.getText().trim();
        if (text.length() <= 0)
            return;

        String tagName = getParserStateTagName();
        debug("processText: " + tagName + "[" + text + "]");
    }

    /**
     * Prints debugging statement.
     * @throws Exception
     */
    protected void debugParser() throws Exception {
        if (_parser.getEventType() == XmlPullParser.START_TAG ||
            _parser.getEventType() == XmlPullParser.END_TAG) {
            debug("Dispatching " + eventToString(_parser.getEventType())
                + " to state " + getParserStateString() + " (tag: "
                + _parser.getName() + ")");
        } else {
            debug("Dispatching " + eventToString(_parser.getEventType())
                + " to state " + getParserStateString());
        }
    }

    /**
     * Converts event type to a string.
     * @param type The event type.
     * @return The string value.
     */
    protected String eventToString(int type) {
        if (type == XmlPullParser.START_DOCUMENT)
            return "START_DOCUMENT";
        if (type == XmlPullParser.END_DOCUMENT)
            return "END_DOCUMENT";
        if (type == XmlPullParser.START_TAG)
            return "START_TAG";
        if (type == XmlPullParser.END_TAG)
            return "END_TAG";
        if (type == XmlPullParser.TEXT)
            return "TEXT";
        return "UNKNOWN_EVENT";
    }
}
