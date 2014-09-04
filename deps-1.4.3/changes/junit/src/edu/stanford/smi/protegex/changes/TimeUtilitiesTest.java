package edu.stanford.smi.protegex.changes;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.util.interval.TimeIntervalCalculator;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Transaction;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.server_changes.ChangesProject;

public class TimeUtilitiesTest extends TestCase {
    private OWLModel model;
    
    private OWLNamedClass a;
    private OWLNamedClass b;
    private OWLNamedClass c;
    private Date first;
    private Date second;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        JunitUtilities.buildScratchProject();
        List errors = new ArrayList();
        Project p = new Project(JunitUtilities.SCRATCH_PROJECT, errors);
        assertTrue(errors.isEmpty());
        model = (OWLModel) p.getKnowledgeBase();
        ChangesProject.initialize(p);
    }
    
    public void testServer1() throws Exception {
        JunitUtilities.startServer();
        model = JunitUtilities.connectToServer();
        makeChanges();
        TimeIntervalCalculator t = TimeIntervalCalculator.get(ChAOKbManager.getChAOKb(model));
        JunitUtilities.flushChanges(model);
        checkChanges(t);
        t.dispose();
        model.getProject().dispose();
    }

    public void testServer2() throws Exception {
        JunitUtilities.startServer();
        model = JunitUtilities.connectToServer();
        KnowledgeBase changesKb = ChAOKbManager.getChAOKb(model);
        TimeIntervalCalculator t = TimeIntervalCalculator.get(changesKb);
        makeChanges();
        JunitUtilities.flushChanges(model);
        checkChanges(t);
        t.dispose();
        model.getProject().dispose();
    }

    public void testBeforeAndAfter1() throws InterruptedException, RemoteException {
        makeChanges();
        TimeIntervalCalculator t = TimeIntervalCalculator.get(ChAOKbManager.getChAOKb(model));
        checkChanges(t);
        t.dispose();
        model.getProject().dispose();
    }
    
    public void testBeforeAndAfter2() throws InterruptedException, RemoteException {
        TimeIntervalCalculator t = TimeIntervalCalculator.get(ChAOKbManager.getChAOKb(model));
        makeChanges();
        checkChanges(t);
        t.dispose();
        model.getProject().dispose();
    }
    
    public void testAddedCompositeChangeStandalone() throws Exception {
        TimeIntervalCalculator t = TimeIntervalCalculator.get(ChAOKbManager.getChAOKb(model));
        new Transaction<Boolean>(model, "Create class and add definition") {
            
            @Override
            public boolean doOperations() {
                OWLNamedClass a = model.createOWLNamedClass("A");
                OWLNamedClass b = model.createOWLNamedClass("B");
                OWLObjectProperty p = model.createOWLObjectProperty("p");
                model.createOWLSomeValuesFrom(p, b);
                return Boolean.TRUE;
            }
        }.execute();
        for (Change change : t.getTopLevelChanges()) {
            assertTrue(change.getPartOfCompositeChange() == null);
        }
        t.dispose();
        model.getProject().dispose();
    }
    
    public void testAddedCompositeChangeServer() throws IOException, NotBoundException  {
        JunitUtilities.startServer();
        model = JunitUtilities.connectToServer();
        KnowledgeBase changesKb = ChAOKbManager.getChAOKb(model);
        TimeIntervalCalculator t = TimeIntervalCalculator.get(changesKb);
        new Transaction<Boolean>(model, "Create class and add definition") {
            
            @Override
            public boolean doOperations() {
                OWLNamedClass a = model.createOWLNamedClass("A");
                OWLNamedClass b = model.createOWLNamedClass("B");
                OWLObjectProperty p = model.createOWLObjectProperty("p");
                model.createOWLSomeValuesFrom(p, b);
                return Boolean.TRUE;
            }
        }.execute();
        JunitUtilities.flushChanges(model);
        assertTrue(!t.getTopLevelChanges().isEmpty());
        for (Change change : t.getTopLevelChanges()) {
            assertTrue(change.getPartOfCompositeChange() == null);
        }
        t.dispose();
        model.getProject().dispose();
    }
    
    private void makeChanges() throws InterruptedException {
        a = model.createOWLNamedClass("A");

        Thread.sleep(1000);
        first = new Date();
        Thread.sleep(1000);

        b = model.createOWLNamedClass("B");

        Thread.sleep(1000);
        second = new Date();
        Thread.sleep(1000);

        c = model.createOWLNamedClass("C");
    }
    
    private void checkChanges(TimeIntervalCalculator t) throws RemoteException {
        boolean foundA;
        boolean foundB;
        boolean foundC;

        
        Collection<Change> changes = t.getTopLevelChanges(first, second);
        for (Change change : changes) {
            assertTrue(change.getApplyTo().getCurrentName().equals(b.getName()));
        }
        
        foundB = foundC = false;
        changes = t.getTopLevelChangesAfter(first);
        for (Change change : changes) {
            if (change.getApplyTo().getCurrentName().equals(b.getName()) && !foundC) {
                foundB = true;
            }
            else if (change.getApplyTo().getCurrentName().equals(c.getName())) {
                foundC = true;
            }
            else {
                fail();
            }
        }
        assertTrue(foundB && foundC);
        
        foundA = foundB = false;
        changes = t.getTopLevelChangesBefore(second);
        for (Change change : changes) {
            if (change.getApplyTo().getCurrentName().equals(a.getName()) && !foundB) {
                foundA = true;
            }
            else if (change.getApplyTo().getCurrentName().equals(b.getName())) {
                foundB = true;
            }
            else {
                fail();
            }
        }
        assertTrue(foundA && foundB);
    }

}
