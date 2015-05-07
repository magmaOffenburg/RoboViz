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

    private Container container;

    private JButton   fileOpenButton;

    private LogPlayer player;

    private JButton   rewindButton;

    private JButton   slowerButton;

    private JButton   stepBackwardButton;

    private JButton   playButton;

    private JButton   stepForwardButton;

    private JButton   pauseButton;

    private JButton   fasterButton;

    private JSlider   slider;

    public PlayerControls(LogPlayer playerRef) {
        super("Logplayer");
        this.player = playerRef;
        createControls();
        player.attach(this);
    }

    public void setVisible(boolean b) {
        frame.setVisible(b);
    }

    /**
     * Create the buttons and other GUI controls
     */
    private void createControls() {
        int buttonId = 0;

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 110);
        frame.setResizable(false);
        container = frame.getContentPane();
        container.setLayout(null);

        fileOpenButton = createButton(buttonId++, "file_open", "Open Logfile...",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!player.isPlaying())
                            player.openFile();
                    }
                });

        rewindButton = createButton(buttonId++, "rewind", "Rewind", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.rewind();
            }
        });

        slowerButton = createButton(buttonId++, "fast_backward", "Slower", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.changePlayBackSpeed(false);
            }
        });

        pauseButton = createButton(buttonId++, "pause", "Pause", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (player.isPlaying())
                    player.pause();
            }
        });

        fasterButton = createButton(buttonId++, "fast_forward", "Faster", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.changePlayBackSpeed(true);
            }
        });

        stepBackwardButton = createButton(buttonId++, "previous_frame", "Step back",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!player.isPlaying())
                            player.stepBackward();
                    }
                });

        playButton = createButton(buttonId++, "play", "Play", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.resume();
            }
        });

        stepForwardButton = createButton(buttonId++, "next_frame", "Step forward",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!player.isPlaying())
                            player.stepForward();
                    }
                });

        slider = new JSlider(0, player.getNumFrames(), player.getFrame());
        slider.addChangeListener(this);
        slider.setBounds(10, 50, 380, 20);
        slider.setEnabled(false);
        slider.setMajorTickSpacing(1000);
        slider.setMinorTickSpacing(500);
        slider.setPaintTicks(true);
        slider.setToolTipText("Select Frame");

        container.add(slider);
    }

    private RoundButton createButton(int id, String iconName, String tooltip,
            ActionListener listener) {
        final int KNOB_SIZE = 32;

        String iconPath = String.format("resources/images/%s.png", iconName);
        RoundButton button = new RoundButton(new ImageIcon(iconPath));
        button.setBounds(10 + id * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        button.setToolTipText(tooltip);
        button.addActionListener(listener);
        container.add(button);
        return button;
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
        slowerButton.setEnabled(isValid && playing && !atEnd);
        pauseButton.setEnabled(isValid && playing && !atEnd);
        fasterButton.setEnabled(isValid && playing && !atEnd);
        playButton.setEnabled(isValid && !playing && !atEnd);
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
