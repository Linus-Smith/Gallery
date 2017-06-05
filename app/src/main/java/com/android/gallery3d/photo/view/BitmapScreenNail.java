package com.android.gallery3d.photo.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by Linus on 2017/5/22.
 */

public class BitmapScreenNail implements ScreenNail {

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;

    public BitmapScreenNail(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public BitmapScreenNail(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void draw(Canvas canvas, int x, int y, int width, int height) {
        if(mBitmap != null) {
            canvas.drawBitmap(mBitmap, x, y, new Paint());
        } else{
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            canvas.drawRect(x, y , mWidth, mHeight, paint);
        }
    }

    @Override
    public void noDraw() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void draw(Canvas canvas, RectF source, RectF dest) {

    }
}
