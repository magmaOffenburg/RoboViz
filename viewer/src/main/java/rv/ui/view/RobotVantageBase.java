package rv.ui.view;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import jsgl.jogl.view.Camera3D;
import jsgl.jogl.view.Viewport;
import jsgl.math.vector.Matrix;
import rv.world.objects.Agent;

public abstract class RobotVantageBase extends Camera3D implements Agent.ChangeListener
{
	protected final Agent agent;

	protected RobotVantageBase(Agent agent, float fovY)
	{
		super(agent.getHeadCenter(), 0.1f, 300);
		this.fovY = fovY;
		this.agent = agent;
		agent.addChangeListener(this);
	}

	@Override
	public void apply(GL2 gl, GLU glu, Viewport vp)
	{
		if (viewMatrix == null) {
			return;
		}
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fovY, getAspect(vp), near, far);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLoadMatrixd(viewMatrix.wrap());
	}

	public void detach()
	{
		agent.removeChangeListener(this);
	}

	public void transformChanged(Matrix t)
	{
		updateView();
	}

	@Override
	public void addListeners(GLCanvas canvas)
	{
	}

	@Override
	public void removeListeners(GLCanvas canvas)
	{
	}

	public Agent getAgent()
	{
		return agent;
	}

	protected float getAspect(Viewport vp)
	{
		return vp.getAspect();
	}

	public void setFOV(int fovY)
	{
		this.fovY = fovY;
	}
}
