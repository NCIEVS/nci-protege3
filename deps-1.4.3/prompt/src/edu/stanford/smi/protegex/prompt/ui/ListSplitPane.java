 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.operation.*;

public abstract class ListSplitPane extends SelectableContainer {
    private JSplitPane _splitPane;
    private LabeledComponent _lc;
    public static final int TOP = 0;
    public static final int BOTTOM = 1;

    public void createListSplitPane (String label1, JComponent list1, String label2, JComponent list2,
    								int selection, boolean button) {
      setLayout (new BorderLayout());
      _splitPane = createListSplitPane (label1, list1, label2, list2);
      add (_splitPane, BorderLayout.CENTER);
      if (button)
        add (DisplayUtilities.createPerformButton(DisplayUtilities.getDoItString(), (selection==TOP) ? list1 : list2),
      		BorderLayout.SOUTH);
    }


    private JSplitPane  createListSplitPane (String label1, JComponent list1,
    										String label2,	JComponent list2) {
      _lc = new LabeledComponent (label1, list1, true);
      JComponent bottomPanel = new LabeledComponent (label2, list2, true);

      JSplitPane splitPane = ComponentFactory.createTopBottomSplitPane ();
      splitPane.setTopComponent(_lc);
      splitPane.setBottomComponent(bottomPanel);
      
      return splitPane;
    }

    public LabeledComponent getLabeledComponent () {return _lc;}

  public void reshape (int x, int y, int w, int h) {
    float oldFraction = 0;
    if (_splitPane != null)
       oldFraction = DisplayUtilities.rememberOldHeightRatio(_splitPane);
    super.reshape (x,y,w,h);
    if (_splitPane != null)
      DisplayUtilities.setNewHeightRatio(_splitPane, this, oldFraction, 2, 3);
  }

  public abstract void postChange ();
  public abstract void postChange (Operation o);
  public abstract void addSelectionListener ();

  public String toString () {
    return "ListSplitPane";
  }

}
