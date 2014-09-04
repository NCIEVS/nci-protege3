package com.clarkparsia.dig20.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.io.IncrementalClassifierPersistence;

/**
 * Orchestrates the process of preserving the state of the explanation server (currently only IncrementalClassifier) to the files.
 * This class contains a thread that will periodically start the saving process, if there are any changes to the ontology. It also contains
 * a few flags that allow other parts of the server to set flags whether a restore is needed, the persistence is enabled, or the current state is already saved. 
 * The actual saving is delegated to the IncrementalClassifierPersistence.
 * 
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
public class PersistenceManager {	
	/**
	 * The background thread that triggers periodic saving of the state.
	 */
	private Thread thread;
	
	/**
	 * The code that triggers the periodic saving of the state.
	 */
	private Timer timer;
	
	/**
	 * The reference to the current explanation server.
	 */
	private AbstractServer server;
	
	/**
	 * If set to true, the manager will trigger periodically saving of the state. If false, the manager will only supervise any restore (if any).
	 */
	private boolean persisting = false;
	
	/**
	 * If set to true, it means that a restore was requested (e.g., as the command-line argument), and has not yet been performed.
	 */
	private boolean restoreNeeded = false;
	
	/**
	 * If set to true, it means that the current state of the explanation server has not changed since the last save.
	 */
	private boolean currentStatePersisted = false;
	
	private boolean noOntologySync = false;
	
	/**
	 * The directory where the files with the persisted information are stored. 
	 */
	private File saveDirectory;

	/**
	 * Creates a new persistence manager. By default, the manager will not perform any save operations, until start() method is called.
	 * 
	 * @param server the explanation server
	 * @param saveDirectory the directory where the saved information should be stored.
	 * @param interval the interval (in hours) when saving should be performed 
	 */
	public PersistenceManager( AbstractServer server, File saveDirectory, int interval ) {
		timer = new Timer( interval * 3600l * 1000l );
		this.server = server;
		this.saveDirectory = saveDirectory;
	}

	/**
	 * Changes the mode of this persistence manager to periodic persisting of the state. (The persistence operation will not start until start() is called.) 
	 * @param persisting if set to true, it will persist periodically the state of the explanation server
	 */
	public void setPersisting( boolean persisting ) {
		this.persisting = persisting;
	}
	
	/**
	 * Checks whether this persistence manager is in the persisting mode.
	 * @return true if the manager is in the persisting mode.
	 */
	public boolean isPersisting() {
		return persisting;
	}
	
	/**
	 * Sets the flag indicating whether a restore operation is requested (e.g., by a command line argument).
	 * The restore typically occurs when the explanation server loads/reloads an ontology (e.g., during the startup).  
	 * This flag should be cleared after a restore.
	 * @param restoreNeeded true if a restore operation should be performed.
	 */
	public void setRestoreNeeded( boolean restoreNeeded ) {
		this.restoreNeeded = restoreNeeded;
	}
	
	/**
	 * Gets the flag telling whether a restore operation is requested.
	 * @return true if a restore operation was requested but not yet performed.
	 */
	public boolean isRestoreNeeded() {
		return restoreNeeded;
	}
	
	/**
	 * Gets the flag indicating whether the current state of the server is the same as the state stored in a file during the most recent save
	 * operation.
	 * 
	 * @return true if the recently stored state is the same as the current state of the server.
	 */
	public boolean isCurrentStatePersisted() {
		return currentStatePersisted;
	}
	
	/**
	 * Sets the flag indicating whether the current state of the server is the same as the state stored in a file during the most recent save
	 * operation.
	 * 
	 * @param currentStatePersisted true if the recently stored state is the same as the current state of the server.
	 */
	public void setCurrentStatePersisted( boolean currentStatePersisted ) {
		this.currentStatePersisted = currentStatePersisted;
	}
	
	public boolean isNoOntologySync() {
		return noOntologySync;
	}
	
	public void setNoOntologySync( boolean noOntologySync ) {
		this.noOntologySync = noOntologySync;
	}
	
	/**
	 * Starts the periodic invokation of the persistence operation (if the state of the server changes since the last save, as indicated by currentStatePersisted flag).
	 * This method starts a background thread that will periodcally call persist() method of this object.
	 * 
	 * @throws IllegalStateException if start() has already been called, or if persistence manager operates in a non-persisting mode.
	 */
	public void start() throws IllegalStateException {
		if( !persisting ) {
			throw new IllegalStateException( "Persistence manager cannot start in non-persisting mode." );
		}
		
		if( timer.isStarted() || ( thread != null ) ) {
			throw new IllegalStateException(
					"The timer for the PersistenceManager is already started.");
		}

		thread = new Thread( timer );
		thread.start();
	}

	/**
	 * Request the termination of the background thread that performs the periodic invokation of the persistence operation. This method will block
	 * until the background thread terminates.
	 * 
	 * @throws IllegalStateException if the background thread is not running right now
	 */
	public void stop() throws IllegalStateException {
		if( !timer.isStarted() ) {
			throw new IllegalStateException(
					"The timer for the Persistence is not running right now.");
		}

		timer.stop();
		thread = null;
	}
	
	/**
	 * Localizes the most recently saved state of the incremental classifier and reads the classifier. This method must not be called while
	 * the background thread for periodic persistence is running. (Typically, the restore operation precedes the start of the background thread.) 
	 * 
	 * @return the read classifier
	 * @throws IllegalStateException if the method was invoked while the background thread is running
	 * @throws IOException if an I/O error should occur during the restore operation
	 * @throws OWLReasonerException if there is an error while updating the classifier with changes
	 */
	public IncrementalClassifier restore( OWLOntology ontology ) throws IllegalStateException, IOException, OWLReasonerException {
		if( timer.isStarted() ) {
			throw new IllegalStateException( "The restore operation cannot be done while the timer is running" );
		}
		
		System.out.print( "Restoring classifier from file ... " );
		System.out.flush();
		
		long startTime = System.currentTimeMillis();
		
		File restoreFile = determineRestoreFile();		
		
		if( restoreFile == null ) {
			throw new IllegalStateException( "There is no suitable file with the data to restore" );
		}
		
		FileInputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream( restoreFile );
			
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			
			if( ontology == null ) { 
				return IncrementalClassifierPersistence.load( ontologyManager, inputStream );
			} else {
				HashSet<OWLOntology> ontologies = new HashSet<OWLOntology>();
				ontologies.add( ontology );
				return IncrementalClassifierPersistence.load( ontologyManager, inputStream, ontologies );
			}
		} finally {
			if( inputStream != null ) {
				try {
					inputStream.close();
					long endTime = System.currentTimeMillis();
					
					System.out.println("done (in " + (endTime - startTime) + " ms)" );
					
					// set the time of last persistence time to the modification time of this file
					lastPersistFinishTime = restoreFile.lastModified();
				} catch( IOException e ) {
					// ignored
				}
			}
		}
	}
	
	/**
	 * Maximum radix for encoding of the MD5 of the root ontology URI
	 */
	private static final int ENCODING_RADIX = 36;
	
	/**
	 * The pattern for the names of the files containing the persisted data of the incremental classifier. The first
	 * parameter in the pattern should be replaced with the MD5 of the root ontology URI (to prevent mixing up files that belong to different ontologies), 
	 * and the second with a time stamp (to determine the most recent file).
	 */
	private static final String FILE_NAME_PATTERN 	= "persisted-state-%s-%s.zip";
	
	/**
	 * The part of the file name pattern before the time stamp. The parameter in the patter should be replaced with the MD5 of the root ontology URI
	 * (to prevent mixing up files that belong to different ontologies).
	 */
	private static final String FILE_NAME_PREFIX 	= "persisted-state-%s-";
	
	/**
	 * The suffix of the file name pattern after the time stamp.
	 */
	private static final String FILE_NAME_SUFFIX	= ".zip";
	
	/**
	 * Determines which of the files contains the most recent information.
	 * 
	 * @param files the files containing the persisted data
	 * @param prefix the common file name prefix for these files (containing the hash code of the URI of the ontology).
	 * @return the most recent file (as determined by the time stamp in the file name) or null if the files array was empty
	 */
	private File getLatestFile( File[] files, String prefix ) {
		File result = null;
		long latestTimestamp = 0l;
		
		for( File file : files ) {
			String timestampWithSuffix = file.getName().substring( prefix.length() );
			String timestampString = timestampWithSuffix.substring( 0, timestampWithSuffix.length() - FILE_NAME_SUFFIX.length() );
			
			long timestamp = Long.parseLong( timestampString );
			
			if( timestamp > latestTimestamp ) {
				latestTimestamp = timestamp;
				result = file;
			}
		}
		
		return result;
	}
	
	/**
	 * Searches for the file containing the most recent information for the incremental classifier. The files are sought in saveDirectory 
	 * (set up in the constructor), and they are identified by the hash code of the URI for the root ontology. (This means that the ontologies
	 * must be already loaded into the explanation server for this method to be called.) 
	 * 
	 * @return the file containing the most recent information or null, if there seems to be no suitable file.
	 */
	private File determineRestoreFile() {
		final String prefix = String.format( FILE_NAME_PREFIX, hashOntologyURI() );
		
		File[] potentialFiles = saveDirectory.listFiles( new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith( prefix ) && name.endsWith( FILE_NAME_SUFFIX );
			}			
		} );
		
		return getLatestFile( potentialFiles, prefix );
	}
	
	/**
	 * Computes the hash code of the root ontology URI and returns the string representation of the hash code. The hash code
	 * is used to identify which files contain information about the particular ontology (and we can't use directly URIs since they can contain special
	 * characters that are not allowed in file names, not to mention that this would make the file names too long).
	 * 
	 *  This method obtains the root ontology URI from the explanation server, which means that the ontology has to be loaded at the point this method is invoked.
	 * 
	 * @return the string representation of the hash code of the root ontology URI
	 */
	private String hashOntologyURI() {
		byte[] uriBytes = server.getRootOntologyURI().toString().getBytes();
		
		byte[] hashBytes = MD5.digest( uriBytes );
		
		BigInteger bi = new BigInteger(1, hashBytes);
		
		return bi.toString(ENCODING_RADIX);
	}
	
	/**
	 * Computes the name of the file to which the incremental will be persisted.
	 * 
	 * @return the file name where the information should be stored.
	 */
	private File determineSaveFile() {
		String fileName = String.format( FILE_NAME_PATTERN, hashOntologyURI(), String.valueOf( System.currentTimeMillis() ) );
		
		return new File( saveDirectory, fileName );
	}
	
	public long getLastPersistFinishTime() {
		return lastPersistFinishTime;
	}
	
	public long getLastPersistDuration() {
		return lastPersistDuration;
	}
	
	private volatile long lastPersistFinishTime = -1l;
	private volatile long lastPersistDuration = 0l;
	
	/**
	 * The method called periodically by the background thread to save the current state of the incremental classifier.
	 * This method determines first if there are any unsaved changes (since the last invokation), and if yes, it initiates the saving
	 * process using IncrementalClassifierPersistence.
	 */
	private void persist() {	
		if( isCurrentStatePersisted() )
			return; // noop if nothing changed since the last save		
		
		FileOutputStream outputStream = null;
		long persistStartTime = System.currentTimeMillis();
		
		try {
			File saveFile = determineSaveFile();
			
			outputStream = new FileOutputStream( saveFile );
			
			server.saveClassifier( outputStream ); 
			setCurrentStatePersisted( true );
		} catch( IOException e ) {
			// TODO handle
		} finally {
			if( outputStream != null ) {				
				try {
					outputStream.close();					
				} catch( IOException e ) {
					// ignored
				}
				lastPersistFinishTime = System.currentTimeMillis();
				lastPersistDuration = lastPersistFinishTime - persistStartTime;
			}
		}
	}
	
	public void requestPersist() {
		if( !timer.isStarted() || ( thread == null ) ) {
			throw new IllegalStateException(
					"Can't request a persistence action without the timer thread running.");
		}
		
		timer.requestPersist();
	}

	/**
	 * The class computing the MD5 hash code
	 */
	private static MessageDigest MD5;
		
	static {
		try {
			MD5 = MessageDigest.getInstance( "MD5" );
		} catch (NoSuchAlgorithmException e) {
			// ignored
		}
	}
	
	/**
	 * The code executed in the background that periodically invokes persist() to save the current state of the
	 * incremental classifier.
	 * 
	 * @author Blazej Bulka <blazej@clarkparsia.com>
	 */
	private class Timer implements Runnable {
		/**
		 * The interval (in milliseconds) at which persist() should be called.
		 */
		private long interval;
		
		/**
		 * This flag is set to true if the code is running in the background
		 */
		private volatile boolean started = false;
		
		/**
		 * This flag is set to true if the stop of the background thread was requested.
		 */
		private volatile boolean stopRequested = false;

		/**
		 * The time (in milliseconds since Epoch) when the last invokation of persist() occurred. If it never occurred,
		 * it contains 0.
		 */
		private long lastInvokation = 0;
		
		private volatile boolean persistRequested = false;

		/**
		 * Creates a new timer code.
		 * 
		 * @param interval the interval (in milliseconds) that specifies how frequently the persist() method should be called.
		 */
		Timer ( long interval ) {
			this.interval = interval;
		}
		
		/**
		 * Checks whether enough time has passed from the last invokation of persist() to warrant another invokation.
		 * 
		 * return true if enough time has passed from the last invokation
		 */
		private boolean isTimeToInvoke() {
			long time = System.currentTimeMillis();
			
			return ( persistRequested || ( ( time - lastInvokation ) >= interval ) );
		}
		
		/**
		 * The main method of the timer thread.
		 */
		public synchronized void run() {
			started = true;
			
			while( !stopRequested ) {
				boolean timeToInvoke = false;
				while( !stopRequested && !( timeToInvoke = isTimeToInvoke() ) ) {
					try {
						wait(1000);
					} catch( InterruptedException e ) {
						// nothing
					}					
				}
				
				if( timeToInvoke ) {
					persist();
					lastInvokation = System.currentTimeMillis();
					persistRequested = false;
				}
			}
			
			started = false;
			notifyAll();
		}

		/**
		 * Checks whether the main loop of the background thread is running.
		 * 
		 * @return true, if the background thread is running
		 */
		public boolean isStarted() {
			return started;
		}

		/**
		 * Stops the timer loop in the background thread. This method blocks until the background thread
		 * notifies it that it is exiting.
		 */
		public synchronized void stop() {
			stopRequested = true;
			notifyAll();

			while (started) {
				try {
					wait();
				} catch (InterruptedException e) {
					// nothing
				}
			}
		}
		
		public synchronized void requestPersist() {
			persistRequested = true;
			notifyAll();
		}
	}
}
