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

package jsgl.jogl.verts;

import com.jogamp.opengl.GL2;

public abstract class Vertex
{
	/** Returns the size of the vertex stride in bytes */
	public abstract int getSize();

	/** Declares the vertex format as the current layout being processed */
	public abstract void setState(GL2 gl);

	/** Unsets the client states modified by the vertex format */
	public abstract void unsetState(GL2 gl);

	/** Returns the values of the vertex in a single array */
	public abstract float[] getElements();
}