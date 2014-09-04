package com.clarkparsia.dig20.server.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import org.coode.xml.XMLWriter;
import org.coode.xml.XMLWriterImpl;
import org.coode.xml.XMLWriterNamespaceManager;

import com.clarkparsia.dig20.server.admin.AdminResponse;
import com.clarkparsia.dig20.server.admin.AdminResponseVisitor;
import com.clarkparsia.dig20.server.admin.InfoResponse;

public class AdminResponseRenderer implements AdminResponseVisitor {

	private String						documentTag	= "AdminResponse";
	protected XMLWriter					xmlWriter;
	
	public void startRendering(Writer writer) throws IOException {
		XMLWriterNamespaceManager nsManager = new XMLWriterNamespaceManager(AdminVocabulary.ADMIN_NS);
		xmlWriter = new XMLWriterImpl(writer, nsManager);
		xmlWriter.startDocument( documentTag );
	}
	
	public void visit(InfoResponse infoResponse) {
		try {
			xmlWriter.writeStartElement(AdminVocabulary.INFO_VALUES.getURI().toString());
			
			for (Enumeration<?> e = infoResponse.propertyNames(); e.hasMoreElements(); ) {
				String propertyName = (String) e.nextElement();

				xmlWriter.writeStartElement(AdminVocabulary.PROPERTY.getURI().toString());
				
				xmlWriter.writeStartElement(AdminVocabulary.PROPERTY_NAME.getURI().toString());
				xmlWriter.writeTextContent(propertyName);
				xmlWriter.writeEndElement(); // PROPERTY_NAME

				String propertyValue = infoResponse.getProperty(propertyName);

				xmlWriter.writeStartElement(AdminVocabulary.PROPERTY_VALUE.getURI().toString());
				xmlWriter.writeTextContent(propertyValue);
				xmlWriter.writeEndElement(); // PROPERTY_VALUE
				
				xmlWriter.writeEndElement(); // PROPERTY
			}
			xmlWriter.writeEndElement();
		} catch (IOException e) {
			// TODO handle
		}
	}
	
	public void render(AdminResponse response) throws IOException {
		response.accept(this);
	}
	
	public void endRendering() throws IOException {
		xmlWriter.endDocument();
		xmlWriter = null;
	}

	public void setDocumentTag(String documentTag) {
		this.documentTag = documentTag;
	}
}
