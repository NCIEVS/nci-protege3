package gov.nih.nci.protegex.batch;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.protegex.edit.NCIEditFilter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.StringUtil;

import java.util.Vector;
import java.util.logging.Level;

/**
 * @Author: Bob Dionne
 */

public class BatchLoadByNameTask extends BatchTask {	

	NCIEditTab tab = null;

	OWLModel owlModel;

	OWLWrapper wrapper = null;

	public BatchLoadByNameTask(NCIEditTab tab, String infile,
			String outfile) {
		super(tab.getBatchProcessPanel());
		this.tab = tab;
		this.infile = infile;
		this.outfile = outfile;
		this.wrapper = tab.getWrapper();
		this.owlModel = tab.getOWLModel();
		data_vec = getData(infile);
		setMax(data_vec.size());
		setMessage("Batch Load processing in progress, please wait ...");
	}

	public static long create_time = 0;
	public static long evs_time = 0;
	public boolean processTask(int taskId) {
		try {
			String s = (String) data_vec.elementAt(taskId);
			Vector<String> w = getTokenStr(s, 3);
			
			String name = (String) w.elementAt(0);
			String pt = StringUtil.cleanString((String) w.elementAt(1), false);
			String sup = (String) w.elementAt(2);

			if (owlModel == null) {
				System.out.println("WARNING: owlModel is null...");
				return false;
			}
			if (super.checkNoErrors(w, taskId)) {
				//ok
			} else {
				return false;
			}
			
			
			
			
			
			

			// owlModel.beginTransaction("BatchLoad. Creating " + name);
			super.print("Creating " + name);

			if (!NCIEditFilter.checkXMLNCNameCompliance(name)) {
				super.print("\t" + name + " is invalid, unable to create. \n");
				return false;

			} else {
				long beg = System.currentTimeMillis();
				OWLNamedClass cls = wrapper.createCls(name, pt, sup);
				create_time += System.currentTimeMillis() - beg;

				if (cls != null) {
					beg = System.currentTimeMillis();
				    tab.recordHistory(NCIEditTab.EVSHistoryAction.CREATE, cls, "");
				    evs_time += System.currentTimeMillis() - beg;
					super.print("\t" + name + " created. \n");
				} else {
					super.print("\t" + "Unable to create class " + name + "\n");
					return false;
				}
			}

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
			return false;
		}

		return true;
	}

	
	/*
	 * Olfactory_Cistern_sub_1 Olfactory_Cistern_pt_1 Olfactory_Cistern
	 * Olfactory_Cistern_sub_2 Olfactory_Cistern_pt_2 Olfactory_Cistern
	 * Olfactory_Cistern_sub_3 Olfactory_Cistern_pt_3 Olfactory_Cistern
	 * Olfactory_Cistern_sub_4 Olfactory_Cistern_pt_4 Olfactory_Cistern
	 * Olfactory_Cistern_sub_5 Olfactory_Cistern_pt_5 Olfactory_Cistern
	 * Olfactory_Cistern_sub_6 Olfactory_Cistern_pt_6 Olfactory_Cistern
	 * Olfactory_Cistern_sub_7 Olfactory_Cistern_pt_7 Olfactory_Cistern
	 * Olfactory_Cistern_sub_8 Olfactory_Cistern_pt_8 Olfactory_Cistern
	 */

	public Vector<String> validateData(Vector<String> v) {
		Vector<String> w = new Vector<String>();
		try {
			
				
				String name = (String) v.elementAt(0);
				String sup = (String) v.elementAt(2);

				if (!NCIEditFilter.checkXMLNCNameCompliance(name)) {
					String error_msg = " -- concept name " + name + " is invalid.";
					w.add(error_msg);
					System.out.println(error_msg);
				}

				if (owlModel.getRDFSNamedClass(name) != null) {
					String error_msg = " -- concept " + name + " already exists.";
					w.add(error_msg);
					System.out.println(error_msg);
				}

				if (owlModel.getRDFSNamedClass(sup) == null) {
					String error_msg = " -- superconcept does not exist.";
					w.add(error_msg);
					System.out.println(error_msg);
				} else if (wrapper.isRetired(wrapper.getOWLNamedClass(sup))) {
	                String error_msg = " -- cannot edit retired concept";
	                w.add(error_msg);
	                System.out.println(error_msg);
	            }
				
				
			

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
			return null;
		}

		return w;

	}

	


}
