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


/** Role
 *
 * @since DTSRPC 1.0
 * @author Northrop Grumman Information Technology
 * @version 1.0
 */

public class Role implements Serializable {
	
	private static final long serialVersionUID = 441936038070346951L;

	/** Role name
	 */
	String name;
	/** Role value
	 */
	String value;
	/** Role modifier
	 */
	String modifier;


	/** constructor
	 */
	public Role() {
		this.name = "";
		this.value = "";
		this.modifier = "";
	}

	/** Constructs a Role with the given name and value.
	 *
	 * @since DTSRPC 2.0
	 * @param name name of the Role.
	 * @param value value of the Role.
	 */
	public Role(String name, String value) {
		this.name = name;
		this.value = value;
		this.modifier = "";
	}

	/** Constructs a Role with the given name, value, and modifier.
	 *
	 * @since DTSRPC 2.0
	 * @param name name of the Role.
	 * @param value value of the Role.
	 * @param modifier modifier of the Role.
	 */
	public Role(String name, String value, String modifier) {
		this.name = name;
		this.value = value;
		this.modifier = modifier;
	}

	/** Assigns a value to name
	 *
	 * @param name property name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** Assigns a value to value
	 *
	 * @param value property value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/** Assigns a value to modifier
	 *
	 * @param modifier modifier value
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
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

	/** Gets modifier
	 *
	 * @return modifier
	 */
	public String getModifier() {
		return this.modifier;
	}

	/** Gets a XML representation of a Role object in String
	 *
	 * @return a XML representation of a Role object in String
	 */
	public String toXML() {
		return writeDocument(toDOM());
	}

	/** Converts a XML document to a String
	 *
	 * @param doc a XML document
	 * @return a String representation of a XML document
	 */
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
	        Log.getLogger().log(Level.WARNING, "Exception caught", ex);
	    }
	    return null;
	}

	/** Gets a String representation of a Role object
	 *
	 * @return a String representation of a Role object
	 */
	public String toString() {
		return this.name;
	}

	/** Gets a XML Document representation of a Role object.
	 *
	 * @return a XML Document representation of a Role object
	 */
	public Document toDOM()
	{
		Document doc= new DocumentImpl();
		try {
			Element root = doc.createElement("Role");
			Element element;
			element = doc.createElement("name");
			element.appendChild(doc.createTextNode(name));
			root.appendChild(element);

			element = doc.createElement("value");
			element.appendChild(doc.createTextNode(value));
			root.appendChild(element);

			element = doc.createElement("modifier");
			element.appendChild(doc.createTextNode(modifier));
			root.appendChild(element);

			doc.appendChild(root);
	    } catch (Exception ex) {
	        Log.getLogger().log(Level.WARNING, "Exception caught", ex);
	    }
		return doc;
	}

} // end of Role
