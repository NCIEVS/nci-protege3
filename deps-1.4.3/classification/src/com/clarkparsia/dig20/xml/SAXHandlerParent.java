package com.clarkparsia.dig20.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface SAXHandlerParent<T extends ContentHandler> {
	
	public void handleChild(T child) throws SAXException;

}