package com.clarkparsia.dig20.client.async;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChange;

import com.clarkparsia.dig20.client.DefaultDigClient;
import com.clarkparsia.dig20.client.DigReasoner;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.DigClientHttpErrorException;
import com.clarkparsia.dig20.exceptions.DigClientPostIOException;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;

public class DigOntologyChangeOperation extends AbstractDigAsynchronousOperation implements
		DigAsynchronousOperation {

	private static final Logger			log	= Logger.getLogger( DefaultDigClient.class.getName() );

	final private static int			RETRY_DELAY_MS;
	static {
		RETRY_DELAY_MS = 5000;
	}

	final private OWLOntologyChange[]	changes;

	public DigOntologyChangeOperation(DigAsynchronousOperationListener listener,
			DigReasoner digReasoner, Collection<? extends OWLOntologyChange> changes) {
		super( listener, digReasoner );
		this.changes = changes.toArray( new OWLOntologyChange[0] );
	}

	public void accept(DigAsynchronousOperationVisitor visitor) {
		visitor.visit( this );
	}

	public Collection<? extends OWLOntologyChange> getChanges() {
		return Collections.unmodifiableList( Arrays.asList( changes ) );
	}

	public void run() {

		DigClientException digException = null;
		ErrorResponseException errorException = null;
		boolean success = false;
		boolean failure = false;

		while( !success && !failure ) {

			try {
				synchronized( digReasoner ) {
					digReasoner.ontologiesChanged( Arrays.asList( changes ) );
					digReasoner.synchronize();
					success = true;
				}
			} catch( DigClientPostIOException e ) {
				log.log( Level.WARNING, "Network I/O error.", e.getCause() );
			} catch( DigClientHttpErrorException e ) {
				if( e.getRetry() ) {
					log.log( Level.WARNING, "Recoverable HTTP error", e );
				}
				else {
					log.log( Level.SEVERE, "Non-recoverable HTTP error", e );
					digException = e;
					failure = true;
				}
			} catch( DigClientException e ) {
				log.log( Level.SEVERE, "Non-recoverable error", e );
				digException = e;
				failure = true;
			} catch( ErrorResponseException e ) {
				log.log( Level.SEVERE, "Error response to ontology changes", e );
				errorException = e;
				failure = true;
			}
			catch( OWLException e ) {
				log.log( Level.SEVERE, "Non-recoverable, unexpected error", e );
				failure = true;
			}

			if( !success && !failure ) {
				try {
					log.info( "Delaying " + RETRY_DELAY_MS + " ms, then retrying" );
					Thread.sleep( RETRY_DELAY_MS );
				} catch( InterruptedException intE ) {
					log.log( Level.INFO, "Unexpected thread interruption, continuing without delay", intE );
				}
			}

		}

		if( failure ) {
			if( digException != null )
				notifyFailure( digException );
			else if( errorException != null )
				notifyFailure( errorException );
			else
				throw new RuntimeException();
		}
		else
			notifySuccess();
	}
}
