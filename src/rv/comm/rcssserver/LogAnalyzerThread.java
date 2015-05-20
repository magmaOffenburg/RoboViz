package rv.comm.rcssserver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import rv.world.WorldModel;

public class LogAnalyzerThread extends Thread {

    public interface ResultCallback {

        void stepSizeFound(float stepSize, int numFrames);

        void goalFound(int goalFrame);

        void finished(int numFrames);
    }

    private final File           file;
    private final ResultCallback callback;

    private WorldModel           world;
    private MessageParser        parser;
    private ILogfileReader       logfile;
    private int                  lastScoreLeft  = -1;
    private int                  lastScoreRight = -1;
    private int                  numPauseFrames = 0;
    private Float                startTime      = null;
    private Float                lastTime       = null;
    private boolean              stepSizeFound  = false;
    private boolean              aborted        = false;

    public LogAnalyzerThread(File file, ResultCallback callback) {
        super();
        this.file = file;
        this.callback = callback;
    }

    public void abort() {
        this.aborted = true;
    }

    @Override
    public void run() {
        world = new WorldModel();
        parser = new MessageParser(world);
        logfile = null;

        try {
            logfile = new LogfileReaderBuffered(new Logfile(file), 200);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!logfile.isAtEndOfLog() && !aborted) {
            processFrame();
            try {
                logfile.stepForward();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        processFrame();

        callback.finished(logfile.getNumFrames());
    }

    private void processFrame() {
        String msg = logfile.getCurrentFrameMessage();
        if (msg != null) {
            try {
                parser.parse(msg);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            processGoals();
            processStepSize();
        }
    }

    private void processGoals() {
        int scoreLeft = world.getGameState().getScoreLeft();
        int scoreRight = world.getGameState().getScoreRight();

        if (lastScoreLeft != -1 && lastScoreRight != -1
                && (scoreLeft != lastScoreLeft || scoreRight != lastScoreRight)) {
            callback.goalFound(logfile.getCurrentFrame());
        }

        lastScoreLeft = scoreLeft;
        lastScoreRight = scoreRight;
    }

    private void processStepSize() {
        if (stepSizeFound)
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
                int numFrames = (int) ((1 / stepSize) * halfTime) + numPauseFrames;
                callback.stepSizeFound(stepSize, numFrames);
                stepSizeFound = true;
            }

            lastTime = time;
        }
    }
}