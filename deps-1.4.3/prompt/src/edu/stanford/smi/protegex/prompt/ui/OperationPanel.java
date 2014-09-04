 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import javax.swing.JPanel;

import edu.stanford.smi.protegex.prompt.operation.editor.Editor;

public class OperationPanel {
    private Editor _editor;
    private JPanel _panel;

    public OperationPanel (Editor editor, JPanel panel) {
     	_editor = editor;
        _panel = panel;
    }

    public Editor getEditor () {return _editor;}

    public JPanel getPanel () {return _panel;}


  public String toString () {
    return "OperationPanel";
  }

}
