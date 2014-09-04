/**
 * 
 */
package gov.nih.nci.protegex.workflow.report;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.SystemUtilities;
import gov.nih.nci.protegex.workflow.*;

import java.io.*;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.*;

/**
 * A general purpose class for querying the Workflow store and producing reports
 * and filtered views of the assignments. This class can be run via a shell script from the command line.
 * It can take several options, documented below, as well as an input file of options. For example
 * 
 * <p>
 * -local workflow
 * -user Kim Ong
 * -pass secret
 * -modeler Marvin Modeler
 * -status completed
 * -outputFile report1
 * </p>
 * 
 * More details are given in the design document include with these javadocs * 
 * 
 * @author Bob Dionne
 * 
 */
public class Reporter {

	/**
	 * if any args are faulty then this flag is set that prevents the Reporter
	 * from running
	 */
	private boolean okToRun = true;

	/**
	 * -local provides the name of the workflow store to access. Currently this defaults to "workflow". This
	 * os here for future use
	 */
	private final String localOp = "-local";
	private String localStore = null;

	/**
	 * -user is required to determine the permissions of the user, manager or modeler
	 */
	private final String userOp = "-user";
	private String user = null;

	/**
	 * the user's password, this is also required. Technically when running locally 
	 * this is not needed as only the user name is required for the Workflow object.
	 * The reason for this is that Workflow leverages the Protege login. When a workflow
	 * object is created the assumption is a valid use is already logged in.
	 */
	private final String passOp = "-pass";
	private String pass = null;

	/**
	 * when running in client server mode the host
	 * is the name of the machine to conect to
	 */
	private final String hostOp = "-host";
	private String host = null;

	/**
	 * project name to access, also only need in the remote case.
	 */
	private final String projectOp = "-project";
	private String project = null;

	/**
	 * -status provides a filter on the assignments, only those
	 * whose status matches the option value are included in the report
	 */
	private final String statusOp = "-status";
	private String status = null;
	
	/**
	 * which modeler is the report generated for. Currently there is only
	 * one value allowed, but conceivably one might want to include multiple
	 * modelers.
	 */
	private final String modelerOp = "-modeler";
	private String modeler = null;

	/**
	 * the name of an output file for the report. Each assignment is
	 * printed in one row as tab delimited values.
	 */
	private final String outFileOp = "-outputFile";
	private String outFileName = null;

	/**
	 * several command line options can be included in an input
	 * file and that can be passed to the Reporter instead. This
	 * simplifies usage and enables several configurations by using
	 * multiple files.
	 */
	private final String inputArgsFileOp = "-inputArgsFile";

	

	private final String tabKey = "\t";

	private String[] validOptions = { localOp, userOp, passOp, hostOp,
			projectOp, statusOp, modelerOp, inputArgsFileOp, outFileOp };

	ArrayList<Assignment> reports = null;

	/**
	 * Takes an array of command arguments and constructs a Reporter object
	 * 
	 * @param args
	 */
	public Reporter(String[] args) {

		parseArgs(args, true);

	}

	/**
	 * 
	 * @return false if the reporter cannot run
	 */
	public boolean isReady() {
		return okToRun;
	}

	/**
	 * At this point the args are parsed and all valid. Run collects Assignments
	 * based on the query criteria, filters them out, and produces the output,
	 * depending on which fields are selected
	 * 
	 */
	public void run() {
		if (localStore != null) {
			runLocally();
		} else {
			runRemotely();
		}
	}

	private void runLocally() {
		try {
			Workflow wf = Workflow.openWorkflow(user, null);
			reports = wf.getAllAssignments(wf.getUser(modeler));
			printReports();
		} catch (WorkflowException e) {
			e.printStackTrace();
		}
	}

