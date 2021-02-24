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

varying vec4 vertPos;

void main()
{
	// homogeneous coordinate division
	float depth = vertPos.z / vertPos.w;
	
	// [-1,1] -> [0,1] range
	depth = depth * 0.5 + 0.5;
	
	// store depth in red channel; depth bias in green
	float dx = dFdx(depth);
	float dy = dFdy(depth);
	float m = depth * depth + (dx * dx + dy * dy) * 0.25;
	
	gl_FragColor = vec4(depth, m, 1.0, 1.0);
}