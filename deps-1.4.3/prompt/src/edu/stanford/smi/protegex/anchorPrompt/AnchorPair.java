/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import edu.stanford.smi.protege.model.*;

public class AnchorPair {
    Cls _anchor1;
    Cls _anchor2;
    float _related = 1; //1 = equivalent; 1/2 = related

    public AnchorPair () {
    }

    public AnchorPair (Cls a1, Cls a2) {
    	initialize (a1, a2, -1);
    }

    public AnchorPair (Cls a1, Cls a2, float r) {
    	initialize (a1, a2, r);
    }

    private void initialize (Cls a1, Cls a2, float r) {
     	_anchor1 = a1;
        _anchor2 = a2;
        if (r >= 0)
        	_related = r;
    }

    public void setArgument (Cls v, int index) {
     	if (index == 0) _anchor1 = v;
     	if (index == 1) _anchor2 = v;

    }

    public Cls getAnchor (int index) {
     	if (index == 0) return _anchor1;
     	if (index == 1) return _anchor2;
        return null;
    }

    public Cls getCounterpart (Cls cls) {
     	if (_anchor1 != null && _anchor1.equals (cls)) return _anchor2;
     	if (_anchor2 != null && _anchor2.equals (cls)) return _anchor1;
		return null;
    }

    public void swapAnchors () {
     	Cls temp = _anchor1;
        _anchor1 = _anchor2;
        _anchor2 = temp;
    }

    public String toString () {
     	return "Anchors: " + _anchor1.getName() + "\t" + _anchor2.getName();
    }
}
