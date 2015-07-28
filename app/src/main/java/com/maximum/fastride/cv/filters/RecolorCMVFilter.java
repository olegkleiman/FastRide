package com.maximum.fastride.cv.filters;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by Oleg Kleiman on 07-Jul-15.
 */

// The effect of this filter is to desaturate yellows,
// leaving a limited color palette of cyan, magenta, and white.
// (similar to games of the 1980s)
public class RecolorCMVFilter implements Filter {

    private final ArrayList<Mat> mChannels = new ArrayList<>(4);

    @Override
    public void apply(Mat src, Mat dst) {
        Core.split(src, mChannels);
        // 0 - red
        // 1 - green
        // 2 - blue
        Mat r = mChannels.get(0);
        Mat g = mChannels.get(1);
        Mat b = mChannels.get(2);

        // dst.b = max(dst.r, dst.g, dst.b)
        Core.max(b, r, b);
        Core.max(b, g, b);

        mChannels.set(3, b);
        Core.merge(mChannels, dst);
    }
}
