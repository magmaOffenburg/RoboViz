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

package rv.comm.drawing.annotations;

import java.nio.ByteBuffer;

import com.jogamp.opengl.util.awt.TextRenderer;

import js.io.ByteUtil;
import rv.comm.drawing.commands.Command;

/**
 * Text overlay
 * @author justin
 *
 */
public class Annotation {

    private float[] pos;
    private float[] color;
    private String text;
    private String set;
    
    public float[] getPos() {
        return pos;
    }
    
    public float[] getColor() {
        return color;
    }
    
    public String getText() {
        return text;
    }
    
    public String getSet() {
        return set;
    }

    public Annotation(String text, float[] pos, float[] color, String set) {
        this.text = text;
        this.pos = pos;
        this.color = color;
        this.set = set;
    }
    
    public static Annotation parse(ByteBuffer buf) {

        String text = Command.getString(buf);
        float[] pos = Command.readCoords(buf, 3);
        float[] color = Command.readRGB(buf);
        String set = Command.getString(buf);
        
        return new Annotation(text, pos, color, set);
    }
    
    public void render(TextRenderer tr) {
        tr.draw3D(text, pos[0], pos[1], pos[2], 1);
    }
}
