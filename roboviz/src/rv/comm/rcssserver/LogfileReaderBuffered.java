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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Decorator of logfile readers that adds buffering to speed up stepping backwards.
 * 
 * @author klaus
 */
public class LogfileReaderBuffered implements ILogfileReader {

    /** the reader to decorate */
    private ILogfileReader decoratee;

    /** the maximal size of the buffer */
    private int            bufferSize;

    /** buffer for the frame messages */
    private List<String>   buffer;

    /** the frame that is buffered at position 0 of the buffer */
    private int            bufferZeroFrame;

    /** index of the current Frame */
    private int            currentFrame;

    /**
     * Default constructor
     * 
     * @param file
     *            the logfile to open
     * @param bufferSize
     *            the number of frames to buffer
     * @throws FileNotFoundException
     *             if the logfile can not be opened
     */
    public LogfileReaderBuffered(ILogfileReader decoratee, int bufferSize)
            throws FileNotFoundException {
        this.decoratee = decoratee;
        this.bufferSize = bufferSize;
        open();
    }

    /**
     * Opens the file for buffered reading
     * 
     * @throws FileNotFoundException
     */
    private void open() throws FileNotFoundException {
        buffer = new LinkedList<String>();
        buffer.add(decoratee.getCurrrentFrameMessage());
        bufferZeroFrame = 0;
        currentFrame = 0;
    }

    @Override
    public boolean isValid() {
        return decoratee != null && decoratee.isValid();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#isAtEndOfLog()
     */
    @Override
    public boolean isAtEndOfLog() {
        return decoratee.isAtEndOfLog() && getBufferIndex(currentFrame) == buffer.size() - 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getNumFrames()
     */
    @Override
    public int getNumFrames() {
        return decoratee.getNumFrames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getCurrentFrame()
     */
    @Override
    public int getCurrentFrame() {
        return currentFrame;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#getCurrrentFrameMessage()
     */
    @Override
    public String getCurrrentFrameMessage() {
        return buffer.get(getBufferIndex(currentFrame));
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#setCurrentFrame(int)
     */
    private String setCurrentFrame(int frame) throws IOException {

        int bufferIndex = getBufferIndex(frame);
        if (bufferIndex < 0) {
            // outside buffer left: fill buffer starting with beginning of file
            rewind();
            while (currentFrame < frame) {
                stepForward();
            }

        } else if (bufferIndex >= buffer.size()) {
            // outside buffer right: fill buffer continuing from current position
            currentFrame = bufferZeroFrame + buffer.size() - 1;
            while (currentFrame < frame && !isAtEndOfLog()) {
                stepForward();
            }

        } else {
            // we are still inside the buffer
            currentFrame = frame;
        }
        return getCurrrentFrameMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#rewind()
     */
    @Override
    public void rewind() throws IOException {
        decoratee.rewind();
        open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#close()
     */
    @Override
    public void close() {
        decoratee.close();
        buffer = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepForward()
     */
    @Override
    public String stepForward() throws IOException {
        if (getBufferIndex(currentFrame) == buffer.size() - 1) {
            // stepping outside buffer right side
            String line = decoratee.stepForward();
            if (line != null) {
                buffer.add(line);
                if (buffer.size() > bufferSize) {
                    // before exceeding specified size we remove oldest entry
                    buffer.remove(0);
                    bufferZeroFrame++;
                }
                currentFrame++;
                System.out.println("Reading frame: " + currentFrame);
            }
        } else {
            // we are inside the buffer
            currentFrame++;
        }
        return getCurrrentFrameMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepBackward()
     */
    @Override
    public void stepBackward() throws IOException {
        if (currentFrame > 0) {
            setCurrentFrame(currentFrame - 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rv.comm.rcssserver.ILogfileReader#stepAnywhere(int)
     */
    @Override
    public void stepAnywhere(int frame) throws IOException {
        if (frame < 0) {
            frame = 0;
        }
        setCurrentFrame(frame);
    }

    /**
     * @param frame
     *            index of the frame to retrieve
     * @return the index in the buffer for the passed frame index
     */
    private int getBufferIndex(int frame) {
        return frame - bufferZeroFrame;
    }
}
