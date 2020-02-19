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

const int SAMPLES = 5;

uniform sampler2D tex;
uniform vec2 offsets[SAMPLES];
uniform float weights[SAMPLES];

void main()
{	
	vec4 color = vec4(0.0);

	for (int i = 0; i < SAMPLES; i++)
		color += texture2D(tex, gl_TexCoord[0].xy + offsets[i]) * weights[i];
		
	gl_FragColor = color;
} 
