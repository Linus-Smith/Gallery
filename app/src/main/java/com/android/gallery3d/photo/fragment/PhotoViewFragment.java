package com.android.gallery3d.photo.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AppBridge;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.data.ComboAlbum;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.FilterDeleteSet;
import com.android.gallery3d.data.FilterSource;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.SecureAlbum;
import com.android.gallery3d.data.SecureSource;
import com.android.gallery3d.data.SnailAlbum;
import com.android.gallery3d.data.SnailItem;
import com.android.gallery3d.data.SnailSource;
import com.android.gallery3d.photo.adapter.PhotoDataAdapter;
import com.android.gallery3d.photo.adapter.PhotoViewAdapter;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.UsageStatistics;

/**
 * Created by Linus on 2017/5/16.
 */

public class PhotoViewFragment extends Fragment {

    private ViewGroup mRootView;
    private ViewPager mViewPager;
    private PhotoDataAdapter mPhotoDataAdapter;
    private PhotoViewAdapter mPhotoViewAdapter;
    private String mSetPathString;

    private FilterDeleteSet mMediaSet;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        initView(inflater, container);
        initData();
        return mRootView;
    }




    private void initView(LayoutInflater inflater, ViewGroup container) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_photoview, container, false);
        mViewPager = (ViewPager) mRootView.findViewById(R.id.vp_pager);
    }

    private void initData() {
         Bundle data = getArguments();
        mSetPathString = data.getString(PhotoPage.KEY_MEDIA_SET_PATH);
        String itemPathString = data.getString(PhotoPage.KEY_MEDIA_ITEM_PATH);
        Path itemPath = itemPathString != null ?
                Path.fromString(data.getString(PhotoPage.KEY_MEDIA_ITEM_PATH)) :
                null;
        int currentIndex = data.getInt(PhotoPage.KEY_INDEX_HINT, 0);
        if (mSetPathString != null) {
            mSetPathString = "/filter/delete/{" + mSetPathString + "}";
            mMediaSet = (FilterDeleteSet) DataManager.from(mContext)
                    .getMediaSet(mSetPathString);
        }

        mPhotoDataAdapter = new PhotoDataAdapter(mContext, mMediaSet, itemPath, currentIndex);
        mPhotoViewAdapter = new PhotoViewAdapter(getActivity());
        mPhotoViewAdapter.setDataCommunicationCallBack(mPhotoDataAdapter);
        mViewPager.setAdapter(mPhotoViewAdapter);
        mViewPager.addOnPageChangeListener(mPhotoViewAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPhotoDataAdapter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPhotoDataAdapter.pause();
    }
}
