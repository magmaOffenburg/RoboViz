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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import rv.Configuration;
import config.RVConfigure.SaveListener;

public class GraphicsPanel extends JPanel implements SaveListener {

    final Configuration.Graphics config;

    JCheckBox                    bloomCB;
    JCheckBox                    phongCB;
    JCheckBox                    shadowCB;
    JCheckBox                    softShadowCB;
    JCheckBox                    fsaaCB;
    JCheckBox                    stereoCB;
    JCheckBox                    vsyncCB;
    JCheckBox                    maximizedCB;
    JCheckBox                    centerCB;
    JCheckBox                    saveStateCB;
    JTextField                   samplesTF;
    JTextField                   shadowResTB;
    JSpinner                     fpsSpinner;
    JSpinner                     fovSpinner;
    JSpinner                     fxSpinner;
    JSpinner                     fySpinner;
    JSpinner                     fwSpinner;
    JSpinner                     fhSpinner;
    final JLabel                 shadowResLabel = new JLabel("Shadow Resolution: ",
                                                        SwingConstants.RIGHT);
    final JLabel                 samplesLabel   = new JLabel("Samples: ", SwingConstants.RIGHT);

    public GraphicsPanel(RVConfigure configProg) {
        config = configProg.config.graphics;
        initGUI();
        configProg.listeners.add(this);
    }

    void initGUI() {

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        addConstrained(initLightingPanel(), this, c, 0, 0);
        addConstrained(initAAPanel(), this, c, 0, 1);
        addConstrained(initGeneral(), this, c, 0, 2);
        addConstrained(initFramePanel(), this, c, 0, 3);
    }

    private void addConstrained(JComponent comp, JComponent container, GridBagConstraints c, int x,
            int y) {
        c.gridx = x;
        c.gridy = y;
        container.add(comp, c);
    }

    void addLabel(String name, JComponent component, GridBagConstraints c, int x, int y) {
        c.gridx = x;
        c.gridy = y;
        JLabel l = new JLabel(name, SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(60, 28));
        component.add(l, c);
    }

