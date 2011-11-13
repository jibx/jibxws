
package com.sosnoski.seismic.common;

public class QuakeSet
{
    private int seismicRegion;
    private String seismicName;
    private Region[] regions;
    private Quake[] quakes;
    
    private int getRegionsLength() {
        return regions.length;
    }
    private int getQuakesLength() {
        return quakes.length;
    }
    
    private void setRegionsLength(int length) {
        regions = new Region[length];
    }
    private void setQuakesLength(int length) {
        quakes = new Quake[length];
    }
    
    private Object getRegion(int i) {
        return regions[i];
    }
    private Object getQuake(int i) {
        return quakes[i];
    }
    
    private void setRegion(int i, Object region) {
        regions[i] = (Region)region;
    }
    private void setQuake(int i, Object quake) {
        quakes[i] = (Quake)quake;
    }
    
    public Quake[] getQuakes() {
        return quakes;
    }
    public Region[] getRegions() {
        return regions;
    }
    public String getSeismicName() {
        return seismicName;
    }
    public int getSeismicRegion() {
        return seismicRegion;
    }

    public void setQuakes(Quake[] list) {
        quakes = list;
    }
    public void setRegions(Region[] list) {
        regions = list;
    }
    public void setSeismicName(String string) {
        seismicName = string;
    }
    public void setSeismicRegion(int i) {
        seismicRegion = i;
    }
}

