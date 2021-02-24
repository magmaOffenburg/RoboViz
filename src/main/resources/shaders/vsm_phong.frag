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

// Single directional light shadow mapping with Phong illumination. Also
// supports texture mapping for a single texture. If object does not have any
// textures and should only use material settings, bind a white 1x1 texture to
// the diffuse texture sampler or use alternative shader that does not use
// textures.

const float light_vsm_epsilon = 0.000001;

varying vec3    normal;
varying vec3    lightDir;
varying vec3    halfVector;
varying vec4    C_diffuse;
varying vec4    C_ambient;
varying vec4    C_specular;
varying vec2    diffuseTexCoords;
varying vec2    shadowTexCoords;
varying float   fragDepth;

uniform sampler2D diffuseTexture;
uniform sampler2D shadowTexture;

float linstep(float minVal, float maxVal, float val)  
{  
    float a = val - minVal;
    float b = maxVal - minVal;
    return clamp(a / b, 0.0, 1.0);  
}  

float lightBleedReduction(float p_max, float Amount)
{
    return linstep(Amount, 1.0, p_max); 
}

float calcShadowFactor()
{
    vec2 moments = texture2D(shadowTexture, shadowTexCoords).rg;

    float p_max = (fragDepth <= moments.x) ? 1.0 : 0.0;

    // calculate variance
    float E_x2 = moments.y;
    float Ex_2 = moments.x * moments.x;
    float variance = min(max(E_x2 - Ex_2, light_vsm_epsilon), 1.0);
    
    float m_d = moments.x - fragDepth;
    float p = variance / (variance + m_d * m_d);

    p = lightBleedReduction(p, 0.3);

    return max(p_max, p);
}

void main()
{
	vec4 C_light = C_ambient;
	vec4 C_texture = texture2D(diffuseTexture, diffuseTexCoords);
	
	vec3 n = normalize(normal);
	vec3 h = normalize(halfVector);
	
	float I_diffuse = max(dot(n, lightDir), 0.0);
	float I_shadow = min(calcShadowFactor() + 0.6, 1.0);
    float I_specular = pow(max(dot(n, h), 0.0), gl_FrontMaterial.shininess);

    C_light += I_shadow * (I_diffuse * C_diffuse + I_specular * C_specular);
	
    gl_FragColor = C_light * ((1.0 - C_texture.a) + C_texture);
    gl_FragColor.a = C_texture.a;
}
