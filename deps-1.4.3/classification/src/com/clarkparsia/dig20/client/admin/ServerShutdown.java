package com.clarkparsia.dig20.client.admin;

import java.net.MalformedURLException;
import java.net.URL;

import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.server.AbstractServer;

public class ServerShutdown {
	public static void main( String[] args ) {
		// default server URL
		URL digServerUrl = null;

		try {
			digServerUrl = new URL( "http://localhost:"
					+ AbstractServer.DEFAULT_PORT + "/explain" );

			if ( args.length > 0 ) {
				digServerUrl = new URL( args[0] );
			}
		} catch ( MalformedURLException e ) {
			System.err.println("Invalid server URL " + e);
			System.exit(1);
		}

		DigClientAdmin digClientAdmin = new DigClientAdmin( digServerUrl );
		
		try {
			digClientAdmin.shutdown();
			System.out.println( "Server shutdown request sent." );
		} catch ( DigClientException e ) {
			System.err.println( "Unable to shutdown the server " + e);
			System.exit( 1 );
		}
	}
}
