/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;
import edu.stanford.smi.protegex.anchorPrompt.ui.action.*;

public class SelectAnchorsPane extends JPanel {
  private Collection _currentAnchorPairs;
  private JList _list;
  static private AbstractAction _createAction;
  static private AbstractAction _viewAction;
  static private AbstractAction _removeAction;

  SelectAnchorsPane () {
    _list = initializeSuggestionList ();
    initialize();
  }

  private void initialize () {
	_list.setCellRenderer(new AnchorPairRenderer());
    LabeledComponent lc = new LabeledComponent ("Anchors", new JScrollPane (_list));
    setLayout (new BorderLayout());
    addButtons (lc);
    add (lc, BorderLayout.CENTER);
    add (createPerformButton("Go"), BorderLayout.SOUTH);
  }

  private JList initializeSuggestionList () {
    JList list = ComponentFactory.createList(null);
    _currentAnchorPairs = AnchorPromptTab.getCurrentAnchors();
    ComponentUtilities.setListValues(list, _currentAnchorPairs);
    return list;
 }


  private void addButtons (LabeledComponent c) {
  	_createAction = new CreateAnchorPairAction ("Create pair", _list);
  	_viewAction = new ViewAnchorPairAction ("View pair", _list);
  	_removeAction = new RemoveAnchorPairAction ("Remove pair", _list);

    c.addHeaderButton(_viewAction);
    c.addHeaderButton(_createAction);
    c.addHeaderButton(_removeAction);
    disableButtonsForEmptyList (true);
  }


  public void postChange (boolean changed) {
    if (changed) {
      _currentAnchorPairs = AnchorPromptTab.getCurrentAnchors();
      ((SimpleListModel)_list.getModel()).clear();
      ComponentUtilities.setListValues(_list, _currentAnchorPairs);
      if (((SimpleListModel)_list.getModel()).getSize() > 0) {
        _list.setSelectedIndex (0);
    	disableButtonsForEmptyList (false);
      } else
    	disableButtonsForEmptyList (true);
    }
  }

  private void disableButtonsForEmptyList (boolean v) {
    _viewAction.setEnabled(!v);
    _createAction.setEnabled (true);
    _removeAction.setEnabled (!v);
  }

  public String toString () {
    return "SuggestionListPane";
  }

  static public JPanel createPerformButton (String label){
    JPanel buttonPanel = new JPanel (new FlowLayout());
    JButton performButton = new JButton (label);
    performButton.addActionListener (new PerformButtonActionListener());
    buttonPanel.add (performButton);
    return buttonPanel;
 }


}

