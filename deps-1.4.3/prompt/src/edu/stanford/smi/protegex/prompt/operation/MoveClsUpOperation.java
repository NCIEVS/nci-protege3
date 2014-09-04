 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MoveClsUpOperation extends MoveUpOperation {
  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [MOVE_OPERATION_ARITY];

  public MoveClsUpOperation (Frame frame) {
    super (frame);
    setDefaultParameters ();
    initialize ();
  }

  public MoveClsUpOperation (Frame frame, Explanation exp) {
    super (frame, exp);
    setDefaultParameters ();
    initialize ();
  }

//  public MoveClsUpOperation (Frame frame, boolean copySubclasses, boolean copyInstances,  boolean copyEverythingRequired) {
  public MoveClsUpOperation (Frame frame, TraversalDirective td,  boolean copyEverythingRequired) {
    super (frame);
    _traversalDirective = td;
//   	_copySubclasses = copySubclasses;
//        _copyInstances = copyInstances;
    _copyEverythingRequired = copyEverythingRequired;
    initialize ();
  }

  private void initialize () {
    _name = "shallow-move-class-up";
    _prettyName = "move class up";
    _shortName = "move up";
  }

  protected void moveRequiredFrames (Frame oldFrame) {
    super.moveRequiredFrames(oldFrame);
    moveSuperclasses ((Cls)oldFrame);
  }

  private void moveSuperclasses (Cls oldFrame) {
    Collection superclasses = Util.getDirectSuperclasses (oldFrame);
    Iterator i = superclasses.iterator();
    while (i.hasNext()) {
      Cls next = (Cls)i.next();
      (new MoveClsUpOperation (next, new TraversalDirective (next, false, false), true)).performOperation();
    }

  }

//   protected Frame createFrame (Frame oldFrame) {
//      Cls result = (Cls)super.createFrame(oldFrame);
//      return result;
//   }

  protected void moveDependentFrames (Frame oldFrame, Frame newFrame) {
      super.moveDependentFrames(oldFrame, newFrame);
      moveAndSetTemplateSlotsAndFacets ((Cls)oldFrame, (Cls)newFrame);
  }
  
  protected void moveOptionalFrames (Frame oldFrame, Frame newFrame) {
  	if (_traversalDirective.copySubclasses())
  		moveTree ((Cls)oldFrame);
  	if (_traversalDirective.copyInstances())
  		moveInstances ((Cls)oldFrame);
  }



  protected static String checkOtherReferences (Frame oldFrame) {
    String superclassesResult = checkSuperclasses ((Cls)oldFrame);
    if (superclassesResult != null) {
      return superclassesResult;
    }


    String templateSlotsAndFacetsResult =  checkTemplateSlotsAndFacets ((Cls) oldFrame);
    if (templateSlotsAndFacetsResult != null) {
      return templateSlotsAndFacetsResult;
    }
    return null;
  }

  protected static String checkSuperclasses (Cls oldFrame) {
    Collection superclasses = Util.getDirectSuperclasses(oldFrame);
    Iterator i = superclasses.iterator();
    while (i.hasNext()) {
      Cls next = (Cls)i.next();
      String warning = "Class " + next + ", which is a superclass of class " + oldFrame + ", is in a different included project.";
      String checkResult = checkRequiredFrame (oldFrame, next, warning);
      if (checkResult != null) return checkResult;
    }
    return null;
  }

  protected static String checkTemplateSlotsAndFacets (Cls oldFrame) {
     Collection slots = oldFrame.getDirectTemplateSlots();
     if (slots == null) return null;
	Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore (oldFrame.getKnowledgeBase());

     Iterator i = slots.iterator();
     while (i.hasNext()) {
       Slot next = (Slot) i.next();
       if (ownSlotsToIgnore.contains (next)) continue;

       String warning = "Slot " + next + " attached to class " + oldFrame + " is in a different included project.";
       String checkResult = checkRequiredFrame (oldFrame, next, warning);
       if (checkResult != null) return checkResult;

       String facetsResult = checkFacets (oldFrame, next);
       if (facetsResult != null) return facetsResult;
     }
     return null;
  }

  protected static String checkFacets (Cls oldFrame, Slot oldSlot) {
      Collection facets = oldFrame.getTemplateFacets(oldSlot);
      if (facets == null || facets.isEmpty()) return null;
      Iterator i = facets.iterator();
      while (i.hasNext()) {
        Facet nextFacet = (Facet) i.next();
        Collection facetValues = oldFrame.getDirectTemplateFacetValues(oldSlot, nextFacet);
        if (facetValues == null || facetValues.isEmpty()) continue;
        Iterator j = facetValues.iterator();
        while (j.hasNext()) {
          Object value = j.next();
          if (value instanceof Frame) {
            Frame nextFrameValue = (Frame)value;
            String warning = "Facet value " + nextFrameValue + " for facet " + nextFacet + " for template slot " + oldSlot +
                    " at class " + oldFrame  + " is in a different included project.";
            String facetValueResult = checkRequiredFrame (oldFrame, nextFrameValue, warning);
            if (facetValueResult != null) return facetValueResult;
          }
        }
      }
    return null;
  }

  protected void  moveInstances (Cls cls) {
    Collection instances = cls.getDirectInstances();
    if (instances == null || instances.size() == 0) return;
    Iterator i = instances.iterator();
    while (i.hasNext()) {
        Instance next = (Instance)i.next();
        MoveUpOperation.createOperation (next, new TraversalDirective (next, false, false), _copyEverythingRequired).performOperation ();
    }
  }

  protected void moveTree (Cls cls) {
  	Collection subclasses = cls.getDirectSubclasses();
    if (subclasses == null || subclasses.size() == 0) return;
    Iterator i = subclasses.iterator();
    while (i.hasNext()) {
//		MoveUpOperation.createOperation ((Frame)i.next(), _copySubclasses, _copyInstances, _copyEverythingRequired).performOperation ();
		Frame next = (Frame)i.next();
		MoveUpOperation.createOperation (next, _traversalDirective.sameDirective (next), _copyEverythingRequired).performOperation ();
    }
  }
  
   private void moveAndSetTemplateSlotsAndFacets (Cls oldFrame, Cls newFrame) {
     Collection oldSlots = oldFrame.getDirectTemplateSlots();

     if (oldSlots == null) return;

     Iterator i = oldSlots.iterator();
     Slot next, nextCopy;

     while (i.hasNext()) {
       next = (Slot) i.next();
       (new MoveSlotUpOperation (next, _copyEverythingRequired)).performOperation();
       Slot newSlot = _includedKb.getSlot(next.getName());
       newFrame.addDirectTemplateSlot(newSlot);
       moveAndSetFacets (oldFrame, newFrame, next, newSlot);
     }
   }

   private void moveAndSetFacets (Cls oldFrame, Cls newFrame, Slot oldSlot, Slot newSlot) {
      Collection facets = oldFrame.getTemplateFacets(oldSlot);
      if (facets == null || facets.isEmpty()) return;
      Iterator i = facets.iterator();
      while (i.hasNext()) {
        Facet nextFacet = (Facet) i.next();
        Collection facetValues = oldFrame.getDirectTemplateFacetValues(oldSlot, nextFacet);
        if (facetValues == null || facetValues.isEmpty()) continue;
        Iterator j = facetValues.iterator();
        while (j.hasNext()) {
          Object value = j.next();
          if (value instanceof Frame) {
            MoveUpOperation.createOperation((Frame)value, new TraversalDirective ((Frame)value, false, false), _copyEverythingRequired).performOperation();
            newFrame.addTemplateFacetValue(newSlot,
											_includedKb.getFacet (nextFacet.getName()),
											_includedKb.getFrame(((Frame)value).getName()));
          } else
            newFrame.addTemplateFacetValue(newSlot, _includedKb.getFacet (nextFacet.getName()), value);
        }
      }
   }

}
