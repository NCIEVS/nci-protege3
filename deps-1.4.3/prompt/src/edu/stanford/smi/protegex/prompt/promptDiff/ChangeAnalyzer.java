
/*
 * Contributor(s): Michel Klein <michel.klein@cs.vu.nl>
 *                 Natasha Noy <noy@smi.stanford.edu>
 */



package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.prompt.Preferences;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.prompt.util.Util;


public class ChangeAnalyzer {
	private static ResultTable _results;
	private boolean _firstRun = true;
	private String _operation;
	private boolean _changesMade = false;
	
	private static Collection _explanation = new ArrayList();
	
	
	public ChangeAnalyzer(ResultTable _result) {
		_results = _result;
	}
	
	public boolean findChanges() {
		// run the first time to find unchanged or isomorphic frames
		runAlgorithm();
		// run a second time to find additional unchanged frames
		runAlgorithm();
		
		return _changesMade;
	}
	
	private static int _count = 0;
	private void runAlgorithm () {
		Collection existingRows = new HashSet (_results.values());
		Iterator i = existingRows.iterator();
		_count = 1;
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			if (_count % 5000 == 0)
				Log.getLogger().info ("" + _count + ":" + new Date());
			_explanation.clear();
			if (updateTableEntry (next))
				_changesMade = true;
			_count ++;
		}
		if (_firstRun) _firstRun = false;
		
	}
	
	//returns true if the operation in the TableRow was changed
	private boolean updateTableEntry (TableRow row) {
		if (row.getOperationValue () != TableRow.OPERATION_MAP) return false;
		
		Frame f1 = row.getF1Value();
		Frame f2 = row.getF2Value();
		boolean unchanged = true;
		
		// ignore the unchanged or strong-isomorphic changes in the second run, because they
		// cannot be made "more unchanged"
		if (!_firstRun && (row.getMappingLevel() == TableRow.MAPPING_LEVEL_UNCHANGED ||
				row.getMappingLevel() == TableRow.MAPPING_LEVEL_STRONG_ISOMORPHIC))
			return false;
		
		if (PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousClassFrame (f1) && OWLUtil.isOWLAnonymousClassFrame (f2)) {
			OWLUtil.compareAnonymousClasses (row);
			return _firstRun;
		}
		
		if (PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousIndividual (f1) && OWLUtil.isOWLAnonymousIndividual (f2)) {
			OWLUtil.compareAnonymousIndividuals (row);
			return _firstRun;
		}
		
		if (f1 != null && Util.isSystem(f1)) {
			row.setMappingLevel (TableRow.MAPPING_LEVEL_UNCHANGED);
			return true;
		}
		
		_explanation.clear();
		if (_firstRun)
			row.clearOperationExplanation();
		else
			row.clearOperationExplanationsReferringToFrames();
		
		// will now try to raise from weak isomorphism to unchanged or strong isomorphism
		if (f1 == null || f2 == null) return false;
		
		if (!compareTypes (f1, f2)){
			unchanged = false;
		}
		
		if (f1 instanceof Cls && f2 instanceof Cls) {
			if (!compareSuperclasses ((Cls)f1, (Cls) f2)) {
				unchanged = false;
			}
			if (PromptTab.kbInOWL()) { //if kb is in OWL, need to compare restrictions rather than direct template slots
				if (!OWLUtil.compareRestrictions ((Cls)f1, (Cls)f2))
					unchanged = false;
				if (_firstRun && !OWLUtil.compareDefinedAndPrimitive ((Cls)f1, (Cls)f2))
					unchanged = false;
			} else {
				if (!compareDirectTemplateSlots ((Cls)f1, (Cls)f2)) {
					unchanged = false;
				}
				if (!compareOverriddenTemplateSlots ((Cls)f1, (Cls)f2)) {
					unchanged = false;
				}
			}
			
			
		}
		if (f1 instanceof Slot && f2 instanceof Slot) {
			if (!compareSuperslots ((Slot)f1, (Slot)f2)) {
				unchanged = false;
			}
			//          if (!compareSubslots ((Slot)f1, (Slot)f2)) {
			//            	unchanged = false;
			//          }
		}
		if (!compareOwnSlotValues ((Instance)f1, (Instance)f2, _firstRun)) {
			unchanged = false;
		}
		
		row.appendOperationExplanation (_explanation);
		
		// both conditionals are possible; first one is "cleaner", because there is only one place (TableRow)
		// where is decided whether a row has changed or not
		
		if (_firstRun)
			row.setDirectlyChanged();
		else
			row.setAllMappingLevels ();
		//        if (!row.isChanged()) {
		//        //if (unchanged) {
		//        	// set flag to know whether we detected a mapping in this round
		//        	// (cannot be detected on-the-fly, because new changes affect the value for getMappingLevel())
		//        	row.setMappingDetected();
		//        	return true;
		//        }
		
		return !unchanged;
	}
	
	
	private boolean compareTypes(Frame f1, Frame f2) {
		//compare direct types
		Instance inst1 = (Instance)f1;
		Instance inst2 = (Instance)f2;
		
		String relation = FrameDifferenceElement.TYPE;
		
		if (inst1 instanceof Cls) relation = FrameDifferenceElement.META_CLASS;
		else if (inst1 instanceof Slot) relation = FrameDifferenceElement.META_SLOT;
		
		Collection types1 = inst1.getDirectTypes();
		Collection types2 = inst2.getDirectTypes();
		return compareCollections (types1, types2, relation);
		
//		TableRow rowWithTypes = getRowWithFrames (type1, type2);
//		
//		if (rowWithTypes == null) {
//			_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT,
//					FrameDifferenceElement.OP_CHANGED, relation,
//					type1, type2));
//			return false;
//		} else if (rowWithTypes.getMappingLevel().equals(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED)){
//			_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_ISOMORPHIC,
//					FrameDifferenceElement.OP_CHANGED, relation, type1, type2));
//		}
//		
//		return true;
	}
	
	//    private boolean compareInstances (Cls cls1, Cls cls2) {
	//     	Collection instances1 = cls1.getDirectInstances();
	//        Collection instances2 = cls2.getDirectInstances();
	//        return compareCollections (instances1, instances2, FrameDifferenceElement.LEVEL_ISOMORPHIC, FrameDifferenceElement.INSTANCE);
	//    }
	//
	private boolean compareSuperclasses (Cls cls1, Cls cls2) {
		Collection superClses1 = Util.getDirectSuperclasses(cls1);
		Collection superClses2 = Util.getDirectSuperclasses(cls2);
		return compareCollections (superClses1, superClses2, FrameDifferenceElement.SUPERCLASS);
	}
	
	//    private boolean compareSubclasses (Cls cls1, Cls cls2) {
	//     	Collection subClses1 = cls1.getDirectSubclasses();
	//        Collection subClses2 = cls2.getDirectSubclasses();
	//        return compareCollections (subClses1, subClses2, FrameDifferenceElement.LEVEL_ISOMORPHIC, FrameDifferenceElement.SUBCLASS);
	//    }
	//
	private boolean compareSuperslots (Slot slot1, Slot slot2) {
		// getSuperslots is not implemented yet
		return true;
		/*
		 Collection superSlots1 = slot1.getSuperslots();
		 Collection superSlots2 = slot2.getSuperslots();
		 return compareCollections (superSlots1, superSlots2);f
		 */
	}
	
	//    private boolean compareSubslots (Slot slot1, Slot slot2) {
	//     	Collection subSlots1 = slot1.getSubslots();
	//     	Collection subSlots2 = slot2.getSubslots();
	//        return compareCollections (subSlots1, subSlots2, \FrameDifferenceElement.SUBSLOT);
	//    }
	//  
	
	
	private Collection getOwnSlots (Instance inst) {
		Collection ownSlots = new LinkedHashSet(inst.getOwnSlots());
		
		removeSystemSlots (inst.getKnowledgeBase(), ownSlots);
		return ownSlots;
	}
	
        private static HashMap<Instance, Collection<SlotValueCollectionsRecord>> _slotsToCheckOnSecondRun 
               = new HashMap<Instance, Collection<SlotValueCollectionsRecord>> (); //<inst; collection of objecttype slots that have a value>
	private boolean compareOwnSlotValues (Instance inst1, Instance inst2, boolean firstRun) {
		// true if values are the same for the same slots
		
		if (!firstRun)
			return compareStoredSlotValues (inst1);
		
		boolean unchanged = true;
		Collection<SlotValueCollectionsRecord> slotsToCheckOnSecondRunForThisInstance = new ArrayList<SlotValueCollectionsRecord>(); //Collection of records: <slot, values1, values2>
		
		Collection ownSlots1 = new LinkedHashSet (inst1.getOwnSlots());
		Collection ownSlots2 = new HashSet (inst2.getOwnSlots());
		
		if (ownSlots1.isEmpty() && ownSlots2.isEmpty()) return true;
		
		removeSystemSlots (inst1.getKnowledgeBase(), ownSlots1);
		removeSystemSlots (inst2.getKnowledgeBase(), ownSlots2);
		
		if (ownSlots1.isEmpty() && ownSlots2.isEmpty()) return true;
		
		Iterator i = ownSlots1.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			Collection rows = _results.getRows(nextSlot);
			TableRow row = getRowWithImage (rows);
			if (row == null) { // template slot doesn't exist anymore
				_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_DELETED, FrameDifferenceElement.OWN_SLOT, nextSlot, null));
				unchanged = false;
				continue;
			}
			Frame nextImage = row.getF2Value();
