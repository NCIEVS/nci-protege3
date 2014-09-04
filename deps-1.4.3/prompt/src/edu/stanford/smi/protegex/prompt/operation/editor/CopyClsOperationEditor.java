 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class CopyClsOperationEditor extends Editor{
  static final String [] _responsibleFor = new String [] {
  		OPERATIONS_PACKAGE + "KeepClsOperation",
  		OPERATIONS_PACKAGE + "DeepCopyClsOperation",
  };

  public static String getPrettyName () {
    return "copy class";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }

  private static final int COPY_SUBCLASSES_INDEX = 1;
  private static final int COPY_SUPERCLASSES_INDEX = 3;
  private static final int COPY_INSTANCES_INDEX = 2;
  private static final int COPY_EVERYTHING_RELATED_INDEX = 4;
  private static final int NUMBER_OF_WIDGETS = (PromptTab.extracting() ? 6 : 4);

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
	_argumentWidgets = new GetValueWidget [NUMBER_OF_WIDGETS];
    _argumentWidgets[0] = new SelectClsWidget ("Source ontology", 0, chooseOntology, willBeModal);
    _argumentWidgets[COPY_SUPERCLASSES_INDEX] = new CheckBoxWidget (DEEP_COPY, false);
    _argumentWidgets[COPY_SUBCLASSES_INDEX] = new CheckBoxWidget (COPY_SUBCLASSES, false);
	_argumentWidgets[COPY_INSTANCES_INDEX] = new CheckBoxWidget (COPY_INSTANCES, false);
	if (PromptTab.extracting ()) {
		_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX] = new CheckBoxWidget (COPY_EVERYTHING_RELATED, false);
		_argumentWidgets[5] = new GetSlotDirectivesWidget (this, Cls.class);
	}
    return _argumentWidgets;
  }
  
  public  Operation collectData () {
	Operation result = null;
	Object arg = _argumentWidgets[0].getValue ();

	//deep or shallow copy
	boolean deepOrShallow = ((Boolean)_argumentWidgets[COPY_SUPERCLASSES_INDEX].getValue()).booleanValue();
	boolean copyTree = ((Boolean)_argumentWidgets[COPY_SUBCLASSES_INDEX].getValue()).booleanValue();
	boolean copyInstances = ((Boolean)_argumentWidgets[COPY_INSTANCES_INDEX].getValue()).booleanValue();
	HashMap slotLevels = null;
	boolean copyEverythingRelated = false;
	int levels = -1;
	
	if (PromptTab.extracting()) {
		copyEverythingRelated = ((Boolean)_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX].getValue()).booleanValue();
		levels = ((GetSlotDirectivesWidget)_argumentWidgets[5]).getCommonLevel();

		slotLevels = new HashMap ((HashMap)((GetSlotDirectivesWidget)_argumentWidgets[5]).getValue());
	}
	
	TraversalDirective td = new TraversalDirective ((Cls)arg, slotLevels, true);
	if (slotLevels == null || slotLevels.size() == 0) {
		td.setCopyEverythingRelated (copyEverythingRelated);
		td.setCopyInstances (copyInstances);
		td.setCopySubclasses (copyTree);
	}

	if (levels > 0){
		td.setNumberOfLevels (levels);
	}

	if (deepOrShallow == true)  //perform deep copy
		result = new DeepCopyClsOperation ((Cls)arg, td);
	else
		result = new KeepClsOperation ((Cls)arg, td, false);


	for (int i = 0; i < _argumentWidgets.length; i++)
	  _argumentWidgets[i].clear();

	return result;
  }

  public boolean copySubclasses () {
	return ((Boolean)_argumentWidgets[COPY_SUBCLASSES_INDEX].getValue()).booleanValue();
  }
  
  public boolean copySuperclasses () {
	return ((Boolean)_argumentWidgets[COPY_SUPERCLASSES_INDEX].getValue()).booleanValue();
  }
  
  public boolean copyInstances () {
  	return ((Boolean)_argumentWidgets[COPY_INSTANCES_INDEX].getValue()).booleanValue();
  }
  
  public boolean copyEverythingRelated () {
  	return ((Boolean)_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX].getValue()).booleanValue();
  }
  
  public void copySubclasses (boolean b) {
	_argumentWidgets[COPY_SUBCLASSES_INDEX].setValue(new Boolean (b));
  }

  public void copySuperclasses (boolean b) {
	_argumentWidgets[COPY_SUPERCLASSES_INDEX].setValue(new Boolean (b));
  }

  public void copyInstances (boolean b) {
	_argumentWidgets[COPY_INSTANCES_INDEX].setValue(new Boolean (b));
  }

  public void copyEverythingRelated (boolean b) {
	_argumentWidgets[COPY_EVERYTHING_RELATED_INDEX].setValue(new Boolean (b));
  }

  public Object [] createArgs (Operation a) {
   	Object [] args = a.getArgs().toArray();
	_args = new Object [NUMBER_OF_WIDGETS];
    _args[0] = args[0];
    _args[COPY_SUPERCLASSES_INDEX] = new Boolean (a instanceof DeepCopyFrameOperation);
    _args[COPY_SUBCLASSES_INDEX] = new Boolean (a.copySubclasses());
    _args[COPY_INSTANCES_INDEX] = new Boolean (a.copyInstances());
    if (PromptTab.extracting ())
		_args[COPY_EVERYTHING_RELATED_INDEX] = new Boolean (a.copyEverythingRelated());
//	_args[5] = new Boolean (a.copyEverythingRelated());
    return _args;
  }

  public String [] responsibleFor () {
   	return _responsibleFor;
  }

}
