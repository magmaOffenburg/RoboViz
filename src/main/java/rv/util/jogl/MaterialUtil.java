package rv.util.jogl;

import java.awt.Color;
import jsgl.jogl.model.ObjMaterial;

public class MaterialUtil
{
	public static void setColor(ObjMaterial material, Color color)
	{
		float[] components = new float[] {
				(float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1};
		material.setAmbient(components);
		material.setDiffuse(components);
	}
}
