package com.clarkparsia.dig20.xml.responses;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.clarkparsia.dig20.xml.SAXHandlerParent;
import com.clarkparsia.dig20.xml.StackableHandler;

public class MessageHandler extends StackableHandler {

	private StringBuilder						allChars	= new StringBuilder();
	private int									code;
	private SAXHandlerParent<MessageHandler>	parent;

	public MessageHandler(SAXHandlerParent<MessageHandler> parent, Attributes atts) {
		this.parent = parent;
		String codeStr = atts.getValue( ResponsesVocabulary.CODE_ATTRIBUTE );

		if( codeStr == null )
			throw new RuntimeException( new SAXParseException( "No message code attribute found.",
					locator ) );
		try {
			this.code = Integer.parseInt( codeStr );
		} catch( NumberFormatException e ) {
			// TODO: Something smarter
			throw new RuntimeException( new SAXParseException( "Invalid message code attribute: "
					+ e.toString(), locator ) );
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		allChars.append( ch, start, length );
	}

	public int getCode() {
		return code;
	}

	public String getContent() {
		return allChars.toString();
	}

	@Override
	public ContentHandler newChild(String uri, String localName, String name, Attributes atts)
			throws SAXException {
		throw new SAXParseException( "Unexpected child element", locator );
	}

	@Override
	public void notifyParent() throws SAXException {
		parent.handleChild( this );
	}
}