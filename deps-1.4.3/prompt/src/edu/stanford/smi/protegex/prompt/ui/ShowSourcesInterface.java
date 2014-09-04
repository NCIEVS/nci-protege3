 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import edu.stanford.smi.protege.model.Frame;

public interface ShowSourcesInterface {
  public Class getSelectedTabFrameType ();

  public void selectTab (Frame frame);

  public void unselectAll ();
}
