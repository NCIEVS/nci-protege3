 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
  *                 Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.PromptTab;


public class ProtegeLogging {
    private static final Logger log = Log.getLogger(ProtegeLogging.class);

    private static ArrayList debugList = new ArrayList();
    private static ArrayList LogFileEntries = new ArrayList();
    private static KnowledgeBase kbOLD = PromptTab.getPromptDiff().getKb1();
    private static KnowledgeBase kb = PromptTab.getPromptDiff().getKb2();
    private static HashSet allUserList = new HashSet();
    private static HashSet activeUserList = new HashSet();
    private static Hashtable ConceptInfoList = new Hashtable();
    private static Hashtable UserInfoList = new Hashtable();

    private static boolean loggingInitialized = false;




    public ProtegeLogging(URI logFileURI) {
        initializeDataStructures(logFileURI);
        AuthorManagement.setActiveUserList(AuthorManagement.getAllUsersWithChanges());
    }

    public static HashSet getAllUserList() {
        return allUserList;
    }

    public static void setAllUserList(HashSet allList) {
        allUserList = allList;
    }



    private void initializeDataStructures(URI logFileURI) {
        setKb(PromptTab.getPromptDiff().getKb2());
        setKbOLD(PromptTab.getPromptDiff().getKb1());

        ChangeManagement cm = ChangeManagement.getInstance();
        if(cm._changesProjectDefined){
            Collection changes = cm.getAllChanges();

            Iterator i = changes.iterator();
            while (i.hasNext()) {

                loggingInitialized = true;
                Instance nextChange = (Instance)i.next();

                String author = cm.getAuthor(nextChange);
                Date changeDate =  cm.getTimeStamp(nextChange);
                String applyTo = cm.getChangedClassName(nextChange);

                if(applyTo != null && !applyTo.equals(":STANDARD-CLASS")) {
					getLogFileEntries().add(new LogEntry(changeDate, author, applyTo));
				}
            }
        }
		/*
        final int MAXTOKS = 50;
             try {
                File logFile = new File(logFileURI);
            BufferedReader in;
            in = new BufferedReader(new FileReader(logFile));
            loggingInitialized = true;
            String line;
            String reference;
            String conceptName;
            String[] tokens = new String[MAXTOKS];
            int nTokens = 0;
            while ((line = in.readLine()) != null) {
                StringTokenizer strtok = new StringTokenizer(line);
                nTokens = 0;
                while (nTokens < MAXTOKS && strtok.hasMoreTokens()) {
                    	if (nTokens == 3)
                    		tokens[nTokens++] = strtok.nextToken("-");
                		else
                			tokens[nTokens++] = strtok.nextToken(" ");
                }
                if(nTokens < 4)
                {
                    continue;
                }
                Date dt = getDate(tokens[0], tokens[1]);
                String methodName = tokens[5].split("\\(")[0];
                if (methodName.equalsIgnoreCase("setdirectownslotvalues")) {
                    if (withinTransaction) continue;
                    String[] subArr = tokens[5].split("\\(");
                    String[] params = subArr[1].split(",");
                    String concept = params[0];
                    if (tokens[6].equalsIgnoreCase("Split_From,") && !tokens[7].equals("[]") && !tokens[7].equals("[])"))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.split, ""));
                    else if (tokens[6].equalsIgnoreCase("Merge_Into,") && !tokens[7].equals("[]") && !tokens[7].equals("[])"))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.merge, ""));
                    else if (!tokens[7].equals("[]") && !tokens[7].equals("[])"))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, ""));

                } else if (methodName.equalsIgnoreCase("setframename")) {
                    if (withinTransaction) continue;
                    String[] subArr = tokens[5].split("\\(");
                    String[] params = subArr[1].split(",");
                    String concept = params[0];
                    if (!tokens[6].equals(""))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, ""));
                    if (false) System.out.print(methodName);
                } else if (methodName.equalsIgnoreCase("createcls")) {
                    if (withinTransaction) continue;
                    String concept = tokens[6];
                    if (!tokens[6].equals(""))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, tokens[8]));
                    if (false) System.out.print(methodName);
                } else if (methodName.equalsIgnoreCase("adddirectsuperclass")) {
                    if (withinTransaction) continue;
                    String[] subArr = tokens[5].split("\\(");
                    String[] params = subArr[1].split(",");
                    String concept = params[0];
                    if (!tokens[6].equals(""))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, tokens[6]));
                    if (false) System.out.print(methodName);

                } else if (methodName.equalsIgnoreCase("removedirectsuperclass")) {
                    if (withinTransaction) continue;
                    String[] subArr = tokens[5].split("\\(");
                    String[] params = subArr[1].split(",");
                    String concept = params[0];
                    if (!tokens[6].equals(""))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, tokens[6]));
                    if (false) System.out.print(methodName);
                } else if (methodName.equalsIgnoreCase("adddirecttemplateslot")) {
                    if (withinTransaction) continue;
                    String[] subArr = tokens[5].split("\\(");
                    String[] params = subArr[1].split(",");
                    String concept = params[0];
                    if (!tokens[6].equals(""))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, tokens[6]));

                    if (false) System.out.print(methodName);

                } else if (methodName.equalsIgnoreCase("setdirecttemplatefacetvalues")) {
                    if (withinTransaction) continue;
                    if (false) System.out.print(methodName);
                } else if (methodName.equalsIgnoreCase("createslot")) {
                    if (withinTransaction) continue;
                    String concept = tokens[6];
                    if (!tokens[8].equals("[],"))
                        getLogFileEntries().add(new LogEntry(dt, tokens[3], concept, methodName, EDITACTION.modify, tokens[6]));
                    if (false) System.out.print(methodName);
                }
                nTokens = 0;
            }
            in.close();
//            printList();
             } catch (IOException ex) {
        		Log.getLogger().warning ("No journaling file");
        		return;
        }*/
    }