//			if (nextSlot.getFrameID().equals(Model.SlotID.NAME) && 
//					!(inst1 instanceof Cls || inst1 instanceof Slot || inst1 instanceof Facet)) {
//				ownSlots2.remove(nextImage);
//				continue; //assume that if individuals were mapped to each other and their :NAME slot (usually a meaningless name anyway) doesn't match, we don't care
//			}
			if (!ownSlots2.remove(nextImage)) {
				_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_ADDED, FrameDifferenceElement.OWN_SLOT, null, nextSlot));
				unchanged = false; // image is not a template slot of cls2
				continue;
			}
			//            if (!row.isMappingDetected()) {
			//                _explanation.add (new FrameDifferenceElement (FrameDifferenceElement.LEVEL_ISOMORPHIC, FrameDifferenceElement.OP_ALTERED, FrameDifferenceElement.OWN_SLOT, nextSlot, nextImage));
			//            }
			if (nextImage instanceof Slot)
				unchanged = dealWithValues (inst1, nextSlot, inst2, (Slot)nextImage, slotsToCheckOnSecondRunForThisInstance, unchanged);
		}
		
		_slotsToCheckOnSecondRun.put(inst1, slotsToCheckOnSecondRunForThisInstance);
		
		return processRemainder (ownSlots2, inst2, unchanged);
	}
	
	private boolean dealWithValues (Instance inst1, 
                                        Slot nextSlot, 
                                        Instance inst2, 
                                        Slot nextImageSlot, 
                                        Collection<SlotValueCollectionsRecord> slotsToCheckOnSecondRunForThisInstance, 
                                        boolean unchanged) {
		Collection values1 = Util.getDirectOwnSlotValues(inst1, nextSlot);
		Collection values2 = Util.getDirectOwnSlotValues(inst2, nextImageSlot);
		ValueType valueType1 = nextSlot.getValueType();
		ValueType valueType2 = nextImageSlot.getValueType();
		
		if (values1 != null && values2 != null && 
				(valueType1 == ValueType.INSTANCE || valueType1 == ValueType.CLS) &&
				(valueType2 == ValueType.INSTANCE || valueType2 == ValueType.CLS))
			slotsToCheckOnSecondRunForThisInstance.add (new SlotValueCollectionsRecord (nextSlot, values1, values2));
		
		if (!compareCollections (values1, values2, nextSlot, FrameDifferenceElement.OWN_SLOT_VALUE)){
			unchanged = false;
		}
		return unchanged;
	}
	
	private boolean processRemainder (Collection ownSlots2, Instance inst2, boolean unchanged) {
		
		if (ownSlots2.isEmpty())
			return unchanged;
		else {
			//          if (ownSlotsHaveValues (inst2, ownSlots2)) {
			Iterator r = ownSlots2.iterator();
			while (r.hasNext()) {
				Slot nextSlot = (Slot)r.next();
				if (inst2.getDirectOwnSlotValues(nextSlot) != null && !inst2.getDirectOwnSlotValues(nextSlot).isEmpty()) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_ADDED, FrameDifferenceElement.OWN_SLOT, null, nextSlot));
					unchanged = false;
				}
			}
			//                return false;
			//          } else
			return unchanged;
		}
	}
	
	private static boolean compareStoredSlotValues (Instance inst) {
		Collection<SlotValueCollectionsRecord> allRecords = _slotsToCheckOnSecondRun.get(inst);
		if (allRecords == null || allRecords.isEmpty())
			return true;
		
		boolean unchanged = true;
		Iterator<SlotValueCollectionsRecord> i = allRecords.iterator();
		while (i.hasNext()) {
			SlotValueCollectionsRecord nextRecord = i.next();
			//if (nextRecord.getSlot().getName().equals ("slot3"))
			//Log.trace ("here", ChangeAnalyzer.class, "compareStoredSlotValues");			
			if (!compareCollections (nextRecord.getValues1(), nextRecord.getValues2(), nextRecord.getSlot(), FrameDifferenceElement.OWN_SLOT_VALUE)) 
				unchanged = false;
		}
		return unchanged;
	}
	
	private static TableRow getRowWithImage (Collection rows) {
		if (rows == null) return null;
		Iterator i = rows.iterator();
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			if (next.getF2Value() != null)
				return next;
		}
		return null;
	}
	
	private HashMap<KnowledgeBase, Collection<Slot>> _systemSlotsToRemove 
	              = new HashMap<KnowledgeBase, Collection<Slot>> (2);
	private void removeSystemSlots (KnowledgeBase kb, Collection c) {
		if (c == null) return;
		Collection<Slot> systemSlots = _systemSlotsToRemove.get(kb);
		if (systemSlots == null || systemSlots.isEmpty()) {
			createSystemSlotsToRemove (kb);
			systemSlots = _systemSlotsToRemove.get(kb);
		} 
		Iterator<Slot> i = systemSlots.iterator();
		while (i.hasNext()) {
			c.remove (i.next());
		}
	}
	
	private void createSystemSlotsToRemove (KnowledgeBase kb) {
		Collection <Slot> systemSlots = new ArrayList<Slot>();
		SystemFrames frames = kb.getSystemFrames();

		systemSlots.add(frames.getDirectTypesSlot());
		systemSlots.add(frames.getDirectSuperclassesSlot());
		systemSlots.add(frames.getDirectSubclassesSlot());
		systemSlots.add(frames.getDirectTemplateSlotsSlot());
		systemSlots.add(frames.getDirectInstancesSlot());
		systemSlots.add(frames.getCreationTimestampSlot());
		systemSlots.add(frames.getCreatorSlot());
		systemSlots.add(frames.getModificationTimestampSlot());
		systemSlots.add(frames.getModifierSlot());
		systemSlots.addAll(createUserSpecifiedSlotsToRemove(kb));
		
		if (PromptTab.kbInOWL()) {
			systemSlots.addAll(OWLUtil.systemSlotsToIgnore((OWLModel) kb));
		}
		_systemSlotsToRemove.put(kb, systemSlots);
	}
	
	private List<Slot> createUserSpecifiedSlotsToRemove(KnowledgeBase kb) {
	    List<Slot> slots = new ArrayList<Slot>();
	    int counter = 0;
	    String property;
	    while ((property = ApplicationProperties.getString(
	                                Preferences.PROMPT_IGNORED_SYSTEM_SLOTS + "." + (counter++))) != null) {
	        Frame f = kb.getFrame(property);
	        if (f != null && f instanceof Slot) {
	            slots.add((Slot) f);
	        }
	    }
	    return slots;
	}
	
	private boolean ownSlotsHaveValues (Instance inst, Collection slots) {
		Iterator i = slots.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			Collection values = inst.getDirectOwnSlotValues(nextSlot);
			if (values != null && !values.isEmpty())
				return true;
		}
		return false;
	}
	
	private boolean compareDirectTemplateSlots (Cls cls1, Cls cls2) {
		boolean unchanged = true;
		Collection templateSlots1 = new ArrayList(cls1.getDirectTemplateSlots());
		Collection templateSlots2 = new ArrayList(cls2.getDirectTemplateSlots());
		
		
		if ((templateSlots1 == null) && (templateSlots2 == null)) return true;
		if (templateSlots1.isEmpty() && templateSlots2.isEmpty()) return true;
		
		
		Iterator i = templateSlots1.iterator();
		while (i.hasNext()) {
			Slot nextSlot =(Slot)i.next();
			Collection rows = _results.getRows(nextSlot);
			TableRow row = getRowWithImage (rows);
			if (row == null) { //slot has no image
				_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_DELETED, FrameDifferenceElement.TEMPLATE_SLOT, nextSlot, null));
				// the removed slot could have overriding facets; we need this to be complete
				if (cls1.hasDirectlyOverriddenTemplateSlot(nextSlot)) {
					listAllFacetOverridings(FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_DELETED, cls1, nextSlot);
					unchanged = false;
				}
				unchanged = false;
				continue;
			}
			Frame nextImage = row.getF2Value(); // image is not a template slot of cls2
			if (!templateSlots2.remove(nextImage)) {
				_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, FrameDifferenceElement.TEMPLATE_SLOT, nextSlot, null));
				if (cls1.hasDirectlyOverriddenTemplateSlot(nextSlot)) {
					listAllFacetOverridings(FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_DELETED, cls1, nextSlot);
					unchanged = false;
				}
				unchanged = false;
				continue;
			}
			//            if (!row.isMappingDetected()) {
			//                _explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_ISOMORPHIC, FrameDifferenceElement.OP_ALTERED, FrameDifferenceElement.TEMPLATE_SLOT, nextSlot, nextImage));
			//            }
			if (!PromptTab.kbInOWL() && !compareFacets (cls1, nextSlot, cls2, (Slot)nextImage)) unchanged = false;
		}
		if (templateSlots2.isEmpty())
			return unchanged;
		else {
			Iterator r = templateSlots2.iterator();
			while (r.hasNext()) {
				Slot next = (Slot) r.next();
				_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_ADDED, FrameDifferenceElement.TEMPLATE_SLOT, null, next));
			}
			return false;
		}
	}
	
	private boolean compareOverriddenTemplateSlots (Cls cls1, Cls cls2) {
		// Returns false if some of the mapped inherited slots have different directly overridden facets
		
		boolean unchanged = true;
		Collection templateSlots1 = getInheritedTemplateSlots(cls1);
		Collection templateSlots2 = getInheritedTemplateSlots(cls2);
		
		// If both sets of inherited slots is empty, facets can't be different
		if (templateSlots1.isEmpty() && templateSlots2.isEmpty()) return true;
		
		Iterator i = templateSlots1.iterator();
		while (i.hasNext()) {
			Slot nextSlot =(Slot)i.next();
			Collection rows = _results.getRows(nextSlot);
			TableRow row = getRowWithImage (rows);
			if (row == null) { // there was no image of slot
				// the removed slot could have overriding facets; we need this to be complete
				if (cls1.hasDirectlyOverriddenTemplateSlot(nextSlot)) {
					listAllFacetOverridings(FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_DELETED, cls1, nextSlot);
					unchanged = false;
				}
				continue;
			}
			Frame nextImage = row.getF2Value();
			if (!templateSlots2.remove(nextImage)) { // image was not a slot of other class
				// the detached slot could have overriding facets; we need this to be complete
				if (cls1.hasDirectlyOverriddenTemplateSlot(nextSlot)) {
					listAllFacetOverridings(FrameDifferenceElement.LEVEL_IMPLIED, FrameDifferenceElement.OP_DELETED, cls1, nextSlot);
					unchanged = false;
				}
				continue;
			}
			
			// we have found similar inherited slots, compare the facets now
			if (!compareFacets (cls1, nextSlot, cls2, (Slot)nextImage)) unchanged = false;
		}
		
		// check the remaining templateSlots2 for overridden facets
		if (!templateSlots2.isEmpty()) {
			Iterator r = templateSlots2.iterator();
			while (r.hasNext()) {
				Slot next = (Slot) r.next();
				if (cls2.hasDirectlyOverriddenTemplateSlot(next)) {
					listAllFacetOverridings(FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_ADDED, cls2, next);
					unchanged = false;
				}
			}
		}
		return unchanged;
	}
	
	private boolean compareFacets (Cls cls1, Slot slot1, Cls cls2, Slot slot2) {
		// Returns false if the directly overridden facets of the slot are different
		
		boolean unchanged = true;
		
		//for later: Remove "efficiency hook" at beginning
		// If there are no directly overridden slots, there can't be a difference
		if (!cls1.hasDirectlyOverriddenTemplateSlot(slot1)) {
			if (!cls2.hasDirectlyOverriddenTemplateSlot(slot2)) {
				// none has directly overridden facets
				return true;
			} else {
				// only slot2 has directly overridden facets
				listAllFacetOverridings(FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_ADDED, cls2, slot2);
				return false;
			}
		} else {
			if (!cls2.hasDirectlyOverriddenTemplateSlot(slot2)) {
				// only slot1 has directly overridden facets
				listAllFacetOverridings(FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, cls1, slot1);
				return false;
			} else {
				// both have directly overridden facets
				// Check whether they are the same
				Collection facets1 = new ArrayList(cls1.getTemplateFacets (slot1));
				Collection facets2 = new ArrayList(cls2.getTemplateFacets (slot2));
				
				Iterator i = facets1.iterator();
				while (i.hasNext()) {
					Facet nextFacet = (Facet)i.next();
					
					Collection rows = _results.getRows(nextFacet);
					TableRow row = getRowWithImage (rows);
					if (row == null) { // there was no image of facet
						listFacetOverridings(FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, cls1, slot1, nextFacet);
						unchanged = false;
						continue;
					}
					Frame nextImage = row.getF2Value();
					if (!facets2.remove(nextImage)) {  // image was no facet of other slot
						listFacetOverridings(FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, cls1, slot1, nextFacet);
						unchanged = false;
						continue;
					}
					
					// image is facet of other slot
					Facet imageFacet = (Facet)nextImage;
					// ignore if both are not directly overridden facets, then the cause of the change was somewhere else
					if (!cls1.hasDirectlyOverriddenTemplateFacet(slot1, nextFacet) &&
							!cls2.hasDirectlyOverriddenTemplateFacet(slot2, imageFacet)) {
						continue;
					}
					
					// now, compare values for directly overridden slots
					if (!compareCollections (cls1.getDirectTemplateFacetValues(slot1, nextFacet),
							cls2.getDirectTemplateFacetValues(slot2, imageFacet), slot1, nextFacet, FrameDifferenceElement.FACET_VALUE)) {
						
						unchanged = false;
					}
					
				}
				if (!facets2.isEmpty()) {
					Iterator r = facets2.iterator();
					while (r.hasNext()) {
						listFacetOverridings(FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_ADDED, cls2, slot2, (Facet)r.next());
					}
					unchanged = false;
				}
				return unchanged;
			}
		}
	}
	
	private void listAllFacetOverridings(int changeLevel, int operation, Cls cls, Slot slot) {
		Collection facets = cls.getTemplateFacets(slot);
		
		Iterator i = facets.iterator();
		while (i.hasNext()) {
			listFacetOverridings(changeLevel, operation, cls, slot, (Facet)i.next());
		}
	}
	
	
	private void listFacetOverridings(int changeLevel, int operation, Cls cls, Slot slot, Facet facet) {
		if (cls.hasDirectlyOverriddenTemplateFacet(slot, facet)) {
			// Overcome bug(?) in Protege
			//			String name = facet.getName();
			//			if (name.equals(":MAXIMUM-CARDINALITY") || name.equals(":VALUE-TYPE")) return;
			if (facet.getFrameID().equals(Model.FacetID.MAXIMUM_CARDINALITY) ||
					facet.getFrameID().equals(Model.FacetID.VALUE_TYPE)) return;
			Collection values = cls.getDirectTemplateFacetValues(slot, facet);
			Iterator r = values.iterator();
			
			while (r.hasNext()) {
				if(operation != FrameDifferenceElement.OP_DELETED){
					_explanation.add(new FrameDifferenceElement (changeLevel, operation, FrameDifferenceElement.FACET, slot, facet, "", r.next()));
				}else{
					_explanation.add(new FrameDifferenceElement (changeLevel, operation, FrameDifferenceElement.FACET, slot, facet, r.next(),""));
				}
			}
		}
	}
	
	private Collection getInheritedTemplateSlots(Cls cls) {
		Collection inheritedSlots = new ArrayList();
		Collection allSlots = cls.getTemplateSlots();
		Iterator i = allSlots.iterator();
		
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			if (!cls.hasDirectTemplateSlot(nextSlot))
				inheritedSlots.add(nextSlot);
		}
		return inheritedSlots;
	}
	
	private boolean facetsHaveValues (Cls cls, Slot slot, Collection facets) {
		Iterator i = facets.iterator();
		while (i.hasNext()) {
			Facet nextFacet = (Facet)i.next();
			Collection values = cls.getTemplateFacetValues (slot, nextFacet);
			if (values != null && !values.isEmpty())
				return true;
		}
		return false;
	}
	
	
	public static boolean compareCollections (Collection c1, Collection c2, String expl) {
		return compareCollections(c1, c2, null, null, expl);
	}
	
	public static  boolean compareCollections (Collection c1, Collection c2, Slot slot, String expl) {
		return compareCollections(c1, c2, slot, null, expl);
	}
	
	public static boolean compareCollections (Collection c1UnMod, Collection c2UnMod, Slot slot, Facet facet, String expl) {
		boolean unchanged = true;
		
		if (c1UnMod == null && c2UnMod == null) return true;
		if (c1UnMod.isEmpty() && c2UnMod.isEmpty()) return true;
		
		Collection c1 = new ArrayList (c1UnMod);
		Collection c2 = new ArrayList (c2UnMod);
		
		// Very special case: if the collection represent value-types, the collection starts with the "type" of values
		if ((c1.size()>=2) && (c2.size()>=2)) {
			if ( ((facet != null) && facet.getFrameID().equals(Model.FacetID.VALUE_TYPE)) ||
					((slot != null) && slot.getFrameID().equals(Model.SlotID.VALUE_TYPE)) ) {
				
				Object first = CollectionUtilities.getFirstItem(c1);
				//				Object second = i1.next();
				// if first object of c1 is also in c2, remove both
				if (c2.remove(first)) {
					c1.remove(first);
				}
			}
		}
		
		
		// Special case: if both collections only have one element, check if it is changed
		if ((c1.size() == 1) && (c2.size() == 1)) {
			Object o1 = CollectionUtilities.getSoleItem(c1);
			Object o2 = CollectionUtilities.getSoleItem(c2);
			
			if (o1 instanceof Frame && o2 instanceof Frame) {
				TableRow row = getRowWithFrames ((Frame)o1, (Frame)o2);
				if (row == null) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_CHANGED, expl, slot, facet, o1, o2));
					return false;
				} else if (row.getMappingLevel().equals(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED)){
					//***??? special-case owl restrictions: it seems that if restriction has change, it's still a direct change
					if (PromptTab.kbInOWL() && (OWLUtil.isOWLAnonymousClassFrame((Frame)o1) || OWLUtil.isOWLAnonymousIndividual((Frame)o1)))
						_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_CHANGED, expl, slot, facet, o1, o2));
					else
						_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_ISOMORPHIC, FrameDifferenceElement.OP_CHANGED, expl, slot, facet, o1, o2));
					return false;
				}
			} else {
				if (!o1.equals(o2)) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_CHANGED, expl, slot, facet, o1, o2));
					return false;
				}
			}
			return true;
		}
		
		Iterator i1 = c1.iterator();
		// Iterate through all collections
		while (i1.hasNext()) {
			Object next = i1.next();
			if (next instanceof Frame) {
				Collection rows = _results.getRows((Frame)next);
				TableRow row = getRowWithImage (rows);
				if (row == null) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, expl, slot, facet, next, ""));
					unchanged = false;
					continue;
				}
				Frame image = row.getF2Value();
				if (!c2.remove(image)) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, expl, slot, facet, next, ""));
					unchanged = false;
					continue;
				}
				if (PromptTab.kbInOWL()) {
					if (OWLUtil.isOWLAnonymousClassFrame((Frame)next)) {
						if (row.getMappingLevel().equals(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED)) {
							_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_CHANGED, expl, slot, facet, next, image));
							unchanged = false;
							continue;
						}
					}
				}
				
				if (row.getMappingLevel().equals(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED)) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_ISOMORPHIC, FrameDifferenceElement.OP_CHANGED, expl, slot, facet, next, image));
					unchanged = false;
					continue;
				}
				//	            if (!row.isMappingDetected()) {
				//                  _explanation.add(new FrameDifferenceElement (changeLevel, FrameDifferenceElement.OP_ALTERED, expl, slot, facet, next, image));
				//                }
			} else {
				if (!c2.remove(next)) {
					_explanation.add(new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_DELETED, expl, slot, facet, next, ""));
					unchanged = false;
					continue;
				}
			}
		}
		if (c2.isEmpty()) return unchanged;
		else {
			Iterator j = c2.iterator();
			while (j.hasNext()) {
				FrameDifferenceElement diffElt = new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_ADDED, expl, slot, facet, "", j.next());
				_explanation.add(diffElt);
			}
			return false;
		}
	}
	
	public static void addExplanation (String expl) {
		_explanation.add (new FrameDifferenceElement (FrameDifferenceElement.LEVEL_DIRECT, FrameDifferenceElement.OP_CHANGED, expl, null, null));
	}
	
	//assume that f1 is from column1 and f2 is from column2
	//    private static boolean isImage (Frame f1, Frame f2) {
	//    	Collection rowsForF1 = _results.getRows(f1);
	//        Collection rowsForF2 = _results.getRows(f2);
	//		if (rowsForF1 == null || rowsForF2 == null) return false;
	//        rowsForF1.retainAll(rowsForF2);
	//        return (!rowsForF1.isEmpty());
	//    }
	//
	//	private static String getMappingLevelForImages (Frame f1, Frame f2) {
	//		Collection rowsForF1 = _results.getRows(f1);
	//		Collection rowsForF2 = _results.getRows(f2);
	//		if (rowsForF1 == null || rowsForF2 == null) return null;
	//		rowsForF1.retainAll(rowsForF2);
	//		return ((TableRow)CollectionUtilities.getFirstItem (rowsForF1)).getMappingLevel();
	//	}
	
	private static TableRow getRowWithFrames (Frame f1, Frame f2) {
		Collection rowsForF1 = _results.getRows(f1);
		Collection rowsForF2 = _results.getRows(f2);
		if (rowsForF1 == null || rowsForF2 == null) return null;
		rowsForF1.retainAll(rowsForF2);
		if (rowsForF1.isEmpty()) return null;
		return ((TableRow)CollectionUtilities.getFirstItem (rowsForF1));
	}
	
	public String toString () {
		return "ChangeAnalyzer";
	}
	
	/*
	 private void removeUnchangedMappingsFromExplanation (TableRow row) {
	 Collection explanation = new ArrayList (row.getOperationExplanation());
	 if (explanation == null) return;
	 //Log.trace("row: " + row, FindUnchangedEntries.class, "removeUnchangedMappingsFromExplanation");
	  //Log.trace("explanation: " + explanation, FindUnchangedEntries.class, "removeUnchangedMappingsFromExplanation");
	   
	   Iterator i = explanation.iterator();
	   while (i.hasNext()) {
	   FrameDifferenceElement next = (FrameDifferenceElement) i.next();
	   //Log.trace ("next = " + next, FindUnchangedEntries.class, "removeUnchangedMappingsFromExplanation");
	    if (next.getMappingLevel() == TableRow.MAPPING_LEVEL_UNCHANGED)
	    row.removeOperationExplanation(next);
	    if (next.getMappingLevel() == TableRow.MAPPING_LEVEL_STRONG_ISOMORPHIC ||
	    next.getMappingLevel() == TableRow.MAPPING_LEVEL_WEAK_ISOMORPHIC)
	    next.setMappingLevel(TableRow.MAPPING_LEVEL_ISOMORPHIC);
	    }
	    }
	    */
	
	class SlotValueCollectionsRecord {
		private Slot _slot;
		private Collection _values1;
		private Collection _values2;
		
		SlotValueCollectionsRecord (Slot slot, Collection values1, Collection values2) {
			_slot = slot;
			_values1 = values1;
			_values2 = values2;
		}
		public Slot getSlot() {
			return _slot;
		}
		public Collection getValues1() {
			// XXX Auto-generated method stub
			return _values1;
		}
		public Collection getValues2() {
			// XXX Auto-generated method stub
			return _values2;
		}
	}
	
}
