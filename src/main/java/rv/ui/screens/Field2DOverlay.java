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
import com.jogamp.opengl.util.gl2.GLUT;
import jsgl.jogl.view.Viewport;
import jsgl.math.vector.Vec3f;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.world.Team;
import rv.world.WorldModel;

/**
 * Displays player positions from a 2D top-down view of field
 *
 * @author justin
 */
public class Field2DOverlay extends ScreenBase implements GameStateChangeListener
{
	private final WorldModel world;

	private float fieldWidth = 180;
	private float fieldLength = 120;
	private int screenWidth = 1;
	private int screenHeight = 1;
	private int yPos = 10;

	public void setyPos(int yPos)
	{
		this.yPos = yPos;
	}

	public Field2DOverlay(WorldModel world)
	{
		this.world = world;
		world.getGameState().addListener(this);
	}

	private void setView(GL2 gl, GLU glu)
	{
		float hfw = fieldWidth / 2;
		float hfl = fieldLength / 2;

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrtho(-hfl, hfl, -hfw, hfw, 1, 5);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluLookAt(0, 4, 0, 0, 0, 0, 0, 0, 1);

		int displayWidth = (int) (screenWidth * 0.3f);
		int displayHeight = (int) (displayWidth * fieldWidth / fieldLength);
		gl.glViewport(10, yPos, displayWidth, displayHeight);
	}

	private void unsetView(GL2 gl, Viewport vp)
	{
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix();
		vp.apply(gl);
	}

	private void drawPoints(GL2 gl, int pSize, boolean manualColor)
	{
		gl.glPointSize(pSize);
		gl.glBegin(GL2.GL_POINTS);
		drawTeam(gl, manualColor, world.getRightTeam());
		drawTeam(gl, manualColor, world.getLeftTeam());
		gl.glEnd();

		gl.glPointSize(pSize * 0.5f);
		gl.glBegin(GL2.GL_POINTS);
		Vec3f p = world.getBall().getPosition();
		if (p != null) {
			if (!manualColor)
				gl.glColor3f(1, 1, 1);
			gl.glVertex3f(p.x, p.y, p.z);
		}
		gl.glEnd();
	}

	private void drawTeam(GL2 gl, boolean manualColor, Team team)
	{
		if (!manualColor)
			gl.glColor3fv(team.getColorMaterial().getDiffuse(), 0);
		for (int i = 0; i < team.getAgents().size(); i++) {
			Vec3f p = team.getAgents().get(i).getPosition();
			if (p != null) {
				gl.glVertex3f(p.x, p.y, p.z);
			}
		}
	}

	@Override
	public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp)
	{
		if (world.getField().getModel().isLoaded() && visible) {
			screenWidth = vp.w;
			screenHeight = vp.h;

			gl.glColor4f(1, 1, 1, 0.1f);
			setView(gl, glu);
			world.getField().render(gl);

			int pSize = (int) (screenWidth * 0.01125);

			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glColor4f(0, 0, 0, 0.5f);
			drawPoints(gl, pSize, true);
			drawPoints(gl, pSize - 2, false);
			gl.glDisable(GL2.GL_POINT_SMOOTH);

			unsetView(gl, vp);
		}
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
		fieldWidth = gs.getFieldWidth();
		fieldLength = gs.getFieldLength();
	}

	@Override
	public void gsPlayStateChanged(GameState gs)
	{
	}

	@Override
	public void gsTimeChanged(GameState gs)
	{
	}
}