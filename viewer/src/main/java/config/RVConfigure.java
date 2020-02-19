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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import rv.Configuration;
import rv.Globals;
import rv.Viewer;
import rv.util.swing.SwingUtil;

public class RVConfigure extends JFrame
{
	interface SaveListener {
		void configSaved(RVConfigure configProg);
	}

	Configuration config;
	final JButton saveButton;
	final ArrayList<SaveListener> listeners = new ArrayList<>();

	public RVConfigure()
	{
		try {
			config = Configuration.loadFromFile();
		} catch (Exception e) {
			e.printStackTrace();
			config = new Configuration();
		}

		Globals.setLookFeel();
		setTitle("RoboViz Configuration");
		setIconImage(Globals.getIcon());
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;

		c.gridx = 0;
		c.gridy = 0;
		add(new GraphicsPanel(this), c);

		JPanel networkPanel = new NetworkPanel(this);
		c.gridx = 0;
		c.gridy = 2;
		networkPanel.add(new GeneralPanel(this), c);
		c.gridy = 3;
		networkPanel.add(new TeamColorsPanel(this), c);
		c.gridy = 4;
		JPanel southPanel = new JPanel(new GridLayout(1, 2));
		networkPanel.add(southPanel, c);

		c.gridx = 1;
		c.gridy = 0;
		add(networkPanel);

		saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			for (SaveListener l : listeners)
				l.configSaved(RVConfigure.this);
			config.write();
		});
		southPanel.add(saveButton);

		JButton startButton = new JButton("Start RoboViz");
		startButton.addActionListener(arg0 -> {
			RVConfigure.this.setVisible(false);
			Viewer.main(new String[] {});
		});
		southPanel.add(startButton);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		getContentPane().setBackground(networkPanel.getBackground());
		getRootPane().setDefaultButton(startButton);
		pack();
		Point desiredLocation = new Point(config.graphics.frameX, config.graphics.frameY);
		if (config.graphics.centerFrame)
			desiredLocation.setLocation(0, 0);
		SwingUtil.centerOnScreenAtLocation(this, desiredLocation);
	}

	public static void main(String[] args)
	{
		new RVConfigure().setVisible(true);
	}
}
