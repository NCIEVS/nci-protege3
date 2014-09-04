package com.clarkparsia.protege.reasoner;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.dig20.client.DigReasoner;
import com.clarkparsia.dig20.client.admin.DigClientAdmin;
import com.clarkparsia.protege.change.ChangeLog;
import com.clarkparsia.protege.change.listener.EventSessionDemuxer;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.AbstractProjectPlugin;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.inference.util.ReasonerPreferences;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerPluginMenuManager;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Title: An implementation of the Protege ProjectPlugin which overrides the default Protege OWL Reasoner with a custom,
 * Pellet-backed implementation of the ProtegeOWLReasoner interface.  Also registers a change logging service to listen
 * to a Protege KnowledgeBase (specifically an OWLModel) and track the changes made to the KB.  <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Oct 18, 2007 8:11:38 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 * @author Michael Smith
 * @see com.clarkparsia.protege.reasoner.CustomProtegeOWLReasoner
 */
public class CustomReasonerProjectPlugin extends AbstractProjectPlugin {
    private final static Logger LOGGER = Log.getLogger(CustomProtegeOWLReasoner.class);

    private final static OWLDataFactory FACTORY;
    private final static OWLOntologyManager MANAGER;

    private final static EventSessionDemuxer EVENT_SESSION_DEMUXER = new EventSessionDemuxer();
    private final static Set<Project> REGISTERED_PROJECTS = Collections.synchronizedSet(new HashSet<Project>());
    /**
     * Concurrent access guarded by synchronizing on {@link #DIG_REASONERS}
     */
    private final static Map<Project, DigReasoner> DIG_REASONERS;
    private final static Map<Project, DigClientAdmin> DIG_CLIENT_ADMINS;

    private final static String DIG_KBID_KEY;
    private final static String DIG_REASONER_URL_KEY;
	private final static String REASONER_CLASS_NAME_KEY;

	public static final String KEY_SYNCH = "com.clarkparsia.protege3.reasoner/synch";

	public static final String KEY_TRACK_CHANGES = "com.clarkparsia.protege3.reasoner/track-changes";

	public static final String VALUE_REALTIME = "realtime";

	public static final String VALUE_QUERY = "query";

	static {
        MANAGER = OWLManager.createOWLOntologyManager();
        FACTORY = MANAGER.getOWLDataFactory();

        DIG_KBID_KEY = "com.clarkparsia.protege3.dig20/kbid";
        DIG_REASONER_URL_KEY = "com.clarkparsia.protege3.dig20/reasoner-url";
		REASONER_CLASS_NAME_KEY = "com.clarkparsia.protege3.reasoner/class-name";

        DIG_REASONERS = new HashMap<Project, DigReasoner>();
        DIG_CLIENT_ADMINS = new HashMap<Project, DigClientAdmin>();
    }

    public CustomReasonerProjectPlugin() {
		LOGGER.info("Initializing C&P Custom Reasoner Project Plugin");
		
		//ReasonerManager.getInstance().setDefaultReasonerClass(CustomProtegeOWLReasoner.class.getCanonicalName());

		LOGGER.fine("C&P Custom Reasoner Project Plugin Initialization Completed");
	}

    public void afterCreate(Project theProject) {
        // do nothing
    }

    public void afterLoad(Project theProject) {
    	registerProjectIfNeeded(theProject);
    }

    public void afterSave(Project theProject) {
        // do nothing
    }

    public void afterShow(ProjectView theProjectView, ProjectToolBar projectToolBar,
			ProjectMenuBar projectMenuBar) {
		// This event is used because it occurs more reliably than afterCreate
		registerProjectIfNeeded(theProjectView.getProject());
	}

	public void beforeSave(Project theProject) {

		final KnowledgeBase aKB = theProject.getKnowledgeBase();
		if( aKB instanceof OWLModel ) {
			final OWLModel aModel = (OWLModel) aKB;

			/*
			 * Getting the DigReasoner verifies that the application reasoner
			 * URL has been set as Project client information
			 */
			getDigReasoner( theProject );

			ProtegeReasoner aReasoner = ReasonerManager.getInstance().getProtegeReasoner( aModel );
			if( aReasoner == null ) {
				theProject.setClientInformation( REASONER_CLASS_NAME_KEY,
						ReasonerPluginMenuManager.NONE_REASONER );
			}
			else {
				theProject.setClientInformation( REASONER_CLASS_NAME_KEY, aReasoner.getClass()
						.getName() );
			}
		}
	}

