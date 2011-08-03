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
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import js.math.Maths;

/**
 * Abstraction for a log that can be viewed frame by frame
 * 
 * @author justin
 */
public class Logfile {

    // position in bytes of the start of each line (server message) in the log
    private List<Long>       framePositions = new ArrayList<Long>();

    // used for frame-by-frame access when stepping forward or backward
    private RandomAccessFile raf;

    // used for sequentially playing frames
    private BufferedReader   br;

    private File             logsrc;

    // frame referenced by the random access reader
    private int              curFramePtr    = 0;

    // frame that is referenced by the buffered reader
    private int              brFramePtr     = 0;

    private int              numFrames      = 0;

    // file input streams are assumed to be open
    private boolean          open           = false;

    private boolean          atEndOfLog     = false;

    // stores the server message at the current frame position
    private String           curFrameMsg    = null;

    public boolean isOpen() {
        return open;
    }

    public boolean isAtEndOfLog() {
        return atEndOfLog;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public int getCurrentFrame() {
        return curFramePtr;
    }

    public String getCurrrentFrameMessage() {
        return curFrameMsg;
    }

    /**
     * Changes the current frame using random access file. The contents of the current frame will be
     * updated.
     */
    public void setCurrentFrame(int frame) throws IOException {
        int prevFrame = curFramePtr;
        this.curFramePtr = Maths.clamp(frame, 0, numFrames - 1);

        // avoid seeking if frame hasn't changed
        if (prevFrame == curFramePtr)
            return;

        raf.seek(framePositions.get(curFramePtr).longValue());
        curFrameMsg = raf.readLine();
        atEndOfLog = (curFramePtr == numFrames - 1);
    }

    public Logfile(File file) throws Exception {
        this.logsrc = file;

        // count number of frames and save their positions in the file
        long lastMark = 0;
        String line;
        BufferedReader br = new BufferedReader(new FileReader(logsrc));
        while ((line = br.readLine()) != null) {
            numFrames++;
            framePositions.add(new Long(lastMark));
            lastMark += line.length() + 1; // +1 for newline character
        }
        br.close();
    }

    /**
     * Opens the logfile for reading. If it is already opened, the method call is ignored. The
     * current frame is reset to the start of the log.
     * 
     * @throws IOException
     */
    public void open() throws IOException {
        if (open)
            return;

        br = new BufferedReader(new FileReader(logsrc));
        raf = new RandomAccessFile(logsrc, "r");
        open = true;
        curFramePtr = 0;
        brFramePtr = 0;
        atEndOfLog = (curFramePtr == numFrames);
    }

    /**
     * Closes any open file streams. If the logfile is already closed, this method is ignored. While
     * closed, the logfile frames cannot be read.
     */
    public void close() {
        if (!open)
            return;
        try {
            br.close();
            raf.close();
            open = false;
        } catch (Exception ex) {
        }
    }

    /**
     * The user may manually switch to a random frame, which is then read from the random access
     * file. This method ensures reading from the buffered reader (for fast sequential playback)
     * continues from the current frame selected in the random access file.
     * 
     * @throws IOException
     */
    private void sync() throws IOException {
        if (br != null)
            br.close();
        br = new BufferedReader(new FileReader(logsrc));
        br.skip(framePositions.get(curFramePtr).longValue());
        brFramePtr = curFramePtr;
    }

    /**
     * Moves the current frame ahead by one frame.
     */
    public void stepForward() throws IOException, ParseException {
        if (atEndOfLog)
            return;

        if (curFramePtr != brFramePtr)
            sync();

        curFrameMsg = br.readLine();

        brFramePtr++;
        curFramePtr++;
        atEndOfLog = (curFramePtr == numFrames - 1);
    }

    /**
     * Moves the current frame back by one frame.
     */
    public void stepBackward() throws IOException, ParseException {
        // if current frame is at k, then the k-1 frame is visible; set current
        // to k-2, buffer the frame, then set current to k-1
        setCurrentFrame(curFramePtr - 2);
        curFramePtr++;
    }

    /**
     * Moves the current frame back by one frame.
     */
    public void stepAnywhere(int frame) throws IOException, ParseException {
        setCurrentFrame(frame);
        curFramePtr = frame + 1;
    }
}
