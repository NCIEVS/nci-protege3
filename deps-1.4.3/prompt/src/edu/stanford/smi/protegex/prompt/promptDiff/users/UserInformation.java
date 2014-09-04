 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


public class UserInformation {
    String user;
    Date firstEditDate;
    Date lastEditDate;
    Collection logEntries = new ArrayList();
    Collection conceptList = new ArrayList();
  //  ChangeStats stats = new ChangeStats();

    UserInformation(ConceptEntry concept, String user, Date editdate, LogEntry ent) {
     	logEntries.add(ent);
        this.user = user;
        conceptList.add(concept);
        firstEditDate = editdate;
        lastEditDate = editdate;
    /*    if (EDITACTION.create == action) {
            stats.setCreateCount(stats.getCreateCount() + 1);
        } else if (EDITACTION.modify == action) {
            stats.setModifyCount(stats.getModifyCount() + 1);
        } else if (EDITACTION.delete == action) {
            stats.setDeleteCount(stats.getDeleteCount() + 1);
        }
*/    }
}
