package com.android.gallery3d.mediaCore.anim;

import android.graphics.Bitmap;
import android.view.animation.Interpolator;

import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.GLCanvas;

/**
 * Created by linusyang on 16-12-9.
 */

public abstract class BitmapStream  extends MediaStream {

    protected Interpolator mInterpolator;
    protected BitmapTexture mCurrentTexture;
    protected int mRotation;

    protected int bitmapWidth;
    protected int bitmapHeight;

    public BitmapStream(Bitmap bitmap , int rotation) {
        if(bitmap == null) throw new NullPointerException("bitmap == null");
        mRotation = rotation;
        mCurrentTexture = new BitmapTexture(bitmap);
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
    }

    @Override
    void apply(GLCanvas canvas) {
        Bitmap bitmap = mCurrentTexture.getBitmap();
        if(bitmap == null || bitmap.isRecycled()) return;
        super.apply(canvas);
    }

    @Override
    public MediaStream getCurrentStream() {
        return this;
    }
}
