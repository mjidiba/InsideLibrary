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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.indooratlas.android.sdk._internal.ar.c.i;

public class MapActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Logging";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 2;
    public static final float Y1=40.67f ,X1 =49f;
    public static final float Y2= 26.4f,X2 = 33.6f;
    public static final float Y3= 22.2f,X3 = 65.5f;
    public static final float Y4= 46.72f,X4 = 83.5f;
    public static final float []  P1={29.6f,61.74f};
    public static final float []  P2={21.35f,21.35f};

    private static final float dotRadius = 1.0f;
    private IALocationManager mIALocationManager;
    private IAResourceManager mFloorPlanManager;
    private IATask<IAFloorPlan> mPendingAsyncResult;
    public static IAFloorPlan mFloorPlan;
    private BlueDotClass mImageView;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private List<Destination> listz = new ArrayList<Destination>();
    private float [] X= {0f,0f};
    private float [] Y= {0f,0f};
    private CoordinatorLayout coordinatorLayout;
    private TextView bookText;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    private IALocationListener mLocationListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude());
            if (mImageView != null && mImageView.isReady()) {
                final IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());

                PointF point = mFloorPlan.coordinateToPoint(latLng);
                setPos(point);
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
                Toast.makeText(MapActivity.this, id, Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_map);
        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        if (!wifiAndLocation())
            new SweetAlertDialog(this)
                .setTitleText("Veuillez vérifier votre connexion Wi-fi et votre GPS")
                .show();
        else{
            mImageView = (BlueDotClass) findViewById(R.id.imageView);


            mIALocationManager = IALocationManager.create(this);
            mFloorPlanManager = IAResourceManager.create(this);
            readBookDestination();
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .map_layout);
        bookText = (TextView) findViewById(R.id.textView);
        bookText.setText(getIntent().getStringExtra("book"));
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
        Log.e("compass", String.valueOf(currentDegree));
        if(mImageView!=null && mFloorPlan!=null)
            mImageView.setBearing(currentDegree-mFloorPlan.getBearing());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private void cancelPendingNetworkCalls() {
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled()) {
            mPendingAsyncResult.cancel();
        }
    }

    /**
     * Methods for fetching floor plan data and bitmap image.
     * Method {@link #fetchFloorPlan(String id)} fetches floor plan data including URL to bitmap
     */

     /*  Broadcast receiver for floor plan image download */
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) {
                Log.w(TAG, "Ignore unrelated download");
                return;
            }
            Log.w(TAG, "Image download completed");
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = mDownloadManager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // process download
                    String filePath = c.getString(c.getColumnIndex(
                            DownloadManager.COLUMN_LOCAL_FILENAME));
                    showFloorPlanImage(filePath);
                }
            }
            c.close();
        }
    };

    private void showFloorPlanImage(String filePath) {
        mImageView.setRadius(mFloorPlan.getMetersToPixels() * dotRadius);
        if (mFloorPlan.getFloorLevel()==1){
            //mImageView.setOrientation(180);
            mImageView.setImage(ImageSource.resource(R.drawable.floor1).tilingDisabled());
            }
        else if(mFloorPlan.getFloorLevel()==2){
            mImageView.setImage(ImageSource.resource(R.drawable.floor2).tilingDisabled());
            }
        else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Veuillez Entrer à la Bibliothèque", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
            mImageView.setImage(ImageSource.resource(R.drawable.floor1).tilingDisabled());
        }


        //mImageView.setImage(ImageSource.resource(R.drawable.floor1).tilingDisabled());
        setDest();
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
        //mImageView.setImage(ImageSource.uri(filePath));

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
                        String fileName = mFloorPlan.getId() + ".img";
                        String filePath = Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
                        File file = new File(filePath);
                        if (!file.exists()) {
                            DownloadManager.Request request =
                                    new DownloadManager.Request(Uri.parse(mFloorPlan.getUrl()));
                            request.setDescription("IndoorAtlas floor plan");
                            request.setTitle("Floor plan");
                            // requires android 3.2 or later to compile
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.
                                        Request.VISIBILITY_HIDDEN);
                            }
                            request.setDestinationInExternalPublicDir(Environment.
                                    DIRECTORY_DOWNLOADS, fileName);

                            mDownloadId = mDownloadManager.enqueue(request);
                        } else {
                            showFloorPlanImage(filePath);
                        }
                    } else {
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // we don't have access to coarse locations, hence we have not access to wifi either
            // check if this requires explanation to user
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_request_title)
                        .setMessage(R.string.location_permission_request_rationale)
                        .setPositiveButton("Accepter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "request permissions");
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MapActivity.this,
                                        "Accès à la position interdit",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();

            } else {

                // ask user for permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);

            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_ACCESS_COARSE_LOCATION:

                if (grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Accès à la position interdit",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }

    }


    private void readBookDestination(){
        Destination destination1 = getIntent().getParcelableExtra("Destination1");
        Destination destination2 =destination1;
        int length = getIntent().getIntExtra("length",1);

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
                    Log.e("ID is", String.valueOf(listz.get(i).getId()));
                    Y[i] = Y3;
                    X[i] = X3 + 2.23f * (listz.get(i).getId() - 16);
                }
                if (listz.get(i).getId() >= 25) {
                    Y[i] = Y4;
                    X[i] = X4 - 2.23f * (listz.get(i).getId() - 25);
                    if (listz.get(i).getId() > 27) X[i] += 10;
                }
            }

            PointF destPoint1 = new PointF (X[0]*mFloorPlan.getMetersToPixels(),Y[0]*mFloorPlan.getMetersToPixels());
            PointF destPoint2 = new PointF (X[1]*mFloorPlan.getMetersToPixels(),Y[1]*mFloorPlan.getMetersToPixels());
            destPoint1.set(setPos(destPoint1));
            destPoint2.set(setPos(destPoint2));

            if((listz.get(0).getId()<=15 && mFloorPlan.getFloorLevel()==1) || (listz.get(0).getId()>=16 && mFloorPlan.getFloorLevel()==2))
            {


                String toast = new String("Le livre se trouve dans l");
                if (listz.get(0).getShelfs().length > 1)
                {
                    toast += "es étagères: " + listz.get(0).getShelfs()[0];
                    for (int i = 1; i < listz.get(0).getShelfs().length; i++) {
                        toast += ", " + listz.get(0).getShelfs()[i];
                    }
                    toast+=" au point rouge";
                }
                if (listz.get(0).getShelfs().length == 1)
                {toast += "'étagère: " + listz.get(0).getShelfs()[0];
                    toast+=" au point rouge";}

                else
                {toast = "Livre non Trouvé.";}
                if(listz.get(0).getId()!=listz.get(1).getId())
                {   mImageView.setDestCenter(destPoint2,destPoint1);
                    toast+=" ou bien dans l";
                    if (listz.get(1).getShelfs().length > 1)
                    {
                        toast += "es étagères: " + listz.get(1).getShelfs()[0];
                        for (int i = 1; i < listz.get(1).getShelfs().length; i++) {
                            toast += ", " + listz.get(1).getShelfs()[i];
                        }
                    }
                    if (listz.get(0).getShelfs().length == 1)
                        toast += "'étagère: " + listz.get(1).getShelfs()[0];
                    toast+=" au point noir.";
                    Log.e("id0", String.valueOf(listz.get(0).getId()));
                    Log.e("id1", String.valueOf(listz.get(1).getId()));
                }
                else    mImageView.setDestCenter(destPoint1,destPoint2);


                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, toast, Snackbar.LENGTH_INDEFINITE);

                snackbar.show();

            }

            else
            {
                if(mFloorPlan.getFloorLevel()==1)
                {   Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Le Livre se trouve à la Mezzanine", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();}
                if(mFloorPlan.getFloorLevel()==2)
                {   Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Le Livre se trouve à la Salle de Lecture", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();}
            }
    }

    public static PointF setPos(PointF p){

        if(mFloorPlan.getFloorLevel()==2)
        {
            p.x -= P1[1] * mFloorPlan.getMetersToPixels();
            p.y -= P2[1] * mFloorPlan.getMetersToPixels();
        }
        else
            {
                p.x -= P1[0] * mFloorPlan.getMetersToPixels();
                p.y -= P2[0] * mFloorPlan.getMetersToPixels();
            }


        return p;

    }






}
