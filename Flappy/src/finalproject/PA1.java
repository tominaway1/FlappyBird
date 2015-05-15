package finalproject;

import java.io.BufferedReader;
import java.io.FileReader;


import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.lwjgl.openal.AL;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.util.ResourceLoader;
import org.lwjgl.util.glu.Cylinder;

import shaders.*;


public class PA1 {

    String windowTitle = "3D Shapes";
    public boolean closeRequested = false;


    ShaderProgram shader;

    long lastFrameTime; // used to calculate delta
    

    float x_coord = 0f;
    static float y_coord = 0f;

    // random num generator
    Random randomGenerator = new Random();
    float rand1 = 0;
    float rand2 = (float) randomGenerator.nextInt(8)-1;
    float rand3 = (float) randomGenerator.nextInt(8)-1;
    float rand4 = (float) randomGenerator.nextInt(8)-1;
    float velocity = 0; 
    static float birdrotation = 0;

    //Texture mapping
    static Texture texture; 
    static Texture texture2;
    //Sound effects
    static Audio deadSound; 
    static Audio flapSound; 
    static Audio coinSound; 
    static Cylinder newPipe = new Cylinder();
    

    boolean passedOne = false;
    boolean passedTwo = false;

    float triangleAngle; // Angle of rotation for the triangles
    float quadAngle; // Angle of rotation for the quads

    public void run() {

        createWindow();
        getDelta(); // Initialise delta timer
        initGL();
        initShaders();
        
        while (!closeRequested) {
            pollInput();
            updateLogic(getDelta());
            renderGL();

            Display.update();
        }
        
        cleanup();
    }
    
