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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import jsgl.jogl.GLDisposable;
import jsgl.jogl.Texture2D;
import jsgl.jogl.light.Material;
import jsgl.jogl.model.Mesh;
import jsgl.jogl.model.MeshFace;
import jsgl.jogl.model.MeshPart;
import jsgl.jogl.model.MeshVertex;
import jsgl.jogl.model.ObjMaterial;
import jsgl.math.BoundingBox;
import jsgl.math.PerlinNoise;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;
import org.apache.commons.lang3.ArrayUtils;
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

	protected ContentManager contentManager;

	private final Material lineMaterial = new Material();
	private float[][] circleVerts;
	private float[][] lineVerts;
	private int[][] lineIndices;
	private boolean geometryUpdated = false;
	private Vec2f fieldDimensions;
	private Model newModel;
	private int linesDisplayList;
	private boolean disposed = false;
	private Texture2D lineTexture;

	public Field(ContentManager cm, float l, float w)
	{
		super(generateFieldModel(cm, l, w));
		contentManager = cm;
		lineTexture = cm.getWhiteTexture();
		fieldDimensions = new Vec2f(l, w);
	}

	private static Model generateFieldModel(ContentManager cm, float l, float w)
	{
		final Mesh mesh = new Mesh();
		final MeshPart part = new MeshPart();

		final float hl = l / 2;
		final float hw = w / 2;

		final var n = new float[] {0, 1, 0};

		final var v1 = new MeshVertex(new float[] {-hl - 2, 0, hw + 2}, n, new float[] {0, 0, 0});
		final var v2 = new MeshVertex(new float[] {hl + 2, 0, hw + 2}, n, new float[] {1, 0, 0});
		final var v3 = new MeshVertex(new float[] {hl + 2, 0, -hw - 2}, n, new float[] {1, 1, 0});
		final var v4 = new MeshVertex(new float[] {-hl - 2, 0, -hw - 2}, n, new float[] {0, 1, 0});

		mesh.addVertex(v1);
		mesh.addVertex(v2);
		mesh.addVertex(v3);
		mesh.addVertex(v4);

		part.addFace(new MeshFace(new int[] {0, 2, 3}));
		part.addFace(new MeshFace(new int[] {0, 1, 2}));

		var name = "field-" + l + "-" + w;
		var material = new ObjMaterial(name);
		material.setTextureImage(generateTexture(l, w));
		part.setMaterial(material);

		mesh.addPart(part);
		mesh.setBounds(new BoundingBox(new Vec3f(-hl - 2, 0, -hw - 2), new Vec3f(hl + 2, 0, hw + 2)));

		var model = new Model(name, mesh);
		cm.requestModelInitialization(model);
		return model;
	}

	private static BufferedImage generateTexture(float l, float w)
	{
		// Add field border
		l += 4;
		w += 4;

		final int width = Math.round(2000 / w * l);
		final int height = 2000;

		var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float xReal = (float) x / width * l;
				float yReal = (float) y / height * w;
				float noise = PerlinNoise.noise3(xReal, yReal, 0, 0, 0, 0);
				int r;
				int g;
				int b;
				if (xReal < 2 || xReal > l - 2 || yReal < 2 || yReal > w - 2) {
					// Border
					r = (int) (160 + noise * 20);
					g = (int) (220 + noise * 25);
					b = (int) (80 + noise * 20);
				} else if ((int) xReal % 2 == 1) {
					// Stripe 2
					r = (int) (100 + noise * 25);
					g = (int) (190 + noise * 35);
					b = (int) (45 + noise * 20);
				} else {
					// Stripe 1
					r = (int) (120 + noise * 25);
					g = (int) (210 + noise * 35);
					b = (int) (45 + noise * 20);
				}
				pixels[width * y + x] = (r << 16) | (g << 8) | b;
			}
		}
		return img;
	}

	/** Creates the field lines based on dimensions in game state */
	private void calculateLineGeometry(GameState gs)
	{
		float hfl = gs.getFieldLength() / 2.0f;
		float hfw = gs.getFieldWidth() / 2.0f;
		float goalWidth = GOAL_BOX_WIDTH + PENALTY_WIDTH;
		float goalLength = GOAL_BOX_LENGTH + PENALTY_LENGTH;
		if (gs.getGoalieAreaLength() > 0) {
			goalWidth = gs.getGoalieAreaWidth();
			goalLength = gs.getGoalieAreaLength();
		}
		float hgw = goalWidth / 2.0f;
		float hgl = goalLength / 2.0f;
		float penaltyAreaWidth = 0;
		float penaltyAreaLength = 0;
		if (gs.getPenaltyAreaLength().isPresent()) {
			penaltyAreaWidth = gs.getPenaltyAreaWidth().get();
			penaltyAreaLength = gs.getPenaltyAreaLength().get();
		}
		final float hpw = penaltyAreaWidth / 2.0f;
		final float hpl = penaltyAreaLength / 2.0f;

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

				// right penalty area
				{-hfl, 0, hpw + LINE_THICKNESS},
				{-hfl, 0, hpw - LINE_THICKNESS},
				{-hfl + hpl - LINE_THICKNESS, 0, hpw - LINE_THICKNESS},
				{-hfl + hpl - LINE_THICKNESS, 0, -hpw + LINE_THICKNESS},
				{-hfl, 0, -hpw + LINE_THICKNESS},
				{-hfl, 0, -hpw - LINE_THICKNESS},
				{-hfl + hpl + LINE_THICKNESS, 0, -hpw - LINE_THICKNESS},
				{-hfl + hpl + LINE_THICKNESS, 0, hpw + LINE_THICKNESS},

				// left penalty area
				{hfl, 0, hpw + LINE_THICKNESS},
				{hfl, 0, hpw - LINE_THICKNESS},
				{hfl - hpl + LINE_THICKNESS, 0, hpw - LINE_THICKNESS},
				{hfl - hpl + LINE_THICKNESS, 0, -hpw + LINE_THICKNESS},
				{hfl, 0, -hpw + LINE_THICKNESS},
				{hfl, 0, -hpw - LINE_THICKNESS},
				{hfl - hpl - LINE_THICKNESS, 0, -hpw - LINE_THICKNESS},
				{hfl - hpl - LINE_THICKNESS, 0, hpw + LINE_THICKNESS},
		};

		lineIndices =
				new int[][] {{0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}, {8, 9, 10, 11}, {12, 13, 14, 19},
						{19, 14, 15, 18}, {15, 16, 17, 18}, {20, 21, 22, 27}, {27, 22, 23, 26}, {23, 24, 25, 26}};
		if (gs.getPenaltyAreaLength().isPresent()) {
			lineIndices =
					ArrayUtils.addAll(lineIndices, new int[][] {{28, 29, 30, 35}, {35, 30, 31, 34}, {31, 32, 33, 34},
														   {36, 37, 38, 43}, {43, 38, 39, 42}, {39, 40, 41, 42}});
		}

		// center circle
		float radius = gs.getCenterCircleRadius();
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
		if (newModel != null) {
			model.dispose(gl);
			model = newModel;
			newModel = null;
			model.init(gl, contentManager.getMeshRenderMode());
		}

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

	private void updateModel(GameState gs)
	{
		var newDimensions = new Vec2f(gs.getFieldLength(), gs.getFieldWidth());
		if (newDimensions.x != fieldDimensions.x || newDimensions.y != fieldDimensions.y) {
			fieldDimensions = newDimensions;
			newModel = generateFieldModel(contentManager, fieldDimensions.x, fieldDimensions.y);
		}
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
		updateModel(gs);
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
		model.dispose(gl);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}
}
