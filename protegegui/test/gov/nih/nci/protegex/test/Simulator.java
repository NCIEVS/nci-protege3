/**
 * 
 */
package gov.nih.nci.protegex.test;

import java.io.FileInputStream;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * @author bitdiddle
 * 
 */
public class Simulator {

	Properties props = null;

	private final String NO_CLIENTS = "no_clients";

	private final String CONN = "conn";

	private final String WORK = "work";

	private final String INPUT = "input";

	private final String NO_EXE = "no_exe";

	private final String CLIENT = "client";

	ArrayList<ClientRunner> runners = new ArrayList<ClientRunner>();

	private void init() {
		int no_clients = Integer.parseInt(props.getProperty(NO_CLIENTS, "0"));
		for (int i = 1; i <= no_clients; i++) {
			String client = CLIENT + i;

			String classname = props.getProperty(client + "." + WORK, null);
			String infile = props.getProperty(client + "." + INPUT, null);
			String noexe = props.getProperty(client + "." + NO_EXE);
			String conn = props.getProperty(client + "." + CONN, null);
			if (conn != null) {
				OWLModel w = parseAndGetOWLModel(conn);
				if (w != null) {

					if (classname != null) {
						ClientWorker cw = null;
						try {
							Class c = Class.forName(classname);
							cw = (ClientWorker) c.newInstance();
							if (infile != null) {
								cw.init(w, infile);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						int noe = Integer.parseInt(noexe);
						
						runners.add(new ClientRunner(cw, noe));
						
					}
				}
			}

		}
	}

	private class ClientRunner implements Runnable {

		
		
		private ClientWorker worker = null;
		
		private int iterations = 0;

		ClientRunner(ClientWorker cw, int noe) {
			
			worker = cw;
			iterations = noe;
		}

		public void run() {
			
			long beg = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				worker.doWork(i);
			}
			
			System.out.println("Client took: " + (System.currentTimeMillis() - beg) + " ms");
			
			

			

		}
		
		public void cleanUp() {worker.cleanUp();}
	}

	
	private void sysout(String s) {
		System.out.println(s);
	}

	public void run() {
		ArrayList<Thread> ts = new ArrayList<Thread>();
		for (ClientRunner cr : runners) {
			ts.add(new Thread(cr));
		}
		for (Thread t : ts) {
			t.start();
			sysout("kicking off thread");
		}
		for (Thread t : ts) {
			try {
				sysout("waiting on thread to finish");
				t.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (ClientRunner cr : runners) {
			cr.cleanUp();
		}
	}

	private OWLModel parseAndGetOWLModel(String conn) {
		StringTokenizer tk = new StringTokenizer(conn, ",");
		if (tk.countTokens() != 4) {
			return null;
		} else {
			return getOWLModel(tk.nextToken(), tk.nextToken(),
					tk.nextToken(), tk.nextToken());
		}
	}

	// test checkin
	private OWLModel getOWLModel(String h, String u, String p, String pr) {
		String hostname = h;
		String username = u;
		String password = p;
		String projectName = pr;

		OWLModel model = null;
		Project project = null;

		try {
			RemoteServer server = (RemoteServer) Naming.lookup("//" + hostname
					+ "/" + Server.getBoundName());
			RemoteSession session = server.openSession(username,
					SystemUtilities.getMachineIpAddress(), password);
			RemoteServerProject serverProject = server.openProject(projectName,
					session);
			project = RemoteClientProject.createProject(server, serverProject,
					session, true);

			model = (OWLModel) project.getKnowledgeBase();

			

		} catch (Exception e) {
			Log.getLogger().severe(Log.toString(e));
		}

		return model;
	}

	// test
	public Simulator(String fname) {
		props = new Properties();
		try {
			props.load(new FileInputStream(fname));
		} catch (Exception e) {
			e.printStackTrace();
		}
		init();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Simulator sim = new Simulator(args[0]);		
		sim.run();
		// TODO Auto-generated method stub

	}

}
