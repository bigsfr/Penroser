package org.jf.Penroser;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FatHalfRhombus extends HalfRhombus {
    private static final int TOP_FAT = 0;
    private static final int SKINNY = 1;
    private static final int BOTTOM_FAT = 2;

    private static final int NUM_CHILDREN = 3;
    private static final float[] leftVertices;
    private static final int[] leftColors;
    private static final float[] rightVertices;
    private static final int[] rightColors;

    private static int leftVertexVbo;
    private static int leftColorVbo;
    private static int rightVertexVbo;
    private static int rightColorVbo;

    //light blue (reverse RGB)
    private static final int leftColor = 0xFF7D52;

    //dark blue (reverse RGB)
    private static final int rightColor = 0xDB3700;

    static {
        float[][] vertices = new float[1][];
        int [][] colors = new int[1][];

        generateVertices(VBO_LEVEL, LEFT, vertices, colors);
        leftVertices = vertices[0];
        leftColors = colors[0];

        generateVertices(VBO_LEVEL, RIGHT, vertices, colors);
        rightVertices = vertices[0];
        rightColors = colors[0];
    }

    public FatHalfRhombus(int level, int side, float x, float y, float scale, int rotation) {
        super(level, side, x, y, scale, rotation);
    }

@Override
    public void draw(GL11 gl, int maxLevel) {
        if (this.level < maxLevel) {
            for (int i=0; i<NUM_CHILDREN; i++) {
                HalfRhombus halfRhombus = getChild(i);
                halfRhombus.draw(gl, maxLevel);
            }
        } else {
            gl.glPushMatrix();
            gl.glTranslatef(this.x, this.y, 0);
            gl.glScalef(this.scale, this.scale, 0);
            gl.glRotatef(getRotationInDegrees(), 0, 0, -1);

            int vertexVbo;
            int colorVbo;
            int length;
            if (side == LEFT) {
                vertexVbo = leftVertexVbo;
                colorVbo = leftColorVbo;
                length = leftColors.length;
            } else {
                vertexVbo = rightVertexVbo;
                colorVbo = rightColorVbo;
                length = rightColors.length;
            }

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVbo);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorVbo);
            gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, 0);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, length);

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

            gl.glPopMatrix();
        }
    }

    @Override
    public HalfRhombus getChild(int i) {
        int sign = this.side==LEFT?1:-1;

        float newScale = scale / Constants.goldenRatio;

        EdgeLength edgeLength = EdgeLength.getEdgeLength(level);

        switch (i) {
            case TOP_FAT: {
                float topVerticeX = x + edgeLength.x(rotation-(sign*2)) + edgeLength.x(rotation+(sign*2));
                float topVerticeY = y + edgeLength.y(rotation-(sign*2)) + edgeLength.y(rotation+(sign*2));

                //180 degree rotation - we don't care about sign
                return new FatHalfRhombus(level+1, oppositeSide(), topVerticeX, topVerticeY, newScale, rotation+10);
            }
            case SKINNY: {
                float sideVerticeX = x+edgeLength.x(rotation-(sign*2));
                float sideVerticeY = y+edgeLength.y(rotation-(sign*2));
                return new SkinnyHalfRhombus(level+1, oppositeSide(), sideVerticeX, sideVerticeY, newScale, rotation+(sign*2));
            }
            case BOTTOM_FAT: {
                float sideVerticeX = x+edgeLength.x(rotation-(sign*2));
                float sideVerticeY = y+edgeLength.y(rotation-(sign*2));
                return new FatHalfRhombus(level+1, this.side, sideVerticeX, sideVerticeY, newScale, rotation+(sign*8));
            }
        }

        return null;
    }

    public static void onSurfaceCreated(GL11 gl) {
        int[] vboref = new int[1];

        //left vertex vbo
        gl.glGenBuffers(1, vboref, 0);
        leftVertexVbo = vboref[0];

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, leftVertexVbo);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, leftVertices.length * 4, FloatBuffer.wrap(leftVertices), GL11.GL_STATIC_DRAW);

        //left color vbo
        gl.glGenBuffers(1, vboref, 0);
        leftColorVbo = vboref[0];

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, leftColorVbo);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, leftColors.length * 4, IntBuffer.wrap(leftColors), GL11.GL_STATIC_DRAW);

        //right vertex vbo
        gl.glGenBuffers(1, vboref, 0);
        rightVertexVbo = vboref[0];

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, rightVertexVbo);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, rightVertices.length * 4, FloatBuffer.wrap(rightVertices), GL11.GL_STATIC_DRAW);

        //right color vbo
        gl.glGenBuffers(1, vboref, 0);
        rightColorVbo = vboref[0];

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, rightColorVbo);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, rightColors.length * 4, IntBuffer.wrap(rightColors), GL11.GL_STATIC_DRAW);

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    }

    public static void generateVertices(int level, int side, float[][] vertices, int[][] colors) {
        assert vertices != null && vertices.length > 0;
        assert colors != null && colors.length > 0;

        int fat=1;
        int skinny=0;

        for (int i=0; i<level; i++) {
            fat = fat * 2 + skinny;
            skinny = fat+skinny;
        }

        int count = fat+skinny;
        vertices[0] = new float[count*3*2];
        colors[0] = new int[count*3];

        generateVertices(vertices[0], colors[0], 0, 0, level, 0, side, 0, 0);
    }

    //TODO: I'm curious whether using final for these args will cause javac to optimize the many instances of level+1, rotation+n, etc.
    protected static int generateVertices(final float[] vertices, final int[] colors, int index, final int level, final int maxLevel, final int rotation, final int side, final float x, final float y) {
        //x,y are the coordinates of the bottom vertex of the rhombus

        EdgeLength edgeLength = EdgeLength.getEdgeLength(level);

        float sideVertexX, sideVertexY;
        float topVertexX, topVertexY;

        int sign = side==LEFT?1:-1;

        sideVertexX = x + edgeLength.x(rotation-(sign*2));
        sideVertexY = y + edgeLength.y(rotation-(sign*2));

        topVertexX = sideVertexX + edgeLength.x(rotation+(sign*2));
        topVertexY = sideVertexY + edgeLength.y(rotation+(sign*2));

        if (level < maxLevel) {
            //top fat rhombus
            index = FatHalfRhombus.generateVertices(vertices, colors, index, level+1, maxLevel, rotation+10, oppositeSide(side), topVertexX, topVertexY);

            //left skinny rhombus
            index = SkinnyHalfRhombus.generateVertices(vertices, colors, index, level+1, maxLevel, rotation+(sign*2), oppositeSide(side), sideVertexX, sideVertexY);

            //left fat rhombus
            return FatHalfRhombus.generateVertices(vertices, colors, index, level+1, maxLevel, rotation+(sign*8), side, sideVertexX, sideVertexY);
        } else {
            int color = side==LEFT?leftColor:rightColor;

            colors[index>>1] = color;

            vertices[index++] = x;
            vertices[index++] = y;

            colors[index>>1] = color;

            vertices[index++] = sideVertexX;
            vertices[index++] = sideVertexY;

            colors[index>>1] = color;

            vertices[index++] = topVertexX;
            vertices[index++] = topVertexY;

            return index;
        }
    }
}
