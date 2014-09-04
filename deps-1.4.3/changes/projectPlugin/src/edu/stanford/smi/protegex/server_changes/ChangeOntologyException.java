package edu.stanford.smi.protegex.server_changes;

public class ChangeOntologyException extends Exception {
    
    public ChangeOntologyException() {}
    
    public ChangeOntologyException(String msg) {
        super(msg);
    }
    
    public ChangeOntologyException(String msg, Throwable t) {
        super(msg, t);
    }

}
