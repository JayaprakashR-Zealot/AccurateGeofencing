package com.truedreamz.accurategeofencing;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by augray on 4/18/2016.
 */
public class LocationDetailActivity  extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);

        Bundle e = getIntent().getExtras();
        if(e!=null){
          //int  eventID=Integer.valueOf(e.getString("ID"));
          String strEventText=e.getString("NotifyTitle");

            if(strEventText!=null){
                TextView txtEventDetail=(TextView)findViewById(R.id.txtEventDetail);
                txtEventDetail.setText(strEventText);
            }

            /*if(eventID!=-1){
                // if you want cancel notification
                NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(eventID);
            }*/


        }
    }
}
