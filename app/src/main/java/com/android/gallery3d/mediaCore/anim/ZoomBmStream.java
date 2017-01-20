package com.android.gallery3d.mediaCore.anim;

import android.graphics.Bitmap;


import com.android.gallery3d.glrenderer.GLCanvas;

/**
 * Created by linusyang on 16-12-10.
 */

public class ZoomBmStream extends BitmapStream {

    private static final float SCALE_SPEED = 0.20f;

    public ZoomBmStream(Bitmap bitmap, int rotation) {
        super(bitmap, rotation);
    }

    @Override
    protected void onDraw(GLCanvas canvas) {
        int viewWidth = bitmapWidth;
        int viewHeight = bitmapHeight;

        float initScale = Math.min((float)
                viewWidth / bitmapWidth, (float) viewHeight / bitmapHeight);
        float scale = initScale * (1 + SCALE_SPEED * mAnimProgress);

        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        canvas.translate(centerX, centerY);
        canvas.scale(scale, scale, 0);
        canvas.rotate(mRotation, 0, 0, 1);
        mCurrentTexture.draw(canvas, -mCurrentTexture.getWidth() / 2,
                -mCurrentTexture.getHeight() / 2);
    }

}
