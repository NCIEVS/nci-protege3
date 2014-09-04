/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Monica Crubezy crubezy@stanford.edu
 *                 Kyle Bruck kbruck@stanford.edy
 */
package edu.stanford.smi.protegex.prompt.mapping;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.vkbc.storage.MappingInterpreter;
import edu.stanford.smi.protegex.vkbc.storage.ProtegeKb;
import edu.stanford.smi.protegex.vkbc.storage.VKBCKb;

public class VKBCInterpreter {

	  private Project   _sourceProject;
	  private Project   _targetProject;
	  private Project _mappingsProject;
	  private VKBCKb                _vkbcMappingKB; //KB with instances of mapping relations
	  private VKBCKb                _vkbcSourceKBs[]; //domain KBs
	  private VKBCKb                _vkbcTargetKB;  //method KB (first with no instances, that will be fed by vkbcInterpreter
	  private MappingInterpreter    _vkbcInterpreter;


	  public VKBCInterpreter(Project sourceProject, Project targetProject, Project mappingsProject) {
	    _sourceProject = sourceProject;
	    _targetProject = targetProject;
	    _mappingsProject = mappingsProject;
	  }

	  public void initialize() {
	    Log.getLogger().info("VKBC --- Initializing Mapping Interpreter...");

	    Log.getLogger().info("VKBC --- Mapping KB = " + _mappingsProject.getProjectURI().getPath());
	    _vkbcMappingKB = (VKBCKb) new ProtegeKb(_mappingsProject); 
	    _vkbcSourceKBs = new VKBCKb[1];

	    Log.getLogger().info("VKBC --- Domain KB = " +_sourceProject.getProjectURI().getPath());
	    _vkbcSourceKBs[0] = (VKBCKb) new ProtegeKb(_sourceProject);  
	    
	    Log.getLogger().info("VKBC --- Method KB = " +_targetProject.getProjectURI().getPath());
	    _vkbcTargetKB = (VKBCKb) new ProtegeKb(_targetProject); 
			
	    //create MappingInterpreter
	    Log.getLogger().info("VKBC --- Creating Mapping Interpreter...");
	    _vkbcInterpreter = new MappingInterpreter(_vkbcMappingKB, _vkbcSourceKBs, _vkbcTargetKB);
	  }

	  public void invoke() {

	    initialize();

	    Log.getLogger().info("VKBC --- Calling Mapping Interpreter...");
	    _vkbcInterpreter.doAllMappings();
//	    _vkbcTargetKB.closeKb(true);
	    Log.getLogger().info("VKBC --- Done.");
	  }
}


