 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class ProjectAction extends AbstractAction {

   public ProjectAction (String label) {
     super (label);
   }

    public void closeProject (Project project){
      if (project != null)
        if (confirmSaving(project))  {
          PromptTab.clearAll();
        }
    }

    private boolean confirmSaving (Project project) {
      int confirmation = ModalDialog.showDialog(PromptTab.getMainWindow(),
         new JLabel ("Save file before closing?"), "Save?",
         ModalDialog.MODE_YES_NO_CANCEL);
      if (confirmation == ModalDialog.OPTION_CANCEL) return false;

      if (confirmation == ModalDialog.OPTION_NO) return true;

      saveProject (project);
      return true;
  }




    public void saveProject (Project project){
      Collection errors = new ArrayList();
      project.save(errors);
      Util.displayErrors (errors);
    }


  public void actionPerformed (ActionEvent e) {};
}

