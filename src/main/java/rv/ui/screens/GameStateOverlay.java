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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Font;
import java.util.Locale;
import jsgl.jogl.view.Viewport;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.ServerSpeedBenchmarker;

public class GameStateOverlay extends ScreenBase
{
	private class GameStateBar
	{
		private static final int BAR_HEIGHT = 24;
		private static final int NAME_WIDTH = 220;
		private static final int TIME_WIDTH = 78;
		private static final int TIME_PAD = 11;
		private static final int SCORE_BOX_WIDTH = 56;
		private static final int Y_PAD = 4;
		private static final int PLAYMODE_WIDTH = 2 * NAME_WIDTH + SCORE_BOX_WIDTH + TIME_WIDTH + 6;

		private final TextRenderer tr1;
		private final TextRenderer tr2;
		private final TextRenderer tr3;

		private final int x;

		private int y;

		private boolean showServerSpeed = true;

		public GameStateBar(int x, int y)
		{
			this.x = x;
			this.y = y;
			tr1 = new TextRenderer(new Font("Arial", Font.PLAIN, 22), true, false);
			tr2 = new TextRenderer(new Font("Arial", Font.PLAIN, 16), true, false);
			tr3 = new TextRenderer(new Font("Arial", Font.BOLD, 16), true, false);
		}

		void toggleShowServerSpeed()
		{
			showServerSpeed = !showServerSpeed;
		}

		void setShowServerSpeed(boolean showServerSpeed)
		{
			this.showServerSpeed = showServerSpeed;
		}

		void render(GL2 gl, GameState gs, int screenW, int screenH)
		{
			String teamL = gs.getUIStringTeamLeft();
			String teamR = gs.getUIStringTeamRight();

			String scoreText = gs.getScoreLeft() + ":" + gs.getScoreRight();

			int minutes = (int) Math.floor(gs.getTime() / 60.0);
			int seconds = (int) (gs.getTime() - minutes * 60);
			String timeText = String.format(Locale.US, "%02d:%02d", minutes, seconds);

			// truncate team names that are too long to fit within bounds
			while (tr1.getBounds(teamL).getWidth() > NAME_WIDTH - 4)
				teamL = teamL.substring(0, teamL.length() - 1);
			while (tr1.getBounds(teamR).getWidth() > NAME_WIDTH - 4)
				teamR = teamR.substring(0, teamR.length() - 1);

			double lxpad = (NAME_WIDTH - tr1.getBounds(teamL).getWidth()) / 2;
			double rxpad = (NAME_WIDTH - tr1.getBounds(teamR).getWidth()) / 2;
			double sxpad = (SCORE_BOX_WIDTH - tr1.getBounds(scoreText).getWidth()) / 2;

			drawGradientBar(gl, x - 3, y - 24, PLAYMODE_WIDTH, 24, 0.5f, new float[] {0, 0, 0, 0.5f},
					new float[] {0, 0, 0, 0}, false);

			gl.glBegin(GL2.GL_QUADS);
			gl.glColor4f(0, 0, 0, 0.5f);
			drawBox(gl, x - 3, y - 3, 2 * NAME_WIDTH + SCORE_BOX_WIDTH + TIME_WIDTH + 6, BAR_HEIGHT + 6);
			drawBox(gl, x - 3, y - 3, 2 * NAME_WIDTH + SCORE_BOX_WIDTH + TIME_WIDTH + 6, BAR_HEIGHT * 0.6f);
			float[] lc = Renderer.world.getLeftTeam().getColorMaterial().getDiffuse();
			gl.glColor4f(lc[0] * 0.8f, lc[1] * 0.8f, lc[2] * 0.8f, 0.65f);
			drawBox(gl, x, y, NAME_WIDTH, BAR_HEIGHT);
			gl.glColor4f(0.2f, 0.2f, 0.2f, 0.65f);
			drawBox(gl, x + NAME_WIDTH, y, SCORE_BOX_WIDTH, BAR_HEIGHT);
			gl.glColor4f(1, .3f, .3f, 0.65f);
			float[] rc = Renderer.world.getRightTeam().getColorMaterial().getDiffuse();
			gl.glColor4f(rc[0] * 0.8f, rc[1] * 0.8f, rc[2] * 0.8f, 0.65f);
			drawBox(gl, x + NAME_WIDTH + SCORE_BOX_WIDTH, y, NAME_WIDTH, BAR_HEIGHT);
			gl.glEnd();

			int timeX = x + 2 * NAME_WIDTH + SCORE_BOX_WIDTH + TIME_PAD;

			tr1.beginRendering(screenW, screenH);
			tr1.draw(teamL, (int) (x + lxpad), y + Y_PAD);
			tr1.draw(scoreText, (int) (x + NAME_WIDTH + sxpad), y + Y_PAD);
			tr1.draw(teamR, (int) (x + NAME_WIDTH + SCORE_BOX_WIDTH + rxpad), y + Y_PAD);
			tr1.draw(timeText, timeX, y + Y_PAD);
			tr1.endRendering();

			tr2.setColor(0.9f, 0.9f, 0.9f, 1);
			tr2.beginRendering(screenW, screenH);
			tr2.draw("Playmode: " + gs.getPlayMode(), x, y - 20);
			if (showServerSpeed && ssb != null) {
				tr2.draw("Server Speed: " + ssb.getServerSpeed(), x + NAME_WIDTH + SCORE_BOX_WIDTH, y - 20);
			}
			tr2.endRendering();

			float passModeWaitToScoreTime = 0;
			boolean leftTeamPassModeWaitToScore = false;

			if (gs.getPassModeValuesReported()) {
				if (gs.getPassModeScoreWaitLeft() > 0) {
					passModeWaitToScoreTime = gs.getPassModeScoreWaitLeft();
					leftTeamPassModeWaitToScore = true;
				} else if (gs.getPassModeScoreWaitRight() > 0) {
					passModeWaitToScoreTime = gs.getPassModeScoreWaitRight();
				}
			} else if (gs.getPlayModeHistory().size() > 1 && gs.getPlayMode().equals(GameState.PLAY_ON)) {
				GameState.HistoryItem item = gs.getPlayModeHistory().get(gs.getPlayModeHistory().size() - 2);
				if (item.playMode.equals(GameState.PASS_LEFT) || item.playMode.equals(GameState.PASS_RIGHT)) {
					float timeOfLastPassEnd = gs.getPlayModeHistory().get(gs.getPlayModeHistory().size() - 1).time;
					float timePassed = gs.getTime() - timeOfLastPassEnd;
					if (timePassed >= 0) {
						final float COOLDOWN = 10;
						passModeWaitToScoreTime = COOLDOWN - timePassed;
						if (item.playMode.equals(GameState.PASS_LEFT)) {
							leftTeamPassModeWaitToScore = true;
						}
					}
				}
			}

			if (passModeWaitToScoreTime > 0) {
				tr3.beginRendering(screenW, screenH);
				float a = 0.8f;
				if (leftTeamPassModeWaitToScore) {
					tr3.setColor(lc[0] * a, lc[1] * a, lc[2] * a, 1);
				} else {
					tr3.setColor(rc[0] * a, rc[1] * a, rc[2] * a, 1);
				}
				tr3.draw(String.format(Locale.US, "%.1f", passModeWaitToScoreTime), timeX, y - 20);
				tr3.endRendering();
			}
		}
	}

