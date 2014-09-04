 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
  * 					Natasha Noy, noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;

import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
  

public class AuthorManagement {   //implements ProtegeLoggingInterface

	private static ProtegeLogging pl;
	
	public static boolean startAuthorManagement (Project currentProject) {
		URI journalURI = currentProject.getJournalURI(); 
    		pl = new ProtegeLogging (journalURI);
    		
    		return pl.logFileExists ();
	}
	
    public static Collection getDependentUserList(String userName)
    {
      HashSet set = new HashSet();
      UserInformation info =  (UserInformation)ProtegeLogging.getUserInfoList().get(userName);
       if(info!=null)
       {
          Collection conceptCol = info.conceptList;
          for (Iterator iterator = conceptCol.iterator(); iterator.hasNext();) {
           ConceptEntry ent = (ConceptEntry) iterator.next();
           ConceptInformation conInfo = (ConceptInformation)ProtegeLogging.getConceptInfoList().get(ent.getConceptName());
           if(conInfo == null )
           {
               continue;
           }
            set.addAll(conInfo.userList);
          }

       }
       set.remove(userName);
       return set;
    }

    public static void  removeConceptFromChangeList(Object[] conceptArr)
    {
       for (int i=0; i<conceptArr.length;i++) {
          String conceptName = ((Cls)conceptArr[i]).getName();
          removeConceptFromChangeList(conceptName);
       }
    }

    public static void removeConceptFromChangeList(String conceptName) {
        Hashtable clone = new Hashtable();
        ProtegeLogging.getConceptInfoList().remove(conceptName);
        for (Iterator iterator = ProtegeLogging.getUserInfoList().values().iterator(); iterator.hasNext();) {
            UserInformation ent = (UserInformation) iterator.next();
            Collection cloneSub = new ArrayList(ent.conceptList);
            for (Iterator subIter = cloneSub.iterator(); subIter.hasNext();) {
                ConceptEntry concept = (ConceptEntry) subIter.next();
                if (concept.getConceptHash() == conceptName.hashCode()) {
                    ent.conceptList.remove(concept);
                }
            }
            clone.put(ent.user, ent);
        }
        ProtegeLogging.setUserInfoList(clone);
    }

    public static void  removeUserFromChangeList(Object[] userArr)
    {
       for (int i=0; i<userArr.length;i++) {
          String conceptName = (String)userArr[i];
          removeUserFromChangeList(conceptName);
       }
    }

    public static void removeUserFromChangeList(String userName) {
        Hashtable clone = new Hashtable();
        ProtegeLogging.getUserInfoList().remove(userName);

        for (Iterator iterator = ProtegeLogging.getConceptInfoList().values().iterator(); iterator.hasNext();) {
            ConceptInformation ent = (ConceptInformation) iterator.next();
            Collection cloneSub = new ArrayList(ent.userList);
            for (Iterator subIter = cloneSub.iterator(); subIter.hasNext();) {
                String user = (String) subIter.next();
                if (user.hashCode() == userName.hashCode()) {
                    ent.userList.remove(user);
                }
            }
            clone.put(ent.concept.getConceptName(), ent);
        }
        ProtegeLogging.setConceptInfoList(clone);
    }

    public static Collection getActiveUserList() {
        return ProtegeLogging.getActiveUserList();
    }

    public static void addUserToActiveUserList(String userEnt) {
        if (!ProtegeLogging.getActiveUserList().contains(userEnt))
            ProtegeLogging.getActiveUserList().add(userEnt);
    }

    public static void removeUserFromActiveUserList(String userEnt) {
        if (ProtegeLogging.getActiveUserList().contains(userEnt)) {
            ProtegeLogging.getActiveUserList().remove(userEnt);
        }
    }

    public static void clearActiveUserList() {
        ProtegeLogging.getActiveUserList().clear();
    }

    public static void setActiveUserList(HashSet activeUserList) {
        ProtegeLogging.setActiveUserList(activeUserList);
    }

