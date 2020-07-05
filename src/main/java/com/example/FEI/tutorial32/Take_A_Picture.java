package com.example.haotian.tutorial32;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Take_A_Picture extends Activity {

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    public final String TAG = "MyActivity";


    /**
     * A basic Camera preview class
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            Display display = getWindowManager().getDefaultDisplay();
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                if(display.getRotation() == Surface.ROTATION_0){
                    mCamera.setDisplayOrientation(90);
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setRotation(90); //set rotation to save the picture
                    mCamera.setParameters(parameters);
                }
                if(display.getRotation() == Surface.ROTATION_90){
                    mCamera.setDisplayOrientation(0);
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setRotation(180); //set rotation to save the picture
                    mCamera.setParameters(parameters);
                }
                if(display.getRotation() == Surface.ROTATION_270){
                    mCamera.setDisplayOrientation(180);
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setRotation(180); //set rotation to save the picture
                    mCamera.setParameters(parameters);
                }
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }


    /**
     * Create a File for saving an image or video
     */

    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File rootsd = Environment.getExternalStorageDirectory();
        File mediaStorageDir = new File(rootsd.getAbsolutePath() + "/DCIM/GoogleMapPhoto/");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        int currentImageNumber = 1;
        String currentPic = String.valueOf(currentImageNumber);
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                currentPic + ".jpg");
        ;
        while (mediaFile.exists()) {
            currentImageNumber = currentImageNumber + 1;
            currentPic = String.valueOf(currentImageNumber);
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    currentPic + ".jpg");
        }

        picturenumber = currentImageNumber;
        return mediaFile;
    }

    //Capturing pictures
    private PictureCallback mPicture = new PictureCallback() {


        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(1);

            //save the picure information for later saving
            picturefile = pictureFile;
            picureData = data;
        }
    };


    private File picturefile;
    private byte[] picureData = null;
    public int picturenumber;
    private int finishMark = 0;


    private Camera mCamera;
    private CameraPreview mPreview;
    private final int MY_PERMISSIONS_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE_AND_CAMERA_AND_LOCATION = 1337;
    public String TimeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    private String Latitude;
    private String Longitude;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take__a__picture);

        this.requestPermissions(new String[]
                        {Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE_AND_CAMERA_AND_LOCATION);

        // check if GPS enabled
        GPSTracker gpsTracker = new GPSTracker(this);

        if (gpsTracker.getIsGPSTrackingEnabled())
        {
            latitude = gpsTracker.latitude;
            longitude = gpsTracker.longitude;

            Latitude = String.valueOf(gpsTracker.latitude);

            Longitude = String.valueOf(gpsTracker.longitude);
        }

            // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        findViewById();
        addbuttonlistener();
    }

    //set the buttons' function
    private Button button_retake, button_takepic, button_save, button_clear;

    public void findViewById() {
        button_takepic= (Button) findViewById(R.id.button_takePicture);
        button_retake = (Button) findViewById(R.id.button_Retake);
        button_save = (Button) findViewById(R.id.button_OK);
        button_clear= (Button) findViewById(R.id.button_cancel);
    }

    public void addbuttonlistener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View arg) {
                if (arg == button_takepic) {
                    //Button take photos
                    if (picureData == null){
                        mCamera.takePicture(null, null, mPicture);
                        finishMark = 0;
                    }
                } else if (arg == button_retake) {
                    //Button retake
                    mCamera.startPreview();
                    picureData = null;
                    picturefile = null;
                } else if (arg == button_save) {
                    //Button save taken photo
                    if (picturefile != null) {
                        // save the picture
                        try {
                            FileOutputStream fos = new FileOutputStream(picturefile);
                            fos.write(picureData);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d(TAG, "Error accessing file: " + e.getMessage());
                        }

                        // save photo information into CSV File.
                        try {
                            String local_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                            String filename = local_path + "/DCIM/PicInform" + ".csv";
                            File f = new File(filename);
                            CSVWriter writer = null;
                            if (f.exists() && !f.isDirectory()) {
                                writer = new CSVWriter(new FileWriter(filename, true));
                            } else {
                                writer = new CSVWriter(new FileWriter(filename));
                                String[] fileContent = {"TimeStamp", "Latitude", "Longitude", "PicNo","PicDescript"};
                                writer.writeNext(fileContent);
                            }

                            String[] entries = {TimeStamp, Latitude, Longitude, String.valueOf(picturenumber),"please descript"};
                            writer.writeNext(entries);
                            writer.close();
                            Toast.makeText(getApplicationContext(), "PicInform " + " has been recorded", Toast.LENGTH_SHORT).show();

                        } catch (IOException e) {
                            Log.e("Error", "No certain file is detected!");
                        }
                        mCamera.startPreview();
                        picureData = null;
                        picturefile = null;
                        finishMark = 1;
                    }
                }else if (arg == button_clear){
                    //return location information and picture data to Map
                    if (finishMark == 1){
                        Intent intent = new Intent(Take_A_Picture.this,MapsActivity.class) ;
                        //intent.putExtra("column0", TimeStamp);
                        intent.putExtra("datareturn1", latitude);
                        intent.putExtra("datareturn2", longitude);
                        intent.putExtra("picturenumber", picturenumber);
                        setResult(RESULT_OK, intent);
                        finish();
                    }///elseif finishmark ！= 1
                }
            }
        };
        button_takepic.setOnClickListener(listener);
        button_retake.setOnClickListener(listener);
        button_save.setOnClickListener(listener);
        button_clear.setOnClickListener(listener);
    }

}

