package com.android.gallery3d.mediaCore.anim;


import com.android.gallery3d.glrenderer.GLCanvas;

/**
 * Created by linus.yang on 2016/12/14.
 */

public class TransitionAnimStream extends MediaStream {

    @Override
    public MediaStream getCurrentStream() {
        return null;
    }

    @Override
    protected void onDraw(GLCanvas canvas) {
        return;
    }


    @Override
    protected void onCalculate(float progress) {
        super.onCalculate(progress);
    }
}
