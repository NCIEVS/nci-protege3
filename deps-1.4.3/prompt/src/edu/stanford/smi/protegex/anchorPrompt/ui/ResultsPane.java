/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class ResultsPane extends JPanel {
    private Collection _currentResults;
    private JList _resultList = null;
    private AnchorPairRenderer _renderer = new AnchorPairRenderer ();

    ResultsPane() {
      createCurrentResults ();
      _resultList.setCellRenderer(_renderer);
      setLayout (new BorderLayout());
      LabeledComponent lc =  new LabeledComponent ("Results", new JScrollPane (_resultList));
      add (lc, BorderLayout.CENTER);
    }

    private JList createCurrentResults () {
      _resultList = ComponentFactory.createList(null);
      _currentResults = AnchorPromptTab.getCurrentAnchors();
      ComponentUtilities.setListValues(_resultList, _currentResults);

      return _resultList;
   }

  public void postChange (boolean changed, Collection results) {
    if (changed) {
      _currentResults = results;
      ((SimpleListModel)_resultList.getModel()).clear();
      ComponentUtilities.setListValues(_resultList, _currentResults);
      if (((SimpleListModel)_resultList.getModel()).getSize() > 0) 
        _resultList.setSelectedIndex (0);
    }
  }


}
