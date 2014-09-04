/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public  class RemoveAnchorPairAction extends AbstractAction {
     private JList _list;

     public RemoveAnchorPairAction(String prompt, JList list) {
       super(prompt, Icons.getRemoveIcon());
      _list = list;
    }

    public void actionPerformed(ActionEvent e) {
      AnchorPair selected = (AnchorPair)_list.getSelectedValue();
      ComponentUtilities.removeListValue(_list, selected);
      AnchorPromptTab.removeAnchorPair(selected);
    }
  }


