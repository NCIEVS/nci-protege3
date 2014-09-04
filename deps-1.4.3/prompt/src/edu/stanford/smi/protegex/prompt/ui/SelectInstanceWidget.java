 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import edu.stanford.smi.protege.model.Instance;

public class SelectInstanceWidget extends SelectFrameWidget {

    public SelectInstanceWidget (String ontPrompt, int index, boolean chooseOntology, boolean willBeModal) {
      super (Instance.class, "instance", ontPrompt, index, chooseOntology, false, willBeModal);
    }

    public SelectInstanceWidget (String ontPrompt, int index, boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
      super (Instance.class, "instance", ontPrompt, index, chooseOntology, disableOntologyChoices, willBeModal);
    }

    public SelectInstanceWidget (boolean chooseOntology, boolean willBeModal) {
      super (Instance.class, "instance", chooseOntology, false, willBeModal);
    }

/*
    public void  initialize() {
      _choosePrompt = "Choose class";
      _frameType = Cls.class;
      _addPrompt = "Select class";
      _viewPrompt = "View class";
      _removePrompt = "Remove class";
    }
*/

}
