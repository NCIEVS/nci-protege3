 /*
  * Contributor(s): Kyle Bruck kbruck@cs.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import edu.stanford.smi.protege.model.*;
import java.util.*;

public class AlgorithmPluginTest implements AlgorithmPluginInterface
{
	private KnowledgeBase _source;
	private KnowledgeBase _target;
	
	AlgorithmPluginTest()
	{
			
	}
	
	public void setSource(KnowledgeBase source)
	{
		_source = source;
	}
	
	public void setTarget(KnowledgeBase target)
	{
		_target = target;
	}
	
	public void setSource(String source)
	{
		
	}
	
	public void setTarget(String source)
	{
		
	}
	
	public boolean useProtegeKBs()
	{
		return true;
	}
	
	public Collection getResults()
	{
		return null;
	}
	
	public Collection doAlignment()
	{
		return null;
	}
	
	public Collection doAlignment(Collection pairs)
	{
		return null;
	}
	
}
