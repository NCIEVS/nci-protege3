/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

public class DiffIconDialog extends JDialog{

	Icon [] icons =
		{
			null,
			DiffIcons.getTreeAddedIcon(false, false),
			DiffIcons.getTreeDeletedIcon(false, false),
			DiffIcons.getTreeChangedIcon(false, false),
			DiffIcons.getTreeMovedFromIcon(false, false),
			DiffIcons.getTreeMovedToIcon(false, false),
			DiffIcons.getTreeWithWarningIcon(false, false),
			DiffIcons.getRegularTreeIcon(false, false)
		};
		
	JLabel [] fonts = 
		{
			null,
			createBlueUnderlinedLabel (),
			createRedCrossedOutLabel(),
			createBoldLabel (),
			createBlueBoldLabel(),
			createGrayLabel(),
			createRegularFontLabel ()
		};

	// Initialize array of textual descriptions for above icons.
	String[] iconText =
		{
			"Icons (indicate change type for the whole subtree)",
			"Subtree added",
			"Subtree deleted",
			"All classes in subtree changed",
			"Subtree moved elsewhere",
			"Subtree moved from elsewhere",
			"Different changes in the subtree",
			"No changes in the subtree",
		};
		
	String[] fontText = 
		{
			"Fonts (indicate change type for the specific class)",
			"Class added",
			"Class deleted",
			"Class changed",
			"Class moved from elsewhere",
			"Class moved elsewhere",
			"No changes at class"
		};

	private  JLabel createBlueUnderlinedLabel() {
		return new JLabel ("<html><font face = \"" + getFont() + "\">" +
							"<font color = blue><u>" + "blue, underlined" + "</u></font> " +
							"</html>");
	}

	private  JLabel createRedCrossedOutLabel() {
		return new JLabel ("<html><font face = \"" + getFont() + "\">" +
							"<font color = red><s>" + "red, crossed-out" + "</s></font> " +
							"</html>");
	}

	private  JLabel createBoldLabel() {
		return new JLabel ("<html><font face = \"" + getFont() + "\">" +
							"<b>" + "bold" + "</b></font> " +
							"</html>");
	}

	private  JLabel createBlueBoldLabel() {
		return new JLabel ("<html><font face = \"" + getFont() + "\">" +
							"<font color = blue><b>" + "blue, bold" + "</b></font> " +
							"</html>");
	}

	private  JLabel createGrayLabel() {
		return new JLabel ("<html><font face = \"" + getFont() + "\">" +
							"<font color = gray>" + "gray" + "</font> " +
							"</html>");
	}

	private  JLabel createRegularFontLabel() {
		return new JLabel ("<html><font face = \"" + getFont() + "\">" +
							"regular" + "</font> " +
							"</html>");
	}

	public DiffIconDialog() {
		this(null, "", false);
	}

	public DiffIconDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			init();
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeButton_actionPerformed(ActionEvent ae) {
		this.setVisible(false);
	}

	private int getMaxIconHeight() {
		int maxHeight = 0;
		for (int i = 0; i < icons.length; i++) {
			if (icons[i] != null) {
				int height = ((Icon)icons[i]).getIconHeight()+3;
				if (height > maxHeight) {
					maxHeight = height;
				}
			}
		}
		return maxHeight;
	}

	private int getMaxIconWidth() {
		int maxWidth = 0;
		for (int i = 0; i < icons.length; i++) {
			if (icons[i] != null) {
				int width = ((Icon)icons[i]).getIconWidth();
				if (width > maxWidth) {
					maxWidth = width;
				}
			}
		}
		return maxWidth;
	}

	private int getMaxFontHeight() {
		int maxHeight = 0;
		for (int i = 0; i < fonts.length; i++) {
			if (icons[i] != null) {
				int height = ((JLabel)fonts[i]).getHeight()+3;
				if (height > maxHeight) {
					maxHeight = height;
				}
			}
		}
		return maxHeight;
	}

	private int getMaxFontWidth() {
		int maxWidth = 0;
		for (int i = 0; i < fonts.length; i++) {
			if (icons[i] != null) {
				int width = (int) fonts[i].getPreferredSize().getWidth() + 5;
				if (width > maxWidth) {
					maxWidth = width;
				}
			}
		}
		return maxWidth;
	}

	private JTextField getTextHeader(String text) {
		JTextField textField = new JTextField(text);
		textField.setEnabled(false);
		textField.setBackground(getBackground());
		textField.setDisabledTextColor(getForeground());
		textField.setBorder(null);
		textField.setFont(new Font("Dialog", Font.BOLD, 14));
		return textField;
	}

	private void init() throws Exception {
		JPanel iconPanel = new JPanel(new GridLayout(icons.length + fonts.length, 1));
		
		addIcons (iconPanel);
		addFonts (iconPanel);

		// Put icon grid in a scroll pane.
		JScrollPane scrollPane = new JScrollPane(iconPanel);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Build button panel for the "Close" button.
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				closeButton_actionPerformed(ae);
			}
		});
		buttonPanel.add(closeButton);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private void addIcons (JPanel iconPanel) {
		// Get the max width and height of the icons to display.
		int maxIconWidth = getMaxIconWidth();
		int maxIconHeight = getMaxIconHeight();
		Dimension iconDimension = new Dimension(maxIconWidth, maxIconHeight);

		// Populate grid layout with icons and their descriptions.
		for (int i = 0; i < icons.length; i++) {
			JPanel panel = new JPanel(new BorderLayout(15, 0));

			if (icons[i] == null) {
				// This will be a section header.
				JTextField header = getTextHeader(iconText[i]);
				panel.add(header, BorderLayout.CENTER);
			} else {
				// Create a label for the icon.
				JLabel icon = ComponentFactory.createLabel((Icon)icons[i], SwingConstants.CENTER);
				icon.setPreferredSize(iconDimension);
				panel.add(icon, BorderLayout.WEST);

				// Create a label for the icon description.
				JLabel iconDescription = ComponentFactory.createLabel(iconText[i], SwingConstants.LEFT);
				panel.add(iconDescription, BorderLayout.CENTER);
			}
			iconPanel.add(panel);
		}
	}
	
	private void addFonts (JPanel iconPanel) {
		// Get the max width and height of the icons to display.
		int maxFontWidth = getMaxFontWidth();
		int maxFontHeight = getMaxFontHeight();
		Dimension fontDimension = new Dimension(maxFontWidth, maxFontHeight);

		// Populate grid layout with icons and their descriptions.
		for (int i = 0; i < fonts.length; i++) {
			JPanel panel = new JPanel(new BorderLayout());

			if (fonts[i] == null) {
				// This will be a section header.
				JTextField header = getTextHeader(fontText[i]);
				panel.add(header, BorderLayout.CENTER);
			} else {
				// Create a label for the icon.
				fonts[i].setPreferredSize(fontDimension);
				panel.add(fonts[i], BorderLayout.WEST);

				// Create a label for the icon description.
				JLabel fontDescription = ComponentFactory.createLabel(fontText[i], SwingConstants.LEFT);
				panel.add(fontDescription, BorderLayout.CENTER);
			}
			iconPanel.add(panel);
		}
	}
}
