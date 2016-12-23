package com.android.gallery3d.mediaCore.view;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.GLView;

/**
 * Created by bruce.jiang on 2016/12/22.
 */

public class RecoderView extends GLView {

    public GLCanvas getmCanvas() {
        return mCanvas;
    }

    public void setmCanvas(GLCanvas mCanvas) {
        this.mCanvas = mCanvas;
    }

    private GLCanvas mCanvas;
    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);
        mCanvas = canvas;
    }

}

