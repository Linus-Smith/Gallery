package com.android.gallery3d.mediaCore.anim;

import com.android.gallery3d.glrenderer.GLCanvas;

/**
 * Created by linus.yang on 2016/12/14.
 */

public class AlphaComboStream extends ComboStream {


    public AlphaComboStream(MediaStream mediaStream) {
        super(mediaStream);
    }

    @Override
    public void onDrawPreStream(GLCanvas canvas, long animTime, float gradientIndex) {
        canvas.setAlpha(1f - gradientIndex);
        mPreStream.onDraw(canvas);
    }

    @Override
    public void onDrawCurrentStream(GLCanvas canvas, long animTime, float gradientIndex) {
        canvas.setAlpha(gradientIndex);
        mCurrentStream.calculate(animTime);
        mCurrentStream.onDraw(canvas);
    }


}
