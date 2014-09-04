/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

public class GetFrameValueWidget extends GetValueWidget{
  	protected JTextField _textField;
    protected Object _selection;

    protected String _prompt;

    public Collection getSelection() {
    	return CollectionUtilities.createCollection(_selection);
    }

    public Object getValue() {
    	return _selection;
    }

    public void clear () {
      removeDisplayedSelection ();
    }

    public JTextField getTextField() {
    	return _textField;
    }

    public JTextField createTextField() {
    	return ComponentFactory.createTextField();
    }

    protected void setDisplayedSelection (Object selection) {
    	  replaceSelection (selection);
        setText();
    }

    protected void replaceSelection (Object selection) {
        _selection = selection;
    }

    public void setValue (Object value) {
      _selection = value;
      setText();
    }

    protected void setText() {
    	String text;
        if (_selection == null) {
        	text = "";
        } else {
        	text = (String)_selection;
        }
    	_textField.setText(text);
    }

    protected void removeDisplayedSelection() {
    	replaceSelection(null);
      setText();
    }


}
