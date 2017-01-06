package com.android.gallery3d.mediaCore.anim;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import com.android.gallery3d.glrenderer.GLCanvas;

import java.util.Random;

/**
 * Created by linusyang on 16-12-10.
 */

public class ZoomBmStream extends BitmapStream {

    private static final float SCALE_SPEED = 0.20f;
    private static final float MOVE_SPEED = SCALE_SPEED;

    private final PointF mMovingVector;
    private Random mRandom = new Random();

    public ZoomBmStream(Bitmap bitmap, int rotation) {
        super(bitmap, rotation);
        mMovingVector = new PointF(
                MOVE_SPEED * bitmapWidth * (mRandom.nextFloat() - 0.5f),
                MOVE_SPEED * bitmapHeight * (mRandom.nextFloat() - 0.5f));
    }

    @Override
    public void onDraw(GLCanvas canvas) {
        Log.i("jzf","canvas draw"+mAnimProgress);
        int viewWidth = bitmapWidth;
        int viewHeight = bitmapHeight;

        float initScale = Math.min((float)
                viewWidth / bitmapWidth, (float) viewHeight / bitmapHeight);
        float scale = initScale * (1 + SCALE_SPEED * mAnimProgress);

        float centerX = viewWidth / 2 + mMovingVector.x * mAnimProgress;
        float centerY = viewHeight / 2 + mMovingVector.y * mAnimProgress;
        canvas.translate(centerX, centerY);
        canvas.scale(scale, scale, 0);
        canvas.rotate(mRotation, 0, 0, 1);
        mCurrentTexture.draw(canvas, -mCurrentTexture.getWidth() / 2,
                -mCurrentTexture.getHeight() / 2);
    }

}
