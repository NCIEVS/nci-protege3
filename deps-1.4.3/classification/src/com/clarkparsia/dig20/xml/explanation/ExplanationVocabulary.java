package com.clarkparsia.dig20.xml.explanation;

import java.net.URI;

public class ExplanationVocabulary {

	public enum Elements {

		EXPLAIN("explain"), EXPLANATIONS("explanations");

		private String	shortName;
		private URI		uri;

		Elements(String name) {
			this.uri = URI.create( EXPLANATIONS_NS + name );
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

	final static public String	NEXP_ATTRIBUTE	= "nexp";
	final static public String	EXPLANATIONS_NS	= "http://dl.kr.org/dig/lang/explanation-schema#";
}