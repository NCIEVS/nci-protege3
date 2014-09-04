/**
 * 
 */
package gov.nih.nci.protegex.edit;

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
import edu.stanford.smi.protegex.owl.model.*;
import junit.framework.TestCase;

import gov.nih.nci.protegex.util.*;



/**
 * @author Screech Tester
 *
 */
public class TestOWLWrapper extends TestCase {
	
	private static OWLWrapper wrapper = null;
	private static Project project = null;
	
	private static void init() {
		String hostname = "localhost";
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
				project = RemoteClientProject.createProject(server,
						serverProject, session, true);
				
				assertTrue(project != null);
				OWLModel model = (OWLModel) project.getKnowledgeBase();
				
				assertTrue(model != null);
				
				wrapper = OWLWrapper.createInstance(model);
				
				new Config("ncitab.xml", wrapper);
				
				

			} catch (Exception e) {
				Log.getLogger().severe(Log.toString(e));
			}
	}
	
	protected void setUp() throws Exception {
		if (wrapper == null) {
			init();
		}
	}
	
	
	

	public void testInit() {
		assertTrue(wrapper != null);
		
	}
	
	public void testIsReadOnlyProperty() {
		assertTrue(wrapper.isReadOnlyProperty("code"));
		assertTrue(wrapper.isReadOnlyProperty("Merge_Source"));
		assertTrue(!wrapper.isReadOnlyProperty("DEFINITION"));
	}
	
	public void testCodeGen() {
		assertTrue(!wrapper.getCode().equalsIgnoreCase("NOCODE"));
	}
	
	public void testCreateRestriction() {
		
		try {
		OWLNamedClass foo = (OWLNamedClass) wrapper.createCls("foobarnew", true);
		
		
		assertTrue(wrapper.createOWLRestriction(foo, "some", 
				"rAnatomic_Structure_is_Physical_Part_of", 
				"SemConcept1"));
		
		wrapper.deleteOWLNamedClass(foo);
		
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	
	public void testTwoClients() {
		
		try {
			
			
			final OWLNamedClass t1c = (OWLNamedClass) wrapper.createCls("t1", true);
			final OWLNamedClass t2c = (OWLNamedClass) wrapper.createCls("t2", true);
			
			Thread t1 = new Thread() {
				public void run() {
					for (int i = 0; i < 10; i++) {
						wrapper.createCls("t1-" + i, "t1 " + i, t1c, "Another t1 sub");
					}
				}
			};
			
			Thread t2 = new Thread() {
				public void run() {
					for (int i = 0; i < 10; i++) {
						wrapper.createCls("t2-" + i, "t2 " + i, t2c, "Another t2 sub");
					}
				}
			};
			
			t1.start();
			t2.start();
			t1.join();
			t2.join();
			t1c.delete();
			t2c.delete();
			assertTrue(true);
			
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
	
	
	
}
