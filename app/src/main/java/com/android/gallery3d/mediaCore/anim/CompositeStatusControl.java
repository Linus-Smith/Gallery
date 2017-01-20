package com.android.gallery3d.mediaCore.anim;

import com.android.gallery3d.mediaCore.Utils.Utils;

/**
 * Created by linusyang on 17-1-14.
 */

public class CompositeStatusControl extends BaseStatusControl {

    protected final long ONE_MILLION = 1000000L;

    @Override
    CalculateEntry calculate(long currentTimeMillis) {
        CalculateEntry mCalculateEntry = new CalculateEntry();
        long elapse =  currentTimeMillis - mStartTime;
        long duration = mDuration;
        mCurrentDurationTime = elapse > duration ? duration : elapse;
        float x = Utils.clamp((float) elapse / duration, 0f, 1f);
        mCalculateEntry.isCanCalculate = true;
        mCalculateEntry.value = x;
        return mCalculateEntry;
    }

    public void setStartTime(long startTime){
        mStartTime = startTime;
    }

    public long getStartTime() {
        return mStartTime;
    }


    @Override
    boolean isCompletion() {
      return  mDuration * ONE_MILLION <= mCurrentDurationTime;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        CompositeStatusControl compositeStatusControl = new CompositeStatusControl();
        compositeStatusControl.setStartTime(mStartTime);
        return compositeStatusControl;
    }
}
