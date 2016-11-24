package com.truedreamz.accurategeofencing;

import org.json.JSONObject;

import java.io.Serializable;

public class NotificationItem implements Serializable {

    public String notifiID;
    public String notifiIdentifier;
    public String notifiDescription;
    public double notifiGeoLat;
    public double notifiGeoLong;
    public String notifiRadius;
    public JSONObject notifiMessage;
    public String notifiExpire;

    public NotificationItem(){

    }

    public NotificationItem(String notifi_id, String notifi_identi, String notifi_Desc, double notifi_lat, double notifi_long, String notifi_radius,
                            JSONObject message,String expire){
        this.notifiID=notifi_id;
        this.notifiIdentifier=notifi_identi;
        this.notifiDescription=notifi_Desc;
        this.notifiGeoLat=notifi_lat;
        this.notifiGeoLong=notifi_long;
        this.notifiRadius=notifi_radius;
        this.notifiMessage=message;
        this.notifiExpire=expire;
    }

}
