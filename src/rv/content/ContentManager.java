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

package rv.content;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import js.jogl.GLInfo;
import js.jogl.ShaderProgram;
import js.jogl.Texture2D;
import js.jogl.model.Mesh;
import js.jogl.model.MeshPart;
import js.jogl.model.ObjMaterial;
import js.jogl.model.ObjMaterialLibrary;
import js.jogl.model.ObjMeshImporter;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Objects;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.scenegraph.Node;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.SceneGraph.SceneGraphListener;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.ui.DebugInfo;

/**
 * Loads shaders and meshes used in scene graph.
 * 
 * @author justin
 */
public class ContentManager implements SceneGraphListener, GameState.GameStateChangeListener {

    public static final String CONTENT_ROOT  = "resources/";
    public static final String MODEL_ROOT    = CONTENT_ROOT + "models/";
    public static final String TEXTURE_ROOT  = CONTENT_ROOT + "textures/";
    public static final String MATERIAL_ROOT = CONTENT_ROOT + "materials/";

    private class ModelLoader extends Thread {

        private Model model;

        public ModelLoader(Model model) {
            this.model = model;
        }

        public void run() {
            model.readMeshData(ContentManager.this);
            synchronized (ContentManager.this) {
                modelsToInitialize.add(model);
            }
        }
    }

    private final Configuration.TeamColors config;

    // GLU glu = new GLU();
    // GLUT glut = new GLUT();
    private Mesh.RenderMode                meshRenderMode     = Mesh.RenderMode.IMMEDIATE;
    private Texture2D                      whiteTexture;
    public static Texture2D                selectionTexture;
    private List<Model>                    modelsToInitialize = new ArrayList<Model>();
    private List<Model>                    models             = new ArrayList<Model>();
    private ObjMaterialLibrary             naoMaterialLib;

    public Texture2D getSelectionTexture() {
        return selectionTexture;
    }

    public Texture2D getWhiteTexture() {
        return whiteTexture;
    }

    public Mesh.RenderMode getMeshRenderMode() {
        return meshRenderMode;
    }

    public ObjMaterial getMaterial(String name) {
        for (ObjMaterial mat : naoMaterialLib.getMaterials())
            if (mat.getName().equals(name))
                return mat;
        return null;
    }

    /**
     * Retrieves model from content manager. If model is not found in set of loaded models, it is
     * added to a queue and loaded.
     */
    public synchronized Model getModel(String name) {
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            if (model.getName().equals(name)) {
                return model;
            }
        }

        // The requested mesh was not found, so we create a new one and start
        // loading it in a thread.
        Model model = new Model(name);
        models.add(model);
        new ModelLoader(model).start();

