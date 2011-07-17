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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import rv.Configuration;
import rv.comm.drawing.Drawings;
import rv.content.ContentManager;
import rv.world.WorldModel;

/**
 * Renders world model scene using a specific shader, lighting effect, etc.
 * 
 * @author justin
 */
public interface SceneRenderer {

    /**
     * Initializes renderer to be prepared for rendering; must be called once before render method.
     * 
     * @return true if initialization successful
     */
    public boolean init(GL2 gl, Configuration.Graphics conf, ContentManager cm);

    /**
     * Renders world model scene to currently bound frame buffer according to this renderer's
     * implementation
     * 
     * @param gl
     * @param world
     */
    public void render(GL2 gl, WorldModel world, Drawings drawings);

    /**
     * Release any resources used by renderer
     * 
     * @param gl
     */
    public void dispose(GL gl);
}
