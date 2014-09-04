/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class Analysis {
	static MultiMap _anchorToPair;  //<frame, list of anchor pairs with the frame>
	static HashMap _anchorsFromKb0;  //<frameName, frame>  for kb0
	static HashMap _anchorsFromKb1;  //<frameName, frame>  for kb1
    static MultiMap _frameToPathsItStarts; //<frame, {all paths it starts}
    static MultiMap _frameToReachableAnchors; //<frame, {all other anchors reachable from it}
    static MultiMap _frameToPathsItFinishes; //<frame, {all paths it finishes}
    static Collection _pathPairs;
    static MultiMap _pathsBetweenPoints; // {start anchor name + finish anchor name, paths from start to finish}

    private static Collection _allAnchorClses;
    private static ArrayList _results;

	// returns a collection of ScoreTableElements
    public static Collection analyze (Collection anchorPairs) {
    	generateAllPathsFromAnchors(anchorPairs);
        generatePathsToWalk ();
        removeResultsBelowThreshold ();
        return _results;
    }

	// remove the lower half
	private static void removeResultsBelowThreshold () {
		ArrayList result = new ArrayList();
		int counter = _results.size();
		Iterator i = _results.iterator ();
		for (int n = 0; n < counter / 2; n++)
			result.add (i.next());
		_results = result;
	}

    private static void generatePathsToWalk () {
     	Collection anchorsFromKb0 = _anchorsFromKb0.values();
        Iterator i = anchorsFromKb0.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            getAllPathsFromAnchor (next, anchorsFromKb0);
        }
        _results = WalkPaths.walkPaths (_pathPairs);
    }

    private static void getAllPathsFromAnchor (Cls cls, Collection allAnchors) {
        Collection counterparts = getAnchorCounterparts (cls);
		Iterator i = counterparts.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            processPathsForAnchorPair (cls, next);
        }
    }

    private static void processPathsForAnchorPair (Cls cls1, Cls cls2) {
    // cls1 and cls2 are counterparts in an anchor pair
		Collection reachableAnchorsFromCls1 = _frameToReachableAnchors.getValues(cls1);
        Collection reachableAnchorsFromCls2 = _frameToReachableAnchors.getValues(cls2);
        if (reachableAnchorsFromCls1 == null || reachableAnchorsFromCls2 == null) return;
        Iterator i = reachableAnchorsFromCls1.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            Collection nextCounterparts = getAnchorCounterparts (next);
            if (nextCounterparts != null)
	            nextCounterparts.retainAll(reachableAnchorsFromCls2);
            if (nextCounterparts != null)
            	generatePathsToConsider (cls1, cls2, next, nextCounterparts);
        }
    }

    private static void generatePathsToConsider (Cls cls1, Cls cls2, Cls cls3, Collection c) {
     	Iterator i = c.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            // cls1 to cls3; cls2 to next
            PathPairToWalk newPair = new PathPairToWalk (cls1, cls3, cls2, next);
            if (!_pathPairs.contains(newPair))
           		 _pathPairs.add (new PathPairToWalk (cls1, cls3, cls2, next));
        }
    }

    public static Collection getAnchorPairs () {
        if (_anchorToPair == null) return null;
        Collection keys = _anchorToPair.getKeys();
        if (keys == null) return null;
        Set result = new HashSet ();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
			Object next = i.next();
         	Collection nextCollection = (Collection)_anchorToPair.getValues(next);
            if (nextCollection != null)
            	result.addAll(nextCollection);
        }
		return result;
    }

    private static Collection getAnchorCounterparts (Cls cls) {
    	Collection pairs = _anchorToPair.getValues(cls);
        if (pairs == null) return null;
        Collection result = new ArrayList();
        Iterator i = pairs.iterator();
        while (i.hasNext()) {
         	AnchorPair next = (AnchorPair)i.next();
            Frame counterpart = next.getCounterpart (cls);
            if (counterpart != null)
            	result.add (counterpart);
        }
        return result;
    }

    private static void generateAllPathsFromAnchors(Collection anchorPairs) {
      if (anchorPairs != null) {
        	unifyOrder (anchorPairs);
        	setMaps (anchorPairs);
                generatePaths (_anchorsFromKb0.values ());
                generatePaths (_anchorsFromKb1.values ());
        }
    }

    private static void unifyOrder (Collection originalAnchors) {
    	Iterator i = originalAnchors.iterator();
        AnchorPair firstPair =  (AnchorPair)i.next();
        KnowledgeBase kb1 = firstPair.getAnchor(0).getKnowledgeBase();
        while (i.hasNext()) {
         	AnchorPair next = (AnchorPair)i.next();
            KnowledgeBase kb = next.getAnchor(0).getKnowledgeBase();
            if (kb != kb1)
            	next.swapAnchors ();
        }

    }

    private static void setMaps (Collection anchorPairs) {
	//assume that the first element in each pair is
    // from the 1st ontology and the second is from the second
    //(not the other way around)
    	_anchorToPair = new ListMultiMap ();
        _anchorsFromKb0 = new HashMap (anchorPairs.size());
		_anchorsFromKb1 = new HashMap (anchorPairs.size());
        _frameToPathsItStarts = new ListMultiMap();
        _frameToPathsItFinishes = new ListMultiMap();
        _frameToReachableAnchors = new ListMultiMap();
        _pathsBetweenPoints = new ListMultiMap();
        _pathPairs = new ArrayList();

        Iterator i = anchorPairs.iterator();
        while (i.hasNext()) {
           	AnchorPair next = (AnchorPair) i.next();
            Frame anchor0 = (Frame)next.getAnchor (0);
            Frame anchor1 = (Frame)next.getAnchor (1);
            _anchorToPair.addValue (anchor0, next);
            _anchorToPair.addValue (anchor1, next);
            _anchorsFromKb0.put (anchor0.getName(), anchor0);
            _anchorsFromKb1.put (anchor1.getName(), anchor1);
        }

    }

	private static  void generatePaths (Collection clses) {
     	if (clses == null || clses.size() == 0) return;
    	_allAnchorClses = clses;
        Iterator i = clses.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
    		PathGenerator.generatePathsFromCls (next, Parameters.DEPTH);
        }
