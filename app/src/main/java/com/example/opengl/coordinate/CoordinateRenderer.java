package com.example.opengl.coordinate;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.glColor4f;
import static android.opengl.GLES10.glVertexPointer;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;

public class CoordinateRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "FirstRenderer";
    private static final int BYTES_PER_FLOAT = 4;


    // 定义Open GL ES绘制所需要的Buffer对象

    private final ByteBuffer XFacetsBuffer, YFacetsBuffer, ZFacetsBuffer;
    private final FloatBuffer  xyzVertexData;
    private final FloatBuffer mColorData;
    private int mShaderProgram;

    private int aColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;

    private final float[] modelMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private volatile boolean mIsPressed;
    private volatile boolean mHadChanged = false;

    private int mVboBufferId;


    private float[] mColorPoints = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f
    };


    //定义XYZ坐标和显示的字
    float xyzVertices[] = new float[]{
            -0.6f, 0f, 0f,//0 x起点，画坐标轴的
            0.6f, 0f, 0f,//1 X轴的终点
            0.5f,0.1f,0f,//2 X轴箭头1
            0.5f,-0.1f,0f,//3 X轴箭头2

            0f, -0.6f, 0f,//4 Y轴起点
            0f, 0.6f, 0f,//5 Y轴终点
            0.1f ,0.5f ,0f,//6 Y轴箭头1
            -0.1f ,0.5f ,0f,//7 Y轴箭头2

            0f, 0f, -0.6f,//8 Z轴起点
            0f, 0f, 0.6f,//9 Z轴终点
            0f ,0.1f ,0.5f,//10 Z轴箭头1
            0f ,-0.1f ,0.5f,//11 Z轴箭头2

            0.8f,0f,0f,//12 绘制字X
            0.85f,0.1f,0f,//13
            0.75f,0.1f,0f,//14
            0.75f,-0.1f,0f,//15
            0.85f,-0.1f,0f,//16

            0f,0.7f,0f,//17 绘制字Y
            0f,0.65f,0f,//18
            0.05f,0.75f,0f,//19
            -0.05f,0.75f,0f,//20

            -0.05f ,0.05f ,0.7f,//21  绘制字Z
            0.05f,0.05f,0.7f,//22
            -0.05f,-0.05f,0.7f,//23
            0.05f,-0.05f,0.7f,//24

//刻度X轴刻度
            0.3f,0f,0f,//25
            0.3f,0.05f,0f,//26
            -0.3f,0f,0f,//27
            -0.3f,0.05f,0f,//28
//刻度y轴刻度
            0f,0.3f,0f,//29
            -0.05f,0.3f,0f,//30
            0f,-0.3f,0f,//31
            -0.05f,-0.3f,0f,//32
//刻度Z轴刻度
            0f,0f,0.3f,//33
            0f,0.05f,0.3f,//34
            0f,0f,-0.3f,//35
            0f,0.05f,-0.3f//36



    };

    //X坐标及其箭头  
    byte[] XFacets = new byte[] {
//起终点
            0,1,
//箭头
            1,2,
            1,3,
//X
            12,13,
            12,14,
            12,15,
            12,16,
//X坐标
            25,26,
            27,28

    };
    //Y坐标及其箭头  
    byte[] YFacets = new byte[] {
//起终点
            4,5,
//箭头
            5,6,
            5,7,
//字Y
            17,18,
            17,19,
            17,20,
//Y轴刻度
            29,30,
            31,32

    };
    //Z坐标及其箭头  
    byte[] ZFacets = new byte[] {
//起终点
            8,9,
//箭头
            9,10,
            9,11,
//字Z
            21,22,
            22,23,
            23,24,
//Z轴刻度
            33,34,
            35,36
    };


    private String mVertexShaderCode =
            "uniform mat4 u_Matrix;        \n" +
                    "attribute vec4 a_Position;     \n" +
                    "attribute vec4 a_Color;     \n" +
                    "varying vec4 v_Color;     \n" +
                    "void main()                    \n" +
                    "{                              \n" +
                    "    v_Color =  a_Color;  \n" +
                    "    gl_Position =  u_Matrix * a_Position;  \n" +
                    "}   \n";
    private String mFragmentShaderCode =
            "precision mediump float; \n" +
                    "varying vec4 v_Color;     \n" +
                    "void main()                    \n" +
                    "{                              \n" +
                    "    gl_FragColor = v_Color;    \n" +
                    "}";


    CoordinateRenderer() {
        mColorData = ByteBuffer
                .allocateDirect(mColorPoints.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mColorData.put(mColorPoints);

        xyzVertexData = ByteBuffer
                .allocateDirect(xyzVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        xyzVertexData.put(xyzVertices);

        // 将直线的数组包装成ByteBuffer
        XFacetsBuffer = ByteBuffer.wrap(XFacets);
        YFacetsBuffer = ByteBuffer.wrap(YFacets);
        ZFacetsBuffer = ByteBuffer.wrap(ZFacets);
    }



    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        int vertexShader = compileShader(GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, mFragmentShaderCode);
        mShaderProgram = linkProgram(vertexShader, fragmentShader);
        glUseProgram(mShaderProgram);

        aColorLocation = glGetAttribLocation(mShaderProgram, "a_Color");

        aPositionLocation = glGetAttribLocation(mShaderProgram, "a_Position");

        uMatrixLocation = glGetUniformLocation(mShaderProgram, "u_Matrix");

        xyzVertexData.position(0);

        initVBO();
        glBindBuffer(GL_ARRAY_BUFFER, mVboBufferId);
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT,
                false, 3 * BYTES_PER_FLOAT, 0);
//        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT,
//                false, 0, mVertexData);
        glEnableVertexAttribArray(aPositionLocation);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        mColorData.position(0);
        glVertexAttribPointer(aColorLocation, 3, GL_FLOAT,
                false, 0, mColorData);
        glEnableVertexAttribArray(aColorLocation);

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);
        Matrix.perspectiveM(projectionMatrix, 0, 45, (float) width
                / (float) height, 1f, 10f);
        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f,
                0f, 0f, 0f, 1f, 0f);

        setIdentityM(modelMatrix, 0);

    }

    private void updateAngle() {
        if (!mIsPressed) {
            rotateM(modelMatrix, 0, (float) 1.0, 0f, 1f, 0f);
            multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0,
                    viewMatrix, 0);
            multiplyMM(mMVPMatrix, 0, viewProjectionMatrix,
                    0, modelMatrix, 0);
        } else {
            updateVertex();
        }
    }

    private void updateVertex() {
        synchronized (this) {
            if (!mHadChanged) {
                xyzVertexData.clear();
                xyzVertexData.put(xyzVertices);
                xyzVertexData.position(0);
                glBindBuffer(GL_ARRAY_BUFFER, mVboBufferId);
                glBufferData(GL_ARRAY_BUFFER, xyzVertexData.capacity() * BYTES_PER_FLOAT, xyzVertexData, GL_STATIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
                mHadChanged = true;
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        updateAngle();
        // Update the viewProjection matrix, and create an inverted matrix for
        // touch picking.
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0,
                viewMatrix, 0);

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);

        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);
        glClear(GL_COLOR_BUFFER_BIT);

        // Assign the matrix
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
        // Draw the table.
        //glDrawArrays(GL_TRIANGLE_STRIP, 0, 10);
        glLineWidth(3.0f);//直线宽度
        glVertexPointer(3, GL10.GL_FLOAT, 0, xyzVertexData);//设置XYZ的顶点

        // 设置顶点的颜色数据
        //glColor4f(0.0f, 1.0f, 0.0f, 1.0f);//设置绘笔颜色
        glDrawElements(GL10.GL_LINES, XFacetsBuffer.remaining(),
                GL10.GL_UNSIGNED_BYTE, XFacetsBuffer);//X

        //glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
        glDrawElements(GL10.GL_LINES, YFacetsBuffer.remaining(),
                GL10.GL_UNSIGNED_BYTE, YFacetsBuffer);//Y

        //glColor4f(1.0f, 0.0f, 1.0f, 1.0f);
        glDrawElements(GL10.GL_LINES, ZFacetsBuffer.remaining(),
                GL10.GL_UNSIGNED_BYTE, ZFacetsBuffer);//Z



    }


    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            Log.w(TAG, "Could not create new shader.");
            return 0;
        }

        glShaderSource(shaderObjectId, shaderCode);
        glCompileShader(shaderObjectId);

        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:"
                + glGetShaderInfoLog(shaderObjectId));

        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObjectId);
            Log.w(TAG, "Compilation of shader failed.");
            return 0;
        }

        return shaderObjectId;
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // Create a new program object.
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            Log.w(TAG, "Could not create new program");
            return 0;
        }

        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId);
        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId);

        // Link the two shaders together into a program.
        glLinkProgram(programObjectId);

        // Get the link status.
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        // Print the program info log to the Android log output.
        Log.v(TAG, "Results of linking program:\n"
                + glGetProgramInfoLog(programObjectId));

        // Verify the link status.
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);
            Log.w(TAG, "Linking of program failed.");
            return 0;
        }

        // Return the program object ID.
        return programObjectId;
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    private Geometry.Ray convertNormalized2DPointToRay(
            float normalizedX, float normalizedY) {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};

        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        multiplyMV(
                nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(
                farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        Geometry.Point nearPointRay =
                new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);

        Geometry.Point farPointRay =
                new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

        return new Geometry.Ray(nearPointRay,
                Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {
        Log.e(TAG, "normalizedX " + normalizedX + " normalizedY " + normalizedY);
        Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
        Log.e(TAG, "ray " + ray.toString());

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        Geometry.Sphere malletBoundingSphere = new Geometry.Sphere(new Geometry.Point(
                0F,
                0F,
                0F),
                0.1F);

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        mIsPressed = Geometry.intersects(malletBoundingSphere, ray);
    }

    public void handleTouchDown(float normalizedX, float normalizedY) {
        handleTouchPress(normalizedX, normalizedY);
    }

    public void handleTouchUp(float normalizedX, float normalizedY) {
        mIsPressed = false;
    }

    private void initVBO() {
        // Allocate a buffer.
        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);

        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new index buffer object.");
        }

        mVboBufferId = buffers[0];

        // Bind to the buffer.
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

        // Transfer data from native memory to the GPU buffer.
        //glBufferData(GL_ARRAY_BUFFER, mVertexData.capacity() * BYTES_PER_FLOAT, mVertexData, GL_STATIC_DRAW);
        glBufferData(GL_ARRAY_BUFFER, xyzVertexData.capacity() * BYTES_PER_FLOAT, xyzVertexData, GL_STATIC_DRAW);


        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // We let the native buffer go out of scope, but it won't be released
        // until the next time the garbage collector is run.
    }


}