    //Returns a set to log entires corresponding to Altered classes.
    public static Collection getChangesFromUsersInActiveList() {
        Collection col = new ArrayList();
        for (Iterator iterator = ProtegeLogging.getUserInfoList().values().iterator(); iterator.hasNext();) {
            UserInformation ent = (UserInformation) iterator.next();
            if (ProtegeLogging.getActiveUserList().contains(ent.user)) {
                if (!col.contains(ent.conceptList)) {
                    col.addAll(ent.conceptList);
                }
            }
        }
        return col;
    }

    public static Collection getChangedConceptsFromUser(String userName) {
        UserInformation info = ((UserInformation) ProtegeLogging.getUserInfoList().get(userName));
        if (info != null) {
            return info.conceptList;
        } else {
            return new ArrayList();
        }

    }

    public static Collection getChangedConceptsFromUserWithConflicts(String userName) {
        Collection col = new ArrayList();
        for (Iterator iterator = ((UserInformation) ProtegeLogging.getUserInfoList().get(userName)).conceptList.iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(ent.getConceptName());
            if (info.HasConflict()) {
                col.add(ent.getConceptCls());
            }
        }
        return col;
    }

    public static Collection getChangedConceptsFromUserWithOutConflicts(String userName) {
        Collection col = new ArrayList();
        UserInformation userInfo = ((UserInformation) ProtegeLogging.getUserInfoList().get(userName));
        if (userInfo != null) {
            for (Iterator iterator = userInfo.conceptList.iterator(); iterator.hasNext();) {
                ConceptEntry ent = (ConceptEntry) iterator.next();
                ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(ent.getConceptName());
                if (!info.HasConflict()) {
                    col.add(ent.getConceptCls());
                }
            }
        }
        return col;
    }

    public static Collection getChangedConceptsWithConflicts() {
        Collection col = new ArrayList();
        for (Iterator iterator = ProtegeLogging.getConceptInfoList().values().iterator(); iterator.hasNext();) {
            ConceptInformation ent = (ConceptInformation) iterator.next();
            if (ent.HasConflict()) {
                col.add(ent.concept);
            }
        }
        return col;
    }

    public static Collection getChangedConceptsWithOutConflicts() {
        Collection col = new ArrayList();
        for (Iterator iterator = ProtegeLogging.getConceptInfoList().values().iterator(); iterator.hasNext();) {
            ConceptInformation ent = (ConceptInformation) iterator.next();
            if (!ent.HasConflict()) {
                col.add(ent.concept);
            }
        }
        return col;
    }

    public static Collection getAllChangedConcepts() {
        return ProtegeLogging.getConceptInfoList().values();
    }

    public static HashSet getAllUsersWithChanges() {
        return ProtegeLogging.getAllUserList();
    }

    /*
        returns a list fo LogEntry (ies)
    */
    public static Collection getChangesByUserBetweenTimePeriods(String username, Date tsStart, Date tsEnd) {
        ArrayList list = new ArrayList();
        UserInformation info = (UserInformation) ProtegeLogging.getUserInfoList().get(username);
        if (info != null) {
            for (Iterator iterator = info.logEntries.iterator(); iterator.hasNext();) {
                LogEntry logEnt = (LogEntry) iterator.next();
                Date eventDate = logEnt.getLogEntryDate();
                if (eventDate.before(tsEnd) && eventDate.after(tsStart)) {
                    list.add(logEnt);
                }
            }
        }
        return list;
    }

    /*
        Collection (of LogEntry) resultCol ? described above
        NOTE: Here the class name will be repeated in each of the entries of the collection.
        Usage Scenario:
        This method returns all changes made to specific concept by a user in an infinite time period.
        returns a list fo LogEntry (ies)
        */

    public static Collection getChangesToConceptByUser(String username, String conceptName) {
        ArrayList list = new ArrayList();
 //       ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(conceptName.toLowerCase());
        ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(conceptName);
        if (info != null) {
            for (Iterator iterator = info.getLogEntries().iterator(); iterator.hasNext();) {
                LogEntry logEnt = (LogEntry) iterator.next();
                if (logEnt.getUser().hashCode() == username.hashCode()) {
                    {
                        if (logEnt.getConcept().getConceptHash() == conceptName.hashCode()) {
                            list.add(logEnt);
                        }
                    }
                }
            }
        }
        return list;
    }


