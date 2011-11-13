
package com.sosnoski.seismic.common;

public class Region
{
    private int areaCode;
    private int regionCode;
    private String regionName;
    
    public Region() {}
    
    public Region(int area, int code, String name) {
        areaCode = area;
        regionCode = code;
        regionName = name;
    }
    
    private String getIdent() {
        return "rgn" + regionCode;
    }
    
    private void setIdent(String value) {}
    
    public int getRegionCode() {
        return regionCode;
    }
    public String getRegionName() {
        return regionName;
    }

    public void setRegionCode(int i) {
        regionCode = i;
    }
    public void setRegionName(String string) {
        regionName = string;
    }
}

