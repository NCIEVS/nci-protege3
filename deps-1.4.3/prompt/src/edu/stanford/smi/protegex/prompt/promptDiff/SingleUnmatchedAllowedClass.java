
 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class SingleUnmatchedAllowedClass implements DiffAlgorithm {
	public boolean usesClassImageInformationInTable () {return true;}
	public boolean usesSlotImageInformationInTable () {return true;}
	public boolean usesFacetImageInformationInTable () {return false;}
	public boolean usesInstanceImageInformationInTable () {return false;}

	public boolean usesClassOperationInformationInTable () {return false;}
	public boolean usesSlotOperationInformationInTable () {return false;}
	public boolean usesFacetOperationInformationInTable () {return false;}
	public boolean usesInstanceOperationInformationInTable () {return false;}

	public boolean modifiesClassImageInformationInTable () {return true;}
	public boolean modifiesSlotImageInformationInTable () {return false;}
	public boolean modifiesFacetImageInformationInTable () {return false;}
	public boolean modifiesInstanceImageInformationInTable () {return false;}

	public boolean modifiesClassOperationInformationInTable () {return false;}
	public boolean modifiesSlotOperationInformationInTable () {return false;}
	public boolean modifiesFacetOperationInformationInTable () {return false;}
	public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

	private static ResultTable _results;
	private static boolean _changesMade = false;


	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
		Log.getLogger().info ("*********** start SingleUnmatchedAllowedClass ************");
		_changesMade = false;
		if (currentResults == null) return false;
		_results = currentResults;
		startAlgorithm ();
		return _changesMade;
	}

	private static Slot _valueTypeSlotForKb1;
	private static KnowledgeBase _kb1;
	private static void startAlgorithm () {
		ArrayList unmatchedClsesFromO1 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO1 ());
		ArrayList unmatchedClsesFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());
		
		_kb1 = _results.getKb1();
		_valueTypeSlotForKb1 = _kb1.getSlot(Model.Slot.VALUE_TYPE);

		if (unmatchedClsesFromO1 == null || unmatchedClsesFromO1.isEmpty()) return;
		if (unmatchedClsesFromO2 == null || unmatchedClsesFromO2.isEmpty()) return;
		Iterator i = unmatchedClsesFromO1.iterator();
		while (i.hasNext()) {
			Cls nextUnmatchedCls = (Cls)i.next();
			considerMatches (nextUnmatchedCls, unmatchedClsesFromO1, unmatchedClsesFromO2); // one of the previous iterations of this loop
		}
	}

	private static void considerMatches (Cls unmatchedCls, Collection unmatchedClsesFromO1, Collection unmatchedClsesFromO2) {
		Collection slotsWithClsAsAllowedCls = _kb1.getFramesWithValue(_valueTypeSlotForKb1, null, false, unmatchedCls);
		if (slotsWithClsAsAllowedCls == null || slotsWithClsAsAllowedCls.size() != 1) return;

		Iterator i = slotsWithClsAsAllowedCls.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			Collection nextSlotAllowedClses = nextSlot.getAllowedClses();
			if (nextSlotAllowedClses == null || nextSlotAllowedClses.size() == 0) continue;
			nextSlotAllowedClses.retainAll(unmatchedClsesFromO1);
			if (nextSlotAllowedClses.size() != 1) continue;

			Slot nextSlotImage = (Slot)_results.getFirstImage(nextSlot);
			if (nextSlotImage == null) continue;
			Collection nextSlotImageAllowedClses = nextSlotImage.getAllowedClses();
			if (nextSlotImageAllowedClses == null || nextSlotImageAllowedClses.size() == 0) continue;
			nextSlotImageAllowedClses.retainAll(unmatchedClsesFromO2);
			if (nextSlotImageAllowedClses.size() != 1) continue;
			
			Cls nextAllowedCls = (Cls)CollectionUtilities.getSoleItem(nextSlotAllowedClses);
			Cls nextAllowedClsImage = (Cls)CollectionUtilities.getSoleItem(nextSlotImageAllowedClses);
			AlgorithmUtils.createNewMatch (nextAllowedCls, nextAllowedClsImage, "Allowed classes for the same slot", _results);
			unmatchedClsesFromO2.remove(nextAllowedClsImage);
			_changesMade = true;
		}
		
	}
	
	public String toString () {
		return "SingleUnmatchedAllowedClass";
	}


}

