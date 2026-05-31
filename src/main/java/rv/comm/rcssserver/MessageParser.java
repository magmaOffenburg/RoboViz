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
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rv.comm.rcssserver.scenegraph.RSMPSceneGraphType;
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

	private static final Logger LOGGER = LogManager.getLogger();

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
			world.getGameState().startParseStep();

			ArrayList<SExp> expressions = SExp.parse(message);

			// Check if legacy mode shall be used
			boolean legacyProtocol = !expressions.get(0).getChildren().get(0).getAtoms()[0].equals("RSMP");

			if (legacyProtocol) {
				world.getGameState().parse(expressions.get(0).getChildren());
				SceneGraphHeader header = SceneGraphHeader.parse(expressions.get(1));
				if (header.getType().equals(SceneGraphHeader.FULL)) {
					// scene graph structure has changed, so replace the old one and tell
					// any objects that rely on the scene graph to update their references
					SceneGraph sg = new SceneGraph(expressions.get(2).getChildren(), false);
					world.setSceneGraph(sg);
				} else {
					world.getSceneGraph().update(expressions.get(2).getChildren());
				}
			} else {
				final var root = expressions.get(0).getChildren();

				// RSMP version check
				var rsmpHeaderAtoms = root.get(0).getAtoms();
				var rsmpVersion = new ProtocolVersion(Arrays.asList(rsmpHeaderAtoms).subList(1, 3));
				if (!rsmpVersion.supports(1, 0)) {
					LOGGER.error("unsupported RSMP version: {}.{}", rsmpHeaderAtoms[1], rsmpHeaderAtoms[2]);
					return;
				}

				for (var protocolComponent : root.subList(1, root.size())) {
					final var header = protocolComponent.getChildren().get(0);
					final var componentName = header.getAtoms()[0];
					final var version = new ProtocolVersion(Arrays.asList(header.getAtoms()).subList(1, 3));
					final var componentContent =
							protocolComponent.getChildren().subList(1, protocolComponent.getChildren().size());
					switch (componentName) {
					case "gt":
						world.updateGlobalTime(protocolComponent.getAtoms(), version);
						break;
					case "sg":
						if (!version.supports(1, 0)) {
							LOGGER.error("unsupported scene graph version: {}", version);
							continue;
						}
						final var sceneGraphType = protocolComponent.getAtoms()[0];
						switch (sceneGraphType) {
						case RSMPSceneGraphType.FULL:
							final var sg = new SceneGraph(componentContent, true);
							world.setSceneGraph(sg);
							break;
						case RSMPSceneGraphType.DIFF:
							if (world.getSceneGraph() != null)
								world.getSceneGraph().update(componentContent);
							break;
						default:
							LOGGER.error("unsupported scene graph type: {}", protocolComponent.getAtoms()[0]);
							continue;
						}
						break;
					case "ge":
						world.getGameState().parse(componentContent, version);
						break;
					case "gs":
						world.getGameState().parse(componentContent, version);
						break;
					}
				}
			}
			world.getGameState().finishParseStep();
		}
	}
}
