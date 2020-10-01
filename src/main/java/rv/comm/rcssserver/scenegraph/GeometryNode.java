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

import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import rv.comm.rcssserver.SExp;

/**
 * Describes an object and its material. There are two types: static meshes and standard mesh
 * objects (box, cylinder, etc).
 *
 * @author Justin Stoecker
 */
public abstract class GeometryNode extends Node
{
	protected boolean transparent = false;
	protected boolean visible = false;
	protected Matrix scale = Matrix.createIdentity();
	protected String name;
	protected String[] materials;

	public boolean isVisible()
	{
		return visible;
	}

	public boolean isTransparent()
	{
		return transparent;
	}

	public Matrix getScale()
	{
		return scale;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String[] getMaterials()
	{
		return materials;
	}

	public GeometryNode(Node parent, SExp exp)
	{
		super(parent);
		applyOperations(exp);
	}

	private void applyOperations(SExp exp)
	{
		for (SExp e : exp.getChildren()) {
			String operation = e.getAtoms()[0];
			switch (operation) {
			case "load":
				load(e);
				break;
			case "sSc":
				setScale(e);
				break;
			case "setVisible":
				visible = e.getAtoms()[1].equals("1");
				break;
			case "resetMaterials":
				materials = new String[e.getAtoms().length - 1];
				System.arraycopy(e.getAtoms(), 1, materials, 0, materials.length);
				break;
			case "setTransparent":
				transparent = true;
				break;
			}
		}
	}

	protected abstract void load(SExp exp);

	private void setScale(SExp exp)
	{
		float[] xyz = new float[3];
		for (int i = 0; i < 3; i++)
			xyz[i] = Float.parseFloat(exp.getAtoms()[i + 1]);
		scale = Matrix.createScale(new Vec3f(xyz));
		if (localTransform != null)
			localTransform = localTransform.times(scale);
		else
			localTransform = scale;
	}

	public boolean containsMaterial(String name)
	{
		for (String material : materials)
			if (material.equals(name))
				return true;
		return false;
	}

	@Override
	public void update(SExp exp)
	{
		if (exp.getChildren() != null) {
			applyOperations(exp);
		}
		super.update(exp);
	}

	@Override
	public String toString()
	{
		return String.format("%s (%s)", getClass().getName(), name);
	}
}
