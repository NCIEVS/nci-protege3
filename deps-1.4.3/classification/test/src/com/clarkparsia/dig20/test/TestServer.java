package com.clarkparsia.dig20.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import com.clarkparsia.dig20.server.URIBasedServer;

public class TestServer {

	private static TestServer	instance	= new TestServer();
	public static final int		port		= 18085;
	private URIBasedServer		server;

	public static TestServer theInstance() {
		return instance;
	}

	public URI start(String filePath) {
		File f = new File( filePath );
		server = new URIBasedServer( Collections.singleton( f.toURI() ) );
		server.setPort( port );
		server.run();
		return server.getKbURI();
	}

	public URL URL() {
		try {
			return new URL( "http://localhost:" + port + "/explain/" );
		} catch( MalformedURLException e ) {
			throw new RuntimeException( e );
		}
	}

	public void stop() {		
		try {
			server.stop();
		} catch( RuntimeException e ) {
			e.printStackTrace();
		}
	}

	public void reload() {
		server.reload();
	}
}
