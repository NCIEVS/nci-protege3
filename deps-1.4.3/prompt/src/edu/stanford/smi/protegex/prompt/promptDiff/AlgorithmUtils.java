 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class AlgorithmUtils {
    public static ArrayList filterClses (Collection c) {
     	if (c == null) return null;
        Iterator i = c.iterator();
        ArrayList result = new ArrayList();
        while (i.hasNext()) {
         	Frame next = (Frame)i.next();
        	if (next instanceof Cls && next.isVisible())
            	result.add (next);
        }
        return result;
    }

    public static Collection filterSlots (Collection c) {
     	if (c == null) return null;
        Iterator i = c.iterator();
        Collection result = new ArrayList();
        while (i.hasNext()) {
         	Frame next = (Frame)i.next();
        	if (next instanceof Slot)
            	result.add (next);
        }
        return result;
    }

    public static Collection filterFacets (Collection c) {
     	if (c == null) return null;
        Iterator i = c.iterator();
        Collection result = new ArrayList();
        while (i.hasNext()) {
         	Frame next = (Frame)i.next();
        	if (next instanceof Facet)
            	result.add (next);
        }
        return result;
    }

    public static Collection filterInstances (Collection c) {
     	if (c == null) return null;
        Iterator i = c.iterator();
        Collection result = new ArrayList();
        while (i.hasNext()) {
         	Frame next = (Frame)i.next();
        	if (!(next instanceof Cls) && !(next instanceof Slot) && !(next instanceof Facet))
            	result.add (next);
        }
        return result;
    }

    // gets a single image for each element in the collection.
    public static Collection getImages (Collection frames, ResultTable results) {
        if (frames == null) return null;
     	Collection result = new ArrayList();
        Iterator i = frames.iterator();
        while (i.hasNext()){
         	Object nextImage = results.getFirstImage((Frame)i.next());
            if (nextImage != null)
            	result.add (nextImage);
        }
        return result;
    }

//	public static TableRow createNewMatch (Frame f1, Frame f2, boolean compareBrowserText) {
//		return createNewMatch (f1, f2, null, compareBrowserText);
//	}
//
	public static TableRow createNewMatch (Frame f1, Frame f2, String explanation, ResultTable resultTable) {
		return createNewMatch (f1, f2, explanation, false, resultTable);
	}
	
    private static TableRow createNewMatch (Frame f1, Frame f2, String explanation, boolean compareBrowserText, ResultTable results) {
         if (! (f1.getKnowledgeBase().equals(results.getKb1()) &&
        		f2.getKnowledgeBase().equals(results.getKb2()))) {
        		                       Log.getLogger().warning ("Trying to establish a match between frames from the same knowledge base");
        		                       return null;
        		        }
    	TableRow toUpdate = findRowWithEmptyImage (results.getRows(f1));
        if (toUpdate != null) {
        	results.setF2Value(toUpdate, f2);
        } else {
        	toUpdate = new TableRow (f1, f2);
         	results.addElement (toUpdate);
        }
        if (f1 != null && f2 != null && 
        	((compareBrowserText && !f1.getBrowserText().equals (f2.getBrowserText())) ||
        	 (!compareBrowserText && !f1.getName().equals(f2.getName()))))
          results.setRenameValue(toUpdate, TableRow.RENAME_PLUS);
        else
          results.setRenameValue(toUpdate, TableRow.RENAME_MINUS);
        if (explanation != null)
        	results.setRenameExplanation(toUpdate, explanation);
		TableRow toRemove = findRowWithNoSource (f2, results);
        results.removeElement (toRemove);
        return toUpdate;
   }

   private static TableRow findRowWithNoSource (Frame f, ResultTable results) {
    	Collection rows = results.getRows(f);
        if (rows == null) return null;
        Iterator i = rows.iterator();
        while (i.hasNext()) {
         	TableRow next = (TableRow)i.next();
           if (next.getF1Value() == null)
            	return next;
        }
        return null;
   }

   private static TableRow findRowWithEmptyImage (Collection rows) {
    	if (rows == null || rows.isEmpty()) return null;
        Iterator i = rows.iterator();
        while (i.hasNext()) {
        	TableRow next = (TableRow)i.next();
            if (next.getF2Value() == null)
            	return next;
        }
        return null;
   }

    // "same" means all slots of cls2 are the same as cls1 if considerImages is false
    // "same" means all slots of cls2 are images of slots at cls1 if considerImages is true
    public static boolean sameSlots (Cls cls1, Cls cls2, boolean considerImages, ResultTable results) {
     	Collection slots1 = cls1.getDirectTemplateSlots();
        Collection slots2 = new ArrayList(cls2.getDirectTemplateSlots());
        if (slots1.size() != slots2.size()) return false;
        if (considerImages)
        	slots2.removeAll(AlgorithmUtils.getImages(slots1, results));
        else
        	slots2.removeAll(slots1);
        return (slots2.isEmpty());
    }

    public static void removeClsesWithSingleParent (Collection toRemove, Collection from) {
    	Iterator i = toRemove.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            if (Util.getDirectSuperclasses(next).size() == 1)
            	from.remove(next);
        }
    }

    public static Collection findCommonSuperclasses (Collection clses) {
        Collection commonSuperclasses = new ArrayList();
     	Iterator i = clses.iterator();
        Cls first = (Cls)i.next();
        commonSuperclasses.addAll(Util.getDirectSuperclasses(first));
        while (i.hasNext() && !commonSuperclasses.isEmpty()) {
         	Cls next = (Cls)i.next();
			commonSuperclasses.retainAll(Util.getDirectSuperclasses(next));
        }
        return commonSuperclasses;
    }
    
    public static boolean compareCollections (Collection c1, Collection c2, ResultTable resultTable) {
    	c2 = new ArrayList (c2);
    	Iterator i = c1.iterator();
    	while (i.hasNext()) {
    		Object next1 = i.next();
    		Object nextToRemove = next1;
    		if (next1 instanceof Frame) {
				Collection next1Images = resultTable.getImages((Frame)next1);
				if (next1Images == null || next1Images.isEmpty())
					return false;
				Frame next1Image = (Frame)CollectionUtilities.getFirstItem(next1Images);
				nextToRemove = next1Image;
    		}
			if (!c2.remove(nextToRemove)) 
					return false;
    	}
    	if (c2.isEmpty()) return true;
    	
    	return false;
    }
    
	public String toString () {
   		return "AlgorithmUtils";
  	}


}

