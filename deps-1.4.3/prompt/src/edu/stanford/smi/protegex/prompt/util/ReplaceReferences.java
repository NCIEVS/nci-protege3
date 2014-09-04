 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.util;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.conflict.*;

public class ReplaceReferences {
  static public String [] ownSlotsToIgnore =
       {Model.Slot.DIRECT_SUBCLASSES,
       	Model.Slot.DIRECT_SUPERCLASSES,
        Model.Slot.DIRECT_INSTANCES,
        Model.Slot.DIRECT_DOMAIN,
        Model.Slot.DIRECT_TEMPLATE_SLOTS,
        Model.Slot.NAME,
        Model.Slot.CONSTRAINTS,
        Model.Slot.DIRECT_TYPES};

  public static void replaceAllReferences (Frame from, Frame to, KnowledgeBase kb) {
	    if (from.getName().equals (to.getName())) return;

	Collection references = new ArrayList (from.getReferences());
    replaceReferences (references, from, to);

    replaceReferencesInOperations (from, to);
    replaceReferencesInMaps (from, to);
  }

/*
  public static void replaceFrameNameReferences (Frame f, String oldName) {
	KnowledgeBaseInMerging kbInMerging = PromptTab.getKnowledgeBaseInMerging(f.getKnowledgeBase());
    kbInMerging.updateSourceMap (f, oldName);

    Mappings.updateFrameActionsMap (f, oldName);
  }
*/
  protected static void replaceReferences (Collection refs, Frame from, Frame to){
	if (refs == null) return;
    Iterator i = refs.iterator();
    Reference next;
    Frame nextFrame;
    Slot nextSlot;
    Facet nextFacet;
    boolean isTemplate;
    Collection values;
    KnowledgeBase kb = from.getKnowledgeBase();

    while (i.hasNext()) {
    	next = (Reference) i.next();
        nextFrame = next.getFrame();
        nextSlot = next.getSlot();
        nextFacet = next.getFacet();
        isTemplate = next.isTemplate();
        if (nextFacet == null) {
         	if (isTemplate == true && nextFrame instanceof Cls ) {   // currently doesn't happen
            	values = new ArrayList (((Cls)nextFrame).getTemplateSlotValues (nextSlot));
                replaceValueInCollection (values, from, to);
                Cls nextCls = (Cls)nextFrame;
                nextCls.setTemplateFacetValues(nextSlot, kb.getFacet(Model.Facet.VALUES), values);
//                nextCls.setTemplateSlotValues (nextSlot, values);
            } else if (isTemplate == false) {
                if (member (nextSlot.getName(), ownSlotsToIgnore)  ) continue;
            	values = new ArrayList (nextFrame.getOwnSlotValues(nextSlot));
                replaceValueInCollection (values, from, to);
                nextFrame.setOwnSlotValues(nextSlot, values);
            }
        } else {// nextFacet != null, assume nextFacet is template facet
         	values = new ArrayList (((Cls)nextFrame).getDirectTemplateFacetValues (nextSlot, nextFacet));
            replaceValueInCollection (values, from, to);
            ((Cls)nextFrame).setTemplateFacetValues(nextSlot, nextFacet, values);
        }
    }
  }

  private static boolean member (String elt, String [] set) {
   	for (int i = 0; i < set.length; i++) {
     	if (elt == set[i]) return true;
    }
    return false;
  }

  protected static void replaceValueInCollection (Collection c, Frame from, Frame to) {
	c.add (to);
    c.remove (from);
  }

  public static void removeClsToClsReferences (Cls from, Slot forSlot) {
    Collection operationsWithFrame = Mappings.getCurrentOperations (from);
    if (operationsWithFrame == null) return;

    Iterator i =  operationsWithFrame.iterator ();

    while (i.hasNext()) {
      Action next = (Action)i.next();
      if (next instanceof Conflict)
      	((Conflict)next).removeClsToClsReference (from, forSlot);
    }

  }

  static private void replaceReferencesInOperations (Frame from, Frame to) {
    Collection operationsWithFrame = Mappings.getCurrentOperations (from);
    if (operationsWithFrame == null) return;

    operationsWithFrame = new ArrayList(operationsWithFrame);
    Iterator i =  operationsWithFrame.iterator ();

    while (i.hasNext()) {
      Action next = (Action)i.next();
      next.replaceFrameReference (from, to);
    }
  }

  static private void replaceReferencesInMaps (Frame from, Frame to) {
   	replaceReferencesInFrameActionsMap (from, to);
    replaceReferencesInSourceMaps (from, to);
  }

  static private void replaceReferencesInFrameActionsMap (Frame from, Frame to) {
    Collection operationsWithFrame = Mappings.getCurrentOperations (from);
    if (operationsWithFrame != null && !operationsWithFrame.isEmpty()) {
      Iterator i = operationsWithFrame.iterator();
      while (i.hasNext()) {
		Action next = (Action) i.next();
        Mappings.removeFromFrameActionsMap(from, next);
		Mappings.addToFrameActionsMap(to, next);
      }
    }
  }

  static private void replaceReferencesInSourceMaps (Frame from, Frame to) {
	 Collection gotSources = Mappings.getSources(from);
     if (gotSources == null || gotSources.size() == 0) return;

     Collection sources = new ArrayList (gotSources);
     Iterator i = sources.iterator();
     while (i.hasNext()) {
      	Frame nextSource = (Frame) i.next();
        Mappings.updateWhatBecameOfItMap(nextSource, to);
//        Mappings.setSource (nextSource, to);
     }
  }
}
