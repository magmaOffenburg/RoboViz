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
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import js.math.Maths;

/**
 * Log file reader that provides random access to non zipped logfiles.
 * 
 * @author justin
 */
public class LogfileReaderRandomAccess implements ILogfileReader {

    // position in bytes of the start of each line (server message) in the log
    private List<Long>       framePositions;

    // used for frame-by-frame access when stepping forward or backward
    private RandomAccessFile raf;

    /** the file to read from */
    private File             logsrc;

    // frame that is referenced by the buffered reader
    private int              brFramePtr;

    /** stores the server message at the current frame position */
    private String           curFrameMsg;

    /** the number of frames in the logfile */
    private int              numFrames;

    /**
     * Default constructor
     * 
     * @param decoratee
     *            the reader that we decorate
     * @throws Exception
     *             in case the logfile could not be opened
     */
    public LogfileReaderRandomAccess(File file) throws Exception {
        framePositions = null;
        raf = new RandomAccessFile(file, "r");
        brFramePtr = 0;
        curFrameMsg = null;
        open();
    }

    /**
     * Opens the file for buffered reading
     * 
     * @throws FileNotFoundException
     */
    private void open() throws IOException {
        if (framePositions == null) {
            framePositions = new ArrayList<Long>(500);
            // read file to cache
            BufferedReader br = new BufferedReader(new FileReader(logsrc));
            long lastMark = 0;
            String line;
            while ((line = br.readLine()) != null) {
                framePositions.add(new Long(lastMark));
                // +1 for newline character
                lastMark += line.length() + 1;
            }
        }
        raf = new RandomAccessFile(logsrc, "r");
        curFrameMsg = raf.readLine();
        brFramePtr = 0;
    }

    @Override
    public boolean isValid() {
        return raf != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#isAtEndOfLog()
     */
    @Override
    public boolean isAtEndOfLog() {
        return brFramePtr == numFrames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getNumFrames()
     */
    @Override
    public int getNumFrames() {
        return numFrames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getCurrentFrame()
     */
    @Override
    public int getCurrentFrame() {
        return brFramePtr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getCurrrentFrameMessage()
     */
    @Override
    public String getCurrrentFrameMessage() {
        return curFrameMsg;
    }

    private String setCurrentFrame(int frame) throws IOException {
        // avoid seeking if frame hasn't changed
        if (frame == brFramePtr) {
            return curFrameMsg;
        }
        frame = Maths.clamp(frame, 0, numFrames - 1);
        raf.seek(framePositions.get(frame).longValue());
        curFrameMsg = raf.readLine();
        return curFrameMsg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#rewind()
     */
    @Override
    public void rewind() throws IOException {
        close();
        open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#close()
     */
    @Override
    public void close() {
        try {
            raf.close();
        } catch (Exception ex) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepForward()
     */
    @Override
    public String stepForward() throws IOException {
        if (isAtEndOfLog())
            return curFrameMsg;

        curFrameMsg = raf.readLine();
        brFramePtr++;
        return curFrameMsg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepBackward()
     */
    @Override
    public void stepBackward() throws IOException {
        setCurrentFrame(brFramePtr - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepAnywhere(int)
     */
    @Override
    public void stepAnywhere(int frame) throws IOException {
        setCurrentFrame(frame);
    }
}
