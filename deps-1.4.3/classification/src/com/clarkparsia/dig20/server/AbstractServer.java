package com.clarkparsia.dig20.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.semanticweb.owl.model.OWLOntology;

import com.clarkparsia.dig20.server.admin.InfoResponse;
import com.clarkparsia.dig20.server.admin.InfoResponseProperty;

/**
 * <p>
 * Title: AbstractServer
 * </p>
 * <p>
 * Description: Server side of an http client/server based explanation
 * generator.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public abstract class AbstractServer {

	public static final int			DEFAULT_PORT;
	protected static final Logger	log;
	private static final String		ROOT_PATH;

	static {
		log = Logger.getLogger( AbstractServer.class.getName() );

		DEFAULT_PORT = 18080;
		ROOT_PATH = "/";
	}

	protected DigRequestHandler		handler			    = new DigRequestHandler();
	private URI						kbURI			    = null;
	private int						port			    = DEFAULT_PORT;
	private HttpServer				server			    = new HttpServer();
	private DateFormat				dateFormat 		    = SimpleDateFormat.getInstance(); 
	private Collection<String>      ignoredImports      = Collections.emptyList();
	private boolean					loadImportsTogether = false;
	private PersistenceManager		persistenceManager 	= null;

	private long 					startTime;
	
	public void freshKbURI() {
		UUID uuid = UUID.randomUUID();
		setKbURI( URI.create( String.format( "tag:clarkparsia.com,2009:dig20:server:kbid/%s", uuid
				.toString() ) ) );
	}

	public URI getKbURI() {
		return kbURI;
	}

	public int getPort() {
		return this.port;
	}
		
	public void setPersistenceManager( PersistenceManager persistenceManager ) {
		this.persistenceManager = persistenceManager;
		handler.setPersistenceManager( persistenceManager );
	}
	
	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}
	
	public void saveClassifier( OutputStream outputStream ) throws IOException {
		handler.saveClassifier( outputStream );
	}
		
	public abstract URI getRootOntologyURI();
	
	protected abstract void load(Map<String, String> theLoadParameters);

	protected abstract void loadData();

	public abstract void reload();

	public abstract Date getLastReloadTime();
	
	public void setMultiThreadedClassification( boolean multiThreadedClassification ) {
		handler.setMultiThreadedClassification( multiThreadedClassification );
	}
	
	public boolean isMultiThreadedClassification() {
		return handler.isMultiThreadedClassification();
	}
	
	public void run() {		
		loadData();

		handler.prepareTaxonomy();

		if( persistenceManager != null ) {
			if ( persistenceManager.isPersisting() ) {
				persistenceManager.start();
			}
			
			// at this point we need to turn off the no-sync flag -- it will prevent any reload requests
			persistenceManager.setNoOntologySync( false );
		}
		
		start();
	}
	
	public Collection<String> getIgnoredImports() {
		return Collections.unmodifiableCollection( ignoredImports );
	}
	
	public void setIgnoredImports( Collection<String> theIgnoredImports ) {
		ignoredImports = new ArrayList<String>( theIgnoredImports );
	}

	public void setKbURI(URI kbURI) {
		this.kbURI = kbURI;
	}

	public void setPort(int port) {
		this.port = port;
	}

	protected void shutdown() {
		System.out.println( "Server shutting down..." );
		
		if( ( getPersistenceManager() != null ) && ( getPersistenceManager().isPersisting() ) ) {
			persist();
			getPersistenceManager().stop();			
		}
		
		stop();

		System.exit( 0 );
	}

	public void start() {
		startTime = System.currentTimeMillis();
		// HTTP setup

		SocketListener listener = new SocketListener();
		listener.setPort( port );
		server.addListener( listener );

		HttpContext aExplainContext = server.addContext( ROOT_PATH + "explain" );
		aExplainContext.addHandler( handler );

		HttpContext aAdminContext = server.addContext( ROOT_PATH + "admin" );
		aAdminContext.addHandler( new AdminHttpHandler( this ) );

		try {
			server.start();
		} catch( Exception e ) {
			log.log( Level.SEVERE, "Unable to start server", e );
			throw new RuntimeException( e );
		}		
	}
	
	public void stop() {
		try {
			server.stop();
			System.out.println("Server stopped on " + dateFormat.format(new Date()));
		} catch( Exception e ) {
			log.log( Level.SEVERE, "Unable to stop server", e );
			throw new RuntimeException( e );
		}
	}
	
	private String getRunTime() {
		long runTime = System.currentTimeMillis() - startTime;
		
		return formatDuration(runTime);
	}
	
	private static String formatDuration(long duration) {
		long days = duration / (24 * 3600 * 1000);
		
		duration -= (days * 24 * 3600 * 1000);
				
		long hours = duration / (3600 * 1000);
		
		duration -= (hours * 3600 * 1000);
		
		long minutes = duration / (60 * 1000);
		
		duration -= minutes * 60 * 1000;
		
		long seconds = duration / 1000;
	
		duration -= seconds * 1000;
		
		return String.format("%d days %dh %dm %ds %d ms", days, hours, minutes, seconds, duration);		
	}
	
	public String getStartTime() {
		return dateFormat.format(new Date(startTime));	
	}
	
	private int getOntologyCount() {		
		return handler.getLoadedOntologies().size();
	}
	
	private String getOntologies() {
		StringBuffer result = new StringBuffer();
		
		boolean first = true;
		for(OWLOntology ontology : handler.getLoadedOntologies()) {
			if (!first) {
				result.append(", ");
			}
			
			result.append(ontology.getURI());			
			first = false;
		}
		
		return result.toString();
	}
	
	private int getAxiomCount() {
		int result = 0;
		for(OWLOntology ontology : handler.getLoadedOntologies()) {
			result += ontology.getAxiomCount();
		}		
		
		return result;
	}

	private int getLogicalAxiomCount() {
		int result = 0;
		for(OWLOntology ontology : handler.getLoadedOntologies()) {
			result += ontology.getLogicalAxiomCount();
		}		
		
		return result;
	}
	
	private int getClassAxiomCount() {
		int result = 0;
		for(OWLOntology ontology : handler.getLoadedOntologies()) {
			result += ontology.getClassAxioms().size();
		}		
		
		return result;
	}
	
	private String getHostName() {
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			
			return localAddress.getCanonicalHostName();			
		} catch (UnknownHostException e) {
			return "UKNOWN";
		}
	}

	public void setLoadImportsTogether(boolean loadImportsTogether) {
		this.loadImportsTogether = loadImportsTogether;
	}

	public boolean isLoadImportsTogether() {
		return loadImportsTogether;
	}
	
	public InfoResponse info() {
		InfoResponse response = new InfoResponse();
		
		response.setProperty(InfoResponseProperty.START_TIME.toString(), getStartTime());
		response.setProperty(InfoResponseProperty.RUN_TIME.toString(), getRunTime()); 
		
		if (server.getListeners().length > 0) {
			response.setProperty(InfoResponseProperty.HOST.toString(), getHostName());
			response.setProperty(InfoResponseProperty.PORT.toString(), String.valueOf(this.port));
		}
		
		response.setProperty(InfoResponseProperty.ONTOLOGY_COUNT.toString(), String.valueOf(getOntologyCount()));
		response.setProperty(InfoResponseProperty.ONTOLOGIES.toString(), getOntologies());
		response.setProperty(InfoResponseProperty.AXIOMS.toString(), String.valueOf(getAxiomCount()));
		response.setProperty(InfoResponseProperty.LOGICAL_AXIOMS.toString(), String.valueOf(getLogicalAxiomCount()));
		response.setProperty(InfoResponseProperty.CLASS_AXIOMS.toString(), String.valueOf(getClassAxiomCount()));
		response.setProperty(InfoResponseProperty.REGULAR_CLASSIFICATION_TIME.toString(), String.valueOf(handler.getClassificationTimer("regularClassify")));
		response.setProperty(InfoResponseProperty.INCREMENTAL_CLASSIFICATION_TIME.toString(), String.valueOf(handler.getClassificationTimer("incrementalClassify")));
		response.setProperty(InfoResponseProperty.TOTAL_CLASSIFICATION_TIME.toString(), String.valueOf(handler.getClassificationTimer("regularClassify") + handler.getClassificationTimer("incrementalClassify")));
		
		URI kbURI = getKbURI();
		
		if (kbURI == null) {
			response.setProperty(InfoResponseProperty.SERVER_KB_URI.toString(), "NONE");
		} else {
			response.setProperty(InfoResponseProperty.SERVER_KB_URI.toString(), getKbURI().toString());
		}
		
		Date lastReloadTime = getLastReloadTime();
		
		if (lastReloadTime == null) {
			response.setProperty(InfoResponseProperty.LAST_RELOAD_TIME.toString(), "NEVER");
		} else {
			response.setProperty(InfoResponseProperty.LAST_RELOAD_TIME.toString(), dateFormat.format(lastReloadTime));
		}
		
		if (getPersistenceManager() != null) {
			long lastPersistTime = getPersistenceManager().getLastPersistFinishTime();									
			long lastPersistDuration = getPersistenceManager().getLastPersistDuration();
			boolean currentStatePersisted = getPersistenceManager().isCurrentStatePersisted();
			
			if (lastPersistTime < 0) {
				response.setProperty(InfoResponseProperty.LAST_PERSIST_TIME.toString(), "NEVER");
				response.setProperty(InfoResponseProperty.LAST_PERSIST_DURATION.toString(), "N/A");
			} else {
				response.setProperty(InfoResponseProperty.LAST_PERSIST_TIME.toString(), dateFormat.format(new Date(lastPersistTime)));
				response.setProperty(InfoResponseProperty.LAST_PERSIST_DURATION.toString(), String.valueOf(lastPersistDuration));
			}
			
			response.setProperty(InfoResponseProperty.CURRENT_STATE_PERSISTED.toString(), String.valueOf(currentStatePersisted));
		} else {
			response.setProperty(InfoResponseProperty.LAST_PERSIST_TIME.toString(), "Server in non-persisting mode");
			response.setProperty(InfoResponseProperty.LAST_PERSIST_DURATION.toString(), "Server in non-persisting mode");
			response.setProperty(InfoResponseProperty.CURRENT_STATE_PERSISTED.toString(), "false");
		}
		
		return response;
	}
	
	public void persist() {
		if (getPersistenceManager() != null) {
			getPersistenceManager().requestPersist();
		}
	}
}
