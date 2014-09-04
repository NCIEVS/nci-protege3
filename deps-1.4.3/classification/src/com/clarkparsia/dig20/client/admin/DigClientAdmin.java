package com.clarkparsia.dig20.client.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.coode.xml.XMLWriter;
import org.coode.xml.XMLWriterImpl;
import org.coode.xml.XMLWriterNamespaceManager;
import org.coode.xml.XMLWriterPreferences;
import org.xml.sax.SAXException;

import com.clarkparsia.dig20.HTTPConstants;
import com.clarkparsia.dig20.client.DefaultDigClient;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.DigClientHttpErrorException;
import com.clarkparsia.dig20.exceptions.DigClientPostIOException;
import com.clarkparsia.dig20.exceptions.DigClientResponseIOException;
import com.clarkparsia.dig20.server.xml.AdminVocabulary;

public class DigClientAdmin {

	private static final Logger log = Logger.getLogger(DefaultDigClient.class
			.getName());

	final private static String ADMIN_PATH = "/admin/";

	final private URL administrationURL;

	final private URL reasonerURL;

	public DigClientAdmin(URL digServer) {
		URL tempAdministrationURL = null;
		URL tempReasonerURL = null;

		try {
			tempReasonerURL = digServer;
			tempAdministrationURL = extractAdministrationURL(digServer);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// TODO error handling
		}

		administrationURL = tempAdministrationURL;
		reasonerURL = tempReasonerURL;
	}

	private static URL extractAdministrationURL(URL digServer) throws MalformedURLException {
		String filePortion = digServer.getFile();
		String digServerString = digServer.toString();
		String hostPortion = digServerString.substring(0, digServerString
				.indexOf(filePortion));
		
		return new URL(hostPortion + ADMIN_PATH);
	}

	public URL getAdministrationURL() {
		return administrationURL;
	}

	public URL getReasonerURL() {
		return reasonerURL;
	}

	public void reload() throws DigClientException {
		HttpURLConnection connection = null;

		try {
			connection = initHttpPost();

			OutputStreamWriter toServer = new OutputStreamWriter(connection
					.getOutputStream());
			XMLWriter xmlWriter = createXMLWriter(toServer);

			xmlWriter.startDocument(AdminVocabulary.ADMIN.getShortName());

			xmlWriter.writeStartElement(AdminVocabulary.RELOAD.getURI()
					.toString());
			xmlWriter.writeEndElement();

			xmlWriter.endDocument();
			toServer.flush();
			toServer.close();
		} catch (IOException e) {
			throw new DigClientPostIOException(e);
		}

		try {
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK)
				throwHttpException(connection, responseCode);

			connection.disconnect();
		} catch (IOException e) {
			throw new DigClientResponseIOException(e);
		}
	}

	public void shutdown() throws DigClientException {
		HttpURLConnection connection = null;

		try {
			connection = initHttpPost();

			OutputStreamWriter toServer = new OutputStreamWriter(connection
					.getOutputStream());
			XMLWriter xmlWriter = createXMLWriter(toServer);

			xmlWriter.startDocument(AdminVocabulary.ADMIN.getShortName());

			xmlWriter.writeStartElement(AdminVocabulary.SHUTDOWN.getURI()
					.toString());
			xmlWriter.writeEndElement();

			xmlWriter.endDocument();
			toServer.flush();
			toServer.close();
		} catch (IOException e) {
			throw new DigClientPostIOException(e);
		}

		try {
			// technically, if the server shut down successfully, the code below will not be
			// executed successfully (i.e., an exception will be thrown)
			// Therefore, the code below is included to get any error codes (different than HTTP_OK)
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK)
				throwHttpException(connection, responseCode);

			connection.disconnect();
		} catch (IOException e) {
			// ignored -- when the server shuts down, it is expected that the connection will terminate
		}
	}

	public void load() {
		// TODO
	}

	public Properties info() throws DigClientException {
		Properties result = null;
		
		HttpURLConnection connection = null;

		try {
			connection = initHttpPost();

			OutputStreamWriter toServer = new OutputStreamWriter(connection
					.getOutputStream());
			XMLWriter xmlWriter = createXMLWriter(toServer);

			xmlWriter.startDocument(AdminVocabulary.ADMIN.getShortName());

			xmlWriter.writeStartElement(AdminVocabulary.INFO.getURI()
					.toString());
			xmlWriter.writeEndElement();

			xmlWriter.endDocument();
			toServer.flush();
			toServer.close();
		} catch (IOException e) {
			throw new DigClientPostIOException(e);
		}

		try {
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK)
				throwHttpException(connection, responseCode);


			SAXParserFactory aFactory = SAXParserFactory.newInstance();
			aFactory.setNamespaceAware( true );
			SAXParser parser = aFactory.newSAXParser();

			AdminResponseXMLHandler xmlHandler = new AdminResponseXMLHandler();
			parser.parse(connection.getInputStream(), xmlHandler);

			result = xmlHandler.getReadProperties();

			connection.disconnect();
		} catch (IOException e) {
			throw new DigClientResponseIOException(e);
		} catch (SAXException e) {
			throw new DigClientException("Unable to parse the server's response", e);
		} catch (ParserConfigurationException e) {
			throw new DigClientException("Unable to instantiate the XML parser", e);
		}

		return result;
	}

	private static XMLWriter createXMLWriter(Writer writer) {
		XMLWriterNamespaceManager nsManager = new XMLWriterNamespaceManager(
				AdminVocabulary.ADMIN_NS);
		XMLWriter xmlWriter = new XMLWriterImpl(writer, nsManager);

		return xmlWriter;
	}

	private HttpURLConnection initHttpPost() throws IOException {
		HttpURLConnection connection;

		connection = (HttpURLConnection) administrationURL.openConnection();
	
		/*
		 * Post request
		 */
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				HTTPConstants.REQUEST_CONTENT_TYPE);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.connect();

		return connection;
	}

	private void throwHttpException(HttpURLConnection connection,
			int responseCode) throws IOException, DigClientHttpErrorException {
		InputStream errorStream = connection.getErrorStream();
		String errorDump;
		if (errorStream == null)
			errorDump = "No error content";
		else {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					errorStream));
			StringBuffer sbuf = new StringBuffer();
			char buf[] = new char[1024];
			int nread;
			while ((nread = br.read(buf)) > 0) {
				sbuf.append(buf, 0, nread);
			}
			br.close();
			errorStream.close();
			errorDump = (sbuf.length() > 0) ? sbuf.toString()
					: "No error content";
		}
		final String msg = "HTTP Post failed. Code: " + responseCode
				+ " Message: " + connection.getResponseMessage() + " Content: "
				+ errorDump;
		log.warning(msg);
		throw new DigClientHttpErrorException(msg,
				(responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT));
	}

	static {
		// Configure XML writer to minimize serialization overhead
		XMLWriterPreferences.getInstance().setIndenting(false);
		XMLWriterPreferences.getInstance().setUseNamespaceEntities(false);
	}
}
