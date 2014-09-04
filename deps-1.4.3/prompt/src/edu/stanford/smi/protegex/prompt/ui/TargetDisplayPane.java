 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class TargetDisplayPane extends JTabbedPane implements ShowSourcesInterface {
  static private final int CLSES_PANEL = 0;
  static private final int SLOTS_PANEL = 1;
  static private final int INSTANCES_PANEL = 2;
  static private String _prefix = "Result";

	SourceClsesPane _classes;
    SourceSlotsPane _slots;
    SourceInstancesPane _instances;

    public TargetDisplayPane (Dimension size, KnowledgeBaseInMerging kbInMerging) {
     	initialize (size, kbInMerging, true, false);
    }

    public TargetDisplayPane (Dimension size, KnowledgeBaseInMerging kbInMerging, boolean allowSelection) {
     	initialize (size, kbInMerging, allowSelection, false);
    }

    public TargetDisplayPane (Dimension size, KnowledgeBaseInMerging kbInMerging, boolean allowSelection, String prefix, boolean isSource) {
    	    _prefix = prefix;
     	initialize (size, kbInMerging, allowSelection, isSource);
    }
    
    public SourceClsesPane getClsPanel() {
    	return _classes;
    }

    private void initialize (Dimension size, KnowledgeBaseInMerging kbInMerging, boolean allowSelection, boolean isSource) {
     	_classes = new SourceClsesPane (kbInMerging, allowSelection, isSource);
     	_slots = new SourceSlotsPane (kbInMerging, allowSelection, isSource);
     	_instances = new SourceInstancesPane (size, kbInMerging, allowSelection,isSource);
        add (_classes, CLSES_PANEL);
        setTitleAt (CLSES_PANEL, _prefix +" classes");

        add (_slots, SLOTS_PANEL);
        setTitleAt (SLOTS_PANEL, _prefix +" slots");

        add (_instances, INSTANCES_PANEL);
        setTitleAt (INSTANCES_PANEL, _prefix +" instances");

        setSelectedIndex(CLSES_PANEL);
        TabComponent.postTargetTabChanged (getSelectedTabFrameType());

        addChangeListener (new ChangeListener () {
       		public void stateChanged (ChangeEvent event) {
         		TabComponent.postTargetTabChanged (getSelectedTabFrameType());
        	}
	   });
    }

    public void addSelectionListener () {
     	_classes.addSelectionListener();
     	_slots.addSelectionListener();
     	_instances.addSelectionListener();
    }

    public void postKnowledgeBaseChanged () {
		_classes.updateDisplay();
        _slots.updateDisplay();
        _instances.updateDisplay();
    }

  public Class getSelectedTabFrameType () {
    switch (getSelectedIndex()) {
     	case CLSES_PANEL:
        	return Cls.class;
        case SLOTS_PANEL:
        	return Slot.class;
        case INSTANCES_PANEL:
        default:
        	return Instance.class;
    }
  }

  public String toString () {
   	return "TargetDisplayPane";
  }


  public void selectTab (Frame frame) {
   	if (frame instanceof Cls)
		setSelectedIndex (CLSES_PANEL);
    else if (frame instanceof Slot)
    	setSelectedIndex (SLOTS_PANEL);
    else if (frame instanceof Instance)
    	setSelectedIndex (INSTANCES_PANEL);
  }

  public void unselectAll () {
  	ComponentUtilities.applyToDescendents(this, new UnaryFunction () {
		public Object apply (Object o) {
            if (o instanceof SourceFramesPane)
            	((SourceFramesPane)o).unselect();
            return null;
        }
    });
  }
  
  public void revalidateClassDisplay () {
  	_classes.revalidateDisplay();
  }


}