	private final GameStateBar gsBar;
	private ServerSpeedBenchmarker ssb = null;

	public GameStateOverlay()
	{
		gsBar = new GameStateBar(20, 20);
	}

	/**
	 * Draws a rectangle with a single linear gradient
	 *
	 * @param x
	 *            - left coordinate of rectangle
	 * @param y
	 *            - bottom coordinate of rectangle
	 * @param w
	 *            - width in pixels
	 * @param h
	 *            - height in pixels
	 * @param gStart
	 *            - percentage of bar width to start gradient at (0 - 1)
	 */
	void drawGradientBar(GL2 gl, float x, float y, float w, float h, float gStart, float[] startColor, float[] endColor,
			boolean flipVertical)
	{
		gStart = 1 - gStart;
		float gx = w * gStart;

		float[][] v = {{x, y}, {x + gx, y}, {x + w, y}, {x + w, y + h}, {x + gx, y + h}, {x, y + h}};

		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4fv(startColor, 0);
		if (flipVertical) {
			gl.glVertex2fv(v[4], 0);
			gl.glVertex2fv(v[1], 0);
			gl.glVertex2fv(v[2], 0);
			gl.glVertex2fv(v[3], 0);
			gl.glVertex2fv(v[1], 0);
			gl.glVertex2fv(v[4], 0);
			gl.glColor4fv(endColor, 0);
			gl.glVertex2fv(v[5], 0);
			gl.glVertex2fv(v[0], 0);
		} else {
			gl.glVertex2fv(v[5], 0);
			gl.glVertex2fv(v[0], 0);
			gl.glVertex2fv(v[1], 0);
			gl.glVertex2fv(v[4], 0);
			gl.glVertex2fv(v[4], 0);
			gl.glVertex2fv(v[1], 0);
			gl.glColor4fv(endColor, 0);
			gl.glVertex2fv(v[2], 0);
			gl.glVertex2fv(v[3], 0);
		}
		gl.glEnd();
	}

	@Override
	public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp)
	{
		gsBar.y = vp.h - GameStateBar.BAR_HEIGHT - 20;
		gsBar.render(gl, Renderer.world.getGameState(), vp.w, vp.h);
	}

	public void toggleShowServerSpeed()
	{
		gsBar.toggleShowServerSpeed();
	}

	public void setShowServerSpeed(boolean showServerSpeed)
	{
		gsBar.setShowServerSpeed(showServerSpeed);
	}

	public void addServerSpeedBenchmarker(ServerSpeedBenchmarker benchmarker)
	{
		ssb = benchmarker;
	}

	static void drawBox(GL2 gl, float x, float y, float w, float h)
	{
		gl.glVertex2f(x, y);
		gl.glVertex2f(x + w, y);
		gl.glVertex2f(x + w, y + h);
		gl.glVertex2f(x, y + h);
	}
}
