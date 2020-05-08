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
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import config.RVConfigure.SaveListener;
import rv.Configuration;
import rv.util.swing.SwingUtil;

public class GraphicsPanel extends JPanel implements SaveListener
{
	final Configuration.Graphics config;

	JCheckBox bloomCB;
	JCheckBox phongCB;
	JCheckBox shadowCB;
	JCheckBox softShadowCB;
	JCheckBox fsaaCB;
	JCheckBox stereoCB;
	JCheckBox vsyncCB;
	JCheckBox maximizedCB;
	JCheckBox centerCB;
	JCheckBox saveStateCB;
	IntegerTextField samplesTF;
	IntegerTextField shadowResTB;
	JSpinner fpsSpinner;
	JSpinner fpFovSpinner;
	JSpinner tpFovSpinner;
	JSpinner fxSpinner;
	JSpinner fySpinner;
	JSpinner fwSpinner;
	JSpinner fhSpinner;
	final JLabel shadowResLabel = new JLabel("Shadow Resolution: ", SwingConstants.RIGHT);
	final JLabel samplesLabel = new JLabel("Samples: ", SwingConstants.RIGHT);

	private Consumer<Void> onChange;

	public GraphicsPanel(RVConfigure configProg)
	{
		config = configProg.config.graphics;
		initGUI();
		configProg.listeners.add(this);
		onChange = configProg.updateSaveButton;
	}

	void initGUI()
	{
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		addConstrained(initLightingPanel(), this, c, 0, 0);
		addConstrained(initAAPanel(), this, c, 0, 1);
		addConstrained(initGeneral(), this, c, 0, 2);
		addConstrained(initFramePanel(), this, c, 0, 3);
	}

	private void addConstrained(JComponent comp, JComponent container, GridBagConstraints c, int x, int y)
	{
		c.gridx = x;
		c.gridy = y;
		container.add(comp, c);
	}

	void addLabel(String name, JComponent component, GridBagConstraints c, int x, int y)
	{
		c.gridx = x;
		c.gridy = y;
		JLabel l = new JLabel(name, SwingConstants.RIGHT);
		l.setPreferredSize(new Dimension(60, 28));
		component.add(l, c);
	}

	JPanel initAAPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Anti-Aliasing"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 10;

