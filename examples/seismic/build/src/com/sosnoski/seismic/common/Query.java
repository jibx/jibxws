
package com.sosnoski.seismic.common;

import java.util.Date;

public class Query
{
    private Date minDateTime;
    private Date maxDateTime;
    private Float minLongitude;
    private Float maxLongitude;
    private Float minLatitude;
    private Float maxLatitude;
    private Float minMagnitude;
    private Float maxMagnitude;
    private Float minDepth;
    private Float maxDepth;
    
    public Date getMaxDateTime() {
        return maxDateTime;
    }
    public Float getMaxDepth() {
        return maxDepth;
    }
    public Float getMaxLatitude() {
        return maxLatitude;
    }
    public Float getMaxLongitude() {
        return maxLongitude;
    }
    public Float getMaxMagnitude() {
        return maxMagnitude;
    }
    public Date getMinDateTime() {
        return minDateTime;
    }
    public Float getMinDepth() {
        return minDepth;
    }
    public Float getMinLatitude() {
        return minLatitude;
    }
    public Float getMinLongitude() {
        return minLongitude;
    }
    public Float getMinMagnitude() {
        return minMagnitude;
    }

    public void setMaxDateTime(Date date) {
        maxDateTime = date;
    }
    public void setMaxDepth(Float f) {
        maxDepth = f;
    }
    public void setMaxLatitude(Float f) {
        maxLatitude = f;
    }
    public void setMaxLongitude(Float f) {
        maxLongitude = f;
    }
    public void setMaxMagnitude(Float f) {
        maxMagnitude = f;
    }
    public void setMinDateTime(Date date) {
        minDateTime = date;
    }
    public void setMinDepth(Float f) {
        minDepth = f;
    }
    public void setMinLatitude(Float f) {
        minLatitude = f;
    }
    public void setMinLongitude(Float f) {
        minLongitude = f;
    }
    public void setMinMagnitude(Float f) {
        minMagnitude = f;
    }
}

