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
import jsgl.math.vector.Vec3f;
import rv.comm.drawing.commands.Command;
import rv.world.WorldModel;
import rv.world.objects.Agent;

/**
 * This is a special annotation type that is attached to an agent.
 */
public class AgentAnnotation extends Annotation
{
	private static final Vec3f OFFSET = new Vec3f(0, 0.7f, 0);
	private final Agent agent;

	@Override
	public float[] getPos()
	{
		if (agent.getPosition() == null)
			return new float[] {0, 0, 0};
		return agent.getPosition().plus(OFFSET).getVals();
	}

	public AgentAnnotation(String text, Agent agent, float[] color)
	{
		super(text, agent.getHeadCenter().getVals(), color, agent.getShortName() + ".Annotation");
		this.agent = agent;
		agent.setAnnotation(this);
	}

	public static AgentAnnotation parse(ByteBuffer buf, WorldModel world)
	{
		Agent agent = Command.readAgent(buf, world);
		float[] color = Command.readRGB(buf);
		String text = Command.getString(buf);

		if (agent == null || agent.getHeadCenter() == null)
			return null;
		return new AgentAnnotation(text, agent, color);
	}
}
