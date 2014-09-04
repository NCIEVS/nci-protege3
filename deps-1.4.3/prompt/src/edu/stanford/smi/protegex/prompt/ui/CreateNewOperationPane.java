 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.UnaryFunction;
import edu.stanford.smi.protege.util.Validatable;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.operation.editor.Editor;

public class CreateNewOperationPane extends JPanel implements Validatable {
  private static  Object[] _allowedOperationsArray = Editor.getAllowedOperations().toArray();
  private static  String[] _allowedOperationsPrettyNames = setOperationsPrettyNames ();
  private Editor [] _editors = new Editor [Editor.numberOfOperations()];
  private JComboBox _allowedOperationsList;
  private boolean _chooseOntology = true;
  private boolean _willBeModal = true;
  private JDialog _parent = null;

  private JPanel _cards = new JPanel();
  private int _selectedIndex = -1;

  private Operation _originalOperation = null;
  private Operation _operation = null;

  private static final int OPTION_DO_IT = 1;
  private static final int OPTION_ADD = 2;
  private static final int OPTION_CANCEL = 3;
  private static final int OPTION_CLEAR = 4;


  public CreateNewOperationPane (boolean chooseOntology, boolean willBeModal) {
    initialize(null, null, chooseOntology, willBeModal);
  }

  public CreateNewOperationPane (Operation a, JDialog parent) {
    initialize (a, parent, _chooseOntology, _willBeModal);
  }

  public CreateNewOperationPane (JDialog parent) {
    initialize (null, parent, _chooseOntology, _willBeModal);
  }

  private void initialize (Operation a, JDialog parent,
                            boolean chooseOntology, boolean willBeModal) {
    _originalOperation = a;
    _chooseOntology = chooseOntology;
    _willBeModal = willBeModal;
    _parent = parent;
    if (a != null) {
    	String name = a.getPrettyName();
    	for (int i = 0; i < Editor.numberOfOperations(); i++)
      		if (_allowedOperationsPrettyNames[i].equals (name))  {
        	  _selectedIndex = i;
        	  break;
   			}
    }
    createCardsPanel(a);
    setLayout (new BorderLayout());

    JPanel comboBoxAndLabelsPanel = createOperationSelectionComboBox (a);
    add (comboBoxAndLabelsPanel, BorderLayout.NORTH);

    add (_cards, BorderLayout.CENTER);
    add (createButtons (), BorderLayout.SOUTH);
  }

  private JComponent createButtons () {
    JPanel buttons = ComponentFactory.createPanel();
//    buttons.setLayout(new GridLayout(1, 0, 10, 10));
    buttons.setLayout(new BorderLayout());
    JPanel inside = new JPanel ();
    JButton button = createButton (OPTION_DO_IT, DisplayUtilities.getDoItString(), Icons.getOkIcon());
    inside.add(button);
    buttons.add (inside, BorderLayout.SOUTH);

//    buttons.add (createButton (OPTION_ADD, "Put on ToDo list", null));
    if (_willBeModal)
    	buttons.add (createButton (OPTION_CANCEL, "Cancel", Icons.getCancelIcon()));

    return buttons;
  }

  private void performAction (int result) {
    	if (_willBeModal && result == OPTION_CANCEL) {
            _parent.dispose();
            return;
        }
        if (result == OPTION_CLEAR) {
        	clearCurrentEditor ();
         	return;
        }
        if (validateContents ()) {
	  if (_willBeModal)
            	_parent.dispose();
        	if (result == OPTION_DO_IT)
                _operation.performOperation();
            else if (result == OPTION_ADD) {
             	if (_originalOperation != null)
                	_operation.setPriority(_originalOperation.getPriority());
                if (_operation != _originalOperation)
            		SuggestionsAndConflicts.addToDoListOperation (_operation, _originalOperation, true);
            }
        }

	}

	private JButton createButton(final int result, String text, Icon icon) {
		javax.swing.Action action =
			new AbstractAction(text, icon) {
				public void actionPerformed(ActionEvent event) {
					performAction(result);
				}
            }
            ;
        JButton button = ComponentFactory.createButton(action);
		return button;
	}

