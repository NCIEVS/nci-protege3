 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.util.CompareNames;
import edu.stanford.smi.protegex.prompt.util.Util;

public class FramesWithSimilarNames implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return true;}
    public boolean usesFacetImageInformationInTable () {return true;}
    public boolean usesInstanceImageInformationInTable () {return true;}

    public boolean usesClassOperationInformationInTable () {return false;}
    public boolean usesSlotOperationInformationInTable () {return false;}
    public boolean usesFacetOperationInformationInTable () {return false;}
    public boolean usesInstanceOperationInformationInTable () {return false;}

    public boolean modifiesClassImageInformationInTable () {return true;}
    public boolean modifiesSlotImageInformationInTable () {return true;}
    public boolean modifiesFacetImageInformationInTable () {return true;}
    public boolean modifiesInstanceImageInformationInTable () {return true;}

    public boolean modifiesClassOperationInformationInTable () {return false;}
    public boolean modifiesSlotOperationInformationInTable () {return false;}
    public boolean modifiesFacetOperationInformationInTable () {return false;}
    public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

    private static ResultTable _results;
    private static boolean _changesMade = false;
    private static final int MAXIMUM_NUMBER_OF_FRAMES_TO_RUN = 100;

    public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start FramesWithSimilarNames ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        Collection unmatchedFromO1 = _results.getUnmatchedEntriesFromO1 ();
        Collection unmatchedFromO2 = _results.getUnmatchedEntriesFromO2 ();

        compareCollections (AlgorithmUtils.filterClses(unmatchedFromO1),
                            AlgorithmUtils.filterClses(unmatchedFromO2));
        compareCollections (AlgorithmUtils.filterSlots(unmatchedFromO1),
                            AlgorithmUtils.filterSlots(unmatchedFromO2));
        compareCollections (AlgorithmUtils.filterFacets(unmatchedFromO1),
                            AlgorithmUtils.filterFacets(unmatchedFromO2));
        compareCollections (AlgorithmUtils.filterInstances(unmatchedFromO1),
                            AlgorithmUtils.filterInstances(unmatchedFromO2));
    }
//*** perhaps should be done only for classes??
    private static void compareCollections (Collection c1, Collection c2) {
      if (c1 == null || c2 == null) return;
      if (c1.size() > MAXIMUM_NUMBER_OF_FRAMES_TO_RUN || c2.size() > MAXIMUM_NUMBER_OF_FRAMES_TO_RUN) return;

      Iterator i = c1.iterator();
      while (i.hasNext()) {
        Frame next1 = (Frame)i.next();
        Iterator j = c2.iterator();
        Frame next2 = null;
        while (j.hasNext()) {
          next2 = (Frame)j.next();
		  int comparison = CompareNames.compareNames(Util.getLocalBrowserText(next1), Util.getLocalBrowserText(next2));
          if (comparison == 0 || comparison == CompareNames.APPROXIMATE_MATCH) {
            AlgorithmUtils.createNewMatch(next1, next2, "Names are similar", _results);
            _changesMade = true;
            j.remove();
            break; 
          }
        }
        if (next2 != null) c2.remove(next2);
      }
    }

    public String toString () {
   	return "FramesWithSimilarNames";
    }


}

