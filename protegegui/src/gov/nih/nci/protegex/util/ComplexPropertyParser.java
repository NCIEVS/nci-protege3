package gov.nih.nci.protegex.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class ComplexPropertyParser extends DefaultHandler {
    public final static int TERM_NAME = 0;
    public final static int TERM_GROUP = 1;
    public final static int TERM_SOURCE = 2;
    public final static int TERM_SOURCE_CODE = 3;

    private static Vector<String> properties;
    
    private static DOMParser parser = new DOMParser();
    
    static {
    	parser.setErrorHandler(new ErrorHandler() {

			public void error(SAXParseException exception)
					throws SAXException {
				// TODO Auto-generated method stub
				
			}

			public void fatalError(SAXParseException exception)
					throws SAXException {
				// TODO Auto-generated method stub
				
			}

			public void warning(SAXParseException exception)
					throws SAXException {
				// TODO Auto-generated method stub
				
			}
        	
        });
    }

    

   

    public static int cnt = 0;
    public static long tim_pars = 0;


    public static HashMap<String, String> parseXML(String complex_property) {
        complex_property = complex_property.trim();
        cnt++;
        long beg = System.currentTimeMillis();
        HashMap<String, String> name2val_hashmap = new HashMap<String, String>();
        if (!complex_property.startsWith("<")) {
            // nothing to parse

        } else {
            
            try {
                
                //DOMParser parser = new DOMParser();
                BufferedReader reader = new BufferedReader(new StringReader(complex_property));
                InputSource inputsource = new InputSource(reader);
                
                
               
                parser.parse(inputsource);
                Document document = parser.getDocument();

                // properties = new Vector();
                traverse(document, name2val_hashmap);
                reader.close();
                parser.reset();
            } catch (Exception e) {
                
            }
            
        }
        tim_pars += System.currentTimeMillis() - beg;

        
        return name2val_hashmap;
    }

    private static void traverse(Node node, HashMap<String, String> name2val_hashmap) {
        int type = node.getNodeType();
        if (type == Node.ELEMENT_NODE)

        {
            NamedNodeMap nnm = node.getAttributes();
            if (nnm != null) {
                if (nnm.getLength() > 0) {
                    if (nnm.getNamedItem("xml:lang") != null) {
                        String lang = nnm.getNamedItem("xml:lang").getNodeValue();
                        name2val_hashmap.put("xml:lang", lang);
                    }
                }
            }
            String nodename = node.getNodeName();
            if (nodename.indexOf(":") > 0) {
                nodename = nodename.substring(nodename.indexOf(":") + 1);
            }
            NodeList children = node.getChildNodes();
            if (children != null && children.item(0) != null) {
                String nodevalue = children.item(0).getNodeValue();
                if (nodevalue != null) {

                    name2val_hashmap.put(nodename, nodevalue.trim());
                }
            }
        }
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++)
                traverse(children.item(i), name2val_hashmap);
        }
    }

    public static String getText(String complex_property) {
        HashMap<String, String> map = ComplexPropertyParser.parseXML(complex_property);
        if (map == null)
            return complex_property;
        if (map.isEmpty())
            return complex_property;
        if (map.keySet().isEmpty())
            return complex_property;
        String key = "def-definition";
        if (map.containsKey(key)) {
            return (String) map.get(key);
        }
        key = "go-term";
        if (map.containsKey(key)) {
            return (String) map.get(key);
        }
        return complex_property;
    }

    
    /**
     * This method now acts as the identity function, as there should be no pipe delimited data in the data base. If
     * there is, it is an error condition
     * 
     * @param value
     * @return the same value
     */

    public static String pipeDelim2XML(String value) {
        return value;

    }

    

   

    /**
     * This method was written to help the WorkflowPanel show property differences in the upper and lower panels. There
     * seems to be two DEFINITION (xml tagged) formats used that caused the same definition property to show up
     * differently even though their sub-values are the same. See: OWLWrapper.formatDEFINITION and
     * CustomizedAnnotationDialog.initCADData methods.
     * 
     * @param propertyName The name of the property.
     * @param value contains the complex property in xml tagged format.
     * @return the complex property in xml tagged format.
     
    public static String reformatComplexProperty(String propertyName, String value) {
        if (!propertyName.toUpperCase().contains("DEFINITION"))
            return value;

        // Note: Currently handling DEFINITION property. The other
        // complex property seems fine.
        HashMap<String, String> hmap = ComplexPropertyParser.parseXML(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<ncicp:ComplexDefinition xmlns:ncicp=\"http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#\">");
        //buffer.append("<ncicp:ComplexDefinition>");
        if (hmap.get("xml:lang") != null) {
            StringUtil.createXmlTag(buffer, true, hmap, "ncicp:def-definition", "xml:lang", (String) hmap.get("xml:lang"));

        } else {
            StringUtil.createXmlTag(buffer, true, hmap, "ncicp:def-definition");
        }
        StringUtil.createXmlTag(buffer, true, hmap, "ncicp:Definition_Review_Date");
        StringUtil.createXmlTag(buffer, true, hmap, "ncicp:def-source");
        StringUtil.createXmlTag(buffer, true, hmap, "ncicp:Definition_Reviewer_Name");
        StringUtil.createXmlTag(buffer, true, hmap, "ncicp:attr");
        buffer.append("</ncicp:ComplexDefinition>");
        return buffer.toString();
    }
**/
    /**
     * Returns the term-name value for NCI/PT else null.
     * 
     * @param complex_property The xml string.
     * @return the term-name value for NCI/PT else null.
     */
    public static String getPtNciTermName(String complex_property) {
        HashMap<String, String> hmap = parseXML(pipeDelim2XML(complex_property));
        String term_group = (String) hmap.get("term-group");
        if (term_group == null || !(term_group.equals("PT") || term_group.equals("HD") || term_group.equals("AQ")))       
            return null;
        String term_source = (String) hmap.get("term-source");
        if (term_source == null || !term_source.equals("NCI"))
            return null;
        String term_name = (String) hmap.get("term-name");
        return term_name;
    }

    public static String replaceFullSynValue(HashMap<String, String> hm, String key, String value) {
        String[] order = new String[] { "term-name", "term-group", "term-source", "source-code", "xml:lang" };

        StringBuffer buffer = new StringBuffer();
        buffer.append("<ncicp:ComplexTerm xmlns:ncicp=\"http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#\">");
        //buffer.append("<ncicp:ComplexTerm>");
        for (String tag : order) {
            String hmValue = hm.get(tag);
            if (hmValue == null)
                continue;

            if (tag.equals(key))
                StringUtil.createXmlTag(buffer, true, "ncicp:" + tag, value);
            else
                StringUtil.createXmlTag(buffer, true, "ncicp:" + tag, hmValue);
        }
        buffer.append("</ncicp:ComplexTerm>");
        return buffer.toString();
    }

    
}
