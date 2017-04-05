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

package rv.comm.rcssserver.scenegraph;

import rv.comm.rcssserver.SExp;

/**
 * Identifies the type of scene graph information contained in a message. A full scene graph
 * contains full descriptions for every node and may have a different structure than a previous
 * scene graph. A "diff" scene graph has the same structure as the currently stored scene graph, but
 * only nodes that have changed will have non-empty descriptions.
 *
 * @author Justin Stoecker
 */
public class SceneGraphHeader
{
	public static final String FULL = "RSG";
	public static final String DIFF = "RDS";

	private final String type;
	private final int majorVersion;
	private final int minorVersion;

	public String getType()
	{
		return type;
	}

	public int getMajorVersion()
	{
		return majorVersion;
	}

	public int getMinorVersion()
	{
		return minorVersion;
	}

	private SceneGraphHeader(String type, int major, int minor)
	{
		this.type = type;
		this.majorVersion = major;
		this.minorVersion = minor;
	}

	@Override
	public String toString()
	{
		return String.format("%s v%d.%d", type, majorVersion, minorVersion);
	}

	/**
	 * Parses a scene graph header from an s-expression
	 */
	public static SceneGraphHeader parse(SExp sexp)
	{
		// s-expression: (<type> <major> <minor>)
		// ex. (RDS 0 1)
		String[] atoms = sexp.getAtoms();
		String type = atoms[0];
		int majorVersion = Integer.parseInt(atoms[1]);
		int minorVersion = Integer.parseInt(atoms[2]);

		return new SceneGraphHeader(type, majorVersion, minorVersion);
	}
}
