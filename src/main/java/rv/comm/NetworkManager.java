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

package rv.comm;

import java.net.SocketException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.Main;
import org.magmaoffenburg.roboviz.configuration.Config.Networking;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.drawing.DrawComm;
import rv.comm.rcssserver.ServerComm;

/**
 * Central hub for handling network communication
 *
 * @author Justin Stoecker
 */
public class NetworkManager
{
	private static final Logger LOGGER = LogManager.getLogger();

	private DrawComm agentComm = null;
	private ServerComm serverComm;

	public DrawComm getAgentComm()
	{
		return agentComm;
	}

	public ServerComm getServer()
	{
		return serverComm;
	}

	public void init()
	{
		try {
			agentComm = new DrawComm(Networking.INSTANCE.getListenPort());
		} catch (SocketException e) {
			LOGGER.error("Unable to open draw communation", e);
		}
		serverComm = new ServerComm(Renderer.Companion.getWorld(), Main.Companion.getMode());
		if (agentComm != null) {
			agentComm.addListener(serverComm);
		}
	}

	public void shutdown()
	{
		if (agentComm != null) {
			agentComm.shutdown();
			agentComm.removeListener(serverComm);
			agentComm = null;
		}

		if (serverComm != null) {
			serverComm.disconnect();
		}
	}
}