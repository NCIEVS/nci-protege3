 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.prompt.Preferences;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.ui.OntologySelectionCB;

public class ConfigureAction extends AbstractAction {
    private ConfigurePanel _configurePanel;

    public ConfigureAction () {
      super ("Configure...");
    }

    public void actionPerformed (ActionEvent e) {
      _configurePanel = new ConfigurePanel ();
      int confirmation = ModalDialog.showDialog (PromptTab.getMainWindow(),
                                               _configurePanel, "Warning", ModalDialog.MODE_OK_CANCEL);
      if (confirmation == ModalDialog.OPTION_OK)
        configure (_configurePanel);
    }

    private void configure (ConfigurePanel configurePanel) {
      if (configurePanel.inheritedSlotsChecked ())
        Preferences.considerInheritedSlots(true);
      else
        Preferences.considerInheritedSlots(false);

      if (configurePanel.caseSensitiveChecked())
        Preferences.caseSensitiveConflicts(true);
      else
        Preferences.caseSensitiveConflicts(false);

      if (configurePanel.approximateMatchChecked())
        Preferences.approximateMatch(true);
      else
        Preferences.approximateMatch(false);

      if (configurePanel.preferredChecked()) {
        Preferences.preferredOntology(true);
        Preferences.setPreferredOntology(configurePanel.getPreferredOntology());
//        Preferences.discardNonPreferredSlots(configurePanel.discardNonPreferredSlots());
      }
      else {
        Preferences.preferredOntology(false);
//        Preferences.discardNonPreferredSlots(false);
	  }

    }

    class ConfigurePanel extends JPanel {
      JCheckBox _inheritedSlots;
      JCheckBox _caseSensitive;
      JCheckBox _approximateMatch;
      PreferredOntologyPanel _preferred;

      ConfigurePanel () {
        super();

        setLayout (new BorderLayout ());
		JPanel checkBoxes = new JPanel ();
        checkBoxes.setLayout (new GridLayout (0, 1));
        _inheritedSlots = new JCheckBox ("Consider inherited slots");
        _inheritedSlots.setSelected(Preferences.considerInheritedSlots());
        checkBoxes.add (_inheritedSlots);
        _caseSensitive = new JCheckBox ("Case sensitive ontology");
        _caseSensitive.setSelected(Preferences.caseSensitiveConflicts());
        checkBoxes.add (_caseSensitive);
        checkBoxes.add (new JLabel (""));

        add (checkBoxes, BorderLayout.NORTH);

        _preferred = new PreferredOntologyPanel();
        _preferred.setSelected(Preferences.preferredOntology());
        _preferred.setOntology(Preferences.preferredOntology());

        add (_preferred, BorderLayout.SOUTH);
      }



      boolean inheritedSlotsChecked () {
        return _inheritedSlots.isSelected();
      }

      boolean caseSensitiveChecked () {
        return _caseSensitive.isSelected();
      }

      boolean approximateMatchChecked () {
      	return Preferences.approximateMatch();
      }

      boolean preferredChecked () {
        return _preferred.isSelected();
      }

      boolean discardNonPreferredSlots () {
      	return Preferences.discardNonPreferredSlots();
//       	return _preferred.discardNonPreferredSlots();
      }

      KnowledgeBase getPreferredOntology () {
        return _preferred.getPreferredOntology();
      }
    }

      class  PreferredOntologyPanel  extends JPanel {
        JCheckBox _checkBox;
        JCheckBox _discardNonPreferredSlots;
        OntologySelectionCB _ontology;

        PreferredOntologyPanel () {
//          setLayout (new GridLayout (0, 2));

          setLayout (new BorderLayout());

          _checkBox = new JCheckBox ("Set preferred ontology");
//          _checkBox.addActionListener(new ActionListener() {
//          	public void actionPerformed(ActionEvent e) {
//				_discardNonPreferredSlots.setEnabled (_checkBox.isSelected());
//            }
//          });

          add (_checkBox, BorderLayout.WEST);
          _ontology = new OntologySelectionCB();
          add (_ontology, BorderLayout.EAST);

//          _discardNonPreferredSlots = new JCheckBox ("Discard slots from non-preferred ontology");
//          _discardNonPreferredSlots.setSelected(Preferences.discardNonPreferredSlots());
//          _discardNonPreferredSlots.setEnabled(Preferences.preferredOntology()!=null );
//          add (_discardNonPreferredSlots, BorderLayout.SOUTH);
        }

        void setOntology (KnowledgeBase kb) {
          if (kb != null)
            _ontology.setSelectedKnowledgeBase(kb);
        }

        void setSelected (KnowledgeBase kb) {
          if (kb != null)
            _checkBox.setSelected (true);
          else
            _checkBox.setSelected (false);
        }

        boolean  isSelected () {
          return _checkBox.isSelected();
        }

        boolean discardNonPreferredSlots () {
          return _discardNonPreferredSlots.isSelected();
        }

        KnowledgeBase getPreferredOntology () {
          if (isSelected())
            return _ontology.getSelectedKnowledgeBase();
          else
            return null;
        }
      }

}

