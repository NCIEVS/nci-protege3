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

public  class CreateAnchorPairAction extends AbstractAction {
     private JList _list;

     public CreateAnchorPairAction (String prompt, JList list) {
       super(prompt, Icons.getCreateIcon());
       _list = list;
     }

     public void actionPerformed(ActionEvent e) {
       CreateNewAnchorPairPane panel = new CreateNewAnchorPairPane ();
       if (ModalDialog.OPTION_OK == ModalDialog.showDialog (AnchorPromptTab.getMainWindow(), panel,
                                                            "Create Pair", ModalDialog.MODE_OK_CANCEL))  {
         AnchorPair a = panel.getAnchorPair();
		 AnchorPromptTab.addAnchorPair(a);
       }

     }
  }


