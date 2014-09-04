/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ListMultiMap;
import edu.stanford.smi.protege.util.MultiMap;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.prompt.actionLists.HashMapForCollections;
import edu.stanford.smi.protegex.prompt.util.Util;

public class KnowledgeBaseInMerging {
	KnowledgeBase _kb;
	Project _project;
	String _prettyName;
	MultiMap _frameActionsMap;
	HashMap _whatBecameOfItMap;
	HashMapForCollections _sourceMap;
	static private int _colorIndex = 0;
	static private Color[] _textColors = { Color.red, Color.blue };
	static private Color[] _grayedTextColors = { new Color((float) 1.0, (float) 0.76, (float) 0.80), new Color((float) .48, (float) 0.73, (float) 1.00) };

	Color _textColor;
	Color _includedTextColor = null; //used in partitioning mode only
//	Color _grayedTextColor;
	boolean _target = false;
	ArrayList errors = new ArrayList();

	KnowledgeBaseInMerging(Project project, String prettyName, boolean processingTarget) {
		_prettyName = prettyName;

		_project = project;
		_kb = _project.getKnowledgeBase();
		if (!processingTarget) {
			setTextColor();
		}

		_frameActionsMap = new ListMultiMap();
		if (processingTarget) {
			_sourceMap = new HashMapForCollections();
			_target = true;
		} else {
//			_project.setIsReadonly (true);
		}
		_whatBecameOfItMap = new HashMap();
		ProjectsAndKnowledgeBases.addToKbs(prettyName, this);
//		if (processingTarget) {
//			ProjectsAndKnowledgeBases.setCurrentKnowledgeBase(_kb);
//			ProjectsAndKnowledgeBases.setCurrentProject(_project);
//		}
		_project.setDisplayAbstractClassIcon(false);
		_project.setDisplayMultiParentClassIcon(false);
	}

	private void setTextColor() {
		_textColor = _textColors[_colorIndex % _textColors.length];
		if (PromptTab.moving()) {
			_includedTextColor = _grayedTextColors[_colorIndex % _textColors.length];
//			_grayedTextColor = _grayedTextColors [_colorIndex % _textColors.length];
		}
		_colorIndex++;
	}

	public Color getFrameTextColor() {
		return _textColor;
	}

	/* public Color getGrayedFrameColor () {
	 return _grayedTextColor;
	 }
	 
	 public static Color getGrayedFrameColor (Object f) {
	 if (f == null || ! (f instanceof Frame))
	 return null;
	 KnowledgeBaseInMerging kbInMerging =
	 PromptTab.getKnowledgeBaseInMerging(((Frame)f).getKnowledgeBase());
	 return kbInMerging.getGrayedFrameColor();
	 }
	 */
	public static Color getFrameColor(Object f) {
		Color textColor = null;
		if (f == null || !(f instanceof Frame) || PromptTab.extracting() && PromptTab.getTraversalDirectivesKb().getKnowledgeBase() == ((Frame) f).getKnowledgeBase()) {
			textColor = null;
		} else {
			KnowledgeBaseInMerging kbInMerging = ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(((Frame) f).getKnowledgeBase());
			if (kbInMerging.getKnowledgeBase().equals(ProjectsAndKnowledgeBases.getTargetKnowledgeBase())) {
				if (PromptTab.moving()) {
					if (((Frame) f).isIncluded()) {
						//						textColor = PromptTab.getSourceKnowledgeBasesInMerging()[0].getFrameTextColor();
						textColor = getTextColorFromIncludedProject((Frame) f);
					} else {
						textColor = null;
					}
				} else if (PromptTab.merging() || PromptTab.mapping()) {
					Collection sources = Mappings.getSources((Frame) f);
					KnowledgeBase sourceKb = Util.getSingleSourceKb(sources);
					if (sourceKb == null || sourceKb == ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
						textColor = null;
					} else {
						textColor = ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(sourceKb).getFrameTextColor();
					}
				} else {
					textColor = null;
				}
			} else //f not in target
			if (PromptTab.moving()) { // f is a frame in the project being partitioned (which may include other projects)
				textColor = kbInMerging.getTextColorForPartitioning((Frame) f);
			} else {
				textColor = kbInMerging.getFrameTextColor();
			}
		}
		return textColor;
	}

	/**
	 * Called during partitioniing. Frame f is in the target project but is included. hence, need to
	 * find out what it's color is based on the included project to which frames are being mnoved.
	 */
	private static Color getTextColorFromIncludedProject(Frame frame) {
		KnowledgeBaseInMerging includedKb = ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(ProjectsAndKnowledgeBases.INCLUDED_PROJECT_INDEX);
		Frame f = includedKb.getKnowledgeBase().getFrame(frame.getName());
		if (f == null) { //f is included in the target project but it is not in the included project that the user
			// is working with (it is hence in one of the other included projects that should
			// remain untouched
			return includedKb.getIncludedtextColor();
		} else {
			return includedKb.getTextColorForPartitioning(f);
		}
	}

