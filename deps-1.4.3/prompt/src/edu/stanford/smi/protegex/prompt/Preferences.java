 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt;

import edu.stanford.smi.protege.model.KnowledgeBase;

public class Preferences {
  static public String PROMPT_IGNORED_SYSTEM_SLOTS = "edu.stanford.smi.protegex.prompt.ignored.slots"; 
  
  static boolean _considerInheritedSlots = true;
  static boolean _discardNonPreferredSlots = false;
  static boolean _caseSensitiveConflicts = true;
  static boolean _approximateMatch = true;
  static boolean _pluginMatch = true;
  
  static KnowledgeBase _preferred = null;

  static public boolean considerInheritedSlots () {return _considerInheritedSlots; }

  static public void considerInheritedSlots (boolean b) {
    _considerInheritedSlots = b;
  }
  
  static public boolean pluginMatch () {return _pluginMatch; }

  static public void pluginMatch (boolean b) {
	  _pluginMatch = b;
  }

  static public boolean approximateMatch () {return _approximateMatch; }

  static public void approximateMatch (boolean b) {
    _approximateMatch = b;
  }

  static public boolean caseSensitiveConflicts () {return _caseSensitiveConflicts; }

  static public void caseSensitiveConflicts (boolean b) {
    _caseSensitiveConflicts = b;
  }

  static public KnowledgeBase preferredOntology () {return _preferred ; }

  static public void preferredOntology (boolean b) {
    if (!b) _preferred = null;
  }

  static public void setPreferredOntology (KnowledgeBase preferred) {
    _preferred = preferred;
  }

  static public boolean discardNonPreferredSlots () {return _discardNonPreferredSlots;}

  static public void discardNonPreferredSlots (boolean b) {
   	_discardNonPreferredSlots = b;
  }

}
