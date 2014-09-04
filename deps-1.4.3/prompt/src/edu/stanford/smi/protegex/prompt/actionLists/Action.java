 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.actionLists;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;

public class Action {
//  static private final String BEGIN_ONT_SUFFIX = "---from_";
//  static private final String END_ONT_SUFFIX = "_ont";
  static final protected int DEFAULT_PRIORITY = 10;
  static final protected int MAX_ARITY = 4;
  protected boolean _fromUser = false;

  protected int _arity = MAX_ARITY;

  protected String _name;
  protected String _prettyName = null;
  protected String _shortName = null;
  protected String _connectorString = null;
  protected String _connectorString2 = null;
  protected String _trailingString = null;

  protected ActionArgs _args;

  protected int _priority = DEFAULT_PRIORITY;

  public Action () {
    initialize (MAX_ARITY);
  }

  public Action (int ar) {
    _arity = ar;
    initialize (ar);
  }

  private void initialize(int ar) {
    _args = new ActionArgs (ar);
  }

/*
  protected void addListeners () {
   	// add listeners
    for (int i = 0; i < _arity; i++) {
     	Object next = _args.getArg(i);
        if (next instanceof Frame) {
            if (next instanceof Cls) {
             	Cls nextCls = (Cls)next;
            	nextCls.removeClsListener (PromptTab.getClsListener());
            	nextCls.addClsListener (PromptTab.getClsListener());
            } else {
         		Frame nextFrame = (Frame)next;
            	// we don't know if it is already attached
            	nextFrame.removeFrameListener (PromptTab.getFrameListener());
           		nextFrame.addFrameListener (PromptTab.getFrameListener()); 
            }
        }
    }
  }
*/
  public int getPriority () { return  _priority; }

  public void setPriority (int newPriority) {
  	_priority = newPriority;
  }

  public String getType () { return _name;  }

  public ActionArgs getArgs () { return _args;}

  public void setArgs (ActionArgs args) {
   	_args = args;
  }

  public void setArgs (Object [] args) {
  	_args.setArgs (args);
  }

  public ActionArgs getShortArgs () { return _args;}

  public boolean addToFrameActionsMap () {
    Object nextArg;
    for (int i = 0; i < _arity; i++) {
      nextArg = _args.getArg(i);
      if (nextArg instanceof Frame) {
        Mappings.addToFrameActionsMap((Frame)nextArg, this);
      }
    }
    return true;
  }

  public boolean removeFromFrameActionsMap () {
    Frame nextArg;
    for (int i = 0; i < _arity; i++) {
      if (_args.getArg(i) instanceof Frame) {
	      nextArg = (Frame)_args.getArg(i);
    	  Mappings.removeFromFrameActionsMap(nextArg, this);
      }
    }
    return true;
  }

  public void replaceFrameReference (Frame from, Frame to) {
     for (int i = 0; i < _arity; i++) {
       if (from.equals(_args.getArg(i))) {
         Mappings.removeFromFrameActionsMap (from, this);
         _args.setArg(i, to);
         Mappings.addToFrameActionsMap(to, this);
       }
     }
   }

/*
   public void updateArgument (Object oldArg, Object newArg) {
     for (int i = 0; i < _arity; i++) {
       if (_args[i] == oldArg)
         _args[i] = newArg;
     }
   }
*/
  public int hashCode () {
    int code = _name.hashCode();
    for (int i = 0; i < _arity; i++) {
    	if (_args.getArg(i) != null)
       		code += _args.getArg(i).hashCode();
    }
    return code;
  }

  public boolean equals (Object o) {
    if (o instanceof Action) {
       Action a = (Action)o;
       if (_name.equals(a._name) && // this implies that arity is equal, too
       	   _args.equals (a._args))
        	return true;
    }
    return false;
  }

  public  String getPrettyName () {
    return _prettyName == null ? _name : _prettyName; }

  public  String getShortName () {
    return _shortName == null ? getPrettyName() : _shortName; }

  public  String getConnectorString () {
    return _connectorString == null ? "" : _connectorString; }

  public  String getTrailingString () {
    return _trailingString == null ? "" : _trailingString; }

  public  String getConnectorString2 () {
    return (_args.size() >= 3 && _args.getArg(2) != null && _connectorString2 != null) ?
    				_connectorString2 : ""; }

  public void setFromUser (boolean b) {_fromUser = b; }
  public boolean getFromUser () {return _fromUser; }
}
