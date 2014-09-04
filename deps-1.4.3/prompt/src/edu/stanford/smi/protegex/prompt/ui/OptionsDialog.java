 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.FrameSlotCombination;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.ComponentFactory;

public class OptionsDialog {

  public String showOptionsDialog (String title, TabComponent parent, String prompt,
  									Object[] options, boolean allowOthers) {
    OptionsPanel optionsPanel = new OptionsPanel (prompt, options, allowOthers);
    createDialog (title, parent, optionsPanel);
    return optionsPanel.getSelectedName();

  }

  public int showSlotOptionsDialog (String title, TabComponent parent, String prompt,
  									Object[] options) {
    OptionsPanel optionsPanel = new OptionsPanel (prompt, options);
    createDialog (title, parent, optionsPanel);
    return optionsPanel.getSelectedIndex();
  }

  private void createDialog (String title, TabComponent parent, OptionsPanel optionsPanel) {
     JOptionPane.showMessageDialog(parent, optionsPanel, title, JOptionPane.PLAIN_MESSAGE);
  }

  public class OptionsPanel extends JPanel {
    private ButtonGroup _group = new ButtonGroup();
    private PanelWithRadioButton [] _buttons;
    private PanelWithRadioButton _otherButton;
    private boolean _allowOthers = false;

    OptionsPanel (String prompt, Object[] options, boolean allowOthers) {
      _allowOthers = allowOthers;
      if (allowOthers)
        _buttons = new PanelWithRadioButton [options.length + 1];
      else
        _buttons = new PanelWithRadioButton [options.length];

      initialize (prompt, options);
    }

   OptionsPanel (String prompt, Object[] options) {
      _buttons =  new PanelWithRadioButton [options.length];

      initialize (prompt, options);
   }

   private void initialize (String prompt, Object [] options) {
      setLayout (new BorderLayout ());

      JLabel promptLabel = ComponentFactory.createLabel();
      promptLabel.setText(prompt);
      add (promptLabel, BorderLayout.NORTH);

      JPanel choices = new JPanel (new GridLayout (0, 1, 0, 10));
      PanelWithRadioButton nextButton;

      if (options.length == 0) return;

      boolean slotsOrStrings = (options [0] instanceof FrameSlotCombination ||
      							options [0] instanceof Slot);
      boolean topLevelSlot = options [0] instanceof Slot;

      for (int i = 0; i < options.length; i++) {
        if (slotsOrStrings)
        	if (topLevelSlot)
          		nextButton = new PanelWithRadioButton ((Slot)options[i]);
            else
          		nextButton = new PanelWithRadioButton ((FrameSlotCombination)options[i]);
        else
          nextButton = new PanelWithRadioButton (options[i].toString());

        choices.add (nextButton);
        if (i == 0) nextButton.setSelected (true);

        _group.add (nextButton.getButton());
        _buttons[i] = nextButton;
      }

      if (!slotsOrStrings && _allowOthers) {
        _otherButton = new PanelWithRadioButton();
        choices.add (_otherButton);
        _buttons[options.length] = _otherButton;
        _group.add (_otherButton.getButton());
     }

     add (choices, BorderLayout.CENTER);

    }

    public String getSelectedName () {
      String result = null;
      int regularButtons;
      if (_allowOthers)
        regularButtons = _buttons.length - 1;
      else
        regularButtons = _buttons.length;

      for (int i = 0; i < regularButtons; i++) {
        if (_buttons[i].isSelected())
            result = _buttons[i].getText();
      }
      if (_allowOthers && _otherButton.getButton().isSelected())
        result = _otherButton.getText();
      return result;
    }

    public int getSelectedIndex () {
      for (int i = 0; i < _buttons.length; i++) {
        if (_buttons[i].isSelected())
            return i;
      }
       return -1;
    }

  }



} // class OptionsDialog

