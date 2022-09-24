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

package rv.world.rendering;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.magmaoffenburg.roboviz.configuration.Config;
import rv.comm.drawing.Drawings;
import rv.content.ContentManager;
import rv.world.WorldModel;

/**
 * Renders world model scene using a specific shader, lighting effect, etc.
 *
 * @author justin
 */
public interface SceneRenderer
{
	/**
	 * Initializes renderer to be prepared for rendering; must be called once before render method.
	 *
	 * @return true if initialization successful
	 */
	boolean init(GL2 gl, Config.Graphics conf, ContentManager cm);

	/**
	 * Renders world model scene to currently bound frame buffer according to this renderer's
	 * implementation
	 */
	void render(GL2 gl, WorldModel world, Drawings drawings);

	/**
	 * Release any resources used by renderer
	 */
	void dispose(GL gl);
}
