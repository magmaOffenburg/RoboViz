package org.magmaoffenburg.roboviz.gui.windows.config

import org.magmaoffenburg.roboviz.configuration.Config.Graphics
import java.awt.Dimension
import javax.swing.*

class WindowPanel: JPanel() {

    private val frameLabel = JLabel("Frame")
    private val frameSeparator = JSeparator().apply {
        maximumSize = Dimension(0, frameLabel.preferredSize.height)
    }
    private val fxLabel = JLabel("X:")
    private val fxSpinner = JSpinner(SpinnerNumberModel(Graphics.frameX, Int.MIN_VALUE, Int.MAX_VALUE, 1))
    private val fyLabel = JLabel("Y:")
    private val fySpinner = JSpinner(SpinnerNumberModel(Graphics.frameY, Int.MIN_VALUE, Int.MAX_VALUE, 1))
    private val fwLabel = JLabel("Width:")
    private val fwSpinner = JSpinner(SpinnerNumberModel(Graphics.frameWidth, 1, 10000, 1))
    private val fhLabel = JLabel("Height:")
    private val fhSpinner = JSpinner(SpinnerNumberModel(Graphics.frameHeight, 1, 10000, 1))
    private val centerCb = JCheckBox("Center Position", Graphics.centerFrame)
    private val saveStateCb = JCheckBox("Save Frame State", Graphics.saveFrameState)
    private val maximizedCb = JCheckBox("Maximized", Graphics.isMaximized)

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

    /**
     * initialize the actions for all gui elements
     * used by this panel
     */
    private fun initializeActions() {
        fxSpinner.addChangeListener {
            Graphics.frameX = fxSpinner.value as Int
        }
        fySpinner.addChangeListener {
            Graphics.frameY = fySpinner.value as Int
        }
        fwSpinner.addChangeListener {
            Graphics.frameWidth = fwSpinner.value as Int
        }
        fhSpinner.addChangeListener {
            Graphics.frameHeight = fhSpinner.value as Int
        }
        centerCb.addActionListener {
            Graphics.centerFrame = centerCb.isSelected
        }
        saveStateCb.addActionListener {
            Graphics.saveFrameState = saveStateCb.isSelected
        }
        maximizedCb.addActionListener {
            Graphics.isMaximized = maximizedCb.isSelected
        }
    }
}