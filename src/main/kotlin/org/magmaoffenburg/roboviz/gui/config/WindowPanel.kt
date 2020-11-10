package org.magmaoffenburg.roboviz.gui.config

import org.magmaoffenburg.roboviz.configuration.Config
import java.awt.Dimension
import javax.swing.*

class WindowPanel: JPanel() {

    init {
        initializePanel()
    }

    private fun initializePanel() {
        val layout = GroupLayout(this).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        this.layout = layout

        val frameLabel = JLabel("Frame")
        val frameSeparator = JSeparator().apply {
            maximumSize = Dimension(0, frameLabel.preferredSize.height)
        }
        val fxLabel = JLabel("X:")
        val fxSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.frameX, -100, 10000, 1))
        val fyLabel = JLabel("Y:")
        val fySpinner = JSpinner(SpinnerNumberModel(Config.Graphics.frameY, -100, 10000, 1))
        val fwLabel = JLabel("Width:")
        val fwSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.frameWidth, 1, 10000, 1))
        val fhLabel = JLabel("Height:")
        val fhSpinner = JSpinner(SpinnerNumberModel(Config.Graphics.frameHeight, 1, 10000, 1))
        val centerCb = JCheckBox("Center Position", Config.Graphics.centerFrame)
        val saveStateCb = JCheckBox("Save Frame State", Config.Graphics.saveFrameState)
        val maximizedCb = JCheckBox("Maximized", Config.Graphics.isMaximized)

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(frameLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(frameSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fxLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fxSpinner, 0, 90, 90)
                        .addComponent(fwLabel, 0, 45, 45)
                        .addComponent(fwSpinner, 0, 90, 90)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fyLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fySpinner, 0, 90, 90)
                        .addComponent(fhLabel, 0, 45, 45)
                        .addComponent(fhSpinner, 0, 90, 90)
                )
                .addComponent(centerCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(saveStateCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(maximizedCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
        )

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(frameLabel)
                        .addComponent(frameSeparator)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fxLabel)
                        .addComponent(fxSpinner)
                        .addComponent(fwLabel)
                        .addComponent(fwSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fyLabel)
                        .addComponent(fySpinner)
                        .addComponent(fhLabel)
                        .addComponent(fhSpinner)
                )
                .addComponent(centerCb)
                .addComponent(saveStateCb)
                .addComponent(maximizedCb)
        )
    }
}