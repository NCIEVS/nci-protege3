package com.clarkparsia.dig20.xml;

import java.net.URI;

public enum DigVocabulary {

	ASKS("asks"), AXIOM("axiom"), AXIOMS("axioms"), DIG("dig"), EXPLAIN("explain"),
	EXPLANATIONS("explanations"), RETRACTIONS("retractions");

	final static public String	DIG20_NS		= "http://dl.kr.org/dig/lang/schema#";
	final static public String	ID_ATTRIBUTE	= "id";
	final static public String	NEXP_ATTRIBUTE	= "nexp";
	final static public String	URI_ATTRIBUTE	= "uri";
	private String				shortName;
	private URI					uri;

	DigVocabulary(String name) {
		this.uri = URI.create( DIG20_NS + name );
		shortName = name;
	}

	public String getShortName() {
		return shortName;
	}

	public URI getURI() {
		return uri;
	}

	public String toString() {
		return uri.toString();
	}
}