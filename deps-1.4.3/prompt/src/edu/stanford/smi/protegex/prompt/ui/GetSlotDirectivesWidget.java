/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.editor.*;

public class GetSlotDirectivesWidget extends GetValueWidget { 
	private TraversalDefinitionDialog _dialog;
	private Editor _editor;
	
	public GetSlotDirectivesWidget (Editor editor, final Class frameType) {
		_dialog = new TraversalDefinitionDialog (frameType);
		_editor = editor;
		JButton button = ComponentFactory.createButton(new ButtonAction ());
		button.setText("Slot details...");
		button.setIcon(Icons.getSlotIcon());
		button.setMaximumSize(button.getPreferredSize());
		add (button);
	}
	
	public class ButtonAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			setValuesForDialog (_editor);
			_dialog.reload();
			int details = ModalDialog.showDialog(PromptTab.getMainWindow(), _dialog, 
				"Slot traversal depth", ModalDialog.MODE_OK_CANCEL, null, false);
			if (details == ModalDialog.OPTION_OK) {
				setValuesForEditor (_editor);	
			} 
			_dialog.restoreOld (details != ModalDialog.OPTION_OK);
		}
	}
	
	private void setValuesForEditor (Editor editor) {
		if (editor instanceof CopyClsOperationEditor) {
			((CopyClsOperationEditor)editor).copySubclasses (_dialog.copySubclasses());
			((CopyClsOperationEditor)editor).copySuperclasses (_dialog.copySuperclasses());
			((CopyClsOperationEditor)editor).copyInstances (_dialog.copyInstances());
			((CopyClsOperationEditor)editor).copyEverythingRelated (_dialog.copyEverythingRelated());
		}
		if (editor instanceof CopySlotOperationEditor) {
			((CopySlotOperationEditor)editor).copySubslots (_dialog.copySubslots());
			((CopySlotOperationEditor)editor).copySuperslots (_dialog.copySuperslots());
			((CopySlotOperationEditor)editor).copyEverythingRelated (_dialog.copyEverythingRelated());
		}
		if (editor instanceof CopyInstanceOperationEditor) {
			((CopyInstanceOperationEditor)editor).copyEverythingRelated (_dialog.copyEverythingRelated());
		}
	}
	
	private void setValuesForDialog (Editor editor) {
		if (editor instanceof CopyClsOperationEditor) {
			_dialog.copySubclasses (((CopyClsOperationEditor)editor).copySubclasses());
			_dialog.copySuperclasses (((CopyClsOperationEditor)editor).copySuperclasses());
			_dialog.copyInstances (((CopyClsOperationEditor)editor).copyInstances());
			_dialog.copyEverythingRelated (((CopyClsOperationEditor)editor).copyEverythingRelated());
		}
		if (editor instanceof CopySlotOperationEditor) {
			_dialog.copySubslots (((CopySlotOperationEditor)editor).copySubslots());
			_dialog.copySuperslots (((CopySlotOperationEditor)editor).copySuperslots());
			_dialog.copyEverythingRelated (((CopySlotOperationEditor)editor).copyEverythingRelated());
		}
		if (editor instanceof CopyInstanceOperationEditor) {
			_dialog.copyEverythingRelated (((CopyInstanceOperationEditor)editor).copyEverythingRelated());
		}
	}

// returns a collection of slot-number pairs
	public Object getValue() {
		return _dialog.getValue();
	}
	
	public int getCommonLevel () {
		return _dialog.getCommonLevel();
	}

	public void clear() {
		_dialog.clear();
	}

	public void setValue(Object value) {
		Log.getLogger().severe ("should never be here: GetSlotDirectivesWidget.setValue");
	}

}
