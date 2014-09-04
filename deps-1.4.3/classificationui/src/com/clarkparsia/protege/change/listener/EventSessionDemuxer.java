package com.clarkparsia.protege.change.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.protege.change.ChangeLog;
import com.clarkparsia.protege.change.Converter;
import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.InstanceEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.event.TransactionEvent;
import edu.stanford.smi.protege.event.TransactionListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.ProtegeInstanceListener;
import edu.stanford.smi.protegex.owl.model.event.ProtegeKnowledgeBaseListener;
import edu.stanford.smi.protegex.owl.model.event.ProtegeSlotListener;

/**
 * <p>
 * Title: Event Session Demuxer
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class EventSessionDemuxer {

	private class DemuxingClsListener implements ClsListener {

		public void directInstanceAdded(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Instance aInstance = event.getInstance();
			if (aCls instanceof RDFSClass && aInstance instanceof RDFResource) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFResource aResource = (RDFResource) aInstance;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.instanceAdded(aRDFSClass, aResource);
			}
		}

		public void directInstanceRemoved(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Instance aInstance = event.getInstance();
			if (aCls instanceof RDFSClass && aInstance instanceof RDFResource) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFResource aResource = (RDFResource) aInstance;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.instanceRemoved(aRDFSClass, aResource);
			}
		}

		public void directSubclassAdded(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Cls aSubCls = event.getSubclass();
			if (aCls instanceof RDFSClass && aSubCls instanceof RDFSClass) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFSClass aRDFSSubClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.subclassAdded(aRDFSClass, aRDFSSubClass);
			}
		}

		public void directSubclassMoved(ClsEvent event) {
			// No-op behavior, as in ClassAdapter
		}

		public void directSubclassRemoved(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Cls aSubCls = event.getSubclass();
			if (aCls instanceof RDFSClass && aSubCls instanceof RDFSClass) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFSClass aRDFSSubClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.subclassRemoved(aRDFSClass, aRDFSSubClass);
			}
		}

		public void directSuperclassAdded(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Cls aSupCls = event.getSuperclass();
			if (aCls instanceof RDFSClass && aSupCls instanceof RDFSClass) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFSClass aRDFSSupClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.superclassAdded(aRDFSClass, aRDFSSupClass);
			}
		}

		public void directSuperclassRemoved(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Cls aSupCls = event.getSuperclass();
			if (aCls instanceof RDFSClass && aSupCls instanceof RDFSClass) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFSClass aRDFSSupClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.superclassRemoved(aRDFSClass, aRDFSSupClass);
			}
		}

		public void templateFacetAdded(ClsEvent event) {
			// No-op behavior, as in ClassAdapter
		}

		public void templateFacetRemoved(ClsEvent event) {
			// No-op behavior, as in ClassAdapter
		}

		public void templateFacetValueChanged(ClsEvent event) {
			// No-op behavior, as in ClassAdapter
		}

		public void templateSlotAdded(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Slot aSlot = event.getSlot();
			if (aCls instanceof RDFSClass && aSlot instanceof RDFProperty) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFProperty aProperty = (RDFProperty) aSlot;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.addedToUnionDomainOf(aRDFSClass, aProperty);
			}
		}

		public void templateSlotRemoved(ClsEvent event) {
			final Cls aCls = event.getCls();
			final Slot aSlot = event.getSlot();
			if (aCls instanceof RDFSClass && aSlot instanceof RDFProperty) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFProperty aProperty = (RDFProperty) aSlot;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mClassListener
						.removedFromUnionDomainOf(aRDFSClass, aProperty);
			}
		}

		public void templateSlotValueChanged(ClsEvent event) {
			// No-op behavior, as in ClassAdapter
		}
	}

	private class DemuxingFrameListener implements FrameListener {

		public void browserTextChanged(FrameEvent event) {
			final Frame aFrame = event.getFrame();
			if (aFrame instanceof RDFResource) {
				final RDFResource aResource = (RDFResource) aFrame;
				getListeners(event.getSession(), aResource.getOWLModel()).mValueListener
						.browserTextChanged(aResource);
			}
		}

		public void deleted(FrameEvent event) {
			// No-op behavior, as in PropertyValueAdapter
		}

		public void nameChanged(FrameEvent event) {
			final Frame aFrame = event.getFrame();
			if (aFrame instanceof RDFResource) {
				final RDFResource aResource = (RDFResource) aFrame;
				getListeners(event.getSession(), aResource.getOWLModel()).mValueListener
						.nameChanged(aResource, event.getOldName());
			}
		}

		public void ownFacetAdded(FrameEvent event) {
			// No-op behavior, as in PropertyValueAdapter
		}

		public void ownFacetRemoved(FrameEvent event) {
			// No-op behavior, as in PropertyValueAdapter
		}

		public void ownFacetValueChanged(FrameEvent event) {
			// No-op behavior, as in PropertyValueAdapter
		}

		public void ownSlotAdded(FrameEvent event) {
			// No-op behavior, as in PropertyValueAdapter
		}

		public void ownSlotRemoved(FrameEvent event) {
			// No-op behavior, as in PropertyValueAdapter
		}

		public void ownSlotValueChanged(FrameEvent event) {
			final Frame aFrame = event.getFrame();
			final Slot aSlot = event.getSlot();
			if (aFrame instanceof RDFResource && aSlot instanceof RDFProperty) {
				final RDFResource aResource = (RDFResource) aFrame;
				final RDFProperty aProperty = (RDFProperty) aSlot;
				getListeners(event.getSession(), aResource.getOWLModel()).mValueListener
						.propertyValueChanged(aResource, aProperty, event.getOldValues());
			}
		}

		public void visibilityChanged(FrameEvent event) {
			final Frame aFrame = event.getFrame();
			if (aFrame instanceof RDFResource) {
				final RDFResource aResource = (RDFResource) aFrame;
				getListeners(event.getSession(), aResource.getOWLModel()).mValueListener
						.visibilityChanged(aResource);
			}
		}
	}

	private class DemuxingInstanceListener implements ProtegeInstanceListener {

		public void directTypeAdded(InstanceEvent event) {
			final Cls aCls = event.getCls();
			final Instance aInstance = event.getInstance();
			if (aCls instanceof RDFSClass && aInstance instanceof RDFResource) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFResource aResource = (RDFResource) aInstance;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mResourceListener
						.typeAdded(aResource, aRDFSClass);
			}
		}

		public void directTypeRemoved(InstanceEvent event) {
			final Cls aCls = event.getCls();
			final Instance aInstance = event.getInstance();
			if (aCls instanceof RDFSClass && aInstance instanceof RDFResource) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				final RDFResource aResource = (RDFResource) aInstance;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mResourceListener
						.typeRemoved(aResource, aRDFSClass);
			}
		}

	}

	private class DemuxingKBListener implements ProtegeKnowledgeBaseListener {

		public void clsCreated(KnowledgeBaseEvent event) {
			final Cls aCls = event.getCls();
			if (aCls instanceof RDFSClass) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mModelListener
						.classCreated(aRDFSClass);
			}
		}

		public void clsDeleted(KnowledgeBaseEvent event) {
			final Cls aCls = event.getCls();
			if (aCls instanceof RDFSClass) {
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aRDFSClass.getOWLModel()).mModelListener
						.classDeleted(aRDFSClass);
			}
		}

		public void defaultClsMetaClsChanged(KnowledgeBaseEvent event) {
			// No-op behavior, as in ModelAdapter
		}

		public void defaultFacetMetaClsChanged(KnowledgeBaseEvent event) {
			// No-op behavior, as in ModelAdapter
		}

		public void defaultSlotMetaClsChanged(KnowledgeBaseEvent event) {
			// No-op behavior, as in ModelAdapter
		}

		public void facetCreated(KnowledgeBaseEvent event) {
			// No-op behavior, as in ModelAdapter
		}

		public void facetDeleted(KnowledgeBaseEvent event) {
			// No-op behavior, as in ModelAdapter
		}

		public void frameNameChanged(KnowledgeBaseEvent event) {
			final Frame aFrame = event.getFrame();
			if (aFrame instanceof RDFResource) {
				final RDFResource aResource = (RDFResource) aFrame;
				getListeners(event.getSession(), aResource.getOWLModel()).mModelListener
						.resourceNameChanged(aResource, event.getOldName());
			}
		}

		public void instanceCreated(KnowledgeBaseEvent event) {
			final Frame aFrame = event.getFrame();
			if (aFrame instanceof RDFResource) {
				final RDFResource aResource = (RDFResource) aFrame;
				getListeners(event.getSession(), aResource.getOWLModel()).mModelListener
						.individualCreated(aResource);
			}
		}

		public void instanceDeleted(KnowledgeBaseEvent event) {
			final Frame aFrame = event.getFrame();
			if (aFrame instanceof RDFResource) {
				final RDFResource aResource = (RDFResource) aFrame;
				getListeners(event.getSession(), aResource.getOWLModel()).mModelListener
						.individualDeleted(aResource);
			}
		}

		public void slotCreated(KnowledgeBaseEvent event) {
			final Slot aSlot = event.getSlot();
			if (aSlot instanceof RDFProperty) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				getListeners(event.getSession(), aProperty.getOWLModel()).mModelListener
						.propertyCreated(aProperty);
			}
		}

		public void slotDeleted(KnowledgeBaseEvent event) {
			final Slot aSlot = event.getSlot();
			if (aSlot instanceof RDFProperty) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				getListeners(event.getSession(), aProperty.getOWLModel()).mModelListener
						.propertyDeleted(aProperty);
			}
		}
	}

	private class DemuxingSlotListener implements ProtegeSlotListener {

		public void directSubslotAdded(SlotEvent event) {
			final Slot aSlot = event.getSlot();
			final Slot aSubSlot = event.getSubslot();
			if (aSlot instanceof RDFProperty && aSubSlot instanceof RDFProperty) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				final RDFProperty aSubProperty = (RDFProperty) aSubSlot;
				getListeners(event.getSession(), aProperty.getOWLModel()).mPropertyListener
						.subpropertyAdded(aProperty, aSubProperty);
			}
		}

		public void directSubslotMoved(SlotEvent event) {
			// No-op behavior, as in PropertyAdapter
		}

		public void directSubslotRemoved(SlotEvent event) {
			final Slot aSlot = event.getSlot();
			final Slot aSubSlot = event.getSubslot();
			if (aSlot instanceof RDFProperty && aSubSlot instanceof RDFProperty) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				final RDFProperty aSubProperty = (RDFProperty) aSubSlot;
				getListeners(event.getSession(), aProperty.getOWLModel()).mPropertyListener
						.subpropertyRemoved(aProperty, aSubProperty);
			}
		}

		public void directSuperslotAdded(SlotEvent event) {
			final Slot aSlot = event.getSlot();
			final Slot aSupSlot = event.getSuperslot();
			if (aSlot instanceof RDFProperty && aSupSlot instanceof RDFProperty) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				final RDFProperty aSupProperty = (RDFProperty) aSupSlot;
				getListeners(event.getSession(), aProperty.getOWLModel()).mPropertyListener
						.superpropertyAdded(aProperty, aSupProperty);
			}
		}

		public void directSuperslotRemoved(SlotEvent event) {
			final Slot aSlot = event.getSlot();
			final Slot aSupSlot = event.getSuperslot();
			if (aSlot instanceof RDFProperty && aSupSlot instanceof RDFProperty) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				final RDFProperty aSupProperty = (RDFProperty) aSupSlot;
				getListeners(event.getSession(), aProperty.getOWLModel()).mPropertyListener
						.superpropertyRemoved(aProperty, aSupProperty);
			}
		}

		public void templateSlotClsAdded(SlotEvent event) {
			final Slot aSlot = event.getSlot();
			final Cls aCls = event.getCls();
			if (aSlot instanceof RDFProperty && aCls instanceof RDFSClass) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aProperty.getOWLModel()).mPropertyListener
						.unionDomainClassAdded(aProperty, aRDFSClass);
			}
		}

		public void templateSlotClsRemoved(SlotEvent event) {
			final Slot aSlot = event.getSlot();
			final Cls aCls = event.getCls();
			if (aSlot instanceof RDFProperty && aCls instanceof RDFSClass) {
				final RDFProperty aProperty = (RDFProperty) aSlot;
				final RDFSClass aRDFSClass = (RDFSClass) aCls;
				getListeners(event.getSession(), aProperty.getOWLModel()).mPropertyListener
						.unionDomainClassRemoved(aProperty, aRDFSClass);
			}
		}
	}

	private class DemuxingTransactionListener implements TransactionListener {
		public void transactionBegin(TransactionEvent event) {
			final KnowledgeBase aKb = event.getKnowledgeBase();
			if (aKb instanceof OWLModel) {
				OWLModel aModel = (OWLModel) aKb;
				ListenerCollection aCol = getListeners(event.getSession(), aModel);
				aCol.mChangeLog.transactionBegin(event);
				aCol.mValueListener.startTransaction();
			}
		}

		public void transactionEnded(TransactionEvent event) {
			final KnowledgeBase aKb = event.getKnowledgeBase();
			if (aKb instanceof OWLModel) {
				final OWLModel aModel = (OWLModel) aKb;
				ListenerCollection aCol = getListeners(event.getSession(), aModel);
				aCol.mValueListener.endTransaction();
				aCol.mChangeLog.transactionEnded(event);
			}
		}
	}

	private static class ListenerCollection {

		public final ChangeLog mChangeLog;
		public final ProtegeChangeClassListener mClassListener;
		public final ProtegeChangeModelListener mModelListener;
		public final ProtegeChangePropertyListener mPropertyListener;
		public final ProtegeChangeResourceListener mResourceListener;
		public final ProtegeChangePropertyValueListener mValueListener;

		public ListenerCollection(OWLModel theModel) {
			final Converter aConverter = new Converter(CustomReasonerProjectPlugin
					.getOWLDataFactory());
			mChangeLog = new ChangeLog(aConverter, theModel);
			mClassListener = new ProtegeChangeClassListener(aConverter, mChangeLog);
			mModelListener = new ProtegeChangeModelListener(aConverter, mChangeLog);
			mPropertyListener = new ProtegeChangePropertyListener(aConverter, mChangeLog);
			mResourceListener = new ProtegeChangeResourceListener(aConverter, mChangeLog);
			mValueListener = new ProtegeChangePropertyValueListener(aConverter, mChangeLog,
					theModel);
		}

		public void dispose() {
			mChangeLog.dispose();
		}
	}

	private class SessionKBPair {
		private KnowledgeBase kb;
		private RemoteSession session;

		public SessionKBPair(RemoteSession session, KnowledgeBase kb) {
			this.session = session;
			this.kb = kb;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SessionKBPair) {
				SessionKBPair other = (SessionKBPair) obj;
				return this.kb.equals(other.kb) && this.session.equals(other.session);
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			return prime * session.hashCode() + kb.hashCode();
		}

		public boolean hasKnowledgeBase(KnowledgeBase kb) {
			return this.kb.equals(kb);
		}
	}

	private static class StandaloneSession implements RemoteSession {

		private static StandaloneSession mInstance = new StandaloneSession();

		public static StandaloneSession getInstance() {
			return mInstance;
		}

		private StandaloneSession() {
		}

		public boolean allowDelegation() {
			return false;
		}

		public int getId() {
			return 0;
		}

		public String getRealUserName() {
			return null;
		}

		public long getStartTime() {
			return 0;
		}

		public String getUserIpAddress() {
			return null;
		}

		public String getUserName() {
			return null;
		}

		public void setDelegate(String delegateUserName) {
			
		}



	}

	final private static Logger LOGGER = Log.getLogger(EventSessionDemuxer.class);

	final private DemuxingClsListener mClsListener;
	final private DemuxingFrameListener mFrameListener;
	final private DemuxingInstanceListener mInstanceListener;
	final private DemuxingKBListener mKBListener;
	final private Map<SessionKBPair, ListenerCollection> mSessionKBListenerMap;
	final private DemuxingSlotListener mSlotListener;
	final private DemuxingTransactionListener mTransactionListener;
	final private Set<KnowledgeBase> mRegisteredKbs;

	public EventSessionDemuxer() {
		mSessionKBListenerMap = new HashMap<SessionKBPair, ListenerCollection>();
		mClsListener = new DemuxingClsListener();
		mFrameListener = new DemuxingFrameListener();
		mKBListener = new DemuxingKBListener();
		mInstanceListener = new DemuxingInstanceListener();
		mSlotListener = new DemuxingSlotListener();
		mTransactionListener = new DemuxingTransactionListener();
		mRegisteredKbs = new HashSet<KnowledgeBase>();
	}

	public ChangeLog getChangeLog(RemoteSession theSession, OWLModel theModel) {
		return getListeners(theSession, theModel).mChangeLog;
	}

	private synchronized ListenerCollection getListeners(RemoteSession theSession, OWLModel theModel) {
		if (theSession == null) theSession = StandaloneSession.getInstance();
		final SessionKBPair aPair = new SessionKBPair(theSession, theModel);
		ListenerCollection aListenerCollection = mSessionKBListenerMap.get(aPair);
		if (aListenerCollection == null) {
			aListenerCollection = new ListenerCollection(theModel);
			mSessionKBListenerMap.put(aPair, aListenerCollection);
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Created new session+kb listeners for " + theSession + ":" + theModel);
			}
		}
		return aListenerCollection;
	}

	public boolean isRegistered(KnowledgeBase theKb) {
		return mRegisteredKbs.contains(theKb);
	}

	public void register(KnowledgeBase theKb) {
		if (isRegistered(theKb)) {
			return;
		}

		mRegisteredKbs.add(theKb);

		theKb.addClsListener(mClsListener);
		theKb.addFrameListener(mFrameListener);
		theKb.addKnowledgeBaseListener(mKBListener);
		theKb.addInstanceListener(mInstanceListener);
		theKb.addSlotListener(mSlotListener);
		theKb.addTransactionListener(mTransactionListener);
	}

	public synchronized void unregister(KnowledgeBase theKb) {
		if (!isRegistered(theKb)) {
			return;
		}

		mRegisteredKbs.remove(theKb);
		
		theKb.removeClsListener(mClsListener);
		theKb.removeFrameListener(mFrameListener);
		theKb.removeKnowledgeBaseListener(mKBListener);
		theKb.removeInstanceListener(mInstanceListener);
		theKb.removeSlotListener(mSlotListener);
		theKb.removeTransactionListener(mTransactionListener);
		SessionKBPair[] aView = mSessionKBListenerMap.keySet().toArray(new SessionKBPair[0]);
		for (SessionKBPair aPair : aView) {
			if (aPair.hasKnowledgeBase(theKb)) {
				ListenerCollection aListenerCollection = mSessionKBListenerMap.remove(aPair);
				aListenerCollection.dispose();
			}
		}
	}

}
