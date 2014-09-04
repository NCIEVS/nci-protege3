 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;


public class Statistics {
  static private PrintStream _log = null;
  static private int totalNumberOfSuggestions = 0;
  static private int numberOfSuggestionsFollowed = 0;
  static private int totalNumberOfConflictsDetected = 0;
  static private int numberOfConflictSolutionsUsed = 0;
  static private int numberOfKBOperations = -3;

  static public void openLogFile (String mergedFileName) {
 // 	Log.enter (Statistics.class, "openLogFile", mergedFileName);
	int index = mergedFileName.lastIndexOf('.');
    String logFileName = (index > 0) ? mergedFileName.substring (0, index) : mergedFileName;
    logFileName += ".mlog";
    try {
      URI uri =  new URI(logFileName);
      _log = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File (uri))));
      _log.println ("-------------------------START-------------------------");
    } catch (Exception e) {
      Log.getLogger().warning("Could not open log file");
    }
  }

  public static void addToLogStream (String str) {
  	if (_log == null) return;
   	_log.print(new Date().toString());
    _log.println(", " + str);
    _log.flush();
  }

  public static void increaseTotalNumberOfSuggestions () {
     totalNumberOfSuggestions++;
  }

  public static void increaseTotalNumberOfSuggestions (int n) {
     totalNumberOfSuggestions += n;
  }

  public static void increaseNumberOfSuggestionsFollowed () {
     numberOfSuggestionsFollowed++;
  }

  public static void increaseNumberOfSuggestionsFollowed (int n) {
     numberOfSuggestionsFollowed += n;
  }

  public static void increaseTotalNumberOfConflictsDetected () {
     totalNumberOfConflictsDetected++;
  }

  public static void increaseTotalNumberOfConflictsDetected (int n) {
     totalNumberOfConflictsDetected += n;
  }

  public static void increaseNumberOfConflictSolutionsUsed () {
     numberOfConflictSolutionsUsed++;
  }

  public static void increaseNumberOfConflictSolutionsUsed (int n) {
     numberOfConflictSolutionsUsed += n;
  }

  public static void increaseNumberOfKBOperations () {
     numberOfKBOperations++;
  }

  public static void increaseNumberOfKBOperations (int n) {
     numberOfKBOperations += n;
  }

  public static void printStatistics () {
    if (_log != null) {
      _log.println("Total number of generated suggestions: " + totalNumberOfSuggestions);
      _log.println("Number of generated suggestions that were followed by the user: " + numberOfSuggestionsFollowed);
      _log.println("Total number of conflicts detected: " + totalNumberOfConflictsDetected);
      _log.println("Number of conflict solutions used: " + numberOfConflictSolutionsUsed);
      _log.println("Total number of KB operations: " + numberOfKBOperations);
	  _log.flush();
    }
  }



}
