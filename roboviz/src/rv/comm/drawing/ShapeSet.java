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

import rv.comm.drawing.shapes.Shape;

/**
 * A container class that allows shapes to be rendered and added simultaneously.
 * This class contains two buffers for shape objects: the "front" and "back"
 * buffers. The back buffer is where all newly received shapes are added. This
 * buffer continues to grow until swapBuffers() is called. During the swap, the
 * two buffers exchange roles and the new back buffer has its contents cleared. <br>
 * <br>
 * Typically swapBuffers() is called when the thread receiving draw packets gets
 * a "clear" packet marking the start of a batch of drawings. This allows all
 * the shapes from the last batch to be read without a concurrency error caused
 * by new shapes being added to the same buffer. The methods to read the last
 * batch of shapes and swap the buffers are marked synchronized to block other
 * threads from calling these methods at the same time.
 * 
 * @author Justin Stoecker
 */
public class ShapeSet {

    private boolean            visible  = true;
    private final String       name;
    private int                frontBuf = 0;
    private int                backBuf  = 1;
    @SuppressWarnings("unchecked")
    private ArrayList<Shape>[] buffers  = new ArrayList[2];

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public ShapeSet(String name) {
        this.name = name;
        buffers[0] = new ArrayList<Shape>();
        buffers[1] = new ArrayList<Shape>();
    }

    /**
     * Gets a copy of the previous batch of shapes.
     */
    public synchronized ArrayList<Shape> getShapes() {
        // a copy is made because we don't want to return a reference to the
        // current front buffer; this buffer may be swapped while the thread
        // is still using the shapes in it (such as rendering).
        ArrayList<Shape> shapes = buffers[frontBuf];
        ArrayList<Shape> shapesCopy = new ArrayList<Shape>(shapes.size());
        shapesCopy.addAll(shapes);

        return shapesCopy;
    }

    /**
     * Adds a shape to the current batch of shapes. This method is not
     * synchronized as the only thread that should be using it is the one that
     * is also responsible for swapping the buffers.
     */
    public void put(Shape s) {
        buffers[backBuf].add(s);
    }

    /**
     * Swaps the front and back buffers and clears the new back buffer.
     */
    public synchronized void swapBuffers() {
        int temp = backBuf;
        backBuf = frontBuf;
        frontBuf = temp;
        buffers[backBuf].clear();
    }
}
