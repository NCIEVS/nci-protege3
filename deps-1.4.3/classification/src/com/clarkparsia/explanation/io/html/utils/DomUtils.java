package com.clarkparsia.explanation.io.html.utils;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import java.io.Writer;
import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * Title: DomUtils<br>
 * Description: Some simple DOM utilities.<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Mar 7, 2008 12:54:36 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class DomUtils {
    public static Element createCell(Element theRow) {
        return createCell(theRow, -1);
    }

    public static Element createCell(Element theRow, int theSpan) {
        Element aElem = theRow.getOwnerDocument().createElement("td");
        aElem.setAttribute( "valign", "top" );
        aElem.setAttribute( "nowrap", "nowrap" );
        aElem.setAttribute( "style", "padding: 2px 0px;" );
        if (theSpan != -1) {
            aElem.setAttribute("colspan", ""+theSpan);
        }

        theRow.appendChild(aElem);

        return aElem;
    }

    public static Element createRow(Element theTable) {
        Element aRow = theTable.getOwnerDocument().createElement("tr");

        theTable.appendChild(aRow);

        return aRow;
    }

    public static Element createTable(Element theParent) {
        Element aElem = theParent.getOwnerDocument().createElement("table");
        
        aElem.setAttribute( "border", "0" );
        aElem.setAttribute( "cellspacing", "0" );
        aElem.setAttribute( "cellpadding", "0" );
        
        theParent.appendChild(aElem);

        return aElem;
    }

    public static String renderElement(Element theElem) {
        try {
            Writer aOutput = new StringWriter();

            Source aSource = new DOMSource(theElem);
            StreamResult aStreamResult = new StreamResult(aOutput);

            Transformer aTransformer = TransformerFactory.newInstance().newTransformer();

            aTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            aTransformer.setOutputProperty(OutputKeys.METHOD, "html");
            aTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            aTransformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            aTransformer.transform(aSource, aStreamResult);

            aOutput.close();

            // hack because the &nbsp; elements were getting escaped for some reason
            return aOutput.toString().replaceAll("&amp;nbsp;", "&nbsp;");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
