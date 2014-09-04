 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;

import java.util.Date;
import java.util.Iterator;

import edu.stanford.smi.protege.util.Log;

public class TestClass {
    public static void main(String[] argv) {
        
        Log.getLogger().info("Test1 starts\n\n");
        for (Iterator iterator = AuthorManagement.getAllChangedConcepts().iterator(); iterator.hasNext();) {
            ConceptInformation logEnt = (ConceptInformation) iterator.next();
            Log.getLogger().info(logEnt.concept.getConceptName());
        }
        Log.getLogger().info("\n\n\nTest2 starts\n\n\n");
        for (Iterator iterator = AuthorManagement.getChangedConceptsWithConflicts().iterator(); iterator.hasNext();) {
            ConceptEntry logEnt = (ConceptEntry) iterator.next();
            Log.getLogger().info(logEnt.getConceptName());
        }
        Log.getLogger().info("\n\n\nTest3 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangedConceptsWithOutConflicts().iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());
        }
        Log.getLogger().info("\n\n\nTest4 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangedConceptsFromUser("prashr").iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());
        }
        AuthorManagement.addUserToActiveUserList("prashanth");

        Log.getLogger().info("\n\n\nTest5 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangesFromUsersInActiveList().iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());

        }
        AuthorManagement.addUserToActiveUserList("prashr");
        Log.getLogger().info("\n\n\nTest6 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangesFromUsersInActiveList().iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());

        }
        AuthorManagement.removeUserFromActiveUserList("prashr");
        Log.getLogger().info("\n\n\nTest7 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangesFromUsersInActiveList().iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());

        }

        Log.getLogger().info("\n\n\nTest8 starts\n\n");
        AuthorManagement.removeConceptFromChangeList("moose");
        Log.getLogger().info("\n\n\nTest9 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangesFromUsersInActiveList().iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());

        }

        Log.getLogger().info("\n\n\nTest10 starts\n\n");
        //AuthorManagement.removeUserFromChangeList("prashanth");
        AuthorManagement.addUserToActiveUserList("prashr");

        Log.getLogger().info("\n\n\nTest11 starts\n\n");
        for (Iterator iterator = AuthorManagement.getChangesFromUsersInActiveList().iterator(); iterator.hasNext();) {
            ConceptEntry ent = (ConceptEntry) iterator.next();
            Log.getLogger().info(ent.getConceptName());

        }
        Log.getLogger().info("\n\n\nTest12 starts\n\n");
        for (Iterator iterator = AuthorManagement.getUserListForConcept("Degree_split").iterator(); iterator.hasNext();) {
            String ent = (String) iterator.next();
            Log.getLogger().info(ent);

        }
        Log.getLogger().info("\n\n\nTest13 starts\n\n");

        for (Iterator iterator = AuthorManagement.getAllChangesBetweenTimePeriods(new Date(0), new Date()).iterator(); iterator.hasNext();) {
            LogEntry ent = (LogEntry) iterator.next();
            Log.getLogger().info(ent.getConcept().getConceptName());

        }
        Log.getLogger().info("\n\n\nTest14 starts\n\n");

        for (Iterator iterator = AuthorManagement.getDependentUserList("prashanth").iterator(); iterator.hasNext();) {
            String ent = (String) iterator.next();
            Log.getLogger().info(ent);
        }

    }
}
