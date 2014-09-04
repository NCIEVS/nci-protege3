/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.analysis;

import edu.stanford.smi.protege.model.*;

public class PathPairToWalk {
	private Cls _start1;
	private Cls _start2;
	private Cls _finish1;
	private Cls _finish2;

    public PathPairToWalk (Cls c1, Cls c2, Cls c3, Cls c4) {
        _start1 = c1;
        _finish1 = c2;
        _start2 = c3;
        _finish2 = c4;
    }

    public Cls getFirstStartPoint () {return _start1;}

    public Cls getSecondStartPoint () {return _start2;}

    public Cls getFirstEndPoint () {return _finish1;}

    public Cls getSecondEndPoint () {return _finish2;}

    public String toString () {
     	return "_start1: " + _start1 + ", _finish1: " + _finish1
        	+ ", _start2: " + _start2 + ", _finish2: " + _finish2;
    }

}
