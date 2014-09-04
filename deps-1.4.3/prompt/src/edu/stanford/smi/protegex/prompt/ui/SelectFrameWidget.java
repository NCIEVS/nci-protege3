 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class SelectFrameWidget extends GetValueWidget {
	protected JList _list;
    protected Object _selection = null;

    private KnowledgeBase _kb;
    private OntologySelectionCB _ontologySelection;
    String[] _prettyNamesKbs = ProjectsAndKnowledgeBases.getSourceProjectsPrettyNames();
    String _ontName = _prettyNamesKbs[0];

    protected String _prompt;
    String _choosePrompt = "Choose ";
    Class _frameType;
    boolean _chooseOntology;
    boolean _willBeModal;

    private AddFrameAction _addAction = null;

    private ViewFrameAction _viewAction;
    private RemoveFrameAction _removeAction;
    private LoadFrameAction _loadAction = null;
    String _loadPrompt = "Load ";
    String _addPrompt = "Select ";
    String _viewPrompt = "View ";
    String _removePrompt = "Remove ";

    public SelectFrameWidget (Class frameType, String suffix, String ontPrompt, int index,
    							boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
      super ();
      initialize(frameType, suffix, ontPrompt, index, chooseOntology, disableOntologyChoices, willBeModal);
    }

    public SelectFrameWidget (Class frameType, String suffix, String ontPrompt,
    						boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
      super ();
      initialize(frameType, suffix, ontPrompt, -1, chooseOntology, disableOntologyChoices, willBeModal);
    }

    public SelectFrameWidget (Class frameType, String suffix, boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
      super ();
      initialize(frameType, suffix, null, -1, chooseOntology, disableOntologyChoices, willBeModal);
    }

    public void initialize(Class frameType, String suffix, String ontPrompt, int index,
    						boolean chooseOntology, boolean disableOntologyChoices, boolean willBeModal) {
        _kb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
        _frameType = frameType;
        _chooseOntology = chooseOntology;
        _willBeModal = willBeModal;
        setPrompts(suffix, ontPrompt);
        setLayout (new BoxLayout(this, BoxLayout.Y_AXIS));
//        setAlignmentX(Component.LEFT_ALIGNMENT);
//        setLayout (new GridLayout (0, 1));

        if (ontPrompt == null)
        	ontPrompt = "Select ontology";
        if (_chooseOntology && index != -1) {
          _ontologySelection = new OntologySelectionCB();
          _ontologySelection.setSelectedIndex (index);
          _ontologySelection.setEnabled(!disableOntologyChoices);
          //LabeledComponent comboBox = new LabeledComponent (ontPrompt, _ontologySelection);
          add (_ontologySelection);
        }

        add (createChooseFramePanel ());

        doLayout();
    }

    public void postBuildTabChanged (Class frameType) {
		if (_kb != ProjectsAndKnowledgeBases.getTargetKnowledgeBase())
        	postChange (frameType);
    }

    private void postChange (Class frameType){
        	if (_frameType == frameType ||
            	(Cls.class.isAssignableFrom (_frameType) && !Slot.class.isAssignableFrom (frameType)))
        		_loadAction.setEnabled (true);
	        else
    	    	_loadAction.setEnabled (false);
    }

    public void postTargetTabChanged (Class frameType) {
		if (_kb == ProjectsAndKnowledgeBases.getTargetKnowledgeBase())
        	postChange (frameType);
    }

    private void setPrompts(String suffix, String ontPrompt) {
        _prompt = ontPrompt;
        _choosePrompt += suffix;
        _addPrompt += suffix;
        _viewPrompt += suffix;
		_loadPrompt += suffix;
        _removePrompt += suffix;
    }

    private JList createList () {
    	JList list = ComponentFactory.createSingleItemList(null);
        list.setCellRenderer(new FrameInMergingRenderer());
        return list;
    }

    private LabeledComponent createChooseFramePanel () {
        _list = createList();
        LabeledComponent c = new LabeledComponent(_choosePrompt, _list);
        addButtons (c);
//        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        return c;
    }

    public void setValue (Object o) {
     	if (o != null && PromptTab.moving() && ((Frame)o).isIncluded()) return;
     	_selection = o;
        ComponentUtilities.setListValues(_list, CollectionUtilities.createCollection(_selection));
        enableButtons();
    }

    public Object getValue  () {
    	return _selection;
    }

    public void clear () {
     	setValue (null);
    }

/*
    public void setDisplayedSelection (Object o) {
     	_selection = o;
        ComponentUtilities.setListValues(_list, CollectionUtilities.createCollection(_selection));
    }

*/

    private void  addButtons (LabeledComponent c) {
        if (!_willBeModal) {
        	_viewAction = new ViewFrameAction (_viewPrompt);
            c.addHeaderButton(_viewAction);
            _viewAction.setEnabled(false);

            _loadAction = new LoadFrameAction (_loadPrompt);
            c.addHeaderButton(_loadAction);
            _loadAction.setEnabled (true);
        }
    	if (_chooseOntology) {
	        _addAction = new AddFrameAction (this, _addPrompt);
        	c.addHeaderButton(_addAction);
        }
        _removeAction = new RemoveFrameAction (_removePrompt);
        c.addHeaderButton(_removeAction);
        enableButtons ();
   }

   public void enableButtons () {
	if (_viewAction != null)
	   	_viewAction.setEnabled(_selection != null);
    _removeAction.setEnabled(_selection != null);
    if (_loadAction != null)
	      loadActionSetEnabled ();
   }

    public void changeKnowledgeBase (String name) {
      _kb = ProjectsAndKnowledgeBases.getKnowledgeBase (name);
      if (_loadAction != null)
	      loadActionSetEnabled ();
    }

    private void loadActionSetEnabled () {
    	if (TabComponent.isSelected (_frameType, _kb)) {
			Frame selection = null;
	   		selection = TabComponent.getSelectionFromTree (_kb, _frameType); 
	   		if (selection != null && (!PromptTab.moving() || !((Frame)selection).isIncluded()))
				_loadAction.setEnabled (true);
			else
				_loadAction.setEnabled (false);
   		}
        else if (Cls.class.isAssignableFrom(_frameType) && TabComponent.isSelected (Instance.class, _kb))
            _loadAction.setEnabled (true);
        else
        	_loadAction.setEnabled (false);
    }

   public String toString () {
    	return "SelectFrameWidget";
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
      return ProjectsAndKnowledgeBases.getKnowledgeBase (_ontName);
    }
  }



  class LoadFrameAction extends AbstractAction {


     public LoadFrameAction(String prompt) {
       super(prompt, ComponentUtilities.loadImageIcon(SuggestionListPane.class, "images/Load.gif"));
     }

     public void actionPerformed(ActionEvent e) {
       Frame selection = null;
       selection = TabComponent.getSelectionFromTree (_kb, _frameType);
       if (selection != null)
    	   setValue (selection);
     }
  }

  class AddFrameAction extends AbstractAction {

    Component _component;


     public AddFrameAction(Component component, String prompt) {
       super(prompt, Icons.getAddIcon());
       _component = component;
     }

     public void actionPerformed(ActionEvent e) {
       Frame selection = null;
       if (_frameType == Cls.class)
         selection =
           edu.stanford.smi.protege.ui.DisplayUtilities.pickCls
                (_component, _kb, CollectionUtilities.createCollection (_kb.getRootCls()));

       if (_frameType == Slot.class)
          selection =
            edu.stanford.smi.protege.ui.DisplayUtilities.pickSlot
               (_component, _kb.getSlots());

       if (_frameType == Instance.class)
          selection =
            edu.stanford.smi.protege.ui.DisplayUtilities.pickInstance
               (_component, _kb);

       if (selection != null)
    	   setValue (selection);
       // ** add other stuff here
     }
  }


  class RemoveFrameAction extends AbstractAction {

    public RemoveFrameAction (String prompt) {
       super(prompt, Icons.getRemoveIcon());
    }

    public void actionPerformed(ActionEvent e) {
      clear ();
//      setText();
    }
  }


  class ViewFrameAction extends AbstractAction {

    public ViewFrameAction (String prompt) {
      super(prompt, Icons.getViewIcon());
    }

    public void actionPerformed(ActionEvent e) {
      if (_frameType == Cls.class ||
          _frameType == Instance.class ||
          _frameType == Slot.class)
        ProjectsAndKnowledgeBases.getProject (_kb).show ((Instance)_selection);
    }
  }
}


