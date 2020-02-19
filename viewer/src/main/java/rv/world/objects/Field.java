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

package rv.world.objects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import jsgl.jogl.GLDisposable;
import jsgl.jogl.Texture2D;
import jsgl.jogl.light.Material;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.content.ContentManager;
import rv.content.Model;
import rv.world.ModelObject;

/**
 * Soccer field. Consists of a textured quad for the grass and field lines drawn as independent
 * shapes.
 *
 * @author Justin Stoecker
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class Field extends ModelObject implements GameStateChangeListener, GLDisposable
{
	public static final Matrix DEFAULT_MODEL_MATRIX =
			new Matrix(new double[] {-1.5, 0, 0, 0, 0, 0, 1, 0, 0, 1.5, 0, 0, 0, 0, 0, 1});

	private static final int CIRCLE_SEGMENTS = 60;
	private static final float PENALTY_WIDTH = 2.1f;
	private static final float PENALTY_LENGTH = 1.8f;
	private static final float GOAL_BOX_WIDTH = 3.9f;
	private static final float GOAL_BOX_LENGTH = 1.8f;
	private static final float LINE_THICKNESS = 0.02f;

	private final Material lineMaterial = new Material();
	private float[][] circleVerts;
	private float[][] lineVerts;
	private int[][] lineIndices;
	private boolean geometryUpdated = false;
	private int linesDisplayList;
	private boolean disposed = false;
	private Texture2D lineTexture;

	public Field(Model model, ContentManager cm)
	{
		super(model);
		lineTexture = cm.getWhiteTexture();
	}

	/** Creates the field lines based on dimensions in game state */
	private void calculateLineGeometry(GameState gs)
	{
		float hfl = gs.getFieldLength() / 2.0f;
		float hfw = gs.getFieldWidth() / 2.0f;
		float goalWidth = GOAL_BOX_WIDTH + PENALTY_WIDTH;
		float goalLength = GOAL_BOX_LENGTH + PENALTY_LENGTH;
		float hgw = goalWidth / 2.0f;
		float hgl = goalLength / 2.0f;

		lineVerts = new float[][] {
				// border lines
				{-hfl - LINE_THICKNESS, 0, hfw + LINE_THICKNESS},
				{-hfl - LINE_THICKNESS, 0, -hfw - LINE_THICKNESS},
				{hfl + LINE_THICKNESS, 0, -hfw - LINE_THICKNESS},
				{hfl + LINE_THICKNESS, 0, hfw + LINE_THICKNESS},
				{-hfl + LINE_THICKNESS, 0, hfw - LINE_THICKNESS},
				{-hfl + LINE_THICKNESS, 0, -hfw + LINE_THICKNESS},
				{hfl - LINE_THICKNESS, 0, -hfw + LINE_THICKNESS},
				{hfl - LINE_THICKNESS, 0, hfw - LINE_THICKNESS},

				// center line
				{-LINE_THICKNESS, 0, hfw},
				{-LINE_THICKNESS, 0, -hfw},
				{LINE_THICKNESS, 0, -hfw},
				{LINE_THICKNESS, 0, hfw},

				// right goal box
				{-hfl, 0, hgw + LINE_THICKNESS},
				{-hfl, 0, hgw - LINE_THICKNESS},
				{-hfl + hgl - LINE_THICKNESS, 0, hgw - LINE_THICKNESS},
				{-hfl + hgl - LINE_THICKNESS, 0, -hgw + LINE_THICKNESS},
				{-hfl, 0, -hgw + LINE_THICKNESS},
				{-hfl, 0, -hgw - LINE_THICKNESS},
				{-hfl + hgl + LINE_THICKNESS, 0, -hgw - LINE_THICKNESS},
				{-hfl + hgl + LINE_THICKNESS, 0, hgw + LINE_THICKNESS},

				// left goal box
				{hfl, 0, hgw + LINE_THICKNESS},
				{hfl, 0, hgw - LINE_THICKNESS},
				{hfl - hgl + LINE_THICKNESS, 0, hgw - LINE_THICKNESS},
				{hfl - hgl + LINE_THICKNESS, 0, -hgw + LINE_THICKNESS},
				{hfl, 0, -hgw + LINE_THICKNESS},
				{hfl, 0, -hgw - LINE_THICKNESS},
				{hfl - hgl - LINE_THICKNESS, 0, -hgw - LINE_THICKNESS},
				{hfl - hgl - LINE_THICKNESS, 0, hgw + LINE_THICKNESS},
		};

		lineIndices =
				new int[][] {{0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}, {8, 9, 10, 11}, {12, 13, 14, 19},
						{19, 14, 15, 18}, {15, 16, 17, 18}, {20, 21, 22, 27}, {27, 22, 23, 26}, {23, 24, 25, 26}};

		// center circle
		float radius = gs.getFreeKickDistance();
		circleVerts = new float[CIRCLE_SEGMENTS * 2][3];
		double angleInc = Math.PI * 2.0 / CIRCLE_SEGMENTS;
		int j = 0;
		for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
			Vec3f v = new Vec3f((float) Math.cos(angleInc * i), 0, (float) Math.sin(angleInc * i));
			v = v.normalize();
			circleVerts[j++] = v.times(radius - LINE_THICKNESS).getVals();
			circleVerts[j++] = v.times(radius + LINE_THICKNESS).getVals();
		}

		geometryUpdated = true;
	}

	private void renderLines(GL2 gl)
	{
		linesDisplayList = gl.glGenLists(1);

		gl.glNewList(linesDisplayList, GL2.GL_COMPILE);
		{
			gl.glBegin(GL2.GL_QUADS);
			for (int[] lineIndice : lineIndices)
				for (int j = 0; j < lineIndice.length; j++)
					gl.glVertex3fv(lineVerts[lineIndice[j]], 0);
			gl.glEnd();

			gl.glBegin(GL2.GL_QUAD_STRIP);
			for (float[] circleVert : circleVerts)
				gl.glVertex3fv(circleVert, 0);
			gl.glVertex3fv(circleVerts[0], 0);
			gl.glVertex3fv(circleVerts[1], 0);
			gl.glEnd();
		}
		gl.glEndList();
	}

	public void render(GL2 gl)
	{
		super.render(gl);

		if (geometryUpdated) {
			renderLines(gl);
			geometryUpdated = false;
			lineTexture = Texture2D.loadTex(gl, "textures/white.png", getClass().getClassLoader());
		}

		gl.glNormal3f(0, 1, 0);
		gl.glColor4f(1, 1, 1, 1);
		lineMaterial.setDiffAmbient(1, 1, 1, 1);
		lineMaterial.apply(gl);
		lineTexture.bind(gl);
		gl.glCallList(linesDisplayList);
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
		calculateLineGeometry(gs);
	}

	@Override
	public void gsPlayStateChanged(GameState gs)
	{
	}

	@Override
	public void gsTimeChanged(GameState gs)
	{
	}

	@Override
	public void dispose(GL gl)
	{
		gl.getGL2().glDeleteLists(linesDisplayList, 1);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}
}
