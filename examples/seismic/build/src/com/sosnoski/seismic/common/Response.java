
package com.sosnoski.seismic.common;

public class Response
{
    private QuakeSet[] sets;
    
    private int getLength() {
        return sets.length;
    }
    
    private void setLength(int length) {
        sets = new QuakeSet[length];
    }
    
    private Object getSet(int i) {
        return sets[i];
    }
    
    private void setSet(int i, Object set) {
        sets[i] = (QuakeSet)set;
    }
    
    public QuakeSet[] getSets() {
        return sets;
    }

    public void setSets(QuakeSet[] sets) {
        this.sets = sets;
    }
}
