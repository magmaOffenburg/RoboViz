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
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.drawing.annotations.StandardAnnotation;
import rv.world.objects.Agent;

/**
 * Text annotation drawing
 * 
 * @author justin
 */
public class DrawAnnotation extends Command {

    public static final int STANDARD    = 0;
    public static final int AGENT_ADD   = 1;
    public static final int AGENT_CLEAR = 2;

    private final Drawings  drawings;
    private Annotation      annotation;

    public DrawAnnotation(ByteBuffer buf, Viewer viewer) {
        this.drawings = viewer.getDrawings();

        int type = ByteUtil.uValue(buf.get());

        switch (type) {
        case STANDARD:
            annotation = StandardAnnotation.parse(buf);
            break;
        case AGENT_ADD:
            annotation = AgentAnnotation.parse(buf, viewer.getWorldModel());
            break;
        case AGENT_CLEAR:
            Agent agent = Command.readAgent(buf, viewer.getWorldModel());
            if (agent != null)
                agent.setAnnotation(null);
            break;
        default:
            System.err.println("Unknown annotation : " + type);
            annotation = null;
        }
    }

    @Override
    public void execute() {
        if (annotation != null)
            drawings.addAnnotation(annotation);
    }
}
