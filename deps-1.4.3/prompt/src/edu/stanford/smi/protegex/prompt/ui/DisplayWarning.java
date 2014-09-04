 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class DisplayWarning {
  private static final String _chooseOwnSlotTitle = "CONFLICT: different own slot values";
  private static final String _chooseNameTitle = "Choose frame name";
  private static final int NUMBER_OF_LINES = 3;

  static public boolean showYesOrNoDialog (Collection c, String warning) {
    JPanel warningPanel = new JPanel (new BorderLayout());
    warningPanel.add(new JLabel (warning), BorderLayout.NORTH);
    warningPanel.add(ComponentFactory.createScrollPane(createList (c)), BorderLayout.CENTER);
    return (ModalDialog.OPTION_YES ==
              ModalDialog.showDialog (PromptTab.getMainWindow(), warningPanel, "Frames will be moved", ModalDialog.MODE_YES_NO));
  }

  static private JList createList (Collection c) {
    Vector vector = new Vector (c);
    JList result = new JList (vector);
    result.setEnabled(false);
    result.setCellRenderer(FrameRenderer.createInstance());
    return result;
  }

  static public boolean showYesOrNoDialog (Warning warning) {
    JPanel warningPanel = createWarningPanel (warning);
  	return (ModalDialog.OPTION_YES ==
    			ModalDialog.showDialog (PromptTab.getMainWindow(), warningPanel,
                						"", ModalDialog.MODE_YES_NO));
  }

  static public void showMessageDialog (Warning warning) {
  	JPanel warningPanel = createWarningPanel (warning);
    ModalDialog.showMessageDialog(warningPanel, "");
  }

  static private JPanel createWarningPanel (Warning warning) {
  	JPanel warningPanel = ComponentFactory.createPanel();
    warningPanel.setLayout(new BorderLayout ());
//    warningPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    JList list = ComponentFactory.createList(null);
    list.setCellRenderer(new ExplanationRenderer (NUMBER_OF_LINES));
    list.setEnabled(false);

  	Collection listValues = new ArrayList ();
    for (int i = 0; i < NUMBER_OF_LINES; i++) {
             	listValues.add (warning);
    }
    ComponentUtilities.setListValues (list, listValues);

    warningPanel.add(list);
	return warningPanel;
  }
  
  static public boolean showYesOrNoDialog (String [] lines) {
     JPanel mP = DisplayUtilities.createPanelWithMultipleLines (lines);
     return (ModalDialog.OPTION_YES ==
           ModalDialog.showDialog(PromptTab.getMainWindow(), mP, "", ModalDialog.MODE_YES_NO));

  }

  static public String selectValue  (String title, String prompt, Object [] options,
  									boolean allowOthers) {
    OptionsDialog optionsDialog = new OptionsDialog ();
    String result = optionsDialog.showOptionsDialog (title, PromptTab.getTabComponent(),
    												prompt, options, allowOthers);
    return result;
  }

  static public int selectSlotFacets  (String title, String prompt,
  										Object [] options) {
    OptionsDialog optionsDialog = new OptionsDialog ();
    int result = optionsDialog.showSlotOptionsDialog (title, PromptTab.getTabComponent(),
    													prompt, options);
    return result;
  }

}


