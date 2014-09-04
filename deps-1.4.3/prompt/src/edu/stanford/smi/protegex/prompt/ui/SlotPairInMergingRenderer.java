 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
    *                 Kyle Bruck kbruck@stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class SlotPairInMergingRenderer extends SlotPairRenderer {
	Frame _frame = null;

	public Color getTextColor() {
    	if (!PromptTab.mergingHasBeenSetUp())
        	return super.getTextColor();

		Color textColor = KnowledgeBaseInMerging.getFrameColor (_frame);
        if (textColor != null)
        	return textColor;
        else
        	return super.getTextColor();
    }


    public void load(Object value) {
		if(value instanceof Frame && ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(((Frame)value).getKnowledgeBase()) != null)
		{
			Frame frame = (Frame) value;
		   	if (PromptTab.mergingHasBeenSetUp() &&
		        	value instanceof FrameSlotCombination)
		           	_frame = ((FrameSlotCombination) value).getSlot();
		        else
		        	_frame = null;

				super.load (value);
		        addTrailingIcon ();			
		} else {
			super.load(value);
		}
    	
    	
 	}

    private void addTrailingIcon () {
        if (PromptTab.mergingHasBeenSetUp() &&
        	_frame != null &&
        	!Util.isSystem(_frame) &&
     		!(PromptTab.extracting() && PromptTab.getTraversalDirectivesKb().getKnowledgeBase() == _frame.getKnowledgeBase())) {
            if (KnowledgeBaseInMerging.isInTarget (_frame)) {
            	if (PromptTab.moving() && !_frame.isIncluded())
	        		appendIcon (ComponentUtilities.loadImageIcon
            					(FrameInMergingRenderer.class, "images/Local.gif"));
            }
            else if (Mappings.getWhatBecameOfIt(_frame) != null)
	        	appendIcon (ComponentUtilities.loadImageIcon
            		(FrameInMergingRenderer.class, "images/Mapped.gif"));
            }
    }

}

