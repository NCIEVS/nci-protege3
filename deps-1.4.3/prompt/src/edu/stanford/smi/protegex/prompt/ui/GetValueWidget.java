 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import javax.swing.JPanel;

public abstract class GetValueWidget extends JPanel{
    public abstract Object getValue ();

    public abstract void clear ();

    public abstract void setValue (Object value);
}
