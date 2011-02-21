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

package rv.comm.drawing.commands;

import java.nio.ByteBuffer;

import js.io.ByteUtil;

import rv.Viewer;
import rv.comm.drawing.Drawings;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.drawing.shapes.Circle;
import rv.comm.drawing.shapes.Line;
import rv.comm.drawing.shapes.Point;
import rv.comm.drawing.shapes.Polygon;
import rv.comm.drawing.shapes.Shape;
import rv.comm.drawing.shapes.Sphere;

/**
 * Text annotation drawing
 * @author justin
 */
public class DrawAnnotation extends Command {

    public static final int SIMPLE = 0;
    
    private Drawings drawings;
    private Annotation annotation;
    
    public DrawAnnotation(ByteBuffer buf, Viewer viewer) {
        this.drawings = viewer.getDrawings();

        int type = ByteUtil.uValue(buf.get());

        switch (type) {
        case SIMPLE:
            annotation = Annotation.parse(buf);
            break;
        default:
            System.err.println("Unknown annotation : " + type);
            annotation = null;
        }
    }
    
    @Override
    public void execute() {
        drawings.addAnnotation(annotation);
    }
}
