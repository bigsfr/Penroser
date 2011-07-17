package org.jf.Penroser;

import android.app.Activity;
import android.os.Bundle;

import java.util.Random;

public class Penroser extends Activity
{
    public static final Random random = new Random();

    public static final HalfRhombusPool halfRhombusPool = new HalfRhombusPool();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PenroserGLView penroserGLView = new PenroserGLView(this);
        setContentView(penroserGLView);
    }
}
