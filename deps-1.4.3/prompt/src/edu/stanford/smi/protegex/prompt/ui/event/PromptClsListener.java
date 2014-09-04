 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.event;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.Statistics;
import edu.stanford.smi.protegex.prompt.operation.AddParentOperation;
import edu.stanford.smi.protegex.prompt.operation.RemoveParentOperation;
import edu.stanford.smi.protegex.prompt.operation.RemoveTemplateSlotOperation;

public class PromptClsListener implements ClsListener {
  public void directSubclassAdded(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
    (new AddParentOperation (event.getSubclass(), event.getCls(), true)).performOperation();
    Statistics.increaseNumberOfKBOperations();
  }

  public void directSubclassRemoved(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
//   (new RemoveParentOperation (event.getSubclass(), event.getCls())).performOperation();
  }

  public void directSuperclassAdded(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
    //(new AddParentOperation (event.getCls(), event.getSuperclass())).performOperation();
  }

  public void directSuperclassRemoved(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
    (new RemoveParentOperation (event.getCls(), event.getSuperclass(), true)).performOperation();
    Statistics.increaseNumberOfKBOperations();
  }

  public void directInstanceCreated(ClsEvent event) {}
  public void directInstanceRemoved(ClsEvent event) {}
  public void directInstanceAdded(ClsEvent event) {}
  public void directInstanceDeleted(ClsEvent event) {}
  public void directSubclassesReordered(ClsEvent event) {}
  public void directSubclassMoved(ClsEvent event) {}
  public void templateFacetAdded(ClsEvent event){}
  public void templateFacetRemoved(ClsEvent event) {}
  public void templateFacetValueChanged(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
    Statistics.addToLogStream("Template facet " + event.getFacet() + " for slot " + event.getSlot()+
                              " at class " + event.getCls() +
                              " changed to " +
                              CollectionUtilities.toString
                              (event.getCls().getTemplateFacetValues (event.getSlot(), event.getFacet())));
    Statistics.increaseNumberOfKBOperations();
  }
  public void templateSlotAdded(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
    Statistics.addToLogStream("Template slot " + event.getSlot() + " added to " + event.getCls());
    Statistics.increaseNumberOfKBOperations();
  }
  public void templateSlotRemoved(ClsEvent event) {
    if (PromptTab.processingOperation()) return;
    (new RemoveTemplateSlotOperation (event.getCls(), event.getSlot(), true)).performOperation();
    Statistics.addToLogStream("Template slot " + event.getSlot() + " removed from " + event.getCls());

    Statistics.increaseNumberOfKBOperations();
  }
  public void templateSlotValueChanged(ClsEvent event) {}
}
