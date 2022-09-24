/*
 *  Copyright 2011 Justin Stoecker
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

package jsgl.io;

import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates Wavefront .obj files from 3D geometry data
 *
 * @author Justin Stoecker
 */
public class ObjExporter
{
	private static final Logger LOGGER = LogManager.getLogger();

	/** Geometry data to be written to translated and written to file */
	public static class Data
	{
		/** Primitive shape type vertices are associated with */
		public enum FaceType
		{
			TriangleList
		}

		float[][] vertices;
		float[][] normals;
		float[][] texCoords;
		int[][] faces;
		FaceType faceType;

		/**
		 * Creates a set of data that will be used for writing an .obj model.
		 *
		 * @param vertices
		 *           - Vertex position coordinates. Each vertex position should be
		 *           an array such that vertices.length is the number of vertices
		 *           in the model; vertices[i][j] refers to the j-th component of
		 *           the i-th vertex.
		 * @param normals
		 *           - Vertex normal vector components. Each vertex normal should
		 *           be an array such that normals.length is the number of normal
		 *           vectors in the model; normals[i][j] refers to the j-th
		 *           component of the i-th normal vector. If set to null, no
		 *           normals are used.
		 * @param texCoords
		 *           - Vertex texture coordinates. Each texture coordinate should
		 *           be an array such that texCoords.length is the number of
		 *           vertices in the model; texCoords[i] refers to the (u,v)
		 *           coordinate pair for the i-th vertex. If set to null, no
		 *           texture coordinates are used.
		 * @param faces
		 *           - Face vertex indices. Each face should be an array of the
		 *           indices of the vertices for that face such that faces.length
		 *           is the number of faces in the model; faces[i][j] refers to
		 *           the j-th vertex index for the i-th face.
		 * @param faceType
		 *           - Primitive type for faces: triangle list, triangle fan,
		 *           quads, etc.
		 * @param zeroIndexed
		 *           - Should be set to true if vertex indices (provided in faces
		 *           parameters) begin with 0. Obj format starts indexing at 1, so
		 *           this should be set to false if the indices do not start at 0.
		 */
		public Data(float[][] vertices, float[][] normals, float[][] texCoords, int[][] faces, FaceType faceType,
				boolean zeroIndexed)
		{
			this.vertices = vertices;
			this.normals = normals;
			this.texCoords = texCoords;
			this.faces = faces;
			this.faceType = faceType;

			if (zeroIndexed)
				for (int i = 0; i < faces.length; i++)
					for (int j = 0; j < faces[i].length; j++)
						faces[i][j]++;
		}
	}

	/** Writes geometry data to file in a thread */
	private static class DataWriteThread extends Thread
	{
		private File file;
		private Data data;

		public DataWriteThread(File file, Data data)
		{
			this.file = file;
			this.data = data;
		}

		@Override
		public void run()
		{
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(file));
				writeToFile(out);
				out.close();
			} catch (IOException e) {
				LOGGER.error("Error writing obj file", e);
			}
		}

		private void writeToFile(BufferedWriter out) throws IOException
		{
			LOGGER.info("Writing .obj file...");

			boolean useTexCoords = data.texCoords != null;
			boolean useNormals = data.normals != null;

			out.write(String.format("# Num. Vertices: %d\n", data.vertices.length));
			out.write(String.format("# Num. Faces (%s): %d\n", data.faceType.toString(), data.faces.length));
			out.write(String.format("# Normals: %b\n", useNormals));
			out.write(String.format("# Tex. Coordinates: %b\n", useTexCoords));

			for (int i = 0; i < data.vertices.length; i++)
				writeVertex(data.vertices[i], out);

			if (useTexCoords)
				for (int i = 0; i < data.texCoords.length; i++)
					writeTexCoord(data.texCoords[i], out);

			if (useNormals)
				for (int i = 0; i < data.normals.length; i++)
					writeNormal(data.normals[i], out);

			for (int i = 0; i < data.faces.length; i++)
				writeFace(data.faces[i], useTexCoords, useNormals, data.faceType, out);

			LOGGER.info(file.getName() + " successfully written.");
		}
	}

	public ObjExporter()
	{
	}

	public void export(File file, Data data) throws IOException
	{
		new DataWriteThread(file, data).start();
	}

	private static void writeVertex(float[] v, BufferedWriter out) throws IOException
	{
		out.write(String.format("v %f %f %f\n", v[0], v[1], v[2]));
	}

	private static void writeNormal(float[] n, BufferedWriter out) throws IOException
	{
		out.write(String.format("vn %f %f %f\n", n[0], n[1], n[2]));
	}

	private static void writeTexCoord(float[] tc, BufferedWriter out) throws IOException
	{
		out.write(String.format("vt %f %f\n", tc[0], tc[1]));
	}

	private static void writeFace(
			int[] f, boolean texCoords, boolean normals, Data.FaceType faceType, BufferedWriter out) throws IOException
	{
		if (texCoords) {
			if (normals) {
				// f v/vt/vn v/vt/vn v/vt/vn
				if (faceType == Data.FaceType.TriangleList) {
					out.write(String.format(
							"f %d/%d/%d %d/%d/%d %d/%d/%d\n", f[0], f[0], f[0], f[1], f[1], f[1], f[2], f[2], f[2]));
				}
			} else {
				// f v/vt v/vt v/vt
				if (faceType == Data.FaceType.TriangleList) {
					out.write(String.format("f %d/%d %d/%d %d/%d\n", f[0], f[0], f[1], f[1], f[2], f[2]));
				}
			}
		} else {
			if (normals) {
				// f v//vn v//vn v//vn
				if (faceType == Data.FaceType.TriangleList) {
					out.write(String.format("f %d//%d %d//%d %d//%d\n", f[0], f[0], f[1], f[1], f[2], f[2]));
				}
			} else {
				// f v v v
				if (faceType == Data.FaceType.TriangleList) {
					out.write(String.format("f %d %d %d\n", f[0], f[1], f[2]));
				}
			}
		}
	}
}
