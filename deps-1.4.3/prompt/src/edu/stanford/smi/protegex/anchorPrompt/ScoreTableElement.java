/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import edu.stanford.smi.protege.model.*;

    public class ScoreTableElement {
    	Cls _c1;
        Cls _c2;
        int _score;

        static final public int EQUIVALENT = 2000;
        static final public int SUBCLASS_SUPERCLASS = 1000;
        static final public int UNRELATED = -1000;

        int _correctness = UNRELATED;
        public ScoreTableElement (Cls c1, Cls c2, int score) {
         	_c1 = c1;
            _c2 = c2;
            _score = score;
        }
        
        public void incrementScore (int incrementBy) {
         	_score += incrementBy;
        }

        public boolean argsEqual (ScoreTableElement other) {
         	return (_c1.equals (other._c1) && _c2.equals (other._c2) ||
                    _c1.equals (other._c2) && _c2.equals (other._c1));
        }

        public int getScore () {return _score;}

        public String toString() {
         	return _c1.getName() +
            	 "\t" + _c2.getName() + "\t" + _score + "\t" + getCorretnessAsString ();
        }

        public Cls getFirstElement () {return _c1;}

        public Cls getSecondElement () {return _c2;}

        public boolean subsumes (ScoreTableElement s) {
//Log.trace("_c1 = " + _c1 + ", kb = " + _c1.getKnowledgeBase(), this, "subsumes");
//Log.trace("_c2 = " + _c2 + ", kb = " + _c2.getKnowledgeBase(), this, "subsumes");
//Log.trace("s._c1 = " + s._c1 + ", kb = " + s._c1.getKnowledgeBase(), this, "subsumes");
//Log.trace("s._c2 = " + s._c2 + ", kb = " + s._c2.getKnowledgeBase(), this, "subsumes");
         	return (_c1.hasSuperclass(s._c1) || s._c1.hasSuperclass(_c1) || (s._c1.equals (_c1))) &&
        	       (_c2.hasSuperclass(s._c2) || s._c2.hasSuperclass(_c2) || (s._c2.equals (_c2))) &&
                   (_score >= s._score);
        }

        static final public String EQUIVALENT_STRING = "equivalent";
        static final public String SUBCLASS_SUPERCLASS_STRING = "subclass-superclass";
        static final public String UNRELATED_STRING = "unrelated";

        public String getCorretnessAsString () {
         	if (_correctness == EQUIVALENT)
            	return EQUIVALENT_STRING;
         	if (_correctness == SUBCLASS_SUPERCLASS)
            	return SUBCLASS_SUPERCLASS_STRING;
         	if (_correctness == UNRELATED)
            	return UNRELATED_STRING;
            return null;
        }

        public static boolean isSubclassSuperclassValue (String string) {
         	return string.equals (SUBCLASS_SUPERCLASS_STRING);
        }

        public static boolean isEquivalentValue (String string) {
         	return string.equals (EQUIVALENT_STRING);
        }

        public void setCorrectness (int c) {
         	_correctness = c;
        }
    }

