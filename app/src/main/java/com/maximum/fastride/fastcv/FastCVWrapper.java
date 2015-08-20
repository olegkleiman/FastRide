package com.maximum.fastride.fastcv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

/**
 * Created by Oleg Kleiman on 05-Jul-15.
 */
public class FastCVWrapper {


    public String pathToCascade;

    public FastCVWrapper(String cascadeFilePath){

        pathToCascade = cascadeFilePath; // TODO: remove this member varibale
        mNativeObj = nativeCreateObject(cascadeFilePath);
    }

    public native void FrameTick();
    public native void FindFeaturesFAST(long matAddrGr, long matAddrRgba);
    public native void FindFeaturesORB(long matAddrGr, long matAddrRgba);
    public native void FindFeaturesKAZE(long matAddrGr, long matAddrRgba);
    public native int DetectFaces(long matAddrRgba, String face_cascade_name);
    public native void Blur(long matAddrGr);

    public int detect(Mat imageGray, int minFaceSize) {
        return nativeDetectFaces(imageGray.getNativeObjAddr(), mNativeObj, minFaceSize);
    }

    private native long nativeCreateObject(String cascadeFileName);
    private native int nativeDetectFaces(long matAddrRgba, long thiz, int minFaceSize);

    private long mNativeObj = 0;
}
