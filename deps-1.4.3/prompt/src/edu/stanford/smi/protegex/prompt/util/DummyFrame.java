/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.util;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class DummyFrame {
	static private final String DUMMY_FRAMES_META_CLS_NAME = "_DUMMY-FRAMES-METACLASS";
	static private final String DUMMY_FRAMES_META_SLOT_NAME = "_DUMMY-FRAMES-METASLOT";
	static private final String DUMMY_FRAMES_META_FACET_NAME = "_DUMMY-FRAMES-METAFACET";
	static private final String DUMMY_FRAMES_CLS_NAME = "_TEMPORARY-ITEMS";
	static private final String REFERENCES_SLOT_NAME = "_REFERENCES";
	static private final String REFERENCES_FACET_NAME = "_REFERENCES_FACET";
	static private Cls DUMMY_FRAME_CLS;
	static private Cls DUMMY_FRAME_META_CLS;
	static private Cls DUMMY_FRAME_META_SLOT;
	static private Cls DUMMY_FRAME_META_FACET;
	static private Facet REFERENCES_FACET;
	static private Slot REFERENCES_SLOT;
	static private final String TEMP_SUFFIX = "_temp";
	
	static public void createDummyFrameCls (KnowledgeBase kb) {
		DUMMY_FRAME_META_CLS = kb.getCls(DUMMY_FRAMES_META_CLS_NAME);
		DUMMY_FRAME_CLS = kb.getCls(DUMMY_FRAMES_CLS_NAME);
		DUMMY_FRAME_META_SLOT = kb.getCls(DUMMY_FRAMES_META_SLOT_NAME);
		DUMMY_FRAME_META_FACET = kb.getCls(DUMMY_FRAMES_META_FACET_NAME);
		REFERENCES_SLOT = kb.getSlot(REFERENCES_SLOT_NAME);
		if (DUMMY_FRAME_META_CLS != null || DUMMY_FRAME_CLS != null || DUMMY_FRAME_META_SLOT != null ||
				DUMMY_FRAME_META_FACET != null)
			return;
		
		
		Collection metaClassParent = CollectionUtilities.createCollection (Util.getStandardMetaclass(kb));
		Cls dummyFrameMetaCls = kb.createCls (DUMMY_FRAMES_META_CLS_NAME, metaClassParent);
		Cls dummyFrameCls = kb.createCls (DUMMY_FRAMES_CLS_NAME,
				CollectionUtilities.createCollection (Util.getRootCls(kb)));
		//CollectionUtilities.createCollection (Util.getRootCls(kb)));
		dummyFrameCls.setVisible(false);
		dummyFrameMetaCls.setAbstract (false);
		
		Collection metaSlotParent = CollectionUtilities.createCollection (Util.getStandardMetaslot(kb));
		Cls dummyFrameMetaSlot = kb.createCls (DUMMY_FRAMES_META_SLOT_NAME, metaSlotParent);
		dummyFrameMetaSlot.setAbstract (false);
		
		Collection metaFacetParent = CollectionUtilities.createCollection (kb.getCls (Model.Cls.STANDARD_FACET));
		Cls dummyFrameMetaFacet = kb.createCls (DUMMY_FRAMES_META_FACET_NAME, metaFacetParent);
		dummyFrameMetaFacet.setAbstract (false);
		
		REFERENCES_FACET = kb.createFacet (REFERENCES_FACET_NAME);
		REFERENCES_SLOT = kb.createSlot (REFERENCES_SLOT_NAME);
		REFERENCES_SLOT.setValueType(ValueType.INTEGER);
		REFERENCES_FACET.setAssociatedSlot (REFERENCES_SLOT);
		dummyFrameMetaCls.addDirectTemplateSlot (REFERENCES_SLOT);
		dummyFrameCls.addDirectTemplateSlot(REFERENCES_SLOT);
		dummyFrameMetaSlot.addDirectTemplateSlot(REFERENCES_SLOT);
		dummyFrameMetaFacet.addDirectTemplateSlot(REFERENCES_SLOT);
		
		DUMMY_FRAME_META_CLS = dummyFrameMetaCls;
		DUMMY_FRAME_CLS = dummyFrameCls;
		DUMMY_FRAME_META_SLOT = dummyFrameMetaSlot;
		DUMMY_FRAME_META_FACET = dummyFrameMetaFacet;
		
		DUMMY_FRAME_META_CLS.setIncluded(true);
		DUMMY_FRAME_CLS.setIncluded(true);
		DUMMY_FRAME_META_SLOT.setIncluded(true);
		DUMMY_FRAME_META_FACET.setIncluded(true);
		REFERENCES_SLOT.setIncluded(true);
		REFERENCES_FACET.setIncluded(true);
		
	}
	
	static public void removeDummyFrames () {
		KnowledgeBase kb = DUMMY_FRAME_CLS.getKnowledgeBase();
		Collection instances = DUMMY_FRAME_CLS.getInstances();
		instances.addAll(DUMMY_FRAME_META_CLS.getInstances());
		Collection instances2 = new ArrayList (instances);
		Iterator i = instances2.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			kb.deleteFrame(next);
		}
		kb.deleteFrame(DUMMY_FRAME_CLS);
		kb.deleteFrame(DUMMY_FRAME_META_CLS);
		
		kb.deleteFrame(REFERENCES_FACET);
		kb.deleteFrame(REFERENCES_SLOT);
		kb.deleteFrame(DUMMY_FRAME_META_SLOT);
		kb.deleteFrame(DUMMY_FRAME_META_FACET);
		
	}
	
	static public Frame createDummyFrame (Frame frame, KnowledgeBase kb) {
		if (isDummyFrame (frame))
			return incrementReferenceNumber (frame, kb);
		
		KnowledgeBase sourceKb = frame.getKnowledgeBase();
		String frameName = (PromptTab.merging() || PromptTab.mapping()) ? Mappings.createNameWithSource (frame.getName(), sourceKb) : frame.getName();
		
		frameName += TEMP_SUFFIX;
		Frame f = kb.getFrame (frameName);
		
		Frame newFrame;
		
		if (f == null)
			newFrame = createNewDummyFrame (frameName, frame, kb);
		else
			newFrame = incrementReferenceNumber (f, kb);
		
		Mappings.createBindingToDummyFrame(frame, newFrame);
		newFrame.setIncluded(true);
		return newFrame;
		
	}
	
	static private  Frame createNewDummyFrame (String frameName, Frame frame, KnowledgeBase kb){
		Frame dummy = null;
		if (frame instanceof Cls) {
			dummy = kb.createCls (frameName, CollectionUtilities.createCollection (DUMMY_FRAME_CLS),
					DUMMY_FRAME_META_CLS);
			((Cls)dummy).setAbstract (false);
		}  else if (frame instanceof Slot) {
			dummy =  kb.createSlot (frameName,  DUMMY_FRAME_META_SLOT);
		} else if (frame instanceof Facet) {
			dummy = kb.createFacet(frameName, DUMMY_FRAME_META_FACET);
		} else if (frame instanceof Instance) {
			Frame typeMapping = Mappings.getWhatBecameOfIt(((Instance)frame).getDirectType());
			if (typeMapping == null || ! (typeMapping instanceof Cls))
				dummy = kb.createInstance(frameName, DUMMY_FRAME_CLS);
			else
				//            dummy = kb.createInstance(frameName, (Cls)typeMapping);
				dummy = kb.createInstance(frameName, DUMMY_FRAME_CLS);
		}
		((Instance)dummy).setOwnSlotValue (REFERENCES_SLOT, new Integer (1));
		dummy.setIncluded(true);
		return dummy;
	}
	
	static private Instance  incrementReferenceNumber (Frame frame, KnowledgeBase kb) {
		Instance instance = (Instance)frame;
		Integer current = (Integer)instance.getOwnSlotValue (REFERENCES_SLOT);
		current = new Integer (current.intValue() + 1);
		instance.setOwnSlotValue (REFERENCES_SLOT, current);
		return instance;
	}
	
	static public void replaceReferencesWithRealFrame (Frame dummy, Frame real) {
		KnowledgeBase kb = real.getKnowledgeBase();
		ReplaceReferences.replaceAllReferences(dummy, real, kb);
		kb.deleteFrame(dummy);
	}
	
	static public boolean referenceToNotReplace (Slot slot) {
		if (slot.getFrameID().equals (Model.SlotID.DIRECT_SUBCLASSES)) return true;
		if (slot.getFrameID().equals (Model.SlotID.DIRECT_INSTANCES)) return true;
		return false;
	}
	/*
	 static public boolean removeReferenceToDummy (String name, KnowledgeBase kb) {
	 Frame frame = kb.getFrame (name);
	 
	 if (frame == null || !(frame instanceof Cls)) return false;
	 
	 Cls cls = (Cls)frame;
	 int numberOfReferences = ((Integer)cls.getOwnSlotValue (REFERENCES_SLOT)).intValue();
	 if (numberOfReferences == 1) {
	 kb.deleteCls (cls);
	 return false;
	 }
	 else  {
	 cls.setOwnSlotValue (REFERENCES_SLOT, new Integer (numberOfReferences - 1));
	 return true;
	 }
	 
	 }
	 */
	
	static public String removeDummySuffix (String string) {
		int index =  string.indexOf (TEMP_SUFFIX);
		if (index == -1)
			return string;
		else
			return string.substring (0, index);
	}
	
	static public boolean isDummyFrame  (Frame f) {
		boolean returning = (f != null && KnowledgeBaseInMerging.isInTarget(f) &&
				(f instanceof Cls && ((Cls)f).hasType (DUMMY_FRAME_META_CLS) ||
						f instanceof Slot && ((Slot)f).hasType(DUMMY_FRAME_META_SLOT) ||
						f instanceof Facet && ((Facet)f).hasType (DUMMY_FRAME_META_FACET) ||
						f instanceof Instance && ((Instance)f).hasType (DUMMY_FRAME_CLS)));
		return returning;
	}
	
	
}