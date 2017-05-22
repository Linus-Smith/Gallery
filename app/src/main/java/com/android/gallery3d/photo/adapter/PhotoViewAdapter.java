package com.android.gallery3d.photo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.gallery3d.R;
import com.android.gallery3d.photo.utils.RangeArray;
import com.android.gallery3d.photo.view.PrettyImageView;

import java.util.Random;

/**
 * Created by Linus on 2017/5/16.
 */

public class PhotoViewAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener{

    public interface DataCommunicationCallBack {
        public int getItemCount();
    }

    public static final int SCREEN_NAIL_MAX = 3;
    private Context mContext;
    private DataCommunicationCallBack mDataCommunicationCallBack;

    private RangeArray<ImageView>  mCacheImages = new RangeArray<ImageView>(-SCREEN_NAIL_MAX, SCREEN_NAIL_MAX);


    public PhotoViewAdapter(Context context) {

        mContext = context;
        for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; ++i) {
            ImageView view = new ImageView(context);
            view.setBackgroundResource(R.mipmap.image1);
            mCacheImages.put(i, view);
        }
        System.out.println(mCacheImages.getSize()+"==========ss=======33");
    }

    public void setDataCommunicationCallBack(DataCommunicationCallBack dataCommunicationCallBack) {
        this.mDataCommunicationCallBack = dataCommunicationCallBack;
    }




    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = mCacheImages.get( position % (mCacheImages.getSize()), false);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mCacheImages.get(position % (mCacheImages.getSize()), false));
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        return  mDataCommunicationCallBack.getItemCount();
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

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
