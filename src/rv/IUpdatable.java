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

package rv;

import javax.media.opengl.GL2;
import rv.ui.UserInterface;
import rv.world.WorldModel;

/**
 * Interface for classes that should be updated in the main update loop
 * 
 * @author Justin Stoecker
 */
public interface IUpdatable {

    /**
     * Allows a module to update its state.
     *
     * @param world
     *            - world model state
     * @param ui
     *            - user interface
     * @param elapsedMS
     *            - milliseconds since previous update
     */
    void update(GL2 gl, WorldModel world, UserInterface ui, double elapsedMS);
}
