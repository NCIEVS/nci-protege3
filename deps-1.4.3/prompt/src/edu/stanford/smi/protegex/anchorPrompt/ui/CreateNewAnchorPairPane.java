/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class CreateNewAnchorPairPane extends JPanel implements Validatable {
  JComboBox _relatedValues;
  GetValueWidget [] widgets = new GetValueWidget [3];

  AnchorPair _pair = new AnchorPair ();

  public CreateNewAnchorPairPane (AnchorPair a) {
    initialize (a);
  }

  public CreateNewAnchorPairPane () {
    initialize(null);
  }

  private void initialize (AnchorPair a) {
    setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));

    for (int i = 0; i <= 1; i++) {
      	SelectClsWidget next = new SelectClsWidget ("Ontology " + (i+1), i);
        next.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        widgets[i] = next;
		add(next);
    }

  }

  public AnchorPair getAnchorPair () {return _pair;}

  public boolean validateContents() {
	for (int i = 0; i <= 1; i++) {
        GetValueWidget next = (GetValueWidget)widgets[i];
        if (next.getValue() == null) return false;
        _pair.setArgument((Cls)next.getValue(), i);
    }
    return true;
  }

  public void  saveContents() {};

}

