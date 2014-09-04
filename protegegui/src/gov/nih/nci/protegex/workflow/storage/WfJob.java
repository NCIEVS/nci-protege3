/**
 * 
 */
package gov.nih.nci.protegex.workflow.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//import edu.stanford.smi.protege.model.KnowledgeBase;
//import edu.stanford.smi.protege.model.Localizable;

/**
 * @author Bob Dionne
 * 
 */
public class WfJob implements Serializable {

	public static final long serialVersionUID = 323456792L;

	public static Logger logger = Logger.getLogger(WfJob.class.getName());

	static {
		try {
			FileHandler fh = new FileHandler("logs" + File.separatorChar
					+ "workflow_%u.log");
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
		} catch (IOException e) {
			logger.severe("can't configure logger, goodbye, you lose");
		}
	}

	private final String topFolder = "workflow";
	
	private final String archiveFolderName = "archive";
	private String archiveFolder = null;

	private final String usersFileName = "users";
	private String users = null;

	private String assidFileName = "assid";
	private String assid = null;

    private String pkgidFileName = "pkgid";
    private String pkgid = null;

    private void init() throws IOException {

		File toplevel = new File(topFolder);
		if (!toplevel.exists()) {
			toplevel.mkdir();
			File archiveLevel = new File(topFolder + File.separatorChar + archiveFolderName);
			archiveLevel.mkdir();
		}

		users = topFolder + File.separatorChar + usersFileName;
		File uf = new File(users);
		if (!uf.exists()) {
			uf.createNewFile();
		}

		assid = topFolder + File.separatorChar + assidFileName;
		createIDFile(assid);

        pkgid = topFolder + File.separatorChar + pkgidFileName;
        createIDFile(pkgid);

        archiveFolder = topFolder + File.separatorChar + archiveFolderName;

	}
    
    /**
     * Creates an ID file if it does not already exists.  This file is created
     *   with the integer 1 value in it.
     * @param filename The filename.
     * @throws IOException
     */
    private void createIDFile(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists())
            return;

