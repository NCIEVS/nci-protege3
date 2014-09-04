/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.*;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.ui.event.*;

public class DisplayUtilities {
	public static final int DEFAULT_TEXT_COMPONENT_COLUMNS = 40;
	public static final int DEFAULT_TEXT_COMPONENT_ROWS = 5;
	/*
	 static private Map displayNumbered (Object [] arr)  {
	 Collection coll = new ArrayList (arr.length);
	 for (int i = 0; i < arr.length; i++)
	 coll.add (arr[i]);
	 return displayNumbered (coll);
	 }
	 
	 static Map displayNumbered (Collection c) {
	 return displayNumbered (c, 0);
	 }
	 
	 
	 
	 public static JTextComponent createDisabledTextField (String text) {
	 return createDisabledTextField (text, DEFAULT_TEXT_FIELD_COLUMN_WIDTH);
	 }
	 */
	
	public static JTextComponent createDisabledTextField (String text) {
		return  createDisabledTextComponent (text, null, true);
	}
	
	public static JTextComponent createDisabledTextField (String text, Color color) {
		return createDisabledTextComponent (text, color, true);
	}
	
	public static JTextComponent createDisabledTextComponent (String text) {
		if (text.length() < DEFAULT_TEXT_COMPONENT_COLUMNS)
			return  createDisabledTextComponent (text, null, true);
		else
			return  createDisabledTextComponent (text, null, false);
	}
	
	
	private static JTextComponent createDisabledTextComponent (String text, Color color,  boolean textField) {
		JTextComponent field;
		
		if (textField) {
			field = new JTextField (text);
		} else {
			field = new JTextArea (DEFAULT_TEXT_COMPONENT_ROWS, DEFAULT_TEXT_COMPONENT_COLUMNS);
			((JTextArea)field).setLineWrap (true);
			((JTextArea)field).setWrapStyleWord (true);
			field.setText(text);
		}
		
		field.setEnabled(false);
		if (color != null)
			field.setForeground(color);
		return field;
	}
	
	public static JFileChooser createFileChooser(String description, String extension) {
		URI directoryURI = ProjectManager.getProjectManager().getCurrentProject().getProjectDirectoryURI();
		String directory = (directoryURI == null) ? null : directoryURI.getPath();
		if (directory == null) {
			directory = ApplicationProperties.getApplicationDirectory().toString();
		}
		JFileChooser chooser = new JFileChooser(directory) {
			public int showDialog(Component c, String s) {
				int rval = super.showDialog(c, s);
				return rval;
			}
		};
		chooser.setDialogTitle(description);
		if (extension == null) {
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		} else {
			String text = Text.getProgramName() + " " + description;
			chooser.setFileFilter(new ExtensionFilter(extension, text));
		}
		return chooser;
	}
	
	
	
	static public String displayFrameWithAffiliation (Frame f) {
//		return "<" + f.toString() + ", " +
//		PromptTab.getKnowledgeBasePrettyName (f.getKnowledgeBase ()) + "_ont>";
		return (f==null) ? null : "<" + f.getBrowserText() + ", " + f.getKnowledgeBase ();
	}
	
	static public float rememberOldHeightRatio (JSplitPane splitPane) {
		int oldHeight = splitPane.getHeight ();
		float oldFraction = ((oldHeight == 0) ? 0 : ((float)splitPane.getDividerLocation ()) / oldHeight);
		
		return oldFraction;
	}
	
	static public float rememberOldWidthRatio (JSplitPane splitPane) {
		int oldWidth = splitPane.getWidth ();
		float oldFraction = ((oldWidth == 0) ? 0 : ((float)splitPane.getDividerLocation ()) / oldWidth);
		
		return oldFraction;
	}
	
	static public void setNewHeightRatio (JSplitPane splitPane, JComponent comp,  float oldFraction, int v1, int v2) {
		int h =  comp.getHeight();
		splitPane.setDividerLocation((int)((oldFraction <= 0.01) ? v1 * h/v2 : splitPane.getHeight() * oldFraction));
	}
	
	static public void setNewWidthRatio (JSplitPane splitPane, JComponent comp, float oldFraction, int v1, int v2) {
		int w = comp.getWidth();
		splitPane.setDividerLocation((int)((oldFraction <= 0.01) ? v1 * w/v2 : splitPane.getWidth() * oldFraction));
	}
	
	static public JPanel createPerformButton (String label, JComponent list){
		JPanel buttonPanel = new JPanel (new FlowLayout());
		JButton performButton = new JButton (label);
		performButton.setIcon(Icons.getOkIcon());
		performButton.addActionListener (new PerformButtonActionListener(list));
		buttonPanel.add (performButton);
		return buttonPanel;
	}
	
	static public JPanel createPanelWithMultipleLines (String[] lines) {
		JPanel result =  new MultipleLinesMessagePanel (lines);
		return result;
	}
	
	static class MultipleLinesMessagePanel extends JPanel {
		MultipleLinesMessagePanel (String[] lines) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			for (int i = 0; i < lines.length; i++) {
				JLabel label = new JLabel(lines[i]);
				label.setHorizontalAlignment(JLabel.CENTER);
				add(label);
			}
		}
	}
	
	static String getDoItString () {
		if (PromptTab.merging()) return "Do It";
		if (PromptTab.mapping()) return "Create Mapping";
		return "";
	}

	public static String getSuggestionListName() {
		if (PromptTab.mapping()) return "Candidate Mappings";
		return "To Do list";
	}
	
}
