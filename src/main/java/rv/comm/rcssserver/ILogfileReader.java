package rv.comm.rcssserver;

import java.io.File;
import java.io.IOException;

public interface ILogfileReader
{
	interface LogfileListener
	{
		void haveDrawCmds();
	}

	/**
	 * @return true if the reader represents a valid logfile
	 */
	boolean isValid();

	boolean isAtBeginningOfLog();

	boolean isAtEndOfLog();

	void setNumFrames(int numFrames);

	int getNumFrames();

	int getCurrentFrame();

	String getCurrentFrameMessage();

	/**
	 * Switches back to the start of the logfile.
	 */
	void rewind() throws IOException;

	/**
	 * Closes any open file streams. If the logfile is already closed, this method is ignored. While
	 * closed, the logfile frames cannot be read.
	 */
	void close();

	/**
	 * Moves the current frame ahead by one frame.
	 *
	 * @return the line that was read
	 */
	String stepForward() throws IOException;

	/**
	 * Moves the current frame back by one frame.
	 */
	void stepBackward() throws IOException;

	/**
	 * Moves the current frame back by one frame.
	 */
	void stepAnywhere(int frame) throws IOException;

	void addListener(LogfileListener l);

	void removeListener(LogfileListener l);

	File getFile();
}