    JPanel initAAPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Anti-Aliasing"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        fsaaCB = new JCheckBox("Enabled", config.useFsaa);
        fsaaCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateAAEnabled();
            }
        });
        samplesTF = new IntegerTextField(config.fsaaSamples, 1, Integer.MAX_VALUE);
        updateAAEnabled();

        addConstrained(fsaaCB, panel, c, 0, 0);
        addConstrained(samplesLabel, panel, c, 1, 0);
        addConstrained(samplesTF, panel, c, 2, 0);

        return panel;
    }

    void updateAAEnabled() {
        samplesTF.setEnabled(fsaaCB.isSelected());
        samplesLabel.setEnabled(fsaaCB.isSelected());
    }

    JPanel initGeneral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("General Graphics"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        stereoCB = new JCheckBox("Stereo 3D", config.useStereo);
        vsyncCB = new JCheckBox("V-Sync", config.useVsync);
        fpsSpinner = createSpinner(config.targetFPS, 1, 60);
        fovSpinner = createSpinner(config.thirdPersonFOV, 1, 300);

        int y = 0;
        addConstrained(stereoCB, panel, c, 0, y);

        addConstrained(vsyncCB, panel, c, 1, y);

        addLabel("FPS: ", panel, c, 2, y);
        addConstrained(fpsSpinner, panel, c, 3, y);

        y++;
        JLabel label = new JLabel("Third Person FOV: ");
        label.setPreferredSize(new Dimension(95, label.getPreferredSize().height));
        addConstrained(label, panel, c, 2, y);
        addConstrained(fovSpinner, panel, c, 3, y);

        return panel;
    }

    JPanel initFramePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Frame"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        fxSpinner = createSpinner(config.frameX, -100, 10000);
        fySpinner = createSpinner(config.frameY, -100, 10000);
        fwSpinner = createSpinner(config.frameWidth, 1, 10000);
        fhSpinner = createSpinner(config.frameHeight, 1, 10000);
        maximizedCB = new JCheckBox("Maximized", config.isMaximized);
        centerCB = new JCheckBox("Center Position", config.centerFrame);
        centerCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFramePositionEnabled();
            }
        });
        updateFramePositionEnabled();
        saveStateCB = new JCheckBox("Save Frame State", config.saveFrameState);

        int y = 0;
        addLabel("X: ", panel, c, 0, y);
        addConstrained(fxSpinner, panel, c, 1, y);

        addLabel("Width: ", panel, c, 2, y);
        addConstrained(fwSpinner, panel, c, 3, y);

        y++;
        addLabel("Y: ", panel, c, 0, y);
        addConstrained(fySpinner, panel, c, 1, y);

        addLabel("Height: ", panel, c, 2, y);
        addConstrained(fhSpinner, panel, c, 3, y);

        y++;
        addConstrained(centerCB, panel, c, 1, y);
        addConstrained(maximizedCB, panel, c, 2, y);
        addConstrained(saveStateCB, panel, c, 3, y);

        return panel;
    }

    void updateFramePositionEnabled() {
        boolean enabled = !centerCB.isSelected();
        fxSpinner.setEnabled(enabled);
        fySpinner.setEnabled(enabled);
    }

    JSpinner createSpinner(int value, int min, int max) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, 1));
    }

    JPanel initLightingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lighting"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        bloomCB = new JCheckBox("Bloom", config.useBloom);
        phongCB = new JCheckBox("Phong", config.usePhong);
        shadowCB = new JCheckBox("Shadows", config.useShadows);
        shadowCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                updateShadowsEnabled();
            }
        });

        softShadowCB = new JCheckBox("Soft Shadows", config.useSoftShadows);
        shadowResTB = new IntegerTextField(config.shadowResolution, 1, Integer.MAX_VALUE);
        updateShadowsEnabled();

        addConstrained(phongCB, panel, c, 0, 0);
        addConstrained(bloomCB, panel, c, 1, 0);
        addConstrained(shadowCB, panel, c, 2, 0);

        addConstrained(softShadowCB, panel, c, 0, 2);
        addConstrained(shadowResLabel, panel, c, 1, 2);
        addConstrained(shadowResTB, panel, c, 2, 2);

        return panel;
    }

    void updateShadowsEnabled() {
        softShadowCB.setEnabled(shadowCB.isSelected());
        shadowResTB.setEnabled(shadowCB.isSelected());
        shadowResLabel.setEnabled(shadowCB.isSelected());
    }

    @Override
    public void configSaved(RVConfigure configProg) {
        config.useBloom = bloomCB.isSelected();
        config.usePhong = phongCB.isSelected();
        config.useShadows = shadowCB.isSelected();
        config.useSoftShadows = softShadowCB.isSelected();
        config.useFsaa = fsaaCB.isSelected();
        config.useStereo = stereoCB.isSelected();
        config.useVsync = vsyncCB.isSelected();

        try {
            config.fsaaSamples = Integer.parseInt(samplesTF.getText());
        } catch (Exception e) {
            samplesTF.setText(config.fsaaSamples + "");
        }

        try {
            config.shadowResolution = Integer.parseInt(shadowResTB.getText());
        } catch (Exception e) {
            shadowResTB.setText(config.shadowResolution + "");
        }

        config.targetFPS = (Integer) fpsSpinner.getValue();
        config.thirdPersonFOV = (Integer) fovSpinner.getValue();
        config.frameX = (Integer) fxSpinner.getValue();
        config.frameY = (Integer) fySpinner.getValue();
        config.frameWidth = (Integer) fwSpinner.getValue();
        config.frameHeight = (Integer) fhSpinner.getValue();
        config.centerFrame = centerCB.isSelected();
        config.isMaximized = maximizedCB.isSelected();
        config.saveFrameState = saveStateCB.isSelected();
    }
}
