package gov.nih.nci.protegex.edit;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.logging.Level;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.stanford.smi.protege.util.Log;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class Qualifier implements Serializable {
	
	private static final long serialVersionUID = 441936038070346950L;

	int namespaceId;
	String name;
	String value;


	public Qualifier() {
		this.namespaceId = -1;
		this.name = "";
		this.value = "";
	}

	public Qualifier(String name, String value) {
		this.namespaceId = -1;
		this.name = name;
		this.value = value;
	}

	public Qualifier(String name, String value, int namespaceId) {
		this.namespaceId = namespaceId;
		this.name = name;
		this.value = value;
	}

	/** Sets value to namespaceId
	 */
	public void setNamespaceId(int namespaceId) {
		this.namespaceId = namespaceId;
	}

	/** Sets value to name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** Sets value to value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/** Gets namespaceId
	 *
	 * @return namespaceId
	 */
	public int getNamespaceId() {
		return this.namespaceId;
	}

	/** Gets name
	 *
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/** Gets value
	 *
	 * @return value
	 */
	public String getValue() {
		return this.value;
	}

	public String toXML() {
		return writeDocument(toDOM());
	}

	private String writeDocument(Document doc)
	{
	    try {
	        OutputFormat format = new OutputFormat(doc);
			StringWriter stringOut = new StringWriter();
	        XMLSerializer serial = new XMLSerializer(stringOut, format);
	        serial.asDOMSerializer();
	        serial.serialize(doc.getDocumentElement());
	        return stringOut.toString();
	    } catch (Exception ex) {
	        Log.getLogger().log(Level.WARNING, "Exception caught", ex);;
	    }
	    return null;
	}

	public String toString() {
		return this.name + "$" + this.value;
	}

	public Document toDOM()
	{
		Document doc= new DocumentImpl();
		try {
			Element root = doc.createElement("Qualifier");
			Element element;
			element = doc.createElement("namespaceId");
			element.appendChild(doc.createTextNode(intToString(namespaceId)));
			root.appendChild(element);

			element = doc.createElement("name");
			element.appendChild(doc.createTextNode(name));
			root.appendChild(element);

			element = doc.createElement("value");
			element.appendChild(doc.createTextNode(value));
			root.appendChild(element);

			doc.appendChild(root);
	    } catch (Exception ex) {
	        Log.getLogger().log(Level.WARNING, "Exception caught", ex);;
	    }
		return doc;
	}

	/** Converts an int value Date to a String
	 *
	 * @param i an int value
	 * @return a String representation of an int value
	 */
	private String intToString(int i)
	{
		return "" + i;
	}

} // end of Qualifier
