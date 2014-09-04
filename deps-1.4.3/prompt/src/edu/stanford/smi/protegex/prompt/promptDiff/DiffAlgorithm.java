 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;


public interface DiffAlgorithm {
	//returns true if changes were made
//	public static boolean run (ResultTable currentResults);

    public boolean usesClassImageInformationInTable ();
    public boolean usesSlotImageInformationInTable ();
    public boolean usesFacetImageInformationInTable ();
    public boolean usesInstanceImageInformationInTable ();

    public boolean usesClassOperationInformationInTable ();
    public boolean usesSlotOperationInformationInTable ();
    public boolean usesFacetOperationInformationInTable ();
    public boolean usesInstanceOperationInformationInTable ();

    public boolean modifiesClassImageInformationInTable ();
    public boolean modifiesSlotImageInformationInTable ();
    public boolean modifiesFacetImageInformationInTable ();
    public boolean modifiesInstanceImageInformationInTable ();

    public boolean modifiesClassOperationInformationInTable ();
    public boolean modifiesSlotOperationInformationInTable ();
    public boolean modifiesFacetOperationInformationInTable ();
    public boolean modifiesInstanceOperationInformationInTable ();


}

