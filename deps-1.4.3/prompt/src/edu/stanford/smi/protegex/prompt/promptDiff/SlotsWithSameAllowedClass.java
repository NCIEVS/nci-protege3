 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class SlotsWithSameAllowedClass implements DiffAlgorithm {
	public boolean usesClassImageInformationInTable () {return true;}
	public boolean usesSlotImageInformationInTable () {return true;}
	public boolean usesFacetImageInformationInTable () {return false;}
	public boolean usesInstanceImageInformationInTable () {return false;}

	public boolean usesClassOperationInformationInTable () {return false;}
	public boolean usesSlotOperationInformationInTable () {return false;}
	public boolean usesFacetOperationInformationInTable () {return false;}
	public boolean usesInstanceOperationInformationInTable () {return false;}

	public boolean modifiesClassImageInformationInTable () {return false;}
	public boolean modifiesSlotImageInformationInTable () {return true;}
	public boolean modifiesFacetImageInformationInTable () {return false;}
	public boolean modifiesInstanceImageInformationInTable () {return false;}

	public boolean modifiesClassOperationInformationInTable () {return false;}
	public boolean modifiesSlotOperationInformationInTable () {return false;}
	public boolean modifiesFacetOperationInformationInTable () {return false;}
	public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

	private static ResultTable _results;
	private static boolean _changesMade = false;

	private static HashMap _slotToRangeMap = new HashMap();
	private static HashMap _rangeToSlotMap1 = new HashMap();
	private static HashMap _rangeToSlotMap2 = new HashMap();

	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
		Log.getLogger().info ("*********** start SlotsWithSameAllowedClass ************");
		_changesMade = false;
		if (currentResults == null) return false;
		_results = currentResults;
		startAlgorithm ();
		return _changesMade;
	}

	private static void startAlgorithm () {
		Collection unmatchedSlotsFromO1 = AlgorithmUtils.filterSlots (_results.getUnmatchedEntriesFromO1 ());
		Collection unmatchedSlotsFromO2 = AlgorithmUtils.filterSlots (_results.getUnmatchedEntriesFromO2 ());
		
		if (unmatchedSlotsFromO1 == null || unmatchedSlotsFromO1.isEmpty()) return;
		if (unmatchedSlotsFromO2== null || unmatchedSlotsFromO2.isEmpty()) return;
		
		_slotToRangeMap.clear();
		_rangeToSlotMap1.clear();
		_rangeToSlotMap2.clear();

		Collection unmatchedSlotsToConsider1 = slotsWithUniqueRange  (unmatchedSlotsFromO1, _rangeToSlotMap1);
		Collection unmatchedSlotsToConsider2 = slotsWithUniqueRange  (unmatchedSlotsFromO2, _rangeToSlotMap2);
		
		Iterator i = unmatchedSlotsToConsider1.iterator();
		while (i.hasNext()) {
			Slot nextUnmatchedSlot = (Slot)i.next();
			considerMatches (nextUnmatchedSlot); 
		}
	}

	private static final String STRING_VALUE_TYPE = "String value type";
	private static final String BOOLEAN_VALUE_TYPE = "Boolean value type";
	private static final String NUMERIC_VALUE_TYPE = "Numeric value type";
	
	private static void considerMatches (Slot unmatchedSlot) {
		Object rangeValue = _slotToRangeMap.get(unmatchedSlot);
		if (rangeValue instanceof Cls) {
			Cls rangeCls = (Cls)rangeValue;
			Cls rangeClsImage = (Cls)_results.getFirstImage(rangeCls);
			Slot slotForRangeClsImage = (Slot)_rangeToSlotMap2.get(rangeClsImage);
			if (slotForRangeClsImage != null) {
				AlgorithmUtils.createNewMatch (unmatchedSlot, slotForRangeClsImage, "Slots with same unique value type", _results);
				_changesMade = true;
			}
		} else {
			Slot slot = (Slot)_rangeToSlotMap2.get(rangeValue);
			if (slot != null) {
				AlgorithmUtils.createNewMatch (unmatchedSlot, slot, "Slots with same unique value type", _results);
				_changesMade = true;
			}
				
		}
	}
	
	private static Collection slotsWithUniqueRange (Collection unmatchedSlots, HashMap rangeToSlotMap) {
		Iterator i = unmatchedSlots.iterator();
		Collection result = new ArrayList();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			ValueType nextValueType = nextSlot.getValueType();
			Object nextRangeValue = null;
			if (nextValueType == ValueType.STRING)
				nextRangeValue = STRING_VALUE_TYPE;
			else if  (nextValueType == ValueType.BOOLEAN)
				nextRangeValue = BOOLEAN_VALUE_TYPE;
			else if   (nextValueType == ValueType.FLOAT || nextValueType == ValueType.INTEGER)
				nextRangeValue = NUMERIC_VALUE_TYPE;
			else if  (nextValueType == ValueType.SYMBOL){
				nextRangeValue = nextSlot.getAllowedValues().toString();
			} else if (nextValueType == ValueType.INSTANCE) {
				Collection allowedClses = nextSlot.getAllowedClses();
				//*** can change this later to consider slots with multiple allowed classes
				if (allowedClses.size() != 1) continue;
				nextRangeValue = CollectionUtilities.getSoleItem(allowedClses);
			} else {
				Collection allowedClses = nextSlot.getAllowedParents();
				//*** can change this later to consider slots with multiple allowed classes
				if (allowedClses.size() != 1) continue;
				nextRangeValue = CollectionUtilities.getSoleItem(allowedClses);
			}
			Object exists = rangeToSlotMap.get(nextRangeValue);
			if (exists == null) {
				rangeToSlotMap.put(nextRangeValue, nextSlot);
				_slotToRangeMap.put(nextSlot, nextRangeValue);
				result.add(nextSlot);
			} else {
				_slotToRangeMap.remove (exists);		
				result.remove(exists);		
			}
		}
		return result;
	}
	
	public String toString () {
		return "ClassesWithSameSubclassAndSuperclass";
	}


}

