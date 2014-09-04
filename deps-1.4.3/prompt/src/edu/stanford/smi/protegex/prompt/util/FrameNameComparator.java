 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.util;

import java.util.*;
import edu.stanford.smi.protege.model.*;


public class FrameNameComparator implements Comparator {
    private boolean _reverseStrings = false;
    private boolean _trueComparison = false;

    public FrameNameComparator () {
    	super ();
    }

    public FrameNameComparator (boolean reverseStrings, boolean trueComparison) {
      // trueComparison is true is we don't want to consider "close-enough" matches; e.g. for strict ordering
    	super ();
     	_reverseStrings = reverseStrings;
        _trueComparison = trueComparison;
    }

    public int compare (Object o1, Object o2) {
    	if (_reverseStrings) {
            String name1 = Util.getLocalBrowserText((Frame)o1);
            StringBuffer buffer = new StringBuffer (name1);
            name1 = buffer.reverse().toString();

	    String name2 = Util.getLocalBrowserText((Frame)o2);
            buffer = new StringBuffer (name2);
            name2 = buffer.reverse().toString();

            return CompareNames.compareNames (name1, name2);
        }
        if (_trueComparison)
            return CompareNames.compareNamesWithExactMatch (Util.getLocalBrowserText((Frame)o1), Util.getLocalBrowserText((Frame)o2));

        return CompareNames.compareNames (Util.getLocalBrowserText((Frame)o1), Util.getLocalBrowserText((Frame)o2));
    }
}


