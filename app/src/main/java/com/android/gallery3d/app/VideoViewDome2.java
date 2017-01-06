package com.android.gallery3d.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.mediaCore.Utils.MediaCodecRecoder;
import com.android.gallery3d.mediaCore.Utils.MediaCodecRecoderMuti;
import com.android.gallery3d.mediaCore.anim.AlphaComboStream;
import com.android.gallery3d.mediaCore.anim.MediaStream;
import com.android.gallery3d.mediaCore.anim.VideoStream;
import com.android.gallery3d.mediaCore.anim.ZoomBmStream;
import com.android.gallery3d.mediaCore.recoder.EncodeAndMuxTest;
import com.android.gallery3d.mediaCore.recoder.RecoderRender;
import com.android.gallery3d.mediaCore.recoder.TestGLCanvas;
import com.android.gallery3d.mediaCore.view.VideoView;
import com.android.gallery3d.ui.GLRootView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by linusyang on 16-12-1.
 */

public class VideoViewDome2 extends Activity implements VideoView.PlayStateListener, View.OnClickListener {

    private GLRootView mView;
    private VideoView mVideo;
    private RecoderRender mRender;
    private int bitmapIndex = 1;
    private Button btPrepare;
    private Button btStart;
    private Button btRestartr;
    private Button btPause;
    private Button btStop;
    private Button btPlayState;
    private SeekBar mSeekBar;

    private TextView tvCuTime;
    private TextView tvDuTime;
    private boolean isSeek;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvCuTime.setText(mVideo.getProgress() + " ");
            mSeekBar.setProgress((int) mVideo.getProgress());
            mHandler.sendEmptyMessageDelayed(0, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_dome);
        mView = (GLRootView) findViewById(R.id.gl_root);
        mVideo = new VideoView(this);
        mRender = new RecoderRender(this);

