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

import java.util.ArrayList;
import jsgl.math.vector.Matrix;
import rv.comm.rcssserver.SExp;

/**
 * Element of the scene graph that may have children nodes. Used to organize the arrangement of
 * objects in the server simulation, so each node contains a local transformation matrix. Subclasses
 * may have other properties that define objects, shapes, lights, or other features relevant to the
 * scene.
 *
 * @author Justin Stoecker
 */
public abstract class Node
{
	/** Abbreviation indicating a node declaration in an s-expression */
	public static final String DECL_ABRV = "nd";

	// local transformation matrix is initially null to save space; a null
	// matrix should be treated as an identity matrix when exposing the node
	// to other classes
	protected Matrix localTransform;

	// initially, the node has no children and is therefore a leaf node
	protected ArrayList<Node> children;

	// if the node has no parent, it is assumed to be a root of the graph
	protected final Node parent;

	public Node getParent()
	{
		return parent;
	}

	public boolean isRoot()
	{
		return parent == null;
	}

	public boolean isLeaf()
	{
		return children == null;
	}

	public ArrayList<Node> getChildren()
	{
		return children;
	}

	/**
	 * The absolute transformation for this node in the graph. This is the combination of all local
	 * transformations from each node above this node
	 */
	public Matrix getWorldTransform()
	{
		if (parent == null) {
			if (localTransform == null)
				return Matrix.createIdentity();
			return localTransform;
		} else {
			if (localTransform == null)
				return parent.getWorldTransform();
			return parent.getWorldTransform().times(localTransform);
		}
	}

	public Node(Node parent)
	{
		this.parent = parent;
	}

	protected void update(SExp exp)
	{
		if (exp.getChildren() == null || children == null)
			return;

		// updates in expression should follow same structure as the
		// original scene graph, so children are traversed in same order
		int childIndex = 0;
		int size = children.size();
		for (SExp e : exp.getChildren()) {
			if (e.getAtoms()[0].equals(Node.DECL_ABRV) && childIndex < size) {
				Node child = children.get(childIndex++);
				child.update(e);
			}
		}
	}
}
