/*
 *  Copyright 2011 RoboViz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package config;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import config.RVConfigure.SaveListener;
import rv.Configuration;

/**
 *
 * @author justin
 *
 */
public class NetworkPanel extends JPanel implements SaveListener
{
	final Configuration.Networking config;
	JCheckBox autoConnectCB;
	JTextField defaultServerHostTF;
	IntegerTextField defaultServerPortTF;
	IntegerTextField drawingPortTF;
	IntegerTextField autoConnectDelayTF;
	JButton serverListBtn;

	private Runnable onChange;

	public NetworkPanel(RVConfigure configProg)
	{
		this.config = configProg.config.networking;
		configProg.listeners.add(this);
		onChange = configProg.updateSaveButtonState;
		initGUI();
	}

	void initGUI()
	{
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		c.gridy = 0;
		add(initServerControls(), c);

		c.gridy = 1;
		add(initDrawingControls(), c);
	}

	void addLabel(String name, JComponent component, GridBagConstraints c, int x, int y)
	{
		c.gridx = x;
		c.gridy = y;
		JLabel l = new JLabel(name, SwingConstants.RIGHT);
		l.setPreferredSize(new Dimension(80, 28));
		component.add(l, c);
	}

	JPanel initServerControls()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Server"));

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 10;
		c.fill = GridBagConstraints.HORIZONTAL;

		addLabel("Default Host: ", panel, c, 0, 0);

		c.gridx = 1;
		c.gridy = 0;
		defaultServerHostTF = new JTextField(config.defaultServerHost);
		defaultServerHostTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				config.defaultServerHost = defaultServerHostTF.getText();
				onChange.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				config.defaultServerHost = defaultServerHostTF.getText();
				onChange.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				config.defaultServerHost = defaultServerHostTF.getText();
				onChange.run();
			}
		});
		defaultServerHostTF.setPreferredSize(new Dimension(150, 28));
		panel.add(defaultServerHostTF, c);

		addLabel("Default Port: ", panel, c, 0, 1);

		c.gridx = 1;
		c.gridy = 1;
		defaultServerPortTF = new PortTextField(config.defaultServerPort);
		defaultServerPortTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateDefaultServerPortConfig(false);
				onChange.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateDefaultServerPortConfig(false);
				onChange.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateDefaultServerPortConfig(false);
				onChange.run();
			}
		});
		panel.add(defaultServerPortTF, c);

		addLabel("Delay: ", panel, c, 0, 2);

		c.gridx = 1;
		c.gridy = 2;
		autoConnectDelayTF = new IntegerTextField(config.autoConnectDelay, 1, Integer.MAX_VALUE);
		autoConnectDelayTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateAutoConnectDelayConfig(false);
				onChange.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateAutoConnectDelayConfig(false);
				onChange.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateAutoConnectDelayConfig(false);
				onChange.run();
			}
		});
		panel.add(autoConnectDelayTF, c);

		c.gridx = 1;
		c.gridy = 3;
		autoConnectCB = new JCheckBox("Auto-Connect", config.autoConnect);
		autoConnectCB.addChangeListener(e -> {
			updateAutoConnectEnabled();
			config.autoConnect = autoConnectCB.isSelected();
			onChange.run();
		});
		updateAutoConnectEnabled();
		panel.add(autoConnectCB, c);

		c.gridx = 1;
		c.gridy = 4;
		serverListBtn = new JButton("Server List...");
		serverListBtn.addActionListener(e -> {
			ServerListDialog serverListDialog =
					new ServerListDialog((JFrame) SwingUtilities.getWindowAncestor(this), config.servers);
			serverListDialog.setVisible(true);
			onChange.run();
		});
		panel.add(serverListBtn, c);

		return panel;
	}

	void updateAutoConnectEnabled()
	{
		autoConnectDelayTF.setEnabled(autoConnectCB.isSelected());
	}

	JPanel initDrawingControls()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Drawings"));

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 10;
		c.fill = GridBagConstraints.HORIZONTAL;

		addLabel("Port: ", panel, c, 0, 0);

		c.gridx = 1;
		c.gridy = 0;
		drawingPortTF = new PortTextField(config.listenPort);
		drawingPortTF.setPreferredSize(new Dimension(150, 28));
		drawingPortTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateListenPort(false);
				onChange.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateListenPort(false);
				onChange.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateListenPort(false);
				onChange.run();
			}
		});
		panel.add(drawingPortTF, c);

		return panel;
	}

	private void updateDefaultServerPortConfig(boolean resetOnError)
	{
		try {
			config.defaultServerPort = defaultServerPortTF.getInt();
		} catch (Exception e) {
			if (resetOnError) {
				defaultServerPortTF.setText("" + config.defaultServerPort);
			}
		}
	}

	private void updateAutoConnectDelayConfig(boolean resetOnError)
	{
		try {
			config.autoConnectDelay = autoConnectDelayTF.getInt();
		} catch (Exception e) {
			if (resetOnError) {
				autoConnectDelayTF.setText("" + config.autoConnectDelay);
			}
		}
	}

	private void updateListenPort(boolean resetOnError)
	{
		try {
			config.listenPort = drawingPortTF.getInt();
		} catch (Exception e) {
			if (resetOnError) {
				drawingPortTF.setText("" + config.listenPort);
			}
		}
	}

	@Override
	public void configSaved(RVConfigure configProg)
	{
		config.defaultServerHost = defaultServerHostTF.getText();
		updateDefaultServerPortConfig(true);

		updateListenPort(true);

		config.autoConnect = autoConnectCB.isSelected();
		updateAutoConnectDelayConfig(true);
	}
}
