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

uniform sampler2D inputTexture;

uniform float threshold;

void main()
{
	vec4 sample = texture2D(inputTexture, gl_TexCoord[0].st);
	sample = (sample - threshold) / (1.0 - threshold);
	sample.r = clamp(sample.r,0.0,1.0);
	sample.g = clamp(sample.g,0.0,1.0);
	sample.b = clamp(sample.b,0.0,1.0);

	gl_FragColor = sample;
}
