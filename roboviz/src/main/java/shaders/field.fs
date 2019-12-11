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

uniform sampler2D texSampler;
uniform sampler2D shadowMap;

varying vec4 shadowCoords;
varying vec2 texcoords;

float chebyshevUpperBound(vec4 shadowCoordsPostW)
{
	float distance = shadowCoordsPostW.z;
	vec2 moments = texture2D(shadowMap, shadowCoordsPostW.xy).rg;
	
	// Surface is fully lit. as the current fragment is before the light occluder
	if (distance <= moments.x)
		return 1.0 ;

	// float variance = moments.y - (moments.x * moments.x);
	// variance = max(variance, 0.00012);
	// float d = distance - moments.x;
	// float p_max = variance / (variance + d*d);

	// return p_max;
	
	float E_x2 = moments.y;
    float Ex_2 = moments.x * moments.x;
    float variance = E_x2 - Ex_2;
    float mD = moments.x - distance;
    float mD_2 = mD * mD;
    float p = variance / (variance + mD_2);
    return p;
	
}

void main()
{	
	vec4 shadowCoordsPostW = shadowCoords / shadowCoords.w;
	float shadow = 1.0 - chebyshevUpperBound(shadowCoordsPostW);

	vec4 color = gl_Color;
	color.rgb *= clamp(shadow, 0.0, 1.0);

	gl_FragColor = color * texture2D(texSampler, texcoords);
}