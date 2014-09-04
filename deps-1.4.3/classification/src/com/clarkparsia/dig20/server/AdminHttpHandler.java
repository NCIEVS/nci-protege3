package com.clarkparsia.dig20.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.xml.sax.SAXException;

import com.clarkparsia.dig20.server.admin.AdminResponse;
import com.clarkparsia.dig20.server.xml.AdminResponseRenderer;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 1, 2007 8:25:37 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class AdminHttpHandler extends AbstractHttpHandler {
    private static final Logger LOGGER = Logger.getLogger( AdminHttpHandler.class.getName() );

    private AbstractServer mServer;
    
    public AdminHttpHandler(AbstractServer theServer) {
        mServer = theServer;
    }

    public void handle(String thePathContext, String thePathParams, HttpRequest theRequest, HttpResponse theResponse) throws HttpException, IOException {
        String aClientInfo = "(ip=" + theRequest.getHttpConnection().getRemoteAddr() + ")";
        theRequest.setHandled( true );
 
        try {
            SAXParserFactory aFactory = SAXParserFactory.newInstance();
            aFactory.setNamespaceAware( true );
            SAXParser parser = aFactory.newSAXParser();

            AdminXMLHandler aHandler = new AdminXMLHandler();
            parser.parse( theRequest.getInputStream(), aHandler );

            AdminResponse response = null;            
            
            if (aHandler.isShutdownCommand()) {

                if (LOGGER.isLoggable( Level.FINE )) {
                    LOGGER.fine("Admin Server -- Received Shutdown Command");
                }

                mServer.shutdown();
            }
            else if (aHandler.isLoadCommand()) {
                if (LOGGER.isLoggable( Level.FINE )) {
                    LOGGER.fine("Admin Server -- Received Load Command");
                    LOGGER.fine("Load Arguments -- " + aHandler.getCommandArguments());
                }

                mServer.load(aHandler.getCommandArguments());
            }
            else if (aHandler.isReloadCommand()) {
                if (LOGGER.isLoggable( Level.FINE )) {
                    LOGGER.fine("Admin Server -- Received Reload Command");
                }

                mServer.reload();
            }
            else if (aHandler.isInfoCommand()) {
            	if (LOGGER.isLoggable( Level.FINE )) {
            		LOGGER.fine("Admin Server Received Info Command");
            	}
            	
            	response = mServer.info();
            } 
            else if (aHandler.isPersistCommand()) {
            	if (LOGGER.isLoggable( Level.FINE )) {
            		LOGGER.fine("Admin Server Received Persist Command");
            	}
            	
            	mServer.persist();
            }

            theResponse.setStatus( HttpResponse.__200_OK );
            
            if (response != null) {
            	AdminResponseRenderer responseRenderer = new AdminResponseRenderer();
            	            
            	OutputStream os = theResponse.getOutputStream();
            	OutputStreamWriter writer = new OutputStreamWriter(os);
            
            	responseRenderer.startRendering(writer);
            	responseRenderer.render(response);
            	responseRenderer.endRendering();
            }
        }
        catch (SAXException se) {
            LOGGER.log(Level.SEVERE, "Failed to parse request for client " + aClientInfo, se);
            theResponse.setStatus(HttpResponse.__400_Bad_Request, "Caught SAXException: " + se.getLocalizedMessage());
        }
        catch (ParserConfigurationException pce) {
            LOGGER.log(Level.SEVERE, "Failed request for client " + aClientInfo, pce);
            theResponse.setStatus(HttpResponse.__400_Bad_Request, "Caught ParserConfigurationException: " + pce.getLocalizedMessage());
        }
    }
}
