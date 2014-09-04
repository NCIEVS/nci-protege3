 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.util.FrameNameComparator;
import edu.stanford.smi.protegex.prompt.util.Util;

public class SiblingsWithSameSuffixes implements DiffAlgorithm {
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

    // if the set of unmatched siblings on the one side is the same as the one on the other side
    // and the only difference is the same suffix (or prefix), match them
	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start SiblingsWithSameSuffixes ************");
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
            if (_results.getFirstImage(nextUnmatchedCls) == null) // i.e., we haven't found a match for it in
            	considerMatches (nextUnmatchedCls, unmatchedFromO1, unmatchedFromO2); // one of the previous iterations of this loop
			i++;
        }
    }

    private static void considerMatches (Cls unmatchedCls, Collection unmatchedInO1, Collection unmatchedInO2) {
     	Collection superclasses = Util.getDirectSuperclasses(unmatchedCls);
        Iterator supers = superclasses.iterator();
        while (supers.hasNext()) {
         	Cls nextSuper = (Cls)supers.next();
            // **** need to deal with multiple images here
            Cls nextSuperImage = (Cls)_results.getFirstImage(nextSuper);
            if (nextSuperImage != null) {
	            Collection nextSubs = new ArrayList (Util.getDirectSubclasses(nextSuper));
	            nextSubs.retainAll(unmatchedInO1);
                if (nextSubs.size() < 2) continue;

    	        Collection nextSubsImages = AlgorithmUtils.getImages (nextSubs, _results);
        	    Collection nextSuperImageSubs = new ArrayList (Util.getDirectSubclasses(nextSuperImage));
            	nextSuperImageSubs.removeAll(nextSubsImages);

        	    // now, nextSubs has unmatched subclasses of nextSuper
            	// and nextSuperImageSubs contains unmatched subclasses of nextSuperImage
	            if (!nextSubs.isEmpty() && !nextSuperImageSubs.isEmpty() &&
    	        	(nextSubs.size() == nextSuperImageSubs.size()))
                	compareClsesForSuffixesOrPrefixes (nextSubs, nextSuperImageSubs);
				AlgorithmUtils.removeClsesWithSingleParent (nextSubs, unmatchedInO1);
            }
        }
    }

    private static void compareClsesForSuffixesOrPrefixes (Collection clses1, Collection clses2) {
    	compareClsesForSuffixesOrPrefixes (clses1, clses2, true);
    	compareClsesForSuffixesOrPrefixes (clses1, clses2, false);
    }

	// if compareForSuffixes is true, compare for suffixes, otherwise, compare for prefixes
    private static void compareClsesForSuffixesOrPrefixes (Collection clses1, Collection clses2,
    											    boolean compareForSuffixes) {
    	ArrayList v1 = new ArrayList (clses1);
        ArrayList v2 = new ArrayList (clses2);

        Comparator fc = compareForSuffixes ? new FrameNameComparator() : new FrameNameComparator(true, false);
        Collections.sort(v1, fc);
        Collections.sort(v2, fc);

        String firstFrameIn1 = ((Frame)v1.get(0)).getName();
        String firstFrameIn2 = ((Frame)v2.get(0)).getName();
        String potentialSuffixOrPrefix;
        if (firstFrameIn1.length() < firstFrameIn2.length()) {
        	potentialSuffixOrPrefix = compareForSuffixes ?
                                      	findSuffix (firstFrameIn1, firstFrameIn2) :
                                      	findPrefix (firstFrameIn1, firstFrameIn2);
            if (potentialSuffixOrPrefix != null &&
            	allSameSuffixes (v1, v2, potentialSuffixOrPrefix, compareForSuffixes))
            	createMatches (v1, v2, "same " + (compareForSuffixes ? "suffix" : "prefix") +
                					   " for siblings: " + potentialSuffixOrPrefix);
		} else {
        	potentialSuffixOrPrefix = compareForSuffixes ?
                                      	findSuffix (firstFrameIn2, firstFrameIn1) :
                                      	findPrefix (firstFrameIn2, firstFrameIn1);
            if (potentialSuffixOrPrefix != null &&
            	allSameSuffixes (v2, v1, potentialSuffixOrPrefix, compareForSuffixes))
            	createMatches (v1, v2, "same " + (compareForSuffixes ? "suffix" : "prefix") +
                					   " for siblings: " + potentialSuffixOrPrefix);
        }
    }

    private static String findSuffix (String shorter, String longer) {
    	if (!longer.startsWith (shorter)) return null;
        return longer.substring (shorter.length());
    }

    private static String findPrefix (String shorter, String longer) {
    	if (!longer.endsWith (shorter)) return null;
        return longer.substring (0, longer.length() - shorter.length());
    }

    private static boolean allSameSuffixes (Collection collectionWithShorterNames,
                                    Collection collectionWithLongerNames,
                                    String suffixOrPrefix, boolean compareForSuffixes) {
    	Iterator i = collectionWithShorterNames.iterator();
    	Iterator j = collectionWithLongerNames.iterator();
        while (i.hasNext()) {
         	Frame nextWithShorterName = (Frame)i.next();
            String nextShorterName = nextWithShorterName.getName();
         	Frame nextWithLongerName = (Frame)j.next();
            String nextLongerName = nextWithLongerName.getName();

            if (nextLongerName.equals
            			(compareForSuffixes ? nextShorterName.concat(suffixOrPrefix) :
                                              suffixOrPrefix.concat (nextShorterName)))
            	continue;
            else
            	return false;
        }
        return true;
    }

    private static void createMatches (Collection c1, Collection c2, String explanation) {
    	Iterator i = c1.iterator();
    	Iterator j = c2.iterator();

        while (i.hasNext())
     		AlgorithmUtils.createNewMatch ((Frame)i.next(), (Frame)j.next(), explanation, _results);
        _changesMade = true;
    }

	public String toString () {
   		return "SiblingsWithSameSuffixes";
  	}


}

