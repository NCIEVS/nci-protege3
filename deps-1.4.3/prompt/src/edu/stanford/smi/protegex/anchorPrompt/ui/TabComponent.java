/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class TabComponent extends JPanel {
  private SelectAnchorsPane _selectAnchorsPane;
  private ResultsPane _resultsPane;
  private JSplitPane _splitPane;

  public TabComponent () {
    setLayout (new BorderLayout ());
    add(createContentPane(), BorderLayout.CENTER);
    setVisible (true);
  }

    private JSplitPane createContentPane () {

      _selectAnchorsPane = new SelectAnchorsPane();
      _resultsPane = new ResultsPane ();
      _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   _selectAnchorsPane, _resultsPane);
      _splitPane.setOneTouchExpandable(false);
      return _splitPane;
  }

  public void anchorsChanged (boolean changed) {
    if (changed)
      _selectAnchorsPane.postChange (true);
  }

  public void resultsChanged (boolean changed, Collection results) {
    if (changed)
      _resultsPane.postChange (true, results);
  }

    public void reshape (int x, int y, int w, int h) {
      float oldFraction = DisplayUtilities.rememberOldWidthRatio(_splitPane);
      super.reshape (x,y,w,h);
      DisplayUtilities.setNewWidthRatio(_splitPane, this, oldFraction, 1, 2);
    }

}