        file.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(new Integer(1).toString());
        out.close();
    }

	public enum JobTask {
		/**
		 * Does a given user already existing in the store
		 */
		USEREXISTS,

		/**
		 * What is the role of the user, ADMIN, MODELER, or BOTH
		 */
		USERROLE,

		/**
		 * get all users
		 */
		ALLUSERS,

		/**
		 * store a given assignment for a user, in the user's folder
		 */
		PUTASSIGN,
		
		/**
		 * store an assignment in the archive for future use
		 */
		ARCHIVEASSIGN,

		/**
		 * delete a given assignment for a user, from that users folder. It may
		 * not exist, in which case this is a nop
		 */
		DELASSIGN,

		/**
		 * For a given user, retrieve all the assignments
		 */
		GETALLASSIGN,

		/**
		 * retrieve the next available id to be used for a new assignment
		 */
		NEXTASSID,

        /**
         * retrieve the next available id to be used for a package
         */
        NEXTPKGID,

        /**
		 * create a new user with specified privileges
		 */
		NEWUSER;

	}

	private JobTask currentTask = null;

	private String[] args = null;

	private boolean userInString(String line, String user) {

		if (line.equalsIgnoreCase("")) {
			return false;
		}

		String fuser = fixUpName(user);

		StringTokenizer tk = new StringTokenizer(line);
		String test = tk.nextToken();
		return test.equalsIgnoreCase(fuser);

	}

	private Integer roleInString(String line, String user) {

		String fuser = fixUpName(user);

		StringTokenizer tk = new StringTokenizer(line);
		String test = tk.nextToken();
		if (test.equalsIgnoreCase(fuser)) {
			return new Integer(Integer.parseInt(tk.nextToken()));
		} else {
			return new Integer(-1);
		}

	}

	private String fixUpName(String name) {
		if (name.indexOf(" ") > 0) {
			return name.replaceAll(" ", "@@@");

		} else {
			return name;
		}
	}

	private String readIntoString(File f) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			StringBuffer res = new StringBuffer();
			String s;
			while ((s = in.readLine()) != null) {
				res.append(s);
			}
            in.close();
			return res.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void setArgs(String[] s) {
		args = s;
	}

	public void setTask(JobTask t) {
		currentTask = t;
	}
	
	public Object run() {
		try {
			init();
		} catch (IOException e) {
			logger.severe("Unable to initialize store: " + e.getMessage());
			return null;
		}

		switch (currentTask) {

		case USEREXISTS:
			try {
				BufferedReader in = new BufferedReader(new FileReader(users));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (userInString(line, args[0])) {
						in.close();
						return "true";
					}

				}
				return "false";
			} catch (Exception e) {
				e.printStackTrace();
				return "false";
			}

		case NEWUSER:
			Integer bad = new Integer(-1);
			try {
				BufferedReader in = new BufferedReader(new FileReader(users));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (userInString(line, args[0])) {
						return bad;
					}

				}
				in.close();

				BufferedWriter out = new BufferedWriter(new FileWriter(users,
						true));

				out.write(fixUpName(args[0]) + " " + args[1]);
				out.newLine();
				out.close();
				return Integer.parseInt(args[1]);

			} catch (Exception e) {
				e.printStackTrace();
				return bad;
			}

		case USERROLE:
			Integer notFound = new Integer(-1);
			try {

				BufferedReader in = new BufferedReader(new FileReader(users));
				String line = "";
				while ((line = in.readLine()) != null) {
					if (userInString(line, args[0])) {
						in.close();
						return roleInString(line, args[0]);
					}

				}
				return notFound;
			} catch (Exception e) {
				e.printStackTrace();
				return notFound;
			}
		case ALLUSERS:

			try {

				BufferedReader in = new BufferedReader(new FileReader(users));

				ArrayList<String> users = new ArrayList<String>();
				String line = "";
				while ((line = in.readLine()) != null)
					users.add(line);
				return users;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		case GETALLASSIGN:
			File mdir = new File(topFolder + File.separatorChar
					+ fixUpName(args[0]));
			if (!mdir.isDirectory()) {
				return null;
			}
			File[] all = mdir.listFiles();
			ArrayList<String> res = new ArrayList<String>();
			for (int i = 0; i < all.length; i++)
			    res.add(readIntoString(all[i]));
			return res;

		case PUTASSIGN:
			try {
				File dir = new File(topFolder + File.separatorChar
						+ fixUpName(args[0]));
				if (!dir.isDirectory()) {
					dir.mkdir();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(
						topFolder + File.separatorChar + fixUpName(args[0])
								+ File.separatorChar + args[2]));
				out.write(args[1]);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;

		case DELASSIGN:
			try {
				File dir = new File(topFolder + File.separatorChar
						+ fixUpName(args[0]));
				if (dir.isDirectory()) {
					// check is assign exists, and delete
					File assign = new File(topFolder + File.separatorChar
							+ fixUpName(args[0]) + File.separatorChar + args[1]);
					if (assign.exists()) {
						assign.delete();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
			
		case ARCHIVEASSIGN:
			try {
				File dir = new File(archiveFolder + File.separatorChar
						+ fixUpName(args[0]));
				if (!dir.isDirectory()) {
					dir.mkdir();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(
						archiveFolder + File.separatorChar + fixUpName(args[0])
								+ File.separatorChar + args[2]));
				out.write(args[1]);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
			
		case NEXTASSID:
		    return incrementIDFile(assid);

        case NEXTPKGID:
            return incrementIDFile(pkgid);

        default:
			return "false";

		}

	}
	
	/**
	 * Returns the integer value stored in the file after it was incremented.
	 * @param filename The filename.
	 * @return the integer value stored in the file after it was incremented.
	 */
	private Integer incrementIDFile(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line = in.readLine();
            Integer id = Integer.parseInt(line);
            Integer nid = new Integer(id.intValue() + 1);
            in.close();

            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(nid.toString());
            out.close();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return new Integer(-1);
        }
	}

}
