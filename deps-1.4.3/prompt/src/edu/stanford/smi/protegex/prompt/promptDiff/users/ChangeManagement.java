 /*
  * Contributor(s): Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;
   

import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;




public class ChangeManagement {
    private KnowledgeBase _changesKB = null;
    private Project _changesProject = null;
    public boolean _changesProjectDefined = false;
    private static ChangeManagement ChangeManagementObject = null;
    
    private Cls _changesCls = null;
    private Cls _classCreated =  null;
    private Cls _classDeleted = null;
    private Cls _classRenamed = null;
    private Slot _authorSlot = null;
    private Slot _createdSlot = null;
    private Slot _applyToSlot = null;
    private Slot _typeSlot = null;
	
    private static final String CHANGES_CLS_NAME = "Change";
    private static final String CLASS_CREATED = "Class_Created";
    private static final String CLASS_DELETED = "Class_Deleted";
    private static final String CLASS_RENAMED = "Name_Changed";
    private static final String APPLY_TO_SLOT_NAME = "applyTo";
    private static final String CREATED_SLOT_NAME = "created";
    private static final String AUTHOR_SLOT_NAME = "author";
    private static final String SLOT_NAME_TYPE = "type";
    
    private static final String ROOT_TYPE = "ROOT";
	
    private ChangeManagement() {
 	    changesProjectDefined();
            if (_changesProjectDefined) {
                _changesKB = _changesProject.getKnowledgeBase();
            }
            initializeModel();
    }
    
    private void initializeModel() {
        _changesCls = _changesKB.getCls(CHANGES_CLS_NAME);
        _classCreated = _changesKB.getCls(CLASS_CREATED);
        _classDeleted = _changesKB.getCls(CLASS_DELETED);
        _classRenamed = _changesKB.getCls(CLASS_RENAMED);
        _authorSlot = _changesKB.getSlot(AUTHOR_SLOT_NAME);
        _createdSlot = _changesKB.getSlot(CREATED_SLOT_NAME);
        _applyToSlot = _changesKB.getSlot(APPLY_TO_SLOT_NAME);
        _typeSlot = _changesKB.getSlot(SLOT_NAME_TYPE);
    }
    
    public static ChangeManagement getInstance(){
  	  if(ChangeManagementObject == null)
  		  ChangeManagementObject = new ChangeManagement();
  	  return ChangeManagementObject;
    }
    
	private void changesProjectDefined(){
        String projectName = ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb2()).getProjectName();
		
		String changeProjectName = "annotation_"+projectName;
		File f = new File(URI.create(ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb2()).getProjectDirectoryURI() + "/" + changeProjectName + ".pprj"));
		if(f.exists()){
			_changesProjectDefined = true;
			Collection errors = new ArrayList();
			_changesProject = Project.loadProjectFromURI(URI.create(ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb2()).getProjectDirectoryURI() + "/" + changeProjectName + ".pprj"),errors);
			
		}
		
	}
    
	public boolean doesChangesProjectExist(){
		return _changesProjectDefined;
	}
    
    public KnowledgeBase getChangesKb() {
        return _changesKB;
    }
    
    
	public Project getChangesProject() {
		return _changesProject;
	}
	
	@SuppressWarnings("unchecked")
    public Collection getAllChanges(){
            Collection changes = _changesCls.getInstances();
            Set<Instance> roots = new HashSet<Instance>();
            for (Object o : changes) {
                Instance change = (Instance) o;
                String type = (String) change.getOwnSlotValue(_typeSlot);
                if (type.equals(ROOT_TYPE)) roots.add(change);
            }
            changes.removeAll(roots);
            return changes;
        }
	
	public Collection getAllChangedClasses(){
		Collection changes = getAllChanges();
		Collection changedClasses = new ArrayList();
	
		Iterator i = changes.iterator();
		while (i.hasNext()) {
		
		    
			Instance nextChange = (Instance)i.next();
		    String clsName = getChangedClassName(nextChange);
		    if(!changedClasses.contains(clsName))
			 changedClasses.add(clsName);
		    
	}
		//changedClasses.remove(":STANDARD-CLASS");//temporary fix 
		return changedClasses;
	}
	
	public Collection getClassCreatedChanges(){
            Collection changes = _classCreated.getInstances();
            return changes;
	}
	
	public Collection getClassDeletedChanges(){
            Collection changes = _classDeleted.getInstances();
            return changes;
		
	}
	
	public Collection getClassRenamedChanges(){
            Collection changes = _classRenamed.getInstances();
            return changes;
		
	}
	
	public String getAuthor(Instance change){
            String author = (String)change.getOwnSlotValue (_authorSlot);
            return author;
	}
	
	public Date getTimeStamp(Instance change){
            String created = (String)change.getOwnSlotValue (_createdSlot);
            Date returnDate = null;
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
            try {
                returnDate = dateFormat.parse(created);
            } catch (ParseException e) {
                e.printStackTrace();  
            }
            return returnDate;
	}
	
	/* This returns the new class name if change instance is of type "Class_Renamed"  */
	public String getChangedClassName(Instance change){
            String applyTo = (String)change.getOwnSlotValue (_applyToSlot);
            return applyTo;
	}
	
	
	public String getOldClassName(Instance classRenameChange){
		Slot _oldClsName = _changesKB.getSlot("oldName");
		String oldName = (String)classRenameChange.getOwnSlotValue(_oldClsName);
		return oldName;
	}
	
	
	
	
}
