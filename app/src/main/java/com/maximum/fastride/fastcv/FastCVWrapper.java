package com.maximum.fastride.fastcv;

/**
 * Created by Oleg Kleiman on 05-Jul-15.
 */
public class FastCVWrapper {
//
//    static {
//        System.loadLibrary("fastcvUtils");
//    }

    public FastCVWrapper(){

    }

    public native void FrameTick();
    public native void FindFeaturesFAST(long matAddrGr, long matAddrRgba);
    public native void FindFeaturesORB(long matAddrGr, long matAddrRgba);
    public native void FindFeaturesKAZE(long matAddrGr, long matAddrRgba);
    public native void DetectFaces(long matAddrRgba, String face_cascade_name);
    public native void Blur(long matAddrGr);
}
