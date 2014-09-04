/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui.action;

import java.awt.event.*;

import edu.stanford.smi.protegex.anchorPrompt.*;
import edu.stanford.smi.protegex.anchorPrompt.analysis.*;

public  class PerformButtonActionListener implements ActionListener {
    public PerformButtonActionListener () {
      super();
    }

    public void actionPerformed(ActionEvent e) {
		AnchorPromptTab.setResults(Analysis.analyze(AnchorPromptTab.getCurrentAnchors()));
   }
  }


