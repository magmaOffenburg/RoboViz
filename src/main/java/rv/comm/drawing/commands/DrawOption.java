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

package rv.comm.drawing.commands;

import java.nio.ByteBuffer;
import jsgl.io.ByteUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.drawing.Drawings;

/**
 * Various formatting options for drawing
 *
 * @author Justin Stoecker
 */
public class DrawOption extends Command
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static final int SWAP_BUFFERS = 0;

	private final String setName;
	private final Drawings drawings;

	public DrawOption(ByteBuffer buf)
	{
		this.drawings = Renderer.Companion.getDrawings();

		int type = ByteUtil.uValue(buf.get());

		switch (type) {
		case SWAP_BUFFERS:
			setName = getString(buf);
			break;
		default:
			LOGGER.warn("Unknown draw option : " + type);
			setName = null;
		}
	}

	@Override
	public void execute()
	{
		drawings.swapBuffers(setName);
	}
}