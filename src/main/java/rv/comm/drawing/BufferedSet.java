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

package rv.comm.drawing;

import java.util.ArrayList;

/**
 * Contains two buffers of the same data type that can be used for asynchronous reading and writing
 * of data
 *
 * @author justin
 */
public class BufferedSet<T> implements VisibleNamedObject
{
	private boolean visible = true;
	private final String name;
	private int frontBuf = 0;
	private int backBuf = 1;

	@SuppressWarnings("unchecked")
	private final ArrayList<T>[] buffers = new ArrayList[2];

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public String getName()
	{
		return name;
	}

	public BufferedSet(String name)
	{
		this.name = name;
		buffers[0] = new ArrayList<>();
		buffers[1] = new ArrayList<>();
	}

	/**
	 * Gets a copy of the set of data stored in the front buffer
	 */
	public synchronized ArrayList<T> getFrontSet()
	{
		// a copy is made because we don't want to return a reference to the
		// current front buffer; this buffer may be swapped while the thread
		// is still using the shapes in it (such as rendering).
		ArrayList<T> set = buffers[frontBuf];
		ArrayList<T> setCopy = new ArrayList<>(set.size());
		setCopy.addAll(set);

		return setCopy;
	}

	/**
	 * Adds data to the back buffer set. This method is not synchronized as the only thread that
	 * should be using it is the one that is also responsible for swapping the buffers.
	 */
	public void put(T data)
	{
		buffers[backBuf].add(data);
	}

	/**
	 * Swaps the front and back buffers and clears the new back buffer.
	 */
	public synchronized void swapBuffers()
	{
		int temp = backBuf;
		backBuf = frontBuf;
		frontBuf = temp;
		buffers[backBuf].clear();
	}
}
