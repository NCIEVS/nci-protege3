 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class SingleUnmatchedDomain implements DiffAlgorithm {
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
		Log.getLogger().info ("*********** start SingleUnmatchedDomain ************");
		_changesMade = false;
		if (currentResults == null) return false;
		_results = currentResults;
		startAlgorithm ();
		return _changesMade;
	}

	private static void startAlgorithm () {
		ArrayList unmatchedClsesFromO1 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO1 ());
		ArrayList unmatchedClsesFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());
		

		if (unmatchedClsesFromO1 == null || unmatchedClsesFromO1.isEmpty()) return;
		if (unmatchedClsesFromO2 == null || unmatchedClsesFromO2.isEmpty()) return;
		
		Iterator i = unmatchedClsesFromO1.iterator();
		while (i.hasNext()) {
			Cls nextUnmatchedCls = (Cls)i.next();
			considerMatches (nextUnmatchedCls, unmatchedClsesFromO1, unmatchedClsesFromO2); // one of the previous iterations of this loop
		}
	}

	private static void considerMatches (Cls unmatchedCls, Collection unmatchedClsesFromO1, Collection unmatchedClsesFromO2) {
		Collection slotsWithClsAsDomain = unmatchedCls.getDirectTemplateSlots();
		if (slotsWithClsAsDomain == null || slotsWithClsAsDomain.isEmpty()) return;
		
		Iterator i = slotsWithClsAsDomain.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			Slot nextSlotImage = (Slot)_results.getFirstImage(nextSlot);
			if (nextSlotImage == null) continue;

			Collection nextSlotImageDomains = nextSlotImage.getDirectDomain();
			if (nextSlotImageDomains == null) continue;
			Util.removeAnonymousClses (nextSlotImageDomains);
			if (nextSlotImageDomains.size() != 1) continue;
			
			Cls nextSlotImageDomain = (Cls) CollectionUtilities.getSoleItem(nextSlotImageDomains);
			
			if (unmatchedClsesFromO2.contains(nextSlotImageDomain)) {
				AlgorithmUtils.createNewMatch (unmatchedCls, nextSlotImageDomain, "Domains for the same slot" + nextSlot, _results);
//Log.trace ("matched " + unmatchedCls + "and" + nextSlotImageDomain + "based on " + nextSlot,
//SingleUnmatchedDomain.class, "considerMatches");
				unmatchedClsesFromO2.remove(nextSlotImageDomain);
				_changesMade = true;
				break;
			}
		}
		
	}
	
	public String toString () {
		return "SingleUnmatchedDomain";
	}


}