//SystemUtilities.pause();
//printPaths ();
    }

    private static  void printPaths () {
     	Collection allFrames = _frameToPathsItStarts.getKeys();
        Iterator i = allFrames.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls) i.next();
            Log.getLogger().info ("Paths starting at " + next);
            Collection paths = _frameToPathsItStarts.getValues(next);
			printPathsForCls (paths);
		}
    }

    private static void printPathsForCls (Collection paths) {
     	if (paths == null)
        	Log.getLogger().info ("No paths from the class");
        else {
         	Iterator i = paths.iterator();
            while (i.hasNext()) {
             	Path next = (Path)i.next();
                Log.getLogger().info ("" + next);
            }
        }
    }

    public static boolean isAnchor (Cls cls) {
    	Cls c = (Cls)_anchorsFromKb0.get(cls.getName());
    	if (c == null) return false;
         if (c.equals (cls)) return true;
        c = (Cls)_anchorsFromKb1.get(cls.getName());
        return (c.equals (cls));

    }

    public static void addPath (Path path) {
    	Cls startAnchor = path.getStartAnchor();
        Cls finishAnchor = path.getFinishAnchor();
		if (!exists (path)) {
        	_frameToPathsItStarts.addValue(startAnchor, path);
	        _frameToReachableAnchors.addValue(startAnchor, finishAnchor);
    	    _frameToPathsItFinishes.addValue (finishAnchor, path);
        	_pathsBetweenPoints.addValue (pathsBetweenPointsKey (startAnchor, finishAnchor), path);
        }
    }

    private static boolean exists (Path path) {
    	Cls startAnchor = path.getStartAnchor();
        Cls finishAnchor = path.getFinishAnchor();
     	Collection sameAnchorPaths =
        	_pathsBetweenPoints.getValues(pathsBetweenPointsKey (startAnchor, finishAnchor));
        if (sameAnchorPaths == null || sameAnchorPaths.size() == 0) {
        	return false;
        }
        Iterator i = sameAnchorPaths.iterator();
        while (i.hasNext()) {
         	Path next = (Path)i.next();
            if (next.equivalent (path))
            	return true;
        }
        return false;
    }

    private static String pathsBetweenPointsKey (Cls key1, Cls key2) {
     	return key1.getKnowledgeBase() + ":" + key1.getName() + "---" + key2.getName();
    }

    public static Collection getPathsBetweenPoints  (Cls key1, Cls key2) {
     	return _pathsBetweenPoints.getValues(pathsBetweenPointsKey (key1, key2));
    }

    public static Collection getAllPathsFromFrame (Cls cls) {
		return 	_frameToPathsItStarts.getValues(cls);
    }

    public static void clearMaps () {
    	_frameToPathsItStarts = null;
    	_frameToReachableAnchors = null;
    	_frameToPathsItFinishes = null;
    	_pathPairs = null;
    	_pathsBetweenPoints = null;
        WalkPaths.clearMaps();
    }
}
