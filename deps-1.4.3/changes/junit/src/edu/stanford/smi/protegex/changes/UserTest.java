package edu.stanford.smi.protegex.changes;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Class;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.User;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultOntology_Class;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultUser;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class UserTest extends TestCase {
    private OWLModel model;
    private KnowledgeBase chaoKb;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        JunitUtilities.buildScratchProject();
        List errors = new ArrayList();
        Project p = new Project(JunitUtilities.SCRATCH_PROJECT, errors);
        assertTrue(errors.isEmpty());
        model = (OWLModel) p.getKnowledgeBase();
        ChangesProject.initialize(p);
        chaoKb = ChAOKbManager.getChAOKb(model);

    }
    
    @Override
    protected void tearDown() throws Exception {
        model.dispose();
    }
    
    public void testUserGetCreate() {
        String userName = "Timothy Redmond";
        OntologyComponentFactory factory = new OntologyComponentFactory(chaoKb);
        assertNull(factory.getUser(userName));
        assertNull(factory.getUser(userName));
        factory.createUser(userName);
        assertNotNull(factory.getUser(userName));
    }
    
    public void testOntologyComponentRename() {
        String userName = "Timothy Redmond";
        
        OntologyComponentFactory factory = new OntologyComponentFactory(chaoKb);
        assertNull(factory.getUser(userName));
        User tr = factory.createUser(userName);

        OWLNamedClass cls1 = model.createOWLNamedClass("A");
        String originalName = cls1.getName();
        Ontology_Class changesCls = (Ontology_Class) ServerChangesUtil.getOntologyComponentFromChao(chaoKb, originalName);
        tr.addWatchedEntity(changesCls);
        assertTrue(tr.getWatchedEntity().size() == 1);
        assertTrue(tr.getWatchedEntity().contains(changesCls));
        assertTrue(changesCls.getWatchedBy().size() == 1);
        assertTrue(changesCls.getWatchedBy().contains(tr));
        
        cls1  = (OWLNamedClass) cls1.rename(originalName + "2");
        String newName = cls1.getName();
        assertTrue(changesCls.getCurrentName().equals(newName));
        assertNull(changesCls.getInitialName());
        assertTrue(tr.getWatchedEntity().size() == 1);
        assertTrue(tr.getWatchedEntity().contains(changesCls));
        assertTrue(changesCls.getWatchedBy().size() == 1);
        assertTrue(changesCls.getWatchedBy().contains(tr));
        
        Instance iUser = ((DefaultUser) tr).getWrappedProtegeInstance();
        Instance iCls  = ((DefaultOntology_Class) changesCls).getWrappedProtegeInstance();
        assertTrue(iUser.getDirectOwnSlotValues(factory.getWatchedEntitySlot()).contains(iCls));
        assertTrue(iCls.getDirectOwnSlotValues(factory.getWatchedBySlot()).contains(iUser));
    }
    
}
