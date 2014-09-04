 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class DeepCopyFrameOperation extends CopyOperation {

  private Frame _newFrame;
  private boolean _continueCopy = true;

  public DeepCopyFrameOperation (Frame frame) {
	super ();
	initialize (frame);
  }

  public DeepCopyFrameOperation (Frame frame, TraversalDirective td) {
	super (td);
	initialize (frame);
  }

  private void initialize (Frame frame) {
    _name = "deep-copy-frame";
    _shortName = "copy";
    _args.setArg(0, frame);
    _prettyName = _name;
  }

  public static DeepCopyFrameOperation createOperation (Frame frame, TraversalDirective td,
  													    boolean fromMerge) {
    if (frame instanceof Cls)
      return new DeepCopyClsOperation (frame, td);
    else if (frame instanceof Slot)
      return new DeepCopySlotOperation (frame, td);
    return null;
  }

   protected void copyDirectSuperclasses (Cls oldCls, Cls newCls){
     Collection superclasses = copyReferencedClses (Util.getDirectSuperclasses(oldCls), _targetKb, _newFrame, true);
     addDirectSuperclasses (newCls, superclasses);
   }

   public Collection copyReferencedClses (Collection c, KnowledgeBase targetKb,
                                           Frame newFrame) {
     return copyReferencedClses (c, targetKb, newFrame, false);
   }

   protected Cls copyDirectType (Frame oldFrame, KnowledgeBase sourceKb, boolean copyLevelOnly) {
     Cls type = ((Instance)oldFrame).getDirectType();

     if (oldFrame instanceof Cls && type.equals(oldFrame)) {
      _typeSameAsFrame = true;
      return Util.getStandardMetaclass(_targetKb);
     }

     Frame whatBecameOfType = Mappings.getWhatBecameOfIt(type);
     if (whatBecameOfType == null) {
	Operation newOp = new KeepClsOperation (type);
        newOp.setTraversalDirective(TraversalDirective.createCopyLevelOnlyDirective(type));
        newOp.performOperation();
        type = (Cls) Mappings.getWhatBecameOfIt(type);
     } else {
      	type =  (Cls)whatBecameOfType;
     }
     return type;
   }

   public Collection copyReferencedClses (Collection c, KnowledgeBase targetKb,
                                           Frame newFrame,
                                           boolean copyingSuperclass) {
     if (c == null) return null;

     Collection newC = new ArrayList();
     Iterator i = c.iterator();
     Frame next;
     Frame mapping;
     while (i.hasNext())  {
       next = (Frame)i.next();
       mapping = Mappings.getWhatBecameOfIt (next);
       if (mapping == null) {
       	   int level =  _traversalDirective.getLevelForSlot(next.getKnowledgeBase().getSlot(Model.Slot.DIRECT_SUPERCLASSES));
           if (copyingSuperclass && level != TraversalDirective.NO_LEVEL_SET)
             (new DeepCopyClsOperation (next, _traversalDirective.nextDirective(next))).performOperation();
           else
             (new KeepFrameOperation (next, _traversalDirective.nextDirective(next), true)).performOperation();
           newC.add (Mappings.getWhatBecameOfIt (next));
        }
       else
           newC.add (mapping);
      }
      return newC;
   }

}
