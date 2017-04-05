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

/**
 * Generic node that doesn't fit into other node categories.
 *
 * @author Justin Stoecker
 */
public class BaseNode extends Node
{
	/** Abbreviation declaring this node type in an s-expression */
	private static final String EXP_ABRV = "BN";

	public BaseNode()
	{
		super(null);
		// (nd BN <contents>)
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
