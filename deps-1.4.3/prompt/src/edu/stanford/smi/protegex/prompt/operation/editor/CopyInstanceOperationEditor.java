 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class CopyInstanceOperationEditor extends Editor {
  static final String [] _responsibleFor = new String [] {
  	OPERATIONS_PACKAGE + "KeepInstanceOperation"};

  public static String getPrettyName () {
    return "copy instance";
  }

  public GetValueWidget [] createValueWidgets () {
  	return createValueWidgets (false, false);
  }
  
  private static final int NUMBER_OF_WIDGETS = (PromptTab.extracting() ? 3 : 1);

  public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal) {
    _argumentWidgets = new GetValueWidget [NUMBER_OF_WIDGETS];
    _argumentWidgets[0] = new SelectInstanceWidget ("Source ontology", 0, chooseOntology, willBeModal);
    if (PromptTab.extracting()) {
		_argumentWidgets[1] = new CheckBoxWidget (COPY_EVERYTHING_RELATED, false);
		_argumentWidgets[2] = new GetSlotDirectivesWidget (this, Instance.class);
//   _argumentWidgets[1] = new CheckBoxWithValueWidget (SLOT_VALUES, false, DEPTH_LIMIT_PREFIX, DEPTH_LIMIT, DEPTH_LIMIT_SUFFIX, false);
  	}

    return _argumentWidgets;
  }
  
  public boolean copyEverythingRelated () {
  	return ((Boolean)_argumentWidgets[1].getValue()).booleanValue();
  }
  
  public void copyEverythingRelated (boolean b) {
	_argumentWidgets[1].setValue(new Boolean (b));
  }

  public Object [] createArgs (Operation a) {
   	Object [] args = a.getArgs().toArray();
	_args = new Object [NUMBER_OF_WIDGETS];
    _args[0] = args[0];
    if (PromptTab.extracting()) {
    	_args[1] = new Boolean (a.copyEverythingRelated());
    } 
    return _args;
  }

  public  String [] responsibleFor () {
   	return _responsibleFor;
  }

  public Operation collectData () {
  	Operation result = null;
    Object arg = _argumentWidgets[0].getValue ();
    
	boolean copyEverythingRelated = false;
	int levels = -1;
	HashMap slotLevels = null;

	if (PromptTab.extracting()) {
    	copyEverythingRelated = ((Boolean)_argumentWidgets[1].getValue()).booleanValue();
    	levels = ((GetSlotDirectivesWidget)_argumentWidgets[2]).getCommonLevel();

		slotLevels = (HashMap)((GetSlotDirectivesWidget)_argumentWidgets[2]).getValue();
	}

	TraversalDirective td = new TraversalDirective ((Instance)arg, slotLevels, true);
	if (slotLevels == null || slotLevels.size() == 0) {
		td.setCopyEverythingRelated (copyEverythingRelated);
	}

	if (levels > 0){
		td.setNumberOfLevels (levels);
	}

    result = new KeepInstanceOperation ((Instance)arg, td);

    for (int i = 0; i < _argumentWidgets.length; i++)
      _argumentWidgets[i].clear();
    return result;
  }

 }
