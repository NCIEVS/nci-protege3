package com.clarkparsia.dig20.server;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.coode.xml.XMLWriterPreferences;
import org.mindswap.pellet.PelletOptions;

public class Server {

	private static final int IGNORE_INVERSES = -2;
	private static final int LOAD_IMPORTS_TOGETHER = -3;
	
	private enum DataSource {
		URI, PROTEGE_STANDALONE, PROTEGE_MULTIUSER
	}

	private static final Logger	log	= Logger.getLogger( Server.class.getCanonicalName() );

	public static void printUsage(PrintStream s) {
		s.println( "Usage:" );
		s.println( "\tServer"
				+ " [global options] --ontology-uri ontology-uri ontology-uri ..." );
		s.println( "\tServer"
				+ " [global options] --protege-standalone project-file.pprj" );
		s.println( "\tServer"
				+ " [global options] --protege-multiuser project-name" );

		s.println( "Global Options:" );

		s.println( "\t-h, --help" );
		s.println( "\t\tPrints this usage message" );

		s.println( "\t-p port, --port port" );
		s.println( "\t\tSets the port the server should listen on" );

		s.println( "\t-i kburi, --kbid kburi" );
		s.println( "\t\tSets the URI used to identify the KB." );
		s.println( "\t\tNote: Argument must be a valid absolute URI" );

		s.println( "Data Source Flags" );

		s.println( "\t-O, --ontology-uri" );
		s.println( "\t\tData will be read from ontologies specified by URI" );

		s.println( "\t-S, --protege-standalone" );
		s.println( "\t\tData will be read from the specified Protege project file" );

		s.println( "\t-M, --protege-multiuser" );
		s.println( "\t\tData will be read from the specified Protege server with\n" +
				"\t\tthe specified project name" );

		s.println( "Protege MultiUser Options" );

		s.println( "\t-s hostname, --hostname hostname" );
		s.println( "\t\tProtege Server hostname (defaults to "
				+ ProtegeMultiUserBasedServer.DEFAULT_PROTEGE_SERVER_HOST + ")" );

		s.println( "\t-x password, --password password" );
		s.println( "\t\tProtege Client password (defaults to "
				+ ProtegeMultiUserBasedServer.DEFAULT_PROTEGE_PASSWORD + ")" );

		s.println( "\t-u username, --user username" );
		s.println( "\t\tProtege Client username (defaults to "
				+ ProtegeMultiUserBasedServer.DEFAULT_PROTEGE_USER + ")" );
		
		s.println( "\t--ignore-inverses" );
		s.println( "\t\tCauses the reasoner to ignore any inverse property axioms\n" +
				"\t\t(WARNING: reasoning results may be incomplete)" );
		
		s.println( "\t-I URIs, --ignore-imports URIs" );
		s.println( "\t\tComma-separated URIs of imported projects, which will be\n" +
				"\t\tignored and not loaded to the reasoner" );
		
		s.println( "\t-m, --multi-threaded" );
		s.println( "\t\tTurns on multi-threaded classification (disabled by default)" );
		
		s.println( "\t--load-imports-together" );
		s.println( "\t\tCauses the reasoner to load the imported ontologies together\n" +
				"\t\twith the base ontology. The default behavior is to load each\n" +
				"\t\timport separately which is significantly more efficient." );
		
		s.println( "\t-P, --persistence=[interval in hours]" );
		s.println( "\t\tTurns on persistence; the internal state of the classifier\n" +
				"\t\twill be saved after ontology changes, but not more frequently\n" +
				"\t\than the time interval (default 1 hour)" );
		
		s.println( "\t-r, --restore" );
		s.println( "\t\tAttempts to restore the state of the explanation server from\n" +
				"\t\ta previously saved session" );
		
		s.println( "\t-N, --no-ontology-sync" );
		s.println( "\t\tCan only be used when -t or --restore is used. It prevents\n" +
				"\t\tsynchronizing the ontology in the saved session with the current\n" +
				"\t\tversion (it assumes that there are no changes between the two)" );
	}

