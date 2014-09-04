package com.clarkparsia.dig20.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLRuntimeException;
import org.xml.sax.SAXException;

import com.clarkparsia.dig20.HTTPConstants;
import com.clarkparsia.dig20.QueryProcessor;
import com.clarkparsia.dig20.RetractAxioms;
import com.clarkparsia.dig20.TellAxioms;
import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.DigClientHttpErrorException;
import com.clarkparsia.dig20.exceptions.DigClientPostIOException;
import com.clarkparsia.dig20.exceptions.DigClientResponseIOException;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.DigRenderer;
import com.clarkparsia.dig20.xml.DigVocabulary;
import com.clarkparsia.dig20.xml.ResponsesDefaultHandler;

public class DefaultDigClient implements QueryProcessor {

	private static final Logger	log	= Logger.getLogger( DefaultDigClient.class.getName() );
	private HttpURLConnection	connection;

	private SAXParser			parser;
	private DigRenderer			renderer;
	private URL					server;

	public DefaultDigClient(URL server) {
		if( server == null )
			throw new NullPointerException();

		this.server = server;
		renderer = new DigRenderer();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );
		try {
			parser = factory.newSAXParser();
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public synchronized Collection<Response> applyChanges(Collection<TellAxioms> additions,
			Collection<RetractAxioms> removals) throws DigClientException {

		if( additions == null )
			throw new NullPointerException();
		if( removals == null )
			throw new NullPointerException();
		if( additions.isEmpty() && removals.isEmpty() )
			throw new IllegalArgumentException();

		// POST
		try {

			initHttpPost();

			OutputStreamWriter toServer = new OutputStreamWriter( connection.getOutputStream() );

			try {

				renderer.setDocumentTag( DigVocabulary.DIG.toString() );
				renderer.startRendering( toServer );
				for( TellAxioms t : additions )
					renderer.renderAxioms( t.getId(), t.getURI(), t.getAxioms() );
				for( RetractAxioms r : removals )
					renderer.renderRetractions( r.getId(), r.getURI(), r.getAxioms() );
				renderer.endRendering();

			} catch( OWLException e ) {
				log.log( Level.SEVERE, "Rethrowing OWLException thrown during rendering", e );
				throw new OWLRuntimeException( e );
			}

			toServer.close();

		} catch( ConnectException e ) {
			log.log( Level.WARNING, String.format( "Failed to connect to %s : %s", server, e
					.getMessage() ) );
			throw new DigClientPostIOException( e );
		} catch( IOException e ) {
			log.log( Level.WARNING, "I/O Exception caught during while attempting POST: "
					+ e.getMessage() );
			throw new DigClientPostIOException( e );
		}

		// RESPONSE
		try {
			final int responseCode = connection.getResponseCode();
			if( responseCode != HttpURLConnection.HTTP_OK )
				throwHttpException( responseCode );

			final InputStream fromServer = connection.getInputStream();
			Collection<Response> responses = parseResponses( fromServer );
			fromServer.close();

			if( responses.size() != (additions.size() + removals.size()) )
				throw new DigClientException( String.format(
						"Number of responses (%d) should match number of directives (%d)",
						responses.size(), additions.size() + removals.size() ) );

			return responses;

		} catch( IOException e ) {
			log.log( Level.WARNING, "I/O Exception caught during while gettting response: "
					+ e.getMessage() );
			throw new DigClientResponseIOException( e );
		}
	}

	public Response getResponse(URI kbURI, AskQuery q) {
		// FIXME: Find a way to avoid wrapping exceptions as RuntimeExceptions
		try {
			Collection<Response> responses = getResponses( kbURI, Collections.singleton( q ) );
			if( responses == null )
				return null;

			final Response r = responses.iterator().next();

			if( !q.getId().equals( r.getId() ) ) {
				final String msg = String.format(
						"Dig response id (%s), does not match query (%s)", r.getId(), q.getId() );
				log.log( Level.SEVERE, msg );
				throw new RuntimeException( new DigClientException( msg ) );
			}

			return r;

		} catch( DigClientException e ) {
			Throwable cause = e.getCause();
			if( cause == null ) {
				throw new RuntimeException( e );
			}
			else {
				if( cause instanceof RuntimeException )
					throw (RuntimeException) cause;
				else
					throw new RuntimeException( cause );
			}
		}
	}

	public synchronized Collection<Response> getResponses(URI kbURI,
			Collection<? extends AskQuery> queries) throws DigClientException {

		// POST
		try {

			initHttpPost();

			OutputStreamWriter toServer = new OutputStreamWriter( connection.getOutputStream() );

			try {

				renderer.setDocumentTag( DigVocabulary.DIG.toString() );
				renderer.startRendering( toServer );
				renderer.render( kbURI, queries );
				renderer.endRendering();

			} catch( OWLException e ) {
				log.log( Level.SEVERE, "Rethrowing OWLException thrown during rendering", e );
				throw new OWLRuntimeException( e );
			}

			toServer.close();
		} catch( ConnectException e ) {
			log.log( Level.WARNING, String.format( "Failed to connect to %s: %s", server, e
					.getMessage() ) );
			throw new DigClientPostIOException( e );
		} catch( IOException e ) {
			log.log( Level.WARNING, "IO exception querying server: " + e.getMessage() );
			throw new DigClientPostIOException( e );
		}

		// RESPONSE
		try {
			int responseCode = connection.getResponseCode();
			if( responseCode != HttpURLConnection.HTTP_OK )
				throwHttpException( responseCode );

			final InputStream fromServer = connection.getInputStream();
			Collection<Response> responses = parseResponses( fromServer );
			fromServer.close();

			if( responses.size() != queries.size() )
				throw new DigClientException( String.format(
						"Number of responses (%d) should match number of queries(%d)", responses
								.size(), queries.size() ) );

			return responses;

		} catch( IOException e ) {
			log.log( Level.WARNING, "IO exception querying server: " + e.getMessage() );
			throw new DigClientResponseIOException( e );
		}
	}

	public synchronized URL getServerURL() {
		return server;
	}

	private void initHttpPost() throws IOException {
		connection = (HttpURLConnection) server.openConnection();

		/*
		 * Post request
		 */
		connection.setRequestMethod( "POST" );
		connection.setRequestProperty( "Content-Type", HTTPConstants.REQUEST_CONTENT_TYPE );
		connection.setDoInput( true );
		connection.setDoOutput( true );
		connection.connect();
	}

	private Collection<Response> parseResponses(InputStream stream) throws IOException,
			DigClientException {
		try {
			ResponsesDefaultHandler handler = new ResponsesDefaultHandler();
			parser.parse( stream, handler );

			return handler.getResponses();

		} catch( SAXException e ) {
			log.log( Level.SEVERE, "Error parsing response from server", e );
			throw new DigClientException( e );
		}
	}

	public synchronized void setServerURL(URL theURL) {
		server = theURL;
	}

	private void throwHttpException(int responseCode) throws IOException,
			DigClientHttpErrorException {
		InputStream errorStream = connection.getErrorStream();
		String errorDump;
		if( errorStream == null )
			errorDump = "No error content";
		else {
			BufferedReader br = new BufferedReader( new InputStreamReader( errorStream ) );
			StringBuffer sbuf = new StringBuffer();
			char buf[] = new char[1024];
			int nread;
			while( (nread = br.read( buf )) > 0 ) {
				sbuf.append( buf, 0, nread );
			}
			br.close();
			errorStream.close();
			errorDump = (sbuf.length() > 0)
				? sbuf.toString()
				: "No error content";
		}
		final String msg = "HTTP Post failed. Code: " + responseCode + " Message: "
				+ connection.getResponseMessage() + " Content: " + errorDump;
		log.warning( msg );
		throw new DigClientHttpErrorException( msg,
				(responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) );
	}
}
