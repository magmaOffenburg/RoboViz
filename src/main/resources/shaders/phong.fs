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

varying vec3    normal;
varying vec3    lightDir;
varying vec3    halfVector;
varying vec4    C_diffuse;
varying vec4    C_ambient;
varying vec4    C_specular;
varying vec2    diffuseTexCoords;

uniform sampler2D diffuseTexture;

void main()
{
    vec4 C_light = C_ambient;
    vec4 C_texture = texture2D(diffuseTexture, diffuseTexCoords);
    
    vec3 n = normalize(normal);
    vec3 h = normalize(halfVector);
    
    float I_diffuse = max(dot(n, lightDir), 0.0);
    float I_specular = pow(max(dot(n, h), 0.0), gl_FrontMaterial.shininess);

    C_light += I_diffuse * C_diffuse + I_specular * C_specular;
    
    gl_FragColor = C_light * ((1.0 - C_texture.a) + C_texture);
    gl_FragColor.a = C_texture.a;
}
