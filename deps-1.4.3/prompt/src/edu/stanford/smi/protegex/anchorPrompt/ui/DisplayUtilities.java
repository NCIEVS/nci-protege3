/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.anchorPrompt.ui.action.*;

public class DisplayUtilities {
  public static final int DEFAULT_TEXT_COMPONENT_COLUMNS = 40;
  public static final int DEFAULT_TEXT_COMPONENT_ROWS = 5;
  static public float rememberOldWidthRatio (JSplitPane splitPane) {
    int oldWidth = splitPane.getWidth ();
    float oldFraction = ((oldWidth == 0) ? 0 : ((float)splitPane.getDividerLocation ()) / oldWidth);

    return oldFraction;
  }

  static public void setNewHeightRatio (JSplitPane splitPane, JComponent comp, float oldFraction, int v1, int v2) {
    int h =  comp.getHeight();
    splitPane.setDividerLocation((int)((oldFraction <= 0.01) ? v1 * h/v2 : h * oldFraction));
  }

  static public void setNewWidthRatio (JSplitPane splitPane, JComponent comp, float oldFraction, int v1, int v2) {
    int w = comp.getWidth();
    splitPane.setDividerLocation((int)((oldFraction <= 0.01) ? v1 * w/v2 : w * oldFraction));
  }

  static public JPanel createPerformButton (String label, JList list){
    JPanel buttonPanel = new JPanel (new FlowLayout());
    JButton performButton = new JButton (label);
    performButton.addActionListener (new PerformButtonActionListener());
    buttonPanel.add (performButton);
    return buttonPanel;
 }

 static public String frameWithKb (Frame frame) {
  	return frame.getName() + ", " + frame.getKnowledgeBase();
 }

}
