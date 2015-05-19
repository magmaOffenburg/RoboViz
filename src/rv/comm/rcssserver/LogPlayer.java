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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
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

    private static final int       DEFAULT_TIMER_DELAY = 150;

    private static final int       GOAL_WINDOW_FRAMES  = 60;

    private ILogfileReader         logfile;
    private final MessageParser    parser;
    private Timer                  timer;
    private boolean                playing;
    private double                 playbackSpeed       = 1;
    private Integer                desiredFrame        = null;
    private List<Integer>          goalFrames          = new ArrayList<>();
    private boolean                goalsProcessed      = false;

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
        openLogfile(file);

        timer = new Timer(DEFAULT_TIMER_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (desiredFrame != null) {
                        setCurrentFrame(desiredFrame);
                        desiredFrame = null;
                    }
                    play();
                } catch (Exception e1) {
                    System.out.println("Invalid Logfile.");
                    e1.printStackTrace();
                    timer.stop();
                    logfile = null;
                    observers.onStateChange(false);
                }
            }
        });
        timer.setRepeats(true);
        timer.setDelay(DEFAULT_TIMER_DELAY);

        parser = new MessageParser(world);

        if (!logfile.isValid()) {
            System.out.println("Logfile could not be loaded.");
            return;
        }
        timer.start();
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

    public int getFrame() {
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
        timer.stop();
    }

    public void stop() {
        setPlaying(false);
        timer.stop();
        logfile.close();
    }

    public void resume() {
        if (!playing)
            timer.start();
    }

    public void rewind() {
        try {
            logfile.rewind();
            parseFrame();
            observers.onStateChange(playing);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlayBackSpeed(double factor) {
        int newDelay = (int) (DEFAULT_TIMER_DELAY / factor);
        timer.setDelay(Maths.clamp(newDelay, 15, 1500));
        playbackSpeed = Maths.clamp(factor, 0.25, 10);
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

    public void play() throws IOException, ParseException {
        setPlaying(true);

        if (logfile.isAtEndOfLog())
            pause();
        else {
            parseFrame();
            logfile.stepForward();
            observers.onStateChange(playing);
        }
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
            setCurrentFrame(targetFrame);
            observers.onStateChange(playing);
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
            setCurrentFrame(targetFrame);
            observers.onStateChange(playing);
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

    private void setCurrentFrame(int frame) {
        if (frame == getFrame()) {
            return;
        }
        try {
            // when jumping forwards we have to make sure not to jump over a full
            // frame
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

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File logFile = fileChooser.getSelectedFile();
        if (logFile.exists()) {
            openLogfile(logFile);
            resume();
        }
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
        } catch (Exception e3) {
            e3.printStackTrace();
        }
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
}