	private void runRemotely() {

		try {
			RemoteServer server = (RemoteServer) Naming.lookup("//" + host
					+ "/" + Server.getBoundName());
			RemoteSession session = server.openSession(user, SystemUtilities
					.getMachineIpAddress(), pass);
			RemoteServerProject serverProject = server.openProject(project,
					session);
			Project p = RemoteClientProject.createProject(server,
					serverProject, session, true);

			Workflow wf = Workflow.openWorkflow(user, p.getKnowledgeBase());
			reports = wf.getAllAssignments(wf.getUser(modeler));
			printReports();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void printReports() {
		try {
			BufferedWriter br = null;

			if (outFileName != null) {
				br = new BufferedWriter(new FileWriter(outFileName));

			} else {
				br = new BufferedWriter(new OutputStreamWriter(System.out));
			}
			for (Assignment a : reports) {
				if (a.getCurrentStatus().getName()
						.equalsIgnoreCase(this.status)) {
					br.write(a.getIdentifier() + tabKey);
					br.write(a.getModeler() + tabKey);
					br.write(a.getDateCreated() + tabKey);
					br.write(a.getCurrentStatus() + tabKey);
					br.write(a.getLastUpdated() + tabKey);
					br.write(a.getTaskDescription() + tabKey);
					br.newLine();
				}
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ArrayList<Assignment> getAssignments() {
		return reports;
	}

	private void parseArgs(String[] args, boolean checkArgs) {

		if (args.length < 1) {
			showUsage();
		} else if (args[0].startsWith("-?")) {
			showUsage();
		} else {
			// args must come in pairs, easy to check
			if (args.length % 2 != 0) {
				showUsage();
			} else {
				for (int i = 0; i < args.length; i += 2) {
					parseArg(args[i], args[i + 1]);
				}
			}
		}

		if (checkArgs) {
			checkArgsValid();
		}

	}

	/**
	 * After all the args are parsed, check that together they are valid. For
	 * example if -local is used one can't use -user and -pass
	 * 
	 */
	private void checkArgsValid() {

		if (localStore == null) {
			// local store not specified, so we need user/pass/project
			int n = 0;
			if (user != null)
				n++;
			if (pass != null)
				n++;
			if (project != null)
				n++;

			if (n != 3) {
				okToRun = false;
			}
		} else {
			// still need user pass but not project
			int n = 0;
			if (user != null)
				n++;
			if (pass != null)
				n++;

			if (n != 2) {
				okToRun = false;

			}
		}

	}

	/**
	 * Check that option is valid
	 * 
	 * @param arg
	 *            option to check
	 * @return false if not valid
	 */
	private boolean checkValidOption(String arg) {

		//boolean ok = true;

		for (int i = 0; i < validOptions.length; i++) {

			if (validOptions[i].equalsIgnoreCase(arg)) {
				return true;
			}

		}
		return false;
	}

	private void parseArg(String arg, String value) {

		System.out.println("arg: " + arg + " val: " + value);
		if (!checkValidOption(arg)) {
			showUsage();
		}
		if (arg.equalsIgnoreCase(localOp)) {
			localStore = value;
		} else if (arg.equalsIgnoreCase(userOp)) {
			user = value;
		} else if (arg.equalsIgnoreCase(passOp)) {
			pass = value;
		} else if (arg.equalsIgnoreCase(projectOp)) {
			project = value;
		} else if (arg.equalsIgnoreCase(hostOp)) {
			host = value;
		} else if (arg.equalsIgnoreCase(statusOp)) {
			status = value;
		} else if (arg.equalsIgnoreCase(modelerOp)) {
			modeler = value;
		} else if (arg.equalsIgnoreCase(outFileOp)) {
			outFileName = value;
		} else if (arg.equalsIgnoreCase(inputArgsFileOp)) {
			parseArgs(readAndCrackArgs(value), false);
		}

	}

	private String[] readAndCrackArgs(String inArgsFileName) {
		ArrayList<String> args = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					inArgsFileName));
			String s;
			while ((s = br.readLine()) != null) {
				StringTokenizer tk = new StringTokenizer(s);
				args.add(tk.nextToken());
				String arg = tk.nextToken();
				if (tk.hasMoreTokens()) {
					StringBuffer sb = new StringBuffer();
					while (tk.hasMoreTokens()) {
						sb.append(" ");
						sb.append(tk.nextToken());
					}
					arg += sb.toString();
				}
				args.add(arg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return args.toArray(new String[args.size()]);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Reporter report = new Reporter(args);

		if (report.isReady()) {
			report.run();
		}

	}

	private void showUsage() {

		System.out.println("Usage: ");
		okToRun = false;

	}

}
