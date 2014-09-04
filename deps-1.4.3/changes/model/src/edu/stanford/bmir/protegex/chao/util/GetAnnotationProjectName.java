package edu.stanford.bmir.protegex.chao.util;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.util.ProtegeJob;

public class GetAnnotationProjectName extends ProtegeJob {
    public static final String METAPROJECT_ANNOTATION_PROJECT_SLOT="annotationProject";
   
    
    public GetAnnotationProjectName(KnowledgeBase kb) {
        super(kb);
    }

    @Override
    public String run() throws ProtegeException {    	
        ProjectInstance annotationProject = getMetaProjectInstance().getAnnotationProject();
		return annotationProject == null ? null : annotationProject.getName();
    }

    public String execute() {
        return (String) super.execute();
    }
    
}
