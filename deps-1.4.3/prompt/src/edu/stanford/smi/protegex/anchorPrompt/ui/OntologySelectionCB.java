/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class OntologySelectionCB extends JComboBox {
    static int numberOfKbs = AnchorPromptTab.getNumberOfKbs();
    String[] _prettyNamesKbs = new String [numberOfKbs];
    String _ontName;
    DefaultComboBoxModel _model;

    public OntologySelectionCB() {
      super ();
      System.arraycopy(AnchorPromptTab.getProjectsPrettyNames(), 0, _prettyNamesKbs, 0, numberOfKbs);
      _model = new DefaultComboBoxModel (_prettyNamesKbs);
      setModel (_model);
      setSelectedIndex(0);
      _ontName = (String)getSelectedItem();
      addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                _ontName = (String)cb.getSelectedItem();
            }
        });
    }

    public KnowledgeBase getSelectedKnowledgeBase () {
      return AnchorPromptTab.getKnowledgeBase (_ontName);
    }

    public void setSelectedKnowledgeBase (KnowledgeBase kb) {
      _ontName = AnchorPromptTab.getKnowledgeBasePrettyName(kb);
      this.setSelectedIndex(findIndex (_ontName, _prettyNamesKbs));
    }

    private int findIndex (Object elt, Object [] array) {
      for (int i = 0; i < array.length; i++) {
        String next = (String) array[i];
        if (elt.equals (next)) return i;
      }
      return -1;
    }
}
