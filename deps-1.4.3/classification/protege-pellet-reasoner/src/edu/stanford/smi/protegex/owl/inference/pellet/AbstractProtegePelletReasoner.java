package edu.stanford.smi.protegex.owl.inference.pellet;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;

import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogRecordFactory;
import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogger;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTask;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTaskAdapter;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTaskEvent;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTaskListener;
import edu.stanford.smi.protegex.owl.inference.reasoner.AbstractProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.inference.util.TimeDifference;

public abstract class AbstractProtegePelletReasoner extends AbstractProtegeReasoner 
													implements ProtegePelletReasoner {

	/*
     * Executes the specified task after synchronizing the reasoner if necessary.
     * The task listener (if not <code>null</code>)  is automatically registered
     * with the task before task execution, and the unregistered after task
     * execution.
     *
     * @param task         The task to be executed.
     * @param taskListener The listener to be registered with the task.  May be
     *                     <code>null</code> if no listener should be registered.
     */
    public void performTask(ReasonerTask task,
                            ReasonerTaskListener taskListener) throws ProtegeReasonerException {
        TimeDifference td = new TimeDifference();

        td.markStart();

        final ReasonerTaskListener tskLsnr = taskListener;

        ReasonerTaskAdapter taskAdapter;

        if (taskListener != null) {
            taskAdapter = new ReasonerTaskAdapter() {
                // Don't override task completed


                public void addedToTask(ReasonerTaskEvent event) {
                    tskLsnr.addedToTask(event);
                }


                public void progressChanged(ReasonerTaskEvent event) {
                    tskLsnr.progressChanged(event);
                }


                public void progressIndeterminateChanged(ReasonerTaskEvent event) {
                    tskLsnr.progressIndeterminateChanged(event);
                }


                public void descriptionChanged(ReasonerTaskEvent event) {
                    tskLsnr.descriptionChanged(event);
                }


                public void messageChanged(ReasonerTaskEvent event) {
                    tskLsnr.messageChanged(event);
                }


                public void taskFailed(ReasonerTaskEvent event) {
                    tskLsnr.taskFailed(event);
                }
            };
        }
        else {
            taskAdapter = new ReasonerTaskAdapter();
        }

        synchronizeReasoner(taskAdapter);

        if (taskListener != null) {
            task.addTaskListener(taskListener);
        }

        try {
            task.run();
        }
        catch (ProtegeReasonerException e) {
            // Attempt to release the old model.  This
            // may result in exceptions, but we have
            // already flagged that the knowledgebase           
        	reset();
            synchronizeReasoner = true;

            ReasonerLogger.getInstance().postLogRecord(ReasonerLogRecordFactory.getInstance().createErrorMessageLogRecord(e.getMessage(), null));
            task.setRequestAbort();
            
            throw e;
        } catch (Exception e) {
        	if (e.getCause() instanceof InconsistentOntologyException) {
        		KnowledgeBase pelletKb = getPelletKB();
        		String explanation = pelletKb == null ? "" : "\nExplanation: " + pelletKb.getExplanation();
        		
        		reset();
        		synchronizeReasoner = true;
            	
            	task.setRequestAbort();
            	
            	throw new ProtegeReasonerException("Cannot do reasoning with inconsistent ontologies!" +
            			explanation, e);
        	}
        }
        finally {
            if (taskListener != null) {
                task.removeTaskListener(taskListener);
            }

        }

        td.markEnd();

        ReasonerLogger.getInstance().postLogRecord(ReasonerLogRecordFactory.getInstance().createInformationMessageLogRecord("Total time: " + td, null));
    }
	
}