    public void beforeHide(ProjectView theProjectView, ProjectToolBar projectToolBar, ProjectMenuBar projectMenuBar) {
        // do nothing
    }

    public void beforeClose(Project theProject) {
        stopTrackingChanges(theProject);

        /*
         * The Protege Reasoner instance is removed from the reasoner map by the
         * Project Close event (initiated by project.dispose()) and shouldn't be
         * removed here.
         */

        synchronized( REGISTERED_PROJECTS ) {
            REGISTERED_PROJECTS.remove(theProject);
        }

        synchronized( DIG_REASONERS ) {
            DIG_REASONERS.remove(theProject);
        }
    }

	/**
	 * Track changes on the project
	 * @param theProject the project to track changes for
	 * @param theTrack true to start tracking changes, false to stop tracking changes
	 */
    public static void trackChanges(Project theProject, boolean theTrack) {
        if (theTrack) {
            startTrackingChanges(theProject);
        }
        else {
            stopTrackingChanges(theProject);
        }
    }

	/**
	 * Stop tracking changes for the given project
	 * @param theProject the project whose KB we should stop tracking the changes on
	 */
    private static void stopTrackingChanges(Project theProject) {
        final KnowledgeBase aKb = theProject.getKnowledgeBase();

		LOGGER.fine("Unregistering OWL Model with Session Demuxing Event Tracker");

        // Unregister the event listeners
        EVENT_SESSION_DEMUXER.unregister(aKb);
    }

	private static void startTrackingChanges(Project theProject) {
		final KnowledgeBase aKb = theProject.getKnowledgeBase();

		LOGGER.fine("Registering OWL Model with Session Demuxing Event Tracker");

		EVENT_SESSION_DEMUXER.register(aKb);
	}

	/**
	 * Return whether or not changes should be tracked for the kb in the given project
	 * @param theProject the project
	 * @return true if changes should be tracked, false otherwise
	 */
    private static boolean shouldTrackChanges(Project theProject) {
        Object aTrack = theProject.getClientInformation(KEY_TRACK_CHANGES);

		// we only want to track changes if they're enabled and we're either in stand alone mode, or the server in client/server
        return aTrack != null && ((Boolean) aTrack) && !theProject.isMultiUserClient();
    }

    private static void registerProjectIfNeeded(Project theProject) {

        synchronized( REGISTERED_PROJECTS ) {

            if (!REGISTERED_PROJECTS.contains(theProject)) {

                REGISTERED_PROJECTS.add(theProject);

                final KnowledgeBase aKb = theProject.getKnowledgeBase();

                if (aKb instanceof OWLModel) {
                    final OWLModel aOWLModel = (OWLModel) aKb;
                    final boolean aServer = theProject.isMultiUserServer();
                    final boolean aClient = theProject.isMultiUserClient();

                    if (!aServer) {
                        // Reasoner URL is project specific
                        Object aObject = theProject.getClientInformation(DIG_REASONER_URL_KEY);
                        if (aObject != null)
                            ReasonerPreferences.getInstance().setReasonerURL(aObject.toString());
                    }
                    else {
                        /*
                         * we need to force synch real time when this stuff is
                         * running on the server so we get the desired behavior
                         */
                        theProject.setClientInformation(KEY_SYNCH,
                                                        VALUE_REALTIME);

                        ServerFrameStore.requestEventDispatch(aOWLModel);
                    }

					// we want the current reasoner to be project specific.  so if we've got a value for it in the project
					// lets overwrite the global value, and set the reasoner if need be.
					Object aReasonerName = theProject.getClientInformation(REASONER_CLASS_NAME_KEY);
					if (aReasonerName != null) {
						ReasonerManager.getInstance().setDefaultReasonerClass(aReasonerName.toString());

						// set the current reasoner to the type specified in the project file if they are different
						ProtegeReasoner aReasoner = ReasonerManager.getInstance().getProtegeReasoner(aOWLModel);
						if (aReasoner != null && !aReasoner.getClass().getName().equals(aReasonerName.toString())) {
							ReasonerManager.getInstance().setProtegeReasonerClass(aOWLModel, aReasonerName.toString());
						}
						else if (aReasonerName.equals(ReasonerPluginMenuManager.NONE_REASONER)) {
							ReasonerManager.getInstance().setProtegeReasonerClass(aOWLModel, (String) null);
						}
					}

                    if (!aClient) {
                        // Force creation of a DIG KB Id if needed
                        getDigKbId(theProject);
                    }

                    if (shouldTrackChanges(theProject)) {
						startTrackingChanges(theProject);
                    }
                }
            }
        }
    }

