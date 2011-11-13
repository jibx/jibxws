
package com.sosnoski.seismic.common;

import java.util.Date;

public class Quake
{
    private Date dateTime;
    private int milliseconds;
    private float longitude;
    private float latitude;
    private float magnitude;
    private String method;
    private float depth;
    private Region region;
    
    public Quake() {}
    
    public Quake(Date time, int millis, float lat, float lng,
        float mag, String meth, float dpth, Region rgn) {
        dateTime = time;
        milliseconds = millis;
        longitude = lng;
        latitude = lat;
        magnitude = mag;
        method = meth;
        depth = dpth;
        region = rgn;
    }
        
    public Date getDateTime() {
        return dateTime;
    }
    public int getMilliseconds() {
        return milliseconds;
    }
    public float getDepth() {
        return depth;
    }
    public float getLatitude() {
        return latitude;
    }
    public float getLongitude() {
        return longitude;
    }
    public float getMagnitude() {
        return magnitude;
    }
    public Region getRegion() {
        return region;
    }

    public void setDateTime(Date date) {
        dateTime = date;
    }
    public void setMilliseconds(int millis) {
        milliseconds = millis;
    }
    public void setDepth(float f) {
        depth = f;
    }
    public void setLatitude(float f) {
        latitude = f;
    }
    public void setLongitude(float f) {
        longitude = f;
    }
    public void setMagnitude(float f) {
        magnitude = f;
    }
    public void setRegion(Region region) {
        this.region = region;
    }
}

