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

package rv.comm.rcssserver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import jsgl.math.Maths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.configuration.Config.General;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.rcssserver.ILogfileReader.LogfileListener;
import rv.comm.rcssserver.LogAnalyzerThread.Goal;
import rv.util.StringUtil;
import rv.util.swing.FileChooser;
import rv.world.WorldModel;

/**
 * Reads simulation messages from a logfile instead of rcssserver3d
 *
 * @author justin
 *
 */
public class LogPlayer implements LogfileListener
{
	public interface StateChangeListener
	{
		void playerStateChanged(boolean playing);

		void logfileChanged();
	}

	private static final Logger LOGGER = LogManager.getLogger();

	/** the $monitorLoggerStep value from spark.rb */
	private static float SECONDS_PER_FRAME = 0.2f;
	/** how many seconds before a goal to jump to */
	public static final int GOAL_WINDOW_SECONDS = 12;
	/** time within which to jump over goals for nicer stepping during playback */
	private static final float GOAL_STEP_THRESHOLD_SECONDS = 3f;

	private ILogfileReader logfile;
	private LogRunnerThread logRunner;
	private LogAnalyzerThread logAnalyzer;
	private final MessageParser parser;
	private boolean playing;
	private double playbackSpeed = 1;
	private Integer desiredFrame = null;
	private final List<Goal> goals = new CopyOnWriteArrayList<>();
	private final List<StateChangeListener> listeners = new CopyOnWriteArrayList<>();
	private boolean logAnalyzed = false;
	private int analyzedFrames = 0;
	private boolean logfileHasDrawCmds = false;
	private boolean foundStepSize = false;

	/**
	 * Default constructor. Opens the passed logfile and starts playing.
	 *
	 * @param file
	 *            the logfile to open. Supported are log, zipped logs and tar.bz2
	 * @param world
	 *            reference to the world model
	 */
	public LogPlayer(File file, WorldModel world)
	{
		playing = false;
		parser = new MessageParser(world);

		if (!file.exists())
			return;

		openLogfile(file);

		if (!logfile.isValid()) {
			LOGGER.error("Logfile could not be loaded.");
			return;
		}

		startRunnerThread();
	}

	public void setWorldModel(WorldModel world)
	{
		parser.setWorldModel(world);
	}

	public void addListener(StateChangeListener l)
	{
		listeners.add(l);
		stateChanged();
	}

	private void stateChanged()
	{
		for (StateChangeListener l : listeners)
			l.playerStateChanged(playing);
	}

	public boolean isValid()
	{
		return logfile != null && logfile.isValid();
	}

	public boolean isPlaying()
	{
		return playing;
	}

	public boolean isAtBeginning()
	{
		return logfile != null && logfile.isAtBeginningOfLog();
	}

	public boolean isAtEnd()
	{
		return logfile != null && logfile.isAtEndOfLog();
	}

	public int getDesiredFrame()
	{
		return desiredFrame == null ? getFrame() : desiredFrame;
	}

	public int getFrame()
	{
		if (logfile == null) {
			return 0;
		}
		return logfile.getCurrentFrame();
	}

	public int getNumFrames()
	{
		if (logfile == null) {
			return 0;
		}
		return logfile.getNumFrames();
	}

	public void pause()
	{
		setPlaying(false);
	}

	public void resume()
	{
		setPlaying(true);
	}

	public void rewind()
	{
		Renderer.Companion.getDrawings().clearAllShapeSets();
		setDesiredFrame(0);
	}

	public void setPlayBackSpeed(double factor)
	{
		playbackSpeed = Maths.clamp(factor, -10, 10);
		stateChanged();
	}

	public void increasePlayBackSpeed()
	{
		setPlayBackSpeed(getPlayBackSpeed() + 0.25);
	}

	public void decreasePlayBackSpeed()
	{
		setPlayBackSpeed(getPlayBackSpeed() - 0.25);
	}

	public double getPlayBackSpeed()
	{
		return playbackSpeed;
	}

	private void parseFrame() throws ParseException
	{
		String msg = logfile.getCurrentFrameMessage();
		if (msg != null)
			parser.parse(msg);
	}

	public void stepBackward()
	{
		setDesiredFrame(getFrame() - 1);
	}

	public void stepForward()
	{
		setDesiredFrame(getFrame() + 1);
	}

