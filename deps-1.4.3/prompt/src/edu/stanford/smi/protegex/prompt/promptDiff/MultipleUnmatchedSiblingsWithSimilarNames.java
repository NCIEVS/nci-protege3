 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.util.CompareNames;
import edu.stanford.smi.protegex.prompt.util.Util;

public class MultipleUnmatchedSiblingsWithSimilarNames implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return false;}
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

    // if there are multiple unmatched siblings of the same class, look for the ones that have similar names
    public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start MultipleUnmatchedSiblingsWithSimilarNames ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        ArrayList unmatchedFromO1 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO1 ());
        ArrayList unmatchedFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());

        if (unmatchedFromO1 == null || unmatchedFromO1.isEmpty()) return;


        int i = 0;
        while (!unmatchedFromO1.isEmpty() && i < unmatchedFromO1.size()) {
            Cls nextUnmatchedCls = (Cls)unmatchedFromO1.get(i);
            i++;
            // **** need to deal with multiple images here
            if (_results.getFirstImage(nextUnmatchedCls) == null) // i.e., we haven't found a match for it in
            	findMatch (nextUnmatchedCls, new ArrayList(unmatchedFromO1), new ArrayList(unmatchedFromO2)); // one of the previous iterations of this loop
        }
    }

    private static void findMatch (Cls unmatchedCls, Collection unmatchedInO1, Collection unmatchedInO2) {
     	Collection superclasses = Util.getDirectSuperclasses(unmatchedCls);
        Iterator supers = superclasses.iterator();
        while (supers.hasNext()) {
         	Cls nextSuper = (Cls)supers.next();
            // **** need to deal with multiple images here
            Cls nextSuperImage = (Cls)_results.getFirstImage(nextSuper);
            if (nextSuperImage != null) {
                Collection nextSubs = new ArrayList(Util.getDirectSubclasses(nextSuper));
    	        Collection nextSubsImages = AlgorithmUtils.getImages (nextSubs, _results);
        	Collection nextSuperImageSubs = new ArrayList(Util.getDirectSubclasses(nextSuperImage));
            	nextSuperImageSubs.removeAll(nextSubsImages);
                nextSuperImageSubs.retainAll(unmatchedInO2);
	        nextSubs.retainAll(unmatchedInO1);

                // now, nextSubs has unmatched subclasses of nextSuper
            	// and nextSuperImageSubs contains unmatched subclasses of nextSuperImage

	        if (!nextSubs.isEmpty() && !nextSuperImageSubs.isEmpty())
        	    compareNamesForSiblings (nextSubs, nextSuperImageSubs);
                AlgorithmUtils.removeClsesWithSingleParent (nextSubs, unmatchedInO1);
            }
        }
    }

    private static void compareNamesForSiblings (Collection clses1, Collection clses2) {
      if (clses1 == null || clses2 == null) return;
      Iterator i = clses1.iterator();

      while (i.hasNext()) {
        Cls nextCls1 = (Cls)i.next();
        Iterator j = clses2.iterator();
        while (j.hasNext()) {
          Cls nextCls2 = (Cls)j.next();
          if (CompareNames.compareNames(Util.getLocalBrowserText(nextCls1), Util.getLocalBrowserText (nextCls2)) == CompareNames.APPROXIMATE_MATCH) {
            AlgorithmUtils.createNewMatch(nextCls1, nextCls2, "multiple unmatched siblings with similar names", _results);
            _changesMade = true;
            j.remove();
            break;
          }
        }
      }
    }

	public String toString () {
   		return "MultipleUnmatchedSiblings";
  	}


}

