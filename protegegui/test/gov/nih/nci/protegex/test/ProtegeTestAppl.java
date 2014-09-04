package gov.nih.nci.protegex.test;

import java.rmi.Naming;

import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.model.*;

import edu.stanford.smi.protegex.owl.model.*;

import edu.stanford.smi.protege.util.SystemUtilities;

import java.util.*;

public abstract class ProtegeTestAppl {
    protected Project _project = null;
    protected OWLModel _model = null;
    protected SimplifiedOWLWrapper _wrapper = null;
    protected int _counter = 0;

    public ProtegeTestAppl(String hostname, String username, String password,
            String projectName) {
        if (! init(hostname, username, password, projectName))
            return;
        run();
    }
    
    private boolean init(String hostname, String username, String password,
            String projectName) {
        try {
            _project = connect(hostname, username, password, projectName);
            _model = (OWLModel) _project.getKnowledgeBase();
            _wrapper = SimplifiedOWLWrapper.createInstance(_model);
            return true;
        } catch (Exception e) {
            debug("Exception: " + e.getMessage());
            return false;
        }
    }

    protected void run() {
        debug("Do Nothing");
    }
    
    private Project connect(String hostname, String username, 
            String password, String projectName) throws Exception {
        RemoteServer server = (RemoteServer) Naming.lookup("//" + hostname + "/"
                + Server.getBoundName());
        RemoteSession session = server.openSession(username,
                SystemUtilities.getMachineIpAddress(), password);
        RemoteServerProject serverProject = server.openProject(
                projectName, session);
        Project project = RemoteClientProject.createProject(server,
                serverProject, session, true);
        return project;
    }
    
    protected Project getProject() {
        return _project;
    }
    
    protected void debug(String text) {
        System.out.println("Debug: " + text);
    }
    
    protected void deleteConcept(String conceptName) {
        try {
            OWLNamedClass cls = (OWLNamedClass) _wrapper.getCls(conceptName);
            _model.beginTransaction("Deleting class " 
                    + cls.getBrowserText(), cls.getName());
            cls.delete();
            _model.commitTransaction();
        } catch (Exception e) {
            _model.rollbackTransaction();
        }
    }
    
    protected void printConceptsFrom(String toplevelConcept) {
        OWLNamedClass cls = (OWLNamedClass) _wrapper.getCls(toplevelConcept);
        
        debug("");
        debug("Method: printConceptsFrom: " + toplevelConcept);
        _counter = 0;
        Collection subs = cls.getNamedSubclasses();
        recurseSubs(subs, 1, "");
        debug("subs size " + subs.size());      
    }
    
    private void printCounter() {
        _counter++;
        if (_counter % 100 == 0)
            debug("---------- ANOTHER 100 ----------" + _counter);
    }

    private void printCls(RDFSNamedClass ocls, String prefix) {
        boolean printMore = false;
        
        printCounter();
        if (printMore)
            debug(prefix + "----------");
        
        debug(prefix + ocls.getBrowserText());
        
        if (printMore) {
            if (ocls instanceof OWLNamedClass)
                debug(prefix + "An OWL class");
    
            Collection<? extends RDFProperty> props =
                (Collection<? extends RDFProperty>) ocls.getRDFProperties();
            debug(prefix + "---------- Props ----------");
            for (RDFProperty prop : props)
                debug(prefix + prop.getBrowserText());
            debug("");
        }
    }

    private void recurseSubs(Collection subs, int level, String indent) {
        String prefix = (new Integer(level)).toString() + ": " + indent;
        Iterator it = subs.iterator();
        while (it.hasNext()) {
            RDFSNamedClass ocls = (RDFSNamedClass) it.next();
            printCls(ocls, prefix);
        }

        ++level;
        Iterator it1 = subs.iterator();
        while (it1.hasNext()) {
            RDFSNamedClass ocls = (RDFSNamedClass) it1.next();
            recurseSubs(ocls.getNamedSubclasses(),
                level, indent + " ");
        }
        --level;
    }
}
