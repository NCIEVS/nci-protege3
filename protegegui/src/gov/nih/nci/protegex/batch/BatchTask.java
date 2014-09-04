package gov.nih.nci.protegex.batch;

import static gov.nih.nci.protegex.batch.BatchTask.TaskType.LOAD;
import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.panel.BatchPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @Author: Bob Dionne
 */
public class BatchTask {

	public static enum TaskType {
		LOAD, EDIT
	};

	BatchPanel tab = null;

	boolean done = false;

	private boolean canProceed = true;

	public boolean canProceed() {
		return canProceed;
	}

	String infile = null;

	String outfile = null;

	Vector<String> data_vec = null;

	String message;
	int max = 10000;
	int min = 0;
	boolean cancelled = false;
	boolean canCancel = true;
	String title = null;

	PrintWriter pw = null;
	TaskType batchtype = LOAD;

	public BatchTask(BatchPanel tab) {
		this.tab = tab;
		setMax(10000);
		cancelled = false;
		String title = "Batch Processing";
		setTitle(title);
		setMessage("Batch processing in progress, please wait ...");
	}

	public void setType(TaskType type) {
		batchtype = type;
	}

	protected void setMax(int max) {
		this.max = max;
	}

	public void cancelTask() {
		closePrintWriter();
		cancelled = true;
	}

	/**
	 * Gets the title for this task.
	 */

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setPrintWriter(PrintWriter pw) {
		this.pw = pw;
	}

	public PrintWriter openPrintWriter(String outputfile) {
		if (outputfile == null)
			return null;
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(outputfile)));
			this.pw = writer;
			print(getToday() + "\n");
			return writer;
		} catch (Exception e) {
			return null;
		}
	}

	public void print(String msg) {
		if (pw != null) {
			pw.println(msg);
		}
		tab.getTextArea().append(msg + "\n");

	}

	public void closePrintWriter() {
		if (pw == null)
			return;
		try {
			pw.close();
			pw = null;
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

	/**
	 * Gets the minimum progress value for this task.
	 */
	public int getProgressMin() {
		return min;
	}

	/**
	 * Gets the maximum progress value for this task.
	 */
	public int getProgressMax() {
		return max;
	}

	/**
	 * Checks whether this Task has been cancelled. Unless either method is
	 * overloaded, this will return true after cancelTask has been called (e.g.,
	 * via the cancel button).
	 * 
	 * @return true if this has been cancelled
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	public void setCanCancel(boolean b) {
		canCancel = b;
	}

	/**
	 * Determines if the task can be cancelled
	 * 
	 * @return <code>true</code> if the task can be cancelled, or
	 *         <code>false</code> if the task cannot be cancelled.
	 */
	public boolean isPossibleToCancel() {
		return canCancel;
	}

	public boolean processTask(int taskId) {

		return true;
	}

	public boolean 	checkNoErrors(Vector<String> w, int i) {
		Vector<String> errors = this.validateData(w);
		if (errors.size() > 0) {
			//data_vec.remove(i);
			//this.setMax(max - 1);
			for (int j = 0; j < errors.size(); j++) {
				print("record " + (i+1) + ": " + errors.elementAt(j));
			}
			return false;

		}
		return true;
	}

	public String getToday() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
		String today = sdf.format(cal.getTime());
		return today;
	}

	public Vector<String> getData(String filename) {
		Vector<String> v = new Vector<String>();
		if (filename == null)
			return v;
		String s;
		int knt = 0;
		BufferedReader inFile = null;
		try {
			inFile = new BufferedReader(new FileReader(filename));
			while ((s = inFile.readLine()) != null) {
				s = s.trim();
				if (s.length() > 0) {
					if (s.startsWith("#")) {
						// ignore comment lines
					} else {
						v.add(s);
						knt++;
					}
				}
			}
			inFile.close();
		} catch (Exception e) {
			System.err.println(e);
		}
		return v;
	}

	public Vector<String> validateData(Vector<String> v) {
		return null;
	}

	public Vector<String> getTokenStr(String value, int length) {
		Vector<String> tokenValues = new Vector<String>();
		String s = value + "\t\t\t\t\t\t\t\t\t";
		int n = s.indexOf("\t");
		int i = 0;
		while (n != -1 && i < length) {
			String t = s.substring(0, n);
			tokenValues.add(t);
			s = s.substring(n + 1);
			i++;
			n = s.indexOf("\t");
		}
		for (i = 0; i < length; i++) {
			s = (String) tokenValues.elementAt(i);
			if (s.compareTo("") == 0) {
				tokenValues.setElementAt("NA", i);
			}
		}
		return tokenValues;
	}

	public String removeTabs(String s) {
		String rets = s;
		while (rets.startsWith("\t")) {
			rets = rets.substring(1);
		}
		return rets;
	}

	public String removeRtnChar(String line) {
		String s = line;
		char c = s.charAt(s.length() - 1);
		if (c == '\n') {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public String getStringName(String line) {
		String s = line;
		int pos = s.indexOf(":");
		if (pos == -1)
			return "";
		return s.substring(0, pos);
	}

	public String getStringValue(String line) {
		String s = line;
		int pos = s.indexOf(":");
		String t1 = s.substring(pos + 1);
		return (removeTabs(t1));
	}

}
