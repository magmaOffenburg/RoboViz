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

package rv.world;

import com.jogamp.opengl.GL2;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Vec3f;

/**
 * Allows an object to be selected
 *
 * @author Justin Stoecker
 */
public interface ISelectable
{
	Vec3f getPosition();

	BoundingBox getBoundingBox();

	void setSelected(boolean selected);

	boolean isSelected();

	void renderSelected(GL2 gl);
}