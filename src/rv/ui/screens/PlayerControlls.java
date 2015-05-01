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
import java.awt.Dialog;
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
import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import rv.comm.rcssserver.LogPlayer;
import rv.util.observer.IObserver;

/**
 * Dialog containing the media player controls for log mode
 * 
 * @author dorer
 */
class PlayerControlls extends JDialog implements ChangeListener, IObserver<Boolean> {

    private static final long serialVersionUID = -3858876806399693025L;

    private JButton           fileOpenButton;

    private LogPlayer         player;

    private JButton           rewindButton;

    private JButton           slowerButton;

    private JButton           stepBackwardButton;

    private JButton           playButton;

    private JButton           stepForwardButton;

    private JButton           pauseButton;

    private JButton           fasterButton;

    private JSlider           slider;

    public PlayerControlls(LogPlayer playerRef) {
        super((Dialog) null, "Logplayer");
        this.player = playerRef;
        createControlls();
        player.attach(this);
    }

    /**
     * Create the buttons and other GUI controlls
     */
    private void createControlls() {
        final int KNOB_SIZE = 32;
        int xOrder = 0;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 110);
        Container myContainer = getContentPane();
        myContainer.setLayout(null);

        ImageIcon theIcon = new ImageIcon("resources/images/file_open.png");
        fileOpenButton = new RoundButton(theIcon);
        fileOpenButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        fileOpenButton.setToolTipText("Open Logfile...");
        fileOpenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying()) {
                    player.openFile();
                }
            }
        });
        myContainer.add(fileOpenButton);

        theIcon = new ImageIcon("resources/images/rewind.png");
        rewindButton = new RoundButton(theIcon);
        rewindButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        rewindButton.setToolTipText("Rewind");
        rewindButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.rewind();
            }
        });
        myContainer.add(rewindButton);

        theIcon = new ImageIcon("resources/images/fast_backward.png");
        slowerButton = new RoundButton(theIcon);
        slowerButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        slowerButton.setToolTipText("Slower");
        slowerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.changePlayBackSpeed(false);
            }
        });
        myContainer.add(slowerButton);

        theIcon = new ImageIcon("resources/images/pause.png");
        pauseButton = new RoundButton(theIcon);
        pauseButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        pauseButton.setToolTipText("Pause");
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (player.isPlaying())
                    player.pause();
            }
        });
        myContainer.add(pauseButton);

        theIcon = new ImageIcon("resources/images/fast_forward.png");
        fasterButton = new RoundButton(theIcon);
        fasterButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        fasterButton.setToolTipText("Faster");
        fasterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.changePlayBackSpeed(true);
            }
        });
        myContainer.add(fasterButton);

        theIcon = new ImageIcon("resources/images/previous_frame.png");
        stepBackwardButton = new RoundButton(theIcon);
        stepBackwardButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        stepBackwardButton.setToolTipText("Step Back");
        stepBackwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.stepBackward();
            }
        });
        myContainer.add(stepBackwardButton);

        theIcon = new ImageIcon("resources/images/play.png");
        playButton = new RoundButton(theIcon);
        playButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        playButton.setToolTipText("Play");
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.resume();
            }
        });

        myContainer.add(playButton);

        theIcon = new ImageIcon("resources/images/next_frame.png");
        stepForwardButton = new RoundButton(theIcon);
        stepForwardButton.setBounds(10 + xOrder++ * (KNOB_SIZE + 10), 10, KNOB_SIZE, KNOB_SIZE);
        stepForwardButton.setToolTipText("Step Forward");
        stepForwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isPlaying())
                    player.stepForward();
            }
        });
        myContainer.add(stepForwardButton);

        slider = new JSlider(0, player.getNumFrames(), player.getFrame());
        slider.addChangeListener(this);
        slider.setBounds(10, 50, 380, 20);
        slider.setEnabled(false);
        slider.setMajorTickSpacing(1000);
        slider.setMinorTickSpacing(500);
        slider.setPaintTicks(true);
        slider.setToolTipText("Select Frame");

        myContainer.add(slider);
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
        private static final long serialVersionUID = 1L;

        protected Shape           shape;

        protected Shape           base;

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
