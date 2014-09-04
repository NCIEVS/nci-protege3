/**
 * 
 */
package gov.nih.nci.protegex.workflow.util;

/**
 * @author Flora B. Workflow
 *
 */
public class XML {
	
	public static void asStartTag(StringBuffer sb, String tag) {
		sb.append("<" + tag + ">");
	}
	
	public static void asEndTag(StringBuffer sb, String tag) {
		sb.append("</" + tag + ">");
	}
	
	public static void asCdata(StringBuffer sb, String val) {
		sb.append("<![CDATA[" + val + "]]>");
	}

}
