 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;

public class CheckBoxWidget extends GetValueWidget {
    private JCheckBox _checkBox;
    private Boolean _defaultValue = null;

    public CheckBoxWidget (String prompt) {
        initialize(prompt);
    }

    public CheckBoxWidget (String prompt, boolean selected) {
        initialize(prompt);
        _checkBox.setSelected(selected);
        _defaultValue = new Boolean (selected);
    }

    public CheckBoxWidget (String prompt, boolean selected, boolean enabled) {
        initialize(prompt);
        _checkBox.setSelected(selected);
        _defaultValue = new Boolean (selected);
        _checkBox.setEnabled(enabled);
    }

    private void initialize (String prompt) {
      	_checkBox = ComponentFactory.createCheckBox (prompt);
		setLayout (new BorderLayout());
        add (_checkBox, BorderLayout.WEST);

    }

    public Object getValue() {
    	return new Boolean (_checkBox.isSelected());
    }

    public void setValue (Object o) {
      if (o instanceof Boolean)
   	    _checkBox.setSelected(((Boolean)o).booleanValue());
      else
      	Log.getLogger().severe ("Silently fail..........Ray says");
    }

    public void setValue (boolean value) {
      _checkBox.setSelected(value);
    }

    public void clear () {
//    	if (_defaultValue != null)
//	    	_checkBox.setSelected(_defaultValue.booleanValue());
//        else
  		//do nothing - keep the old selection
//        ;
    }

}

