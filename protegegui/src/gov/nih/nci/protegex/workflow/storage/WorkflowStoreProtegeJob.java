/**
 * 
 */
package gov.nih.nci.protegex.workflow.storage;

import java.util.ArrayList;

import edu.stanford.smi.protege.model.KnowledgeBase;

import static gov.nih.nci.protegex.workflow.storage.WfJob.*;

/**
 * @author Bob Dionne
 * 
 */
public class WorkflowStoreProtegeJob implements WorkflowStore {

	private KnowledgeBase protegeKb = null;

	private WfJob job = null;

	public WorkflowStoreProtegeJob(KnowledgeBase kb) {

		protegeKb = kb;
		job = new WfJob();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.protegex.workflow.storage.WorkflowStore#findUser(java.lang.String)
	 */
	public boolean userExistsP(String userName) {

		job.setArgs(new String[] { userName });
		job.setTask(JobTask.USEREXISTS);

		Object res = null;

		if (protegeKb != null) {
			res = new RemoteWfJob(protegeKb, job).execute();
		} else {
			res = job.run();
		}
		String bool = (String) res;

		return Boolean.parseBoolean(bool);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.protegex.workflow.storage.WorkflowStore#userRole(java.lang.String)
	 */
	public int userRole(String userName) {

		job.setArgs(new String[] { userName });
		job.setTask(JobTask.USERROLE);

		Object res = execute();

		return ((Integer) res).intValue();

	}

	public String[] getAllUsers() {

	    job.setArgs(new String[] {});
		job.setTask(JobTask.ALLUSERS);
		
        ArrayList<String> list = (ArrayList<String>) execute();
        return list != null ? list.toArray(new String[list.size()]) : null;
	}

	public void storeAssignment(String userName, String assignment, int assid) {

		job.setArgs(new String[] { userName, assignment,
				(new Integer(assid)).toString() });
		job.setTask(JobTask.PUTASSIGN);

		execute();

		// TODO: should we return error condition?

	}
	
	public void archiveAssignment(String userName, String assignment, int assid) {

		job.setArgs(new String[] { userName, assignment,
				(new Integer(assid)).toString() });
		job.setTask(JobTask.ARCHIVEASSIGN);

		execute();

		

	}
	
	public void deleteAssignment(String userName, int assid) {
		
		job.setArgs(new String[] { userName, (new Integer(assid)).toString() });
		job.setTask(JobTask.DELASSIGN);

		execute();
		
	}

	public int getNextAssignmentId() {

		job.setArgs(new String[] {});
		job.setTask(JobTask.NEXTASSID);

		return ((Integer) execute()).intValue();

	}

    public int getNextPackageId() {

        job.setArgs(new String[] {});
        job.setTask(JobTask.NEXTPKGID);

        return ((Integer) execute()).intValue();

    }

    public int createUser(String userName, String role) {

		job.setArgs(new String[] { userName, role });
		job.setTask(JobTask.NEWUSER);

		return ((Integer) execute()).intValue();

	}

	public String[] getAllAssignments(String userName) {

		job.setArgs(new String[] { userName });
		job.setTask(JobTask.GETALLASSIGN);

        ArrayList<String> list = (ArrayList<String>) execute();
        return list != null ? list.toArray(new String[list.size()]) : null;
	}

	private Object execute() {

		if (protegeKb != null) {
			return new RemoteWfJob(protegeKb, job).execute();
		} else {
			return job.run();
		}

	}

}
