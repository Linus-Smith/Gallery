package com.android.gallery3d.mediaCore.recorder;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bruce.jiang on 2017/1/4.
 */

public class CompositeVideo {
    private final String TAG = "CompositeVideo";

    private final static String TIME_STAMP_NAME = "'MyStory'_yyyyMMdd_HHmmss";

    private Context mContext;
    private RecorderRender mRender;
    private OnCompositeListenerInfo mOnCompositeListenerInfo;
    private String mSavePath;
    private MediaScannerConnection mMediaScannerConnection = null;
    //private StoryAlbum mStoryAlbum;

    public interface OnCompositeListenerInfo {
        void setSavePath(String path);

        void onSetCompositeProgress(int progress);

        void onSaveComplete(Uri uri);
    }

//    public CompositeVideo(Context context, StoryAlbum storyAlbum) {
//        mContext = context;
//        mMediaScannerConnection = new MediaScannerConnection(mContext, new MediaScannerClient());
//        mStoryAlbum = storyAlbum;
//    }

    public void setOnCompositeListenerInfo(OnCompositeListenerInfo onCompositeListenerInfo) {
        mOnCompositeListenerInfo = onCompositeListenerInfo;
    }


    public void startComposite(String savePath) {
        this.mSavePath = savePath;
        mRender =  new RecorderRender();
        File file = new File(mSavePath);
        if (!file.exists()) {
            file.mkdir();
        }

        mRender.setOnCompositeListener(new RecorderRender.OnCompositeListener() {
            @Override
            public void onSetCompositeProgress(int progress) {
                mOnCompositeListenerInfo.onSetCompositeProgress(progress);
            }

        });

        mSavePath = mSavePath + "/" + new SimpleDateFormat(TIME_STAMP_NAME).format(
                new Date(System.currentTimeMillis())) + ".mp4";
        mRender.setBGMusic(Environment.getExternalStorageDirectory().getAbsolutePath()+"/sintel.mp4");
        mRender.setTotalTime(30000);

        mRender.setSavePath(mSavePath);
        try {
            mRender.prepareEncoder();
        } catch (IOException e) {
            e.printStackTrace();
        }

    //    mRender.setDataSource(mStoryAlbum.getClips());
        mOnCompositeListenerInfo.setSavePath(mSavePath);
        new Thread(){
            @Override
            public void run() {
                super.run();
                mRender.startEncoder();
            }
        }.start();


    }
    public void stopCmposite(){
        if(mRender!=null){
            mRender.stopEncoder();
        }
    }


    class MediaScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {

        @Override
        public void onMediaScannerConnected() {
            if (mMediaScannerConnection != null) {
                mMediaScannerConnection.scanFile(mSavePath, "video/*");
            }
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (mMediaScannerConnection != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("zs_type", 64);
                mContext.getContentResolver().update(uri, contentValues, null, null);
                mOnCompositeListenerInfo.onSaveComplete(uri);
                mMediaScannerConnection.disconnect();
            }
        }
    }

}