	public void stepBackwardGoal()
	{
		int relativeFrame = getDesiredFrame() - getGoalStepThresholdFrames();
		int closestFrame = -1;
		for (Goal goal : goals) {
			if (goal.viewFrame < relativeFrame && goal.viewFrame > closestFrame) {
				closestFrame = goal.viewFrame;
			}
		}
		if (closestFrame != -1) {
			setDesiredFrame(closestFrame);
		}
	}

	public void stepForwardGoal()
	{
		int relativeFrame = getDesiredFrame() + getGoalStepThresholdFrames();
		int closestFrame = Integer.MAX_VALUE;
		for (Goal goal : goals) {
			if (goal.viewFrame > relativeFrame && goal.viewFrame < closestFrame) {
				closestFrame = goal.viewFrame;
			}
		}
		if (closestFrame != Integer.MAX_VALUE) {
			setDesiredFrame(closestFrame);
		}
	}

	public String getFilePath()
	{
		if (logfile == null)
			return null;
		return logfile.getFile().getPath();
	}

	private int getGoalStepThresholdFrames()
	{
		float fps = 1 / SECONDS_PER_FRAME;
		return Math.round(fps * GOAL_STEP_THRESHOLD_SECONDS);
	}

	public boolean hasPreviousGoal()
	{
		if (goals.isEmpty())
			return false;

		for (Goal goal : goals) {
			if (getDesiredFrame() - getGoalStepThresholdFrames() > goal.viewFrame)
				return true;
		}
		return false;
	}

	public boolean hasNextGoal()
	{
		if (goals.isEmpty())
			return false;

		for (Goal goal : goals) {
			if (getDesiredFrame() + getGoalStepThresholdFrames() < goal.viewFrame)
				return true;
		}
		return false;
	}

	public String getPreviousGoalMessage()
	{
		return formatGoalMessage(getPreviousGoalNumber(), "previous");
	}

	public String getNextGoalMessage()
	{
		return formatGoalMessage(getNextGoalNumber(), "next");
	}

	private String formatGoalMessage(Integer targetGoalFrame, String direction)
	{
		if ((goals.isEmpty() && logAnalyzed) || targetGoalFrame == null) {
			return "No " + direction + " goals";
		}
		return StringUtil.capitalize(direction) + " goal: " + targetGoalFrame + "/" + goals.size();
	}

	private Integer getPreviousGoalNumber()
	{
		Integer previousGoalNumber = null;
		for (int i = 0; i < goals.size(); i++) {
			if (getDesiredFrame() - getGoalStepThresholdFrames() > goals.get(i).viewFrame) {
				previousGoalNumber = i + 1;
			}
		}
		return previousGoalNumber;
	}

	private Integer getNextGoalNumber()
	{
		Integer nextGoalNumber = null;
		for (int i = 0; i < goals.size(); i++) {
			if (getDesiredFrame() + getGoalStepThresholdFrames() < goals.get(i).viewFrame) {
				nextGoalNumber = i + 1;
				break;
			}
		}
		return nextGoalNumber;
	}

	public List<Goal> getGoals()
	{
		return goals;
	}

	public boolean logAnalyzed()
	{
		return logAnalyzed;
	}

	public int getAnalyzedFrames()
	{
		return analyzedFrames;
	}

	public void setDesiredFrame(int frame)
	{
		desiredFrame = frame;
		stateChanged();
	}

	public boolean logfileHasDrawCmds()
	{
		return logfileHasDrawCmds;
	}

	private void setPlaying(boolean playing)
	{
		if (playing != this.playing) {
			this.playing = playing;
			stateChanged();
		}
	}

	public void openFileDialog(JFrame parent)
	{
		JFileChooser fileChooser = new FileChooser();
		String logfileDirectory = General.INSTANCE.getLogfileDirectory();
		if (logfileDirectory != null && !logfileDirectory.isEmpty()) {
			fileChooser.setCurrentDirectory(new File(logfileDirectory));
		}
		int returnVal = fileChooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			return;
		}

