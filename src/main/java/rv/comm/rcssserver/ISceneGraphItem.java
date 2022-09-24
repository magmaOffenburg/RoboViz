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

package rv.comm.rcssserver;

import rv.comm.rcssserver.scenegraph.SceneGraph;

/**
 * An object that may use information in the RCSSServer scene graph. This interface allows specific
 * objects to receive updates when the scene graph changes. Rather than making the scene graph an
 * event source and allowing event listeners, objects that use the scene graph info should implement
 * this interface and add themselves to the WorldModel's scene graph items. There is no need for
 * items to constantly update their state; they only need to be up-to-date just before rendering
 * occurs.
 *
 * @author Justin Stoecker
 */
public interface ISceneGraphItem
{
	/**
	 * The scene graph's structure typically remains consistent throughout the life of the
	 * simulation, but sometimes it is entirely new. This occurs, for example, when the monitor
	 * first connects or an agent joins the simulation. The object implementing this interface is
	 * given a chance to locate its respective node in the scene graph and store a reference to it
	 * for future scene graph updates.
	 */
	void sceneGraphChanged(SceneGraph sg);

	/**
	 * When called, the object implementing this interface can pull data from the scene graph and
	 * store it in its internal state. It is expected that the implementing object will keep a
	 * reference to its node in the scene graph through the findNode method.
	 */
	void update(SceneGraph sg);
}
