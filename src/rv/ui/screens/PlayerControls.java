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
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import rv.comm.rcssserver.LogPlayer;
import rv.ui.FramePanelBase;
import rv.util.observer.IObserver;

/**
 * Dialog containing the media player controls for log mode
 * 
 * @author dorer
 */
class PlayerControls extends FramePanelBase implements ChangeListener, IObserver<Boolean> {

    private static PlayerControls instance;

    public static PlayerControls getInstance(LogPlayer player) {
        if (instance != null) {
            instance.dispose();
        }
        return instance = new PlayerControls(player);
    }

    private Container       container;

    private JButton         fileOpenButton;

    private final LogPlayer player;

    private JButton         rewindButton;

    private JButton         stepBackwardButton;

    private JButton         stepForwardButton;

    private JButton         playPauseButton;

    private JSpinner        playbackSpeedSpinner;

    private JSlider         slider;

    private PlayerControls(LogPlayer player) {
        super("Logplayer");
        this.player = player;
        createControls();
        this.player.attach(this);
    }

    /**
     * Create the buttons and other GUI controls
     */
    private void createControls() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(330, 120);
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
        c.insets = new Insets(5, 5, 0, 0);

        fileOpenButton = createButton(c, "file_open", "Open Logfile...", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.openFile(frame);
            }
        });

        rewindButton = createButton(c, "rewind", "Rewind", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.rewind();
            }
        });

        stepBackwardButton = createButton(c, "previous_frame", "Step back", new ActionListener() {
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
                    playPauseButton.setIcon(getButtonIcon("play"));
                    playPauseButton.setToolTipText("Play");
                } else {
                    player.resume();
                    playPauseButton.setIcon(getButtonIcon("pause"));
                    playPauseButton.setToolTipText("Pause");
                }
            }
        });

        stepForwardButton = createButton(c, "next_frame", "Step forward", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.stepForward();
            }
        });

        c.gridx++;
        c.insets = new Insets(5, 40, 0, 0);
        playbackSpeedSpinner = new JSpinner(new SpinnerNumberModel(1, 0.1, 10, 0.1));
        playbackSpeedSpinner.setToolTipText("Playback speed factor");
        playbackSpeedSpinner.setPreferredSize(new Dimension(60, 30));
        playbackSpeedSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                player.setPlayBackSpeed((double) playbackSpeedSpinner.getValue());
            }
        });
        container.add(playbackSpeedSpinner, c);

        slider = new JSlider(0, player.getNumFrames(), player.getFrame());
        slider.addChangeListener(this);
        slider.setEnabled(false);
        slider.setMajorTickSpacing(1000);
        slider.setMinorTickSpacing(500);
        slider.setPaintTicks(true);
        slider.setToolTipText("Select Frame");

        c.gridwidth = c.gridx + 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(5, 5, 0, 0);
        container.add(slider, c);
    }

    private RoundButton createButton(GridBagConstraints c, String iconName, String tooltip,
            ActionListener listener) {
        RoundButton button = new RoundButton(getButtonIcon(iconName));
        button.setToolTipText(tooltip);
        button.addActionListener(listener);
        c.gridx++;
        container.add(button, c);
        return button;
    }

    private ImageIcon getButtonIcon(String iconName) {
        String iconPath = String.format("resources/images/%s.png", iconName);
        return new ImageIcon(iconPath);
    }

    /**
     * Called in case the player state has changed
     * 
     * @param playing
     *            true if the player is playing
     */
    public void update(Boolean playing) {
        boolean isValid = player.isValid();
        boolean atEnd = player.isAtEnd();
        fileOpenButton.setEnabled(!playing);
        rewindButton.setEnabled(isValid && (!playing || atEnd));
        playPauseButton.setEnabled(isValid && !atEnd);
        playbackSpeedSpinner.setEnabled(isValid && playing && !atEnd);
        stepBackwardButton.setEnabled(isValid && !playing);
        stepForwardButton.setEnabled(isValid && !playing && !atEnd);
        if (slider.getMaximum() < player.getNumFrames()) {
            slider.setMaximum(player.getNumFrames());
        }
        slider.setValue(player.getFrame());
        slider.setEnabled(isValid && !playing);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (slider.isEnabled()) {
            player.setCurrentFrame(slider.getValue());
        }
        playbackSpeedSpinner.setValue(player.getPlayBackSpeed());
    }

    public void dispose() {
        frame.dispose();
    }

    /**
     * Allows to have round swing buttons
     */
    class RoundButton extends JButton {

        protected Shape shape;

        protected Shape base;

        public RoundButton(Icon icon) {
            setModel(new DefaultButtonModel());
            init(null, icon);
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            setBackground(Color.BLACK);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setAlignmentY(Component.TOP_ALIGNMENT);
            initShape();
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
