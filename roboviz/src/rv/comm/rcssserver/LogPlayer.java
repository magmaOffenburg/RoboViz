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

public class LogPlayer {

    private MessageParser parser;
    private final Timer   timer;
    int                   frame   = 0;
    int                   offset  = 1;
    int                   delay   = 150;
    private String[]      serverMessages;
    private boolean       playing = false;

    public boolean isPlaying() {
        return playing;
    }

    public int getFrame() {
        return frame;
    }

    public int getNumFrames() {
        return serverMessages.length;
    }

    public LogPlayer(File file, WorldModel world) {
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

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            ArrayList<String> input = new ArrayList<String>();
            String l;
            while ((l = br.readLine()) != null)
                input.add(l);

            serverMessages = new String[input.size()];
            input.toArray(serverMessages);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    }

    public void resume() {
        timer.start();
    }

    public void rewind() {
        setFrame(0);
    }

    public void addDelay(int ms) {
        int delay = Maths.clamp(timer.getDelay() + ms, 20, 1000);
        timer.setDelay(delay);
        System.out.printf("Player FPS: %.1f\n", 1000.0f / timer.getDelay());
    }

    public void setFrame(int frame) {
        this.frame = Maths.clamp(frame, 0, serverMessages.length - 1);
    }

    public void play() throws IOException, ParseException {
        playing = true;
        if (frame >= serverMessages.length || frame < 0)
            stop();
        else {
            parser.parse(serverMessages[frame]);
            frame += offset;
        }
    }
}