    public static DigReasoner getDigReasoner(Project theProject) {

        synchronized( DIG_REASONERS ) {
            /*
             * Get a reasoner with the correct URL. If the project is
             * standalone, then the URL returned by ReasonerPreferences (and set
             * via the OWL preferences dialog) is authoritative and the project
             * specific setting tracks it. In multiuser mode, the reverse is
             * true (the setting in the server project is authoritative and
             * immutable).
             */

            final String aAppUrl = ReasonerPreferences.getInstance().getReasonerURL();
            final boolean isClient = theProject.isMultiUserClient();
            final boolean isServer = theProject.isMultiUserServer();

            DigReasoner aDigReasoner = DIG_REASONERS.get(theProject);

            if (aDigReasoner != null) {
                final String aCurrentURL = aDigReasoner.getServerURL().toString(); 
                if (!aAppUrl.equals(aCurrentURL)) {
                    if (isClient)
                        ReasonerPreferences.getInstance().setReasonerURL(aCurrentURL);
                    else if (!isServer)
                        aDigReasoner = null;
                }
            }

            if (aDigReasoner == null) {
                String aReasonerUrl;
                final Object aObj = theProject.getClientInformation(DIG_REASONER_URL_KEY);
                if (aObj == null) {
                    aReasonerUrl = aAppUrl;
                    if (!isServer && !isClient) {
                        theProject.setClientInformation(DIG_REASONER_URL_KEY, aAppUrl);
                        LOGGER.info("C&P Custom Reasoner URL initialized for project to: " + aAppUrl);
                    }
                }
                else {
                    final String aProjectUrl = aObj.toString();
                    if (!aAppUrl.equals(aProjectUrl)) {
                        if (isServer) {
                            aReasonerUrl = aProjectUrl;
                        }
                        else if (isClient) {
                            ReasonerPreferences.getInstance().setReasonerURL(aProjectUrl);
                            aReasonerUrl = aProjectUrl;
                        }
                        else {
                            theProject.setClientInformation(DIG_REASONER_URL_KEY, aAppUrl);
                            LOGGER.fine("Project setting for reasoner URL updated to: " + aAppUrl);

                            aReasonerUrl = aAppUrl;
                        }
                    }
                    else aReasonerUrl = aAppUrl;
                }
                URL aDigReasonerURL;
                try {
                    aDigReasonerURL = new URL(aReasonerUrl);
                }
                catch (MalformedURLException e) {
                    String aDefaultURL = ReasonerPreferences.getInstance().getDefaultReasonerURL();
                    LOGGER.warning(String.format(
                            "Reasoner url ('%s') is not a valid, falling back to default ('%s')",
                            aAppUrl, aDefaultURL));
                    try {
                        aDigReasonerURL = new URL(aDefaultURL);
                    }
                    catch (MalformedURLException f) {
                        LOGGER.severe(String.format("Fallback url ('%s') not valid, failing.",
                                aDefaultURL));
                        throw new RuntimeException(f);
                    }
                }
                aDigReasoner = new DigReasoner(aDigReasonerURL, getDigKbId(theProject), MANAGER);
                DIG_REASONERS.put(theProject, aDigReasoner);
            }

            return aDigReasoner;
        }

    }

