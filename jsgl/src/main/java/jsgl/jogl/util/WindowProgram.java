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

package jsgl.jogl.util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import jsgl.jogl.view.Viewport;

/**
 * Interface for a single-window JOGL program.
 *
 * @author Justin Stoecker
 */
public interface WindowProgram extends GLEventListener
{
	GLU getGLU();
	GLUT getGLUT();
	GLAutoDrawable getGLDrawable();
	Viewport getWindowDimensions();

	void update(GL gl);
	void render(GL gl);
}
