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

/**
 * Interface for cleaning up resources that are buffered on the graphics card
 *
 * @author Justin Stoecker
 */
public interface GLDisposable
{
	/** Release any resources buffered in video memory */
	void dispose(GL gl);

	/** The object has released any resources buffered in video memory */
	boolean isDisposed();
}
