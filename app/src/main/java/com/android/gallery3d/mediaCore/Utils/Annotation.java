package com.android.gallery3d.mediaCore.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by linusyang on 16-12-8.
 */

public class Annotation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface IInterface {
        String value();
    }
}
