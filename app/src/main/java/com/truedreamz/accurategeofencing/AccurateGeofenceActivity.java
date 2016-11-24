package com.truedreamz.accurategeofencing;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AccurateGeofenceActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    protected static final String TAG = "AccurateGeofence";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The list of geofences used in this sample.
     */
    public static ArrayList<Geofence> mGeofenceList= new ArrayList<Geofence>();

    /**
     * Used to keep track of whether geofences were added.
     */
    public static boolean mGeofencesAdded;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Used to persist application state about whether geofences were added.
     */
    private SharedPreferences mSharedPreferences;

   /* // Buttons for kicking off the process of adding or removing geofences.
    //private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;
    //private Button btnSendResult;
    private Button btnAddLandmark;

    private ArrayList<NotificationItem> notificationList = new ArrayList<NotificationItem>();*/

    public HashMap<String, LatLng> AUGGY_LANDMARKS = new HashMap<String, LatLng>();

    public HashMap<String, Integer> FENCE_RADIUS = new HashMap<String, Integer>();

    //private TextView batteryTxt,txtDeviceInfo;
    //private TextView txtStartBatteryResult,txtEndBatteryResult;
    private EditText etxtPlace,etxtLatitude,etxtLongitude,etxtRadius;
    private EditText etxtAddress,etxtDesc;
    SharedPreferences pref;
    public static String strBatteryPercentage=null;
    private ImageButton imgGetLatLong;
    public static List<String> listUsedDefinedPlaceName=new ArrayList<String>();
    public static final String PREFS_NAME = "MyPref";
    public static final String NOTIFICATION = "Notification";

    private static final int GET_LOCATION_CODE=1;
    //String strLocalNotificationServiceURL="http://augray.com:8080/AugRayV2/rest/services/AugRayDynamicMessage/";

    //String strLocalNotificationServiceURL="http://ams.augray.com/service/regdevice";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceID = tManager.getDeviceId();

        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        imgGetLatLong= (ImageButton) this.findViewById(R.id.imgGetLatLong);
        /*batteryTxt = (TextView) this.findViewById(R.id.txtBatteryPercen);
        txtDeviceInfo = (TextView) this.findViewById(R.id.txtDeviceInfo);
        txtDeviceInfo.setText(getDeviceInfo());

        txtStartBatteryResult = (TextView) this.findViewById(R.id.txtStartBatteryResult);
        txtEndBatteryResult = (TextView) this.findViewById(R.id.txtEndBatteryResult);*/

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


       // Get the UI widgets.
        /*mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);
        //btnSendResult= (Button) findViewById(R.id.btnSendResult);
        btnAddLandmark= (Button) findViewById(R.id.btnAddLandmark);*/

        etxtPlace= (EditText) findViewById(R.id.etxtPlace);
        etxtLatitude= (EditText) findViewById(R.id.etxtLatitude);
        etxtLongitude= (EditText) findViewById(R.id.etxtLongitude);
        etxtRadius= (EditText) findViewById(R.id.etxtRadius);
        etxtAddress= (EditText) findViewById(R.id.etxtAddr);
        etxtDesc= (EditText) findViewById(R.id.etxtDesc);

        // Empty list for storing geofences.
        //mGeofenceList = new ArrayList<Geofence>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
        //setButtonsEnabledState();

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

        //new getLocalNotificationListTask(Constants.NOTIFICATION_URL).execute();
    }


    public void onGetLatLongListener(View v){
        Intent intent=new Intent(this,MapActivity.class);
        startActivityForResult(intent, GET_LOCATION_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_LOCATION_CODE)
        {
            if(data!=null){
                String lat=data.getStringExtra("LATITUDE");
                String longi=data.getStringExtra("LONGITUDE");
                etxtLatitude.setText(lat);
                etxtLongitude.setText(longi);
            }
        }
    }

    /*private String getDeviceInfo(){

        String os=Build.VERSION.RELEASE; // OS version
        //String api=Build.VERSION.SDK; // API Level
        int apiLevel= Build.VERSION.SDK_INT; // API Level
        String device=Build.DEVICE; // Device
        String model=Build.MODEL;            // Model
        //String product=Build.PRODUCT;          // Product

        StringBuffer strDeviceInfo=new StringBuffer("Device information");
        strDeviceInfo.append("\n"+"Device :"+device);
        strDeviceInfo.append("\n"+"Model :"+model);
        strDeviceInfo.append("\n" + "OS version:" + os);
        strDeviceInfo.append("\n" + "API :" + apiLevel);

        return strDeviceInfo.toString();
    }*/

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, You should enable GPS to work Geo-fence.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*public static JSONObject getLocationInfo(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        try {

            address = address.replaceAll(" ","%20");

            HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }*/

    /*public class getLatLongTask extends AsyncTask<Void,Void,String>{

        ProgressDialog dialog=null;
        String address=null;

        getLatLongTask(String address){
            this.address=address;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog=new ProgressDialog(AccurateGeofenceActivity.this);
            dialog.setMessage("Loading...");
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> input=new HashMap<String,String>();
            return  getLatLongFromPlaceURL(address, input);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            LatLng loc=parseLatLong(result);
            if(loc!=null){
                etxtLongitude.setText(String.valueOf(loc.longitude));
                etxtLatitude.setText(String.valueOf(loc.latitude));
                btnAddLandmark.setEnabled(true);
            }else{
                btnAddLandmark.setEnabled(false);
                Toast.makeText(AccurateGeofenceActivity.this,"Kindly give correct address.",Toast.LENGTH_LONG).show();
            }

            if(dialog!=null){
                if(dialog.isShowing()) dialog.dismiss();
            }
        }
    }*/

    /*private LatLng parseLatLong(String result){
        try {
            JSONObject jsonObj = new JSONObject(result.toString());
            JSONArray resultJsonArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            // resultList = new ArrayList<String>(resultJsonArray.length());

            JSONObject before_geometry_jsonObj = resultJsonArray
                    .getJSONObject(0);

            JSONObject geometry_jsonObj = before_geometry_jsonObj
                    .getJSONObject("geometry");

            JSONObject location_jsonObj = geometry_jsonObj
                    .getJSONObject("location");

            String lat_helper = location_jsonObj.getString("lat");
            double lat = Double.valueOf(lat_helper);


            String lng_helper = location_jsonObj.getString("lng");
            double lng = Double.valueOf(lng_helper);


            LatLng point = new LatLng(lat, lng);

            return point;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return null;
    }

    public String  getLatLongFromPlaceURL(String address,
                                   HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        address = address.replaceAll(" ","%20");

        String requestURL="http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false";

        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }*/

    /*private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mBatInfoReceiver);
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            strBatteryPercentage=String.valueOf(level) + " %";
        }
    };

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    /*public void addGeofencesButtonHandler(View view) {
        addGeofences();
    }*/

    private void addGeofences(){
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // To set flag to disable start geo fence button
            mGeofencesAdded=true;

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().

        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    public void viewLandmarkListener(View view) {

        StringBuffer landmarks=new StringBuffer("");
        int count=1;
        int size=listUsedDefinedPlaceName.size();
        /*for (Map.Entry<String, LatLng> entry : AUGGY_LANDMARKS.entrySet()) {
            landmarks.append(count+". "+entry.getKey()+"\n");
            count++;
        }*/

        if(size>0){
            for (int i=0;i<size;i++){
                landmarks.append(count+". "+listUsedDefinedPlaceName.get(i)+"\n");
                count++;
            }


            new AlertDialog.Builder(AccurateGeofenceActivity.this)
                    .setTitle("List of Landmarks")
                    .setMessage(landmarks)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }else{
            Toast.makeText(AccurateGeofenceActivity.this,"Landmark is empty.",Toast.LENGTH_SHORT).show();
        }
    }

        public void addLandmarkListener(View view) {

        if(etxtPlace.getText().length()!=0 && etxtLatitude.getText().length()!=0
                && etxtLongitude.getText().length()!=0 && etxtRadius.getText().length()!=0){

            if(AUGGY_LANDMARKS.size()<=40){

            String strPlaceName=etxtPlace.getText().toString();

            if(!listUsedDefinedPlaceName.contains(strPlaceName)){

            double latitude=Double.valueOf(etxtLatitude.getText().toString());
            double longitude=Double.valueOf(etxtLongitude.getText().toString());
            int radius=Integer.valueOf(etxtRadius.getText().toString());

            if(radius>200){
                listUsedDefinedPlaceName.add(strPlaceName);

                //mGeofencesAdded = false;
                addUserDefinedGeoFence(strPlaceName,latitude,longitude,radius);

                etxtPlace.setText("");
                etxtDesc.setText("");
                etxtAddress.setText("");
                etxtLatitude.setText("");
                etxtLongitude.setText("");
                etxtRadius.setText("");
                Toast.makeText(AccurateGeofenceActivity.this,"Landmark: "+strPlaceName+ " is added.",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(AccurateGeofenceActivity.this,strPlaceName+ " is already exists.",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(AccurateGeofenceActivity.this,"Minimum radius : 200 m",Toast.LENGTH_SHORT).show();
            }

            }else{
                Toast.makeText(AccurateGeofenceActivity.this,"You reached maximum limit : 40",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(AccurateGeofenceActivity.this,"All Fields are mandatory.",Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserDefinedGeoFence(String strPlaceName,double latitude,double longitude,int radius){

        try{
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            // Add the geofences to be monitored by geofencing service.
            builder.addGeofence(new Geofence.Builder()
                    .setRequestId(strPlaceName)
                    .setCircularRegion(
                            latitude,
                            longitude,
                            //Constants.GEOFENCE_RADIUS_IN_METERS
                            Float.valueOf(radius)
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    builder.build(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().


        }catch (Exception ex){
            Log.e(TAG, "addUserDefinedGeoFence Exception:" + ex.getMessage());
        }
    }

    private void saveStartTestingData(boolean isStarted){
        SharedPreferences.Editor editor = pref.edit();
        if(isStarted){
            editor.putString("StartTestTime", getCurrentDateTime());
            editor.putString("StartTestBattery", strBatteryPercentage);
        }
        else {
            editor.putString("EndTestTime", getCurrentDateTime());
            editor.putString("EndTestBattery", strBatteryPercentage);
        }
        // Save the changes in SharedPreferences
        editor.commit(); // commit changes
    }

    private String getCurrentDateTime(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    private void sendResultInEmail(String subject,String body){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        //need this to prompts email client only
        //emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler(View view) {
            if (!mGoogleApiClient.isConnected()) {
                Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(AccurateGeofenceActivity.this)
                    .setTitle("Remove Geofence")
                    .setMessage("User defined geo fence & Notification log also will be removed. Do you want to contine ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            try {
                                // Remove geofences.
                                LocationServices.GeofencingApi.removeGeofences(
                                        mGoogleApiClient,
                                        // This is the same pending intent that was used in addGeofences().
                                        getGeofencePendingIntent()
                                ).setResultCallback(AccurateGeofenceActivity.this); // Result processed in onResult().

                                // End : Remove test data
                                //saveStartTestingData(false);
                                //btnSendResult.setEnabled(true);
                                listUsedDefinedPlaceName.clear();
                                // Clear all notification
                                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nMgr.cancelAll();

                                mGeofencesAdded=false;
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
                                editor.apply();

                                //setButtonsEnabledState();
                                /*if(GeofenceTransitionsIntentService.list_Notify.size()>0){
                                    // TO load testing data in mail
                                    loadTestingLogToMail();
                                }else{
                                    Toast.makeText(getApplicationContext(), "No data to generate report.", Toast.LENGTH_SHORT).show();
                                }*/

                            } catch (SecurityException securityException) {
                                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                                logSecurityException(securityException);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
    }

    private void removeGeofence(){
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(AccurateGeofenceActivity.this); // Result processed in onResult().

            // Clear all notification
//            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            nMgr.cancelAll();

            mGeofencesAdded=false;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }



    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            //mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    /*private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }*/

    private PendingIntent getGeofencePendingIntent() {
        String GEOFENCE_ACTION="com.wisdom.AugRay.geofence.ACTION_RECEIVE_GEOFENCE";
        if (null != mGeofencePendingIntent) {
            // Return the existing intent
            return mGeofencePendingIntent;
            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
            Intent intent = new Intent(GEOFENCE_ACTION);
            return PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }


    /*public void parsingEventList(String result){
        try {
            JSONObject eventData=new JSONObject(result);
            JSONArray jsonArray = eventData.getJSONArray("geofences");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject json = (JSONObject) jsonArray.get(i);
                if(json.has("id") && json.has("identifier") && json.has("description") && json.has("lat")
                        && json.has("lng") && json.has("radius")){

                notificationList.add(new NotificationItem((String) json.get("id"),
                        (String) json.get("identifier"),
                        (String) json.get("description"),
                        Double.valueOf((String) json.get("lat")),
                        Double.valueOf((String) json.get("lng")),
                        (String) json.get("radius"),
                        (JSONObject) json.get("message"),
                        (String) json.get("expired_on")
                ));
            }
        }
        Log.d(TAG, "Notification list size:"+notificationList.size());

        if(mGeofenceList.size()>0){
            removeGeofence();
        }

        for (int i=0;i<notificationList.size();i++){
            String identifier=notificationList.get(i).notifiIdentifier;

            Geofence geofence=new Geofence.Builder()
                    .setRequestId(identifier)
                    .setCircularRegion(
                            notificationList.get(i).notifiGeoLat,
                            notificationList.get(i).notifiGeoLong,
                            //Constants.GEOFENCE_RADIUS_IN_METERS
                            Float.valueOf(notificationList.get(i).notifiRadius)
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);

            saveNotificationMessageToPreference(identifier,notificationList.get(i).notifiMessage.toString());
        }

            // Adding event data to Geofence
            addGeofences();

        // Commented for testing 6 Jun 16
        *//*for (int i=0;i<notificationList.size();i++){
            AUGGY_LANDMARKS.put(notificationList.get(i).notifiIdentifier, new LatLng(notificationList.get(i).notifiGeoLat,
                    notificationList.get(i).notifiGeoLong));
            FENCE_RADIUS.put(notificationList.get(i).notifiIdentifier,notificationList.get(i).notifiRadius);
        }

        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();*//*

        }catch (JSONException e){
            e.printStackTrace();
        }
    }*/

    private void saveNotificationMessageToPreference(String identifier,String title){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("GeofencePref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(identifier, title);
        // Save the changes in SharedPreferences
        editor.commit(); // commit change
    }
}