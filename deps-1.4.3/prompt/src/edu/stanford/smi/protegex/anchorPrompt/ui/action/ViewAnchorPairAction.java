/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;
import edu.stanford.smi.protegex.anchorPrompt.ui.*;

public  class ViewAnchorPairAction extends AbstractAction {
     private JList _list;

     public ViewAnchorPairAction(String prompt, JList list) {
      super(prompt, Icons.getViewIcon());
      _list = list;
    }

    public void actionPerformed(ActionEvent e) {
       AnchorPair selection = (AnchorPair)_list.getSelectedValue();
       CreateNewAnchorPairPane panel = new CreateNewAnchorPairPane (selection);
       if (ModalDialog.OPTION_OK == ModalDialog.showDialog (AnchorPromptTab.getMainWindow(), panel,
                                                            "Create Pair", ModalDialog.MODE_OK_CANCEL))  {
         AnchorPair a = panel.getAnchorPair();

         ComponentUtilities.replaceListValue(_list, selection, a);
         _list.setSelectedValue(a, true);
       }
    }
  }

