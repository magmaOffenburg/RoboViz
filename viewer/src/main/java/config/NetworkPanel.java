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

import config.RVConfigure.SaveListener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
	JTextField serverHostTF;
	JTextField serverPortTF;
	JTextField drawingPortTF;
	JTextField autoConnectDelayTF;

	public NetworkPanel(RVConfigure configProg)
	{
		this.config = configProg.config.networking;
		configProg.listeners.add(this);
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
		l.setPreferredSize(new Dimension(60, 28));
		component.add(l, c);
	}

	JPanel initServerControls()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Server"));

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 10;
		c.fill = GridBagConstraints.HORIZONTAL;

		addLabel("Host: ", panel, c, 0, 0);

		c.gridx = 1;
		c.gridy = 0;
		serverHostTF = new JTextField(config.serverHost);
		serverHostTF.setPreferredSize(new Dimension(150, 28));
		panel.add(serverHostTF, c);

		addLabel("Port: ", panel, c, 0, 1);

		c.gridx = 1;
		c.gridy = 1;
		serverPortTF = new PortTextField(config.serverPort);
		panel.add(serverPortTF, c);

		addLabel("Delay: ", panel, c, 0, 2);

		c.gridx = 1;
		c.gridy = 2;
		autoConnectDelayTF = new IntegerTextField(config.autoConnectDelay, 1, Integer.MAX_VALUE);
		panel.add(autoConnectDelayTF, c);

		c.gridx = 1;
		c.gridy = 3;
		autoConnectCB = new JCheckBox("Auto-Connect", config.autoConnect);
		autoConnectCB.addChangeListener(e -> updateAutoConnectEnabled());
		updateAutoConnectEnabled();

		panel.add(autoConnectCB, c);

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
		panel.add(drawingPortTF, c);

		return panel;
	}

	@Override
	public void configSaved(RVConfigure configProg)
	{
		config.serverHost = serverHostTF.getText();

		try {
			config.serverPort = Integer.parseInt(serverPortTF.getText());
		} catch (Exception e) {
			serverPortTF.setText("" + config.serverPort);
		}

		config.autoConnect = autoConnectCB.isSelected();

		try {
			config.autoConnectDelay = Integer.parseInt(autoConnectDelayTF.getText());
		} catch (Exception e) {
			autoConnectDelayTF.setText("" + config.autoConnectDelay);
		}

		try {
			config.listenPort = Integer.parseInt(drawingPortTF.getText());
		} catch (Exception e) {
			drawingPortTF.setText("" + config.listenPort);
		}
	}
}
