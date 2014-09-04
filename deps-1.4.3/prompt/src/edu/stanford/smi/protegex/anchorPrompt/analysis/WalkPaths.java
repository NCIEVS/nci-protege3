/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.Queue;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class WalkPaths {
	private static Collection _anchorPairsTableElements = null;
	private static HashMap _scoreTable = new HashMap ();

	public static ArrayList walkPaths (Collection c) {
     	if (c == null) return null;
        Iterator i = c.iterator();
        while (i.hasNext()) {
         	PathPairToWalk next = (PathPairToWalk)i.next();
            Collection pathSet1 = Analysis.getPathsBetweenPoints
            		(next.getFirstStartPoint (), next.getFirstEndPoint ());
            Collection pathSet2 = Analysis.getPathsBetweenPoints
            		(next.getSecondStartPoint (), next.getSecondEndPoint ());
            walkSets (pathSet1, pathSet2);
        }
		ArrayList result = createNewPairs ();
        return result;
    }

    private static ArrayList createNewPairs () {
     	Collection allScoreTableElements =  _scoreTable.values();
		ScoreElementComparator comparator = new  ScoreElementComparator ();
     	Object[] v = allScoreTableElements.toArray();

        Arrays.sort (v, comparator);
        Queue result = new Queue();
        for (int i = 0; i < v.length; i++) {
        	ScoreTableElement elt = (ScoreTableElement)v[i];
            if (!equalsToAnchorPair (elt) &&
            	!subsumed (elt, result.toCollection()))
            	result.put(elt);
        }
        result.reverse();
		ArrayList resultCollection = new ArrayList (result.toCollection ());
//        printScoreTable (resultCollection);
        return resultCollection;
    }

    private static boolean subsumed (ScoreTableElement elt, Collection c) {
     	Iterator i = c.iterator();
        while (i.hasNext()) {
        	ScoreTableElement next = (ScoreTableElement)i.next();
            if (next.subsumes(elt))
            	return true;
        }
        return false;
    }

    private static boolean equalsToAnchorPair (ScoreTableElement elt) {
     	if (_anchorPairsTableElements == null)
        	createAnchorPairsScoreTable();
        Iterator i = _anchorPairsTableElements.iterator();
        while (i.hasNext()){
         	if (elt.argsEqual ((ScoreTableElement)i.next())) {
            	return true;
            }
        }
        return false;
    }

    private static void createAnchorPairsScoreTable () {
    	Collection anchorPairs = Analysis.getAnchorPairs();
        _anchorPairsTableElements = new ArrayList();
        if (anchorPairs == null) return;
        Iterator i =  anchorPairs.iterator();
        while (i.hasNext()) {
         	AnchorPair next = (AnchorPair)i.next();
            ScoreTableElement nextElement =
            	new ScoreTableElement (next.getAnchor(0), next.getAnchor(1), -1);
            _anchorPairsTableElements.add (nextElement);
        }

    }


    private static void printScoreTable (Collection c) {
		if (c == null) return;
        Iterator i = c.iterator();
        while (i.hasNext()) {
            ScoreTableElement next = (ScoreTableElement)i.next();
            Log.getLogger().info (next.toString());
        }
    }

    private static Collection getPathsBetweenPointsToWalk (Cls p1, Cls p2) {
    // filter out the paths that differ only in the finish Equivalence group
        Collection allPathsBetweenPoints = Analysis.getPathsBetweenPoints (p1, p2);
        if  (allPathsBetweenPoints == null) return null;
        Iterator i = allPathsBetweenPoints.iterator();
        Collection result = new ArrayList();
        while (i.hasNext()) {
         	Path next = (Path) i.next();
			if (next.getFinishClses().getValues().size() == 1)
            	result.add(next);
		}
        return result;
    }

	private static void walkSets (Collection pathSet1, Collection pathSet2) {
    	if (pathSet1 == null || pathSet2 == null) return;;
        Iterator i = pathSet1.iterator();
        while (i.hasNext()) {
         	Path nexti = (Path) i.next();
            Iterator j = pathSet2.iterator();
            while (j.hasNext()) {
             	Path  nextj = (Path)j.next();
                walkSinglePaths (nexti, nextj);
            }
        }
    }

    private static void walkSinglePaths (Path path1, Path path2) {
        if (path1.length ()!= path2.length ()) return;
//		Log.trace ("\npath 1:\n" + path1 + "\n",
//                    WalkPaths.class, "walkSinglePaths");
//		Log.trace ("\npath 2:\n" + path2 + "\n",
//                    WalkPaths.class, "walkSinglePaths");
//		Log.trace ("\npath 1 from " + path1.getStartAnchor() + " to " + path1.getFinishAnchor()
//        			+ "\npath 2 from " + path2.getStartAnchor() + " to " + path2.getFinishAnchor() +
//                    "\n", WalkPaths.class, "walkSinglePaths");
        Collection pathGroups1 = path1.getPathEquivalenceGroups ();
        Collection pathGroups2 = path2.getPathEquivalenceGroups ();
        Iterator i = pathGroups1.iterator();
        Iterator j = pathGroups2.iterator();
        i.next(); j.next(); // move forward from anchors
        for (int n = 0; n < path1.length(); n++) {
        	EquivalenceGroup group1 = (EquivalenceGroup)i.next();
            EquivalenceGroup group2 =  (EquivalenceGroup)j.next();
//Log.getLogger().info ("     " + group1 + "    " + group2);
			incrementScoreForGroupMembers (group1, group2);
        }
    }

    private static void incrementScoreForGroupMembers (EquivalenceGroup eq1, EquivalenceGroup eq2) {
     	Collection values1 = eq1.getValues();
     	Collection values2 = eq2.getValues();
        if (values1.size() == 1 && values2.size () == 1)
        	incrementScore ((Cls)CollectionUtilities.getSoleItem(values1),
            				(Cls)CollectionUtilities.getSoleItem(values2), Parameters.HIGH_SCORE);
        else {
         	Iterator i = values1.iterator();
            while (i.hasNext()) {
             	Cls nexti = (Cls)i.next();
                Iterator j = values2.iterator();
                while (j.hasNext()) {
                 	Cls  nextj = (Cls)j.next();
                    incrementScore (nexti, nextj, Parameters.LOW_SCORE);
                }
            }
        }
    }

    private static void incrementScore (Cls c1, Cls c2, int score) {
    	String key = createScoreTableKey (c1, c2);
		Object elt = _scoreTable.get(key);
        ScoreTableElement newElt;
        if (elt == null) {
			newElt = new ScoreTableElement (c1, c2, score);
        } else {
         	newElt =  (ScoreTableElement) elt;
            newElt.incrementScore (score);
        }
        _scoreTable.put(key, newElt);
    }

    private static String createScoreTableKey (Cls c1, Cls c2) {
     	return c1.getName() + "---" + c2.getName();
    }


  private static class ScoreElementComparator implements Comparator {
    public int compare (Object o1, Object o2) {
      int score1 = ((ScoreTableElement)o1).getScore();
      int score2 = ((ScoreTableElement)o2).getScore();
      if (score1 == score2) return 0;
      if (score1 < score2) return -1;
      return 1;
    }
  }

  public static void clearMaps () {
	_anchorPairsTableElements = null;
    _scoreTable.clear();
  }

}


