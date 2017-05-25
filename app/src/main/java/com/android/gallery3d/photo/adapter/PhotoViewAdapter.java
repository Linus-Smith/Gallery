package com.android.gallery3d.photo.adapter;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.gallery3d.R;
import com.android.gallery3d.data.Log;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.photo.utils.RangeArray;
import com.android.gallery3d.photo.view.PrettyImageView;
import com.android.gallery3d.photo.view.ScreenNail;

import java.util.Random;


/**
 * Created by Linus on 2017/5/16.
 */

public class PhotoViewAdapter extends PagerAdapter implements OnPageChangeListener {


    public interface DataCommunicationCallBack extends OnLinkDataInfoListener{
        int getItemCount();
        void moveTo(int index);
        ScreenNail getScreenNail(int offset);
    }

    public interface OnLinkDataInfoListener{
        void setDataListener(PhotoDataAdapter.DataListener dataListener);
    }

    public static final int SCREEN_NAIL_MAX = 3;
    private Context mContext;
    private DataCommunicationCallBack mDataCommunicationCallBack;


    private RangeArray<PrettyImageView> mCacheImages = new RangeArray<>(-SCREEN_NAIL_MAX, SCREEN_NAIL_MAX);


    public PhotoViewAdapter(Context context) {
        mContext = context;
        for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; ++i) {
            PrettyImageView view = new PrettyImageView(context);
            mCacheImages.put(i, view);
        }
    }

    public void setDataCommunicationCallBack(DataCommunicationCallBack dataCommunicationCallBack) {
        this.mDataCommunicationCallBack = dataCommunicationCallBack;
        mDataCommunicationCallBack.setDataListener(mDataListener);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PrettyImageView imageView = mCacheImages.get(position % (mCacheImages.getSize()), false);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mCacheImages.get(position % (mCacheImages.getSize()), false));
    }

    //TODO LOW
    private void updateCacheData(int position) {
        mDataCommunicationCallBack.moveTo(position);
        int currentScrPos =  position % mCacheImages.getSize();
        int mapPos =  currentScrPos - SCREEN_NAIL_MAX;
        for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; ++i) {
            PrettyImageView prettyImageView = mCacheImages.get(i, true);
            int offset = ((mapPos - i) * -1) ;
            if(offset > SCREEN_NAIL_MAX) {
                offset = (offset - SCREEN_NAIL_MAX )* -1 ;
                if(offset == -1) offset = -3;
                else if(offset == -3) offset = -1;
            } else if(offset < -SCREEN_NAIL_MAX) {
                offset = (offset + SCREEN_NAIL_MAX )* -1;
                if(offset == 1) offset = 3;
                else if(offset == 3) offset = 1;
            }
            prettyImageView.setScreenNail(mDataCommunicationCallBack.getScreenNail(offset));
        }
    }


    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        return mDataCommunicationCallBack.getItemCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateCacheData(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private PhotoDataAdapter.DataListener mDataListener = new PhotoDataAdapter.DataListener() {
        @Override
        public void onPhotoChanged(int index, Path item) {
        }

        @Override
        public void onLoadingStarted() {

        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {

        }
    };


}