    public static LogEntry getLastChangeByUser(String username) {

        UserInformation info = (UserInformation) ProtegeLogging.getUserInfoList().get(username);
        if (info != null) {
            for (Iterator iterator = info.logEntries.iterator(); iterator.hasNext();) {

                LogEntry logEnt = (LogEntry) iterator.next();
                Date curDate = logEnt.getLogEntryDate();
                if (curDate == info.lastEditDate) {
                    return logEnt;
                }
            }
        }
        return null;
    }

    /*
        NOTE: A list with a set of name value pairs.
        The name will be one of the EDITACTION (defined above) and the value will be the integer frequency count from the log.
        Usage Scenario:
            The method will be used when Protégé is used in a distributed knowledge acquisition environment. The method will enable the reviewer to evaluate the contribution of a given user between two time periods.
    */

    /*
        Usage Scenario:
        This method allows the user to view the last change made to a specific concept.
    */

    public static LogEntry getLastConceptChange(String conceptName) {
        ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(conceptName);
 //       ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(conceptName.toLowerCase());
        if (info != null) {
            for (Iterator iterator = info.getLogEntries().iterator(); iterator.hasNext();) {
                LogEntry logEnt = (LogEntry) iterator.next();
                Date curDate = logEnt.getLogEntryDate();
                if (curDate == info.lastEditDate) {
                    return logEnt;
                }
            }
        }
        return null;
    }

    /*
        Output Params:
            Collection (of LogEntry) resultCol ? described above
        Usage Scenario:
            The method returns all changes made to a concept in a given time period. This will enable roll back if a roll back feature is implemented.
    */

    public static Collection getConceptChangesBetweenTimePeriods(String conceptName, Date tsStart, Date tsEnd) {
        ArrayList list = new ArrayList();
        ConceptInformation info = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(conceptName);
        if (info != null) {
            for (Iterator iterator = info.getLogEntries().iterator(); iterator.hasNext();) {
                LogEntry logEnt = (LogEntry) iterator.next();
                Date eventDate = logEnt.getLogEntryDate();
                if (eventDate.before(tsEnd) && eventDate.after(tsStart)) {
                    list.add(logEnt);
                }
            }
        }
        return list;
    }

/*
    NOTE: A list with a set of name value pairs. The name will be one of the EDITACTION (defined above) and the value will be the integer frequency count from the log.
    Usage Scenario:
    The method gives the user a summary report for how a concept has changed since creation. This is useful for example if a concept is modified several times over a short period of time it may need to be redefined.
*/


/*
    Output: Collection (of LogEntry)
    Usage Scenario:
    This method will return all concepts that were altered between two time periods. Basically this will be the complete list of all changes.
*/

    public static Collection getAllChangesBetweenTimePeriods(Date tsStart, Date tsEnd) {

        ArrayList list = new ArrayList();
        for (Iterator iterator = ProtegeLogging.getLogFileEntries().iterator(); iterator.hasNext();) {
            LogEntry logEnt = (LogEntry) iterator.next();
            Date eventDate = logEnt.getLogEntryDate();
            if (eventDate.before(tsEnd) && eventDate.after(tsStart)) {
                list.add(logEnt);
            }
        }
        return list;
    }
/*
    Usage Scenario:
    This method will return a count of all changes that occurred within a given period.
*/


    public static Collection getUserListForConcept(String conceptName) {
        ConceptInformation conceptInfo = (ConceptInformation) ProtegeLogging.getConceptInfoList().get(conceptName);
        if (conceptInfo != null) {
            return conceptInfo.userList;
        }
        return new ArrayList();
    }

    public Collection getUserListForConcept(Cls conceptCls){

            return getUserListForConcept(conceptCls.getName());
        }
 }