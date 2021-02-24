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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Decorator of logfile readers that adds buffering to speed up stepping backwards.
 *
 * @author klaus
 */
public class LogfileReaderBuffered implements ILogfileReader
{
	/** the reader to decorate */
	private final ILogfileReader decoratee;

	/** the maximal size of the buffer */
	private final int bufferSize;

	/** buffer for the frame messages */
	private List<String> buffer;

	/** the frame that is buffered at position 0 of the buffer */
	private int bufferZeroFrame;

	/** index of the current Frame */
	private int currentFrame;

	/**
	 * Default constructor
	 *
	 * @param decoratee
	 *            the logfile to open
	 * @param bufferSize
	 *            the number of frames to buffer
	 */
	public LogfileReaderBuffered(ILogfileReader decoratee, int bufferSize)
	{
		this.decoratee = decoratee;
		this.bufferSize = bufferSize;
		open();
	}

	/**
	 * Opens the file for buffered reading
	 */
	private void open()
	{
		buffer = new LinkedList<>();
		buffer.add(decoratee.getCurrentFrameMessage());
		bufferZeroFrame = 0;
		currentFrame = 0;
	}

	@Override
	public boolean isValid()
	{
		return decoratee != null && decoratee.isValid();
	}

	@Override
	public boolean isAtBeginningOfLog()
	{
		return currentFrame == 0;
	}

	@Override
	public boolean isAtEndOfLog()
	{
		return decoratee.isAtEndOfLog() && getBufferIndex(currentFrame) == buffer.size() - 1;
	}

	@Override
	public void setNumFrames(int numFrames)
	{
		decoratee.setNumFrames(numFrames);
	}

	@Override
	public int getNumFrames()
	{
		return decoratee.getNumFrames();
	}

	@Override
	public int getCurrentFrame()
	{
		return currentFrame;
	}

	@Override
	public String getCurrentFrameMessage()
	{
		return buffer.get(getBufferIndex(currentFrame));
	}

	private String setCurrentFrame(int frame) throws IOException
	{
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
		return getCurrentFrameMessage();
	}

	@Override
	public void rewind() throws IOException
	{
		decoratee.rewind();
		open();
	}

	@Override
	public void close()
	{
		decoratee.close();
		buffer = null;
	}

	@Override
	public String stepForward() throws IOException
	{
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
			}
		} else {
			// we are inside the buffer
			currentFrame++;
		}
		return getCurrentFrameMessage();
	}

	@Override
	public void stepBackward() throws IOException
	{
		if (currentFrame > 0) {
			setCurrentFrame(currentFrame - 1);
		}
	}

	@Override
	public void stepAnywhere(int frame) throws IOException
	{
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
	private int getBufferIndex(int frame)
	{
		return frame - bufferZeroFrame;
	}

	@Override
	public File getFile()
	{
		return decoratee.getFile();
	}

	@Override
	public void addListener(LogfileListener l)
	{
		decoratee.addListener(l);
	}

	@Override
	public void removeListener(LogfileListener l)
	{
		decoratee.removeListener(l);
	}
}
