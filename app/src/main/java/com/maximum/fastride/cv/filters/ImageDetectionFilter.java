package com.maximum.fastride.cv.filters;

import android.content.Context;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
//import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

/**
 * Created by Oleg Kleiman on 07-Jul-15.
 */
public class ImageDetectionFilter implements Filter {

    private final Mat mReferenceImage;
    private final Mat mReferenceDescriptors = new Mat();
    private final MatOfKeyPoint mReferenceKeypoints = new MatOfKeyPoint();
    // CVType defines the color depth, number of channels, and channel layout in the image
    private final Mat mReferenceCorners = new Mat(4, 1, CvType.CV_32FC1); // TODO: CV_32FC

    private final MatOfKeyPoint mSceneKeypoints = new MatOfKeyPoint();
    private final Mat mSceneCorners = new Mat(4, 1, CvType.CV_32FC1); // TODO: CV_32FC
    private final Mat mSceneDescriptors = new Mat();

    private final Mat mGraySrc = new Mat();
    private final MatOfDMatch mMatches = new MatOfDMatch();

    private final FeatureDetector mFeatureDetector =
            FeatureDetector.create(FeatureDetector.STAR);
    private final DescriptorExtractor mDescriptorExtractor =
            DescriptorExtractor.create(DescriptorExtractor.FREAK);
    private final DescriptorMatcher mDescriptorMatcher =
            DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    private final Scalar mLineColor = new Scalar(0, 255, 0);

    public ImageDetectionFilter(final Context context,
                                final int referenceImageResourceID)
        throws IOException {
        mReferenceImage = Utils.loadResource(context, referenceImageResourceID, 0);
//                                            Highgui.CV_LOAD_IMAGE_COLOR);


        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2BGRA);

        mReferenceCorners.put(0, 0,
                new double[]{0.0, 0.0});
        mReferenceCorners.put(1, 0,
                new double[]{referenceImageGray.cols(), 0, 0});
        mReferenceCorners.put(2, 0,
                new double[]{referenceImageGray.cols(), referenceImageGray.rows()});
        mReferenceCorners.put(3, 0,
                new double[] { 0.0, referenceImageGray.rows() });

        mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints, mReferenceDescriptors);
    }

    @Override
    public void apply(Mat src, Mat dst) {
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);

        mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
        mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints, mSceneDescriptors);
        mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors, mMatches);

        findScreenCorners();
        draw(src, dst);

    }

    private void findScreenCorners() {

    }

    protected void draw(Mat src, Mat dst) {
        if( dst != src )
            src.copyTo(dst);

        if( mSceneCorners.height() < 4 ) {
            // The target has not been found

            // Draw a thumbnail of the target in the upper-left
            // corner so that the user knows what it is.
            int height = mReferenceImage.height();
            int width = mReferenceImage.width();

            int maxDimension = Math.min(dst.width(), dst.height()) / 2;
            double aspectRatio = width / (double)height;
            if( height > width) {
                height = maxDimension;
                width = (int) (height * aspectRatio);
            } else {
                width = maxDimension;
                height = (int) (width / aspectRatio);
            }

            Mat dstROI = dst.submat(0, height, 0, width);
            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(),
                    0.0, 0.0, Imgproc.INTER_AREA);
            return;
        }

//        Core.line(dst,
//                new Point(mSceneCorners.get(0,0)),
//                new Point(mSceneCorners.get(1, 0)),
//                mLineColor, 4);
//        Core.line(dst,
//                  new Point(mSceneCorners.get(1, 0)),
//                  new Point(mSceneCorners.get(2, 0)),
//                  mLineColor, 4);
//        Core.line(dst,
//                new Point(mSceneCorners.get(2, 0)),
//                new Point(mSceneCorners.get(3, 0)),
//                mLineColor, 4);
//        Core.line(dst,
//                new Point(mSceneCorners.get(0, 0)),
//                new Point(mSceneCorners.get(0, 0)),
//                mLineColor, 4);
    }
}
