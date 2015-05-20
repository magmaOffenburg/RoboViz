package rv.comm.rcssserver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import rv.world.WorldModel;

public class FindGoalsThread extends Thread {

    public interface ResultCallback {

        void goalFound(int goalFrame);

        void finished();
    }

    private final File           file;
    private final ResultCallback callback;

    private WorldModel           world;
    private MessageParser        parser;
    private ILogfileReader       logfile;
    private int                  lastScoreLeft  = -1;
    private int                  lastScoreRight = -1;
    private boolean              aborted        = false;

    public FindGoalsThread(File file, ResultCallback callback) {
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
    }

    private void processFrame() {
        String msg = logfile.getCurrentFrameMessage();
        if (msg != null) {
            try {
                parser.parse(msg);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int scoreLeft = world.getGameState().getScoreLeft();
            int scoreRight = world.getGameState().getScoreRight();

            if (lastScoreLeft != -1 && lastScoreRight != -1
                    && (scoreLeft != lastScoreLeft || scoreRight != lastScoreRight)) {
                callback.goalFound(logfile.getCurrentFrame());
            }

            lastScoreLeft = scoreLeft;
            lastScoreRight = scoreRight;
        }
    }
}