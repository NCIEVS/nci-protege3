 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;


public class LogEntry {
    private Date logEntryDate;
    //private String methodName;
    private String user;
    private ConceptEntry concept;
    //private EDITACTION action;
    //private String reference;


    public ConceptEntry getConcept() {
        return concept;
    }

    public void setConcept(ConceptEntry concept) {
        this.concept = concept;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public LogEntry(Date entrytDate, String userName, String conceptName) {
        logEntryDate = entrytDate;
        conceptName = conceptName.split(",")[0];
        conceptName = conceptName.split("\\)")[0];
        ConceptEntry ent = new ConceptEntry(conceptName);
        if(! (ent.getConceptCls() instanceof Cls))
        {
            return;
        }
        if(PromptTab.kbInOWL () && OWLUtil.isOWLAnonymousClassFrame(ent.getConceptCls()))
        {
            return;
        }
        if(PromptTab.kbInOWL () && OWLUtil.isOWLSystemFrame(ent.getConceptCls()))
        {
            return;
        }
       
        setUser(userName);
        
        setConcept(ent);
        //this.action = action;
        //this.methodName = methodName;
        if (!ProtegeLogging.getConceptInfoList().containsKey(ent.getConceptName())) {
            ProtegeLogging.getConceptInfoList().put(ent.getConceptName(), new ConceptInformation(ent, user, entrytDate, this));
        } else {
            ProtegeLogging.editConceptInformation(ent, user, entrytDate,this);
        }
        if (!ProtegeLogging.getUserInfoList().containsKey(user)) {
        		ProtegeLogging.getUserInfoList().put(user, new UserInformation(ent, user, entrytDate, this));
        } else {
            ProtegeLogging.editUserInformation(ent, user, entrytDate, this);
        }

        if (!ProtegeLogging.getAllUserList().contains(getUser())) {
            ProtegeLogging.getAllUserList().add(getUser());
        }
    }

    public Date getLogEntryDate() {
        return logEntryDate;
    }

    public void setLogEntryDate(Date logEntryDate) {
        this.logEntryDate = logEntryDate;
    }

    /*public EDITACTION getAction() {
        return action;
    }*/

    /*public void setAction(EDITACTION action) {
        this.action = action;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
*/

}