		File logFile = fileChooser.getSelectedFile();
		if (logFile.exists()) {
			openLogfile(logFile);
			rewind();
			startRunnerThread();
		}
	}

	/**
	 * Creates a new instance of a buffered logfile reader representing the passed file.
	 */
	private void openLogfile(File file)
	{
		try {
			if (logfile != null) {
				logfile.close();
				desiredFrame = null;
				goals.clear();
				logAnalyzed = false;
				analyzedFrames = 0;
				logfileHasDrawCmds = false;
				foundStepSize = false;
			}
			logfile = new LogfileReaderBuffered(new Logfile(file, true), 200);
			logfile.addListener(this);
			startAnalyzerThread(file);

			for (StateChangeListener l : listeners)
				l.logfileChanged();
		} catch (Exception e) {
			LOGGER.error("Unable to open log file", e);
		}
	}

	private void startRunnerThread()
	{
		if (logRunner != null) {
			logRunner.abort();
		}
		logRunner = new LogRunnerThread();
		logRunner.start();
	}

	private void startAnalyzerThread(File file)
	{
		if (logAnalyzer != null) {
			logAnalyzer.abort();
		}
		logAnalyzer = new LogAnalyzerThread(file, new LogAnalyzerThread.ResultCallback() {
			@Override
			public void stepSizeFound(float stepSize, int numFrames)
			{
				foundStepSize = true;
				SECONDS_PER_FRAME = stepSize;
				logfile.setNumFrames(numFrames);
			}

			@Override
			public void goalFound(Goal goal)
			{
				goals.add(goal);
				analyzedFrames = goal.frame;
				stateChanged();
			}

			@Override
			public void finished(int numFrames)
			{
				LogPlayer.this.logAnalyzed = true;
				logfile.setNumFrames(numFrames);
				analyzedFrames = numFrames;
				stateChanged();
			}
		}, this);
		logAnalyzer.start();
	}

	public void stopLogPlayer()
	{
		if (logRunner != null)
			logRunner.abort();
		if (logAnalyzer != null)
			logAnalyzer.abort();

		if (logfile != null) {
			logfile.close();
			logfile = null;
			for (StateChangeListener l : listeners)
				l.logfileChanged();
		}

		goals.clear();
	}

	private class LogRunnerThread extends Thread
	{
		private boolean aborted;

		public void abort()
		{
			this.aborted = true;
		}

		@Override
		public void run()
		{
			setPlaying(true);

			try {
				// Make sure we parse and render the first frame of the log
				parseFrame();
			} catch (Exception e) {
			}

			while (!aborted) {
				if ((logfile.isAtEndOfLog() && playbackSpeed > 0) ||
						(logfile.isAtBeginningOfLog() && playbackSpeed < 0))
					pause();

				int previousFrame = getFrame();
				int nextFrame = getFrame();
				try {
					float msPerFrame = SECONDS_PER_FRAME * 1000;
					if (playing && playbackSpeed != 0) {
						Thread.sleep(Math.abs((int) Math.round(msPerFrame / playbackSpeed)));
						if (playbackSpeed > 0)
							nextFrame++;
						else
							nextFrame--;
					} else {
						Thread.sleep(Math.round(msPerFrame));
					}

					if (desiredFrame != null) {
						nextFrame = desiredFrame;
						desiredFrame = null;
					}
				} catch (InterruptedException e) {
				}

				setCurrentFrame(previousFrame, nextFrame);
				stateChanged();
			}
		}

		private void setCurrentFrame(int previousFrame, int frame)
		{
			if (frame == getFrame()) {
				return;
			}

			try {
				if (previousFrame + 1 == frame) {
					logfile.stepForward();
					parseFrame();
				} else {
					stepAnywhere(frame);
				}
			} catch (Exception e) {
			}
		}

		private void stepAnywhere(int frame) throws ParseException, IOException
		{
			// when jumping forwards we have to make sure not to jump over a full frame
			int currentFrame = frame;
			boolean needHeader = true;
			do {
				logfile.stepAnywhere(currentFrame);
				try {
					parseFrame();
					needHeader = false;
				} catch (IndexOutOfBoundsException e) {
					// the frame misses some information from the last full frame
					// DIRTY: I have no idea currently how this can be detected in a
					// nicer way
				}
				currentFrame--;
			} while (needHeader && currentFrame >= 0);
		}
	}

	@Override
	public void haveDrawCmds()
	{
		logfileHasDrawCmds = true;
		if (!foundStepSize) {
			// We know this is a roboviz log so use the default step size for this
			SECONDS_PER_FRAME = 0.04f;
		}
	}
}