	private Color getIncludedtextColor() {
		return _includedTextColor;
	}

//	returns the kb color if the frame is local to the project (which is included in teh target project)
	// returns "included" color otherwise
	private Color getTextColorForPartitioning(Frame f) {
		if (!f.isIncluded() || Util.isSystem(f)) {
			return _textColor;
		} else {
			return _includedTextColor;
		}
	}

	public static boolean isInTarget(Frame f) {
		KnowledgeBaseInMerging kbInMerging = ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(f.getKnowledgeBase());
		if (kbInMerging.getKnowledgeBase().equals(ProjectsAndKnowledgeBases.getTargetKnowledgeBase())) {
			return true;
		} else {
			return false;
		}
	}

	public KnowledgeBase getKnowledgeBase() {
		return _kb;
	}

	public Project getProject() {
		return _project;
	}

	public void targetKbChanged() {
		_kb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
		_project = ProjectsAndKnowledgeBases.getProject(_kb);
	}

	public MultiMap getFrameActionsMap() {
		return _frameActionsMap;
	}

	public Collection<Frame> getSources(Frame f) {
		if (_sourceMap != null) {
			Object sourceFrames = _sourceMap.get(f);
			if (sourceFrames != null) {
				return (Collection<Frame>) sourceFrames;
			}
		}

		return null;
	}

	public HashMap getWhatBecameOfItMap() {
		return _whatBecameOfItMap;
	}

	public Frame getWhatBecameOfIt(Frame f) {
		return (Frame) _whatBecameOfItMap.get(f);
	}

	public String getPrettyName() {
		return _prettyName;
	}

	public boolean isTarget() {
		return _target;
	}

	public void createWhatBecameOfItBinding(Frame oldFrame, Frame newFrame) {
		if (newFrame == null) {
			_whatBecameOfItMap.remove(oldFrame);
			return;
		}
		if (_target) {
			Collection oldSources = Mappings.getSources(oldFrame);

			if (oldSources != null) {
				Iterator i = oldSources.iterator();
				Frame next;
				while (i.hasNext()) {
					next = (Frame) i.next();
					ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(next.getKnowledgeBase()).createWhatBecameOfItBinding(next, newFrame);
//					_sourceMap.put (bindingFrame.getName(), next);
				}
				_sourceMap.remove(oldFrame);
			}
		} else {
			_whatBecameOfItMap.put(oldFrame, newFrame);
			Mappings.setSource(oldFrame, newFrame);
		}
	}

	public void updateWhatBecameOfItBinding(Frame frame, Frame bindingFrame) {
		if (frame.getKnowledgeBase() != ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
			_whatBecameOfItMap.put(frame, bindingFrame);
		}
		Mappings.setSource(frame, bindingFrame);
	}

	public void removeWhatBecameOfItBinding(Frame frame) {
		if (frame.getKnowledgeBase() != ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
			_whatBecameOfItMap.remove(frame);
		}
	}

	public void setSource(Frame oldFrame, Frame newFrame) {
		// we are in the target kb
		if (newFrame.getKnowledgeBase() == getKnowledgeBase()) {
			_sourceMap.put(newFrame, oldFrame);
		}
	}

	/*
	 public void updateSourceMap (Frame f, String oldName) {
	 _sourceMap.updateKey (f, oldName);
	 }
	 */
	public String getName() {
		return _prettyName;
	}

	public void setWhatBecameOfItForImportedOrIncludedFrames(Project mergedProject) {
		if (PromptTab.kbInOWL()) {
			setWhatBecameOfItForImportedResources(mergedProject);
		} else {
			setWhatBecameOfItForIncludedFrames(mergedProject);
		}
	}

	private void setWhatBecameOfItForImportedResources(Project mergedProject) {
		OWLModel mergedOWLModel = (OWLModel) mergedProject.getKnowledgeBase();

		Collection<RDFResource> allResources = ((OWLModel) _kb).getRDFResources();
		Iterator<RDFResource> i = allResources.iterator();
		while (i.hasNext()) {
			RDFResource nextResource = i.next();
			if (!nextResource.isSystem() && nextResource.isIncluded()) {
				String includedResourceURI = ((OWLModel) _kb).getURIForResourceName(nextResource.getName());
				RDFResource nextResourceInMerged = mergedOWLModel.getRDFResource(mergedOWLModel.getResourceNameForURI(includedResourceURI));
				_whatBecameOfItMap.put(nextResource, nextResourceInMerged);
			}
		}
	}

	private void setWhatBecameOfItForIncludedFrames(Project mergedProject) {
		KnowledgeBase mergedKb = mergedProject.getKnowledgeBase();
		Collection<Frame> allFrames = _kb.getFrames();
		Iterator i = allFrames.iterator();
		while (i.hasNext()) {
			Frame nextFrame = (Frame) i.next();
			if (!nextFrame.isSystem() && nextFrame.isIncluded()) {
				Frame nextFrameInMerged = mergedKb.getFrame(nextFrame.getName());
				_whatBecameOfItMap.put(nextFrame, nextFrameInMerged);
			}
		}
	}

	public void updateProjectAndKb(Project mergedProject) {
		_project = mergedProject;
		_kb = mergedProject.getKnowledgeBase();
		ProjectsAndKnowledgeBases.addToKbs(_prettyName, this);
	}

}
