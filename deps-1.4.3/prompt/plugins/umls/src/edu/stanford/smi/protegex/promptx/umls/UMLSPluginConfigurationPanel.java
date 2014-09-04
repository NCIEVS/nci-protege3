/*
 * Contributor(s): Natasha Noy noy@stanford.edu
 */
package edu.stanford.smi.protegex.promptx.umls;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;

public class UMLSPluginConfigurationPanel extends ComparisonAlgorithmPluginConfigurationPanel{
	private JTextField _driver = null;
	private JTextField _jdbcUrl = null;
	private JTextField _userName = null;
	private JPasswordField _password = null;

	public UMLSPluginConfigurationPanel () {
		DBParameters.loadParams();
		_driver = ComponentFactory.createTextField (DBParameters.getDriver());
		_jdbcUrl = ComponentFactory.createTextField (DBParameters.getJdbcUrl());
		_userName = ComponentFactory.createTextField (DBParameters.getUserName());
		_password = ComponentFactory.createPasswordField ();
		_password.setText(DBParameters.getPassword ());
		
		
		LabeledComponent driverName = new LabeledComponent ("JDBC driver class name", _driver);
		LabeledComponent ipAddress = new LabeledComponent ("JDBC URL  for the UMLS database", _jdbcUrl);
		LabeledComponent userName = new LabeledComponent ("User name", _userName);
		LabeledComponent password = new LabeledComponent ("Password", _password);
		
		setLayout (new BorderLayout());
		JPanel insidePanel = new JPanel();
		insidePanel.setLayout (new GridLayout (4, 0));
		insidePanel.add (driverName);
		insidePanel.add (ipAddress);
		insidePanel.add (userName);
		insidePanel.add (password);
				
		add (insidePanel, BorderLayout.CENTER);
	}


	public void setDBConfigurationParameters() {
		DBParameters.setDriver (_driver.getText());
		DBParameters.setJdbcUrl (_jdbcUrl.getText());
		DBParameters.setPassword (new String (_password.getPassword()));
		DBParameters.setUserName (_userName.getText());
	}

	

}
