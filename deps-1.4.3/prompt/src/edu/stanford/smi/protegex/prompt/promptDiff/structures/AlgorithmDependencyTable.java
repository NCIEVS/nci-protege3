 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.promptDiff.DiffAlgorithm;

public class AlgorithmDependencyTable {
	private HashMap<Class, DependencyTableRecord> _dependencyTable; // a collection of DependencyTableRecords
    private Class[] _algorithms;
    private DiffAlgorithm[] _algorithmInstances;
	private static final Class [] _emptyArray = {};

    // input: an array of class names for algorithms
	public AlgorithmDependencyTable (String[] algorithmNames) {
    try {
    	_dependencyTable = new HashMap (algorithmNames.length);
    	_algorithms = new Class [algorithmNames.length];
    	_algorithmInstances = new DiffAlgorithm [algorithmNames.length];
		for (int i = 0; i < algorithmNames.length; i++) {
            _algorithms[i] = Class.forName (algorithmNames[i]);
            _algorithmInstances[i] = (DiffAlgorithm)_algorithms[i].getConstructor (_emptyArray).newInstance((Object[])_emptyArray);
            _dependencyTable.put (_algorithms[i], new DependencyTableRecord (_algorithms[i]));
        }

		for (int i = 0; i < _algorithms.length; i++) {
        	DependencyTableRecord nextRecord =_dependencyTable.get(_algorithms[i]);
            nextRecord.fillInRecord (_algorithms[i], _algorithmInstances[i]);
        }

    } catch (Exception e) {
    	Log.getLogger().severe("Exception occurred: " + e);
    }
    }

    public Collection<Class> getAlgorithmsThatDontAffectOtherAlgorithms () {
     	ArrayList<Class> result = new ArrayList<Class>();
    	Collection<DependencyTableRecord> allRecords = _dependencyTable.values();
        Iterator<DependencyTableRecord> i = allRecords.iterator();
		while (i.hasNext()) {
         	DependencyTableRecord nextRecord = i.next();
            if (!nextRecord.affectsOtherAlgorithms())
            	result.add (result.size(), nextRecord.getAlgorithm());
        }
        return result;
    }

    public Collection<Class> getAllAlgorithms () {
      Collection<Class> result = new ArrayList<Class>();
      for (int i = 0; i < _algorithms.length; i++) {
        result.add(_algorithms[i]);
      }
      return result;
    }

    public Collection getAlgorithmsThatArentAffectedByOtherAlgorithms () {
     	ArrayList result = new ArrayList();
    	Collection allRecords = _dependencyTable.values();
        Iterator i = allRecords.iterator();
	  while (i.hasNext()) {
            DependencyTableRecord nextRecord = (DependencyTableRecord)i.next();
            if (!nextRecord.isAffectedByOtherAlgorithms())
            	result.add(result.size(), nextRecord.getAlgorithm() );
        }
        return result;
    }

    public void printTable () {
    	Collection allRecords = _dependencyTable.values();
        Iterator i = allRecords.iterator();
        while (i.hasNext())
        	((DependencyTableRecord)i.next()).printRecord();

    }

    public void addEffect (Class affectedBy, Class affects) {
    	DependencyTableRecord record =(DependencyTableRecord)_dependencyTable.get(affects);
        record.addToAffectsList (affectedBy);
    }

    public Collection getAlgorithmsThatAreAffectedBy (Class algorithm) {
    	DependencyTableRecord record =(DependencyTableRecord)_dependencyTable.get(algorithm);
        return record.getAlgorithmsThatAreAffectedByMe();
    }

	public String toString () {
   		return "AlgorithmDependencyTable";
  	}

    private class DependencyTableRecord {
     	Class _algorithm;
        Set _affects;
        Set _isAffectedBy;

        DependencyTableRecord (Class algorithm) {
         	_algorithm = algorithm;
            _affects = new HashSet();
            _isAffectedBy = new HashSet();
        }

        public void fillInRecord (Class algorithm, DiffAlgorithm algorithmInstance) {
        try{
        	if (!_algorithm.equals(algorithm)) return;
            for (int i = 0; i < _algorithms.length; i++) {
            	Class nextAlgorithm = _algorithms[i];
				DiffAlgorithm nextInstance = _algorithmInstances[i];
            	if ((algorithmInstance).usesClassImageInformationInTable() &&
                    nextInstance.modifiesClassImageInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesSlotImageInformationInTable() &&
                    nextInstance.modifiesSlotImageInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesFacetImageInformationInTable() &&
                    nextInstance.modifiesFacetImageInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesInstanceImageInformationInTable() &&
                    nextInstance.modifiesInstanceImageInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesClassOperationInformationInTable() &&
                    nextInstance.modifiesClassOperationInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesSlotOperationInformationInTable() &&
                    nextInstance.modifiesSlotOperationInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesFacetOperationInformationInTable() &&
                    nextInstance.modifiesFacetOperationInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}
            	if (algorithmInstance.usesInstanceOperationInformationInTable() &&
                    nextInstance.modifiesInstanceOperationInformationInTable ()) {
                	_isAffectedBy.add(nextAlgorithm);
                    addEffect (algorithm, nextAlgorithm);
				}

            }
        }  catch (Exception e) {
        	Log.getLogger().severe("Exception occurred: " + e);}
        }

        public void addToAffectsList (Class affectedBy) {
         	_affects.add (affectedBy);
        }

        public boolean affectsOtherAlgorithms () {
         	if (_affects.isEmpty()) return false;
            if (_affects.size() == 1 && _affects.contains(_algorithm)) return false;
            return true;
        }

        public Collection getAlgorithmsThatAreAffectedByMe () {
         	return _affects;
        }

        public boolean isAffectedByOtherAlgorithms () {
         	if (_isAffectedBy.isEmpty()) return false;
            if (_isAffectedBy.size() == 1 && _isAffectedBy.contains(_algorithm)) return false;
            return true;
        }

        public void printRecord () {
         	Log.getLogger().info("Algorithm: " + _algorithm);
            Log.getLogger().info ("affects: ");
            printCollection (_affects);
            Log.getLogger().info ("isAffectedBy: ");
            printCollection (_isAffectedBy);
         	Log.getLogger().info("------------------------------------------------");
        }

        private void printCollection (Collection c) {
         	if (c == null || c.isEmpty()) {
            	Log.getLogger().info ("[]");
                return;
            }
            Iterator i = c.iterator();
            while (i.hasNext())
            	Log.getLogger().info ("" + i.next());

        }

        public Class getAlgorithm () {return _algorithm;}
    }

}

