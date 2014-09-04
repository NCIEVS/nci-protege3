package com.clarkparsia.dig20.server.xml;

import java.net.URI;

public enum AdminVocabulary {

	ADMIN("admin"), RELOAD("reload"), SHUTDOWN("shutdown"), LOAD("load"), INFO("info"),
	INFO_VALUES("info-values"), PROPERTY("property"), PROPERTY_NAME("property-name"), PROPERTY_VALUE("property-value");

	private URI					uri;
	private String				shortName;
	final static public String	ADMIN_NS	= "http://clarkparsia.com/xsd/dig20server/admin#";

	AdminVocabulary(String name) {
		this.uri = URI.create( ADMIN_NS + name );
		shortName = name;
	}

	public URI getURI() {
		return uri;
	}

	public String getShortName() {
		return shortName;
	}

	public String toString() {
		return uri.toString();
	}
}