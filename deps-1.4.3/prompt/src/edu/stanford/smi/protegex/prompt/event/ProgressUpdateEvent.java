package edu.stanford.smi.protegex.prompt.event;

import java.util.EventObject;

import edu.stanford.smi.protegex.prompt.plugin.AlgorithmProgressMonitor;

public class ProgressUpdateEvent extends EventObject {
	private AlgorithmProgressMonitor progressMonitor;
	
	public ProgressUpdateEvent(Object source, AlgorithmProgressMonitor progressMonitor) {
		super(source);
		this.progressMonitor = progressMonitor;
	}
	
	public AlgorithmProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}
}
