package com.example.haotian.tutorial32;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity {
    public static final String TAG = "MapsActivity";
    public static final int THUMBNAIL = 1;
    private double TimeStamp = 0;
    private double Latitude = 40;//41.50459;
    private double Longitude = 200;//-81.609713;
    private final String imgDir = Environment.getExternalStorageDirectory().toString() + "/DCIM/GoogleMapPhoto/";
    private Bitmap img;
    private int picturenumber;
    private int previouspicturenumber = 0;
    private Marker myMarker;
    private static final int PictureDescription = 1;
    private String description;

    private PhoneApi phoneApi;
    private PhoneService phoneService;

    static final int REQUEST_IMAGE_CAPUTURE=1;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Button picButton; //takes user to camera


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        //phoneApi = PhoneApi.getApi();
        //phoneService = phoneApi.getService();


        picButton = (Button) findViewById(R.id.photobutton);

        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, Take_A_Picture.class);
                startActivityForResult(intent, 1);
            }
        });
        addMarkerlistener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(41.50459, -81.609713)).title("Happycoding"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).
                snippet("Latitude:"+Latitude+",Longitude:"+Longitude).title(description).icon(BitmapDescriptorFactory.fromBitmap(img)));
    }


    private void shortmessage(Double latitude,Double longitude) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    //TimeStamp = data.getDoubleExtra("column0",0);
                    Latitude = data.getDoubleExtra("datareturn1", 0);
                    Longitude = data.getDoubleExtra("datareturn2", 0);
                    picturenumber = data.getIntExtra("picturenumber", 0);
                    if (previouspicturenumber != picturenumber) {
                        String picturefile = imgDir + String.valueOf(picturenumber) + ".jpg";
                        img = BitmapFactory.decodeFile(picturefile);
                        img = Bitmap.createScaledBitmap(img, 120, 120, false);
                        previouspicturenumber = picturenumber;
                        Log.d(TAG, picturefile);
                        setUpMap();
                    }
                    else {return;}
                }
        }

    }

    // marker click listener
    public void addMarkerlistener(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                showDialog(PictureDescription);
                if (description != null) marker.setTitle(description);
                return false;
            }
        });
    }

    //create Dialog to save edit text for description
    private Dialog PictureDescription (Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(
                R.layout.picutre_description, (ViewGroup)findViewById(R.id.savemodel));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText edit = (EditText)textEntryView.findViewById(R.id.savePath_edit);
        builder.setTitle("Picture Description");
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            description = edit.getText().toString();
                            String local_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                            String filename = local_path + "/DCIM/PicInform" + ".csv";
                            File f = new File(filename);
                            updateCSV(filename,description,picturenumber,4);

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.setView(textEntryView);
        return builder.create();
    }

    /**
     *
     * Citation cite: https://stackoverflow.com/questions/4397907/updating-specific-cell-csv-file-using-java
     * Update CSV by row and column
     *
     * @param fileToUpdate CSV file path to update e.g. D:\\chetan\\test.csv
     * @param replace Replacement for your cell value
     * @param row Row for which need to update
     * @param col Column for which you need to update
     * @throws IOException
     */
    public static void updateCSV(String fileToUpdate, String replace, int row, int col) throws IOException {
        File inputFile = new File(fileToUpdate);
        // Read existing file
        CSVReader reader = new CSVReader(new FileReader(inputFile));
        List<String[]> csvBody = reader.readAll();
        // get CSV row column  and replace with by using row and column
        csvBody.get(row)[col] = replace;
        reader.close();
        // Write to CSV file which is open
        CSVWriter writer = new CSVWriter(new FileWriter(inputFile));
        writer.writeAll(csvBody);
        writer.flush();
        writer.close();
    }

    protected Dialog onCreateDialog(int id) {
        if (id == PictureDescription)
            return PictureDescription(MapsActivity.this);

        return null;
    }



}
