package rv.ui.screens;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.media.opengl.awt.GLCanvas;
import rv.Viewer;
import rv.comm.rcssserver.GameState;

public abstract class ViewerScreenBase implements Screen, KeyListener, MouseListener,
        MouseMotionListener, GameState.GameStateChangeListener {
    protected Viewer viewer;

    private boolean  control = false;
    private boolean  alt     = false;

    public ViewerScreenBase(Viewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled) {
            canvas.addKeyListener(this);
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
            viewer.getUI().getCameraControl().attachToCanvas(canvas);
        } else {
            canvas.removeKeyListener(this);
            canvas.removeMouseListener(this);
            canvas.removeMouseMotionListener(this);
            viewer.getUI().getCameraControl().detachFromCanvas(canvas);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_CONTROL:
            control = true;
            break;
        case KeyEvent.VK_ALT:
            alt = true;
            break;
        case KeyEvent.VK_Q:
            viewer.shutdown();
            break;
        case KeyEvent.VK_SPACE:
            viewer.getUI().getBallTracker().toggleEnabled();
            break;
        case KeyEvent.VK_F11:
            viewer.toggleFullScreen();
            break;
        case KeyEvent.VK_F:
            if (control) {
                viewer.toggleFullScreen();
            } else {
                fPressed();
            }
            break;
        case KeyEvent.VK_ENTER:
            if (alt) {
                viewer.toggleFullScreen();
            }
            break;
        }
    }

    protected void fPressed() {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
        case KeyEvent.VK_CONTROL:
            control = false;
            break;
        case KeyEvent.VK_ALT:
            alt = false;
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void gsTimeChanged(GameState gs) {

    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {

    }

    @Override
    public void gsPlayStateChanged(GameState gs) {

    }
}
