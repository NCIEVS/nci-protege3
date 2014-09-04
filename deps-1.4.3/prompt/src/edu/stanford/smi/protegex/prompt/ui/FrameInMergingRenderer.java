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

public class FrameInMergingRenderer extends FrameRenderer {
    protected Frame _frame = null;

    public Color getTextColor() {
    	if (!PromptTab.mergingHasBeenSetUp() || _frame == null)
        	return super.getTextColor();

        Color textColor = KnowledgeBaseInMerging.getFrameColor (_frame);
        if (textColor != null)
        	return textColor;
        else
        	return super.getTextColor(); 
    }


	public void load(Object value) {
		if(PromptTab.mergingHasBeenSetUp() && value instanceof Frame && ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(((Frame)value).getKnowledgeBase()) != null)
		{
			Frame frame = (Frame) value;
		       if (PromptTab.mergingHasBeenSetUp() )
		             _frame = (Frame) value;
		           else
		             _frame = null;
		           super.load (value);
		   		if (_frame != null &&
		   			PromptTab.extracting() && 
		   			PromptTab.getTraversalDirectivesKb().getKnowledgeBase() == _frame.getKnowledgeBase()) {
		   			if (PromptTab.getTraversalDirectivesKb().isSlotDirective(_frame)) 
		   				setMainText (PromptTab.getTraversalDirectivesKb().getSlotDirectiveBrowserText ((Instance)_frame));	
		   			}
		           addTrailingIcon ();
		} else {
			super.load(value);
		}
			
		
 
	}


    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width += 15;
      return d;
    }



    private void addTrailingIcon () {
    	if (_frame != null) {
	        _displayAbstractIcon = false;
    	    _displayMultipleParentsIcon = false;
//	        _displayAbstractIcon = _frame.getProject().getDisplayAbstractClassIcon();
//    	    _displayMultipleParentsIcon = _frame.getProject().getDisplayMultiParentClassIcon();
        }
        if (PromptTab.mergingHasBeenSetUp() &&
        	_frame != null &&
        	!(PromptTab.extracting() && PromptTab.getTraversalDirectivesKb().getKnowledgeBase() == _frame.getKnowledgeBase()) &&
		!Util.isSystem(_frame) &&
		!_frame.isIncluded()) {
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

    public String toString () {
     	return "FrameInMergingRenderer";
    }

}

