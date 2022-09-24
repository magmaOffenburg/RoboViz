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
import java.util.List;
import rv.comm.rcssserver.SExp;

/**
 * Contains scene information from rcssserver: geometry, transformations, lighting, etc.
 *
 * @author Justin Stoecker
 */
public class SceneGraph
{
	public interface SceneGraphListener
	{
		void newSceneGraph(SceneGraph sg);

		void updatedSceneGraph(SceneGraph sg);
	}

	private final Node root;

	public Node getRoot()
	{
		return root;
	}

	public void print()
	{
		print(root, 0, 0);
	}

	private void print(Node n, int depth, int childNumber)
	{
		for (int i = 0; i < depth; i++)
			System.out.print("--");
		System.out.printf("[%d,%d]: %s\n", depth, childNumber, n.toString());

		if (n.getChildren() == null)
			return;

		int chNum = 0;
		for (Node child : n.getChildren()) {
			print(child, depth + 1, chNum++);
		}
	}

	/**
	 * Finds the first instance of a StaticMeshNode with a specified name starting at the root node
	 * n
	 */
	public StaticMeshNode findStaticMeshNode(String name, Node n)
	{
		return findStaticMeshNode(n, name);
	}

	/** Finds the first instance of a StaticMeshNode with a specified name */
	public StaticMeshNode findStaticMeshNode(String name)
	{
		return findStaticMeshNode(root, name);
	}

	/**
	 * Finds a StaticMeshNode that contains (at least) all materials in the provided list of
	 * materials
	 */
	public StaticMeshNode findStaticMeshNode(Node node, String[] materials)
	{
		// check if current node is the node we're looking for
		if (node instanceof StaticMeshNode) {
			StaticMeshNode smn = (StaticMeshNode) node;
			String[] nodeMats = smn.getMaterials();

			// make sure each material in the list is in the node's materials
			int numMatsContained = 0;
			for (String material : materials) {
				boolean containsMaterial = false;

				// see if node contains current material and stop when it is found
				for (String nodeMat : nodeMats) {
					if (nodeMat.equals(material)) {
						containsMaterial = true;
						numMatsContained++;
						break;
					}
				}

				// if current material in list wasn't found, don't bother
				// checking for the others
				if (!containsMaterial)
					break;
			}

			if (numMatsContained == materials.length)
				return smn;
		}

		// no children? can't search any further...
		if (node.getChildren() == null)
			return null;

		// recursive check on children
		for (Node child : node.getChildren()) {
			StaticMeshNode smn = findStaticMeshNode(child, materials);
			if (smn != null)
				return smn;
		}

		// nothing found in children, so it couldn't be found
		return null;
	}

	private StaticMeshNode findStaticMeshNode(Node parent, String name)
	{
		// check if current node is the node we're looking for
		if (parent instanceof StaticMeshNode) {
			StaticMeshNode smn = (StaticMeshNode) parent;
			if (smn.name.endsWith(name))
				return smn;
		}

		// no children? can't search any further...
		if (parent.getChildren() == null)
			return null;

		// recursive check on children
		for (Node child : parent.getChildren()) {
			StaticMeshNode smn = findStaticMeshNode(child, name);
			if (smn != null)
				return smn;
		}

		// nothing found in children, so it couldn't be found
		return null;
	}

	/**
	 * Retrieves a list of all mesh nodes
	 */
	public List<StaticMeshNode> getAllMeshNodes()
	{
		return getAllMeshNodes(root);
	}

	/**
	 * Retrieves a list of all mesh nodes below a given node in the graph
	 */
	public List<StaticMeshNode> getAllMeshNodes(Node rootNode)
	{
		List<StaticMeshNode> nodes = new ArrayList<>();
		appendMeshNode(nodes, rootNode);
		return nodes;
	}

	private void appendMeshNode(List<StaticMeshNode> list, Node node)
	{
		if (node instanceof StaticMeshNode)
			list.add((StaticMeshNode) node);

		if (node.getChildren() != null) {
			for (int i = 0; i < node.getChildren().size(); i++)
				appendMeshNode(list, node.getChildren().get(i));
		}
	}

	/**
	 * Creates a new scene graph by parsing nodes contained in s-expression
	 */
	public SceneGraph(SExp exp)
	{
		root = new BaseNode();
		readNodes(root, exp);
	}

	/**
	 * Updates scene graph with new information. The structure of the scene graph remains unchanged.
	 */
	public void update(SExp exp)
	{
		root.update(exp);
	}

	/**
	 * Recursive method that reads nodes from expression and adds them to parent
	 */
	private void readNodes(Node parent, SExp exp)
	{
		// if there are no children expressions, the parent node must be a leaf
		ArrayList<SExp> subExpressions = exp.getChildren();
		if (subExpressions == null)
			return;

		// otherwise, there may be nodes to parse and add to the parent node
		for (SExp e : subExpressions) {
			// each node declaration starts with "nd" followed by its type
			String[] atoms = e.getAtoms();
			if (atoms[0].equals(Node.DECL_ABRV)) {
				String type = atoms[1];
				Node node = null;
				switch (type) {
				case TransformNode.EXP_ABRV:
					node = new TransformNode(parent, e);
					break;
				case LightNode.EXP_ABRV:
					node = new LightNode(parent, e);
					break;
				case StaticMeshNode.EXP_ABRV:
					node = new StaticMeshNode(parent, e);
					break;
				case StandardMeshNode.EXP_ABRV:
					node = new StandardMeshNode(parent, exp);
					break;
				}

				if (node != null) {
					if (parent.children == null)
						parent.children = new ArrayList<>();
					parent.children.add(node);

					// keep reading child's branch of nodes recursively
					readNodes(node, e);
				}
			}
		}
	}
}
