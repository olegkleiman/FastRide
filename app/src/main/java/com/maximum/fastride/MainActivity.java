package com.maximum.fastride;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rekognition.RekoSDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.azure.storage.*;


public class MainActivity extends Activity {

    static final int REGISTER_USER_REQUEST = 1;

	static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final String LOG_TAG = "fast_ride";
	private ProgressDialog mDialog = null;

    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPrefs.getString("username", "");

        if( username.isEmpty() ) {

            try {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER_USER_REQUEST);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            mClient = new MobileServiceClient("https://fastride.azure-mobile.net/",
                    "omCudOMCUJgIGbOklMKYckSiGKajJU91",
                    this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void onUseCamera(View v){
    	Intent intent = new Intent(this, CameraActivity.class);
    	startActivity(intent);
    }
    
	public void onTakePicture(View v) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		try {
			this.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}catch(Exception ex) {
        	Log.e(LOG_TAG, ex.getMessage());
        }

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == REGISTER_USER_REQUEST && resultCode == RESULT_OK) {

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			if( data != null) {
				
				try{
	    		Bundle extras = data.getExtras();
		        Bitmap imageBitmap = (Bitmap) extras.get("data");
		        
		        // In order to be detected by FaceDetector, the image must be in 565 format:
		        // each pixel is stored on 2 bytes on only RGB schema,
		        // 5 bits for red, 6 bits for green, 5 bits for blue
//		        Bitmap imgBitmap = imageBitmap.copy(Bitmap.Config.RGB_565, false);
//		        if( imgBitmap != null ){
//
//			        int MAX_FACES = 4;
//			        FaceDetector fd = new FaceDetector(imgBitmap.getWidth(), 
//			        									imgBitmap.getHeight(), 
//			        									MAX_FACES);
//			        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
//			        int numberOfFaceDetected = fd.findFaces(imgBitmap, faces);
//			        
//			        for (int i = 0; i < numberOfFaceDetected; i++) {
//			        	Face face = faces[i];
//			        	
//			        	PointF midPoint = new PointF();
//			        	face.getMidPoint(midPoint);
//			        	
//			        	float eyesDistance = face.eyesDistance();
//			        }
//			        
//			        TextView txtView = (TextView)findViewById(R.id.txtFacesDetected);
//			        txtView.setText(Integer.toString(numberOfFaceDetected));
			        
			        FacesImageView imageView = (FacesImageView)findViewById(R.id.faceDetectedImageView);
			        Log.i(LOG_TAG,
			        	"Image width: " + Integer.toString(imageBitmap.getWidth()) + " height: " + Integer.toString(imageBitmap.getHeight()));
			        imageView.setImageBitmap(imageBitmap);
			        int nDetectedFaces = imageView.getNumberOfFacesDetected();
			        
			        TextView textView = (TextView)findViewById(R.id.txtFacesDetected);
			        textView.setText(Integer.toString(nDetectedFaces));
			        
			        ImageView rawImageView = (ImageView)findViewById(R.id.rawImageView);
			        rawImageView.setImageBitmap(imageBitmap);
			        
		        //}
		        
				} catch(Exception ex) {
					Log.e(LOG_TAG, ex.getMessage());
				}
		        
//		        Uri chosenImageUri = data.getData();
//		        
//		        FileOutputStream out = null;
		        
//		        try {
//		        	File outputDir = this.getCacheDir();
//		        	String imageFileName = getTempFileName();
//		        	
//		        	File photoFile = File.createTempFile(imageFileName, ".png", outputDir);
//		        	out = new FileOutputStream(photoFile);
//		        	
//		        	imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		        	
//		        	ImageDataHolder.INSTANCE.loadData(chosenImageUri, this);
//		        	onFaceDetection(null);
		        	
//		        }catch (IOException e) {
//					Log.e(LOG_TAG, e.getMessage());
//				}
			}
		}
	}
	
	// Generates random file name 
	@SuppressLint("SimpleDateFormat") 
	private String getTempFileName() {
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "Montreux_" + timeStamp;
	}	
	
    public void onFaceDetection(View view) {
        RekoSDK.APICallback callback = new RekoSDK.APICallback() {
            public void gotResponse(String sResponse) {
            	
            	Log.i(LOG_TAG, sResponse);
            	final ImageView imageView = (ImageView)findViewById(R.id.faceDetectedImageView);

            	Bitmap bitmap = null;
            	try {
					JSONObject jsonRoot = new JSONObject(sResponse);
					if (jsonRoot.has("face_detection")) {
						bitmap = ImageDataHolder.INSTANCE.getBitmap().copy(Bitmap.Config.ARGB_8888 ,true);
						JSONArray jsonArray = jsonRoot.getJSONArray("face_detection");
						
		                int count = 0;
		                for (int i = 0; i < jsonArray.length(); i++) {
		                	JSONObject json = jsonArray.getJSONObject(i);
		                	if (json.getDouble("confidence") >= 0.1) {
		                		count ++;
		                	}
		                }
					}
				} catch (JSONException e) {
					 Log.v("Exception", e.getMessage());
				}
            	
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mDialog.dismiss();
                        
                        Bitmap bmp = ImageDataHolder.INSTANCE.getBitmap();
                        imageView.setImageBitmap(bmp);
                    }
                });
 
            }
        };
        
        mDialog = ProgressDialog.show(this, "", "Processing...");
        RekoSDK.face_detect(ImageDataHolder.INSTANCE.getByteArray(), callback);
    }
}
