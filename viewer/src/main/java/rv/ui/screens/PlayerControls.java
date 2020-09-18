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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import rv.comm.rcssserver.LogPlayer;
import rv.ui.FramePanelBase;

/**
 * Dialog containing the media player controls for log mode
 *
 * @author dorer
 */
class PlayerControls extends FramePanelBase implements LogPlayer.StateChangeListener
{
	private static PlayerControls instance;

	public static PlayerControls getInstance(LogPlayer player)
	{
		if (instance != null) {
			instance.dispose();
		}
		return instance = new PlayerControls(player);
	}

	private final LogPlayer player;
	private Container container;
	private JButton rewindButton;
	private JButton previousFrameButton;
	private JButton playPauseButton;
	private JButton nextFrameButton;
	private JButton previousGoalButton;
	private JButton nextGoalButton;
	private JSpinner playbackSpeedSpinner;
	private JSlider slider;
	private boolean sliderUpdate;
	
	private ImageIcon fileOpenIcon;
	private ImageIcon rewindIcon;
	private ImageIcon previousFrameIcon;
	private ImageIcon playIcon;
	private ImageIcon pauseIcon;
	private ImageIcon nextFrameIcon;
	private ImageIcon previousGoalIcon;
	private ImageIcon nextGoalIcon;
	
	private PlayerControls(LogPlayer player)
	{
		super("Logplayer");
		this.player = player;
		createControls();
		this.player.addListener(this);
	}

	/**
	 * Create the buttons and other GUI controls
	 */
	private void createControls()
	{
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(380, 120);
		frame.setResizable(false);
		container = frame.getContentPane();

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1, 1, 1, 1, 1, 1};
		layout.columnWeights = new double[] {0, 0, 0, 0, 0, 0};
		layout.rowHeights = new int[] {1, 1};
		layout.rowWeights = new double[] {0.0, 0.0};
		container.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 5, 0, 0);
		
		fileOpenIcon = new ImageIcon(getClass().getResource("/images/baseline_folder_open_black_24dp.png"));
		rewindIcon = new ImageIcon(getClass().getResource("/images/baseline_replay_black_24dp.png"));
		previousFrameIcon = new ImageIcon(getClass().getResource("/images/baseline_skip_previous_black_24dp.png"));
		playIcon = new ImageIcon(getClass().getResource("/images/baseline_pause_black_24dp.png"));
		pauseIcon = new ImageIcon(getClass().getResource("/images/baseline_play_arrow_black_24dp.png"));
		nextFrameIcon = new ImageIcon(getClass().getResource("/images/baseline_skip_next_black_24dp.png"));
		previousGoalIcon = new ImageIcon(getClass().getResource("/images/baseline_undo_black_24dp.png"));
		nextGoalIcon = new ImageIcon(getClass().getResource("/images/baseline_redo_black_24dp.png"));
		
		createButton(c, fileOpenIcon, "Open logfile...", e -> player.openFileDialog(frame));

		rewindButton = createButton(c, rewindIcon, "Rewind", e -> player.rewind());

		previousFrameButton = createButton(c, previousFrameIcon, "Step back", e -> {
			if (!player.isPlaying())
				player.stepBackward();
		});

		playPauseButton = createButton(c, pauseIcon, "Pause", e -> {
			if (player.isPlaying()) {
				player.pause();
				playPauseButton.setIcon(playIcon);
				playPauseButton.setToolTipText("Play");
			} else {
				player.resume();
				playPauseButton.setIcon(pauseIcon);
				playPauseButton.setToolTipText("Pause");
			}
		});

		nextFrameButton = createButton(c, nextFrameIcon, "Step forward", e -> {
			if (!player.isPlaying())
				player.stepForward();
		});

		previousGoalButton = createButton(c, previousGoalIcon, null, e -> player.stepBackwardGoal());

		nextGoalButton = createButton(c, nextGoalIcon, null, e -> player.stepForwardGoal());

		c.gridx++;
		c.insets = new Insets(0, 25, 0, 0);
		playbackSpeedSpinner = new JSpinner(new SpinnerNumberModel(1, -10, 10, 0.25));
		playbackSpeedSpinner.setToolTipText("Playback speed factor");
		playbackSpeedSpinner.setPreferredSize(new Dimension(60, 30));
		playbackSpeedSpinner.addChangeListener(e -> player.setPlayBackSpeed((double) playbackSpeedSpinner.getValue()));
		container.add(playbackSpeedSpinner, c);

		slider = new JSlider(0, player.getNumFrames(), player.getDesiredFrame());
		slider.addChangeListener(e -> {
			if (slider.isEnabled() && !sliderUpdate) {
				int frame = slider.getValue();
				player.setDesiredFrame(frame);

				boolean atBeginning = frame == 0;
				boolean atEnd = !(frame < player.getNumFrames());
				updateButtons(player.isPlaying(), atBeginning, atEnd);
			}
		});
		slider.setToolTipText("Select Frame");

		c.gridwidth = c.gridx + 1;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(15, 5, 0, 0);
		container.add(slider, c);
	}
	
	private JButton createButton(GridBagConstraints c, ImageIcon icon, String tooltip, ActionListener listener)
	{
		JButton button = new JButton();
		button.setPreferredSize(new Dimension(36, 36));
		button.setIcon(icon);
		button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		button.setToolTipText(tooltip);
		button.addActionListener(listener);

		c.gridx++;
		container.add(button, c);
		return button;
	}

	@Override
	public void playerStateChanged(boolean playing)
	{
		updateButtons(playing, player.isAtBeginning(), player.isAtEnd());
		updateSlider(playing);
	}

	@Override
	public void logfileChanged()
	{
	}

	private void updateButtons(Boolean playing, boolean atBeginning, boolean atEnd)
	{
		boolean isValid = player.isValid();
		rewindButton.setEnabled(isValid);
		previousFrameButton.setEnabled(isValid && !playing && !atBeginning);
		playPauseButton.setEnabled(isValid && (!atEnd || player.getPlayBackSpeed() <= 0) &&
								   (!atBeginning || player.getPlayBackSpeed() >= 0));
		playPauseButton.setIcon(playing ? pauseIcon : playIcon);
		previousGoalButton.setEnabled(isValid && player.hasPreviousGoal());
		previousGoalButton.setToolTipText(player.getPreviousGoalMessage());
		nextGoalButton.setEnabled(isValid && player.hasNextGoal());
		nextGoalButton.setToolTipText(player.getNextGoalMessage());
		nextFrameButton.setEnabled(isValid && !playing && !atEnd);
		playbackSpeedSpinner.setEnabled(isValid);
		playbackSpeedSpinner.setValue(player.getPlayBackSpeed());
	}

	private void updateSlider(Boolean playing)
	{
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
			SwingUtilities.invokeLater(() -> {
				slider.setValue(player.getDesiredFrame());
				slider.setEnabled(player.isValid());
				sliderUpdate = false;
			});
		}
	}

	public void dispose()
	{
		frame.dispose();
	}
	
}
