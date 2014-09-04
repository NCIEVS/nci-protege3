 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;

public class EDITACTION {
    public static final EDITACTION create = new EDITACTION("create");
    public static final EDITACTION modify = new EDITACTION("modify");
    public static final EDITACTION split = new EDITACTION("split");
    public static final EDITACTION merge = new EDITACTION("merge");
    public static final EDITACTION delete = new EDITACTION("delete");

    private final String myName; // for debug only

    private EDITACTION(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
