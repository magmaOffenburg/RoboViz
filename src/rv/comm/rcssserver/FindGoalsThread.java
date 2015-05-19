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
    private boolean              lastFrameGoalLeft  = false;
    private boolean              lastFrameGoalRight = false;

    public FindGoalsThread(File file, ResultCallback callback) {
        super();
        this.file = file;
        this.callback = callback;
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

        while (!logfile.isAtEndOfLog()) {
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

            switch (world.getGameState().getPlayMode()) {
            case "Goal_Left":
                if (!lastFrameGoalLeft) {
                    callback.goalFound(logfile.getCurrentFrame());
                    lastFrameGoalLeft = true;
                    lastFrameGoalRight = false;
                }
                break;
            case "Goal_Right":
                if (!lastFrameGoalRight) {
                    callback.goalFound(logfile.getCurrentFrame());
                    lastFrameGoalLeft = false;
                    lastFrameGoalRight = true;
                }
                break;
            default:
                lastFrameGoalLeft = false;
                lastFrameGoalRight = false;
                break;
            }
        }
    }
}