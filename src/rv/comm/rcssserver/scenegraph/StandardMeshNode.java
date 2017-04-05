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
 * Predefined mesh type using standard shapes
 *
 * @author Justin Stoecker
 */
public class StandardMeshNode extends GeometryNode
{
	/** Abbreviation declaring this node type in an s-expression */
	public static final String EXP_ABRV = "SMN";

	public StandardMeshNode(Node parent, SExp exp)
	{
		super(parent, exp);

		// TODO: currently this type of mesh isn't used

		// (nd SMN (load StdUnitBox) (sSc 1 31 1) (sMat matGrey))
		// (nd SMN (load StdUnitCylinder 0.015 0.08) (sSc 1 1 1) (sMat
		// matDarkGrey))
	}

	@Override
	protected void load(SExp exp)
	{
		// TODO Auto-generated method stub
		name = exp.getAtoms()[1];
	}
}
