package com.android.gallery3d.mediaCore.recoder;

/**
 * Created by bruce.jiang on 2016/12/21.
 */

public interface RecoderInterface {

    void prepare();
    void start();
    void pause();
    void stop();
    void setDuration(long duration);
}
