package com.clarkparsia.protege.reasoner;

import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogRecord;
import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogRecordFactory;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.protegereasoner.AbstractReasonerTask;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protege.util.Log;

/**
 * Title: A default implementation of a ReasonerTask.<br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Sep 7, 2007 11:32:46 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class DefaultReasonerTask extends AbstractReasonerTask {
    private int mTaskSize;

    private ReasonerLogRecord mParentRecord;

    public DefaultReasonerTask(ProtegeReasoner theReasoner, int theTaskSize, String theRecordName) {
        super(theReasoner);

        mTaskSize = theTaskSize;
        
        if (theRecordName != null) {
            mParentRecord = ReasonerLogRecordFactory.getInstance().createInformationMessageLogRecord(theRecordName, null);
        
            postLogRecord(mParentRecord);
        }
    }

    public ReasonerLogRecord getParentRecord() {
        return mParentRecord;
    }

    public int getTaskSize() {
        return mTaskSize;
    }

    protected void incrementProgress() {
        super.setProgress(getProgress() + 1);
    }

    @Override
    public void setProgressIndeterminate(boolean theBool) {
        super.setProgressIndeterminate(theBool);
    }

    @Override
    protected void setDescription(String theDescription) {
        super.setDescription(theDescription);
    }

    @Override
    protected void setMessage(String theMessage) {
        super.setMessage(theMessage);
    }

    public void run() throws ProtegeReasonerException {
        // no-op - its retarded to have a task for each thing we need to do, we dont need to needlessly proliferate classes
    }

    @Override
    public void setRequestAbort() {
        super.setRequestAbort();
    }

    protected void taskFailed() {
        setTaskFailed();
    }

    protected void taskCompleted() {
        if (getProgress() < getTaskSize()) {
            setProgress(getTaskSize());
        }

        setDescription("Status");
        setMessage("Task Complete");

        setTaskCompleted();
    }
}
