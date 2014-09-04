 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import edu.stanford.smi.protege.model.*;

public class SelectClsWidget extends SelectFrameWidget {

    public SelectClsWidget (String ontPrompt, int index, boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
      super (Cls.class, "class", ontPrompt, index, chooseOntology, disableOntologyChoices, willBeModal);
    }

    public SelectClsWidget (String ontPrompt, int index, boolean chooseOntology, boolean willBeModal) {
      super (Cls.class, "class", ontPrompt, index, chooseOntology, false, willBeModal);
    }

    public SelectClsWidget (String ontPrompt, boolean chooseOntology, boolean willBeModal) {
      super (Cls.class, "class", ontPrompt, chooseOntology, false, willBeModal);
    }

    public SelectClsWidget (boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
      super (Cls.class, "class", chooseOntology, false, willBeModal);
    }

}
