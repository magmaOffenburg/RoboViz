package jsgl.jogl.model;

import jsgl.math.BoundingBox;
import jsgl.math.geom.GeodesicSphere;
import jsgl.math.vector.Vec3f;

public class StdMeshImporter
{
	public static final String UNIT_SPHERE_NAME = "StdUnitSphere";
	public static final String UNIT_BOX_NAME = "StdUnitBox";
	public static final String CAPSULE_NAME = "StdCapsule";
	public static final String UNIT_CYLINDER_NAME = "StdUnitCylinder";

	public static Mesh generate(String name)
	{
		return switch (name) {
			case UNIT_SPHERE_NAME -> generateUnitSphere();
			case UNIT_BOX_NAME -> generateUnitBox();
			case CAPSULE_NAME -> generateCapsule();
			case UNIT_CYLINDER_NAME -> generateUnitCylinder();
			default -> null;
		};
	}

	private static Mesh generateUnitSphere()
	{
		final var sphere = new GeodesicSphere(1, 4);
		final Mesh mesh = new Mesh();
		final MeshPart part = new MeshPart();
		for (var v : sphere.getVerts()) {
			mesh.addVertex(new MeshVertex(v, v, null));
		}
		for (var t : sphere.getTriangles()) {
			part.addFace(new MeshFace(t));
		}
		part.setMaterial(new ObjMaterial("Default"));
		mesh.addPart(part);
		mesh.setBounds(new BoundingBox(new Vec3f(-0.5f, -0.5f, -0.5f), new Vec3f(0.5f, 0.5f, 0.5f)));
		return mesh;
	}

	private static Mesh generateUnitBox()
	{
		final Mesh mesh = new Mesh();
		final MeshPart part = new MeshPart();

		final var n1 = new float[] {0, 0, -1};
		final var n2 = new float[] {0, -1, 0};
		final var n3 = new float[] {1, 0, 0};
		final var n4 = new float[] {0, 1, 0};
		final var n5 = new float[] {-1, 0, 0};
		final var n6 = new float[] {0, 0, 1};

		final var v1 = new MeshVertex(new float[] {-0.5f, -0.5f, -0.5f}, n1, null);
		final var v2 = new MeshVertex(new float[] {0.5f, -0.5f, -0.5f}, n1, null);
		final var v3 = new MeshVertex(new float[] {0.5f, 0.5f, -0.5f}, n1, null);
		final var v4 = new MeshVertex(new float[] {-0.5f, 0.5f, -0.5f}, n1, null);
		final var v5 = new MeshVertex(new float[] {-0.5f, -0.5f, 0.5f}, n6, null);
		final var v6 = new MeshVertex(new float[] {0.5f, -0.5f, 0.5f}, n6, null);
		final var v7 = new MeshVertex(new float[] {0.5f, 0.5f, 0.5f}, n6, null);
		final var v8 = new MeshVertex(new float[] {-0.5f, 0.5f, 0.5f}, n6, null);
		final var v9 = new MeshVertex(new float[] {-0.5f, -0.5f, -0.5f}, n2, null);
		final var v10 = new MeshVertex(new float[] {0.5f, -0.5f, -0.5f}, n2, null);
		final var v11 = new MeshVertex(new float[] {0.5f, 0.5f, -0.5f}, n4, null);
		final var v12 = new MeshVertex(new float[] {-0.5f, 0.5f, -0.5f}, n4, null);
		final var v13 = new MeshVertex(new float[] {-0.5f, -0.5f, 0.5f}, n2, null);
		final var v14 = new MeshVertex(new float[] {0.5f, -0.5f, 0.5f}, n2, null);
		final var v15 = new MeshVertex(new float[] {0.5f, 0.5f, 0.5f}, n4, null);
		final var v16 = new MeshVertex(new float[] {-0.5f, 0.5f, 0.5f}, n4, null);
		final var v17 = new MeshVertex(new float[] {-0.5f, -0.5f, -0.5f}, n5, null);
		final var v18 = new MeshVertex(new float[] {0.5f, -0.5f, -0.5f}, n3, null);
		final var v19 = new MeshVertex(new float[] {0.5f, 0.5f, -0.5f}, n3, null);
		final var v20 = new MeshVertex(new float[] {-0.5f, 0.5f, -0.5f}, n5, null);
		final var v21 = new MeshVertex(new float[] {-0.5f, -0.5f, 0.5f}, n5, null);
		final var v22 = new MeshVertex(new float[] {0.5f, -0.5f, 0.5f}, n3, null);
		final var v23 = new MeshVertex(new float[] {0.5f, 0.5f, 0.5f}, n3, null);
		final var v24 = new MeshVertex(new float[] {-0.5f, 0.5f, 0.5f}, n5, null);

		mesh.addVertex(v1);
		mesh.addVertex(v2);
		mesh.addVertex(v3);
		mesh.addVertex(v4);
		mesh.addVertex(v5);
		mesh.addVertex(v6);
		mesh.addVertex(v7);
		mesh.addVertex(v8);
		mesh.addVertex(v9);
		mesh.addVertex(v10);
		mesh.addVertex(v11);
		mesh.addVertex(v12);
		mesh.addVertex(v13);
		mesh.addVertex(v14);
		mesh.addVertex(v15);
		mesh.addVertex(v16);
		mesh.addVertex(v17);
		mesh.addVertex(v18);
		mesh.addVertex(v19);
		mesh.addVertex(v20);
		mesh.addVertex(v21);
		mesh.addVertex(v22);
		mesh.addVertex(v23);
		mesh.addVertex(v24);

		part.addFace(new MeshFace(new int[] {0, 1, 2}));
		part.addFace(new MeshFace(new int[] {2, 3, 0}));
		part.addFace(new MeshFace(new int[] {4, 5, 6}));
		part.addFace(new MeshFace(new int[] {6, 7, 4}));
		part.addFace(new MeshFace(new int[] {8, 9, 13}));
		part.addFace(new MeshFace(new int[] {13, 12, 8}));
		part.addFace(new MeshFace(new int[] {17, 18, 22}));
		part.addFace(new MeshFace(new int[] {22, 21, 17}));
		part.addFace(new MeshFace(new int[] {10, 11, 15}));
		part.addFace(new MeshFace(new int[] {15, 14, 10}));
		part.addFace(new MeshFace(new int[] {19, 16, 20}));
		part.addFace(new MeshFace(new int[] {20, 23, 19}));

		part.setMaterial(new ObjMaterial("Default"));
		mesh.addPart(part);
		mesh.setBounds(new BoundingBox(new Vec3f(-0.5f, -0.5f, -0.5f), new Vec3f(0.5f, 0.5f, 0.5f)));

		return mesh;
	}

