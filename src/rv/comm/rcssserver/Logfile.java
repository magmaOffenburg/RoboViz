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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Abstraction for a log that can be viewed frame by frame. Supports unpacked, single file zipped
 * and tar.bz2 files.
 * 
 * @author justin
 */
public class Logfile implements ILogfileReader {

    /** used for sequentially playing frames */
    private BufferedReader br;

    /** the file to read from */
    private File           logsrc;

    /** index of the frame that is currently buffered */
    private int            curFramePtr;

    /** the number of frames in the logfile, initially estimated */
    private int            numFrames;

    /** stores the server message at the current frame position */
    private String         curFrameMsg;

    /**
     * Default constructor
     * 
     * @param file
     *            the logfile to open
     * @throws Exception
     *             if the logfile can not be opened
     */
    public Logfile(File file) throws Exception {
        this.logsrc = file;
        numFrames = 1700;
        open();
    }

    /**
     * Opens the file for buffered reading
     * 
     * @throws FileNotFoundException
     */
    private void open() throws IOException {
        br = TarBz2ZipUtil.createBufferedReader(logsrc);
        if (br != null) {
            curFrameMsg = br.readLine();
        }
        curFramePtr = 0;
    }

    @Override
    public boolean isValid() {
        return br != null;
    }

    @Override
    public boolean isAtEndOfLog() {
        return curFrameMsg == null;
    }

    @Override
    public int getNumFrames() {
        return numFrames;
    }

    @Override
    public int getCurrentFrame() {
        return curFramePtr;
    }

    @Override
    public String getCurrentFrameMessage() {
        return curFrameMsg;
    }

    private String setCurrentFrame(int frame) throws IOException {

        if (frame < curFramePtr) {
            // we have a sequential reader, for stepping backwards we have to start from beginning
            close();
            open();
        }

        String line = curFrameMsg;
        while (curFramePtr < frame && line != null) {
            line = stepForward();
        }
        return line;
    }

    @Override
    public void rewind() throws IOException {
        close();
        open();
    }

    @Override
    public void close() {
        try {
            br.close();
        } catch (Exception ex) {
        }
    }

    @Override
    public String stepForward() throws IOException {
        if (isAtEndOfLog())
            return null;

        curFrameMsg = br.readLine();
        curFramePtr++;
        if (curFramePtr >= numFrames) {
            // the number of frames was estimated too low
            numFrames++;
        } else if (curFrameMsg == null) {
            // the number of frames was estimated too high
            numFrames = curFramePtr + 1;
        }
        return curFrameMsg;
    }

    @Override
    public void stepBackward() throws IOException {
        if (curFramePtr > 0) {
            setCurrentFrame(curFramePtr - 1);
        }
    }

    @Override
    public void stepAnywhere(int frame) throws IOException {
        if (frame < 0) {
            frame = 0;
        }
        setCurrentFrame(frame);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
