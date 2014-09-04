/*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
   *                 Kyle Bruck kbruck@stanford.edu
 */
package edu.stanford.smi.protegex.prompt.operation;

import java.util.Collection;
import edu.stanford.smi.protege.model.*;

public interface AlgorithmPluginInterface 
{
	//Initialize Mode:
	//UI Configs - Drop down in Initial screen.
	
	//Use Current Mappings Mode:
	//UI Configs - Possibly in Prompt Menu with submenu for multiple plugins
	
	//Let the User Specify Mode:
	//UI Configs - Possibly in Prompt Menu.
	
	
	public Collection getResults();
	public void setSource(KnowledgeBase source);
	public void setTarget(KnowledgeBase target);
	public void setSource(String source);
	public void setTarget(String target);
	public boolean useProtegeKBs();
	public Collection doAlignment();
	
	//Current Mapping Pairs in the Collection
	//Add
	public Collection doAlignment(Collection pairs);
}
