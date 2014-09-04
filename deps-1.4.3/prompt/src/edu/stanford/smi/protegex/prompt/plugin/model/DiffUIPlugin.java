 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  */

package edu.stanford.smi.protegex.prompt.plugin.model;

import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;

/**
 * Interface for extending the PromptDiff user interface.
 * @author seanf
 *
 */
public interface DiffUIPlugin extends PromptUIPlugin {
	public void saveToFile(String fileName, TableRow[] tableRows, FrameDifferenceElement[] frameDifferencElements);
	
	public String getFileFormatName();
}