    public static DigClientAdmin getDigClientAdmin(Project theProject) {
        synchronized( DIG_CLIENT_ADMINS ) {
            /*
             * Get a reasoner with the correct URL. If the project is
             * standalone, then the URL returned by ReasonerPreferences (and set
             * via the OWL preferences dialog) is authoritative and the project
             * specific setting tracks it. In multiuser mode, the reverse is
             * true (the setting in the server project is authoritative and
             * immutable).
             */

            final String aAppUrl = ReasonerPreferences.getInstance().getReasonerURL();
            final boolean isClient = theProject.isMultiUserClient();
            final boolean isServer = theProject.isMultiUserServer();

            DigClientAdmin aDigClientAdmin = DIG_CLIENT_ADMINS.get(theProject);

            if (aDigClientAdmin != null) {
                final String aCurrentURL = aDigClientAdmin.getReasonerURL().toString(); 
                if (!aAppUrl.equals(aCurrentURL)) {
                    if (isClient)
                        ReasonerPreferences.getInstance().setReasonerURL(aCurrentURL);
                    else if (!isServer)
                        aDigClientAdmin = null;
                }
            }

            if (aDigClientAdmin == null) {
                String aReasonerUrl;
                final Object aObj = theProject.getClientInformation(DIG_REASONER_URL_KEY);
                if (aObj == null) {
                    aReasonerUrl = aAppUrl;
                    if (!isServer && !isClient) {
                        theProject.setClientInformation(DIG_REASONER_URL_KEY, aAppUrl);
                        LOGGER.info("C&P Custom Reasoner URL initialized for project to: " + aAppUrl);
                    }
                }
                else {
                    final String aProjectUrl = aObj.toString();
                    if (!aAppUrl.equals(aProjectUrl)) {
                        if (isServer) {
                            aReasonerUrl = aProjectUrl;
                        }
                        else if (isClient) {
                            ReasonerPreferences.getInstance().setReasonerURL(aProjectUrl);
                            aReasonerUrl = aProjectUrl;
                        }
                        else {
                            theProject.setClientInformation(DIG_REASONER_URL_KEY, aAppUrl);
                            LOGGER.fine("Project setting for reasoner URL updated to: " + aAppUrl);

                            aReasonerUrl = aAppUrl;
                        }
                    }
                    else aReasonerUrl = aAppUrl;
                }
                URL aDigReasonerURL;
                try {
                    aDigReasonerURL = new URL(aReasonerUrl);
                }
                catch (MalformedURLException e) {
                    String aDefaultURL = ReasonerPreferences.getInstance().getDefaultReasonerURL();
                    LOGGER.warning(String.format(
                            "Reasoner url ('%s') is not a valid, falling back to default ('%s')",
                            aAppUrl, aDefaultURL));
                    try {
                        aDigReasonerURL = new URL(aDefaultURL);
                    }
                    catch (MalformedURLException f) {
                        LOGGER.severe(String.format("Fallback url ('%s') not valid, failing.",
                                aDefaultURL));
                        throw new RuntimeException(f);
                    }
                }
                aDigClientAdmin = new DigClientAdmin(aDigReasonerURL);
                DIG_CLIENT_ADMINS.put(theProject, aDigClientAdmin);
            }

            return aDigClientAdmin;
        }
    }

    
    public static URI getDigKbId(Project theProject) {
        Object o = theProject.getClientInformation(DIG_KBID_KEY);
        URI aKbId = null;
        if (o != null) {
            try {
                aKbId = new URI(o.toString());
                if (!aKbId.isAbsolute()) {
                    LOGGER.warning("Ignoring DIG KB identifier that is not an absolute URI: "
                            + o.toString());
                    aKbId = null;
                }
            }
            catch (URISyntaxException e) {
                LOGGER.warning("Ignoring DIG KB identifier that is not an absolute URI: "
                        + o.toString());
            }
        }
        if (aKbId == null) {
            aKbId = URI.create(String.format("tag:clarkparsia.com,2009:protege3:dig20:kbid/%s",
                    UUID.randomUUID()));
            LOGGER.info("No DIG KB identifier found for project, using: " + aKbId);
            theProject.setClientInformation(DIG_KBID_KEY, aKbId.toASCIIString());
        }

        return aKbId;
    }

    /**
     * Return the OWLDataFactory used by the reasoner
     * @return the reasoner's OWLDataFactory
     */
    public static OWLDataFactory getOWLDataFactory() {
        return FACTORY;
    }

    /**
     * Return the OWLOntologyManager used by the reasoner
     * @return the reasoner's OWLOntologyManager
     */
    public static OWLOntologyManager getOWLOntologyManager() {
        return MANAGER;
    }

    /**
     * Return a ChangeLog associated with the specified OWLModel
     * @param theSession
     * @param theModel the OWLModel you wish to get a ChangeLog for
     * @return a ChangeLog for the specified OWLModel.
     */
    public static ChangeLog getChangeLog(RemoteSession theSession, OWLModel theModel) {
        return EVENT_SESSION_DEMUXER.getChangeLog(theSession, theModel);
    }
}
