 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;

public class UnmatchedInverseSlot implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return false;}
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

	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start UnmatchedInverseSlot ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        Collection unmatchedFromO1 = AlgorithmUtils.filterSlots(_results.getUnmatchedEntriesFromO1 ());
        Collection unmatchedFromO2 = AlgorithmUtils.filterSlots(_results.getUnmatchedEntriesFromO2 ());

		if (unmatchedFromO1 == null || unmatchedFromO1.isEmpty()) return;
        Iterator i =  unmatchedFromO1.iterator();
        while (i.hasNext()) {
         	Slot next = (Slot)i.next();
            Slot inverse = next.getInverseSlot();
            if (inverse == null) continue;
            // **** need to deal with multiple images here
            Slot inverseImage = (Slot)_results.getSoleImage(inverse);
            if (inverseImage == null) continue;
            Slot potentialMatch = inverseImage.getInverseSlot();
            if (potentialMatch == null) continue;
            if (unmatchedFromO2.contains(potentialMatch)) {
            	AlgorithmUtils.createNewMatch (next, potentialMatch, "inverses match", _results);
                _changesMade = true;
            }
        }

    }

	public String toString () {
   		return "UnmatchedInverseSlot";
  	}


}

