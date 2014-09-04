package gov.nih.nci.protegex.edit;

import edu.stanford.smi.protege.util.Log;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** Property
 *
 * @author Northrop Grumman Information Technology
 * @version 1.0
 */

public class Property implements Serializable {
	
	private static final long serialVersionUID = 441936038070346947L;

	/** property name
	 */
	String name;
	/** property value
	 */
	String value;

	// caCORE 3.1 addition:
	/** qualifiers
	*/
	Vector qualifiers;

	/** contains Qualifier
	*/
	boolean containsQualifier;


	/** default constructor
	 *
	 * @since DTSRPC 2.1
	 */
	public Property() {
		this.name = "";
		this.value = "";
		this.containsQualifier = false;
		this.qualifiers = new Vector();
	}

	/** Constructs a Property with the given name and value.
	 *
	 * @since DTSRPC 2.0
	 * @param name name of the Property.
	 * @param value value of the Property.
	 */
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
		this.containsQualifier = false;
		this.qualifiers = new Vector();
	}

	/** Assigns a value to property name
	 *
	 * @since DTSRPC 2.0
	 * @param name property name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** Assigns a value to property value
	 *
	 * @since DTSRPC 2.0
	 * @param value property name
	 */
	public void setValue(String value) {
		this.value = value;
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

	// caCORE 3.1 addition:
	/** Adds the specific qualifier to the property.
	 *
	 * @since DTSRPC 2.1
	 * @param qualifierName the qualifier name.
	 * @param qualifierValue the qualifier value.
	 */
	public void addQualifier(String qualifierName, String qualifierValue) {
		Qualifier qualifier = new Qualifier(qualifierName, qualifierValue);
		qualifiers.add(qualifier);
		if (qualifiers.size() > 0) containsQualifier = true;
	}


	/** Adds the specific qualifier to the property.
	 *
	 * @since DTSRPC 2.1
	 * @param qualifier an instance of Qualifier
	 */
	public void addQualifier(Qualifier qualifier) {
		if (qualifier == null)
		{
		    return;
		}
		qualifiers.add(qualifier);
		if (qualifiers.size() > 0) containsQualifier = true;
	}

	/** Gets qualifiers
	 *
	 * @since DTSRPC 2.1
	 * @return qualifiers
	 */
	public Vector getQualifiers() {
		return this.qualifiers;
	}


	/** Gets qualifiers
	 *
	 * @since DTSRPC 2.1
	 * @return qualifiers
	 */
	public Vector getQualifierCollection() {
		return this.qualifiers;
	}



	/** Checks if contains any qualifier
	 *
	 * since DTSRPC 2.1
	 * @return qualifiers
	 */
	public boolean hasQualifier() {
		return this.containsQualifier;
	}


	/** Gets a XML representation of a Property object in String
	 *
	 * @return a XML representation of a Property object in String
	 */
	public String toXML() {
		return writeDocument(toDOM());
	}

	/** Converts a XCML Document to a String
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
	        Log.getLogger().log(Level.WARNING, "Exception caught", ex);;
	    }
	    return null;
	}

	/** Gets a String representation of a Property object
	 *
	 * @return a String representation of a Property object
	 */
	public String toString() {
		return this.name + "$" + this.value;
	}

	/** Gets a XML Document representation of a Property object
	 *
	 * @return a XML Document representation of a Property object
	 */
	public Document toDOM()
	{
		Document doc= new DocumentImpl();
		try {
			Element root = doc.createElement("Property");
			Element element;
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


} // end of Property
