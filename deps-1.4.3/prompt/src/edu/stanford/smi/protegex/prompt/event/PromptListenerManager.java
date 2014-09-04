/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.event;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.promptDiff.PromptDiff;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;

public class PromptListenerManager {
	private static transient Vector _listeners;

	synchronized public static void addDiffListener(PromptListener l) {
		  if (_listeners == null)
			_listeners = new Vector();
		  _listeners.addElement(l);
		}  

		synchronized public static void removeDiffListener(PromptListener l) {
		  if (_listeners == null)
			_listeners = new Vector();
		  _listeners.removeElement(l);
		}
		
		synchronized public static void addListener(PromptListener l) {
			  if (_listeners == null)
				_listeners = new Vector();
			  _listeners.addElement(l);
			}  

			synchronized public static void removeListener(PromptListener l) {
			  if (_listeners == null)
				_listeners = new Vector();
			  _listeners.removeElement(l);
			}
			
	public static void 	fireTableBuilt (DiffTableView view) {
		if (_listeners != null && !_listeners.isEmpty()) {
		  PromptEvent event = 
			new PromptEvent(PromptDiff.class);

		  Vector targets;
		  synchronized (PromptDiff.class) {
			targets = (Vector) _listeners.clone();
		  }

		  Enumeration e = targets.elements();
		  while (e.hasMoreElements()) {
		  	PromptListener l = (PromptListener) e.nextElement();
			l.diffTableViewBuilt(view, event);
		  }
		}		
	}

	public static void fireDiffUIBuilt (JTabbedPane mainPane) {
		if (_listeners != null && !_listeners.isEmpty()) {
		  PromptEvent event = 
			new PromptEvent(PromptDiff.class);

		  Vector targets;
		  synchronized (PromptDiff.class) {
			targets = (Vector) _listeners.clone();
		  }

		  Enumeration e = targets.elements();
		  while (e.hasMoreElements()) {
		  	PromptListener l = (PromptListener) e.nextElement();
			l.diffUIBuilt(mainPane, event);
		  }
		}		
	}
	
	public static void fireTaskComplete(boolean interrupted) {
		if (_listeners != null && !_listeners.isEmpty()) {
			PromptEvent event = new PromptEvent(PromptTab.class);

			Vector targets;
			synchronized (PromptTab.class) {
				targets = (Vector) _listeners.clone();
			}

			Enumeration e = targets.elements();
			while (e.hasMoreElements()) {
				PromptListener l = (PromptListener) e.nextElement();
				l.taskComplete(event, interrupted);
			}
		}
	}
	

	public static void fireUIBuilt (TabComponent mainPane) {
		if (_listeners != null && !_listeners.isEmpty()) {
		  PromptEvent event = 
			new PromptEvent(PromptDiff.class);

		  Vector targets;
		  synchronized (PromptDiff.class) {
			targets = (Vector) _listeners.clone();
		  }

		  Enumeration e = targets.elements();
		  while (e.hasMoreElements()) {
		  	PromptListener l = (PromptListener) e.nextElement();
			l.UIBuilt(mainPane, event);
		  }
		}		
	}

	public static void fireOperationStarted (Operation operation) {
		if (_listeners != null && !_listeners.isEmpty()) {
		  PromptEvent event = 
			new PromptEvent(PromptDiff.class);

		  Vector targets;
		  synchronized (PromptDiff.class) {
			targets = (Vector) _listeners.clone();
		  }

		  Enumeration e = targets.elements();
		  while (e.hasMoreElements()) {
		  	PromptListener l = (PromptListener) e.nextElement();
			l.operationStarted (operation, event);
		  }
		}		
	}

	public static void fireOperationCompleted (Operation operation) {
		if (_listeners != null && !_listeners.isEmpty()) {
		  PromptEvent event = 
			new PromptEvent(PromptDiff.class);

		  Vector targets;
		  synchronized (PromptDiff.class) {
			targets = (Vector) _listeners.clone();
		  }

		  Enumeration e = targets.elements();
		  while (e.hasMoreElements()) {
		  	PromptListener l = (PromptListener) e.nextElement();
			l.operationCompleted (operation, event);
		  }
		}		
	}

	public static void fireInitializationDone () {
		if (_listeners != null && !_listeners.isEmpty()) {
		  PromptEvent event = 
			new PromptEvent(PromptDiff.class);

		  Vector targets;
		  synchronized (PromptDiff.class) {
			targets = (Vector) _listeners.clone();
		  }

		  Enumeration e = targets.elements();
		  while (e.hasMoreElements()) {
		  	PromptListener l = (PromptListener) e.nextElement();
			l.initializationDone(event);
		  }
		}		
	}

	public static void fireDiffDone () {
	  if (_listeners != null && !_listeners.isEmpty()) {
		PromptEvent event = 
		  new PromptEvent(PromptDiff.class);

		Vector targets;
		synchronized (PromptDiff.class) {
		  targets = (Vector) _listeners.clone();
		}

		Enumeration e = targets.elements();
		while (e.hasMoreElements()) {
		  PromptListener l = (PromptListener) e.nextElement();
		  l.diffDone(event);
		}
	  }
	}
	

	public static void fireBeforeClose () {
		  if (_listeners != null && !_listeners.isEmpty()) {
			PromptEvent event = 
			  new PromptEvent(PromptDiff.class);

			Vector targets;
			synchronized (PromptDiff.class) {
			  targets = (Vector) _listeners.clone();
			}

			Enumeration e = targets.elements();
			while (e.hasMoreElements()) {
			  PromptListener l = (PromptListener) e.nextElement();
			  l.beforeClose(event);
			}
		  }
		}
}
