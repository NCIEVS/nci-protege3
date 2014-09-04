 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.util.DummyFrame;
import edu.stanford.smi.protegex.prompt.util.Util;

public class RemoveSuffixesAction extends AbstractAction {
    public RemoveSuffixesAction () {
      super ("RemoveSuffixes");
    }

    public void actionPerformed (ActionEvent e) {
	  removeSuffixes ();
	  removeDummyFrames ();
	  
    }
    
    public void removeDummyFrames () {
    	DummyFrame.removeDummyFrames();
    }
    
    public void removeSuffixes () {
      PromptTab.startOperation();
      KnowledgeBase targetKb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
      Collection allFrames = new ArrayList (Util.getFrames(targetKb));
      if (allFrames == null) return;

      Iterator i = allFrames.iterator();
      Frame next;
      while (i.hasNext()) {
        next = (Frame) i.next();
		if (!next.isIncluded()) {
        	String realName = Mappings.getRealName (next);
            if (!realName.equals(next.getName())) {
				realName = ensureUniqueName (realName, targetKb);
				// ToDo should I do something with the new frame to replace the old?
				next.rename(realName);
            }
        }
      }
      PromptTab.completeOperation();

    }
    
    private String ensureUniqueName (String realName, KnowledgeBase kb) {
    	while (true) {
    		Frame existingFrame = kb.getFrame(realName);
			if (existingFrame == null) return realName;
			realName += "_";
    	}
     }

    class WarningPanel extends JPanel  {
      WarningPanel () {
        super();
        setLayout (new GridLayout (0, 1));
        add (new JLabel ("Performing this operation may introduce new conflicts to the ontology."));
        JLabel question = new JLabel ("Do you still want to do it?");
        add (question);
      }
    }
}

