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

import java.util.SortedMap;
import java.util.TreeMap;
import rv.comm.rcssserver.GameState.ServerMessageReceivedListener;
import rv.comm.rcssserver.ServerComm.ServerChangeListener;

/**
 * Estimates the speed of the server
 *
 * @author Patrick MacAlpine
 */
public class ServerSpeedBenchmarker implements ServerMessageReceivedListener, ServerChangeListener
{
	private static final boolean USE_NANOS = false;
	private long msgTime;
	private float lastGameTime;

	private float serverSpeed = -1;
	private TreeMap<Long, Float> serverMsgDeltas = new TreeMap<>();
	private float accumulatedServerTime;

	public String getServerSpeed()
	{
		if (serverSpeed < 0) {
			return "---";
		}

		return Math.round(100 * serverSpeed) + "%";
	}

	private void updateServerSpeed(GameState gs)
	{
		final long TIME_WINDOW;
		if (USE_NANOS) {
			TIME_WINDOW = 5000000000L;
		} else {
			TIME_WINDOW = 5000;
		}
		final float DEFAULT_MSG_TIME_DELTA = 0.04f;

		float time = gs.getTime();

		// Add message time info to map
		if (serverMsgDeltas.isEmpty()) {
			serverMsgDeltas.put(msgTime, -1.0f);
			accumulatedServerTime = 0;
		} else {
			long lastMsgTime = serverMsgDeltas.lastKey();

			float serverTimeDelta;
			if (time - lastGameTime > 0) {
				// We have a game time change for the amount of time passed
				serverTimeDelta = time - lastGameTime;
			} else {
				// The game is paused so use DEFAULT_MSG_TIME_DELTA for amount of time passed
				serverTimeDelta = DEFAULT_MSG_TIME_DELTA;
			}

			if (msgTime - lastMsgTime > 0) {
				serverMsgDeltas.put(msgTime, serverTimeDelta + accumulatedServerTime);
				accumulatedServerTime = 0;
			} else {
				// Messages are coming in so fast that they have the same time stamp so just save
				// the time delta to add to the next entry with a new time stamp
				accumulatedServerTime += serverTimeDelta;
			}
		}

		// Remove map entries outside of time window
		SortedMap<Long, Float> oldEntries = serverMsgDeltas.headMap(msgTime - TIME_WINDOW);
		while (!oldEntries.isEmpty()) {
			serverMsgDeltas.remove(oldEntries.firstKey());
		}

		float sumDeltas = 0;

		Float[] deltas = serverMsgDeltas.values().toArray(new Float[0]);
		for (int i = 1; i < deltas.length; i++) {
			float delta = deltas[i];
			if (delta > 0) {
				sumDeltas += delta;
			}
		}

		long timePassed = serverMsgDeltas.lastKey() - serverMsgDeltas.firstKey();

		if (timePassed > 0) {
			if (USE_NANOS) {
				serverSpeed = sumDeltas / (timePassed / 1000000000.0f);
			} else {
				serverSpeed = sumDeltas / (timePassed / 1000.0f);
			}
		} else {
			serverSpeed = -1;
		}
	}

	@Override
	public void gsServerMessageReceived(GameState gs)
	{
		if (USE_NANOS) {
			msgTime = System.nanoTime();
		} else {
			msgTime = System.currentTimeMillis();
		}
		lastGameTime = gs.getTime();
	}

	@Override
	public void gsServerMessageProcessed(GameState gs)
	{
		updateServerSpeed(gs);
	}

	@Override
	public void connectionChanged(ServerComm server)
	{
		if (server.isConnected()) {
			serverMsgDeltas.clear();
		}
	}
}
