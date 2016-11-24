package com.truedreamz.accurategeofencing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class GeofecneBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG="GeofecneReceiver";
    public static final String GEOFENCE_ACTION="com.wisdom.AugRay.geofence.ACTION_RECEIVE_GEOFENCE";
    Context context;
    Intent broadcastIntent = new Intent();
    String payload_data=null;
    private static final boolean DEBUG = true;
    public GeofecneBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        this.context = context;
        Log.d(TAG, "onReceive");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            handleError(intent);
        } else {
            handleEnterExit(geofencingEvent);
        }
    }

    private void handleError(Intent intent){
        // Get the error code
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        String errorMessage = GeofenceErrorMessages.getErrorString(context,
                geofencingEvent.getErrorCode());

        // Log the error
        Log.e(TAG,"Geofence handleError:"+errorMessage);

        // Set the action and error message for the broadcast intent
        broadcastIntent
                .setAction(GEOFENCE_ACTION)
                .putExtra("GEOFENCE_STATUS", errorMessage);

        // Broadcast the error *locally* to other components in this app
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                broadcastIntent);
    }


    private void handleEnterExit(GeofencingEvent geofencingEvent) {
        Log.d(TAG, "handleEnterExit");

        // Get the type of transition (entry or exit)
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that a valid transition was reported
        if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                || (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                //payload_data=getNotificationMessageFromPreference(geofence.getRequestId());
                //String strNotificationTitle=getNotificationTitle(payload_data);
                String geofenceTransitionString = getTransitionString(geofenceTransition);
                String geofenceText=geofenceTransitionString+" : "+geofence.getRequestId();
                Log.i(TAG, "Geofence Transition:" + geofenceText);

                sendEventDetailNotificatonIntent(geofenceText);

                // Create an Intent to broadcast to the app
                broadcastIntent
                        .setAction(GEOFENCE_ACTION)
                        .putExtra("EXTRA_GEOFENCE_ID", geofence.getRequestId())
                        .putExtra("EXTRA_GEOFENCE_TRANSITION_TYPE", geofenceTransitionString);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
            }
        } else {
            // Always log as an error
            Log.e(TAG,
                    context.getString(R.string.geofence_transition_invalid_type,
                            geofenceTransition));
        }
    }

    private String getNotificationTitle(String payload){
        String title=null;
        try {
            JSONObject json = new JSONObject(payload);
            if(json.has("title")) {
                title=(String) json.get("title");
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return title;
    }


    private String getMessage(String payload){
        String message=null;
        try {
            JSONObject json = new JSONObject(payload);
            if(json.has("msg")) {
                message=(String) json.get("msg");
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return message;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return context.getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return context.getString(R.string.geofence_transition_exited);
            default:
                return context.getString(R.string.unknown_geofence_transition);
        }
    }

    protected void sendEventDetailNotificatonIntent(String event_name) {
        Log.d(TAG, "sendEventDetailNotificatonIntent");

        int requestID = (int) System.currentTimeMillis();
        Intent event_detail_intent = new Intent(context, LocationDetailActivity.class);
        event_detail_intent.putExtra("NotifyTitle",event_name);

        PendingIntent pIntent = PendingIntent.getActivity(context, requestID,
                event_detail_intent, 0);
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(event_name)
                .setSmallIcon(R.drawable.notify_small);
        // Set pending intent
        notificationBuilder.setContentIntent(pIntent);

        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        //	defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        notificationBuilder.setDefaults(defaults);
        // Set the content for Notification
        notificationBuilder.setContentText("Tap here to view detail");
        // Set autocancel
        notificationBuilder.setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Assign random number for multiple notification
        Random random = new Random();
        int randomNumber = random.nextInt(9999 - 1000) + 1000;
        notificationManager.notify(randomNumber /* ID of notification */, notificationBuilder.build());
    }
}