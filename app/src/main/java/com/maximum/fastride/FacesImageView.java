package com.maximum.fastride;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

class FacesImageView extends View { // ImageView {

	private static final String LOG_TAG = "fast_ride";
	
	private Bitmap m_image;
	private int m_numberOfFaceDetected;
	private FaceDetector.Face[] faces;
	
    // preallocate for onDraw(...)
	private PointF tmp_point = new PointF(); 
	private Paint tmp_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	// This c'tor used when View built from layout XML.
	public FacesImageView(Context context, AttributeSet attrse) {
			    super(context, attrse);
	}

	public FacesImageView(Context context, AttributeSet attrs, int defaultStyle) {
		    super(context, attrs, defaultStyle);
	}

	public void onDraw(Canvas canvas) {
		
			super.onDraw(canvas);
			
			if( m_image != null) {
				
				Log.i(LOG_TAG, "Canvas width: " + canvas.getWidth() + " height: " + canvas.getHeight());
				Log.i(LOG_TAG, "ImageView width: " + m_image.getWidth() + " height: " + m_image.getHeight());

				canvas.drawBitmap(m_image, 0, 0, null);
				
                for (int i = 0; i < m_numberOfFaceDetected; i++) {
                        FaceDetector.Face face = faces[i];
                        tmp_paint.setColor(Color.YELLOW);
                        tmp_paint.setStyle(Paint.Style.STROKE);
                        tmp_paint.setStrokeWidth(3);
                        tmp_paint.setDither(true);

                        face.getMidPoint(tmp_point);
                        
                        float eyesDistance = face.eyesDistance();
                        canvas.drawRect(tmp_point.x - eyesDistance, 
                        				tmp_point.y - eyesDistance,
                        				tmp_point.x + eyesDistance,
                        				tmp_point.y + eyesDistance,
                        				tmp_paint);

                }

			}
	}
	
	@Override
	protected void onMeasure(int width, int height){
		setMeasuredDimension(width, height);
	}
		
	//@Override
	public void setImageBitmap(Bitmap bm){

        // In order to be detected by FaceDetector, the image must be in 565 format:
        // each pixel is stored on 2 bytes on only RGB schema,
        // 5 bits for red, 6 bits for green, 5 bits for blue
		m_image = bm.copy(Bitmap.Config.RGB_565, false);
		
        int MAX_FACES = 4;
        FaceDetector fd = new FaceDetector(m_image.getWidth(), 
        									m_image.getHeight(), 
        									MAX_FACES);
        faces = new FaceDetector.Face[MAX_FACES];
        m_numberOfFaceDetected = fd.findFaces(m_image, faces);
		
		//Drawable drawable = new BitmapDrawable(getResources(), m_image);
		//setImageDrawable(drawable);
		invalidate();
	}
	
	public int getNumberOfFacesDetected()
	{
		return m_numberOfFaceDetected;
	}

}
