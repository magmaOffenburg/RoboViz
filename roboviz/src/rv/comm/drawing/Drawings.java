/*
 *  Copyright 2011 RoboViz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package rv.comm.drawing;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import js.jogl.Texture2D;

import rv.comm.drawing.shapes.Shape;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Contains and manages shape sets
 * 
 * @author Justin Stoecker
 */
public class Drawings {

    /** Event object launched when the list of sets is modified */
    public class SetListChangeEvent extends EventObject {

        private ArrayList<ShapeSet> sets;

        public ArrayList<ShapeSet> getSets() {
            return sets;
        }

        public SetListChangeEvent(Drawings source) {
            super(source);
            this.sets = source.shapeSets;
        }
    }

    /** Interface for listeners of set list change events */
    public interface ShapeListListener extends EventListener {
        public void setListChanged(SetListChangeEvent evt);
    }

    private ArrayList<ShapeListListener> listeners       = new ArrayList<Drawings.ShapeListListener>();
    private HashMap<String, ShapeSet>    shapeSetListing = new HashMap<String, ShapeSet>();
    private ArrayList<ShapeSet>          shapeSets       = new ArrayList<ShapeSet>();
    private boolean                      changed         = false;

    private boolean                      visible         = true;

    public boolean isVisible() {
        return visible;
    }

    public void toggle() {
        visible = !visible;
    }

    public void addShapeSetListener(ShapeListListener listener) {
        listeners.add(listener);
    }

    public void removeShapeSetListener(ShapeListListener listener) {
        listeners.remove(listener);
    }

    private void fireShapeChangeListener() {
        SetListChangeEvent evt = new SetListChangeEvent(this);
        for (ShapeListListener listener : listeners)
            listener.setListChanged(evt);
    }

    public void addShape(Shape shape) {
        String setName = shape.getSetName();
        ShapeSet set = shapeSetListing.get(setName);

        if (set == null) {
            // shape has a set name that hasn't been seen, so create a new set
            ShapeSet newSet = new ShapeSet(setName);
            newSet.put(shape);
            synchronized (this) {
                shapeSets.add(newSet);
            }
            shapeSetListing.put(setName, newSet);
            changed = true;
        } else {
            set.put(shape);
        }
    }

    /** Removes all known shape sets */
    public synchronized void clearAllShapeSets() {
        shapeSetListing.clear();
        shapeSets.clear();
        fireShapeChangeListener();
    }

    /** Retrieves a shape set by name */
    public ShapeSet getShapeSet(String name) {
        return shapeSetListing.get(name);
    }

    /**
     * Swaps buffers on set with specified name; if name is empty, all buffers
     * are swapped.
     */
    public void swapBuffers(String name) {
        if (name.isEmpty()) {
            for (ShapeSet set : shapeSets)
                set.swapBuffers();
        } else {
            for (ShapeSet p : shapeSets)
                if (p.getName().startsWith(name))
                    p.swapBuffers();
        }
    }

    public synchronized void render(GL2 gl, GLUT glut) {
        gl.glPushAttrib(GL2.GL_ENABLE_BIT);
        gl.glEnable(GL.GL_BLEND);
        gl.glEnable(GL.GL_DEPTH_TEST);
        // gl.glEnable(GL.GL_LINE_SMOOTH);
        // gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_LIGHTING);

        for (ShapeSet set : shapeSets) {
            if (set.isVisible()) {
                ArrayList<Shape> shapes = set.getShapes();
                for (Shape s : shapes) {
                    if (s != null) {
                        s.draw(gl);
                    }
                }
            }
        }
        gl.glPopAttrib();

    }

    public synchronized void update() {
        if (changed) {
            fireShapeChangeListener();
            changed = false;
        }
    }
}
