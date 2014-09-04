/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import java.io.*;

import edu.stanford.smi.protege.model.*;

public class Parameters {

    public static int DEPTH = 3;
    public static int EQUIVALENCE_THRESHOLD = 2 ;
    public static int HIGH_SCORE = 1;
    public static int LOW_SCORE = 1;

    private static int _iteration = 0;

    static public String [] OWN_SLOTS_TO_IGNORE =
       {
        Model.Slot.DIRECT_INSTANCES,
        Model.Slot.DIRECT_TEMPLATE_SLOTS,
        Model.Slot.NAME,
        Model.Slot.CONSTRAINTS,
        Model.Slot.DIRECT_TYPES,
        null,
        null
//       	Model.Slot.DIRECT_SUBCLASSES,
//       	Model.Slot.DIRECT_SUPERCLASSES,
	};

    static private final int [] DEPTH_VALUES = {4, 3, 2};
    static private final int [] EQUIVALENCE_THRESHOLD_VALUES = {2, 1, 0};
    static private final int [] HIGH_SCORE_VALUES = {3, 1};
    static private final int [] LOW_SCORE_VALUES = {1};


    static private final int SUBCLASSES_INDEX = 5;
    static private final int SUPERCLASSES_INDEX = 6;


    static public void nextParameterSet (PrintStream stream) {
    	setSlotsToIgnore ();
        setDepth();
        setEquivalenceGroupThreshold();
        setHighAndLowScore();
        if (stream != null)
	        printParameters (stream);
        _iteration++;

    }

  	static private void setSlotsToIgnore () {
   		OWN_SLOTS_TO_IGNORE [SUPERCLASSES_INDEX] = null;
        if (_iteration < (DEPTH_VALUES.length *
        				   EQUIVALENCE_THRESHOLD_VALUES.length *
                           HIGH_SCORE_VALUES.length *
                           LOW_SCORE_VALUES.length))
            OWN_SLOTS_TO_IGNORE [SUBCLASSES_INDEX] = null;
        else
        	OWN_SLOTS_TO_IGNORE [SUBCLASSES_INDEX] = Model.Slot.DIRECT_SUBCLASSES;

   	}

	static private void setDepth () {
        DEPTH = DEPTH_VALUES[_iteration % DEPTH_VALUES.length];
   	}

	static private void setEquivalenceGroupThreshold () {
    	EQUIVALENCE_THRESHOLD =
        		EQUIVALENCE_THRESHOLD_VALUES [(_iteration /  DEPTH_VALUES.length) %
   	                                          EQUIVALENCE_THRESHOLD_VALUES.length];
    }

    static private void setHighAndLowScore () {
    	HIGH_SCORE =
        	HIGH_SCORE_VALUES [ (_iteration / (DEPTH_VALUES.length *
            									EQUIVALENCE_THRESHOLD_VALUES.length)) %
                                 HIGH_SCORE_VALUES.length];
    	LOW_SCORE =
        	LOW_SCORE_VALUES [ (_iteration / (DEPTH_VALUES.length *
            									EQUIVALENCE_THRESHOLD_VALUES.length *
                                                HIGH_SCORE_VALUES.length)) %
                                 LOW_SCORE_VALUES.length];

    }

    static boolean doneWithExperiments () {

    	boolean result = (_iteration == DEPTH_VALUES.length *
            				   EQUIVALENCE_THRESHOLD_VALUES.length *
                               HIGH_SCORE_VALUES.length *
                               LOW_SCORE_VALUES.length *
                               2);
        if (result) _iteration = 0;
     	return result;
//		return (_iteration == 3);
    }

    static private void printParameters (PrintStream stream) {
     	stream.println ("\n");
        stream.println ("DEPTH:\t" + DEPTH);
        stream.println ("EQUIVALENCE_THRESHOLD: " + EQUIVALENCE_THRESHOLD);
        stream.println ("HIGH_SCORE:\t" + HIGH_SCORE);
        stream.println ("LOW_SCORE:\t" + LOW_SCORE);
        if (OWN_SLOTS_TO_IGNORE[SUBCLASSES_INDEX] != null)
             stream.println ("Consider subclasses:\tno");
        else
             stream.println ("Consider subclasses:\tyes");
        if (OWN_SLOTS_TO_IGNORE[SUPERCLASSES_INDEX] != null)
             stream.println ("Consider superclasses:\tno");
        else
             stream.println ("Consider superclasses:\tyes");

    }

    static public boolean considerSubclasses () {
    	return OWN_SLOTS_TO_IGNORE [SUBCLASSES_INDEX] == null;
    }

}
