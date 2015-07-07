package com.maximum.fastride.cv.filters;

import org.opencv.core.Mat;

/**
 * Created by Oleg on 07-Jul-15.
 */
public interface Filter {
    public abstract void apply(final Mat src, final Mat dst);
}
