package com.android.gallery3d.mediaCore.anim;

import android.graphics.Bitmap;

import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.Utils.VideoScreenNail;

import java.io.File;

/**
 * Created by linusyang on 16-12-9.
 */

public class VideoStream extends MediaStream {

    private int mVideoWidth = -1;
    private int mVideoHeight = -1;
    private int drawX = 0;
    private int drawY = 0;
    private boolean isBlurEdge = false;
    private VideoScreenNail mVideoScreenNail;
    protected BitmapTexture mBlurTexture;
    private File mVideoPath;

    public VideoStream(File videoPath, VideoScreenNail videoScreenNail) {
        if(!videoPath.exists()) throw new RuntimeException("video not exists ");
        mVideoScreenNail  = videoScreenNail;
        mVideoScreenNail.setmFile(videoPath);
        mVideoPath = videoPath;
    }

    public VideoStream(Bitmap bitmap, File videoPath, VideoScreenNail videoScreenNail, int videoWidth, int videoHeight) {
        this(videoPath, videoScreenNail);
        mBlurTexture = new BitmapTexture(bitmap);
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }

    @Override
    void setResolution(int width, int height) {
        super.setResolution(width, height);
        float ratio = Math.min(((float)mWidth) / mVideoWidth, ((float)mHeight) / mVideoHeight);
        if (ratio > 1.0f) {
            ratio = 1.0f;
            isBlurEdge = false;
        } else {
            isBlurEdge = true;
        }
        mVideoWidth = (int)(mVideoWidth * ratio);
        mVideoHeight = (int)(mVideoHeight * ratio);
        drawX = (mWidth - mVideoWidth) / 2;
        drawY = (mHeight - mVideoHeight) / 2;
    }

    @Override
    protected void onDraw(GLCanvas canvas) {
        if(isBlurEdge){
            canvas.drawTexture(mBlurTexture, 0, 0, mWidth, mHeight);
        }
        mVideoScreenNail.draw(canvas, drawX, drawY, mVideoWidth, mVideoHeight);
    }


    @Override
    protected void onCalculate(float progress) {
        super.onCalculate(progress);
    }

    @Override
    void prepare() {
        mVideoScreenNail.prepare();
    }

    @Override
    void start() {
        if(mStatusControl.getPlayState() != PLAY_STATE_START){
            mVideoScreenNail.start();
        }
        super.start();

    }
    @Override
    void pause() {


        if(mStatusControl.getPlayState() == PLAY_STATE_START){
            mVideoScreenNail.pause();
        }
        super.pause();
    }

    @Override
    void stop(){
        super.stop();
        mVideoScreenNail.stop();
    }

    @Override
    MediaStream getCurrentStream() {
        return this;
    }

    @Override
    void seekTo(long durationT) {
        super.seekTo(durationT);
        mVideoScreenNail.seekTo(durationT*1000);
    }

    @Override
    void restart() {
        super.restart();
        mVideoScreenNail.restart();
    }

}