    //TODO: This is a debug method for test only...
    private void printList() {
        for (Iterator iterator = debugList.iterator(); iterator.hasNext();) {
            String logEnt = (String) iterator.next();
            if (false) {
				Log.getLogger().info(logEnt);
			}
        }
    }

    public static Hashtable getConceptInfoList() {
        return ConceptInfoList;
    }

    public static void editUserInformation(ConceptEntry concept, String user, Date editdate,LogEntry entry) {
        UserInformation userInfo = (UserInformation) getUserInfoList().get(user);
        userInfo.logEntries.add(entry);
        boolean found = false;
        for (Iterator iterator = userInfo.conceptList.iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            if (ent.getConceptHash() == concept.getConceptHash()) {
				found = true;
			}
        }
        if (!found) {
            userInfo.conceptList.add(concept);
        }
        /*if (EDITACTION.create == action) {
            userInfo.stats.setCreateCount(userInfo.stats.getCreateCount() + 1);
        } else if (EDITACTION.modify == action) {
            userInfo.stats.setModifyCount(userInfo.stats.getModifyCount() + 1);
        }
        if (EDITACTION.delete == action) {
            userInfo.stats.setDeleteCount(userInfo.stats.getDeleteCount() + 1);
        }*/
        if (editdate.before(userInfo.firstEditDate)) {
            userInfo.firstEditDate = editdate;
        }
        if (editdate.before(userInfo.lastEditDate)) {
            userInfo.lastEditDate = editdate;
        }
        // remove the old list entry and replace with a new one.
        getUserInfoList().remove(user);
        getUserInfoList().put(user, userInfo);
    }

    public static void editConceptInformation(ConceptEntry concept, String user,Date editdate,LogEntry entry) {
        ConceptInformation conceptInfo = (ConceptInformation) getConceptInfoList().get(concept.getConceptName());
        conceptInfo.getLogEntries().add(entry);
        if (conceptInfo.concept.getConceptHash() == concept.getConceptHash()) { //This is just a debug safety check.
            if (!conceptInfo.userList.contains(user)) {
                conceptInfo.userList.add(user);
                if (conceptInfo.userList.size() > 1) {
                    conceptInfo.setHasConflict(true);
                }
            }
            /*if (EDITACTION.create == action) {
                conceptInfo.stats.setCreateCount(conceptInfo.stats.getCreateCount() + 1);
            } else if (EDITACTION.modify == action) {
                conceptInfo.stats.setModifyCount(conceptInfo.stats.getModifyCount() + 1);
            }
            if (EDITACTION.delete == action) {
                conceptInfo.stats.setDeleteCount(conceptInfo.stats.getDeleteCount() + 1);
            }*/
            if (editdate.before(conceptInfo.firstEditDate)) {
                conceptInfo.firstEditDate = editdate;
            }
            if (editdate.before(conceptInfo.lastEditDate)) {
                conceptInfo.lastEditDate = editdate;
            }
        }
        // remove the old list entry and replace with a new one.
        getConceptInfoList().remove(concept.getConceptName());
//        getConceptInfoList().put(concept.getConceptName().toLowerCase(), conceptInfo);
        getConceptInfoList().put(concept.getConceptName(), conceptInfo);
    }

    public static void setConceptInfoList(Hashtable conceptInfoList) {
        ConceptInfoList = conceptInfoList;
    }

    public static KnowledgeBase getKb() {
        return kb;
    }

    public static void setKb(KnowledgeBase kb) {
        ProtegeLogging.kb = kb;
    }

    public static Hashtable getUserInfoList() {
        return UserInfoList;
    }

    public static void setUserInfoList(Hashtable userInfoList) {
        UserInfoList = userInfoList;
    }

    public static ArrayList getLogFileEntries() {
        return LogFileEntries;
    }

    public void setLogFileEntries(ArrayList logFileEntries) {
        LogFileEntries = logFileEntries;
    }

    public static HashSet getActiveUserList() {
        return activeUserList;
    }

    public static void setActiveUserList(HashSet activeUserList) {
        ProtegeLogging.activeUserList = activeUserList;
    }

    public boolean logFileExists () {
		return loggingInitialized;
    }

    public static KnowledgeBase getKbOLD() {
        return kbOLD;
    }

    public static void setKbOLD(KnowledgeBase kbOLD) {
        ProtegeLogging.kbOLD = kbOLD;
    }
}

