 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;

public class BuildPane extends JPanel {
  CreateNewOperationPane _operation;
  SourcesPane _sourcesTabbedPane;

  public BuildPane (Dimension size) {
    JPanel operationPanel = createOperationPanel ();
    Dimension operationPanelSize = operationPanel.getPreferredSize();


  	_sourcesTabbedPane = new SourcesPane (new Dimension (size.width,
                                                         size.height - operationPanelSize.height));
    setLayout (new BorderLayout());
    add (_sourcesTabbedPane, BorderLayout.CENTER);
    add (operationPanel, BorderLayout.SOUTH);
/*
	JSplitPane bigSplitPane = ComponentFactory.createTopBottomSplitPane(false);
    bigSplitPane.setTopComponent(sourcesTabbedPane);
    bigSplitPane.setBottomComponent(ComponentFactory.createScrollPane(operationPanel));

    add (bigSplitPane, BorderLayout.CENTER);
    bigSplitPane.setDividerLocation(.66);
*/
	_sourcesTabbedPane.addChangeListener(new ChangeListener () {
       	public void stateChanged (ChangeEvent event) {
         	_operation.postBuildTabChanged (_sourcesTabbedPane.getSelectedTabFrameType());
        }
    });
    _operation.postBuildTabChanged (_sourcesTabbedPane.getSelectedTabFrameType());
  }

  public void postTargetTabChanged (Class frameType) {
  	_operation.postTargetTabChanged (frameType);
  }

  private JPanel createOperationPanel () {
     _operation = new CreateNewOperationPane(true, false);
    JPanel operationPanel = new JPanel ();
    operationPanel.setLayout(new BorderLayout ());
    operationPanel.add(_operation, BorderLayout.CENTER);
  	return operationPanel;
  }

	public void addArgumentToOperation (Object o) {
     	_operation.addArgument((Cls)o);
    }

  public String toString () {
    return "BuildPane";
  }

}
