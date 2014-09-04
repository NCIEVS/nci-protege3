 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;

// checkbox and a checkbox with value in parens:
// V slot values ( V depth limit: __ )
public class CheckBoxWithValueWidget extends GetValueWidget {
    private JCheckBox _checkBox1;
    private JCheckBox _checkBox2;
    private JTextField _textField;
    private Boolean _defaultValue1 = null;
    private Boolean _defaultValue2 = null;
    private Integer _value = new Integer(0); //0 means no limit

    public CheckBoxWithValueWidget (String prompt1, String prefix, String prompt2, String suffix) {
        initialize(prompt1, prefix, prompt2, suffix);
    }

    public CheckBoxWithValueWidget (String prompt1, boolean selected1, String prefix, String prompt2, String suffix, boolean selected2) {
        initialize(prompt1, prefix, prompt2, suffix);
        _checkBox1.setSelected(selected1);
        _checkBox2.setSelected(selected2);
        _defaultValue1 = new Boolean (selected1);
        _defaultValue2 = new Boolean (selected2);
    }

    private void initialize (String prompt1, String prefix, String prompt2, String suffix) {

        _checkBox1 = ComponentFactory.createCheckBox (prompt1);

        JLabel prefixLabel = ComponentFactory.createLabel();
        prefixLabel.setText(prefix);

      	_checkBox2 = ComponentFactory.createCheckBox (prompt2);

        _textField = ComponentFactory.createTextField();
        _textField.setColumns(3);

        JLabel suffixLabel = ComponentFactory.createLabel();
        suffixLabel.setText(suffix);

	setLayout (new BorderLayout());

        JPanel westPanel = new JPanel (new BorderLayout());
        westPanel.add(_checkBox1, BorderLayout.CENTER);

        JPanel eastPanel = new JPanel (new BorderLayout());
        eastPanel.add(prefixLabel, BorderLayout.WEST);
        eastPanel.add (_checkBox2, BorderLayout.CENTER);

        JPanel textBoxPanel = new JPanel (new BorderLayout());
        textBoxPanel.add (_textField, BorderLayout.CENTER);
        textBoxPanel.add (suffixLabel, BorderLayout.EAST);

        eastPanel.add(textBoxPanel, BorderLayout.EAST);

        add (westPanel, BorderLayout.WEST);
        add (eastPanel, BorderLayout.CENTER);
    }

    public int getIntegerValue () {
      _value = new Integer(0);
      if (_checkBox2.isSelected()) {
        String text = _textField.getText();
        try {
          _value = new Integer(text);
        } catch (NumberFormatException e) {
          return 0;
        } //will assume it's 0

      }
      return _value.intValue();
    }

    public Object getValue () {
      return new Boolean (_checkBox1.isSelected());
    }

    public void setValue (Object o) {
      if (o instanceof Boolean)
   	    _checkBox1.setSelected(((Boolean)o).booleanValue());
      else
      	Log.getLogger().severe ("Silently fail..........Ray says");
    }

    public void setValue (boolean value) {
      _checkBox1.setSelected(value);
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