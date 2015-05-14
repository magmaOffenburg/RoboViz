package rv.ui.view;

import javax.media.opengl.awt.GLCanvas;
import js.jogl.view.Camera3D;
import js.math.vector.Matrix;
import rv.world.objects.Agent;

public abstract class RobotVantageBase extends Camera3D implements Agent.ChangeListener {
    protected final Agent agent;

    protected RobotVantageBase(Agent agent, float fovY) {
        super(agent.getHeadCenter(), 0.1f, 300);
        this.fovY = fovY;
        this.agent = agent;
        agent.addChangeListener(this);
    }

    public void detach() {
        agent.removeChangeListener(this);
    }

    public void transformChanged(Matrix t) {
        updateView();
    }

    @Override
    public void addListeners(GLCanvas canvas) {
    }

    @Override
    public void removeListeners(GLCanvas canvas) {
    }

    public Agent getAgent() {
        return agent;
    }
}
