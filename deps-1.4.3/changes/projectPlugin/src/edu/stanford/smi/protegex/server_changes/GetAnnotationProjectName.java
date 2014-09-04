package edu.stanford.smi.protegex.server_changes;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.ProtegeJob;

public class GetAnnotationProjectName extends ProtegeJob {
    public static final String METAPROJECT_ANNOTATION_PROJECT_SLOT="annotationProject";
   
    
    public GetAnnotationProjectName(KnowledgeBase kb) {
        super(kb);
    }

    @Override
    public Object run() throws ProtegeException {
        Server server = Server.getInstance();
        KnowledgeBase metaProject = server.getMetaProject();
        SimpleInstance myProjectInstance = getMyProjectInstance(server);
        Slot annotationProjectSlot = metaProject.getSlot(METAPROJECT_ANNOTATION_PROJECT_SLOT);
        
        if (annotationProjectSlot == null) {
        	return null;
        }
        
        Object value = myProjectInstance.getDirectOwnSlotValue(annotationProjectSlot);
        return ((SimpleInstance) value).getDirectOwnSlotValue(server.getNameSlot());
    }
    
    private SimpleInstance getMyProjectInstance(Server server) {
        Project myProject = getKnowledgeBase().getProject();
        for (Object o : server.getProjectCls().getInstances()) {
            if (o instanceof SimpleInstance) {
                SimpleInstance projectInstance = (SimpleInstance) o;
                Object name = projectInstance.getDirectOwnSlotValue(server.getNameSlot());
                if (name instanceof String &&
                        myProject.equals(server.getProject((String) name))) {
                    return projectInstance;
                }
            }
        }
        throw new IllegalStateException("Sorry - the caller doesn't seem to have a project!");
    }
    
}
