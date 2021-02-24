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

package rv.ui.screens;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import rv.comm.rcssserver.GameState;
import rv.world.WorldModel;

/**
 * A 2D overlay that displays text on the screen
 *
 * @author justin
 */
public class TextOverlay implements GameState.GameStateChangeListener
{
	private static final int FADE_DURATION = 750;

	private final String text;
	private final WorldModel world;
	private int duration;
	private int x, y;
	private float alpha = 1;
	private int elapsedReal = 0;
	private long lastTimeReal = 0;
	private float startTimeServer = 0;
	private float curTimeServer = 0;
	private boolean expired = false;
	private final float[] color;

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public boolean isExpired()
	{
		return expired;
	}

	public TextOverlay(String text, WorldModel world, int duration)
	{
		this(text, world, duration, new float[] {1, 1, 1, 1});
	}

	public TextOverlay(String text, WorldModel world, int duration, float[] color)
	{
		this.world = world;
		this.duration = duration;
		this.startTimeServer = world.getGameState().getTime();
		curTimeServer = startTimeServer;
		this.text = text;
		this.color = color;
		world.getGameState().addListener(this);
	}

	private void update()
	{
		long curTimeReal = System.currentTimeMillis();
		if (lastTimeReal != 0)
			elapsedReal += (curTimeReal - lastTimeReal);
		lastTimeReal = curTimeReal;

		int elapsedServer = Math.abs((int) ((curTimeServer - startTimeServer) * 1000));

		int elapsed = Math.max(elapsedServer, elapsedReal);
		if (elapsed > duration)
			alpha = color[3] * Math.max(1 - (float) (elapsed - duration) / FADE_DURATION, 0);

		if (alpha == 0) {
			expired = true;
			world.getGameState().removeListener(this);
		}
	}

	private void calcXY(BorderTextRenderer tr, int w, int h)
	{
		Rectangle2D bounds = tr.getBounds(text);
		x = (int) (w - bounds.getWidth()) / 2;
		y = (int) (h - bounds.getHeight()) / 2;
	}

	public void render(BorderTextRenderer tr, int w, int h)
	{
		if (duration > 0)
			update();
		calcXY(tr, w, h);
		Color textColor = new Color(color[0], color[1], color[2], alpha);
		Color shadowColor = new Color(0, 0, 0, alpha);
		tr.drawWithShadow(text, x, y, textColor, shadowColor);
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
	}

	@Override
	public void gsPlayStateChanged(GameState gs)
	{
	}

	@Override
	public void gsTimeChanged(GameState gs)
	{
		curTimeServer = gs.getTime();
	}
}
