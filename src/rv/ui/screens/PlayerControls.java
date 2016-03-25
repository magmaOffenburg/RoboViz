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

package rv.ui.screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import rv.comm.rcssserver.LogPlayer;
import rv.ui.FramePanelBase;
import rv.util.swing.SwingUtil;

/**
 * Dialog containing the media player controls for log mode
 * 
 * @author dorer
 */
class PlayerControls extends FramePanelBase implements LogPlayer.StateChangeListener {

    private static PlayerControls instance;

    public static PlayerControls getInstance(LogPlayer player) {
        if (instance != null) {
            instance.dispose();
        }
        return instance = new PlayerControls(player);
    }

    private final LogPlayer player;
    private Container       container;
    private RoundButton     rewindButton;
    private RoundButton     previousFrameButton;
    private RoundButton     playPauseButton;
    private RoundButton     nextFrameButton;
    private RoundButton     previousGoalButton;
    private RoundButton     nextGoalButton;
    private JSpinner        playbackSpeedSpinner;
    private JSlider         slider;
    private boolean         sliderUpdate;

    private PlayerControls(LogPlayer player) {
        super("Logplayer");
        this.player = player;
        createControls();
        this.player.addListener(this);
    }

    /**
     * Create the buttons and other GUI controls
     */
    private void createControls() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(380, 120);
        frame.setResizable(false);
        container = frame.getContentPane();

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { 1, 1, 1, 1, 1, 1 };
        layout.columnWeights = new double[] { 0, 0, 0, 0, 0, 0 };
        layout.rowHeights = new int[] { 1, 1 };
        layout.rowWeights = new double[] { 0.0, 0.0 };
        container.setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 0);

        createButton(c, "file_open", "Open logfile...", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.openFileDialog(frame);
            }
        });

        rewindButton = createButton(c, "rewind", "Rewind", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.rewind();
            }
        });

        previousFrameButton = createButton(c, "previous_frame", "Step back", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.stepBackward();
            }
        });

        playPauseButton = createButton(c, "pause", "Pause", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (player.isPlaying()) {
                    player.pause();
                    playPauseButton.setIcon("play");
                    playPauseButton.setToolTipText("Play");
                } else {
                    player.resume();
                    playPauseButton.setIcon("pause");
                    playPauseButton.setToolTipText("Pause");
                }
            }
        });

        nextFrameButton = createButton(c, "next_frame", "Step forward", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.stepForward();
            }
        });

        previousGoalButton = createButton(c, "previous_goal", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.stepBackwardGoal();
            }
        });

        nextGoalButton = createButton(c, "next_goal", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.stepForwardGoal();
            }
        });

        c.gridx++;
        c.insets = new Insets(0, 25, 0, 0);
        playbackSpeedSpinner = new JSpinner(new SpinnerNumberModel(1, -10, 10, 0.25));
        playbackSpeedSpinner.setToolTipText("Playback speed factor");
        playbackSpeedSpinner.setPreferredSize(new Dimension(60, 30));
        playbackSpeedSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                player.setPlayBackSpeed((double) playbackSpeedSpinner.getValue());
            }
        });
        container.add(playbackSpeedSpinner, c);

        slider = new JSlider(0, player.getNumFrames(), player.getDesiredFrame());
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (slider.isEnabled() && !sliderUpdate) {
                    int frame = slider.getValue();
                    player.setDesiredFrame(frame);

                    boolean atBeginning = frame == 0;
                    boolean atEnd = !(frame < player.getNumFrames());
                    updateButtons(player.isPlaying(), atBeginning, atEnd);
                }
            }
        });
        slider.setToolTipText("Select Frame");

        c.gridwidth = c.gridx + 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(15, 5, 0, 0);
        container.add(slider, c);
    }

    private RoundButton createButton(GridBagConstraints c, String iconName, String tooltip,
            ActionListener listener) {
        RoundButton button = new RoundButton(iconName);
        button.setToolTipText(tooltip);
        button.addActionListener(listener);
        c.gridx++;
        container.add(button, c);
        return button;
    }

    @Override
    public void playerStateChanged(boolean playing) {
        updateButtons(playing, player.isAtBeginning(), player.isAtEnd());
        updateSlider(playing);
    }

    @Override
    public void logfileChanged() {
    }

    private void updateButtons(Boolean playing, boolean atBeginning, boolean atEnd) {
        boolean isValid = player.isValid();
        rewindButton.setEnabled(isValid);
        previousFrameButton.setEnabled(isValid && !playing && !atBeginning);
        playPauseButton.setEnabled(isValid && (!atEnd || player.getPlayBackSpeed() <= 0)
                && (!atBeginning || player.getPlayBackSpeed() >= 0));
        playPauseButton.setIcon(playing ? "pause" : "play");
        previousGoalButton.setEnabled(isValid && player.hasPreviousGoal());
        previousGoalButton.setToolTipText(player.getPreviousGoalMessage());
        nextGoalButton.setEnabled(isValid && player.hasNextGoal());
        nextGoalButton.setToolTipText(player.getNextGoalMessage());
        nextFrameButton.setEnabled(isValid && !playing && !atEnd);
        playbackSpeedSpinner.setEnabled(isValid);
        playbackSpeedSpinner.setValue(player.getPlayBackSpeed());
    }

    private void updateSlider(Boolean playing) {
        sliderUpdate = true;
        if (slider.getMaximum() < player.getNumFrames()) {
            slider.setMaximum(player.getNumFrames());
        }

        if (!player.logfileHasDrawCmds()) {
            slider.setValue(player.getDesiredFrame());
            slider.setEnabled(player.isValid());
            sliderUpdate = false;
        } else {
            // Swing is not thread safe and running draw commands with the call to set the value of
            // slider can lock things up if we don't protect against this
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    slider.setValue(player.getDesiredFrame());
                    slider.setEnabled(player.isValid());
                    sliderUpdate = false;
                }
            });
        }
    }

    public void dispose() {
        frame.dispose();
    }

    class RoundButton extends JButton {

        private Shape  shape;
        private Shape  base;
        private String iconName;

        public RoundButton(String iconName) {
            setModel(new DefaultButtonModel());
            setIcon(iconName);
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            setBackground(Color.BLACK);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setAlignmentY(Component.TOP_ALIGNMENT);
            initShape();
        }

        public void setIcon(String iconName) {
            if (this.iconName != null && this.iconName.equals(iconName)) {
                return;
            }
            this.iconName = iconName;
            ImageIcon icon = getImageIcon(iconName);
            setIcon(icon);
            createRolloverIcon(icon);
        }

        private void createRolloverIcon(ImageIcon icon) {
            RescaleOp op = new RescaleOp(1.6f, -75, null);
            BufferedImage bufferIcon = SwingUtil.imageIconToBufferedImage(icon);
            op.filter(bufferIcon, bufferIcon);
            setRolloverIcon(new ImageIcon(bufferIcon));
        }

        private ImageIcon getImageIcon(String iconName) {
            String iconPath = String.format("resources/images/%s.png", iconName);
            return new ImageIcon(iconPath);
        }

        protected void initShape() {
            if (!getBounds().equals(base)) {
                Dimension s = getPreferredSize();
                base = getBounds();
                shape = new Ellipse2D.Float(0, 0, s.width - 1, s.height - 1);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            Icon icon = getIcon();
            Insets i = getInsets();
            int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
            return new Dimension(iw + i.right + i.left, iw + i.top + i.bottom);
        }

        @Override
        public boolean contains(int x, int y) {
            initShape();
            return shape.contains(x, y);
        }
    }
}
