package com.clarkparsia.dig20.xml.responses;

import java.net.URI;

public class ResponsesVocabulary {

	public enum Elements {

		BOOLEAN("boolean"), ERROR("error"), OK("ok"), RESPONSE("response"), RESPONSES("responses"),
		SYNONYMS("synonyms");

		private String	shortName;
		private URI		uri;

		Elements(String name) {
			this.uri = URI.create( RESPONSES_NS + name );
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

	final static public String	CODE_ATTRIBUTE	= "code";
	final static public String	ID_ATTRIBUTE	= "id";
	final static public String	RESPONSES_NS	= "http://dl.kr.org/dig/lang/responses-schema#";
}