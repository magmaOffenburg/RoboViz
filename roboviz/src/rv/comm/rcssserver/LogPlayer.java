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

    private ILogfileReader   logfile;
    private MessageParser    parser;
    private Timer            timer;
    int                      delay   = 150;
    private boolean          playing = false;
    private Subject<Boolean> observers;

    public LogPlayer(File file, WorldModel world) {

        observers = new Subject<Boolean>();
        try {
            logfile = new LogfileReaderBuffered(new Logfile(file), 200);
        } catch (Exception e3) {
            e3.printStackTrace();
        }

        timer = new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
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
        timer.setDelay(delay);

        parser = new MessageParser(world);

        if (!logfile.isValid()) {
            System.out.println("Logfile could not be loaded.");
            return;
        }
        timer.start();
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

    public void changePlayBackSpeed(boolean accelerate) {
        int acc = 25;
        if (accelerate) {
            acc = -25;
        }
        int delay = Maths.clamp(timer.getDelay() + acc, 20, 1000);
        timer.setDelay(delay);
        System.out.printf("Player FPS: %.1f\n", 1000.0f / timer.getDelay());
    }

    private void parseFrame() throws ParseException {
        String msg = logfile.getCurrrentFrameMessage();
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

    public void setCurrentFrame(int frame) {
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
}
