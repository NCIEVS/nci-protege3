package com.clarkparsia.dig20.xml.asks;

import org.xml.sax.ContentHandler;

import com.clarkparsia.dig20.asks.AskQuery;

public interface AsksQuerySAXHandler extends ContentHandler {

	public AskQuery getQuery();

}
