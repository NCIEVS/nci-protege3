package edu.stanford.smi.protegex.widget.editorpane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.BrowserLauncher;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.widget.TextComponentWidget;

/**
 * Slot widget for detecting hyperlinks in entered text and opening
 * the link in a new browser when it is clicked.
 * It also supports simple HTML tags, such as bold, italics, underline,
 * strike-through and inserting images.
 * It also detects links to internal ontologies entities and opens
 * them in a separate window when clicked.
 *
 *  @author Vivek Tripathi (vivekyt@stanford.edu)
 */
public class EditorPaneWidget extends TextComponentWidget {

	private static final long serialVersionUID = -1198463792463053162L;
	private EditorPaneComponent epc;
	private LabeledComponent labeledComponent;
	JComboBox internalLinkTo;
	private final String EditorPaneHelpURL = "http://protegewiki.stanford.edu/index.php/EditorPane";

	@Override
	protected JComponent createCenterComponent(JTextComponent textComponent) {
		return ComponentFactory.createScrollPane(textComponent);
	}


	@Override
	protected JTextComponent createTextComponent() {
		return createEditorPane();
	}

	public JEditorPane getEditorPane() {
		return (JEditorPane) getTextComponent();
	}

	public JEditorPane createEditorPane() {
		epc = new EditorPaneComponent();

		KnowledgeBase kb = ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase();
		String[] options1 = { "Add Internal Link To" , "Class", "Property" , "Individual"};
		String[] options2 = { "Add Internal Link To" , "Class", "Slot", "Instance"  };


		if(PluginUtilities.isOWL(kb))
		{
			epc.setOWLMode(true);
			internalLinkTo = new JComboBox(options1);

		}
		else
		{
			epc.setOWLMode(false);
			internalLinkTo = new JComboBox(options2);

		}

		internalLinkTo.setRenderer(new FrameRenderer() {
			@Override
			public void load(Object value) {
				if (value.equals("Class")) {
					setMainIcon(Icons.getClsIcon());
					setMainText("Class");
				} else if (value.equals("Slot")) {
					setMainIcon(Icons.getSlotIcon());
					setMainText("Slot");
				} else if (value.equals("Instance")) {
					setMainIcon(Icons.getInstanceIcon());
					setMainText("Instance");
				}
				else if (value.equals("Property")) {
					setMainIcon(Icons.getSlotIcon());
					setMainText("Property");
				} else if (value.equals("Individual")) {
					setMainIcon(Icons.getInstanceIcon());
					setMainText("Individual");
				} else {
					super.load(value);
				}
			}
		}
		);

		internalLinkTo.setSelectedIndex(0);
		internalLinkTo.addActionListener(epc.getAddInternalLinkActionListener());

	//	add(internalLinkTo, BorderLayout.BEFORE_FIRST_LINE);
		EditorPaneLinkDetector epl = epc.createEditorPaneLinkDetector();
	//	lc = new LabeledComponent("", epl, false, true);
//		lc.setHeaderComponent(internalLinkTo, BorderLayout.WEST);
		return epl;
	}


