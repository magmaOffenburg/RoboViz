package org.magmaoffenburg.roboviz.gui.config

import org.magmaoffenburg.roboviz.configuration.Config
import java.awt.Dimension
import javax.swing.*

class GraphicsPanel: JPanel() {

    init {
        initializePanel()
    }

    private fun initializePanel() {
        val layout = GroupLayout(this).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        this.layout = layout

        val space = Box.createHorizontalStrut(10)

        // lighting
        val lightingLabel = JLabel("Lighting")
        val lightingSeparator = JSeparator().apply {
            maximumSize = Dimension(0, lightingLabel.preferredSize.height)
        }
        val bloomCb = JCheckBox("Bloom", Config.Graphics.useBloom)
        val phongCb = JCheckBox("Phong", Config.Graphics.usePhong)
        val shadowsCb = JCheckBox("Shadows", Config.Graphics.useShadows)
        val softShadowsCb = JCheckBox("Soft Shadows", Config.Graphics.useSoftShadows)
        val shadowResLabel = JLabel("Shadow Resolution:", SwingConstants.RIGHT)
        val shadowResSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.shadowResolution, 1, Int.MAX_VALUE, 1))

        // fsaa
        val fsaaLabel = JLabel("Anti-Aliasing")
        val fsaaSeparator = JSeparator().apply {
            maximumSize = Dimension(0, fsaaLabel.preferredSize.height)
        }
        val fsaaCB = JCheckBox("Enabled", Config.Graphics.useFsaa)
        val samplesLabel = JLabel("Samples:", SwingConstants.RIGHT)
        val samplesSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.fsaaSamples, 1, Int.MAX_VALUE, 1))

        // general
        val generalLabel = JLabel("General Graphics")
        val generalSeparator = JSeparator().apply {
            maximumSize = Dimension(0, generalLabel.preferredSize.height)
        }
        val stereoCb = JCheckBox("Stereo 3D", Config.Graphics.useStereo)
        val vsyncCb = JCheckBox("V-Sync", Config.Graphics.useVsync)
        val fpsLabel = JLabel("FPS:")
        val fpsSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.targetFPS, 1, Int.MAX_VALUE, 1))
        val fpFovLabel = JLabel("First Person FOV:")
        val fpFovSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.firstPersonFOV, 1, Int.MAX_VALUE, 1))
        val tpFovLabel = JLabel("Third Person FOV:")
        val tpFovSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.thirdPersonFOV, 1, Int.MAX_VALUE, 1))

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
                .addComponent(space, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())

                // fsaa
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fsaaLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fsaaSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fsaaCB, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(samplesLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(samplesSpinner, 0, 90, 90)
                )
                .addComponent(space, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())

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
                .addComponent(space)

                // fsaa
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fsaaLabel)
                        .addComponent(fsaaSeparator)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fsaaCB)
                        .addComponent(samplesLabel)
                        .addComponent(samplesSpinner)
                )
                .addComponent(space)

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

}