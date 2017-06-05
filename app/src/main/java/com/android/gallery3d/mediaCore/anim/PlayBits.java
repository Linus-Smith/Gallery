package com.android.gallery3d.mediaCore.anim;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.view.Inte.OnNotifyChangeListener;
import com.android.gallery3d.mediaCore.view.Inte.StatusIs;

/**
 * Created by linusyang on 16-12-8.
 */

public class PlayBits extends BaseBits {

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
        if (mCurrentMediaStream != null) {
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            mCurrentMediaStream.apply(canvas);
            canvas.restore();
        }

    }


    public void prepare(ComboStream comboStream, long playDuration, long transitionDuration, int transitionMode) {
        comboStream.setPreStream(mCurrentMediaStream);
        comboStream.setTransitionPlayMode(transitionMode);
        comboStream.setDuration(transitionDuration);
        long tempDuration;
        if(transitionDuration == StatusIs.TRANSITION_PLAY_MODE_ISOLATE) {
            tempDuration = playDuration - transitionDuration;
        } else {
            tempDuration = playDuration;
        }
        mCurrentMediaStream = comboStream;
        mCurrentMediaStream.setResolution(mWidth, mHeight);
        comboStream.setTotalDuration(tempDuration);
        mCurrentMediaStream.getCurrentStream().setDuration(playDuration);
        prepare();
    }

    public void prepare(MediaStream mediaStream, long playDuration) {
        if(mediaStream instanceof  ComboStream) throw new RuntimeException("prepare error");
        this.mCurrentMediaStream = mediaStream;
        mCurrentMediaStream.setResolution(mWidth, mHeight);
        mCurrentMediaStream.setDuration(playDuration);
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

    public void startRecord(long beginTime){
        mCurrentMediaStream.start();
    }

    public void record(GLCanvas canvas,long timeStamp){
        if (mCurrentMediaStream != null) {

            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            mCurrentMediaStream.apply(canvas);
            canvas.restore();
        }
    }
    @Override
    public void seekTo(long durationT) {
        mCurrentMediaStream.seekTo(durationT);
        mOnNotifyChangeListener.doInvalidate();
    }

    @Override
    public int getPlayState() {
        return mCurrentMediaStream.getPlayState();
    }

    @Override
    public long getProgress() {
        return mCurrentMediaStream.getProgress();
    }

    @Deprecated
    @Override
    public void setDuration(long duration) {
        throw new RuntimeException("please call prepare");
    }

    @Override
    public long getDuration() {
        return mCurrentMediaStream.getDuration();
    }


    public boolean calculate(long currentTimeMillis) {
        if(mCurrentMediaStream == null) return false;
        return mCurrentMediaStream.calculate(currentTimeMillis);
    }

    public boolean isCompletion() {
        if(mCurrentMediaStream == null) return false;
        return mCurrentMediaStream.isCompletion();
    }

}
