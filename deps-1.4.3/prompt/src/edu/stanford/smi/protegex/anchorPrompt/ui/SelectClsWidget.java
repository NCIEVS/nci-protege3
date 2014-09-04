/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class SelectClsWidget extends GetFrameValueWidget {
    private KnowledgeBase _kb;
    private OntologySelectionCB _ontologySelection;
    String[] _prettyNamesKbs = AnchorPromptTab.getProjectsPrettyNames();
    String _ontName = _prettyNamesKbs[0];

    String _choosePrompt = "Choose ";

    private AddClsAction _addAction;
    private RemoveClsAction _removeAction;
    String _addPrompt = "Select ";
    String _removePrompt = "Remove ";

    public SelectClsWidget (String ontPrompt, int index) {
      super ();
      initialize(ontPrompt, index);
    }

    public SelectClsWidget (String ontPrompt) {
      super ();
      initialize(ontPrompt, -1);
    }

    public SelectClsWidget () {
      super ();
      initialize(null, -1);
    }

    public void initialize(String ontPrompt, int index) {
        _kb = AnchorPromptTab.getTargetKnowledgeBase();
        setPrompts(ontPrompt);
        setLayout (new BoxLayout(this, BoxLayout.Y_AXIS));

        if (ontPrompt == null)
        	ontPrompt = "Select ontology";

        if (index != -1) {
          _ontologySelection = new OntologySelectionCB();
          _ontologySelection.setSelectedIndex (index);
          LabeledComponent comboBox = new LabeledComponent (ontPrompt, _ontologySelection);
          add (comboBox);
        }

        add (createChooseFramePanel ());
        doLayout();
    }

    private void setPrompts(String ontPrompt) {
        _prompt = ontPrompt;
    }


    private LabeledComponent createChooseFramePanel () {
      	_textField = createTextField();
        _textField.setEnabled(false);

        LabeledComponent c = new LabeledComponent(_choosePrompt, _textField);
        addButtons (c);
        return c;
    }

    protected void setText() {
    	String text;
        if (_selection == null) {
        	text = "";
        } else {
        	text = ((Frame)_selection).getBrowserText();
        }
    	_textField.setText(text);
    }

    private void  addButtons (LabeledComponent c) {
        _addAction = new AddClsAction (this, _addPrompt);
        _removeAction = new RemoveClsAction (_removePrompt);

        c.addHeaderButton(_addAction);
        c.addHeaderButton(_removeAction);
   }

    public void changeKnowledgeBase (String name) {
      _kb = AnchorPromptTab.getKnowledgeBase (name);
    }

  class OntologySelectionCB extends JComboBox {
    DefaultComboBoxModel _model;

    public OntologySelectionCB() {
      super ();
      _model = new DefaultComboBoxModel (_prettyNamesKbs);
      setModel (_model);
      setSelectedIndex(0);
      addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String _ontName = (String)cb.getSelectedItem();
                changeKnowledgeBase (_ontName);
            }
        });
    }

    public KnowledgeBase getSelectedKnowledgeBase () {
      return AnchorPromptTab.getKnowledgeBase (_ontName);
    }
  }


  class AddClsAction extends AbstractAction {
    Component _component;

     public AddClsAction(Component component, String prompt) {
       super(prompt, Icons.getAddIcon());
       _component = component;
     }

     public void actionPerformed(ActionEvent e) {
       Frame selection = null;
         selection =
           edu.stanford.smi.protege.ui.DisplayUtilities.pickCls
                (_component, _kb, CollectionUtilities.createCollection (_kb.getRootCls()));
       if (selection != null)
    	   setDisplayedSelection (selection);

     }
  }

  class RemoveClsAction extends AbstractAction {

    public RemoveClsAction (String prompt) {
       super(prompt, Icons.getRemoveIcon());
    }

    public void actionPerformed(ActionEvent e) {
      replaceSelection (null);
      setText();
    }
  }

}



