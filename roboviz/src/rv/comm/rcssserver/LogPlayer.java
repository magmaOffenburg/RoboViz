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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.Timer;

import js.math.Maths;
import rv.world.WorldModel;

/**
 * Reads simulation messages from a logfile instead of rcssserver3d
 * @author justin
 *
 */
public class LogPlayer {

    private Logfile       logfile;
    private MessageParser parser;
    private final Timer   timer;
    int                   delay   = 150;
    private boolean       playing = false;

    public boolean isPlaying() {
        return playing;
    }

    public int getFrame() {
        return logfile.getCurrentFrame();
    }

    public int getNumFrames() {
        return logfile.getNumFrames();
    }
    
    public Logfile getLogfile() {
        return logfile;
    }

    public LogPlayer(File file, WorldModel world) {

        try {
            logfile = new Logfile(file);
        } catch (Exception e3) {
            e3.printStackTrace();
        }

        timer = new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    play();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    timer.stop();
                } catch (ParseException e2) {
                    e2.printStackTrace();
                    timer.stop();
                }
            }
        });
        timer.setRepeats(true);
        timer.setDelay(delay);

        parser = new MessageParser(world);

        timer.start();
    }

    public void pause() {
        playing = false;
        timer.stop();
    }

    public void stop() {
        playing = false;
        timer.stop();
        if (logfile.isOpen())
            logfile.close();
    }

    public void resume() {
        if (!playing)
            timer.start();
    }

    public void rewind() {
        logfile.close();
        try {
            logfile.open();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDelay(int ms) {
        int delay = Maths.clamp(timer.getDelay() + ms, 20, 1000);
        timer.setDelay(delay);
        System.out.printf("Player FPS: %.1f\n", 1000.0f / timer.getDelay());
    }

    private void parseFrame() throws ParseException {
        String msg = logfile.getCurrrentFrameMessage();
        if (msg != null)
            parser.parse(msg);
    }

    public void play() throws IOException, ParseException {
        playing = true;

        if (logfile.isAtEndOfLog())
            stop();
        else {
            if (!logfile.isOpen())
                logfile.open();
            logfile.stepForward();
            parseFrame();
        }
    }
    
    public void stepBackward() {
        try {
            logfile.stepBackward();
            parseFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stepForward() {
        try {
            logfile.stepForward();
            parseFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
