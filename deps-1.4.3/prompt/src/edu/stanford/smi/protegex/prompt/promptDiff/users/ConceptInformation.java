 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;


public class ConceptInformation {
    ConceptEntry concept;
    private boolean hasConflict = false;
    Date firstEditDate;
    Date lastEditDate;
    private Collection logEntries = new ArrayList();
    Collection userList = new HashSet();
    //ChangeStats stats = new ChangeStats();

    ConceptInformation(ConceptEntry concept, String user, Date editdate, LogEntry ent) {
        getLogEntries().add(ent);
        this.concept = concept;
        userList.add(user);
        /*if (EDITACTION.create == action) {
            stats.setCreateCount(stats.getCreateCount() + 1);
        } else if (EDITACTION.modify == action) {
            stats.setModifyCount(stats.getModifyCount() + 1);
        } else if (EDITACTION.delete == action) {
            stats.setDeleteCount(stats.getDeleteCount() + 1);
        }*/
        firstEditDate = editdate;
        lastEditDate = editdate;
        setHasConflict(false);
    }

    public boolean HasConflict() {
        return hasConflict;
    }

    public void setHasConflict(boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public Collection getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(Collection logEntries) {
        this.logEntries = logEntries;
    }
}
