package com.android.gallery3d.photo.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.gallery3d.photo.adapter.PhotoViewAdapter;
import com.android.gallery3d.ui.PhotoView;

/**
 * Created by Linus on 2017/5/16.
 */

public class PhotoViewPage extends ViewPager {

    public PhotoViewPage(Context context) {
        super(context);
    }

    public PhotoViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
