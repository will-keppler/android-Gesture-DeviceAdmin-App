package com.androiddev.will.my_gesture_application;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothClass;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GesturePoint;
import android.gesture.GestureStore;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends Activity implements GestureOverlayView.OnGesturePerformedListener{
    private final static String DEBUG_TAG = "MainActivity.java; ";
    private static final int REQUEST_CODE_ENABLE_ADMIN = 10;

    private GestureStore gestureLib = new GestureStore();
    private InputStream inStream;
    static final int ACTIVATION_REQUEST = 10;
    DevicePolicyManager mPolicyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        mPolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdminSample = new ComponentName(this, DeviceAdminSample.class);

        //Gesture Overlay View
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_main, null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.setDrawingCacheEnabled(true);
        //When a gesture is performed it will call OnGesturePerformed()
        gestureOverlayView.addOnGesturePerformedListener(this);

        //Get InputStream of gesture library
        //The inputStream is passed to the GestureStore object
        //GestureLibrary did not work for me [as per the tutorial] but
        //Reading the android.gesture Gesture Store says: quote documentation
        try{
            Resources res = getResources();
            inStream = res.openRawResource(R.raw.gestures);

        }catch (Exception e){
            Log.e(DEBUG_TAG, "Get inputStream of gesture library FAILED ===>");
            e.printStackTrace();
        }

        //This should load the gestures file from /res/raw/gestures
        //into the gestureStore object (was GestureLibrary)
        try {
            gestureLib.load(inStream);

        }catch (Exception e){
            Log.e(DEBUG_TAG, "Load gesture file from /res/raw FAILED ===>");
            e.printStackTrace();
        }

        setContentView(gestureOverlayView);

        try{
            //This intent will pop up a window that will allow the user to allow or deny this app's
            //request for admin privileges when it is first ran
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);

            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            Log.d(DEBUG_TAG, "After the startActivityForResult()");
        }catch(Exception e) {
            e.printStackTrace();
        }

        //mPolicyManager.lockNow();
        //above code will lock the device as soon as the application starts
        //For test purposes
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        Log.d(DEBUG_TAG, "onGesturePerformed()");

        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        Log.d(DEBUG_TAG, "predictions.size = " + predictions.size());
        for (Prediction prediction : predictions){
            Log.d(DEBUG_TAG,"inside for loop; prediction.name = " + prediction.name);
            Log.d(DEBUG_TAG, "inside for loop; prediction.score = " + prediction.score);
        }
        if(predictions.get(0).score > 2 ) {
            //.get(0) gets the first prediction from 'predictions'
            //i want the first because the predictions are sorted based on their score
            // each time a gesture is performed..
            //If the highest score prediction [i.e. the most likely gesture match] > 2
            //and the name is == "MyFunction"//Fix: have to use .equals("MyFunction") (== "MyFunction") did not match
            //lock the device
            Log.d(DEBUG_TAG, "predictions.get(0) = " + predictions.get(0));
            if(predictions.get(0).name.equals("MyFunction")){
                Log.d(DEBUG_TAG, "Locking the Device...");
                mPolicyManager.lockNow();
            }//====================================================>>>>>>>>>>>>>>>>>
        }else {
            //if the gesture did not have a score > 2 then no match
            Log.d(DEBUG_TAG, "predictions.get(0).score < 2; NO MATCH ===>");
        }

    }

    //This is triggered after the request to the user for the administrative permissions returns
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(DEBUG_TAG, "In onActivityResult; resultCode = " + resultCode);
        switch (requestCode) {
            case ACTIVATION_REQUEST://If the request succeeded it will have the same requestCode as
                if (resultCode == Activity.RESULT_OK) {//startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
                    Log.i(DEBUG_TAG, "Administration enabled!");
                    //toggleButton.setChecked(true); This was from tutorial code
                } else {
                    Log.i(DEBUG_TAG, "Administration enable FAILED!");
                    //toggleButton.setChecked(false); Tutorial code
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
