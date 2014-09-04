 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.event;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.*;

public class PromptKnowledgeBaseListener implements KnowledgeBaseListener {
  public void clsCreated(KnowledgeBaseEvent event) {
    if (PromptTab.processingOperation()) return;
    Cls newCls = event.getCls();
    newCls.addClsListener(PromptTab.getClsListener());
    Statistics.increaseNumberOfKBOperations();
  }

  public void clsDeleted(KnowledgeBaseEvent event) {
    if (PromptTab.processingOperation()) return;
    Cls newCls = event.getCls();
    String oldName = event.getOldName();
//Log.trace ("newCls = " + newCls + ", oldName = " + oldName, this, "clsDeleted");
    if (oldName != null)
		(new DeleteFrameOperation (newCls, oldName, true)).performOperation();
    Statistics.increaseNumberOfKBOperations();
  }

  public void frameNameChanged(KnowledgeBaseEvent event)  {
    if (PromptTab.processingOperation()) return;
    Frame newFrame = event.getFrame();
    if (newFrame instanceof Cls)
      (new RenameClsOperation (newFrame, newFrame.getName(), event.getOldName(), true)).performOperation();
    if (newFrame instanceof Slot)
      (new RenameSlotOperation (newFrame, newFrame.getName(), event.getOldName(), true)).performOperation();
    Statistics.increaseNumberOfKBOperations();
  }

  public void defaultClsMetaClsChanged(KnowledgeBaseEvent event) {}
  public void defaultFacetMetaClsChanged(KnowledgeBaseEvent event) {}
  public void defaultSlotMetaClsChanged(KnowledgeBaseEvent event) {}
  public void facetCreated(KnowledgeBaseEvent event){}
  public void facetDeleted(KnowledgeBaseEvent event) {}
  public void instanceCreated(KnowledgeBaseEvent event) {}

  public void instanceDeleted(KnowledgeBaseEvent event) {
    if (PromptTab.processingOperation()) return;
    Frame newInstance = event.getFrame();
    String oldName = event.getOldName();
	(new DeleteFrameOperation (newInstance, oldName, true)).performOperation();
    Statistics.increaseNumberOfKBOperations();
  }

  public void slotCreated(KnowledgeBaseEvent event) {
    if (PromptTab.processingOperation()) return;
    Statistics.addToLogStream("Slot created: " + event.getSlot());
    Statistics.increaseNumberOfKBOperations();
  }
  public void slotDeleted(KnowledgeBaseEvent event)  {
    if (PromptTab.processingOperation()) return;
    Frame newSlot = event.getFrame();
    String oldName = event.getOldName();
	(new DeleteFrameOperation (newSlot, oldName, true)).performOperation();
    Statistics.increaseNumberOfKBOperations();
  }
}
