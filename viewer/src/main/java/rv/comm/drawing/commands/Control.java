package rv.comm.drawing.commands;

import java.nio.ByteBuffer;
import jsgl.io.ByteUtil;
import rv.Viewer;
import rv.world.objects.Agent;

public class Control extends Command
{
	public static final int AGENT_SELECT = 0;

	private final Viewer viewer;
	private final Agent agent;

	public Control(ByteBuffer buf, Viewer viewer)
	{
		super();
		this.viewer = viewer;

		int type = ByteUtil.uValue(buf.get());

		switch (type) {
		case AGENT_SELECT:
			agent = Command.readAgent(buf, viewer.getWorldModel());
			break;
		default:
			System.err.println("Unknown control : " + type);
			agent = null;
		}
	}

	@Override
	public void execute()
	{
		if (agent != null) {
			viewer.getWorldModel().setSelectedObject(agent);
		}
	}
}
