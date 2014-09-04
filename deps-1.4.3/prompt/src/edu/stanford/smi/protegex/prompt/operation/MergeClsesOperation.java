 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MergeClsesOperation extends MergeFramesOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [MERGE_OPERATION_ARITY];

  public MergeClsesOperation () {
    super ();
    initialize ();
  }

  public MergeClsesOperation (Frame f1, Frame f2) {
    super (f1, f2);
    initialize ();
  }

 // public MergeClsesOperation (Frame f1, Frame f2, boolean copyInstances, boolean copyTree, boolean deepCopy) {
	public MergeClsesOperation (Frame f1, Frame f2, TraversalDirective td, boolean deepCopy) {
	  super (f1, f2);
	  _traversalDirective = td;
	  _deepCopy = deepCopy;
	  initialize ();
	}


  public MergeClsesOperation (Frame f1, Frame f2, Explanation exp) {
    super (f1, f2, exp);
    initialize ();
  }

  private void initialize () {
		String mergeOrMap = Util.mergeOrMapString ();
	    _name = mergeOrMap + "-classes";
	    _prettyName = mergeOrMap + " classes";
	    _shortName = mergeOrMap;
  }

  public void actualOperation () {
    super.actualOperation();
  }
  
  protected void createMapping () {
	Collection sources = getSourcesForMappingKb ();
	if (sources == null || sources.size() < 2) return;
	Iterator i = sources.iterator();
	Cls source = (Cls)i.next();
	Cls target = (Cls)i.next();
	if (_mappingStoragePlugins == null) return;
	for (int m = 0; m < _mappingStoragePlugins.length; m++) {
		_mappingStoragePlugins[m].createClassToClassMapping (source, target);
	}
  }
  
  public void setArgs (Object[] args) {
   	_args.setArg(0, args[0]);
    _args.setArg(1, args[1]);
    _deepCopy = ((Boolean)args[2]).booleanValue();
    _traversalDirective.setCopySubclasses(((Boolean)args[3]).booleanValue());
	_traversalDirective.setCopyInstances(((Boolean)args[4]).booleanValue());
  }

/*
  static public void createValuesWidgetsImplementation () {
    _argumentWidgets[0] = new SelectClsWidget ("Ontology for the first class", 0);
    _argumentWidgets[1] = new SelectClsWidget ("Ontology for the second class", 1);

  }
*/
}
