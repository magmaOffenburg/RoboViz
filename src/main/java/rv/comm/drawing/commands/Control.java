package rv.comm.drawing.commands;

import java.nio.ByteBuffer;
import jsgl.io.ByteUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.world.objects.Agent;

public class Control extends Command
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static final int AGENT_SELECT = 0;

	private final Agent agent;

	public Control(ByteBuffer buf)
	{
		super();

		int type = ByteUtil.uValue(buf.get());

		switch (type) {
		case AGENT_SELECT:
			agent = Command.readAgent(buf, Renderer.Companion.getWorld());
			break;
		default:
			LOGGER.warn("Unknown control : " + type);
			agent = null;
		}
	}

	@Override
	public void execute()
	{
		if (agent != null) {
			Renderer.Companion.getWorld().setSelectedObject(agent);
		}
	}
}
