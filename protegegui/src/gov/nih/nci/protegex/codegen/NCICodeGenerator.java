/**
 * 
 */
package gov.nih.nci.protegex.codegen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;

import edu.stanford.smi.protege.util.Log;

/**
 * @author Manual Re Count
 * 
 */
public class NCICodeGenerator {
	
	

	private static String PREFIX = null;
	private static String DELIMITER = null;	
	private static String SUFFIX = null;

	private static final String data_filename = "codegen.dat";
	private String props_filename = "codegen.properties";
	
	private boolean is_initialized = true;
	

	private static NCICodeGenerator instance = null;

	public synchronized static NCICodeGenerator getInstance() {
		if (instance != null) {

		} else {
			instance = new NCICodeGenerator();
		}
		return instance;
	}
	
	public synchronized static NCICodeGenerator getInstance(String config_filename) {
		if (instance != null) {

		} else {
			instance = new NCICodeGenerator(config_filename);
		}
		return instance;
		
	}

	private void init() {

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(props_filename));

			PREFIX = props.getProperty("codegen.prefix");
			DELIMITER = props.getProperty("codegen.delimiter");
			SUFFIX = props.getProperty("codegen.suffix");
			
		} catch (IOException e) {
			Log.getLogger(getClass()).warning("No properties file, codes will be integers");
			is_initialized = false;
			return;
		}
		
		try {

			File data = new File("codegen.dat");

			if (data.exists()) {

			} else {
				data.createNewFile();
				String seed = props.getProperty("codegen.seed");

				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(data)));
				out.println(seed);
				out.close();
				Log.getLogger(getClass()).info("codegen.dat file created successfully");

			}

		} catch (IOException e) {
			Log.getLogger(getClass()).severe("Error creating codegen data file");
			is_initialized = false;
		}

	}

	private NCICodeGenerator() {
		init();
	}
	
	private NCICodeGenerator(String props) {
		this.props_filename = props;
		init();
	}

	public String getCode() {
		
		if (!is_initialized) {
			return "INV" + Math.abs((new Random()).nextInt());
		}

		try {

			File f = new File(data_filename);
			if (!f.exists()) {
				System.out.println(f.getAbsolutePath());
			}
			int code = -1;
			BufferedReader in = new BufferedReader(
					new FileReader(data_filename));
			code = Integer.parseInt(in.readLine().trim());
			//System.out.println("The current code is " + code);
			
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(data_filename)));
			out.println(code + 1);
			out.close();
			
			in.close();
			
			String codeS = new Integer(code).toString();
			
			//System.out.println("The current code is " + code);
			
			String res = "";
			if (PREFIX != null) {
				res += PREFIX;
			}
			if (DELIMITER != null) {
				res += DELIMITER;
			}
			
			res += codeS;
			
			if (SUFFIX != null) {
				if (DELIMITER != null) {
					res += DELIMITER;
				}
				res += SUFFIX;
			}

			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return PREFIX + DELIMITER + "NOCODE";
		}
	}
}