        btPrepare = (Button) findViewById(R.id.bt_prepare);
        btStart = (Button) findViewById(R.id.bt_start);
        btRestartr = (Button) findViewById(R.id.bt_restart);
        btPause = (Button) findViewById(R.id.bt_pause);
        btStop = (Button) findViewById(R.id.bt_stop);
        btPlayState = (Button) findViewById(R.id.bt_play_state);
        tvCuTime = (TextView) findViewById(R.id.tv_cu_time);
        tvDuTime = (TextView) findViewById(R.id.tv_du_time);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        //setListener
        setSeekChangeListener();
        btPrepare.setOnClickListener(this);
        btStart.setOnClickListener(this);
        btRestartr.setOnClickListener(this);
        btPause.setOnClickListener(this);
        btStop.setOnClickListener(this);
        btPlayState.setOnClickListener(this);
        mView.setContentPane(mVideo);
        mVideo.setPlayStateListener(this);
        findViewById(R.id.recoder).setOnClickListener(this);
    }

    private Bitmap getBitmap() {
        if (bitmapIndex == 5) {
            bitmapIndex = 1;
            return  null;
        }

        int bitmapId = getResources().getIdentifier("image" + bitmapIndex, "mipmap", getPackageName());
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        //  mOptions.inSampleSize = 2;
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), bitmapId, mOptions);
        bitmapIndex++;
        return mBitmap;
    }

    private File getFile(boolean isFirst){
        if(isFirst){
            return new File("/storage/emulated/0/1.mp4");
        }else {
            return new File("/storage/emulated/0/sintel.mp4");
        }

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_prepare:
                handlerPrepareClick();
                break;
            case R.id.bt_start:
                handlerStartClick();
                break;
            case R.id.bt_restart:
                handlerRestartClick();
                break;
            case R.id.bt_pause:
                handlerPauseClick();
                break;
            case R.id.bt_stop:
                handlerStopClick();
                break;
            case R.id.bt_play_state:
                handlerPlayStateClick();
                break;
            case R.id.recoder:
                recodeList();
                break;
        }
    }

    private void recode() {
        new Thread(){
            @Override
            public void run() {
                String path2 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sintel.mp4";
                MediaCodecRecoder recoder = new MediaCodecRecoder();
                MediaCodecRecoder.SaveTask task = new MediaCodecRecoder.SaveTask(recoder);
                try {
                    recoder.saveVideo(new File(path2));
                    task.execute();

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }.start();

    }

    private void recode2(){
        new Thread() {
            public void run() {
                String path1 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/1.mp4";
                String path2 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sintel.mp4";
                MediaCodecRecoderMuti recoder = new MediaCodecRecoderMuti(VideoViewDome2.this);


                MediaCodecRecoderMuti.SaveTask task = new MediaCodecRecoderMuti.SaveTask(recoder);
                try {
                    ArrayList<File> files = new ArrayList<File>();
                    files.add(new File(path1));
                    files.add(new File(path2));
                    recoder.saveVedios(files);
                    task.execute();

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }.start();
    }
    private void recode3(){
        TestGLCanvas test = new TestGLCanvas(VideoViewDome2.this);
        test.testEncodeVideoToMp4();
    }
    private void recodeList(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                mRender.startEncoder();
            }
        }.start();

    }


    private void handlerPrepareClick() {
        ZoomBmStream mZoomBmStream = new ZoomBmStream(getBitmap(), 0);
        mVideo.prepare(mZoomBmStream);
        mVideo.setDuration(3000);
        tvDuTime.setText(mVideo.getDuration() + " ");
        mSeekBar.setMax((int) mVideo.getDuration());
        mSeekBar.setProgress(0);

    }

    private void handlerPrepareVedio() {
        File mFile = new File("/storage/emulated/0/1.mp4");

        MediaStream mMediaStream = null;
        AlphaComboStream  alphaComboStrea = new AlphaComboStream( new VideoStream(mFile, mVideo.getVideoScreenNail()) );
        alphaComboStrea.setTransitionAnimDuration(1000);
        mMediaStream = alphaComboStrea;
        mVideo.prepare(mMediaStream);
        mVideo.setDuration(3000);
        tvDuTime.setText(mVideo.getDuration() + " ");
        mSeekBar.setMax((int) mVideo.getDuration());
        mSeekBar.setProgress(0);

    }


    private void handlerStartClick() {
        mVideo.start();
        mHandler.sendEmptyMessage(0);
    }

    private void handlerRestartClick() {
        mVideo.restart();
        mHandler.sendEmptyMessage(0);
    }

    private void handlerPauseClick() {
        mVideo.pause();
        mHandler.removeMessages(0);
    }

    private void handlerStopClick() {
        mVideo.stop();
        mHandler.removeMessages(0);
    }

    private void handlerPlayStateClick() {
        int playState = mVideo.getPlayState();
    }

    boolean isfirst;
    @Override
    public void onCompletion() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("jzf","oncall");
//                mVideo.stop();
                isfirst=!isfirst;
                File mFile = getFile(isfirst);

                MediaStream mMediaStream = null;
                Bitmap mBitmap = null;
                if(mBitmap != null) {
                 AlphaComboStream  alphaComboStream = new AlphaComboStream(new ZoomBmStream(mBitmap , 0));
                    alphaComboStream.setTransitionAnimDuration(1000);
                  //  alphaComboStream.setTransitionPlayMode(StateIs.TRANSITION_PLAY_MODE_ISOLATE);
                    mMediaStream = alphaComboStream;
                } else {
                    AlphaComboStream  alphaComboStrea = new AlphaComboStream( new VideoStream(mFile, mVideo.getVideoScreenNail()) );
                    alphaComboStrea.setTransitionAnimDuration(1000);
                    mMediaStream = alphaComboStrea;
                }
                mVideo.prepare(mMediaStream);
                mVideo.setDuration(4000);
                tvDuTime.setText(mVideo.getDuration() + " ");
                mSeekBar.setMax((int) mVideo.getDuration());
                mSeekBar.setProgress(0);
                mVideo.start();
            }
        });
    }

    private void setSeekChangeListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(isSeek){
                    mVideo.seekTo(i);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeek = false;
            }
        });
    }


}
