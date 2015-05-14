package rv.ui.screens;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Viewport;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.world.ISelectable;
import com.jogamp.opengl.util.gl2.GLUT;

public abstract class ViewerScreenBase implements Screen, KeyListener, MouseListener,
        MouseMotionListener, GameState.GameStateChangeListener {
    protected final Viewer           viewer;

    protected final GameStateOverlay gsOverlay;
    private final Field2DOverlay     fieldOverlay;
    protected final List<Screen>     overlays = new ArrayList<>();

    public ViewerScreenBase(Viewer viewer) {
        this.viewer = viewer;
        gsOverlay = new GameStateOverlay(viewer);
        fieldOverlay = new Field2DOverlay(viewer.getWorldModel());
        overlays.add(fieldOverlay);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        for (Screen overlay : overlays)
            overlay.render(gl, glu, glut, vp);
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

        for (Screen overlay : overlays)
            overlay.setEnabled(canvas, enabled);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_Q:
            viewer.shutdown();
            break;
        case KeyEvent.VK_SPACE:
            viewer.getUI().getBallTracker().toggleEnabled();
            break;
        case KeyEvent.VK_F11:
            if (!e.isControlDown())
                viewer.toggleFullScreen();
            break;
        case KeyEvent.VK_F:
            if (e.isControlDown()) {
                viewer.toggleFullScreen();
            } else {
                fieldOverlay.setVisible(!fieldOverlay.isVisible());
            }
            break;
        case KeyEvent.VK_ENTER:
            if (e.isAltDown()) {
                viewer.toggleFullScreen();
            }
            break;
        case KeyEvent.VK_F1:
            if (!e.isControlDown())
                viewer.getUI().getShortcutHelpPanel().showFrame(viewer.getFrame());
            break;
        case KeyEvent.VK_B:
            if (e.isControlDown()) {
                viewer.getWorldModel().setSelectedObject(viewer.getWorldModel().getBall());
            } else {
                bPressed();
            }
            break;
        }
    }

    protected void bPressed() {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            viewer.getUI().getObjectPicker().updatePickRay(viewer.getScreen(), e.getX(), e.getY());

            boolean handled = false;
            ISelectable selectedObject = viewer.getWorldModel().getSelectedObject();
            if (selectedObject != null) {
                handled = selectedObjectClick(selectedObject, e);
            }

            if (!handled) {
                ISelectable newSelection = viewer.getUI().getObjectPicker().pickObject();
                viewer.getWorldModel().setSelectedObject(newSelection);
            }
        }
    }

    protected boolean selectedObjectClick(ISelectable object, MouseEvent e) {
        return false;
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
