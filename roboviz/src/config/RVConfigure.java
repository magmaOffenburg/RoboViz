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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import rv.*;

public class RVConfigure extends JFrame {

    interface SaveListener {
        void configSaved(RVConfigure configProg);
    }

    Configuration           config;
    final JButton           saveButton = new JButton("Save");
    ArrayList<SaveListener> listeners  = new ArrayList<SaveListener>();

    public RVConfigure() {

        try {
            config = Configuration.loadFromFile();
        } catch (Exception e) {
            e.printStackTrace();
            config = new Configuration();
        }

        setTitle("RoboViz Configuration");
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        c.gridx = 0;
        c.gridy = 0;
        add(new NetworkPanel(this), c);

        c.gridx = 0;
        c.gridy = 1;
        add(new GraphicsPanel(this), c);

        c.gridx = 0;
        c.gridy = 2;
        JPanel southPanel = new JPanel(new GridLayout(1, 2));
        add(southPanel, c);

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (SaveListener l : listeners)
                    l.configSaved(RVConfigure.this);
                config.write();
            }
        });
        southPanel.add(saveButton);

        JButton startButton = new JButton("Start RoboViz");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                RVConfigure.this.setVisible(false);
                Viewer.main(new String[] {});
            }
        });
        southPanel.add(startButton);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        new RVConfigure().setVisible(true);
    }
}
