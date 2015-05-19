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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import js.math.Maths;
import rv.util.observer.IObserver;
import rv.util.observer.ISubscribe;
import rv.util.observer.Subject;
import rv.world.WorldModel;

/**
 * Reads simulation messages from a logfile instead of rcssserver3d
 * 
 * @author justin
 * 
 */
public class LogPlayer implements ISubscribe<Boolean> {

    private static final int       MS_PER_MESSAGE     = 200;
    private static final int       GOAL_WINDOW_FRAMES = 60;

    private ILogfileReader         logfile;
    private LogRunnerThread        logRunner;
    private final MessageParser    parser;
    private boolean                playing;
    private double                 playbackSpeed      = 1;
    private Integer                desiredFrame       = null;
    private List<Integer>          goalFrames         = new ArrayList<>();
    private boolean                goalsProcessed     = false;

    /** the list of observers that are informed if something changes */
    private final Subject<Boolean> observers;

    /** file chooser for opening logfiles (instance attribute to stay in the selected path) */
    private final JFileChooser     fileChooser;

    /**
     * Default constructor. Opens the passed logfile and starts playing.
     * 
     * @param file
     *            the logfile to open. Supported are log, zipped logs and tar.bz2
     * @param world
     *            reference to the world model
     */
    public LogPlayer(File file, WorldModel world) {

        observers = new Subject<>();
        playing = false;
        fileChooser = new JFileChooser();
        parser = new MessageParser(world);

        if (logfile == null)
            return;

        openLogfile(file);

        if (!logfile.isValid()) {
            System.out.println("Logfile could not be loaded.");
            return;
        }

        startRunnerThread();
    }

    public void setWorldModel(WorldModel world) {
        parser.setWorldModel(world);
    }

    public boolean isValid() {
        return logfile != null && logfile.isValid();
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isAtEnd() {
        return logfile != null && logfile.isAtEndOfLog();
    }

    public int getDesiredFrame() {
        return desiredFrame == null ? getFrame() : desiredFrame;
    }

    private int getFrame() {
        if (logfile == null) {
            return 0;
        }
        return logfile.getCurrentFrame();
    }

    public int getNumFrames() {
        if (logfile == null) {
            return 0;
        }
        return logfile.getNumFrames();
    }

    public void pause() {
        setPlaying(false);
    }

    public void resume() {
        setPlaying(true);
    }

    public void rewind() {
        setDesiredFrame(0);
    }

    public void setPlayBackSpeed(double factor) {
        playbackSpeed = Maths.clamp(factor, -10, 10);
        observers.onStateChange(playing);
    }

    public void increasePlayBackSpeed() {
        setPlayBackSpeed(getPlayBackSpeed() + 0.25);
    }

    public void decreasePlayBackSpeed() {
        setPlayBackSpeed(getPlayBackSpeed() - 0.25);
    }

    public double getPlayBackSpeed() {
        return playbackSpeed;
    }

    private void parseFrame() throws ParseException {
        String msg = logfile.getCurrentFrameMessage();
        if (msg != null)
            parser.parse(msg);
    }

    public void stepBackward() {
        try {
            logfile.stepBackward();
            parseFrame();
            observers.onStateChange(playing);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stepForward() {
        try {
            logfile.stepForward();
            parseFrame();
            observers.onStateChange(playing);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stepBackwardGoal() {
        int relativeFrame = getFrame();
        int closestFrame = -1;
        for (Integer goalFrame : Collections.synchronizedList(goalFrames)) {
            if (goalFrame < relativeFrame && goalFrame > closestFrame) {
                closestFrame = goalFrame;
            }
        }
        if (closestFrame != -1) {
            int targetFrame = Math.max(closestFrame - GOAL_WINDOW_FRAMES, 0);
            setDesiredFrame(targetFrame);
        }
    }

    public void stepForwardGoal() {
        int relativeFrame = getFrame() + GOAL_WINDOW_FRAMES;
        int closestFrame = Integer.MAX_VALUE;
        for (Integer goalFrame : Collections.synchronizedList(goalFrames)) {
            if (goalFrame > relativeFrame && goalFrame < closestFrame) {
                closestFrame = goalFrame;
            }
        }
        if (closestFrame != Integer.MAX_VALUE) {
            int targetFrame = Math.max(closestFrame - GOAL_WINDOW_FRAMES, 0);
            setDesiredFrame(targetFrame);
        }
    }

    public boolean hasGoals() {
        return !goalFrames.isEmpty();
    }

    public boolean goalsProcessed() {
        return goalsProcessed;
    }

    public void setDesiredFrame(int frame) {
        desiredFrame = frame;
    }

    @Override
    public void attach(IObserver<Boolean> observer) {
        observers.attach(observer);
        observers.onStateChange(false);
    }

    private void setPlaying(boolean playing) {
        if (playing != this.playing) {
            this.playing = playing;
            observers.onStateChange(playing);
        }
    }

    /**
     * Allows the user to choose a logfile to open.
     */
    public void openFile(JFrame parent) {
        if (logfile != null) {
            File logDir = new File(logfile.getFile().getPath());
            fileChooser.setCurrentDirectory(logDir);
        }
        int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File logFile = fileChooser.getSelectedFile();
        if (logFile.exists()) {
            openLogfile(logFile);
            resume();
        }
        startRunnerThread();
    }

    /**
     * Creates a new instance of a buffered logfile reader representing the passed file.
     * 
     * @param file
     *            the logfile to open
     */
    private void openLogfile(File file) {
        try {
            if (logfile != null) {
                logfile.close();
            }
            logfile = new LogfileReaderBuffered(new Logfile(file), 200);
            startGoalFinder(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRunnerThread() {
        if (logRunner != null) {
            logRunner.interrupt();
        }
        logRunner = new LogRunnerThread();
        logRunner.start();
    }

    private void startGoalFinder(File file) {
        new FindGoalsThread(file, new FindGoalsThread.ResultCallback() {
            @Override
            public void goalFound(int goalFrame) {
                goalFrames.add(goalFrame);
                observers.onStateChange(playing);
            }

            @Override
            public void finished() {
                LogPlayer.this.goalsProcessed = true;
            }
        }).start();
    }

    public boolean hasLogfile() {
        return logfile != null;
    }

    private class LogRunnerThread extends Thread {
        @Override
        public void run() {
            setPlaying(true);

            while (true) {
                if (logfile.isAtEndOfLog())
                    pause();

                int previousFrame = getFrame();
                int nextFrame = getFrame();
                try {
                    if (playing && playbackSpeed != 0) {
                        Thread.sleep(Math.abs((int) (MS_PER_MESSAGE / playbackSpeed)));
                        if (playbackSpeed > 0)
                            nextFrame++;
                        else
                            nextFrame--;
                    } else {
                        Thread.sleep(MS_PER_MESSAGE);
                    }

                    if (desiredFrame != null) {
                        nextFrame = desiredFrame;
                        desiredFrame = null;
                    }
                } catch (InterruptedException e) {
                }

                setCurrentFrame(previousFrame, nextFrame);
                observers.onStateChange(playing);
            }
        }

        private void setCurrentFrame(int previousFrame, int frame) {
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

        private void stepAnywhere(int frame) throws ParseException, IOException {
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
}
