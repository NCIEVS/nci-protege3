 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.Action;

public class Warning extends Action {
  final static int WARNING_ARITY = 2;

  final public static int FRAME_EXISTS = 1;
  final public static int FRAME_WITH_NAME_EXISTS = 2;
  final public static int MAPPING_EXISTS = 3;
  final public static int SLOT_EXISTS_AND_WILL_BE_ATTACHED = 4;

  final public static int ASK_TO_CONTINUE = 5;
  final public static int ASK_TO_COPY_SLOT = 6;
  final public static int SUGGEST_MERGE = 7;
  final public static int SUGGEST_LATER_MERGE = 8;
  final public static int USE_MAPPING = 9;
  final public static int ASK_TO_COPY_INSTANCES = 10;

  private Warning (Object [] args) {
    super (WARNING_ARITY);
    _args.setArg (0, args[0]);
    _args.setArg (1, args[1]);
  }

  private void setMainString (String string) {
   	_shortName = string;
  }

  private void setConnectorString (String string) {
   	_connectorString = string;
  }

  private void setTrailingString (String string) {
   	_trailingString = string;
  }

  static public boolean confirmToContinue
  			(int suggestion, String name, Frame existing) {
    Warning warning = new Warning (new Object [] {name, existing});
    warning.setMainString (" Frame with the name");
    warning.setConnectorString (" already exists.");
    setSuggestion (warning, suggestion);
    return  DisplayWarning.showYesOrNoDialog(warning);
  }

  static public boolean confirmToContinue
  			(int conflict, int suggestion, Frame f, Frame existing) {
   	Warning warning = new Warning (new Object [] {f, existing});
    if (conflict == FRAME_EXISTS) {
    	warning.setMainString (" Frame with the same name as");
    	warning.setConnectorString (" already exists:");
    } else if (conflict == MAPPING_EXISTS) {
    	warning.setMainString (" Frame");
    	warning.setConnectorString (" already has been mapped into");
    } else if (conflict == FRAME_WITH_NAME_EXISTS) {
    } else if (conflict == SLOT_EXISTS_AND_WILL_BE_ATTACHED) {
    	warning.setMainString (" Slot with the same name already exists:");
    	warning.setConnectorString (" and will be attached to the current frame if you don't copy:");
    }

// don't ask what to do if mapping exists. Assume that the answer is "no".
// (which it is 99% of the time
// will need to figure out later what to do to allow "yes"
// maybe a preference dialog
	if (conflict == MAPPING_EXISTS || conflict == FRAME_EXISTS) {
		if (suggestion == ASK_TO_CONTINUE || suggestion == ASK_TO_COPY_SLOT)
			return false;
		if (suggestion == ASK_TO_COPY_INSTANCES || suggestion == ASK_TO_COPY_SLOT || suggestion == USE_MAPPING)
			return true;
	} 

    setSuggestion (warning, suggestion);
    return  DisplayWarning.showYesOrNoDialog(warning);
  }

  static private void setSuggestion (Warning warning, int suggestion){
	if (suggestion == ASK_TO_CONTINUE)
    	warning.setTrailingString (" Continue with the current operation anyway?");
    else if (suggestion == ASK_TO_COPY_SLOT)
    	warning.setTrailingString (" Copy slot anyway?");
    else if (suggestion == SUGGEST_MERGE)
    	warning.setTrailingString (" Perform MERGE instead?");
    else if (suggestion == SUGGEST_LATER_MERGE)
	    warning.setTrailingString (" Merge the resulting frame with the existing class as well?");
    else if (suggestion == USE_MAPPING)
        warning.setTrailingString (" Do you want to use the latter frame instead of the former?");
    else if (suggestion == ASK_TO_COPY_INSTANCES)
    	warning.setTrailingString (" Copy only instances? (Answering 'no' will copy the class as well)");
  }

  static public void differentFrameTypesForMerge (Frame f1, Frame f2) {
  	Warning warning = new Warning (new Object [] {f1, f2});
    warning.setMainString (" Frame");
    warning.setConnectorString (" cannot be merged with");
    warning.setTrailingString (" Cannot merge frames of different types");
    DisplayWarning.showMessageDialog (warning);
  }


  static public void inform (String string) {
	ModalDialog.showDialog (PromptTab.getMainWindow(), new JLabel (string), "Cannot initialize", ModalDialog.MODE_CLOSE);
  }

}
