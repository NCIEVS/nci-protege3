 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.actionLists;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class ActionArgs {
	private Object [] _args;
    private int _arity;

    static private HashSet _clsesWithListeners = new HashSet ();
    static private HashSet _framesWithListeners = new HashSet ();

    public ActionArgs (int ar) {
     	_args = new Object [ar];
        _arity = ar;
    }

    public void setArg (int index, Object value) {
     	if (index >= _arity)
     		Log.getLogger().severe ("Index out of bounds: " + index);
        else  {
        	_args[index] = value;
      		if (value instanceof Cls &&
            	ProjectsAndKnowledgeBases.getTargetKnowledgeBase().equals(((Cls)value).getKnowledgeBase ()) )  {
                addClsListener ((Cls) value);
            }
        	else if (value instanceof Frame &&
            		 ProjectsAndKnowledgeBases.getTargetKnowledgeBase().equals(((Frame)value).getKnowledgeBase ()) ) {
				addFrameListener ((Frame)value);
            }

        }
    }

    private void addClsListener (Cls cls) {
     	if (_clsesWithListeners.contains(cls))
        	return;
        cls.addClsListener (PromptTab.getClsListener());
        _clsesWithListeners.add(cls);
    }

    private void addFrameListener (Frame f) {
     	if (_framesWithListeners.contains(f))
        	return;
        f.addFrameListener (PromptTab.getFrameListener ());
        _framesWithListeners.add(f);
    }

    public Object getArg (int index) {
     	if (index >= _arity) {
             return null;
        }
        else
            return _args[index];
    }

    public boolean equals (Object a) {
     	return Arrays.asList(_args).containsAll (Arrays.asList (((ActionArgs)a)._args));
    }

    public int size () {
     	return _args.length;
    }

    public Object [] toArray () {
     	Object [] clone = new Object [_arity];
        for (int i = 0; i < _args.length; i++)
        	clone[i] = _args[i];
        return clone;
    }
    
    public Collection toCollection () {
    	Collection clone = new ArrayList();
    	for (int i = 0; i < _args.length; i++)
    		clone.add (_args[i]);
    	return clone;
    }

    public void setArgs (Object [] args) {
     	_args = args;
    }
    
    public String toString () {
    	String result = "";
    	for (int i = 0; i < _args.length; i++)
    		result += _args[i];
      	return result;
    }
}
