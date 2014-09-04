/*
 * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
*/

package edu.stanford.smi.protegex.prompt.promptDiff.users;

import edu.stanford.smi.protege.model.Cls;

public class ConceptEntry implements Comparable {
    private String conceptName;
    private int conceptHash;
    private Cls conceptCls;

    ConceptEntry(String conceptName) {
        setConceptName(conceptName);
        setConceptHash(conceptName.hashCode());
        //todo: Will this always work? what is a class was newly created/deleted will KB2 always have the class
        Cls cl = null;
        /*if (action == EDITACTION.delete) {
            if (ProtegeLogging.getKbOLD() != null)
                cl = ProtegeLogging.getKbOLD().getCls(conceptName);
        } else {
            if (ProtegeLogging.getKb() != null)
                cl = ProtegeLogging.getKb().getCls(conceptName);           
        }*/
        if (ProtegeLogging.getKbOLD() != null)
            cl = ProtegeLogging.getKbOLD().getCls(conceptName);
        if (cl == null)
        	if (ProtegeLogging.getKb() != null)
                cl = ProtegeLogging.getKb().getCls(conceptName);           
        setConceptCls(cl);
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public int getConceptHash() {
        return conceptHash;
    }

    public void setConceptHash(int conceptHash) {
        this.conceptHash = conceptHash;
    }

    public Cls getConceptCls() {
        return conceptCls;
    }

    public void setConceptCls(Cls conceptCls) {
        this.conceptCls = conceptCls;
    }

    public int compareTo(Object o) {
        if (((ConceptEntry) o).getConceptHash() == conceptHash)
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        else
            return -1;
    }
}
