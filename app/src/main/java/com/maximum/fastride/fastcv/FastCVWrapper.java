package com.maximum.fastride.fastcv;

import org.opencv.core.MatOfRect;

/**
 * Created by Oleg Kleiman on 05-Jul-15.
 */
public class FastCVWrapper {
//
//    static {
//        System.loadLibrary("fastcvUtils");
//    }

    public String pathToCascade;

    public FastCVWrapper(String path){
        pathToCascade = path;
    }

    public native void FrameTick();
    public native void FindFeaturesFAST(long matAddrGr, long matAddrRgba);
    public native void FindFeaturesORB(long matAddrGr, long matAddrRgba);
    public native void FindFeaturesKAZE(long matAddrGr, long matAddrRgba);
    public native int DetectFaces(long matAddrRgba, String face_cascade_name);
    public native void Blur(long matAddrGr);
}
