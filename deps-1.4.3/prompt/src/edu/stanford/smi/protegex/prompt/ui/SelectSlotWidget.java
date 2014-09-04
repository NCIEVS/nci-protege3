 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import edu.stanford.smi.protege.model.Slot;

public class SelectSlotWidget extends SelectFrameWidget {

    public SelectSlotWidget (String ontPrompt, int index, boolean chooseOntology, boolean willBeModal) {
      super (Slot.class, "slot", ontPrompt, index, chooseOntology, false, willBeModal);
    }

    public SelectSlotWidget (String ontPrompt, int index, boolean chooseOntology, boolean disableOntologyChoices,  boolean willBeModal) {
      super (Slot.class, "slot", ontPrompt, index, chooseOntology, disableOntologyChoices, willBeModal);
    }

    public SelectSlotWidget (boolean chooseOntology, boolean willBeModal) {
      super (Slot.class, "slot", chooseOntology, false, willBeModal);
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