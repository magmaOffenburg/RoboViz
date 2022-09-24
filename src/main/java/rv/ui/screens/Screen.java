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
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import jsgl.jogl.view.Viewport;
import rv.util.WindowResizeListener;

/**
 * A screen represents a visual 2D interface that is displayed on top of the 3D rendering of the
 * scene. A screen typically acts as a keyboard and mouse listener as well.
 *
 * @author justin
 */
public interface Screen extends WindowResizeListener
{
	void setEnabled(GLCanvas canvas, boolean enabled);

	void render(GL2 gl, GLU glu, GLUT glut, Viewport vp);

	boolean isVisible();

	void setVisible(boolean visible);

	void stop();
}