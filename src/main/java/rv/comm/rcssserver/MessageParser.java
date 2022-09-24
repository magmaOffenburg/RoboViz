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

import java.text.ParseException;
import java.util.ArrayList;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.SceneGraphHeader;
import rv.world.WorldModel;

/**
 * Reads a message from server, converts it into s-expressions, determines the type of message from
 * the structure of the expressions, then parses the expressions to update the world state
 *
 * @author Justin Stoecker
 */
public class MessageParser
{
	private WorldModel world;

	public MessageParser(WorldModel world)
	{
		this.world = world;
	}

	public void setWorldModel(WorldModel world)
	{
		this.world = world;
	}

	public void parse(String message) throws ParseException
	{
		synchronized (world) {
			ArrayList<SExp> expressions = SExp.parse(message);

			world.getGameState().parse(expressions.get(0), world);
			SceneGraphHeader header = SceneGraphHeader.parse(expressions.get(1));
			if (header.getType().equals(SceneGraphHeader.FULL)) {
				// scene graph structure has changed, so replace the old one and tell
				// any objects that rely on the scene graph to update their references
				SceneGraph sg = new SceneGraph(expressions.get(2));
				world.setSceneGraph(sg);
			} else {
				world.getSceneGraph().update(expressions.get(2));
			}
		}
	}
}