	private static Mesh generateCapsule()
	{
		// TODO
		return null;
	}

	private static Mesh generateUnitCylinder()
	{
		final Mesh mesh = new Mesh();
		final MeshPart part = new MeshPart();

		final var nTop = new float[] {0, 0, 1};
		final var nBottom = new float[] {0, 0, -1};
		final var topMid = new MeshVertex(new float[] {0, 0, 0.5f}, nTop, null);
		mesh.addVertex(topMid);
		final var bottomMid = new MeshVertex(new float[] {0, 0, -0.5f}, nBottom, null);
		mesh.addVertex(bottomMid);

		final int segments = 50;
		for (int i = 0; i < segments; i++) {
			final double angle1 = i * (2 * Math.PI / segments);
			final double angle2 = (i + 1) * (2 * Math.PI / segments);
			final float x1 = (float) Math.cos(angle1);
			final float y1 = (float) Math.sin(angle1);
			final float x2 = (float) Math.cos(angle2);
			final float y2 = (float) Math.sin(angle2);

			// Top
			final var top1 = new MeshVertex(new float[] {x1, y1, 0.5f}, nTop, null);
			final var top2 = new MeshVertex(new float[] {x2, y2, 0.5f}, nTop, null);
			mesh.addVertex(top1);
			mesh.addVertex(top2);
			part.addFace(new MeshFace(new int[] {0, mesh.getVertices().size() - 1, mesh.getVertices().size() - 2}));

			// Bottom
			final var bottom1 = new MeshVertex(new float[] {x1, y1, -0.5f}, nBottom, null);
			final var bottom2 = new MeshVertex(new float[] {x2, y2, -0.5f}, nBottom, null);
			mesh.addVertex(bottom1);
			mesh.addVertex(bottom2);
			part.addFace(new MeshFace(new int[] {1, mesh.getVertices().size() - 1, mesh.getVertices().size() - 2}));

			// Side
			final var side1 = new MeshVertex(new float[] {x1, y1, 0.5f}, new float[] {x1, y1, 0}, null);
			final var side2 = new MeshVertex(new float[] {x2, y2, 0.5f}, new float[] {x2, y2, 0}, null);
			final var side3 = new MeshVertex(new float[] {x1, y1, -0.5f}, new float[] {x1, y1, 0}, null);
			final var side4 = new MeshVertex(new float[] {x2, y2, -0.5f}, new float[] {x2, y2, 0}, null);
			mesh.addVertex(side1);
			mesh.addVertex(side2);
			mesh.addVertex(side3);
			mesh.addVertex(side4);
			part.addFace(new MeshFace(new int[] {
					mesh.getVertices().size() - 4, mesh.getVertices().size() - 3, mesh.getVertices().size() - 2}));
			part.addFace(new MeshFace(new int[] {
					mesh.getVertices().size() - 3, mesh.getVertices().size() - 1, mesh.getVertices().size() - 2}));
		}

		part.setMaterial(new ObjMaterial("Default"));
		mesh.addPart(part);
		mesh.setBounds(new BoundingBox(new Vec3f(-1, -1, -0.5f), new Vec3f(1, 1, 0.5f)));

		return mesh;
	}
}
