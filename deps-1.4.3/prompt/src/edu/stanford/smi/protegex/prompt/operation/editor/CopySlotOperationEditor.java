 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class CopySlotOperationEditor  extends Editor {
  static private Object [] _args = new Object [2];
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "KeepSlotOperation",
  		OPERATIONS_PACKAGE + "DeepCopySlotOperation",
  };

  public static String getPrettyName () {
    return "copy slot";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  private static final int COPY_SUBSLOTS_INDEX = 1;
  private static final int COPY_SUPERSLOTS_INDEX = 2;
  private static final int COPY_EVERYTHING_RELATED_INDEX = 3;	
  private static final int SLOT_DIRECTIVE_INDEX = 4;	
  private static final int NUMBER_OF_WIDGETS = (PromptTab.extracting() ? 5 : 3);

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
    _argumentWidgets = new GetValueWidget [NUMBER_OF_WIDGETS];
    _argumentWidgets[0] = new SelectSlotWidget ("Source ontology", 0, chooseOntology, willBeModal);
	_argumentWidgets[COPY_SUPERSLOTS_INDEX] = new CheckBoxWidget (DEEP_COPY_SLOTS, false);
	_argumentWidgets[COPY_SUBSLOTS_INDEX] = new CheckBoxWidget (COPY_SUBSLOTS, false);
	if (PromptTab.extracting()) {
		_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX] = new CheckBoxWidget (COPY_EVERYTHING_RELATED, false);
		_argumentWidgets[SLOT_DIRECTIVE_INDEX] = new GetSlotDirectivesWidget (this, Slot.class);
//   _argumentWidgets[2] = new CheckBoxWithValueWidget (SLOT_VALUES, false, DEPTH_LIMIT_PREFIX, DEPTH_LIMIT, DEPTH_LIMIT_SUFFIX, false);
	}
    return _argumentWidgets;
  }
  
  public boolean copySubslots () {
	return ((Boolean)_argumentWidgets[COPY_SUBSLOTS_INDEX].getValue()).booleanValue();
  }
  
  public void copySubslots (boolean  b) {
	_argumentWidgets[COPY_SUBSLOTS_INDEX].setValue(new Boolean (b));
  }

  public boolean copySuperslots () {
	return ((Boolean)_argumentWidgets[COPY_SUPERSLOTS_INDEX].getValue()).booleanValue();
  }
  
  public void copySuperslots (boolean  b) {
	_argumentWidgets[COPY_SUPERSLOTS_INDEX].setValue(new Boolean (b));
  }

  public boolean copyEverythingRelated () {
   return ((Boolean)_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX].getValue()).booleanValue();
 }
  
 public void copyEverythingRelated (boolean b) {
   _argumentWidgets[COPY_EVERYTHING_RELATED_INDEX].setValue(new Boolean (b));
 }

 public Object [] createArgs (Operation a) {
	_args = new Object [NUMBER_OF_WIDGETS];
   	Object [] args = a.getArgs().toArray();
    _args[0] = args[0];
    _args[COPY_SUBSLOTS_INDEX] = new Boolean (a instanceof DeepCopyFrameOperation);
    if (PromptTab.extracting()) {
	    _args[COPY_EVERYTHING_RELATED_INDEX] = new Boolean (a.copyEverythingRelated());
	}
    return _args;
  }

  public Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();

    boolean deepOrShallow = ((Boolean)_argumentWidgets[COPY_SUPERSLOTS_INDEX].getValue()).booleanValue();
	boolean copySubslots = ((Boolean)_argumentWidgets[COPY_SUBSLOTS_INDEX].getValue()).booleanValue();
	boolean copyEverythingRelated = false;
	int levels = -1;
	HashMap slotLevels = null;
	
	if (PromptTab.extracting()) {
		copyEverythingRelated = ((Boolean)_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX].getValue()).booleanValue();
    	levels = ((GetSlotDirectivesWidget)_argumentWidgets[SLOT_DIRECTIVE_INDEX]).getCommonLevel();

		slotLevels = (HashMap)((GetSlotDirectivesWidget)_argumentWidgets[SLOT_DIRECTIVE_INDEX]).getValue();
	}
	TraversalDirective td = new TraversalDirective ((Slot)arg, slotLevels, true);
	if (slotLevels == null || slotLevels.size() == 0) {
		td.setCopyEverythingRelated (copyEverythingRelated);
		td.setCopySubslots (copySubslots);
	}

	if (levels > 0){
		td.setNumberOfLevels (levels);
	}

    if (deepOrShallow == true)  //perform deep copy
    	result = new DeepCopySlotOperation ((Slot)arg);
    else
        result = new KeepSlotOperation ((Frame)arg);


    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
    return result;
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }

/*
   public CopySlotOperationEditor () {
    _name = "copy-slot";
    _prettyName = "copy slot";
  }

 static public GetValueWidget [] getValueWidgets () {
   	return _argumentWidgets;
  }

  public void collectData () {
	//get frame
    _arg = _argumentWidgets[0].getValues ();
      if (_arg instanceof Collection)
    _arg = CollectionUtilities.getFirstItem ((Collection)_arg);

	//deep or shallow copy
    boolean deepOrShallow = ((Boolean)_argumentWidgets[1].getValues()).booleanValue();

    if (deepOrShallow == true)  //perform deep copy
    	_dispatchOperation = new DeepCopySlotOperation ((Slot)_arg);
    else
        _dispatchOperation = new KeepSlotOperation ((Frame)_arg);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
  }
*/
}
