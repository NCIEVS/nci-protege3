 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;


public class ComparisonMode {
    public static final ComparisonMode UserName = new ComparisonMode("UserName");
    public static final ComparisonMode ConceptName = new ComparisonMode("ConceptName");
    public static final ComparisonMode ConceptInfo = new ComparisonMode("ConceptInfo");
    private final String myName; // for debug only

    private ComparisonMode(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
