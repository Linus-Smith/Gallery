package com.android.gallery3d.app;

import android.app.Activity;
import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.mediaCore.view.VideoView;
import com.android.gallery3d.ui.GLRootView;

/**
 * Created by bruce.jiang on 2016/11/30.
 */

public class VideoDomeActivity extends Activity {


    private GLRootView mView;
    private VideoView mVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_dome);
        mView = (GLRootView) findViewById(R.id.gl_root);

    }

    public GLRootView getGLRoot() {
        return mView;
    }


}
