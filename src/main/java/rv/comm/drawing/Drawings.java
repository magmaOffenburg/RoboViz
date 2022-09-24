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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.drawing.shapes.Shape;

/**
 * Contains and manages shape sets
 *
 * @author Justin Stoecker
 */
public class Drawings
{
	/** Event object launched when the list of sets is modified */
	public static class SetListChangeEvent extends EventObject
	{
		private final CopyOnWriteArrayList<BufferedSet<Shape>> shapeSets;
		private final CopyOnWriteArrayList<BufferedSet<Annotation>> annotationSets;

		public CopyOnWriteArrayList<BufferedSet<Shape>> getShapeSets()
		{
			return shapeSets;
		}

		public CopyOnWriteArrayList<BufferedSet<Annotation>> getAnnotationSets()
		{
			return annotationSets;
		}

		public SetListChangeEvent(Drawings source)
		{
			super(source);
			this.shapeSets = source.shapeSets;
			this.annotationSets = source.annotationSets;
		}
	}

	/** Interface for listeners of set list change events */
	public interface ShapeListListener extends EventListener
	{
		void setListChanged(SetListChangeEvent evt);
	}

	private final ArrayList<ShapeListListener> listeners = new ArrayList<>();
	private final HashMap<String, BufferedSet<Shape>> shapeSetListing = new HashMap<>();
	private final HashMap<String, BufferedSet<Annotation>> annotationSetListing = new HashMap<>();
	private final CopyOnWriteArrayList<BufferedSet<Shape>> shapeSets = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<BufferedSet<Annotation>> annotationSets = new CopyOnWriteArrayList<>();
	private boolean changed = false;
	private boolean visible = true;

	public boolean isVisible()
	{
		return visible;
	}

	public void toggle()
	{
		visible = !visible;
	}

	public List<BufferedSet<Annotation>> getAnnotationSets()
	{
		return annotationSets;
	}

	public void addShapeSetListener(ShapeListListener listener)
	{
		listeners.add(listener);
	}

	public void removeShapeSetListener(ShapeListListener listener)
	{
		listeners.remove(listener);
	}

	private void fireShapeChangeListener()
	{
		SetListChangeEvent evt = new SetListChangeEvent(this);
		for (ShapeListListener listener : listeners)
			listener.setListChanged(evt);
	}

	public void addAnnotation(Annotation annotation)
	{
		String setName = annotation.getSet();

		if (annotation instanceof AgentAnnotation) {
			// agent annotations are not added to bufferedsets, so they must
			// be treated specially
			return;
		}

		BufferedSet<Annotation> set = annotationSetListing.get(setName);

		if (set == null) {
			// shape has a set name that hasn't been seen, so create a new set
			BufferedSet<Annotation> newSet = new BufferedSet<>(setName);
			newSet.put(annotation);
			synchronized (this) {
				annotationSets.add(newSet);
			}
			annotationSetListing.put(setName, newSet);
			changed = true;
		} else {
			set.put(annotation);
		}
	}

	public void addShape(Shape shape)
	{
		String setName = shape.getSetName();
		BufferedSet<Shape> set = shapeSetListing.get(setName);

		if (set == null) {
			// shape has a set name that hasn't been seen, so create a new set
			BufferedSet<Shape> newSet = new BufferedSet<>(setName);
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
	public synchronized void clearAllShapeSets()
	{
		shapeSetListing.clear();
		shapeSets.clear();
		annotationSetListing.clear();
		annotationSets.clear();
		fireShapeChangeListener();
	}

	/** Retrieves a shape set by name */
	public BufferedSet<Shape> getShapeSet(String name)
	{
		return shapeSetListing.get(name);
	}

	public BufferedSet<Annotation> getAnnotationSet(String name)
	{
		return annotationSetListing.get(name);
	}

	/**
	 * Swaps buffers on set with specified name; if name is empty, all buffers are swapped.
	 */
	public void swapBuffers(String name)
	{
		if (name.isEmpty()) {
			for (BufferedSet<Shape> set : shapeSets)
				set.swapBuffers();
			for (BufferedSet<Annotation> set : annotationSets)
				set.swapBuffers();
		} else {
			for (BufferedSet<Shape> p : shapeSets)
				if (p.getName().startsWith(name))
					p.swapBuffers();
			for (BufferedSet<Annotation> p : annotationSets)
				if (p.getName().startsWith(name))
					p.swapBuffers();
		}
	}

	public synchronized void render(GL2 gl, GLUT glut)
	{
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		gl.glEnable(GL.GL_BLEND);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);

		for (BufferedSet<Shape> setBuffer : shapeSets) {
			if (setBuffer.isVisible()) {
				ArrayList<Shape> shapes = setBuffer.getFrontSet();
				for (Shape s : shapes) {
					if (s != null) {
						s.draw(gl);
					}
				}
			}
		}

		gl.glPopAttrib();
	}

	public synchronized void update()
	{
		if (changed) {
			fireShapeChangeListener();
			changed = false;
		}
	}
}
