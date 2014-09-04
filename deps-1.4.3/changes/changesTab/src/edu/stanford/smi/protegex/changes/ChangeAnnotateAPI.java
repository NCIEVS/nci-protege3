package edu.stanford.smi.protegex.changes;

import java.util.Collection;

import edu.stanford.smi.protege.model.Instance;

public interface ChangeAnnotateAPI {

	public Instance createChange(String changeClsName, String author, String apply, String action, String created, String desc, String typ);
	
	public Instance createNameChange(String changeClsName, String author, String apply, String action, String created, String desc, String typ, String oldName, String newName);
	
	public Instance createAnnotation(Collection changes, String title, String author, String created );
	
	public String getAuthor(Instance aInst);
	
	public String getTimeCreated(Instance aInst);
	
	public String getApplyTo(Instance aInst);
	
	public String getDescription(Instance aInst);
	
	public String getAction(Instance aInst);
	
}
