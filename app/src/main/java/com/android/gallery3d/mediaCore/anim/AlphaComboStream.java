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
    protected void onDrawPreStream(GLCanvas canvas, float gradientIndex) {
        canvas.setAlpha(1f - gradientIndex);
        mPreStream.apply(canvas);
    }

    @Override
    protected void onDrawCurrentStream(GLCanvas canvas, float gradientIndex) {
        canvas.setAlpha(gradientIndex);
        mCurrentStream.apply(canvas);
    }


}
