package com.truedreamz.accurategeofencing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MapActivity extends FragmentActivity implements LocationListener,AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "MapActivity";
    GoogleMap googleMap;
    double latitude=0;
    double longitude =0;
    private static final int GET_LOCATION_CODE=1;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAOLfVEAzUpcaQ0zTDuhCsrMk9ZDuyr5lw";

    boolean isMarkerAdded=false;
    MarkerOptions marker = new MarkerOptions();
    private TextView locationTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_map);

        locationTv= (TextView) findViewById(R.id.latlongLocation);

        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 5000, 0, this);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                //placeMarkersOnMap(location);
                addingMarkerOnMap(new LatLng(location.getLatitude(),location.getLongitude()),"My location!");
                break;
            }/*else{
                Toast.makeText(getApplicationContext(),"Failed to get location.",Toast.LENGTH_SHORT).show();
            }*/
        }

        findViewById(R.id.imgSaveLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("LATITUDE", String.valueOf(latitude));
                intent.putExtra("LONGITUDE", String.valueOf(longitude));
                setResult(GET_LOCATION_CODE, intent);
                finish();
            }
        });

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewPlaces);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
    }

    public class getLatLongTask extends AsyncTask<Void,Void,String> {

        ProgressDialog dialog=null;
        String address=null;

        getLatLongTask(String address){
            this.address=address;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            locationTv.setText("");
            dialog=new ProgressDialog(MapActivity.this);
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
                addingMarkerOnMap(loc,address);
            }

            /*if(loc!=null){
                etxtLongitude.setText(String.valueOf(loc.longitude));
                etxtLatitude.setText(String.valueOf(loc.latitude));
                btnAddLandmark.setEnabled(true);
            }else{
                btnAddLandmark.setEnabled(false);
                Toast.makeText(AccurateGeofenceActivity.this,"Kindly give correct address.",Toast.LENGTH_LONG).show();
            }*/

            if(dialog!=null){
                if(dialog.isShowing()) dialog.dismiss();
            }
        }
    }

    private void addingMarkerOnMap(LatLng loc,String address){

        googleMap.clear();

        MarkerOptions new_marker = new MarkerOptions();
        new_marker.position(loc).title(address);
        // Changing marker icon
        new_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        new_marker.draggable(true);
        // adding marker
        googleMap.addMarker(new_marker);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(loc)
                        .bearing(45)
                        .tilt(90)
                        .zoom(15)
                        .build();
        googleMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                3000,
                null);

        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        latitude=loc.latitude;
        longitude=loc.longitude;

        locationTv.setText("Latitude:" + latitude + ", Longitude:" + longitude);
    }

    private LatLng parseLatLong(String result){
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
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
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
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {

        hideKeyboard();
        if(isNetworkAvailable()){
            String strAddress = (String) adapterView.getItemAtPosition(position);
            //Toast.makeText(this, strAddress, Toast.LENGTH_SHORT).show();
            new getLatLongTask(strAddress).execute();
        }else{
            Toast.makeText(this, "Kindly connect to internet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            //sb.append("&components=country:gr");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("=======================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }


    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}