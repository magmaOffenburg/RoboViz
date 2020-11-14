package org.magmaoffenburg.roboviz.gui.windows.config

import org.magmaoffenburg.roboviz.configuration.Config.Graphics
import org.magmaoffenburg.roboviz.rendering.Renderer
import java.awt.Dimension
import javax.swing.*

class GraphicsPanel: JPanel() {

    // lighting
    private val lightingLabel = JLabel("Lighting")
    private val lightingSeparator = JSeparator().apply {
        maximumSize = Dimension(0, lightingLabel.preferredSize.height)
    }
    private val bloomCb = JCheckBox("Bloom", Graphics.useBloom)
    private val phongCb = JCheckBox("Phong", Graphics.usePhong)
    private val shadowsCb = JCheckBox("Shadows", Graphics.useShadows)
    private val softShadowsCb = JCheckBox("Soft Shadows", Graphics.useSoftShadows).apply {
        isEnabled = Graphics.useShadows
    }
    private val shadowResLabel = JLabel("Shadow Resolution:", SwingConstants.RIGHT)
    private val shadowResSpinner = JSpinner(SpinnerNumberModel(Graphics.shadowResolution, 1, Int.MAX_VALUE, 1)).apply {
        isEnabled = Graphics.useShadows && Graphics.useSoftShadows
    }

    // fsaa
    private val fsaaLabel = JLabel("Anti-Aliasing")
    private val fsaaSeparator = JSeparator().apply {
        maximumSize = Dimension(0, fsaaLabel.preferredSize.height)
    }
    private val fsaaCb = JCheckBox("Enabled", Graphics.useFsaa)
    private val samplesLabel = JLabel("Samples:", SwingConstants.RIGHT)
    private val samplesSpinner = JSpinner(SpinnerNumberModel(Graphics.fsaaSamples, 1, Int.MAX_VALUE, 1)).apply {
        isEnabled = Graphics.useFsaa
    }

    // general
    private val generalLabel = JLabel("General Graphics")
    private val generalSeparator = JSeparator().apply {
        maximumSize = Dimension(0, generalLabel.preferredSize.height)
    }
    private val stereoCb = JCheckBox("Stereo 3D", Graphics.useStereo)
    private val vsyncCb = JCheckBox("V-Sync", Graphics.useVsync)
    private val fpsLabel = JLabel("FPS:")
    private val fpsSpinner = JSpinner(SpinnerNumberModel(Graphics.targetFPS, 1, Int.MAX_VALUE, 1))
    private val fpFovLabel = JLabel("First Person FOV:")
    private val fpFovSpinner = JSpinner(SpinnerNumberModel(Graphics.firstPersonFOV, 1, Int.MAX_VALUE, 1))
    private val tpFovLabel = JLabel("Third Person FOV:")
    private val tpFovSpinner = JSpinner(SpinnerNumberModel(Graphics.thirdPersonFOV, 1, Int.MAX_VALUE, 1))

    init {
        initializeLayout()
        initializeActions()
    }

    /**
     * initialize the panels layout
     */
    private fun initializeLayout() {
        val layout = GroupLayout(this).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        this.layout = layout

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)

                // lighting
                .addGroup(layout.createSequentialGroup()
                        .addComponent(lightingLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lightingSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(bloomCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(phongCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(shadowsCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(softShadowsCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(shadowResLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(shadowResSpinner, 0, 90, 90)
                )

                // fsaa
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fsaaLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fsaaSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fsaaCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(samplesLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(samplesSpinner, 0, 90, 90)
                )

                // general
                .addGroup(layout.createSequentialGroup()
                        .addComponent(generalLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(generalSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(stereoCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(vsyncCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fpsLabel, 0, 120, 120)
                        .addComponent(fpsSpinner, 0, 90, 90)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fpFovLabel, 0, 120, 120)
                        .addComponent(fpFovSpinner, 0, 90, 90)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(tpFovLabel, 0, 120, 120)
                        .addComponent(tpFovSpinner, 0, 90, 90)
                )
        )

        layout.setVerticalGroup(layout.createSequentialGroup()
                // lighting
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lightingLabel)
                        .addComponent(lightingSeparator)
                )
                .addComponent(bloomCb)
                .addComponent(phongCb)
                .addComponent(shadowsCb)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(softShadowsCb)
                        .addComponent(shadowResLabel)
                        .addComponent(shadowResSpinner)
                )

                // fsaa
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fsaaLabel)
                        .addComponent(fsaaSeparator)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fsaaCb)
                        .addComponent(samplesLabel)
                        .addComponent(samplesSpinner)
                )

                // general
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(generalLabel)
                        .addComponent(generalSeparator)
                )
                .addComponent(stereoCb)
                .addComponent(vsyncCb)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fpsLabel)
                        .addComponent(fpsSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fpFovLabel)
                        .addComponent(fpFovSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(tpFovLabel)
                        .addComponent(tpFovSpinner)
                )
        )
    }

    /**
     * initialize the actions for all gui elements
     * used by this panel
     */
    private fun initializeActions() {
        // lighting
        bloomCb.addActionListener {
            Graphics.useBloom = bloomCb.isSelected
            Renderer.renderSettingsChanged = true
        }
        phongCb.addActionListener {
            Graphics.usePhong = phongCb.isSelected
            Renderer.renderSettingsChanged = true
        }
        shadowsCb.addActionListener {
            Graphics.useShadows = shadowsCb.isSelected
            softShadowsCb.isEnabled = Graphics.useShadows
            shadowResSpinner.isEnabled = Graphics.useShadows
            Renderer.renderSettingsChanged = true
        }
        softShadowsCb.addActionListener {
            Graphics.useSoftShadows = softShadowsCb.isSelected
            shadowResSpinner.isEnabled = Graphics.useSoftShadows

            Renderer.renderSettingsChanged = true
        }
        shadowResSpinner.addChangeListener {
            Graphics.shadowResolution = shadowResSpinner.value as Int
            Renderer.renderSettingsChanged = true
        }

        // fsaa
        fsaaCb.addActionListener {
            Graphics.useFsaa = fsaaCb.isSelected
            samplesSpinner.isEnabled = Graphics.useFsaa
            Renderer.renderSettingsChanged = true
        }
        samplesSpinner.addChangeListener {
            Graphics.fsaaSamples = samplesSpinner.value as Int
        }

        // general
        stereoCb.addActionListener {
            Graphics.useStereo = stereoCb.isSelected
        }
        vsyncCb.addActionListener {
            Graphics.useVsync = vsyncCb.isSelected
        }
        fpsSpinner.addChangeListener {
            Graphics.targetFPS = fpsSpinner.value as Int
        }
        fpFovSpinner.addChangeListener {
            Graphics.firstPersonFOV = fpFovSpinner.value as Int
        }
        tpFovSpinner.addChangeListener {
            Graphics.thirdPersonFOV = tpFovSpinner.value as Int
        }
    }

}