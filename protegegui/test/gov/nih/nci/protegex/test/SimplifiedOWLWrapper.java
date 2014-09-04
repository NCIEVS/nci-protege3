package gov.nih.nci.protegex.test;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

public class SimplifiedOWLWrapper {
    private static SimplifiedOWLWrapper wrapper = null;
    private OWLModel owlModel;
    
    private SimplifiedOWLWrapper(OWLModel kb) {
        owlModel = kb;
    }
    
    public static SimplifiedOWLWrapper createInstance(OWLModel m) {
        if (wrapper == null) {
            wrapper = new SimplifiedOWLWrapper(m);
        }
        return getInstance();
    }
    
    public static SimplifiedOWLWrapper getInstance() {
        return wrapper;
    }
    
    private void debug(String text) {
        System.out.println("Debug: " + text);
    }
    
    public Cls getCls(String name) {
        //Note: Simplified method.  Modified from OWLWrapper.
        return owlModel.getRDFSNamedClass(name);
    }
    
    private String assignName(Cls cls, String internalName) {
        //Note: Simplified method.  Modified from OWLWrapper.
        SystemFrames _systemFrames = owlModel.getSystemFrames();
        Slot name_slot = _systemFrames.getNameSlot();
        cls.setDirectOwnSlotValue(name_slot, internalName);
        return internalName;
    }

    public boolean addDirectSuperclass(RDFSClass hostClass, RDFSClass superCls) {
        //Note: Simplified method.  Modified from OWLWrapper.
        hostClass.addSuperclass(superCls);
        hostClass.removeSuperclass(owlModel.getOWLThingClass());
        return true;
    }
    
    public boolean addDirectSuperclass(Cls subCls, Cls supCls) {
        return addDirectSuperclass((RDFSClass) subCls, (RDFSClass) supCls);
    }
    
    public OWLNamedClass createCls(String name, String pt, String supClsName) {
        Cls supCls = getCls(supClsName);
        
        debug("");
        debug("Method: createCls");
        debug("  name=" + name + ", pt=" + pt + ", supCls=" + supCls.getName());

        try {
            owlModel.beginTransaction("Create class " + name, name);
            OWLNamedClass cls = owlModel.createOWLNamedClass(null);
            assignName(cls, name);
            addDirectSuperclass(cls, supCls);
            owlModel.commitTransaction();
            return cls;
        } catch (Exception e) {
            owlModel.rollbackTransaction();
            debug("  Exception(createCls): " + e.getMessage());
            return null;
        }
    }
}
