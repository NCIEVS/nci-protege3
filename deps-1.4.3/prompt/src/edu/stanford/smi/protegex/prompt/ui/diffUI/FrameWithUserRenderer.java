/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui.diffUI;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.*;
import edu.stanford.smi.protegex.prompt.promptDiff.users.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class FrameWithUserRenderer extends DiffFrameRenderer implements
		ListCellRenderer {

    public FrameWithUserRenderer() {
        super(PromptTab.getPromptDiff().getResultsTable());
    }

    public void load(Object value) {
        super.load(value);
        if (!(value instanceof Cls)) return;

        String authors = Util.createAuthorString (AuthorManagement.getUserListForConcept(((Cls)value).getName()));
        this.appendText("(" + authors +  ")");
    }

}
