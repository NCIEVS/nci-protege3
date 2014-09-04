/*
 * Contributor(s): Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt.plugin;

import java.util.Iterator;
import java.util.LinkedList;

import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.event.ProgressUpdateEvent;
import edu.stanford.smi.protegex.prompt.event.ProgressUpdateListener;

/**
 * This class is used to store the progress of an algorithm's execution.
 * Prompt plugins can use this to provide feedback to Prompt about their progress.
 * This progress is then displayed in the AlgorithmProgressDialog.
 * 
 * @author seanf
 */
public class AlgorithmProgressMonitor {
	/** The title of the progress monitor */
	private String progressTitle = "";
	
	/** The current progress of the algorithm */
	private String progressText = "";
	
	/** Indicates the algorithm completed */
	private boolean complete;
	
	/** List of listener objects that listen to update changes on this object */
	private LinkedList updateListeners = new LinkedList();
	
	public AlgorithmProgressMonitor() {}
	
	public synchronized void setProgressTitle(String progressTitle) {
		this.progressTitle = progressTitle;
		
		fireUpdateProgressEvent();
	}
	
	public synchronized String getProgressTitle() {
		return progressTitle;
	}
	
	public synchronized void setProgressText(String progressText) {
		if(!PromptTab.initializeSilently) {
			this.progressText = progressText;
			
			fireUpdateProgressEvent();
		}
	}
	
	public synchronized String getProgressText() {
		return progressText;
	}
	
	public synchronized void setCompleted(boolean complete) {
		this.complete = complete;
		
		fireUpdateProgressEvent();
	}
	
	public synchronized boolean isCompleted() {
		return complete;
	}
	
	public synchronized void reset() {
		progressText = "";
		progressTitle = "";
		complete = false;
	}
	
	public synchronized void addProgressUpdateListener(ProgressUpdateListener l) {
		updateListeners.add(l);
	}
	
	public synchronized void removeProgressUpdateListener(ProgressUpdateListener l) {
		updateListeners.remove(l);
	}
	
	private synchronized void fireUpdateProgressEvent() {
		ProgressUpdateEvent updateEvent = new ProgressUpdateEvent(this, this);
		for(Iterator iter = updateListeners.iterator(); iter.hasNext(); ) {
			((ProgressUpdateListener)iter.next()).progressUpdateReceived(updateEvent);
		}
	}
}


