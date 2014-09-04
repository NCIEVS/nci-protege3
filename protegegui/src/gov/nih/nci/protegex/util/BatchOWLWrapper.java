package gov.nih.nci.protegex.util;

import java.rmi.Naming;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import gov.nih.nci.protegex.edit.OWLWrapper;

/**
 * @author Bob Dionne
 * 
 */
public class BatchOWLWrapper {

	public BatchOWLWrapper() {
	    run();
	}

	private void run() {
	    String hostname = "localhost";
		String username = "Bob Dionne";
		String password = "bob";
		String projectName = "NCIThesaurus31";
		
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
			
			OWLWrapper wrapper = OWLWrapper.createInstance(model);
			
			test_simulateBatchLoad(wrapper);
			
			//this.testFindEquivs(model, wrapper);
			//this.testGetDirectSupers(model, wrapper);
			
			//test_createAndDeleteConcept(model);
	        //test_printBCodes(model);
			//test_printConceptsFrom(model, "Anatomy_Kind");

		} catch (Exception e) {
			Log.getLogger().severe(Log.toString(e));
		}
	}
	

	
	private void test_simulateBatchLoad(OWLWrapper wrapper) {
        //OWLWrapper wrapper = new OWLWrapper(model);
        //OWLNamedClass cls = wrapper.createCls("foo2", true);
        
        wrapper.createCls("baz", true); 
        for (int i = 0; i < 3; i++) {
        wrapper.createCls("baz-" + i, "baz" + i, "baz");
        }
        
       
	}
	

	

	


	public static void main(String[] args) {
		new BatchOWLWrapper();
	}
}
