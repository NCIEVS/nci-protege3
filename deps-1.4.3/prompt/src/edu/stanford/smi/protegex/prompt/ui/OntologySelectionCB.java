 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;

// This class is used only in the preferences dialog.
public class OntologySelectionCB extends JComboBox {
    static int numberOfKbs = ProjectsAndKnowledgeBases.getNumberOfKbs();
    String[] _prettyNamesKbs = new String [numberOfKbs];
    String _ontName;
    DefaultComboBoxModel _model;
//    final static String SUFFIX = " ontology";
// to add suffix to lines - reactivate the commented lines and removes the lines below them

    public OntologySelectionCB() {
      super ();
      System.arraycopy(ProjectsAndKnowledgeBases.getSourceProjectsPrettyNames(), 0, _prettyNamesKbs, 0, numberOfKbs);
//      _prettyNamesKbs = addSuffixToOntologyNames (_prettyNamesKbs);
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
      return ProjectsAndKnowledgeBases.getKnowledgeBase (_ontName);
//      return PromptTab.getKnowledgeBase (_ontName.substring (0, _ontName.indexOf(SUFFIX)));
    }

    public void setSelectedKnowledgeBase (KnowledgeBase kb) {
      _ontName = ProjectsAndKnowledgeBases.getKnowledgeBasePrettyName(kb);
      this.setSelectedIndex(findIndex (_ontName, _prettyNamesKbs));
    }

/*
    private String[] addSuffixToOntologyNames (String[] list) {
      if (list == null) return null;
      for (int i = 0; i < list.length; i++)
        list[i] += SUFFIX;
      return list;
    }
*/
    private int findIndex (Object elt, Object [] array) {
      for (int i = 0; i < array.length; i++) {
        String next = (String) array[i];
//        if (elt.equals (next.substring (0, next.indexOf(SUFFIX)))) return i;
        if (elt.equals (next)) return i;
      }
      return -1;
    }
}
