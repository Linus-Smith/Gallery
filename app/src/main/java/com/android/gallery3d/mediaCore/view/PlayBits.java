package com.android.gallery3d.mediaCore.view;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.anim.ComboStream;
import com.android.gallery3d.mediaCore.anim.MediaStream;
import com.android.gallery3d.mediaCore.view.Inte.OnNotifyChangeListener;
import com.android.gallery3d.mediaCore.view.Inte.StateIs;
import com.android.gallery3d.mediaCore.view.Inte.VIPlayControl;

/**
 * Created by linusyang on 16-12-8.
 */

public class PlayBits implements VIPlayControl,StateIs {


    protected int mWidth;
    protected int mHeight;
    private MediaStream mCurrentMediaStream;

    private OnNotifyChangeListener mOnNotifyChangeListener;


    public void setOnNotifyChangeListener(OnNotifyChangeListener listener) {
        this.mOnNotifyChangeListener = listener;
    }

    public void setResolution(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;

    }

    public  void onDraw(GLCanvas canvas) {
        boolean requestRender = false;
        if (mCurrentMediaStream != null) {
            requestRender |= mCurrentMediaStream.calculate(MediaStream.getCurrentTime());
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            mCurrentMediaStream.apply(canvas);
            canvas.restore();
            if(mCurrentMediaStream.isCompletion()) playCompletion();
        }
        if (requestRender) mOnNotifyChangeListener.doInvalidate();
    }


    public void prepare(MediaStream mediaStream) {
        if(mediaStream instanceof ComboStream) {
            ((ComboStream) mediaStream).setPreStream(mCurrentMediaStream);
        }
        this.mCurrentMediaStream = mediaStream;
        mCurrentMediaStream.setResolution(mWidth, mHeight);
        prepare();
    }

    @Override
    public void prepare() {
        mCurrentMediaStream.prepare();
        mOnNotifyChangeListener.doInvalidate();
    }

    @Override
    public void start() {
        mCurrentMediaStream.start();
        mOnNotifyChangeListener.doInvalidate();
    }

    @Override
    public void restart() {
        mCurrentMediaStream.restart();
        mOnNotifyChangeListener.doInvalidate();
    }

    @Override
    public void pause() {
        mCurrentMediaStream.pause();
        mOnNotifyChangeListener.doInvalidate();
    }

    @Override
    public void stop() {
        mCurrentMediaStream.stop();
        mOnNotifyChangeListener.doInvalidate();
    }

    @Override
    public void seekTo(long durationT) {
        mCurrentMediaStream.seekTo(durationT);
    }

    @Override
    public int getPlayState() {
        return mCurrentMediaStream.getPlayState();
    }

    @Override
    public long getProgress() {
        return mCurrentMediaStream.getProgress();
    }

    @Override
    public void setDuration(long duration) {
        mCurrentMediaStream.setDuration(duration);
    }

    @Override
    public long getDuration() {
        return mCurrentMediaStream.getDuration();
    }

    protected void playCompletion() {
        stop();
        mOnNotifyChangeListener.notifyCompletion();
    }

}
