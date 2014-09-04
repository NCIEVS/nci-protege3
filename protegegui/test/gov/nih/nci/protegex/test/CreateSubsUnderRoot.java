/**
 * 
 */
package gov.nih.nci.protegex.test;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.Config;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Bob Dionne
 * 
 */
public class CreateSubsUnderRoot implements ClientWorker {

	private OWLWrapper w = null;

	private String cname = null;

	private OWLNamedClass root = null;

	/*
	 *
	 */
	public void doWork(int i) {

		String sub_name = cname + "sub_" + i;
		String sub_pref_name = sub_name.replaceAll("_", " ");

		try {
			w.createCls(sub_name,sub_pref_name, root, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void cleanUp() {
		System.out.println("deleting concept");
		if (root != null) {
			root.delete();
		}
	}

	/*
	 * 
	 */
	public void init(OWLModel model, String fname) {
		w = OWLWrapper.createInstance(model);
		w.setConfig(new Config("ncitab_byName.xml", w));
		try {
			BufferedReader fr = new BufferedReader(new FileReader(fname));
			cname = fr.readLine().trim();
			root = (OWLNamedClass) w.createCls(cname, cname, "Concept1");
			if (root != null) {
				
			} else {
				System.out.println("root class not created, this client will horribly fail!!!!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
