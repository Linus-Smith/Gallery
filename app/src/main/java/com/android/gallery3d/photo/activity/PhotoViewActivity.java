package com.android.gallery3d.photo.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.android.gallery3d.R;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.photo.fragment.PhotoViewFragment;

/**
 * Created by Linus on 2017/5/16.
 */

public class PhotoViewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle mData =getIntent().getBundleExtra("DOME");
        PhotoViewFragment mPhotoViewFragment = new PhotoViewFragment();
        mPhotoViewFragment.setArguments(mData);
        mFragmentTransaction.replace(R.id.ll_page, mPhotoViewFragment);
        mFragmentTransaction.commitNow();

    }
}
