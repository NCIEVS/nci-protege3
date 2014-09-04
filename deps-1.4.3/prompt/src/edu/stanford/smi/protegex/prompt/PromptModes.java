 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
    *                 Kyle Bruck kbruck@stanford.edu
*/

package edu.stanford.smi.protegex.prompt;


public class PromptModes {
//    static public final String MERGING_MODE = "Merge two ontologies";
//    static public final String EXTRACTING_MODE = "Extract a part of an ontology";
//    static public final String MOVING_MODE = "Move frames from included into including project";
//    static public final String DIFF_MODE = "Compare versions of the ontology";

    static public final int MERGING_MODE = 0;
    static public final int EXTRACTING_MODE = 1;
    static public final int MOVING_MODE = 2;
    static public final int DIFF_MODE = 3;
    static public final int MAPPING_MODE = 4;

    static public final String MERGING_MODE_NAME = "Merge";
    static public final String EXTRACTING_MODE_NAME = "Extract";
    static public final String MOVING_MODE_NAME = "Move";
    static public final String DIFF_MODE_NAME = "Compare";
    static public final String MAPPING_MODE_NAME = "Map";

    static public final int [] _modes = {DIFF_MODE, MAPPING_MODE, EXTRACTING_MODE, MOVING_MODE, MERGING_MODE};

    static public final String MERGING_DETAILS = "two ontologies and add the resulting merged ontology to your current project.";
    static public final String DIFF_DETAILS = "your current ontology to a different version of the same ontology.";
    static public final String MOVING_DETAILS = "frames between your current including project and one of the included projects";
    static public final String EXTRACTING_DETAILS = "a portion of another ontology and add it to your current project.";
    static public final String MAPPING_DETAILS = "two ontologies and transform the data from one to another.";

//    static public final String MERGING_DETAILS = "<html><FONT COLOR=BLUE><B>Merge</B></FONT> two ontologies and add the resulting merged ontology to your current project.";
//    static public final String DIFF_DETAILS = "<html><FONT COLOR=BLUE><B>Compare</B></FONT> your current ontology to a different version of the same ontology.";
//    static public final String MOVING_DETAILS = "<html><FONT COLOR=BLUE><B>Move</B></FONT> frames between your current including project and one of the included projects"
//                            + "\n\tNote: Your current projects must have at least one included projects to perform this operation."
                            ;
//    static public final String EXTRACTING_DETAILS = "<html><FONT COLOR=BLUE><B>Extract</B></FONT> a portion of another ontology and add it to your current project.";

     public static String getDetails (int mode) {
    	 switch (mode) {
    	 case MAPPING_MODE: return MAPPING_DETAILS;
    	 case MERGING_MODE: return MERGING_DETAILS;
    	 case EXTRACTING_MODE: return EXTRACTING_DETAILS;
    	 case MOVING_MODE: return MOVING_DETAILS;
    	 case DIFF_MODE: return DIFF_DETAILS;
    	 
    	 }
    	 return null;
     }
    
    public static String getModeName (int mode) {
    	switch (mode) {
    	case MAPPING_MODE: return MAPPING_MODE_NAME;
    	case MERGING_MODE: return MERGING_MODE_NAME;
    	case EXTRACTING_MODE: return EXTRACTING_MODE_NAME;
    	case MOVING_MODE: return MOVING_MODE_NAME;
    	case DIFF_MODE: return DIFF_MODE_NAME;   	
    	}
    	return null;
    }

    public static int[] getModes () {return _modes;}

    public static int getNumberOfModes () {return _modes.length;}

	public static int getModeFromName(String name) {
		if (name.equals(MAPPING_MODE_NAME)) return MAPPING_MODE;
		if (name.equals(MERGING_MODE_NAME)) return MERGING_MODE;
		if (name.equals(EXTRACTING_MODE_NAME)) return EXTRACTING_MODE;
		if (name.equals(DIFF_MODE_NAME)) return DIFF_MODE;
		if (name.equals(MOVING_MODE_NAME)) return MOVING_MODE;
		return 0;
	}


}
