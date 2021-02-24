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
 * Describes a light that is applied to objects in the scene
 *
 * @author Justin Stoecker
 */
public class LightNode extends Node
{
	public static final String EXP_ABRV = "Light";

	private final float[] diffuse = new float[4];
	private final float[] ambient = new float[4];
	private final float[] specular = new float[4];

	public LightNode(Node parent, SExp exp)
	{
		super(parent);
		// (nd Light (setDiffuse x y z w) (setAmbient x y z w)
		// (setSpecular x y z w))
		for (SExp e : exp.getChildren()) {
			String operation = e.getAtoms()[0];
			switch (operation) {
			case "setDiffuse":
				copyValues(e, diffuse);
				break;
			case "setAmbient":
				copyValues(e, ambient);
				break;
			case "setSpecular":
				copyValues(e, specular);
				break;
			}
		}
	}

	/** Copies values to diffuse, ambient, or specular from expression */
	private void copyValues(SExp exp, float[] array)
	{
		String[] atoms = exp.getAtoms();
		for (int i = 0; i < 4; i++)
			array[i] = Float.parseFloat(atoms[i + 1]);
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
