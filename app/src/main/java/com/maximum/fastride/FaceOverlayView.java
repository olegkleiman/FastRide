package com.maximum.fastride;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera.Face;
import android.view.View;

public class FaceOverlayView extends View {

	private Paint mPaint;
	private Paint mTextPaint;
	
	private int mDisplayOrientation;
	private int mOrientation;
	
	private Face[] mFaces;
	
	public FaceOverlayView(Context context) {
		super(context);
		initialize();
	}
	
	private void initialize() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.YELLOW);
		mPaint.setAlpha(128);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setDither(true);
		mTextPaint.setColor(Color.GREEN);
		mTextPaint.setStyle(Paint.Style.FILL);
	}
	
	public void setDisplayOrientation(int displayOrientation) {
		mDisplayOrientation = displayOrientation;
	}
	
	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public void setFaces(Face[] faces){
		mFaces = faces;
	}
	
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		if( mFaces != null && mFaces.length > 0) {
			canvas.save();
			canvas.rotate(-mOrientation);
			
			RectF rectF = new RectF();
			for(Face face: mFaces) {
				Matrix matrix = new Matrix();
				rectF.set(face.rect);
				matrix.mapRect(rectF);
				
				canvas.drawRect(rectF, mPaint);
			}
			
			canvas.restore();
		}
	}
}
