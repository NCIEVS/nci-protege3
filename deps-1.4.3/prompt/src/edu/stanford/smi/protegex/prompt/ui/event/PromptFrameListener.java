 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.event;

import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.Statistics;

public class PromptFrameListener implements FrameListener {
	public void browserTextChanged(FrameEvent event) {}

	public void deleted(FrameEvent event) {}

	public void nameChanged(FrameEvent event) {}

	public void visibilityChanged(FrameEvent event) {}

	public void ownFacetAdded(FrameEvent event) {}

	public void ownFacetRemoved(FrameEvent event) {}

	public void ownFacetValueChanged(FrameEvent event) {}

	public void ownSlotAdded(FrameEvent event) {}

	public void ownSlotRemoved(FrameEvent event) {}

	public void ownSlotValueChanged(FrameEvent event) {
    	if (PromptTab.processingOperation()) return;
    	Frame f = event.getFrame();
        if (f instanceof Slot) {
         	Slot s = (Slot)f;
    		Statistics.addToLogStream
            	("Own slot " + event.getSlot() + " for slot " + s +
                              " changed to " +
                              CollectionUtilities.toString
                              (s.getOwnSlotValues(event.getSlot())));
		    Statistics.increaseNumberOfKBOperations();
        }

    }

}
