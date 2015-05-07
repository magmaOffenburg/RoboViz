package rv.ui.screens;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.media.opengl.awt.GLCanvas;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.world.ISelectable;
import rv.world.WorldModel;

public abstract class ViewerScreenBase implements Screen, KeyListener, MouseListener,
        MouseMotionListener, GameState.GameStateChangeListener {
    protected Viewer viewer;

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

    private void toggleSelection(ISelectable selectable) {
        if (selectable.isSelected())
            changeSelection(null);
        else
            changeSelection(selectable);
    }

    protected void changeSelection(ISelectable newSelection) {
        WorldModel worldModel = viewer.getWorldModel();
        if (newSelection != null) {
            if (worldModel.getSelectedObject() != null)
                worldModel.getSelectedObject().setSelected(false);
            worldModel.setSelectedObject(newSelection);
            worldModel.getSelectedObject().setSelected(true);
        } else {
            if (worldModel.getSelectedObject() != null)
                worldModel.getSelectedObject().setSelected(false);
            worldModel.setSelectedObject(null);
        }
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
            viewer.toggleFullScreen();
            break;
        case KeyEvent.VK_F:
            if (e.isControlDown()) {
                viewer.toggleFullScreen();
            } else {
                fPressed();
            }
            break;
        case KeyEvent.VK_ENTER:
            if (e.isAltDown()) {
                viewer.toggleFullScreen();
            }
            break;
        case KeyEvent.VK_F1:
            viewer.getUI().getShortcutHelpPanel().showFrame();
            break;
        case KeyEvent.VK_B:
            if (e.isControlDown()) {
                toggleSelection(viewer.getWorldModel().getBall());
            } else {
                bPressed();
            }
            break;
        }
    }

    protected void fPressed() {

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
                changeSelection(newSelection);
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
