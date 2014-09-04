 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;

import edu.stanford.smi.protege.model.Cls;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;


public interface ProtegeLoggingInterface {

    // These users have altered one of more concepts creating a conflict
    public Collection getDependentUserList(String userName);

    public void removeConceptFromChangeList(String conceptName);

    public void removeUserFromChangeList(String userName);

    public Collection getActiveUserList();

    public void addUserToActiveUserList(String userEnt);

    public void removeUserFromActiveUserList(String userEnt);

    public void clearActiveUserList();

    public void setActiveUserList(HashSet activeUserList);

    public Collection getChangesFromUsersInActiveList();

    public Collection getChangedConceptsFromUser(String userName);

    public Collection getChangedConceptsFromUserWithConflicts(String userName);

    public Collection getChangedConceptsFromUserWithOutConflicts(String userName);

    public Collection getChangedConceptsWithConflicts();

    public Collection getChangedConceptsWithOutConflicts();

    public Collection getAllChangedConcepts();

    public Collection getAllUsersWithChanges();

    public Collection getChangesByUserBetweenTimePeriods(String username, Date tsStart, Date tsEnd);

    public Collection getChangesToConceptByUser(String username, String conceptName);

    public LogEntry getLastChangeByUser(String username);

    public ChangeStats getUserStatistics(String username);

    public LogEntry getLastConceptChange(String conceptName);

    public Collection getConceptChangesBetweenTimePeriods(String conceptName, Date tsStart, Date tsEnd);

    public ChangeStats getConceptChangeStatistics(String conceptName);

    public Collection getAllChangesBetweenTimePeriods(Date tsStart, Date tsEnd);

    public int getChangeCountInTimePeriod(Date tsStart, Date tsEnd);

    public Collection getUserListForConcept(String conceptName);

    public Collection getUserListForConcept(Cls conceptCls);
}
