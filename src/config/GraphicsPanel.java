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

    RVConfigure            configProg;
    Configuration.Graphics config;

    JCheckBox              bloomCB;
    JCheckBox              phongCB;
    JCheckBox              shadowCB;
    JCheckBox              softShadowCB;
    JCheckBox              fsaaCB;
    JCheckBox              stereoCB;
    JTextField             samplesTF;
    JTextField             shadowResTB;
    JSpinner               fpsSpinner;
    JSpinner               fwSpinner;
    JSpinner               fhSpinner;
    JLabel                 shadowResLabel = new JLabel("Shadow Resolution: ", SwingConstants.RIGHT);
    JLabel                 samplesLabel   = new JLabel("Samples: ", SwingConstants.RIGHT);

    public GraphicsPanel(RVConfigure configProg) {
        this.configProg = configProg;
        config = configProg.config.getGraphics();
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

        fsaaCB = new JCheckBox("Enabled", config.useFSAA());
        fsaaCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                samplesTF.setEnabled(fsaaCB.isSelected());
                samplesLabel.setEnabled(fsaaCB.isSelected());
            }
        });
        samplesTF = new JTextField("" + config.getFSAASamples());

        addConstrained(fsaaCB, panel, c, 0, 0);
        addConstrained(samplesLabel, panel, c, 1, 0);
        addConstrained(samplesTF, panel, c, 2, 0);

        return panel;
    }

    JPanel initGeneral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("General Graphics"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        stereoCB = new JCheckBox("Stereo 3D", config.useStereo());
        fpsSpinner = new JSpinner(new SpinnerNumberModel(config.getTargetFPS(), 1, 60, 1));
        fwSpinner = new JSpinner(new SpinnerNumberModel(config.getFrameWidth(), 1, 10000, 1));
        fhSpinner = new JSpinner(new SpinnerNumberModel(config.getFrameHeight(), 1, 10000, 1));

        addConstrained(stereoCB, panel, c, 0, 0);

        addLabel("FPS: ", panel, c, 1, 0);
        addConstrained(fpsSpinner, panel, c, 2, 0);

        addLabel("Frame W: ", panel, c, 0, 1);
        addConstrained(fwSpinner, panel, c, 1, 1);

        addLabel("Frame H: ", panel, c, 0, 2);
        addConstrained(fhSpinner, panel, c, 1, 2);

        return panel;
    }

    JPanel initLightingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lighting"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        bloomCB = new JCheckBox("Bloom", config.useBloom());
        phongCB = new JCheckBox("Phong", config.usePhong());
        shadowCB = new JCheckBox("Shadows", config.useShadows());
        shadowCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                softShadowCB.setEnabled(shadowCB.isSelected());
                shadowResTB.setEnabled(shadowCB.isSelected());
                shadowResLabel.setEnabled(shadowCB.isSelected());
            }
        });

        softShadowCB = new JCheckBox("Soft Shadows", config.useSoftShadows());
        shadowResTB = new JTextField("" + config.getShadowResolution());

        addConstrained(phongCB, panel, c, 0, 0);
        addConstrained(bloomCB, panel, c, 1, 0);
        addConstrained(shadowCB, panel, c, 2, 0);

        addConstrained(softShadowCB, panel, c, 0, 2);
        addConstrained(shadowResLabel, panel, c, 1, 2);
        addConstrained(shadowResTB, panel, c, 2, 2);

        return panel;
    }

    @Override
    public void configSaved(RVConfigure configProg) {
        config.setUseBloom(bloomCB.isSelected());
        config.setUsePhong(phongCB.isSelected());
        config.setUseShadows(shadowCB.isSelected());
        config.setSoftShadow(softShadowCB.isSelected());
        config.setFSAA(fsaaCB.isSelected());
        config.setUseStereo(stereoCB.isSelected());

        try {
            int samples = Integer.parseInt(samplesTF.getText());
            config.setFSAASamples(samples);
        } catch (Exception e) {
            samplesTF.setText(config.getFSAASamples() + "");
        }

        try {
            int shadowRes = Integer.parseInt(shadowResTB.getText());
            config.setShadowResolution(shadowRes);
        } catch (Exception e) {
            shadowResTB.setText(config.getShadowResolution() + "");
        }

        config.setTargetFPS(((Integer) fpsSpinner.getValue()).intValue());
        config.setFrameWidth(((Integer) fwSpinner.getValue()).intValue());
        config.setFrameHeight(((Integer) fhSpinner.getValue()).intValue());
    }
}
