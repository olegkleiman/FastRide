package com.maximum.fastride;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;


public enum ImageDataHolder {

    INSTANCE;

    public boolean loadData(Uri uri, Activity _activity) {
        mActivity = _activity;
        try {
            mBitmap = getBitmapFromInputStream(mActivity.getContentResolver().openInputStream(uri), mActivity.getContentResolver().openInputStream(uri));
            compress();
        } catch(IOException e) {
            return false;
        }
        return true;
    }

    private Bitmap getBitmapFromInputStream(InputStream is, InputStream is2) throws IOException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, o);
        nImage_width = o.outWidth;
        nImage_height = o.outHeight;
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int nScreen_width = size.x;
        int sSreen_height = size.y;
        int nDownSize = 1;
        while (true) {
            if ((nImage_width / nDownSize < nScreen_width) && (nImage_height / nDownSize < sSreen_height )) {
                break;
            }
            nDownSize *= 2;
        }
        o.inSampleSize = nDownSize;
        o.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeStream(is2, null, o);
        return bitmap;
    }


    private void compress() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byteData = baos.toByteArray();
    }

    public void recoverFromByteArray(byte[] byteArray) {
        byteData = byteArray;
        mBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private Bitmap mBitmap;
    private byte[] byteData ;
    private Activity mActivity;

    int nImage_width;
    int nImage_height;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public byte[] getByteArray() {
        return byteData;
    }
}