    private void initGL() {

        /* OpenGL */
        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        GL11.glViewport(0, 0, width, height); // Reset The Current Viewport
        GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
        GL11.glLoadIdentity(); // Reset The Projection Matrix
        GLU.gluPerspective(45.0f, ((float) width / (float) height), 0.1f, 100.0f); // Calculate The Aspect Ratio Of The Window
        GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix
        GL11.glLoadIdentity(); // Reset The Modelview Matrix

        GL11.glShadeModel(GL11.GL_SMOOTH); // Enables Smooth Shading
        GL11.glClearColor(0.7f,0.9f, 1.0f, 1.0f);
        GL11.glClearDepth(1.0f); // Depth Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Test To Do
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // Really Nice Perspective Calculations
        //enable the textures and blending
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //loading the texture and sound effect
        try{
            texture = TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("res/img3.jpg"));
            texture2 = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/img5.png"));
            deadSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("res/dead.wav"));
            flapSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("res/flap.wav"));
            coinSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("res/coin.wav"));
        } catch(IOException e){
            e.printStackTrace();
        }
        Camera.create();        
    }
    
    private void updateLogic(int delta) {
        triangleAngle += 0.1f * delta; // Increase The Rotation Variable For The Triangles
        quadAngle -= 0.05f * delta; // Decrease The Rotation Variable For The Quads
        // polling is required to allow streaming to get a chance to
        // queue buffers.
        SoundStore.get().poll(0);
    }

        // Taken from http://stackoverflow.com/questions/16027229/reading-from-a-text-file-and-storing-in-a-string
    private String read_file(String fileName) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    private void initShaders() {
        String vertex_shader="";
        String fragment_shader="";

        // Read in vertex and fragment shader files 
        try{
            vertex_shader = read_file("src/shaders/shader.vs");
        }
        catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            fragment_shader = read_file("src/shaders/shader_blinn_phong.fs");
        }
        catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }

        

        // System.out.println(vertex_shader);
        // System.out.println(fragment_shader);

        // Create shader program
        try {
            shader = new ShaderProgram(vertex_shader, fragment_shader);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createPipe(float X,float Y){
        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y+10.0f, -1.0f); // Top Right Of The Quad (Top)
        GL11.glVertex3f(X-1.0f, Y+10.0f, -1.0f); // Top Left Of The Quad (Top)
        GL11.glVertex3f(X-1.0f, Y+10.0f, 1.0f); // Bottom Left Of The Quad (Top)
        GL11.glVertex3f(X+1.0f, Y+10.0f, 1.0f); // Bottom Right Of The Quad (Top)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y-10.0f, 1.0f); // Top Right Of The Quad (Bottom)
        GL11.glVertex3f(X-1.0f, Y-10.0f, 1.0f); // Top Left Of The Quad (Bottom)
        GL11.glVertex3f(X-1.0f, Y-10.0f, -1.0f); // Bottom Left Of The Quad (Bottom)
        GL11.glVertex3f(X+1.0f, Y-10.0f, -1.0f); // Bottom Right Of The Quad (Bottom)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y+10.0f, 1.0f); // Top Right Of The Quad (Front)
        GL11.glVertex3f(X-1.0f, Y+10.0f, 1.0f); // Top Left Of The Quad (Front)
        GL11.glVertex3f(X-1.0f, Y-10.0f, 1.0f); // Bottom Left Of The Quad (Front)
        GL11.glVertex3f(X+1.0f, Y-10.0f, 1.0f); // Bottom Right Of The Quad (Front)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y-10.0f, -1.0f); // Bottom Left Of The Quad (Back)
        GL11.glVertex3f(X-1.0f, Y-10.0f, -1.0f); // Bottom Right Of The Quad (Back)
        GL11.glVertex3f(X-1.0f, Y+10.0f, -1.0f); // Top Right Of The Quad (Back)
        GL11.glVertex3f(X+1.0f, Y+10.0f, -1.0f); // Top Left Of The Quad (Back)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X-1.0f, Y+10.0f, 1.0f); // Top Right Of The Quad (Left)
        GL11.glVertex3f(X-1.0f, Y+10.0f, -1.0f); // Top Left Of The Quad (Left)
        GL11.glVertex3f(X-1.0f, Y-10.0f, -1.0f); // Bottom Left Of The Quad (Left)
        GL11.glVertex3f(X-1.0f, Y-10.0f, 1.0f); // Bottom Right Of The Quad (Left)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y+10.0f, -1.0f); // Top Right Of The Quad (Right)
        GL11.glVertex3f(X+1.0f, Y+10.0f, 1.0f); // Top Left Of The Quad (Right)
        GL11.glVertex3f(X+1.0f, Y-10.0f, 1.0f); // Bottom Left Of The Quad (Right)
        GL11.glVertex3f(X+1.0f, Y-10.0f, -1.0f); // Bottom Right Of The Quad (Right)
    }


    private void create(float X,float Y){
        
        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y+1.0f, -1.0f); // Top Right Of The Quad (Top)
        GL11.glVertex3f(X-1.0f, Y+1.0f, -1.0f); // Top Left Of The Quad (Top)
        GL11.glVertex3f(X-1.0f, Y+1.0f, 1.0f); // Bottom Left Of The Quad (Top)
        GL11.glVertex3f(X+1.0f, Y+1.0f, 1.0f); // Bottom Right Of The Quad (Top)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y-1.0f, 1.0f); // Top Right Of The Quad (Bottom)
        GL11.glVertex3f(X-1.0f, Y-1.0f, 1.0f); // Top Left Of The Quad (Bottom)
        GL11.glVertex3f(X-1.0f, Y-1.0f, -1.0f); // Bottom Left Of The Quad (Bottom)
        GL11.glVertex3f(X+1.0f, Y-1.0f, -1.0f); // Bottom Right Of The Quad (Bottom)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y+1.0f, 1.0f); // Top Right Of The Quad (Front)
        GL11.glVertex3f(X-1.0f, Y+1.0f, 1.0f); // Top Left Of The Quad (Front)
        GL11.glVertex3f(X-1.0f, Y-1.0f, 1.0f); // Bottom Left Of The Quad (Front)
        GL11.glVertex3f(X+1.0f, Y-1.0f, 1.0f); // Bottom Right Of The Quad (Front)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y-1.0f, -1.0f); // Bottom Left Of The Quad (Back)
        GL11.glVertex3f(X-1.0f, Y-1.0f, -1.0f); // Bottom Right Of The Quad (Back)
        GL11.glVertex3f(X-1.0f, Y+1.0f, -1.0f); // Top Right Of The Quad (Back)
        GL11.glVertex3f(X+1.0f, Y+1.0f, -1.0f); // Top Left Of The Quad (Back)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X-1.0f, Y+1.0f, 1.0f); // Top Right Of The Quad (Left)
        GL11.glVertex3f(X-1.0f, Y+1.0f, -1.0f); // Top Left Of The Quad (Left)
        GL11.glVertex3f(X-1.0f, Y-1.0f, -1.0f); // Bottom Left Of The Quad (Left)
        GL11.glVertex3f(X-1.0f, Y-1.0f, 1.0f); // Bottom Right Of The Quad (Left)

        GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glVertex3f(X+1.0f, Y+1.0f, -1.0f); // Top Right Of The Quad (Right)
        GL11.glVertex3f(X+1.0f, Y+1.0f, 1.0f); // Top Left Of The Quad (Right)
        GL11.glVertex3f(X+1.0f, Y-1.0f, 1.0f); // Bottom Left Of The Quad (Right)
        GL11.glVertex3f(X+1.0f, Y-1.0f, -1.0f); // Bottom Right Of The Quad (Right)
    }

    private void createFloor(float X,float Y){
        
        //GL11.glColor3f(0.55f, 0.47f, 0.14f); // Set The Color To Bronze
        GL11.glTexCoord3f(1f,1f,0f);
        GL11.glVertex3f(X+80.0f, Y+1.0f, -40.0f); // Top Right Of The Quad (Top)
        GL11.glTexCoord3f(0f,1f,0f);
        GL11.glVertex3f(X-1.0f, Y+1.0f, -40.0f); // Top Left Of The Quad (Top)
        GL11.glTexCoord3f(0f,1f,1f);
        GL11.glVertex3f(X-1.0f, Y+1.0f, 40.0f); // Bottom Left Of The Quad (Top)
        GL11.glTexCoord3f(1f,1f,1f);
        GL11.glVertex3f(X+80.0f, Y+1.0f, 40.0f); // Bottom Right Of The Quad (Top)

        //GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glTexCoord3f(0.0f, 0.0f,0.0f);
        GL11.glVertex3f(X+80.0f, Y-1.0f, 40.0f); // Top Right Of The Quad (Bottom)
        GL11.glTexCoord3f(1.0f, 0.0f,0.0f);
        GL11.glVertex3f(X-1.0f, Y-1.0f, 40.0f); // Top Left Of The Quad (Bottom)
        GL11.glTexCoord3f(1.0f, 0.0f,1.0f);
        GL11.glVertex3f(X-1.0f, Y-1.0f, -40.0f); // Bottom Left Of The Quad (Bottom)
        GL11.glTexCoord3f(0.0f, 0.0f,1.0f);
        GL11.glVertex3f(X+80.0f, Y-1.0f, -40.0f); // Bottom Right Of The Quad (Bottom)

        //GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glTexCoord3f(1.0f, 1.0f,1.0f);
        GL11.glVertex3f(X+80.0f, Y+1.0f, 40.0f); // Top Right Of The Quad (Front)
        GL11.glTexCoord3f(0.0f, 1.0f,1.0f);
        GL11.glVertex3f(X-1.0f, Y+1.0f, 40.0f); // Top Left Of The Quad (Front)
        GL11.glTexCoord3f(0.0f, 0.0f,1.0f);
        GL11.glVertex3f(X-1.0f, Y-1.0f, 40.0f); // Bottom Left Of The Quad (Front)
        GL11.glTexCoord3f(1.0f, 0.0f,1.0f);
        GL11.glVertex3f(X+80.0f, Y-1.0f, 40.0f); // Bottom Right Of The Quad (Front)

        //GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glTexCoord3f(1.0f, 0.0f,0.0f);
        GL11.glVertex3f(X+80.0f, Y-1.0f, -40.0f); // Bottom Left Of The Quad (Back)
        GL11.glTexCoord3f(0.0f, 0.0f,0.0f);
        GL11.glVertex3f(X-1.0f, Y-1.0f, -40.0f); // Bottom Right Of The Quad (Back)
        GL11.glTexCoord3f(0.0f, 1.0f,0.0f);
        GL11.glVertex3f(X-1.0f, Y+1.0f, -40.0f); // Top Right Of The Quad (Back)
        GL11.glTexCoord3f(1.0f, 1.0f,0.0f);
        GL11.glVertex3f(X+80.0f, Y+1.0f, -40.0f); // Top Left Of The Quad (Back)

        //GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glTexCoord3f(0.0f, 1.0f,1.0f);
        GL11.glVertex3f(X-1.0f, Y+1.0f, 40.0f); // Top Right Of The Quad (Left)
        GL11.glTexCoord3f(0.0f, 1.0f,0.0f);
        GL11.glVertex3f(X-1.0f, Y+1.0f, -40.0f); // Top Left Of The Quad (Left)
        GL11.glTexCoord3f(0.0f, 0.0f,0.0f);
        GL11.glVertex3f(X-1.0f, Y-1.0f, -40.0f); // Bottom Left Of The Quad (Left)
        GL11.glTexCoord3f(0.0f, 0.0f,1.0f);
        GL11.glVertex3f(X-1.0f, Y-1.0f, 40.0f); // Bottom Right Of The Quad (Left)

        //GL11.glColor3f(0.3f, 0.3f, 0.1f); // Set The Color To Green
        GL11.glTexCoord3f(1.0f, 1.0f,0.0f);
        GL11.glVertex3f(X+80.0f, Y+1.0f, -40.0f); // Top Right Of The Quad (Right)
        GL11.glTexCoord3f(1.0f, 1.0f,1.0f);
        GL11.glVertex3f(X+80.0f, Y+1.0f, 40.0f); // Top Left Of The Quad (Right)
        GL11.glTexCoord3f(1.0f, 0.0f,1.0f);
        GL11.glVertex3f(X+80.0f, Y-1.0f, 40.0f); // Bottom Left Of The Quad (Right)
        GL11.glTexCoord3f(1.0f, 0.0f,0.0f);
        GL11.glVertex3f(X+80.0f, Y-1.0f, -40.0f); // Bottom Right Of The Quad (Right)
    }

    private boolean checkCollision(){
        if (y_coord - 1 < -7){
                deadSound.playAsSoundEffect(1.0f, 1.0f, false);
                return true;
            } 
        if (x_coord > -12 && x_coord < -7.8){
            if ((y_coord + 1 > 13+rand1-10) || (y_coord - 1 < -13+rand1+10)){
                deadSound.playAsSoundEffect(1.0f, 1.0f, false);
                return true;
            }
            else{
                return false;
            }
        }
        else if (x_coord > -22 && x_coord < -17.8){
            if ((y_coord + 1 > 13+rand2-10) || (y_coord - 1 < -13+rand2+10)){
                deadSound.playAsSoundEffect(1.0f, 1.0f, false);
                return true;
            }
            else{
                return false;
            }
        }

        return false;
        // return true;
    }

    private void reset(){
        passedOne = false;
        passedTwo = false;
        x_coord = 0f;
        y_coord = 0f;
        birdrotation = 0;
        rand1 = 0;
        rand2 = (float) randomGenerator.nextInt(8)-1;
        rand3 = (float) randomGenerator.nextInt(8)-1;
        rand4 = (float) randomGenerator.nextInt(8)-1;
    }
    private void renderGL() {
        // start to use shaders
        shader.begin();
        float dir = (float)(1./Math.sqrt(3));
        shader.setUniform3f("lightDir", dir, dir, dir);
        shader.setUniform3f("ambCol", 1, 0, 0);
        shader.setUniform3f("specCol", 1, 1, 1);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
        GL11.glLoadIdentity(); // Reset The View
        GL11.glTranslatef(0f, 0.0f, -27.0f); // Move Right And Into The Screen

        if (checkCollision()){
            System.out.println("reset");
            reset();
        }
        Camera.apply();
        //create the floor and bind the texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        
        Color.white.bind();
        texture.bind();
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glBegin(GL11.GL_QUADS);
        createFloor(-40,-8);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
       

        GL11.glPushMatrix();
        GL11.glTranslatef(-10,y_coord,0);
        GL11.glRotatef(birdrotation, 0, 0, 1);
        GL11.glTranslatef(10,-y_coord,0);
        // make the bird
        GL11.glBegin(GL11.GL_QUADS);
        create(-10,y_coord);
        GL11.glEnd();
        GL11.glPopMatrix();

        
        // check if passed pipe
        if (x_coord <= -10 && !passedOne){
            coinSound.playAsSoundEffect(1.0f, 1.0f, false);
            passedOne = true;
        }
        if (x_coord + 10 <= -10 && !passedTwo){
            coinSound.playAsSoundEffect(1.0f, 1.0f, false);
            passedOne = true;
        }


        // first pipe
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord,-3+rand1,0);
        GL11.glRotatef(90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord,3+rand1,0);
        GL11.glRotatef(-90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        
        // second pipe
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord+10,-3+rand2,0);
        GL11.glRotatef(90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord+10,3+rand2,0);
        GL11.glRotatef(-90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();

        // third pipe
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord+20,-3+rand3,0);
        GL11.glRotatef(90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord+20,3+rand3,0);
        GL11.glRotatef(-90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();

        // Fourth pipe
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord+30,-3+rand4,0);
        GL11.glRotatef(90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x_coord+30,3+rand4,0);
        GL11.glRotatef(-90, 1, 0, 0);
        newPipe.draw(1,1,10,16,16);
        GL11.glPopMatrix();
        
        if (x_coord < -20){
            rand1 = rand2;
            rand2 = rand3;
            rand3 = rand4;
            rand4 = (float) randomGenerator.nextInt(8)-1;
            x_coord = x_coord+10;
            // passedOne = false;
            passedTwo = false;
        }
        else{
            x_coord = x_coord - .05f;
        }

        y_coord = y_coord - .07f;

        //rotates the bird when the mouse isn't being clicked
        birdrotation -= 2;
        if(birdrotation <= -60 ){
            birdrotation = -60;
        } else if(birdrotation > 50){
            birdrotation = 50;
        }
        shader.end();
       
    }

    public boolean isFalling() {
        return velocity > 110;
    }

    /**
     * Poll Input
     */
    public void pollInput() {
        Camera.acceptInput(getDelta());
        // scroll through key events
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                    closeRequested = true;
                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
                    snapshot();
            }
        }

        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
    }

    public void snapshot() {
        System.out.println("Taking a snapshot ... snapshot.png");

        GL11.glReadBuffer(GL11.GL_FRONT);

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );

        File file = new File("snapshot.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        int delta = (int) (time - lastFrameTime);
        lastFrameTime = time;
     
        return delta;
    }

    private void createWindow() {
        try {
            Display.setDisplayMode(new DisplayMode(640, 480));
            Display.setVSyncEnabled(true);
            Display.setTitle(windowTitle);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Destroy and clean up resources
     */
    private void cleanup() {
        Display.destroy();
        AL.destroy();
    }
    
    public static void main(String[] args) {
        new PA1().run();
    }
    
    public static class Camera {
        public static float moveSpeed = 0.5f;

        private static float maxLook = 85;

        private static float mouseSensitivity = 0.05f;

        private static Vector3f pos;
        private static Vector3f rotation;

        public static void create() {
            pos = new Vector3f(0, 0, 0);
            rotation = new Vector3f(0, 0, 0);
        }

        public static void apply() {
            if (rotation.y / 360 > 1) {
                rotation.y -= 360;
            } else if (rotation.y / 360 < -1) {
                rotation.y += 360;
            }

            //System.out.println(rotation);
            GL11.glRotatef(rotation.x, 1, 0, 0);
            GL11.glRotatef(rotation.y, 0, 1, 0);
            GL11.glRotatef(rotation.z, 0, 0, 1);
            GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
        }

        public static void acceptInput(float delta) {
            //System.out.println("delta="+delta);
            acceptInputRotate(delta);
            acceptInputMove(delta);
        }

        public static void acceptInputRotate(float delta) {
            if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
                
                y_coord = y_coord + .5f;
                birdrotation += 50;
                flapSound.playAsSoundEffect(1.0f, 1.0f, false);

                // float mouseDX = Mouse.getDX();
                // float mouseDY = -Mouse.getDY();
                // //System.out.println("DX/Y: " + mouseDX + "  " + mouseDY);
                // rotation.y += mouseDX * mouseSensitivity * delta;
                // rotation.x += mouseDY * mouseSensitivity * delta;
                // rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
            }
        }

        public static void acceptInputMove(float delta) {
            // boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
            // boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
            boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
            boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
            // boolean keyFast = Keyboard.isKeyDown(Keyboard.KEY_Q);
            // boolean keySlow = Keyboard.isKeyDown(Keyboard.KEY_E);
            // boolean keyFlyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
            // boolean keyFlyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

            float speed;

            // if (keyFast) {
            //     speed = moveSpeed * 5;
            // } else if (keySlow) {
            //     speed = moveSpeed / 2;
            // } else {
            //     speed = moveSpeed;
            // }

            // speed *= delta;

            // if (keyFlyUp) {
            //     pos.y += speed;
            // }
            // if (keyFlyDown) {
            //     pos.y -= speed;
            // }

            // if (keyDown) {
            //     pos.x -= Math.sin(Math.toRadians(rotation.y)) * speed;
            //     pos.z += Math.cos(Math.toRadians(rotation.y)) * speed;
            // }
            // if (keyUp) {
            //     pos.x += Math.sin(Math.toRadians(rotation.y)) * speed;
            //     pos.z -= Math.cos(Math.toRadians(rotation.y)) * speed;
            // }
            // if (keyLeft) {
            //     pos.x += Math.sin(Math.toRadians(rotation.y - 90)) * speed;
            //     pos.z -= Math.cos(Math.toRadians(rotation.y - 90)) * speed;
            // }
            // if (keyRight) {
            //     pos.x += Math.sin(Math.toRadians(rotation.y + 90)) * speed;
            //     pos.z -= Math.cos(Math.toRadians(rotation.y + 90)) * speed;
            // }
            if (keyLeft) {
               rotation.y += -0.5;
               // rotation.x += 0.5;
               }
            if (keyRight) {
                rotation.y += 0.5;
                // rotation.x += -0.5;
            }

        }

        public static void setSpeed(float speed) {
            moveSpeed = speed;
        }

        public static void setPos(Vector3f pos) {
            Camera.pos = pos;
        }

        public static Vector3f getPos() {
            return pos;
        }

        public static void setX(float x) {
            pos.x = x;
        }

        public static float getX() {
            return pos.x;
        }

        public static void addToX(float x) {
            pos.x += x;
        }

        public static void setY(float y) {
            pos.y = y;
        }

        public static float getY() {
            return pos.y;
        }

        public static void addToY(float y) {
            pos.y += y;
        }

        public static void setZ(float z) {
            pos.z = z;
        }

        public static float getZ() {
            return pos.z;
        }

        public static void addToZ(float z) {
            pos.z += z;
        }

        public static void setRotation(Vector3f rotation) {
            Camera.rotation = rotation;
        }

        public static Vector3f getRotation() {
            return rotation;
        }

        public static void setRotationX(float x) {
            rotation.x = x;
        }

        public static float getRotationX() {
            return rotation.x;
        }

        public static void addToRotationX(float x) {
            rotation.x += x;
        }

        public static void setRotationY(float y) {
            rotation.y = y;
        }

        public static float getRotationY() {
            return rotation.y;
        }

        public static void addToRotationY(float y) {
            rotation.y += y;
        }

        public static void setRotationZ(float z) {
            rotation.z = z;
        }

        public static float getRotationZ() {
            return rotation.z;
        }

        public static void addToRotationZ(float z) {
            rotation.z += z;
        }

        public static void setMaxLook(float maxLook) {
            Camera.maxLook = maxLook;
        }

        public static float getMaxLook() {
            return maxLook;
        }

        public static void setMouseSensitivity(float mouseSensitivity) {
            Camera.mouseSensitivity = mouseSensitivity;
        }

        public static float getMouseSensitivity() {
            return mouseSensitivity;
        }
    }
}
