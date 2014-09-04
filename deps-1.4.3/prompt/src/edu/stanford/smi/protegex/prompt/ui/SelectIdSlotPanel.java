/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class SelectIdSlotPanel extends JPanel {
	private SelectIdSlotWidget _selectSlot;
	private JCheckBox _checkBox;

	public SelectIdSlotPanel() {
		_selectSlot = new SelectIdSlotWidget();
		_checkBox = ComponentFactory.createCheckBox();
		_checkBox.setText("Use only concept id slot for comparison (if specified)");
		setLayout (new FlowLayout());
		add (_selectSlot);
		add (_checkBox);
	}
	
	public boolean useIdSlotOnly () {
		return _checkBox.isSelected () && _selectSlot.getValue() != null;
	}
	
	public  Slot getIdSlot () {
		return _selectSlot.getValue();
	}
	
	public class SelectIdSlotWidget extends SelectableContainer {
		private JList _list;
		private ViewAction _viewAction;
		private Project _project;

		public SelectIdSlotWidget () {
			_project = PromptTab.getTargetProject();
			LabeledComponent lc = new LabeledComponent ("Select a slot containing a concept ID (optional)", createSelectionWidget());
			addButtons (lc);
			add(lc);
		}

		public void setInstance(Instance instance) {
			ComponentUtilities.setListValues(_list, CollectionUtilities.createCollection(instance));
		}
	
		private JList createSelectionWidget () {
			_list = ComponentFactory.createSingleItemList(null);
			_list.setCellRenderer(FrameRenderer.createInstance());
			return _list;
		}

		protected void addButtons(LabeledComponent c) {
			c.addHeaderButton(new ViewAction ("ViewSlot", this) {
				public void onView (Object o) {
					_project.show ((Instance)o);	
				}
			});
			c.addHeaderButton (new AddAction("Add Slot") {
				public void onAdd() {
					Instance instance = edu.stanford.smi.protege.ui.DisplayUtilities.pickSlot (PromptTab.getMainWindow(), _project.getKnowledgeBase().getSlots());
					if (instance != null) {
						setInstance(instance);
					}
				}
			});
			c.addHeaderButton(new RemoveAction("Remove Instance", this) {
				public void onRemove(Object o) {
					setInstance(null);
				}
			});
		}
		
		public Slot getValue () {
			Collection result = ComponentUtilities.getListValues(_list);
			if (result == null || result.isEmpty()) return null;
			return (Slot)CollectionUtilities.getFirstItem(result);
		}


	}

}
