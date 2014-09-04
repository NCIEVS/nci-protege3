 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class RenameFrameOperation extends Operation {
  static final int RENAME_OPERATION_ARITY = 2;
  static final String SUFFIX = "1";
  protected boolean _fromListener = false;
  protected String _oldName = null;

//  static private GetValueWidget [] _argumentWidgets;


  public RenameFrameOperation () {
    super (RENAME_OPERATION_ARITY);
    initialize(null, null, null, false);
  }

  public RenameFrameOperation (Frame f, String newName) {
    super (RENAME_OPERATION_ARITY);
    initialize (f, newName, null, false);
  }

  public RenameFrameOperation (Frame f, String newName, String oldName) {
    super (RENAME_OPERATION_ARITY);
    initialize (f, newName, oldName, false);
  }

  public RenameFrameOperation (Frame f, String newName, String oldName, boolean fromListener) {
    super (RENAME_OPERATION_ARITY);
    initialize (f, newName, oldName, fromListener);
  }

  public RenameFrameOperation (Frame f, String newName, String oldName, Explanation exp) {
    super (RENAME_OPERATION_ARITY, exp);
    initialize(f, newName, oldName, false);
  }

  public void initialize (Frame f, String newName, String oldName, boolean fromListener) {
    _name = "rename-frame";
    _prettyName = "rename frame";
    _shortName = "rename";
    _connectorString = "to";
    _args.setArg (0, f);
    _args.setArg (1, newName);
    _oldName = oldName;
    _newFrame = f;
    _fromListener = fromListener;
  }

  public void actualOperation (){
    Frame f = (Frame)_args.getArg(0);

    if (!_fromListener)
       f.rename((String)_args.getArg(1));
//	ReplaceReferences.replaceFrameNameReferences (f, _oldName);

    SuggestionsAndConflicts.addSuggestions (findCandidatesForMerging (f));
//    _currentSuggestedOperations.addAll(findCandidatesForMerging (f));

  }

  protected Collection findCandidatesForMerging (Frame f)  {
	  KnowledgeBase kb =  ProjectsAndKnowledgeBases.getOtherSourceKnowledgeBase (f.getKnowledgeBase());
	  if (kb == null) return null;
	  
	  String name = Mappings.getRealName (f);
	  Class classOfF = f.getClass();
	  Collection result = new ArrayList();
	  
	  Collection allClses = kb.getClses();
	  Iterator i = allClses.iterator();
	  while (i.hasNext()) {
		  Cls next = (Cls)i.next();
		  if (next != f && next.getClass() == classOfF) {
			  int comparisonResult = CompareNames.compareNames(Mappings.getRealName(next), name);
			  if (comparisonResult == CompareNames.EQUAL ||
					  (Preferences.approximateMatch() && comparisonResult == CompareNames.APPROXIMATE_MATCH))
				  if (Mappings.getWhatBecameOfIt(next) == null &&
						  !DummyFrame.isDummyFrame(next))
					  result.add (MergeFramesOperation.createOperation (next, f, comparisonResult));
		  }
	  }
	  return result;
  }

  public static RenameFrameOperation selectRenameOperation (Frame f, Explanation exp) {
      RenameFrameOperation result;
      if (f instanceof Cls)
          result = new RenameClsOperation (f, f.getName(), f.getName() + SUFFIX);
      else if (f instanceof Slot)
          result = new RenameSlotOperation (f, f.getName(), f.getName() + SUFFIX);
      else
         result = new RenameFrameOperation (f, f.getName(), f.getName() + SUFFIX);
      return result;
  }

  public int hashCode () {
    return _name.hashCode() + _args.getArg(0).hashCode();
  }

  public boolean equals (Object o) {
    if (o instanceof RenameFrameOperation) {
       RenameFrameOperation a = (RenameFrameOperation)o;
       if (_name.equals(a._name) && // this implies that arity is equal, too
       	  _args.getArg(0).equals (a._args.getArg(0)))  {
        	return true;
        }
    }
    return false;
  }


/*

  static public JPanel createActionBox () {
    return Operation.createActionBox (_argumentWidgets);
  }

  public void collectData () {
    super.collectData (_argumentWidgets);
  }
*/
}
