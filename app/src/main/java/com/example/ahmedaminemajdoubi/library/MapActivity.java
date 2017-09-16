package com.example.ahmedaminemajdoubi.library;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.indooratlas.android.sdk._internal.ar.c.i;

public class MapActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Logging";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    public static final float Y1=40.67f ,X1 =49f;
    public static final float Y2= 26.4f,X2 = 33.6f;
    public static final float Y3= 22.2f,X3 = 65.5f;
    public static final float Y4= 46.72f,X4 = 83.5f;
    public static final float []  P1={29.6f,61.74f};
    public static final float []  P2={21.35f,21.35f};

    private HorizontalScrollView horizontalScrollView;
    private static final float dotRadius = 1.0f;
    private IALocationManager mIALocationManager;
    private IAResourceManager mFloorPlanManager;
    private IATask<IAFloorPlan> mPendingAsyncResult;
    public static IAFloorPlan mFloorPlan;
    private BlueDotClass mImageView;
    private ShelfClass sImageView1;
    private ShelfClass sImageView2;
    public static List<Destination> listz = new ArrayList<Destination>();
    private float [] X= {0f,0f};
    private float [] Y= {0f,0f};
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private ImageView bigImage;
    private int [] rayon1={0,0,0,0,0,0,0};
    private int [] rayon2={0,0,0,0,0,0,0};


    private IALocationListener mLocationListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude());
            if (mImageView != null && mImageView.isReady()) {
                final IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());

                PointF point = mFloorPlan.coordinateToPoint(latLng);
                setPos(point);

                if(listz.get(0).getId()>15)
                {
                    point = new PointF(510,560);
                    mImageView.setAccuracy(0);
                }
                else
                mImageView.setAccuracy(location.getAccuracy()*mFloorPlan.getMetersToPixels());

                mImageView.setDotCenter(point);
                mImageView.postInvalidate();
            }
        }
    };

    private IARegion.Listener mRegionListener = new IARegion.Listener() {

        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                String id = region.getId();
                Log.d(TAG, "floorPlan changed to " + id);
                fetchFloorPlan(id);
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            // leaving a previously entered region
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        if (!wifiAndLocation())
            new SweetAlertDialog(this)
                .setTitleText("Veuillez vérifier votre connexion Wi-fi et votre GPS")
                .show();
        else{
            mImageView = (BlueDotClass) findViewById(R.id.imageView);
            bigImage = (ImageView) findViewById(R.id.book_cover);
            horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) horizontalScrollView.getLayoutParams();
            DisplayMetrics metrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            layoutParams.bottomMargin = metrics.heightPixels*6/15;
            bigImage.getLayoutParams().width = 180*metrics.heightPixels/640*18/15;
            Picasso.with(this).load(BookDetails.bookCoverLink(getIntent().getStringExtra("isbn"),true)).into(bigImage);
            mIALocationManager = IALocationManager.create(this);
            mFloorPlanManager = IAResourceManager.create(this);
            readBookDestination();
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            setRayon();
            sImageView1 = (ShelfClass) findViewById(R.id.rayon1);
            sImageView2 = (ShelfClass) findViewById(R.id.rayon2);
            sImageView1.setZoomEnabled(false);
            sImageView2.setZoomEnabled(false);
            if (listz.get(0).getId()<=15){
                sImageView1.setImage(ImageSource.resource(R.drawable.shelf6));
                sImageView1.setBigId(6);}
            else
            {
                sImageView1.setImage(ImageSource.resource(R.drawable.shelf5));
                sImageView1.setBigId(5);
            }
            sImageView1.setShelves(rayon1);

            if(!listz.get(0).equals(listz.get(1)))
            {
                if (listz.get(0).getId()<=15){
                    sImageView2.setImage(ImageSource.resource(R.drawable.shelf6));
                    sImageView2.setBigId(6);}
                else
                {
                    sImageView2.setImage(ImageSource.resource(R.drawable.shelf5));
                    sImageView2.setBigId(5);
                }
                sImageView2.setShelves(rayon2);

            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensurePermissions();
        // starts receiving location updates
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mLocationListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIALocationManager.removeLocationUpdates(mLocationListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        mSensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        currentDegree = Math.round(event.values[0]);
        //Log.e("compass", String.valueOf(currentDegree));
        if(mImageView!=null && mFloorPlan!=null){
            if(currentDegree<180)
                currentDegree+=180;
            else
                currentDegree-=180;

            mImageView.setBearing(currentDegree);
        Log.e("Degs", String.valueOf(currentDegree));}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private void cancelPendingNetworkCalls() {
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled()) {
            mPendingAsyncResult.cancel();
        }
    }

    private void showFloorPlanImage() {

        if (listz.get(0).getId()>15) {
            mImageView.setImage(ImageSource.resource(R.drawable.floor2).tilingDisabled());
            Toast.makeText(MapActivity.this,
                    "La localisation n'est pas disponible à la Mezzanine",
                    Toast.LENGTH_LONG).show();
        }
        if(mFloorPlan!=null) {
            Log.e("List", String.valueOf(listz.get(0).getId()));
            mImageView.setRadius(mFloorPlan.getMetersToPixels() * dotRadius);
            if (listz.get(0).getId()<=15 && mFloorPlan.getFloorLevel()==1) {
                mImageView.setImage(ImageSource.resource(R.drawable.floor1).tilingDisabled());
            }

            else if (mFloorPlan.getFloorLevel()==0) {
                Toast.makeText(MapActivity.this,
                        "Veuillez Entrer à la Bibliothèque",
                        Toast.LENGTH_LONG).show();

            }

            setDest();

        }

        if(listz.get(0).getId()<=6 || listz.get(0).getId()>=16)
        {
            mImageView.setId1(1);
        }
        else
            mImageView.setId1(2);

        if(listz.get(1).getId()<=6 || listz.get(1).getId()>=16)
        {
            mImageView.setId2(1);
        }
        else{
            mImageView.setId2(2);}

    }

    /**
     * Fetches floor plan data from IndoorAtlas server. Some room for cleaning up!!
     */
    private void fetchFloorPlan(String id) {
        cancelPendingNetworkCalls();
        final IATask<IAFloorPlan> asyncResult = mFloorPlanManager.fetchFloorPlanWithId(id);
        mPendingAsyncResult = asyncResult;
        if (mPendingAsyncResult != null) {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>() {
                @Override
                public void onResult(IAResult<IAFloorPlan> result) {
                    Log.d(TAG, "fetch floor plan result:" + result);
                    if (result.isSuccess() && result.getResult() != null) {
                        mFloorPlan = result.getResult();
                        showFloorPlanImage();
                    }
                    else {
                        // do something with error
                        if (!asyncResult.isCancelled()) {
                            Toast.makeText(MapActivity.this,
                                    (result.getError() != null
                                            ? "error loading floor plan: " + result.getError()
                                            : "access to floor plan denied"), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }, Looper.getMainLooper()); // deliver callbacks in main thread
        }
    }


    private void ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Accès à la mémoire interdit",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

    }

    private void readBookDestination(){
        Destination destination1 = getIntent().getParcelableExtra("Destination1");
        Destination destination2 =destination1;
        int length = getIntent().getIntExtra("length",1);
        listz.clear();
        if(length>1){
            destination2 = getIntent().getParcelableExtra("Destination2");
        }
        listz.add(destination1);
        listz.add(destination2);

    }

    public boolean wifiAndLocation() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        return !(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable() || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private void setDest()
    {
                    for (int i = 0; i < 2; i++) {



                        if (listz.get(i).getId() <= 6) {

                    Y[i] = Y1;
                    X[i] = X1 - 2.23f * (listz.get(i).getId() - 1);
                    if (listz.get(i).getId() > 3) X[i] -= 3.6;
                }
                if (listz.get(i).getId() <= 15 && listz.get(i).getId() >= 7) {
                    Y[i] = Y2;
                    X[i] = X2 + 2f * (listz.get(i).getId() - 7);
                }
                if (listz.get(i).getId() <= 24 && listz.get(i).getId() >= 16) {
                    Y[i] = Y3;
                    X[i] = X3 + 2.23f * (listz.get(i).getId() - 16);
                }
                if (listz.get(i).getId() >= 25) {
                    Y[i] = Y4;
                    X[i] = X4 - 2.23f * (listz.get(i).getId() - 25);
                    if (listz.get(i).getId() > 27) X[i] += 10;
                }
            }

        float pix = 39.902344f;


        PointF destPoint1 = new PointF (X[0]*pix,Y[0]*pix);
            PointF destPoint2 = new PointF (X[1]*pix,Y[1]*pix);
            destPoint1.set(setPos(destPoint1));
            destPoint2.set(setPos(destPoint2));

            if((listz.get(0).getId()<=15 && mFloorPlan.getFloorLevel()==1) || (listz.get(0).getId()>=16) )
            {
                if(listz.get(0).getId()!=listz.get(1).getId())
                {   mImageView.setDestCenter(destPoint2,destPoint1);

                }

                else    mImageView.setDestCenter(destPoint1,destPoint2);




            }

                if(listz.get(0).getId()>15)
                {
                    Toast.makeText(MapActivity.this, "Le Livre se trouve à la Mezzanine", Toast.LENGTH_SHORT).show();
                }
                if(listz.get(0).getId()<16)
                {
                    Toast.makeText(MapActivity.this, "Le Livre se trouve à la Salle de Lecture", Toast.LENGTH_SHORT).show();
                }
    }

    public void setRayon() {

        if (listz.get(0).getId() <= 15 || listz.get(0).getId() >= 16) {
            Log.i("entered", "test");

            for (int miniShelf : listz.get(0).getShelfs()) {
                rayon1[miniShelf - 1] = 1;
            }
            rayon1[6]=1;

        }
        if(listz.get(0).getId()!=listz.get(1).getId())
        {
            for(int miniShelf : listz.get(1).getShelfs()){
                rayon2[miniShelf-1] = 1;
            }
            rayon2[6]=2;


        }

    }

    public static PointF setPos(PointF p){

        float pix = 39.902344f;


        if(listz.get(0).getId()>15)
        {
            p.x -= P1[1] * pix;
            p.y -= P2[1] * pix;
        }
        else
            {
                p.x -= P1[0] * pix;
                p.y -= P2[0] * pix;
            }
        return p;

    }






}
