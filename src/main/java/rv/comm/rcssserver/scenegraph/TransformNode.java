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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rv.comm.rcssserver.SExp;

/**
 * Describes a local transformation (translation, rotation, scale) applied to current node and all
 * its children
 *
 * @author Justin Stoecker
 */
public class TransformNode extends Node
{
	private static final Logger LOGGER = LogManager.getLogger();

	/** Abbreviation declaring this node type in an s-expression */
	public static final String EXP_ABRV = "TRF";

	public TransformNode(Node parent, SExp exp)
	{
		super(parent);
		// (nd TRF (SLT nx ny nz 0 ox oy oz 0 ax ay az 0 Px Py Pz 1 ))

		// [nx ox ax Px]
		// [ny oy ay Px]
		// [nz oz az Pz]
		// [ 0 0 0 1]
		setMatrix(exp.getChildren().get(0).getAtoms());
	}

	private void setMatrix(String[] atoms)
	{
		if (atoms[0].equals("SLT")) {
			double[] a = new double[16];
			for (int i = 0; i < 16; i++) {
				try {
					a[i] = Double.parseDouble(atoms[i + 1]);
				} catch (NumberFormatException e) {
					LOGGER.error("Error setting matrix", e);
					// ignore nan values from a server bug (see https://gitlab.com/robocup-sim/SimSpark/issues/5)
				}
			}
			localTransform = new Matrix(a);
		}
	}

	@Override
	public void update(SExp exp)
	{
		if (exp.getChildren() != null) {
			setMatrix(exp.getChildren().get(0).getAtoms());
		}
		super.update(exp);
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
