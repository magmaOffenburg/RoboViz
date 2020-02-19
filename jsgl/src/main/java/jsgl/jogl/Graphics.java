/*
 *  Copyright 2011 Justin Stoecker
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

package jsgl.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import jsgl.math.Ray;
import jsgl.math.vector.Vec3d;

/**
 * Graphics math routines
 *
 * @author Justin Stoecker
 */
public class Graphics
{
	public static Ray unproject(GL2 gl, GLU glu, int x, int y)
	{
		int[] viewport = new int[4];
		double[] modelView = new double[16];
		double[] projection = new double[16];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, modelView, 0);
		gl.glGetDoublev(GLMatrixFunc.GL_PROJECTION_MATRIX, projection, 0);

		double[] worldCoords = new double[4];

		glu.gluUnProject(x, viewport[3] - y - 1, 0.0, modelView, 0, projection, 0, viewport, 0, worldCoords, 0);
		Vec3d near = new Vec3d(worldCoords);

		glu.gluUnProject(x, viewport[3] - y - 1, 1.0, modelView, 0, projection, 0, viewport, 0, worldCoords, 0);
		Vec3d far = new Vec3d(worldCoords);
		Vec3d dir = far.minus(near).normalize();

		return new Ray(near.toVec3f(), dir.toVec3f());
	}

	//   public static Ray unproject(GL2 gl, GLU glu, int x, int y, Matrix mv, Matrix projection, Viewport vp) {
	//      double[] worldCoords = new double[4];
	//
	//      glu.gluUnProject(x, viewport[3] - y - 1, 0.0, mv.wrap().array(), 0, projection.wrap().array(),
	//            0, viewport, 0, worldCoords, 0);
	//      Vec3d near = new Vec3d(worldCoords);
	//
	//      glu.gluUnProject(x, viewport[3] - y - 1, 1.0, modelView, 0, projection,
	//            0, viewport, 0, worldCoords, 0);
	//      Vec3d far = new Vec3d(worldCoords);
	//      Vec3d dir = far.minus(near).normalize();
	//
	//      return new Ray(near.toVec3f(), dir.toVec3f());
	//   }
}
