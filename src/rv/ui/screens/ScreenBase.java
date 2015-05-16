package rv.ui.screens;

import javax.media.opengl.awt.GLCanvas;

public abstract class ScreenBase implements Screen {
    protected boolean visible = true;

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
    }
}
