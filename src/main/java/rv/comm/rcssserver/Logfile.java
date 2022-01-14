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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rv.comm.drawing.commands.Command;

/**
 * Abstraction for a log that can be viewed frame by frame. Supports unpacked, single file zipped
 * and tar.bz2 files.
 *
 * @author justin
 */
public class Logfile implements ILogfileReader
{
	private static final Logger LOGGER = LogManager.getLogger();

	/** used for sequentially playing frames */
	private BufferedReader br;

	/** the file to read from */
	private final File logsrc;

	/** index of the frame that is currently buffered */
	private int curFramePtr;

	/** the number of frames in the logfile, initially estimated */
	private int numFrames;

	/** stores the server message at the current frame position */
	private String curFrameMsg;

	/** if we should execute draw commands */
	private final boolean execDrawCmds;

	private final List<LogfileListener> listeners = new ArrayList<>();

	/**
	 * Default constructor
	 *
	 * @param file
	 *            the logfile to open
	 * @param execDrawCmds
	 *            if draw commands should be executed
	 * @throws Exception
	 *             if the logfile can not be opened
	 */
	public Logfile(File file, boolean execDrawCmds) throws Exception
	{
		this.logsrc = file;
		this.execDrawCmds = execDrawCmds;
		numFrames = 1700;
		open();
	}

	/**
	 * Opens the file for buffered reading
	 */
	private void open() throws IOException
	{
		br = TarBz2ZipUtil.createBufferedReader(logsrc);
		if (br != null) {
			curFrameMsg = br.readLine();
			if (curFrameMsg != null && curFrameMsg.startsWith("[")) {
				curFrameMsg = processDrawCmds(curFrameMsg);
			}
		}
		curFramePtr = 0;
	}

	@Override
	public boolean isValid()
	{
		return br != null;
	}

	@Override
	public boolean isAtBeginningOfLog()
	{
		return curFramePtr == 0;
	}

	@Override
	public boolean isAtEndOfLog()
	{
		return curFrameMsg == null;
	}

	@Override
	public void setNumFrames(int numFrames)
	{
		this.numFrames = numFrames;
	}

	@Override
	public int getNumFrames()
	{
		return numFrames;
	}

	@Override
	public int getCurrentFrame()
	{
		return curFramePtr;
	}

	@Override
	public String getCurrentFrameMessage()
	{
		return curFrameMsg;
	}

	private String setCurrentFrame(int frame) throws IOException
	{
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
	public void rewind() throws IOException
	{
		close();
		open();
	}

	@Override
	public void close()
	{
		try {
			br.close();
		} catch (Exception ex) {
		}
	}

	@Override
	public String stepForward() throws IOException
	{
		if (isAtEndOfLog())
			return null;

		curFrameMsg = br.readLine();
		if (curFrameMsg != null && curFrameMsg.startsWith("[")) {
			curFrameMsg = processDrawCmds(curFrameMsg);
		}
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
	public void stepBackward() throws IOException
	{
		if (curFramePtr > 0) {
			setCurrentFrame(curFramePtr - 1);
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

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		close();
	}

	@Override
	public File getFile()
	{
		return logsrc;
	}

	@Override
	public void addListener(LogfileListener l)
	{
		listeners.add(l);
	}

	@Override
	public void removeListener(LogfileListener l)
	{
		listeners.remove(l);
	}

	public String processDrawCmds(String line)
	{
		if (line == null) {
			return null;
		}

		while (line.startsWith("[")) {
			for (LogfileListener l : listeners)
				l.haveDrawCmds();

			int endIndex = line.indexOf("]");
			if (endIndex == -1) {
				break;
			}

			if (execDrawCmds) {
				String drawCmd = line.substring(0, endIndex);
				String[] drawCmdByteValues = drawCmd.substring(1).split(",");
				byte[] drawCmdBytes = new byte[drawCmdByteValues.length];
				for (int i = 0; i < drawCmdBytes.length; i++) {
					try {
						drawCmdBytes[i] = Byte.parseByte(drawCmdByteValues[i].trim());
					} catch (Exception e) {
						LOGGER.error("Error parsing byte of draw command", e);
					}
				}
				ByteBuffer buf = ByteBuffer.wrap(drawCmdBytes);

				while (buf.hasRemaining()) {
					Command cmd = null;
					try {
						cmd = Command.parse(buf);
						if (cmd != null) {
							cmd.execute();
						}
					} catch (Exception e) {
						LOGGER.error("Error while executing draw command", e);
					}
				}
			}
			line = line.substring(endIndex + 1);
		}

		return line;
	}
}