	private static Collection<String> parseIgnoredImports( String ignoredURIs ) {
		StringTokenizer tok = new StringTokenizer( ignoredURIs, "," );
		
		LinkedList<String> result = new LinkedList<String>();
		
		while( tok.hasMoreElements() ) {
			result.add( tok.nextToken().trim() );
		}
		
		return result;
	}
	
	protected static AbstractServer processArgs(String[] args) {

		LongOpt[] longopts = new LongOpt[] {
		    new LongOpt( "help", LongOpt.NO_ARGUMENT, null, 'h' ),
		    new LongOpt( "port", LongOpt.REQUIRED_ARGUMENT, null, 'p' ),
		    new LongOpt( "kbid", LongOpt.REQUIRED_ARGUMENT, null, 'i' ),
		    new LongOpt( "ontology-uri", LongOpt.NO_ARGUMENT, null, 'O' ),
		    new LongOpt( "protege-standalone", LongOpt.NO_ARGUMENT, null, 'S' ),
		    new LongOpt( "protege-multiuser", LongOpt.NO_ARGUMENT, null, 'M' ),
		    new LongOpt( "hostname", LongOpt.REQUIRED_ARGUMENT, null, 's' ),
		    new LongOpt( "password", LongOpt.REQUIRED_ARGUMENT, null, 'x' ),
		    new LongOpt( "user", LongOpt.REQUIRED_ARGUMENT, null, 'u' ),
		    new LongOpt( "ignore-imports", LongOpt.REQUIRED_ARGUMENT, null, 'I' ),
		    new LongOpt( "ignore-inverses", LongOpt.NO_ARGUMENT, null, IGNORE_INVERSES ),
		    new LongOpt( "multi-threaded", LongOpt.NO_ARGUMENT, null, 'm' ),
		    new LongOpt( "load-imports-together", LongOpt.NO_ARGUMENT, null, LOAD_IMPORTS_TOGETHER ),
		    new LongOpt( "multi-threaded", LongOpt.NO_ARGUMENT, null, 'm' ),
		    new LongOpt( "persistence", LongOpt.OPTIONAL_ARGUMENT, null, 'P' ),
		    new LongOpt( "restore", LongOpt.NO_ARGUMENT, null, 'r' ),
		    new LongOpt( "no-ontology-sync", LongOpt.NO_ARGUMENT, null, 'N' )
		};
		    

		Getopt g = new Getopt( Server.class.getSimpleName(), args, "hp:s:u:x:MOSI:i:mP:rN", longopts );

		int c;
		int port = -1;
		boolean setPort = false;
		boolean multiThreadedClassification = false;
		boolean persisting = false;
		boolean restorePreviousState = false;
		boolean noOntologySync = false;
		int persistenceInterval = 1;
		DataSource source = DataSource.URI;
		String password = ProtegeMultiUserBasedServer.DEFAULT_PROTEGE_PASSWORD;
		String hostname = ProtegeMultiUserBasedServer.DEFAULT_PROTEGE_SERVER_HOST;
		String user = ProtegeMultiUserBasedServer.DEFAULT_PROTEGE_USER;
		String ignoredURIs = null;
		URI kbid = null;
		boolean loadImportsTogether = false;
		while( (c = g.getopt()) != -1 ) {
			switch ( c ) {
			case 'h':
				printUsage( System.out );
				return null;
				
			case IGNORE_INVERSES:
				PelletOptions.IGNORE_INVERSES = true;
				break;		
				
			case LOAD_IMPORTS_TOGETHER:
				loadImportsTogether = true;
				break;

			case 'i':
				String arg = g.getOptarg();
				try {
					kbid = new URI( arg );
					if( !kbid.isAbsolute() ) {
						System.err.println( "ERROR: URI must be absolute: " + arg );
						return null;
					}
				} catch( URISyntaxException e ) {
					System.err.println( "ERROR: Invalid URI: " + arg + " : " + e.getMessage() );
					return null;
				}
				break;

			case 'p':
				setPort = true;
				port = Integer.parseInt( g.getOptarg() );
				break;
			case 's':
				hostname = g.getOptarg();
				break;
			case 'u':
				user = g.getOptarg();
				break;
			case 'x':
				password = g.getOptarg();
				break;
			case 'M':
				source = DataSource.PROTEGE_MULTIUSER;
				break;
			case 'O':
				source = DataSource.URI;
				break;
			case 'S':
				source = DataSource.PROTEGE_STANDALONE;
				break;
			case 'I':
				ignoredURIs = g.getOptarg();
				break;				
			case 'm':
				multiThreadedClassification = true;
				break;		
			case 'P':
				persisting = true;
				String persistenceIntervalString = g.getOptarg();
				if( persistenceIntervalString != null ) {
					persistenceInterval = Integer.parseInt( persistenceIntervalString );
				}
				break;
				
			case 'r':
				restorePreviousState = true;
				break;
				
			case 'N':
				noOntologySync = true;
				break;
				
			case '?':
			default:
				printUsage( System.err );
				return null;
			}
		}

		int next = g.getOptind();
		AbstractServer server;
		if( source == DataSource.PROTEGE_STANDALONE ) {
			if( next >= args.length ) {
				server = new ProtegeStandaloneBasedServer();
			}
			else {
				if( next < (args.length - 1) ) {
					log.warning( "Extra arguments encountered.  Ignoring arguments after: "
							+ args[next] );
				}
				server = new ProtegeStandaloneBasedServer( args[next] );
			}
		}
		else if( source == DataSource.PROTEGE_MULTIUSER ) {
			if( next >= args.length ) {
				server = new ProtegeMultiUserBasedServer( hostname, null, user, password );
			}
			else {
				if( next < (args.length - 1) ) {
					log.warning( "Extra arguments encountered.  Ignoring arguments after: "
							+ args[next] );
				}
				server = new ProtegeMultiUserBasedServer( hostname, args[next], user, password );
			}
		}
		else {
			Set<URI> uris = new HashSet<URI>();
			for( int i = next; i < args.length; i++ ) {
				try {
					URI u = new URI( args[i] );
					uris.add( u );
				} catch( URISyntaxException e ) {
					log.warning( "Ontology URI expected as argument.  Ignoring invalid URI: "
							+ args[i] );

				}
			}
			server = new URIBasedServer( uris );
		}

		if( setPort )
			server.setPort( port );

		if (kbid != null)
			server.setKbURI( kbid );
		
		if( ignoredURIs != null ) {
			server.setIgnoredImports( parseIgnoredImports( ignoredURIs ) );
		}
		
		server.setMultiThreadedClassification( multiThreadedClassification );
		server.setLoadImportsTogether( loadImportsTogether );
		
		if( persisting || restorePreviousState ) {
			File saveDirectory = new File( "." ); // TODO: add parameter to specify the save directory
			PersistenceManager persistenceManager = new PersistenceManager( server, saveDirectory, persistenceInterval );
			persistenceManager.setPersisting( persisting );
			persistenceManager.setRestoreNeeded( restorePreviousState );
			persistenceManager.setNoOntologySync( noOntologySync );
			server.setPersistenceManager( persistenceManager );
		}		

		return server;
	}

	public static void main(String[] args) {

		// Configure XML writer to minimize serialization overhead
		XMLWriterPreferences.getInstance().setIndenting( false );
		XMLWriterPreferences.getInstance().setUseNamespaceEntities( false );

		AbstractServer server = processArgs( args );
		if( server == null )
			return;

		server.run();
		
		System.out.println( "Server started on " + server.getStartTime() +", listening on port " + server.getPort() );
	}
}
