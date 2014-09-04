package gov.nih.nci.protegex.dialog;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import gov.nih.nci.protegex.batch.BatchLoadByNameTask;
import gov.nih.nci.protegex.batch.BatchTask;
import gov.nih.nci.protegex.edit.NCIEditTab;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;


/**
 * User: matthewhorridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Sep 12, 2005<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 *
 * Modified by Kim Ong, NGIT
 */

public class TaskProgressDialog extends JDialog implements ActionListener {
	public static final long serialVersionUID = 923456030L;

    private JProgressBar progressBar;

    JButton cancelButton;
    JButton startButton;

    private BatchTask task;

    private static final int DISPLAY_DELAY = 1000;

    private JTextArea primaryMessage;

    private JTextArea secondaryMessage;

    boolean cancelled;

    BatchProc proc;
    boolean canCancel;

    private int num_completed = 0;

    OWLModel owlModel;

    public TaskProgressDialog(JFrame owner, String title, BatchTask task) {
   		super(owner, title, true);

   		this.task = task;

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        createUI(owner);

        cancelled = false;

        owlModel = NCIEditTab.getActiveOWLModel();
    }

    public BatchTask getTask()
    {
		return task;
	}

    public boolean isCancelled()
    {
		return cancelled;
	}

    public void setCancelled(boolean b)
    {
		cancelled = b;
	}

    private void createUI(Frame owner) {
        progressBar = new JProgressBar();
        setProgressIndeterminate(false);

        primaryMessage = new JTextArea(1, 30);

        primaryMessage.setEditable(false);

        primaryMessage.setOpaque(false);
        primaryMessage.setWrapStyleWord(true);
        primaryMessage.setLineWrap(true);
        primaryMessage.setFont(primaryMessage.getFont().deriveFont(12.0f));

        JPanel messagePanel = new JPanel(new BorderLayout(0, 0));
        messagePanel.add(primaryMessage, BorderLayout.NORTH);
        secondaryMessage = new JTextArea(1, 30);//50
        secondaryMessage.setEditable(false);
        secondaryMessage.setWrapStyleWord(true);
        secondaryMessage.setLineWrap(true);
        secondaryMessage.setOpaque(false);
        secondaryMessage.setFont(secondaryMessage.getFont().deriveFont(11.0f));
        messagePanel.add(secondaryMessage, BorderLayout.SOUTH);

        JPanel displayPanel = new JPanel(new BorderLayout(12, 12));
        displayPanel.add(messagePanel, BorderLayout.NORTH);
        displayPanel.add(progressBar, BorderLayout.SOUTH);
        JPanel holderPanel = new JPanel(new BorderLayout(7, 7));
        holderPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        holderPanel.add(displayPanel, BorderLayout.NORTH);

 		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

        canCancel = task.isPossibleToCancel();
		if (canCancel)
		{
			cancelButton.setEnabled(canCancel);
		}

        JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonHolder.add(cancelButton);

        holderPanel.add(buttonHolder, BorderLayout.SOUTH);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(holderPanel);

        cancelButton.setEnabled(task.isPossibleToCancel());
        setTitle(task.getTitle());
        setMessage(task.getMessage());

        progressBar.setMinimum(task.getProgressMin());
        progressBar.setMaximum(task.getProgressMax());

        setProgress(task.getProgressMin());
		//Rectangle p0Rect = progressBar.getBounds();

        Rectangle locRec = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        this.setLocation(locRec.getLocation().x + locRec.width / 3 - getSize().width / 3,
                locRec.getLocation().y + locRec.height / 3 - getSize().height / 3);
		proc = new BatchProc(this);

	}

    public int getNumCompleted()
    {
		return num_completed;
	}

	public void run()
	{
		proc.start();
		pack();
		this.setVisible(true);

	}


    public void setProgress(int progress) {
        progressBar.setValue(progress);
		Rectangle labelRect = primaryMessage.getBounds();
		labelRect.x = 0;
		labelRect.y = 0;
		primaryMessage.paintImmediately( labelRect );

    }


    public void setProgressIndeterminate(boolean b) {
        progressBar.setIndeterminate(b);
    }


    public void setMessage(String message) {
		primaryMessage.setText(message);

		Rectangle labelRect = primaryMessage.getBounds();
		labelRect.x = 0;
		labelRect.y = 0;
		primaryMessage.paintImmediately( labelRect );
    }


    public void setSubTaskMessage(String message) {
        secondaryMessage.setText(message);
    }


    public long getDisplayDelay() {
        return DISPLAY_DELAY;
    }


    public boolean isModal() {
        return true;
    }


 	class BatchProc extends Thread {

        TaskProgressDialog tpd = null;

		public BatchProc(TaskProgressDialog tpd) {
			this.tpd = tpd;
 		}

		public void run()
		{
			num_completed = 0;
			
            int i = 0;

			tpd.setCancelled(false);
			boolean cancelled = tpd.isCancelled();

			if (cancelled)
			{
				task.print("WARNING: cancelled before while loop.");
			}

			boolean eventsEnabled = owlModel.setGenerateEventsEnabled(false);
			
			BatchLoadByNameTask.create_time = 0;
			BatchLoadByNameTask.evs_time = 0;
			long beg = System.currentTimeMillis();
			//task.print("Processing:");
			while (!cancelled && i < tpd.getTask().getProgressMax())
			{
				owlModel = NCIEditTab.getActiveOWLModel();

                
                
                if (task.processTask(i++)) {
                	num_completed++;
                }
				
                


				String message = "Completed " + (i+1) + " out of " + tpd.getTask().getProgressMax();
				tpd.setSubTaskMessage(message);
				tpd.setProgress(i);
				cancelled = tpd.isCancelled();

				if (cancelled) break;
                
                // now sleep for a bit to let things settle
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    task.print("Sleeper interrupted " + e.getLocalizedMessage());
                }

			}
			long total = System.currentTimeMillis() - beg;
			task.print("Total run time was: " + total + " ms");
			//task.print("Time spent in create was: " + BatchLoadTask.create_time + " ms");
			//task.print("Time spent in evs history was: " + BatchLoadTask.evs_time + " ms");
			owlModel.setGenerateEventsEnabled(eventsEnabled);
            
			task.print("Total successful completion: " + num_completed + " out of: " + i);

			if (cancelled)
			{
				task.print("Process interrupted by the user.");
			}

			//task.print("run completion at: " + i);
			//num_completed = i;
			dispose();

		}
	}



	public void actionPerformed(ActionEvent event)
	{
		Object action = event.getSource();

		if (action == cancelButton)
		{
			task.print("Interrupting proc thread...");
			cancelButton.setEnabled(false);
			setCancelled(true);
		}
	}
}

