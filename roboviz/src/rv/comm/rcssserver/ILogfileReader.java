package rv.comm.rcssserver;

import java.io.IOException;

public interface ILogfileReader {

    /**
     * @return true if the reader represents a valid logfile
     */
    public abstract boolean isValid();

    public abstract boolean isAtEndOfLog();

    public abstract int getNumFrames();

    public abstract int getCurrentFrame();

    public abstract String getCurrrentFrameMessage();

    /**
     * Switches back to the start of the logfile.
     * 
     * @throws IOException
     */
    public abstract void rewind() throws IOException;

    /**
     * Closes any open file streams. If the logfile is already closed, this method is ignored. While
     * closed, the logfile frames cannot be read.
     */
    public abstract void close();

    /**
     * Moves the current frame ahead by one frame.
     * 
     * @return the line that was read
     */
    public abstract String stepForward() throws IOException;

    /**
     * Moves the current frame back by one frame.
     */
    public abstract void stepBackward() throws IOException;

    /**
     * Moves the current frame back by one frame.
     */
    public abstract void stepAnywhere(int frame) throws IOException;

}