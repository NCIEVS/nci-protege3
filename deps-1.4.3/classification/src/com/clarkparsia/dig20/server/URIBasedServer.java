package com.clarkparsia.dig20.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

/**
 * <p>
 * Title: URIBasedServer
 * </p>
 * <p>
 * Description: Server implementation that loads data from OWL ontologies specified by URI
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class URIBasedServer extends AbstractServer {

	private List<URI>	ontologyURIs	= new ArrayList<URI>();

	public URIBasedServer(Collection<URI> ontologyURIs) {
		this.ontologyURIs = new ArrayList<URI>( ontologyURIs );
	}

	protected void load(Map<String, String> theArgs) {
		handler.clear();

		ontologyURIs.clear();

		String[] aURIArray = theArgs.get( "url" ).split( "," );
		for( String aURI : aURIArray ) {
			ontologyURIs.add( URI.create( aURI.trim() ) );
		}

		loadData();
	}
	
	private Date lastReloadTime = null;
	
	public Date getLastReloadTime() {
		return lastReloadTime;
	}

	public void reload() {
		handler.clear();

		loadData();
	}

	public URI getRootOntologyURI() {
		if( !ontologyURIs.isEmpty() ) {
			return ontologyURIs.iterator().next();
		}
		
		return null;
	}
	
	protected void loadData() {

		if( ontologyURIs.isEmpty() )
			return;

		URI kbURI = getKbURI();
		if( kbURI == null ) {
			freshKbURI();
			kbURI = getKbURI();
			System.err.println( "***Fresh server KB identified by URI: " + kbURI );
		}

		if( log.isLoggable( Level.FINE ) )
			log.fine( "Initializing KB identified by URI: " + kbURI );

		if( getPersistenceManager() != null) {
			getPersistenceManager().setCurrentStatePersisted( false );
		}
		
		try {
			for( URI u : ontologyURIs ) {
				OWLOntology ontology = null;
				
				if ( ( getPersistenceManager() == null ) || ( !getPersistenceManager().isNoOntologySync() ) ) {
					ontology = handler.getOntologyManager().loadOntology( u );
					lastReloadTime = new Date();
				}
				
				handler.loadOntology( ontology, kbURI );
			}
		} catch( OWLException e ) {
			log.log( Level.SEVERE, "OWL exception caught while trying to load data, rethrowing", e );
			throw new RuntimeException( e );
		}		
	}
}