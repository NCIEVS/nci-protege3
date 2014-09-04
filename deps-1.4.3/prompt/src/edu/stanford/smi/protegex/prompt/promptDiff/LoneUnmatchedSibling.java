 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class LoneUnmatchedSibling implements DiffAlgorithm {
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

	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start LoneUnmatchedSibling ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        Collection unmatchedFromO1 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO1 ());
        Collection unmatchedFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());
        Collection loneSiblingsFromO1 = getLoneSiblings (unmatchedFromO1);
        if (loneSiblingsFromO1 == null)
        	return;

        Iterator i = loneSiblingsFromO1.iterator();
        while (i.hasNext()) {
         	Cls nextLoneSibling = (Cls)i.next();
            findMatch (nextLoneSibling, unmatchedFromO2);
        }
    }

    private static Collection getLoneSiblings (Collection unmatchesClses) {
     	Collection result = new ArrayList ();
        Iterator i = unmatchesClses.iterator();
        while (i.hasNext()) {
            Cls nextCls = (Cls)i.next();
			if (loneUnmatchedSibling (nextCls))
            	result.add(nextCls);
        }
        if (result.isEmpty()) return null;
        return result;
    }

    private static boolean loneUnmatchedSibling (Cls cls){
     	Collection superclasses = Util.getDirectSuperclasses(cls);
        Iterator supers = superclasses.iterator();
        while (supers.hasNext()) {
         	Cls nextSuper = (Cls)supers.next();
            // **** need to deal with multiple images here
			Object nextSuperImage = _results.getFirstImage(nextSuper);
            if (nextSuperImage == null) {
            	return false;
            }
            Collection subclasses = Util.getDirectSubclasses(nextSuper);
            Iterator subs = subclasses.iterator();
            while (subs.hasNext()) {
             	Cls nextSub = (Cls)subs.next();
                            // need to deal with multiple images here
                if (!nextSub.equals (cls) && _results.getFirstImage(nextSub) == null ) {
					return false;
                }
            }
        }
        return true;
    }

    private static void findMatch (Cls loneSibling, Collection unmatchedInO2) {
     	Collection superclasses = Util.getDirectSuperclasses(loneSibling);
        Iterator supers = superclasses.iterator();
        while (supers.hasNext()) {
         	Cls nextSuper = (Cls)supers.next();
			// need to deal with multiple images here
            Cls nextSuperImage = (Cls)_results.getFirstImage(nextSuper);
            Collection nextSubs = Util.getDirectSubclasses(nextSuper);
            Collection nextSubsImages = AlgorithmUtils.getImages (nextSubs, _results);
            Collection nextSuperImageSubs = new ArrayList(Util.getDirectSubclasses(nextSuperImage));
            nextSuperImageSubs.removeAll(nextSubsImages);

            if (nextSuperImageSubs.size() != 0) {
				Cls newMatch = null;
	            if (nextSuperImageSubs.size() == 1) {
            		newMatch = (Cls)CollectionUtilities.getSoleItem(nextSuperImageSubs);
        	    } else {
    	         	newMatch = findBestClsMatch (loneSibling, nextSuperImageSubs);
	            }
            	if (newMatch != null && unmatchedInO2.remove(newMatch)) {
        	    AlgorithmUtils.createNewMatch(loneSibling, newMatch, "lone unmatched sibling", _results);
    	            _changesMade = true;
	        }
            }
        }
    }

    private static Cls findBestClsMatch (Cls loneSibling, Collection remainingElements) {
     //**** implement this method possibly generating a split or a 1-to-many match
    	return null;
    }

	public String toString () {
   		return "LoneUnmatchedSibling";
  	}


}

