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
 * Mesh loaded from an .obj file
 *
 * @author Justin Stoecker
 */
public class StaticMeshNode extends GeometryNode
{
	/** Abbreviation declaring this node type in an s-expression */
	public static final String EXP_ABRV = "StaticMesh";

	final String s;

	public StaticMeshNode(Node parent, SExp exp)
	{
		super(parent, exp);
		s = exp.toString();
		// (nd StaticMesh (load <model>) (sSc <x> <y> <z>) (setVisible 1)
		// (setTransparent) (resetMaterials <material-list>))
	}

	@Override
	protected void load(SExp exp)
	{
		name = exp.getAtoms()[1];
	}

	@Override
	public String toString()
	{
		return StaticMeshNode.class.getName() + ": " + s;
	}
}
