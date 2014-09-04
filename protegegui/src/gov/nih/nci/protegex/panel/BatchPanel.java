/**
 * 
 */
package gov.nih.nci.protegex.panel;
import javax.swing.JTextArea;

/**
 * @author bitdiddle
 *
 */
public interface BatchPanel {
	void enableButton(String name, boolean state);
	JTextArea getTextArea();
	

}
