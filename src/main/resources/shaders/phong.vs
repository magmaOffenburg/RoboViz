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

void main()
{   
    diffuseTexCoords = vec2(gl_MultiTexCoord0);

    normal = normalize(gl_NormalMatrix * gl_Normal);
    lightDir = normalize(gl_LightSource[0].position.xyz);
    halfVector = normalize(gl_LightSource[0].halfVector.xyz);
    C_diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
    C_ambient = gl_FrontMaterial.ambient * (gl_LightSource[0].ambient + 
       gl_LightModel.ambient);
    C_specular = gl_FrontMaterial.specular * gl_LightSource[0].specular;
    
    gl_Position = ftransform();
} 
