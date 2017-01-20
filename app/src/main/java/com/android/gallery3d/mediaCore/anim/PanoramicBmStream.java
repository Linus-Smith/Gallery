package com.android.gallery3d.mediaCore.anim;

import android.graphics.Bitmap;

import com.android.gallery3d.glrenderer.GLCanvas;

public class PanoramicBmStream extends BitmapStream {
    private int mAnimation = 0; // 0 means horizontal, 1 means vertical

    public PanoramicBmStream(Bitmap bitmap, int rotation) {
        super(bitmap, rotation);

        if ((float) bitmap.getWidth() / (float) bitmap.getHeight() > 16.0f / 9.0f) {
            mAnimation = 0;
        } else if ((float) bitmap.getHeight() / (float) bitmap.getWidth() > 16.0f / 9.0f) {
            mAnimation = 1;
        }
    }

    @Override
    protected void onDraw(GLCanvas canvas) {
        int viewWidth = bitmapWidth;
        int viewHeight = bitmapHeight;

        int translation;
        switch (mAnimation) {
            case 0:
                translation = Math.abs((int) ((viewWidth - mWidth) * mAnimProgress));
                canvas.save();
                canvas.drawTexture(mCurrentTexture, -translation, (mHeight - viewHeight) / 2, viewWidth, viewHeight);
                canvas.restore();
                break;
            case 1:
                translation = Math.abs((int) ((viewHeight - mHeight) * mAnimProgress));
                canvas.save();
                canvas.drawTexture(mCurrentTexture, (mWidth - viewWidth) / 2, -translation, viewWidth, viewHeight);
                canvas.restore();
                break;
        }
    }
}
