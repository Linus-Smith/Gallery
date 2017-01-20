package com.android.gallery3d.mediaCore.view.Inte;

/**
 * Created by linusyang on 16-12-8.
 */

public interface StatusIs {

    //Play State
     int PLAY_STATE_START = 1 << 0;
     int PLAY_STATE_PAUSE = 1 << 2;
     int PLAY_STATE_STOP = 1 << 3;

     long NO_ANIMATION = -2;

    //changePlayMode
    int TRANSITION_PLAY_MODE_MERGE = 2 << 0;
    int TRANSITION_PLAY_MODE_ISOLATE = 2 << 1;


    //animationType

}
