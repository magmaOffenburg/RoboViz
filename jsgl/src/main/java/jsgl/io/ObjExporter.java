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

    public enum FaceWriterType {
        TEX_COORDS_NORMALS,
        TEX_COORDS,
        NORMALS,
        DEFAULT
    }

    public static class Data
    {
        public enum FaceType
        {
            TriangleList
        }

        float[][] vertices;
        float[][] normals;
        float[][] texCoords;
        int[][] faces;
        FaceType faceType;

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
        FaceWriterType writerType = getWriterType(texCoords, normals, faceType);
        writeFaceByType(writerType, f, out);
    }

    private static FaceWriterType getWriterType(boolean texCoords, boolean normals, Data.FaceType faceType) {
        // Determine the appropriate FaceWriterType based on conditions
        if (texCoords && normals && faceType == Data.FaceType.TriangleList) {
            return FaceWriterType.TEX_COORDS_NORMALS;
        } else if (texCoords && faceType == Data.FaceType.TriangleList) {
            return FaceWriterType.TEX_COORDS;
        } else if (normals && faceType == Data.FaceType.TriangleList) {
            return FaceWriterType.NORMALS;
        } else if (faceType == Data.FaceType.TriangleList) {
            return FaceWriterType.DEFAULT;
        } else {
            return FaceWriterType.DEFAULT;
        }
    }

    private static void writeFaceByType(FaceWriterType writerType, int[] f, BufferedWriter out) throws IOException {
        switch (writerType) {
            case TEX_COORDS_NORMALS:
                writeFaceWithTexCoordsAndNormals(f, out);
                break;
            case TEX_COORDS:
                writeFaceWithTexCoords(f, out);
                break;
            case NORMALS:
                writeFaceWithNormals(f, out);
                break;
            case DEFAULT:
                writeFaceDefault(f, out);
                break;
        }
    }

    private static void writeFaceWithTexCoordsAndNormals(int[] f, BufferedWriter out) throws IOException {
        out.write(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", f[0], f[0], f[0], f[1], f[1], f[1], f[2], f[2], f[2]));
    }

    private static void writeFaceWithTexCoords(int[] f, BufferedWriter out) throws IOException {
        out.write(String.format("f %d/%d %d/%d %d/%d\n", f[0], f[0], f[1], f[1], f[2], f[2]));
    }

    private static void writeFaceWithNormals(int[] f, BufferedWriter out) throws IOException {
        out.write(String.format("f %d//%d %d//%d %d//%d\n", f[0], f[0], f[1], f[1], f[2], f[2]));
    }

    private static void writeFaceDefault(int[] f, BufferedWriter out) throws IOException {
        out.write(String.format("f %d %d %d\n", f[0], f[1], f[2]));
    }
}
