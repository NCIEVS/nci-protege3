/**
 * 
 */
package gov.nih.nci.protegex.util;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

import java.rmi.Naming;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author bitdiddle
 *
 */
public class RoleInheritance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RoleInheritance ri = new RoleInheritance();
		ri.run();

	}
	
	private void run() {
	    String hostname = "192.168.2.100";
		String username = "Bob Dionne";
		String password = "bob";
		String projectName = "SmallBase";
		
		try {
			RemoteServer server = (RemoteServer) Naming.lookup("//" + hostname + "/"
					+ Server.getBoundName());
			RemoteSession session = server.openSession(username,
					SystemUtilities.getMachineIpAddress(), password);
			RemoteServerProject serverProject = server.openProject(
			        projectName, session);
			Project p = RemoteClientProject.createProject(server,
					serverProject, session, true);
			OWLModel model = (OWLModel) p.getKnowledgeBase();
			
			Collection allClses = model.getRDFSClasses();
			
			Iterator it = allClses.iterator();
			
			while (it.hasNext()) {
				RDFSClass rcl = (RDFSClass) it.next();
				System.out.println(rcl.getBrowserText());
			}
			
			
			//OWLWrapper wrapper = OWLWrapper.createInstance(model);
			
			
			
			//this.testFindEquivs(model, wrapper);
			//this.testGetDirectSupers(model, wrapper);
			
			//test_createAndDeleteConcept(model);
	        //test_printBCodes(model);
			//test_printConceptsFrom(model, "Anatomy_Kind");

		} catch (Exception e) {
			Log.getLogger().severe(Log.toString(e));
		}
	}

}
