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

package jsgl.jogl.model;

/**
 * Smallest element of a mesh that contains data such as position, normal, and
 * texture coordinates.
 *
 * @author Justin Stoecker
 */
public class MeshVertex
{
	private int numElements;
	private int size;
	private float[] position;
	private float[] normal;
	private float[] texCoords;

	public int getNumElements()
	{
		return numElements;
	}

	public int getSizeBytes()
	{
		return size;
	}

	public float[] getPosition()
	{
		return position;
	}

	public float[] getNormal()
	{
		return normal;
	}

	public float[] getTexCoords()
	{
		return texCoords;
	}

	public MeshVertex(float[] position, float[] normal, float[] texCoords)
	{
		this.position = position;
		this.normal = normal;
		this.texCoords = texCoords;

		numElements = position.length;
		if (normal != null)
			numElements += normal.length;
		if (texCoords != null)
			numElements += texCoords.length;

		size = numElements * 4;
	}
}
