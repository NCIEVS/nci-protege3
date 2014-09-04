/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.anchorPrompt.*;
import edu.stanford.smi.protegex.anchorPrompt.Queue;

public class PathGenerator {
    private static int _depth;
    private static Cls _start;
    private static Queue _frontier;    // queue of FrontierElements; can be replaced with Stack


	public static void generatePathsFromCls (Cls cls, int d) {
		_depth = d;
        _start = cls;
        _frontier = new Queue ();
        Path firstPath = new Path(cls);
        firstPath.addNextElement(cls);
        _frontier.put (new FrontierElement (_start, firstPath));
        while (!_frontier.isEmpty()) {
            FrontierElement next = (FrontierElement)_frontier.get();
            processNextElement (next);
        }
    }

    private static void processNextElement (FrontierElement elt) {
 		Cls cls = elt.getCls ();
        Path path = elt.getPath();

		//clone and then retire the path that lead to this element
        Path newPath = new Path (path);
        finishPath (newPath);
        if (path.length() > _depth) return;

        Collection oneRemoved = generateOneRemoved (cls); //Collection of FrameSlotCombinations
//Log.trace ("elt: " + elt + ", oneRemoved size: " + oneRemoved.size(), PathGenerator.class, "processNextElement");
//Log.trace ("current frontier: " + _frontier + ", oneRemoved size: " + oneRemoved.size(), PathGenerator.class, "processNextElement");
        if (oneRemoved != null) {
         	Iterator i = oneRemoved.iterator();
            while (i.hasNext()) {
             	FrameSlotCombination next = (FrameSlotCombination) i.next();
//Log.trace ("next combination: " + next.getFrame() + ", " + next.getSlot() , PathGenerator.class, "processNextElement");
//Log.trace ("path" + path , PathGenerator.class, "processNextElement");
//SystemUtilities.pause();
                addNewFrontierElement (new Path (path), next);
            }
        }
    }

    private static void addNewFrontierElement (Path path, FrameSlotCombination pair) {
     	Slot slot = pair.getSlot();
        Cls nextCls = (Cls)pair.getFrame();
        if (slot.getName() == Model.Slot.DIRECT_SUPERCLASSES || slot.getName() == Model.Slot.DIRECT_SUBCLASSES) {
        	if (path.continueUpTheHierarchy()) {
        		path.addNextElement(nextCls);
        		_frontier.put(new FrontierElement (nextCls, path));
            }
        }
        else {
            path.addNextElement(slot);
            path.addNextElement(nextCls);
            _frontier.put(new FrontierElement (nextCls, path));
        }
    }

    static protected boolean member (String value, String[] array) {
     for (int i = 0; i < array.length; i++)
       if (value.equals (array[i]))
         return true;
     return false;
   }

    private static Collection generateOneRemoved (Cls cls) {
     	Collection result = new ArrayList();
        result.addAll (getSlotRefs (cls, false));
        result.addAll (getSlotRefs (cls, true));
        return result;
    }

    private static Collection getSlotRefs (Cls cls, boolean templateOrOwnSlots) {
     	Collection result = new ArrayList();
        Collection slots = templateOrOwnSlots ? cls.getDirectTemplateSlots() : cls.getOwnSlots();
        if (slots == null) return result;
        Iterator i = slots.iterator();
        while (i.hasNext()) {
         	Slot next = (Slot)i.next();
            if (!member (next.getName(), Parameters.OWN_SLOTS_TO_IGNORE) &&
            	(next.getValueType()==ValueType.INSTANCE || next.getValueType()==ValueType.CLS)) {
            	Collection values = new ArrayList();
                if (templateOrOwnSlots) {
                    Collection templateSlotValues = cls.getTemplateSlotValues(next);
                    Collection allowedClses;
                    if (next.getValueType()==ValueType.INSTANCE)
                       allowedClses = cls.getTemplateSlotAllowedClses(next);
                    else
                    	allowedClses = cls.getTemplateSlotAllowedParents(next);
                    if (templateSlotValues != null)
                    	values.addAll (templateSlotValues);
                    if (allowedClses != null)
                    	values.addAll (allowedClses);
                } else {
                 	values = cls.getOwnSlotValues(next);
                }
             	result.addAll(getRefsFromSlot (cls, next, values));
            }
        }
        return result;
    }

    private static Collection getRefsFromSlot (Cls cls, Slot slot, Collection values) {
     	Collection result = new ArrayList();
        if (values == null) return result;
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Frame next = (Frame)i.next();
         	// ensure that there are no loops: cls != next
            if (next instanceof Cls && cls != next)
            	result.add (new FrameSlotCombination (next, slot));
        }
        return result;
    }

    private static void finishPath (Path path) {
        path.finishPath();
        if (path.length() <= 1) return;
        Collection lastGroup = path.getFinishClses().getValues();
		Collection anchorsInLastGroup = getAnchorsFromCollection (lastGroup);
//Log.trace ("anchorsInLastGroup: " + anchorsInLastGroup, PathGenerator.class, "finishPath");
        if (anchorsInLastGroup == null || anchorsInLastGroup.size() == 0) return;
//        if (lastGroup.size() > anchorsInLastGroup.size()) return;
        Iterator i = anchorsInLastGroup.iterator();
        boolean firstTime = true;
        while (i.hasNext()) {
			Cls nextAnchor = (Cls)i.next();
            if (firstTime) {
             	path.setFinishAnchor(nextAnchor);
                Analysis.addPath (path);
            	firstTime = false;
            } else {
            	Path newPath = new Path (path);
                newPath.setFinishAnchor(nextAnchor);
                Analysis.addPath (newPath);
            }
        }
    }

    private static Collection getAnchorsFromCollection (Collection group) {
     	if (group == null) return null;
        Collection result = new ArrayList ();
        Iterator i = group.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            if (Analysis.isAnchor(next))
            	result.add(next);
        }
		return result;
    }

    static public class FrontierElement {
    // class and path that led to it
        Cls _cls;
        Path _path;

        FrontierElement (Cls cls, Path p) {
             _cls = cls;
             _path = p;
//Log.trace ("new element: " + this, this, "FrontierElement");
        }

        public Cls getCls () {return _cls; }

        public Path getPath () {return _path; }

        public String toString () {
//         	return _cls.getName();
         	return _cls.getName() + ":" + _path.toString();
        }
    }
}
