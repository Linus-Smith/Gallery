package com.android.gallery3d.mediaCore.anim;

import android.app.Activity;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.Utils.VideoScreenNail;

import java.io.File;

/**
 * Created by linusyang on 16-12-9.
 */

public class VideoStream extends MediaStream {

    private VideoScreenNail mVideoScreenNail;
    private File mVideoPath;

    public VideoStream(File videoPath, Activity mActivity) {
        if(!videoPath.exists()) throw new RuntimeException("video not exists ");
        mVideoScreenNail  = new VideoScreenNail(videoPath, mActivity);
        mVideoPath = videoPath;
    }

    public VideoStream(File videoPath, VideoScreenNail videoScreenNail) {
        if(!videoPath.exists()) throw new RuntimeException("video not exists ");
        mVideoScreenNail  = videoScreenNail;
        mVideoScreenNail.setmFile(videoPath);
        mVideoPath = videoPath;
    }

    @Override
    public void onDraw(GLCanvas canvas) {
        mVideoScreenNail.draw(canvas, 0, 0, mWidth, mHeight );
    }


    @Override
    protected void onCalculate(float progress) {

    }

    @Override
    public void prepare() {
        mVideoScreenNail.prepare();
    }

    @Override
    public void start() {
        if(mPlayState != PLAY_STATE_START){
            mVideoScreenNail.start();
        }
        super.start();

    }
    @Override
    public void pause() {


        if(mPlayState == PLAY_STATE_START){
            mVideoScreenNail.pause();
        }
        super.pause();
    }

    public void stop(){

        super.stop();
        mVideoScreenNail.stop();
    }

    @Override
    public void seekTo(long durationT) {

        super.seekTo(durationT);
        mVideoScreenNail.seekTo(durationT*1000);
    }

    @Override
    public void restart() {
        super.restart();
        mVideoScreenNail.restart();
    }

}
