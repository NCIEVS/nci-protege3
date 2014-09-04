package com.clarkparsia.dig20.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLOntology;

import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.impl.AbstractOWLModel;

/**
 * <p>
 * Title: AbstractProtegeBasedServer
 * </p>
 * <p>
 * Description: Common implementation for all Protege based explanation server
 * implementations
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
public abstract class AbstractProtegeBasedServer extends AbstractServer {

	private static final String		PROTEGE_PROJECT_KBID_KEY;
	protected static final Logger	log;

	static {
		log = Logger.getLogger( AbstractProtegeBasedServer.class.getCanonicalName() );

		PROTEGE_PROJECT_KBID_KEY = "com.clarkparsia.protege3.dig20/kbid";
	}
	
	private URI rootOntologyURI;

	public AbstractProtegeBasedServer() {
		super();
	}

	public URI getRootOntologyURI() {
		return rootOntologyURI;
	}
	
	abstract protected Project getProject();

	@Override
	protected void loadData() {
		reload();
	}
	
	private Date lastReloadTime = null;
	
	@Override
	public Date getLastReloadTime() {
		return lastReloadTime;
	}

	@Override
	public void reload() {
		handler.clear();
		
		Project p = getProject();
		if( p == null ) {
			log.warning( "No Protege project loaded.  Continuing with empty dataset." );
			return;
		}
				
		URI kbURI = getKbURI();
		if( kbURI == null ) {
			Object o = p.getClientInformation( PROTEGE_PROJECT_KBID_KEY );
			if( o != null ) {
				try {
					kbURI = new URI( o.toString() );
					if( !kbURI.isAbsolute() ) {
						log.warning( "Ignoring kbid from Project. Not valid absolute URI: " + o );
						kbURI = null;
					}
				} catch( URISyntaxException e ) {
					log.warning( "Ignoring kbid from Project. Not valid URI: " + o );
				}
			}

			if( kbURI == null ) {
				freshKbURI();
				kbURI = getKbURI();
				System.err.println( "*** Fresh server KB identified by URI: " + kbURI );
			}
			else {
				setKbURI( kbURI );
			}
		}
		
		if( log.isLoggable( Level.FINE ) )
			log.fine( "Initializing KB identified by URI: " + kbURI );


		AbstractOWLModel protegeModel;
		{
			KnowledgeBase kb = p.getKnowledgeBase();
			if( !(kb instanceof AbstractOWLModel) ) {
				final String msg = "Protege project not an OWL project";
				log.severe( msg );
				throw new IllegalArgumentException( msg );
			}
			protegeModel = (AbstractOWLModel) kb;
			
			try {
				rootOntologyURI = new URI( protegeModel.getTripleStoreModel().getTopTripleStore().getName() );
			} catch (URISyntaxException e) {
				final String msg = "The root ontology URI is not a proper URI " + protegeModel.getTripleStoreModel().getTopTripleStore().getName(); 
				log.severe( msg );
				throw new IllegalArgumentException( msg );
			}
		}
		
		OWLOntology ontology = null;
		
		if ( ( getPersistenceManager() == null ) || ( !getPersistenceManager().isNoOntologySync() ) ) {
			ontology = createOntology(protegeModel);
			lastReloadTime = new Date();
		}		
		
		p.dispose();
		p = null;
		protegeModel = null;
		
		if( getPersistenceManager() != null ) {
			getPersistenceManager().setCurrentStatePersisted( false );
		}
		
		// loading of ontology should be after the call to setCurrentSetPersisted( false )
		// because the load may decide that the ontology did not changed, and therefore will reset the current state persisted to true
		handler.loadOntology( ontology, kbURI );						
	}
	
	protected OWLOntology createOntology(final OWLModel protegeModel) {
		Set<String> importsToIgnore = new HashSet<String>( getIgnoredImports() );

		List<String> ontologiesToLoad = new ArrayList<String>();
		List<String> ontologiesToIgnore = new ArrayList<String>();
		
		MergingNarrowFrameStore mnfs = MergingNarrowFrameStore.get( protegeModel );
		for( NarrowFrameStore nfs : mnfs.getAvailableFrameStores() ) {
			if( importsToIgnore.remove( nfs.getName() ) ) {				
				mnfs.removeFrameStore( nfs );
				ontologiesToIgnore.add( nfs.getName() );
			}
			else {
				ontologiesToLoad.add( nfs.getName() );
			}
		}
		
		if( !ontologiesToLoad.isEmpty() ) {
			System.err.println( "Explanation server will load the following "
					+ ontologiesToLoad.size() + " ontologies: " );
			for( String ont : ontologiesToLoad ) {
				System.err.println( ont );
			}
		}
		
		if( !ontologiesToIgnore.isEmpty() ) {
			System.err.println( "Explanation server will ignore the following "
					+ ontologiesToIgnore.size() + " imports: " );
			for( String ont : ontologiesToIgnore ) {
				System.err.println( ont );
			}
		}				
		
		OWLOntology ontology = null;
		try {
			ImportOptimizedConverter converter = new ImportOptimizedConverter( protegeModel, handler.getOntologyManager() );	
			converter.setLoadImportsSeparately( isLoadImportsTogether() );
			ontology = converter.convert();
			converter = null;
		} catch (ConversionException e) {
			if ( log.isLoggable( Level.SEVERE ) ) {
				log.log( Level.SEVERE, "Error during the conversion of the Protege model into an OWLOntology: ", e );
				throw new RuntimeException( e );
			}
			
			e.printStackTrace();
		}
		
		return ontology;
	}
}