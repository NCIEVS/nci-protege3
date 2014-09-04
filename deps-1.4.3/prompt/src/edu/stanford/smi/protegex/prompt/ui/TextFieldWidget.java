 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;

import javax.swing.JTextField;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;

public class TextFieldWidget extends GetValueWidget {
    JTextField _textField;

    public TextFieldWidget (String prompt) {
      	_textField = ComponentFactory.createTextField();
        _textField.setEnabled(true);

        LabeledComponent c = new LabeledComponent(prompt, _textField);
        setLayout (new BorderLayout());
        add (c, BorderLayout.CENTER);
    }

    public Object getValue() {
    	return _textField.getText();
    }

    public void setValue (Object o) {
      _textField.setText((String) o);
    }

    public void clear () {
      _textField.setText (null);
    }
}

