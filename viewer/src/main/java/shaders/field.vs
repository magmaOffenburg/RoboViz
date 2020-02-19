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

uniform mat4 modelMatrix;

varying vec2 texcoords;
varying vec4 shadowCoords;

void main()
{
	// texture matrix contains projection and view matrices for the light
	// casting shadow	: todo push modelMatrix onto texture7 instead
	shadowCoords= gl_TextureMatrix[7] * modelMatrix * gl_Vertex;
	texcoords = vec2(gl_MultiTexCoord0);
	
	vec4 color = gl_LightModel.ambient * gl_FrontMaterial.ambient;
	vec3 normal = normalize(gl_NormalMatrix * gl_Normal);
	vec3 lightDir;
	vec4 ambient;
	vec4 diffuse;
	for (int i=0; i<3; i++) {
		lightDir = normalize(vec3(gl_LightSource[i].position));
		diffuse = gl_FrontMaterial.diffuse * gl_LightSource[i].diffuse;
		ambient = gl_FrontMaterial.ambient * gl_LightSource[i].ambient;	
		
		color += ambient + diffuse * max(dot(normal,lightDir),0.0);
	}
	
	gl_FrontColor = color;
	gl_Position = ftransform();
} 