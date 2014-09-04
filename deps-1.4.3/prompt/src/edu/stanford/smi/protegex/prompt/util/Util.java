/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.util; 


import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.clips.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.promptDiff.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class Util {
	
	public static void copyTemplateFacetValues (Cls cls, Slot from, Slot to) {
		Collection facets = cls.getTemplateFacets(to);
		Iterator i = facets.iterator();
		
		while (i.hasNext ()) {
			Facet nextFacet = (Facet)i.next();
			cls.setTemplateFacetValues(to, nextFacet, cls.getTemplateFacetValues(from, nextFacet));
		}
		
	}
	
	public static void displayErrors(Collection errors) {
		Iterator i = errors.iterator();
		while (i.hasNext()) {
			Log.getLogger().info("Error: " + i.next());
		}
	} 
	
	public static void copyTemplateFacetValues (FrameSlotCombination from, FrameSlotCombination to) {
		Cls fromCls = (Cls)from.getFrame();
		Slot fromSlot = from.getSlot();
		Cls toCls = (Cls)to.getFrame();
		Slot toSlot = to.getSlot();
		
		Collection facets = toCls.getTemplateFacets(toSlot);
		Iterator i = facets.iterator();
		while (i.hasNext ()) {
			Facet next = (Facet)i.next();
			toCls.setTemplateFacetValues(toSlot, next, fromCls.getTemplateFacetValues(fromSlot, next));
		}
	}
	
	public static void copyTopLevelSlotValues (Slot from, Slot to) {
		Collection ownSlots = from.getOwnSlots();
		Iterator i = ownSlots.iterator();
		while (i.hasNext ()) {
			Slot next = (Slot)i.next();
			if (next.getName() == Model.Slot.NAME) continue;
			Collection values = from.getOwnSlotValues(next);
			if (values != null)
				to.setOwnSlotValues(next, values);
		}
	}
	
	public static void removeFrame (Frame f) {
		if (isSystem(f)) return;
		KnowledgeBase kb = f.getKnowledgeBase();
		// if there are subclasses that have only f as a superclass, change f to :THING
		if (f instanceof Cls) {
			Collection <Cls> subclasses = new ArrayList <Cls> (Util.getDirectSubclasses ((Cls)f));
			if (subclasses != null && subclasses.size() > 0) {
				Iterator i = subclasses.iterator();
				while (i.hasNext ()) {
					Cls next = (Cls) i.next();
					if (next.getDirectSuperclassCount() == 1)
						next.addDirectSuperclass(kb.getRootCls());
				}
			}
		}
		
		Collection currentRelatedOperations = Mappings.getCurrentOperations (f);
		SuggestionsAndConflicts.removeAll(currentRelatedOperations);
		kb.deleteFrame(f);
	}
	
//	private static void removeMappingFromSources (Frame f) {
//	Collection <Frame> sources = Mappings.getSources (f);
//	if (sources == null || sources.size () == 0) return;
//	Iterator i = sources.iterator();
//	while (i.hasNext()) {
//	Frame next = (Frame)i.next();
//	// technically, nextMapping should be the same as f, but just in case
//	Frame nextMapping = Mappings.getWhatBecameOfIt(next);
//	if (nextMapping.equals (f))
//	Mappings.updateWhatBecameOfItMap (next, null);
//	}
//	}
	
	public static KnowledgeBase getSingleSourceKb (Collection sources) {
		if (sources == null || sources.size() == 0)
			return null;
		Iterator i = sources.iterator();
		KnowledgeBase source = ((Frame)i.next()).getKnowledgeBase();
		while (i.hasNext()) {
			Frame next = (Frame)i.next();
			if (source != next.getKnowledgeBase())
				return null;
		}
		return source;
	}
	
	public static ArrayList <Object> reverseList (ArrayList <Object> l) {
		if (l == null) return null;
		Iterator i = l.iterator();
		ArrayList <Object> result = new ArrayList<Object> ();
		while (i.hasNext()) {
			result.add (0, i.next());
		}
		return result;
	}
	
//	get child objects in a diff tree
	public static Collection <Cls> getDiffChildObjects(Cls currentNode, ResultTable diffTable) {
			Collection <Cls> result = new ArrayList (getDirectSubclasses(currentNode));
			Frame map = getMap (currentNode, diffTable);
			if (map == null) return result;
			
			Collection mapSubclasses = getDirectSubclasses(((Cls)map));
			if (mapSubclasses == null || mapSubclasses.isEmpty()) return result;
			
			try{ 
			Iterator i = mapSubclasses.iterator();
			while (i.hasNext()) {  
				Cls nextMapSubclass = (Cls)i.next();
				TableRow row = (TableRow)CollectionUtilities.getFirstItem(diffTable.getRows(nextMapSubclass));
				
//				*** different code for going from source to image
				if (row != null && row.getOperationValue() == TableRow.OPERATION_DELETE)
					result.add(nextMapSubclass);
				else {
					// determine if there is a moved frame
					Collection <Cls> subsInImage = (Collection) diffTable.getImages(nextMapSubclass);
					if (subsInImage != null && !(new ArrayList <Cls> (result)).removeAll(subsInImage))
						result.addAll (subsInImage);
				}
			}
			return result;
		} catch (ClassCastException e) {
			Log.getLogger().info("***ClassCastException: currentNode = " + currentNode);
			Log.getLogger().info("result = " + result);
			Log.getLogger().info("map = " + map);
			Log.getLogger().info("mapSubclasses: " + mapSubclasses);
			return null;
		}
	}
	
	private static Collection <Cls> removeHiddenClasses (Collection <Cls> subs) {
//		Log.getLogger() .info ("subs = " + subs);
		Collection <Cls> result = new ArrayList <Cls> ();
		if (subs == null || subs.size()== 0) return result;
		Project project = PromptTab.getPromptDiff().getViewSetUp().getProject2();		
		Iterator i = subs.iterator();
		while (i.hasNext()) {
			Cls next = (Cls)i.next();
//			Log.getLogger ().info( "next = " + next);
			if (!project.isHidden(next))
				result.add(next);
		}
		return result;
	}
	
	public static Frame getMap (Frame frame, ResultTable diffTable) {
		if (frame == null) return null;
		if (frame.getKnowledgeBase()==diffTable.getKb1()) {
			return diffTable.getFirstImage(frame);
		} else {
			return diffTable.getFirstSource(frame);
		}
	}
	
	public static boolean isSystem (Frame f) {
		if (f.isSystem()) return true;
		if (PromptTab.kbInOWL() && (OWLUtil.isOWLSystemFrame (f))) return true;
		return false;
	}
	
	public static Cls getStandardMetaclass (KnowledgeBase kb) {
		if (PromptTab.kbInOWL ()) {
			return OWLUtil.getNamedClsMetaCls(kb);
		}
		return kb.getCls(Model.Cls.STANDARD_CLASS);
	}
	
	public static Cls getStandardMetaslot (KnowledgeBase kb) {
		if (PromptTab.kbInOWL ()) {
			return OWLUtil.getPropertyMetaSlot(kb);
		}
		return kb.getCls(Model.Cls.STANDARD_SLOT);
	}
	
	public static Cls getRootCls (KnowledgeBase kb) {
		if (PromptTab.kbInOWL ()) {
			return OWLUtil.getRootCls (kb);
		}
		return kb.getCls(Model.Cls.SYSTEM_CLASS);
	}
	
	private static Collection <Frame> _systemFrames = null;
	
	//private static final String OWL_KNOWLEDGE_BASE_CLASS_NAME = "edu.stanford.smi.protegex.owl.model.impl.DefaultOWLKnowledgeBase";
	
	static public Collection <Frame> getSystemFrames () {
		
		KnowledgeBase kb = ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase();
		
		if (PromptTab.kbInOWL()) 
			_systemFrames = new ArrayList <Frame> (OWLUtil.getOWLSystemFrames(kb));
		else 
			_systemFrames = new HashSet <Frame> ();
		KnowledgeBaseFactory currentFactory = null;
		currentFactory = kb.getKnowledgeBaseFactory();
		Collection errors = new ArrayList();
		Project emptyProject = new Project (null, errors);
		emptyProject.setKnowledgeBaseFactory(currentFactory);
		Util.displayErrors(errors);
		
		errors.clear();
		emptyProject.createDomainKnowledgeBase(currentFactory, errors, false);
		Util.displayErrors(errors);
		_systemFrames.addAll(emptyProject.getKnowledgeBase().getFrames());
		
		return _systemFrames;
		
	}
	
	private static HashMap <KnowledgeBase, Collection<Frame> > _kbToFrames = new HashMap <KnowledgeBase, Collection<Frame> > (3); //<kb, all frames>
	public static Collection <Frame> getFrames (KnowledgeBase kb) { 
		Collection<Frame> frames = (Collection<Frame>)_kbToFrames.get(kb);
		if (frames != null) return frames;
		
//		frames = (PromptTab.kbInOWL ()) ? OWLUtil.getFramesFast (kb) : kb.getFrames();
		frames = kb.getFrames();
		_kbToFrames.put (kb, frames);
		return frames;
		
	}
	
	public static Collection removeAnonymousClses (Collection c) {
		if (PromptTab.kbInOWL())
			c = OWLUtil.removeAnonymousClasses(c);
		return c;
	}
	
	public static Collection <Cls> getDirectSubclasses (Cls cls) {
		Collection <Cls> result = cls.getDirectSubclasses();
		if (PromptTab.kbInOWL())
			result = OWLUtil.removeAnonymousAndEquivalentSubclasses (result, cls, cls.getKnowledgeBase());
		return result;
	}
	
	public static Collection getDirectSuperclasses (Cls cls) {
		Collection result = cls.getDirectSuperclasses();
		
		if (PromptTab.kbInOWL())
			result = OWLUtil.removeAnonymousAndEquivalentSubclasses(result, cls, cls.getKnowledgeBase());
		return result;
	}
	
	public static Collection getDirectOwnSlotValues(Instance inst, Slot slot) {
		Collection result = inst.getDirectOwnSlotValues(slot);
		if (PromptTab.kbInOWL())
			result = OWLUtil.removeAnonymousClasses(result);
		return result;
	}	
	
	public static boolean displayFrameInDiffTable (edu.stanford.smi.protege.model.Frame f) {
		if (f == null) return true;
		
		if (Util.isSystem(f)) return false;
		if (!f.isVisible()) return false;
		if (PromptTab.kbInOWL()) return OWLUtil.displayFrameInDiffTable (f);
		
		return true;
	}
	
	public static boolean kbInOWL (KnowledgeBase kb) {
		int index = (kb.getClass().getName().indexOf("OWL"));
		return (index > 0);
	}
	
	public static Collection <Instance> getDeletedInstances(Cls cls,ResultTable diffTable){
		Collection <Instance> result = new ArrayList<Instance>();
		
		Cls mapping = (Cls)Util.getMap(cls,diffTable);
		if(mapping == null)
			return result;
		
		Collection<Instance> oldInstances = mapping.getDirectInstances();
		
		for(Iterator iter = oldInstances.iterator();iter.hasNext();){
			Instance oldInstance = (Instance)iter.next();
			TableRow row = (TableRow)CollectionUtilities.getFirstItem(diffTable.getRows(oldInstance));
			
			if (row != null && row.getOperationValue() == TableRow.OPERATION_DELETE && !row.isChangeAccepted())
				result.add(oldInstance);
		}
		return result;
	}
	
	
	public static boolean haveInstancesChanged(ResultTable diffTable,Cls cls){
		Collection instances = cls.getDirectInstances();
		for(Iterator iter = instances.iterator();iter.hasNext();){
			Instance instance = (Instance)iter.next();
			if(getStatus(diffTable,instance) != 0)
				return true;
		}
		
		return false;
	}
	
	public static int getStatus(ResultTable diffTable,Frame frame){
		int status = 0;
		TableRow row = (TableRow)CollectionUtilities.getFirstItem(diffTable.getRows(frame));
		if(row == null || row.isChangeAccepted())
			return 0;
		
		String operation = row.getOperationValue();
		String level = row.getMappingLevel();
		String renamed = row.getRenameValue();
		
		if(operation == TableRow.OPERATION_ADD){
			status = FrameDifferenceElement.OP_ADDED;
		}else if(operation == TableRow.OPERATION_DELETE){
			status = FrameDifferenceElement.OP_DELETED;
		}else if(level == TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED || renamed == TableRow.RENAME_PLUS){
			status = FrameDifferenceElement.OP_CHANGED;	
		}
		
		return status;
	}
	
	public static String createAuthorString(Collection users) {
		Collection <String> alreadySeen = new ArrayList<String>();
		String authorLabel = "";
		Iterator c = users.iterator();
		boolean firstAuthor = true;
		while (c.hasNext()) {
			String nextAuthor = (String)c.next();
			if (nextAuthor == null) continue;
			if (alreadySeen.add(nextAuthor)) {
				if (firstAuthor) {
					authorLabel += " ";
					firstAuthor = false;
				} else {
					authorLabel += ", ";
				}
				authorLabel += nextAuthor;
			}
		}
		return authorLabel;
	}
	
	public static URI getProjectURI (String projectName) {
		try {
			File installationDir = new File (PromptTab.getPromptDirectory());
			URI projectURI = getProjectFromJar (installationDir, "prompt.jar", projectName);
			if (projectURI != null)
				return projectURI; 
			else 
				return getProjectFromDirectory (installationDir, projectName);
		} catch (Exception e) {
			Log.getLogger().severe ("Cannot create a URI");
			return null;
		}
	}
	
	public static URI getProjectFromDirectory (File installationDir, String projectName) {
		File projectsDir = new File (installationDir, "projects");
		File project = new File (projectsDir, projectName);
		
		return project.toURI();
	}
	
	public static URI getProjectFromJar (File installationDir, String jarName, String projectName) {
		try {
			File projectsDir = new File (installationDir, jarName);
			if (!projectsDir.exists()) return null;
			
			String projectURIString = "jar:" + projectsDir.toURI().toURL().toString() + "!/projects/" + projectName;
			
			URI project = new URI (projectURIString);
			return project;
			
		} catch (Exception e) {
			Log.getLogger().severe ("Cannot convert URL");
			return null;
		}
	}
	
	public static Collection <String> getBrowserNamesFromCollection (Collection <Frame> c) {
		if (c == null || c.isEmpty()) return null;
		Collection <String> result = new ArrayList<String>();
		Iterator i = c.iterator();
		while (i.hasNext()) {
			Frame next = (Frame)i.next();
			result.add(next.getBrowserText());
		}
		return result;
	}
	
	public static String mergeOrMapString() {
		return PromptTab.merging() ? "merge" : "map";
	}
	
	public static Project createNewClipsProject (String suffix) {
		Project project = null;
		Collection errors = new ArrayList();
		ClipsKnowledgeBaseFactory factory = new ClipsKnowledgeBaseFactory();
		project = Project.createNewProject(factory, errors);
		String targetProjectName = ProjectManager.getProjectManager().getCurrentProject().getProjectName();
		String acProjectName = targetProjectName + suffix;
		project.setProjectURI(URI.create(ProjectManager.getProjectManager().getCurrentProject().getProjectDirectoryURI() + "/" + acProjectName + ".pprj"));
		Util.displayErrors(errors);       
		return project;
	}
	
	public static Project createNewProject (KnowledgeBaseFactory factory) {
		Project project = null;
		Collection errors = new ArrayList();
		project = Project.createNewProject(factory, errors);
		Util.displayErrors(errors);
		return project;
	}
	
	//also removes anonymous classes
	public static Collection <Cls> getLocalClses(KnowledgeBase kb) {
		boolean checkIfAnonymous = PromptTab.kbInOWL();
		Collection<Cls> localAndIncludedFrames = kb.getClses();
		ArrayList<Cls> allClses = new ArrayList<Cls>();
		
		Iterator i = localAndIncludedFrames.iterator();
		Cls next;
		
		while (i.hasNext()) {
			next = (Cls) i.next();
			if (isSystem(next) || next.isIncluded())
				continue;
			if (checkIfAnonymous && OWLUtil.isOWLAnonymousClassFrame(next))
				continue;
			if (!next.isIncluded() && !next.isEditable())
				continue;
			allClses.add(next);
		}
		return allClses;
	}
	
	
	public static String getLocalBrowserText (Frame f) {
		if (!PromptTab.kbInOWL()) return f.getBrowserText();
		
		if (f instanceof RDFIndividual && ((RDFIndividual)f).isAnonymous()) return f.getBrowserText();
		
		KnowledgeBase kb = f.getKnowledgeBase();
		Cls type = ((Instance)f).getDirectType();
		if (type == null) {
			Log.getLogger().warning("Null type for frame " + f);
			return f.getBrowserText();
		}
		BrowserSlotPattern pattern = null;
		if (type instanceof OWLClass && ! ((OWLClass)type).isAnonymous()) 
			pattern = type.getBrowserSlotPattern();
		if (pattern == null) return f.getBrowserText();
		
		Slot browserSlot = pattern.getFirstSlot();
		if (browserSlot == kb.getSlot(Model.Slot.NAME)) {//assume no browser slot set
			return ((RDFResource)f).getLocalName();
		}
		else
			return f.getBrowserText();
	}
	
	public static String getLocalName(Frame f) {
		if (PromptTab.kbInOWL()) 
			return ((RDFResource)f).getLocalName();
		else
			return f.getName();
		
	}
	
//	public static LinkedList reverseList(LinkedList list) {
//	LinkedList result = new LinkedList ();
//	Iterator i = list.iterator();
//	while (i.hasNext()) {
//	result.addFirst(i.next());
//	}
//	return result;
//	}
	
	public static boolean compareInstances (Instance instance1, Instance instance2, Map<Slot, Slot> slotsMap, ResultTable diffTable) {
		Iterator<Slot> i = slotsMap.keySet().iterator();
		while (i.hasNext()) {
			Slot nextSlot = i.next();
			Slot nextSlotImage = (Slot)slotsMap.get(nextSlot);
			Collection values1 = instance1.getDirectOwnSlotValues(nextSlot);
			Collection values2 = instance2.getDirectOwnSlotValues(nextSlotImage);
			boolean same = AlgorithmUtils.compareCollections(values1, values2, diffTable);
			if (!same) return false;
		}
		
		return true;
	}
	
	public static Map<Slot, Slot> createSlotMap (Collection<Slot> slots1, Collection<Slot> slots2, ResultTable diffTable) {
		Map<Slot, Slot> result = new HashMap<Slot, Slot> ();
		Iterator<Slot> i = slots1.iterator();
		while (i.hasNext()) {
			Slot nextSlot = i.next();
			Slot nextSlotImage = (Slot)diffTable.getFirstImage(nextSlot);
			if (nextSlotImage == null) continue;
			if (nextSlot.getFrameID().equals(Model.SlotID.NAME) ||
					nextSlot.getFrameID().equals(Model.SlotID.DIRECT_TYPES)) continue;
			if (!slots2.contains(nextSlotImage)) continue;
			result.put(nextSlot, nextSlotImage);		  
		}
		return result;
	}
	
//	public static boolean isIncludedOrImported (Frame f) {
//	if (isSystem(f)) return true;
//	if (PropmtTab.kbInOWL())
//	return ((RDFSResource)f).
//	}
	
}
