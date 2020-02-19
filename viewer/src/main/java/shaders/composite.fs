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

uniform sampler2D inputTexture1;
uniform sampler2D inputTexture2;

uniform float intensity;

vec4 saturate(vec4 v)
{
	vec4 saturated;
	saturated.r = clamp(v.r,0.0,1.0);
	saturated.g = clamp(v.g,0.0,1.0);
	saturated.b = clamp(v.b,0.0,1.0);
	saturated.a = clamp(v.a,0.0,1.0);

	return saturated;
}

void main()
{
	vec4 a = texture2D(inputTexture1, gl_TexCoord[0].st);
	vec4 b = texture2D(inputTexture2, gl_TexCoord[0].st) * intensity;	

	a *= (1.0 - saturate(b));

	gl_FragColor = a + b;
}