		fsaaCB = new JCheckBox("Enabled", config.useFsaa);
		fsaaCB.addChangeListener(e -> {
			updateAAEnabled();
			config.useFsaa = fsaaCB.isSelected();
			onChange.accept(null);
		});
		samplesTF = new IntegerTextField(config.fsaaSamples, 1, Integer.MAX_VALUE);
		samplesTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateFsaaSamplesConfig(false);
				onChange.accept(null);
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateFsaaSamplesConfig(false);
				onChange.accept(null);
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateFsaaSamplesConfig(false);
				onChange.accept(null);
			}
		});
		updateAAEnabled();

		addConstrained(fsaaCB, panel, c, 0, 0);
		addConstrained(samplesLabel, panel, c, 1, 0);
		addConstrained(samplesTF, panel, c, 2, 0);

		return panel;
	}

	void updateAAEnabled()
	{
		samplesTF.setEnabled(fsaaCB.isSelected());
		samplesLabel.setEnabled(fsaaCB.isSelected());
	}

	JPanel initGeneral()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("General Graphics"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 10;

		stereoCB = new JCheckBox("Stereo 3D", config.useStereo);
		stereoCB.addChangeListener(e -> {
			config.useStereo = stereoCB.isSelected();
			onChange.accept(null);
		});
		vsyncCB = new JCheckBox("V-Sync", config.useVsync);
		vsyncCB.addChangeListener(e -> {
			config.useVsync = vsyncCB.isSelected();
			onChange.accept(null);
		});
		fpsSpinner = createSpinner(config.targetFPS, 1, 60);
		fpsSpinner.addChangeListener(e -> {
			config.targetFPS = (Integer) fpsSpinner.getValue();
			onChange.accept(null);
		});
		fpFovSpinner = createSpinner(config.firstPersonFOV, 1, 300);
		fpFovSpinner.addChangeListener(e -> {
			config.firstPersonFOV = (Integer) fpFovSpinner.getValue();
			onChange.accept(null);
		});
		tpFovSpinner = createSpinner(config.thirdPersonFOV, 1, 300);
		tpFovSpinner.addChangeListener(e -> {
			config.thirdPersonFOV = (Integer) tpFovSpinner.getValue();
			onChange.accept(null);
		});

		int y = 0;
		addConstrained(stereoCB, panel, c, 0, y);

		addConstrained(vsyncCB, panel, c, 1, y);

		addLabel("FPS: ", panel, c, 2, y);
		addConstrained(fpsSpinner, panel, c, 3, y);

		y++;
		JLabel label = new JLabel("First Person FOV: ");
		SwingUtil.setPreferredWidth(label, 95);
		addConstrained(label, panel, c, 2, y);
		addConstrained(fpFovSpinner, panel, c, 3, y);

		y++;
		label = new JLabel("Third Person FOV: ");
		SwingUtil.setPreferredWidth(label, 95);
		addConstrained(label, panel, c, 2, y);
		addConstrained(tpFovSpinner, panel, c, 3, y);

		return panel;
	}

	JPanel initFramePanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Frame"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 10;

		fxSpinner = createSpinner(config.frameX, -100, 10000);
		fxSpinner.addChangeListener(e -> {
			config.frameX = (Integer) fxSpinner.getValue();
			onChange.accept(null);
		});
		fySpinner = createSpinner(config.frameY, -100, 10000);
		fySpinner.addChangeListener(e -> {
			config.frameY = (Integer) fySpinner.getValue();
			onChange.accept(null);
		});
		fwSpinner = createSpinner(config.frameWidth, 1, 10000);
		fwSpinner.addChangeListener(e -> {
			config.frameWidth = (Integer) fwSpinner.getValue();
			onChange.accept(null);
		});
		fhSpinner = createSpinner(config.frameHeight, 1, 10000);
		fhSpinner.addChangeListener(e -> {
			config.frameHeight = (Integer) fhSpinner.getValue();
			onChange.accept(null);
		});
		maximizedCB = new JCheckBox("Maximized", config.isMaximized);
		maximizedCB.addChangeListener(e -> {
			config.isMaximized = maximizedCB.isSelected();
			onChange.accept(null);
		});
		centerCB = new JCheckBox("Center Position", config.centerFrame);
		centerCB.addChangeListener(e -> {
			updateFramePositionEnabled();
			config.centerFrame = centerCB.isSelected();
			onChange.accept(null);
		});
		updateFramePositionEnabled();
		saveStateCB = new JCheckBox("Save Frame State", config.saveFrameState);
		saveStateCB.addChangeListener(e -> {
			config.saveFrameState = saveStateCB.isSelected();
			onChange.accept(null);
		});

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

	void updateFramePositionEnabled()
	{
		boolean enabled = !centerCB.isSelected();
		fxSpinner.setEnabled(enabled);
		fySpinner.setEnabled(enabled);
	}

	JSpinner createSpinner(int value, int min, int max)
	{
		return new JSpinner(new SpinnerNumberModel(value, min, max, 1));
	}

	JPanel initLightingPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Lighting"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 10;

		bloomCB = new JCheckBox("Bloom", config.useBloom);
		bloomCB.addChangeListener(e -> {
			config.useBloom = bloomCB.isSelected();
			onChange.accept(null);
		});
		phongCB = new JCheckBox("Phong", config.usePhong);
		phongCB.addChangeListener(e -> {
			config.usePhong = phongCB.isSelected();
			onChange.accept(null);
		});
		shadowCB = new JCheckBox("Shadows", config.useShadows);
		shadowCB.addChangeListener(e -> {
			updateShadowsEnabled();
			config.useShadows = shadowCB.isSelected();
			onChange.accept(null);
		});

		softShadowCB = new JCheckBox("Soft Shadows", config.useSoftShadows);
		softShadowCB.addChangeListener(e -> {
			config.useSoftShadows = softShadowCB.isSelected();
			onChange.accept(null);
		});
		shadowResTB = new IntegerTextField(config.shadowResolution, 1, Integer.MAX_VALUE);
		shadowResTB.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateShadowResolutionConfig(false);
				onChange.accept(null);
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateShadowResolutionConfig(false);
				onChange.accept(null);
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateShadowResolutionConfig(false);
				onChange.accept(null);
			}
		});
		updateShadowsEnabled();

		addConstrained(phongCB, panel, c, 0, 0);
		addConstrained(bloomCB, panel, c, 1, 0);
		addConstrained(shadowCB, panel, c, 2, 0);

		addConstrained(softShadowCB, panel, c, 0, 2);
		addConstrained(shadowResLabel, panel, c, 1, 2);
		addConstrained(shadowResTB, panel, c, 2, 2);

		return panel;
	}

	void updateShadowsEnabled()
	{
		softShadowCB.setEnabled(shadowCB.isSelected());
		shadowResTB.setEnabled(shadowCB.isSelected());
		shadowResLabel.setEnabled(shadowCB.isSelected());
	}

	private void updateFsaaSamplesConfig(boolean resetOnError)
	{
		try {
			config.fsaaSamples = samplesTF.getInt();
		} catch (Exception e) {
			if (resetOnError) {
				samplesTF.setText(config.fsaaSamples + "");
			}
		}
	}

	private void updateShadowResolutionConfig(boolean resetOnError)
	{
		try {
			config.fsaaSamples = samplesTF.getInt();
		} catch (Exception e) {
			if (resetOnError) {
				samplesTF.setText(config.fsaaSamples + "");
			}
		}
	}

	@Override
	public void configSaved(RVConfigure configProg)
	{
		config.useBloom = bloomCB.isSelected();
		config.usePhong = phongCB.isSelected();
		config.useShadows = shadowCB.isSelected();
		config.useSoftShadows = softShadowCB.isSelected();
		config.useFsaa = fsaaCB.isSelected();
		config.useStereo = stereoCB.isSelected();
		config.useVsync = vsyncCB.isSelected();

		updateFsaaSamplesConfig(true);
		updateShadowResolutionConfig(true);

		config.targetFPS = (Integer) fpsSpinner.getValue();
		config.firstPersonFOV = (Integer) fpFovSpinner.getValue();
		config.thirdPersonFOV = (Integer) tpFovSpinner.getValue();
		config.frameX = (Integer) fxSpinner.getValue();
		config.frameY = (Integer) fySpinner.getValue();
		config.frameWidth = (Integer) fwSpinner.getValue();
		config.frameHeight = (Integer) fhSpinner.getValue();
		config.centerFrame = centerCB.isSelected();
		config.isMaximized = maximizedCB.isSelected();
		config.saveFrameState = saveStateCB.isSelected();
	}
}
