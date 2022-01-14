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
import rv.comm.drawing.shapes.Circle;
import rv.comm.drawing.shapes.Line;
import rv.comm.drawing.shapes.Point;
import rv.comm.drawing.shapes.Polygon;
import rv.comm.drawing.shapes.Shape;
import rv.comm.drawing.shapes.Sphere;

/**
 * Parses a draw shape packet and, when executing, adds the shape to the intended agent's list of
 * shapes
 *
 * @author Justin Stoecker
 */
public class DrawShape extends Command
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static final int CIRCLE = 0;
	public static final int LINE = 1;
	public static final int POINT = 2;
	public static final int SPHERE = 3;
	public static final int POLYGON = 4;

	private final Shape shape;
	private final Drawings drawings;

	public DrawShape(ByteBuffer buf)
	{
		this.drawings = Renderer.Companion.getDrawings();

		int type = ByteUtil.uValue(buf.get());

		switch (type) {
		case CIRCLE:
			shape = Circle.parse(buf);
			break;
		case LINE:
			shape = Line.parse(buf);
			break;
		case POINT:
			shape = Point.parse(buf);
			break;
		case SPHERE:
			shape = Sphere.parse(buf);
			break;
		case POLYGON:
			shape = Polygon.parse(buf);
			break;
		default:
			LOGGER.warn("Unknown shape : " + type);
			shape = null;
		}
	}

	@Override
	public void execute()
	{
		drawings.addShape(shape);
	}

	@Override
	public String toString()
	{
		return String.format("DrawShape: %s", shape);
	}
}