	public void initialize() {
		super.initialize(true, true, 2, 2);
		labeledComponent = (LabeledComponent) getComponent(0);
		JButton helpButton = new JButton("?");
		addButtons(labeledComponent);

		helpButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
			    	try{
			    		BrowserLauncher.openURL(EditorPaneHelpURL);
			    	}
			    	catch (IOException e1){
			    		;
			    	}
		    	}

		});

		JComponent lc1 = new JPanel();
		lc1.add(internalLinkTo, BorderLayout.WEST);
		lc1.add(helpButton, BorderLayout.EAST);
		labeledComponent.setHeaderComponent(lc1,BorderLayout.WEST);

	}


	@Override
	protected Collection<AllowableAction> createActions() {
		ArrayList<AllowableAction> actions = new ArrayList<AllowableAction>();
/*
		AllowableAction b = epc.getBoldAction();
		actions.add(b);


		AllowableAction i = epc.getItalicsAction();
		actions.add(i);


		AllowableAction u = epc.getUnderlineAction();
		actions.add(u);


		AllowableAction s = epc.getStrikeThroughAction();
		actions.add(s);

		AllowableAction h = epc.getHighligherAction();
		actions.add(h);

		AllowableAction insert_image = epc.getInsertImageAction();
		actions.add(insert_image);
		*/
		return actions;
	}


	@Override
	public Collection getValues() {
		String s = getText();
		String modifiedS;
		// this functions strips off the </p> from text. Also it inserts <br> for all newlines
		// which user had entered
		if(s != null) {
			modifiedS = insertBRForNewline(s);
		} else {
			modifiedS = s;
		}

		return CollectionUtilities.createList(modifiedS);
	}

	@Override
	public void setValues(Collection values) {
		Object o = CollectionUtilities.getFirstItem(values);
		String text = o == null ? (String) null : o.toString();

		// the text that we are reading from the knowledge base can be html enabled with
		// html tags or it can be plain text in which case we need to interpret all
		// the hyperlinks on the fly!
		if(text != null && text.indexOf("<html>") == -1 && text.indexOf("<body>") == -1 && text.indexOf("<a href") == -1)
		{
			// so the text in knowledge base doesn't have html tags and we need
			// to detect all the hyperlinks now in order to display them
			String htmltext = enableLinkInPlainText(text);
			setText(htmltext);
		}
		else
		{
			// text in knowledge base is html enabled. so just display it.
			setText(text);
		}

	}

	// this functions strips off the </p> from text. Also it inserts <br> for all newlines
	// which user had entered
	private String insertBRForNewline(String s)
	{
		boolean pPresent = false;
		int pAtIndex = -1;
		int pStyleAtIndex = -1;

		// find index from where user entered text starts
		int beginIndex = s.indexOf("<body>") + "<body>".length()+ 1;

		// s1 contains initial html tags before the user entered text
		String s1 = s.substring(0, beginIndex);

		// s2 contains the user entered text along with intermediate html tags
		String s2 = s.substring(beginIndex, s.indexOf("</body>") - 1);

		// s3 contains finishing html tags after the user entered text
		String s3 = s.substring(s.indexOf("</body>"));

		String modifiedS2;

		if(s2.indexOf("</p>") != -1) // this means html body recognizes
		{							 // new lines in the text and puts
			// </p> for each new line
			pPresent = true;
			modifiedS2 = s2;
			String part1, part2, part3;

			// find where first </p> and <p style is present in the text
			pAtIndex = s2.indexOf("</p>");
			pStyleAtIndex = s2.indexOf("<p style");

			while(pPresent)
			{
				// next 3 lines replace <p style="margin-top: 0"> with null
				part1 = modifiedS2.substring(0, pStyleAtIndex);
				part2 = modifiedS2.substring(pStyleAtIndex + "<p style=\"margin-top: 0\">".length()+1);
				modifiedS2 = part1 + part2;

				// next 5 lines replace </p> with <br>
				pAtIndex = modifiedS2.indexOf("</p>");
				part1 = modifiedS2.substring(0, pAtIndex);
				part2 = "<br>";
				part3 = modifiedS2.substring(pAtIndex + "</p".length()+1);
				modifiedS2 = part1 + part2 + part3;

				// here we check if more replacement needs to be done else we exit the loop
				pStyleAtIndex = modifiedS2.indexOf("<p style");
				if(pStyleAtIndex < 0)
				{
					pPresent =  false;
					break;
				}

			}

			// since we are writing html to the pins file, we don't need any \n. We
			// already have put <br> wherever newlines were starting
			modifiedS2 = modifiedS2.replaceAll("\n    ","");
			modifiedS2 = modifiedS2.replaceAll("\n","");

		}
		else
		{
			// this means that editor pane has not accounted for the new lines
			// which the user might have entered and hence we need to replace
			// all \n with <br>
			s2 = s2.replaceAll("\n    ", "");
			modifiedS2 = s2.replaceAll("\n", "<br>");

		}

		// to remove the last <br> from this string. otherwise extra <br> shows up in the end


		int index = modifiedS2.lastIndexOf("<br>");
		if (index > -1) {
			modifiedS2 = modifiedS2.substring(0, index) + modifiedS2.substring(index+5);
		}
		int length = modifiedS2.length();

		// here we obtain the part which we will be removing in "remainder" string
		// and check that it doesn't contain any actual text apart from <br> and spaces
		String remainder = modifiedS2.substring(length-1);
		remainder = remainder.replaceAll("<br>","");
		remainder = remainder.replaceAll(" ","");
		if(remainder.length() == 0) // this means that the part we are removing from
		{  							// modifiedText just contains one <br> and some spaces
			modifiedS2 = modifiedS2.substring(0, length-1);
		}
		//else the part we want to remove also contains some other characters
		// and we don't change modifiedText since we don't want to get into trouble
		// by removing anything other than spaces and <br>.

		// we were operating on the middle part of entire string. now we
		// complete the original string and return it.
		String modifiedS = s1 + modifiedS2 + s3;
		return modifiedS;
	}

	private int findMin(int a, int b, int c)
	{
		if(a < b && a < c && a != -1) {
			return a;
		}
		if(b < a && b < c && b != -1) {
			return b;
		}
		if(c < a && c < b && c != -1) {
			return c;
		}
		if(a == -1) {
			return findMin(b, c);
		}
		if(b == -1) {
			return findMin(a, c);
		}
		if(c == -1) {
			return findMin(a, b);
		}
		return -1;
	}

	private int findMin(int a, int b)
	{
		if(a < b && a != -1) {
			return a;
		}
		if(b < a && b != -1) {
			return b;
		}
		if(a == -1 && b != -1) {
			return b;
		}
		if(b == -1 && a != -1) {
			return a;
		}
		return -1;
	}

	private String enableLinkInPlainText(String text)
	{
		// text is plain text. so it has \n to represent new lines. but
		// html doesnt understand \n. so we replace all \n with <br>
		String htmltext = text.replaceAll("\n", "<br>");

		int startSearch = 0;
		int linkIndex;
		int linkIndex1, linkIndex2, linkIndex3, linkIndex4, linkIndex5, linkEnds, linkEnds1, linkEnds2;
		boolean linkPresent = false;

		// if there is no link in this plain text, we dont have to do anything
		// else we need to parse links and enable them
		if(htmltext.indexOf("http:") != -1 || htmltext.indexOf("www.") != -1 || htmltext.indexOf("mailto:") != -1 || htmltext.indexOf("ftp://") != -1 || htmltext.indexOf("file:/") != -1 ) {
			linkPresent = true;
		}

		while(linkPresent)
		{
			linkIndex = -1;
			// if http: is present then find its index
			linkIndex1 = htmltext.indexOf("http:", startSearch);

			// if www. is present then find its index
			linkIndex2 = htmltext.indexOf("www.", startSearch);

			// if mailto: is present then find its index
			linkIndex3 = htmltext.indexOf("mailto:", startSearch);

			// if file:/ is present then find its index
			linkIndex4 = htmltext.indexOf("file:/", startSearch);

			// if ftp:// is present then find its index
			linkIndex5 = htmltext.indexOf("ftp:/", startSearch);

			if(linkIndex1 == -1 && linkIndex2 == -1 && linkIndex3 == -1 && linkIndex4 == -1 && linkIndex5 == -1)
			{
				// how can we land here! we shouldnt be in this while loop
				linkIndex = 0;
				linkPresent = false;
				break;
			}

			if(linkIndex1 != -1 || linkIndex2 != -1 || linkIndex3 != -1 ) {
				linkIndex = findMin(linkIndex1, linkIndex2, linkIndex3);
			}
			if(linkIndex != -1 || linkIndex4 != -1 || linkIndex5 != -1 ) {
				linkIndex = findMin(linkIndex, linkIndex4, linkIndex5);
			}

			// now we have in the variable linkIndex the place where first link
			// starts so we now find the place where link ends (either link
			// ends with a space or newline
			linkEnds1 = htmltext.indexOf(" ", linkIndex);
			linkEnds2 = htmltext.indexOf("<br>", linkIndex);

			if(linkEnds2 == -1 && linkEnds1 == -1)
			{
				// this means that the link doesnt end!
				// we avoid enabling such links
				linkEnds = 0;
				linkPresent = false;
				break;
			}

			linkEnds = findMin(linkEnds1, linkEnds2);


			// now we have start and end indexes of the first link.
			// we add tags around this link and get new htmltext with first link tagged
			htmltext = htmltext.substring(0, linkIndex) +
			"<a href='" + htmltext.substring(linkIndex, linkEnds) +
			"'>" + htmltext.substring(linkIndex, linkEnds)+ "</a>" +
			htmltext.substring(linkEnds);

			// now next link should be searched only after the place where first link ends
			startSearch = htmltext.indexOf("</a>", linkEnds) + "</a>".length();

			// here we check whether there is next link in the remaining text or not.
			if(htmltext.indexOf("http:",startSearch) == -1 && htmltext.indexOf("www.",startSearch) == -1 && htmltext.indexOf("mailto:",startSearch) == -1 && htmltext.indexOf("ftp://",startSearch) == -1 && htmltext.indexOf("file:/",startSearch) == -1)
			{
				linkPresent = false;
				break;
			}

		}
		// add higher level html tags and body
		htmltext = "<html> \n <head> \n <style type=\"text/css\"> \n <!-- \n body { font-family: arial; font-size: 12pt } \n  p { margin-top: 2; margin-bottom: 2; margin-left: 2; margin-right: 2; font-family: arial } \n  --> \n  </style> \n </head> \n  <body> \n" + htmltext + "\n </body> \n  </html>";
		return htmltext;
	}


	@Override
	public void dispose() {
		EditorPaneLinkDetector epane = (EditorPaneLinkDetector) getEditorPane();
		// remove mouse listeners for cleanup
		epane.dispose();
		super.dispose();
	}

	protected void addButtons(LabeledComponent c) {
        addButton(epc.getBoldAction());
        addButton(epc.getItalicsAction());
        addButton(epc.getUnderlineAction());
        addButton(epc.getStrikeThroughAction());
        addButton(epc.getHighligherAction());
        addButton(epc.getInsertImageAction());
    }

	public void addButton(Action action) {
        addButton(action, true);
    }

    public void addButton(Action action, boolean defaultState) {
        if (action != null) {
            addButtonConfiguration(action, defaultState);
            if (displayButton(action)) {
            	labeledComponent.addHeaderButton(action);
            }
        }
    }

}




