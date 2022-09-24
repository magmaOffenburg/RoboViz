package rv.comm.rcssserver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rv.world.Team;
import rv.world.WorldModel;

public class LogAnalyzerThread extends Thread
{
	public static class Goal
	{
		public final int frame;
		public final int viewFrame;
		public final int scoringTeam;

		public Goal(int frame, int viewFrame, int scoringTeam)
		{
			this.frame = frame;
			this.viewFrame = viewFrame;
			this.scoringTeam = scoringTeam;
		}
	}

	public interface ResultCallback
	{
		void stepSizeFound(float stepSize, int numFrames);

		void goalFound(Goal goal);

		void finished(int numFrames);
	}

	private static final Logger LOGGER = LogManager.getLogger();

	private final File file;
	private final ResultCallback callback;

	private WorldModel world;
	private MessageParser parser;
	private ILogfileReader logfile;
	private int lastScoreLeft = -1;
	private int lastScoreRight = -1;
	private int numPauseFrames = 0;
	private Float startTime = null;
	private Float lastTime = null;
	private Float stepSize = null;
	private boolean aborted = false;
	private final LogPlayer logPlayer;

	public LogAnalyzerThread(File file, ResultCallback callback, LogPlayer logPlayer)
	{
		super();
		this.file = file;
		this.callback = callback;
		this.logPlayer = logPlayer;
	}

	public void abort()
	{
		this.aborted = true;
	}

	@Override
	public void run()
	{
		world = new WorldModel();
		parser = new MessageParser(world);
		logfile = null;

		try {
			logfile = new LogfileReaderBuffered(new Logfile(file, false), 200);
			logfile.addListener(logPlayer);
		} catch (Exception e) {
			LOGGER.error("Unable to open logfile", e);
		}

		while (!logfile.isAtEndOfLog() && !aborted) {
			processFrame();
			try {
				logfile.stepForward();
			} catch (IOException e) {
				LOGGER.error("Unable to read logfile", e);
			}
		}
		processFrame();

		callback.finished(logfile.getNumFrames());
	}

	private void processFrame()
	{
		String msg = logfile.getCurrentFrameMessage();
		if (msg != null) {
			try {
				parser.parse(msg);
			} catch (ParseException e) {
				LOGGER.error("Unable to parse frame message", e);
			}

			processGoals();
			processStepSize();
		}
	}

	private void processGoals()
	{
		int scoreLeft = world.getGameState().getScoreLeft();
		int scoreRight = world.getGameState().getScoreRight();

		int scoringTeam = -1;
		if (lastScoreLeft != -1 && scoreLeft != lastScoreLeft) {
			scoringTeam = Team.LEFT;
		} else if (lastScoreRight != -1 && scoreRight != lastScoreRight) {
			scoringTeam = Team.RIGHT;
		}

		if (scoringTeam != -1) {
			int frame = logfile.getCurrentFrame();
			int goalWindowFrames = Math.round((1 / stepSize) * LogPlayer.GOAL_WINDOW_SECONDS);
			int viewFrame = Math.max(0, frame - goalWindowFrames);
			callback.goalFound(new Goal(frame, viewFrame, scoringTeam));
		}

		lastScoreLeft = scoreLeft;
		lastScoreRight = scoreRight;
	}

	private void processStepSize()
	{
		if (stepSize != null)
			return;

		Float time = world.getGameState().getTime();

		if (startTime == null) {
			startTime = time;
		} else {
			if (startTime.equals(time)) {
				numPauseFrames++;
				return;
			}

			time -= startTime;
			if (lastTime != null && !time.equals(lastTime)) {
				float stepSize = time - lastTime;
				// estimate total number of frames
				float halfTime = world.getGameState().getHalfTime();
				int numFrames = Math.round(((1 / stepSize) * halfTime) + numPauseFrames);
				callback.stepSizeFound(stepSize, numFrames);
				this.stepSize = stepSize;
			}

			lastTime = time;
		}
	}
}