  void createCardsPanel (Operation a) {
    _cards.setLayout(new CardLayout());
      for (int i = 0; i < Editor.numberOfOperations(); i++) {
        OperationPanel operationPanel;
        JPanel panel;
        if (_selectedIndex == i) {
          operationPanel = Editor.createEditBox (a, _willBeModal);
        }
        else {
          operationPanel = Editor.createActionBox ((Class)_allowedOperationsArray[i],
          											_chooseOntology, _willBeModal);
        }

        panel = operationPanel.getPanel ();
        _editors [i] = operationPanel.getEditor ();
        _cards.add (panel, (String)_allowedOperationsPrettyNames[i]);
      }
  }



  private static String [] setOperationsPrettyNames () {
	String [] result = new String [Editor.numberOfOperations()];

    for (int i = 0; i < Editor.numberOfOperations(); i++) {
      	result[i] = (String)Editor.getPrettyName ((Class)_allowedOperationsArray[i]);
    }
    return result;
  }

  private JPanel  createOperationSelectionComboBox (Operation a){
    JPanel p = new JPanel();
    p.setLayout (new GridLayout (0,1));

    _allowedOperationsList = new JComboBox (_allowedOperationsPrettyNames);
    if (a == null) _selectedIndex = 0;
    _allowedOperationsList.setSelectedIndex (_selectedIndex );
    CardLayout cl = (CardLayout)(_cards.getLayout());
    if (a != null)
      cl.show (_cards, a.getPrettyName());

    _allowedOperationsList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
         JComboBox cb = (JComboBox)e.getSource();
         _selectedIndex = cb.getSelectedIndex();
         CardLayout cl = (CardLayout)(_cards.getLayout());
         cl.show(_cards, (String)e.getItem());
      }
    });


    p.add (_allowedOperationsList);

    p.doLayout();

    return p;
  }

  public void setEditor(Class editor) {
	  for(int i = 0; i < _allowedOperationsPrettyNames.length; i++) {
		  if(_allowedOperationsPrettyNames[i].equals(Editor.getPrettyName(editor))) {
			  _selectedIndex = i;
			  _allowedOperationsList.setSelectedIndex(_selectedIndex);
		  }
	  }
  }

  public Operation getOperation () {return _operation;}


  public void addArgument (Frame frame) {

  	_editors[_selectedIndex].addArgument (frame);

  }


  private void clearCurrentEditor () {

  	Editor selectedEditor = (Editor)_editors[_selectedIndex];

	selectedEditor.clear ();

  }


  public boolean validateContents() {

    Editor selectedEditor = (Editor)_editors[_selectedIndex];

    Operation a = selectedEditor.collectData ();
    boolean allArgumentsDefined = a.allArgumentsDefined();
    String allArgumentsValid = a.allArgumentsValid();

    if (allArgumentsDefined && allArgumentsValid == null)  {
        clearCurrentEditor ();
      	_operation = (Operation)a;
        _operation.setFromUser(true);
        return true;
    } 
    if (!allArgumentsDefined) {
    	Warning.inform ("Some arguments are not defined");
	    return false;
    } else { //not all arguments valid
    	Warning.inform ("Cannot move: " + allArgumentsValid);
    	return false;
    }
	
  }

  public void postBuildTabChanged (Class frameType) {
  	postChange (frameType, false);
  }

  public void postTargetTabChanged (Class frameType) {
  	postChange (frameType, true);
  }

  private void postChange (final Class frameType, final boolean target) {
   	ComponentUtilities.applyToDescendents(this, new UnaryFunction () {
    	public Object apply(Object o) {
         	if (o instanceof SelectFrameWidget) {
            	SelectFrameWidget widget = (SelectFrameWidget)o;
            	if (target)
             		widget.postTargetTabChanged (frameType);
                else
                	widget.postBuildTabChanged (frameType);
            }
            return null;
        }
    });
  }

  public String toString () {
   	return "CreateNewOperationPane";
  }

  public void  saveContents() {};

}






