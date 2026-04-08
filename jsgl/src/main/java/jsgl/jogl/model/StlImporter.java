package jsgl.jogl.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Imports an STL model as a standard Mesh object.
 *
 * @author Hannes Braun
 */
public class StlImporter extends MeshImporter
{
	private static final Logger LOGGER = LogManager.getLogger();

	public StlImporter(String modelPath)
	{
		super(modelPath);
	}

	public StlImporter(String modelPath, String materialPath)
	{
		super(modelPath, materialPath);
	}

	@Override
	public Mesh loadMesh(InputStream is) throws IOException
	{
		return loadMesh(new BufferedInputStream(is));
	}

	public Mesh loadMesh(BufferedInputStream bis) throws IOException
	{
		Mesh mesh = new Mesh();
		MeshPart part = new MeshPart();

		Vec3f min = new Vec3f(Float.POSITIVE_INFINITY);
		Vec3f max = new Vec3f(Float.NEGATIVE_INFINITY);

		var header = new byte[80];
		int n = bis.read(header, 0, header.length);
		if (n == -1) {
			LOGGER.error("missing STL header");
			return null;
		}

		final var numTriBuf = new byte[4];
		n = bis.read(numTriBuf, 0, numTriBuf.length);
		if (n == -1) {
			LOGGER.error("missing number of triangles in STL file");
			return null;
		}
		final int numberTriangles = ByteBuffer.wrap(numTriBuf).order(ByteOrder.LITTLE_ENDIAN).getInt();

		ByteBuffer triBuf = ByteBuffer.allocate(50).order(ByteOrder.LITTLE_ENDIAN);

		for (int i = 0; i < numberTriangles; i++) {
			n = bis.read(triBuf.array(), 0, triBuf.array().length);
			if (n == -1) {
				LOGGER.error("unexpected EOF in STL file");
				return null;
			}

			final float[] normalVector = new float[] {triBuf.getFloat(0), triBuf.getFloat(4), triBuf.getFloat(8)};
			final float[][] v = new float[3][];
			v[0] = new float[] {triBuf.getFloat(12), triBuf.getFloat(16), triBuf.getFloat(20)};
			v[1] = new float[] {triBuf.getFloat(24), triBuf.getFloat(28), triBuf.getFloat(32)};
			v[2] = new float[] {triBuf.getFloat(36), triBuf.getFloat(40), triBuf.getFloat(44)};
			// final short attrByteCount = triBuf.getShort(48);

			for (int j = 0; j < v.length; j++) {
				final var vertex = v[j];
				if (vertex[0] > max.x)
					max.x = vertex[0];
				if (vertex[1] > max.y)
					max.y = vertex[1];
				if (vertex[2] > max.z)
					max.z = vertex[2];
				if (vertex[0] < min.x)
					min.x = vertex[0];
				if (vertex[1] < min.y)
					min.y = vertex[1];
				if (vertex[2] < min.z)
					min.z = vertex[2];

				mesh.addVertex(new MeshVertex(vertex, normalVector, null));
			}

			final var verticesSize = mesh.getVertices().size();
			part.addFace(new MeshFace(new int[] {verticesSize - 3, verticesSize - 2, verticesSize - 1}));
		}

		part.setMaterial(new ObjMaterial("Default"));
		mesh.addPart(part);
		mesh.setBounds(new BoundingBox(min, max));

		return mesh;
	}
}
