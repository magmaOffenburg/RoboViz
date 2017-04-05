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

package rv.comm.drawing.annotations;

import java.nio.ByteBuffer;
import rv.comm.drawing.commands.Command;

public class StandardAnnotation extends Annotation
{
	public StandardAnnotation(String text, float[] pos, float[] color, String set)
	{
		super(text, pos, color, set);
	}

	public static StandardAnnotation parse(ByteBuffer buf)
	{
		float[] pos = Command.readCoords(buf, 3);
		float[] color = Command.readRGB(buf);
		String text = Command.getString(buf);
		String set = Command.getString(buf);

		return new StandardAnnotation(text, pos, color, set);
	}
}
