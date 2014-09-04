 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;


public class MultipleOptionsDialog {
/*
  private OptionsPanel [] _optionsPanels;
  private String title = "Select own slot values for merged frame";
//  private Object[] buttonRowObjects = new Object[] {"Ok", "Cancel"};

  public Collection showMultipleOptionsDialog (Window parent, Collection alternatives) {
    if (options == null) return "";
    Collection result = new ArrayList();
    Iterator i = alternatives.iterator();
    int index = 0;

    while (i.hasNext())
      _optionsPanels[index++] = new OptionsPanel ((String[])i.next());

    int value = JOptionPane.showOptionDialog (parent, _optionsPanels, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, null, null);
    if (value == JOptionPane.CLOSED_OPTION || value == JOptionPane.CANCEL_OPTION)
      return null;
    else
      return _optionsPanel.getSelectedName();
  }

  public class OptionsPanel extends JPanel {
    private ButtonGroup _group = new ButtonGroup();
    private JRadioButton [] _buttons;
    private RadioButtonWithText _otherButton = new RadioButtonWithText ();

    OptionsPanel (String[] options) {
      setBorder (BorderFactory.createTitledBorder ("Frame name:"));
      JRadioButton nextButton;
      _buttons = new JRadioButton [options.length + 1];

      for (int i = 0; i < options.length; i++)  {
        nextButton = new JRadioButton (options[i]);
        if (i == 0) nextButton.setSelected (true);
        add (nextButton);
        _buttons[i] = nextButton;
        _group.add (nextButton);
      }
      add (_otherButton.getPanel());
      _buttons[options.length] = _otherButton;
      _group.add (_otherButton.getButton());
    }

    public String getSelectedName () {
      String result = null;

      for (int i = 0; i < _buttons.length - 1; i++) {
        if (_buttons[i].isSelected())
            result = _buttons[i].getText();
      }

      if (_otherButton.getButton().isSelected())
        result = _otherButton.getText();
      return result;
    }

    public class RadioButtonWithText extends JRadioButton {
      JPanel _me = new JPanel ();
      JRadioButton _button;
      JTextField _textField = new JTextField (15);

      RadioButtonWithText () {
        _me.setLayout (new FlowLayout());

        _button = new JRadioButton ("Other:");
        _me.add (_button);
        _me.add (_textField);
        _textField.setEditable (true);

        _textField.addActionListener (new ActionListener () {
           public void actionPerformed (ActionEvent e) {
             _button.setSelected (true);
           }
        });

      }

      public JPanel getPanel () { return _me;}

      public JRadioButton getButton () { return _button;}

      public String getText () {
Log.trace ("text: " + _textField.getText(), this, "getText");
        return (_textField.getText());
      }
    } // class RadioButtonWithText

   } // class OptionsPanel
*/
} // class OptionsDialog