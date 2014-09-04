 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.model.FrameSlotCombination;
import edu.stanford.smi.protege.model.Slot;

public class PanelWithRadioButton extends JPanel {
      JRadioButton _button;
      JTextComponent _text;

      PanelWithRadioButton (FrameSlotCombination o) {
        initialize (new SlotBindingsChoice (o));
      }

      PanelWithRadioButton (Slot s) {
        initialize (new SlotBindingsChoice (s));
      }

      PanelWithRadioButton (String buttonText) {
//        _textField = DisplayUtilities.createDisabledTextField (buttonText, 20);
        _text = DisplayUtilities.createDisabledTextComponent (buttonText);
        initialize (new JScrollPane (_text));
      }

      PanelWithRadioButton () {
        _text = new JTextField (15);

        initialize ("Other:", _text);

        _text.setEditable (true);
        _text.addCaretListener (new CaretListener () {
           public void caretUpdate (CaretEvent e) {
             if (_text.getText().length () != 0)
               _button.setSelected (true);
           }
        });
      }

      private  void initialize (JComponent content) {
        initialize ("", content);
      }

      private void initialize (String buttonText, JComponent content) {
        setLayout (new BorderLayout());
        _button = new JRadioButton (buttonText);

        add (_button, BorderLayout.WEST);
        if (content != null)
          add (content, BorderLayout.CENTER);
      }

      public String getText () { return _text.getText(); }

      public JRadioButton getButton () { return _button;}

      public boolean isSelected () { return _button.isSelected();}

      public void setSelected (boolean value) { _button.setSelected(value);}
   }