        return model;
    }

    public ContentManager(Configuration.TeamColors config) {
        this.config = config;
    }

    public synchronized void update(GL2 gl) {
        // meshes need a current OpenGL context to finish initializing, so this
        // update pass checks all models that are waiting to initialize and then
        // clears the list

        if (modelsToInitialize.size() == 0)
            return;

        for (Model m : modelsToInitialize)
            m.init(gl, meshRenderMode);

        modelsToInitialize.clear();
    }

    public static void renderSelection(GL2 gl, Vec3f p, float r, float[] color) {
        // gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
        // gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3fv(color, 0);
        ContentManager.selectionTexture.bind(gl);
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(p.x - r, 0, p.z - r);
        gl.glTexCoord2f(1, 0);
        gl.glVertex3f(p.x - r, 0, p.z + r);
        gl.glTexCoord2f(1, 1);
        gl.glVertex3f(p.x + r, 0, p.z + r);
        gl.glTexCoord2f(0, 1);
        gl.glVertex3f(p.x + r, 0, p.z - r);
        gl.glEnd();
        Texture2D.unbind(gl);
        // gl.glPopAttrib();
    }

    public boolean init(GLAutoDrawable drawable, GLInfo glInfo) {

        // use VBOs if they are supported
        if (glInfo.extSupported("GL_ARB_vertex_buffer_object")) {
            meshRenderMode = Mesh.RenderMode.VBO;
        } else {
            // display lists would be preferred, but since the Nao model is
            // shared and the materials change it would require recompilation
            // every render pass
            meshRenderMode = Mesh.RenderMode.VERTEX_ARRAYS;
        }

        whiteTexture = loadTexture(drawable.getGL(), "white.png");
        if (whiteTexture == null)
            return false;
        selectionTexture = loadTexture(drawable.getGL(), "selection.png");
        if (selectionTexture == null)
            return false;

        // load nao materials
        naoMaterialLib = new ObjMaterialLibrary();
        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream("resources/materials/nao.mtl");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            naoMaterialLib.load(br, "resources/textures/", cl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ObjMaterial m : naoMaterialLib.getMaterials())
            m.init(drawable.getGL().getGL2());

        return true;
    }

    public Texture2D loadTexture(GL gl, String name) {
        BufferedImage img;
        try {
            img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
                    "resources/textures/" + name));
            return Texture2D.loadTex(gl, img);
        } catch (IOException e) {
            System.err.println("Error loading texture: " + name);
        } catch (IllegalArgumentException e) {
            System.err.println("Error loading texture: " + name);
        }
        return null;
    }

    public Mesh loadMesh(String name) {
        // System.out.println("Loading " + name);
        String modelPath = "resources/models/";
        String texturePath = "resources/textures/";
        String materialPath = "resources/materials/";
        ObjMeshImporter importer = new ObjMeshImporter(modelPath, materialPath, texturePath);
        ClassLoader cl = this.getClass().getClassLoader();

        importer.setClassLoader(cl);
        InputStream is = cl.getResourceAsStream(modelPath + name);
        Mesh mesh = null;
        try {
            mesh = importer.loadMesh(new BufferedReader(new InputStreamReader(is)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // this is necessary for the shader to blend meshes that have textures
        // for some parts and materials for others
        for (MeshPart p : mesh.getParts()) {
            if (p.getMaterial() instanceof ObjMaterial) {
                ObjMaterial mat = (ObjMaterial) p.getMaterial();
                if (mat.getTexture() == null)
                    mat.setTexture(whiteTexture, false);
            }
        }

        return mesh;
    }

    public void dispose(GL gl) {
        if (whiteTexture != null)
            whiteTexture.dispose(gl);
        if (selectionTexture != null)
            selectionTexture.dispose(gl);
        for (Model model : models)
            model.dispose(gl);
    }

    public ShaderProgram loadShader(GL2 gl, String name) {
        String v = "shaders/" + name + ".vs";
        String f = "shaders/" + name + ".fs";
        ClassLoader cl = this.getClass().getClassLoader();
        return ShaderProgram.create(gl, v, f, cl);
    }

    @Override
    public void newSceneGraph(SceneGraph sg) {
        checkForMeshes(sg.getRoot());
    }

    private void checkForMeshes(Node node) {
        if (node instanceof StaticMeshNode) {
            StaticMeshNode meshNode = (StaticMeshNode) node;
            getModel(meshNode.getName());
        }

        if (node.getChildren() != null) {
            for (int i = 0; i < node.getChildren().size(); i++)
                checkForMeshes(node.getChildren().get(i));
        }
    }

    @Override
    public void updatedSceneGraph(SceneGraph sg) {
    }

    private String teamNameLeft;
    private String teamNameRight;

    @Override
    public void gsPlayStateChanged(GameState gs) {
        // if team name changed, update the materials
        String teamNameLeft = gs.getTeamLeft();
        if (!Objects.equals(teamNameLeft, this.teamNameLeft)) {
            updateTeamColor(teamNameLeft, "matLeft", new float[] { 0.15f, 0.15f, 1.0f });
            this.teamNameLeft = teamNameLeft;
        }
        String teamNameRight = gs.getTeamRight();
        if (!Objects.equals(teamNameRight, this.teamNameRight)) {
            updateTeamColor(teamNameRight, "matRight", new float[] { 1f, 0.15f, 0.15f });
            this.teamNameRight = teamNameRight;
        }
    }

    private void updateTeamColor(String teamName, String materialName, float[] defaultColor) {
        ObjMaterial mat = getMaterial(materialName);
        float[] color = config.find(teamName);
        if (color == null) {
            color = defaultColor;
        }
        mat.setDiffuse(color);
        mat.setAmbient(color);
    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {
    }

    @Override
    public void gsTimeChanged(GameState gs) {
    }
}
