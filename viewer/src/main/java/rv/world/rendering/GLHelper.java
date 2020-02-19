package rv.world.rendering;

import com.jogamp.opengl.GL2;

public class GLHelper
{
	public static void renderQuad(GL2 gl)
	{
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2f(-1, -1);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2f(1, -1);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2f(1, 1);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2f(-1, 1);
		gl.glEnd();
	}
}
