/*
Copyright (c) 2007, Dennis M. Sosnoski.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.sosnoski.seismic.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.sosnoski.seismic.common.Quake;
import com.sosnoski.seismic.common.QuakeSet;
import com.sosnoski.seismic.common.Query;
import com.sosnoski.seismic.common.Region;
import com.sosnoski.seismic.common.Response;

/**
 * Quake in-memory database. This reads input files to generate searchable data
 * structures for the collection of earthquake information.
 */
public class QuakeBase
{
    private static QuakeBase s_instance;

    private SeismicInfo[] m_seismicInfos = new SeismicInfo[50];
    private Region[] m_regions = new Region[729];
    
//    private final DateFormat m_format;
    
    /**
     * Constructor.
     */
    private QuakeBase() {
/*        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        m_format.setCalendar(calendar);
*/        try {
            
            // load the earthquake database
            BufferedReader rrdr = new BufferedReader
                (new InputStreamReader(QuakeBase.class.
                getResourceAsStream("/regions.txt")));
            readRegions(rrdr);
            BufferedReader qrdr =
                new BufferedReader(new InputStreamReader(QuakeBase.class.
                getResourceAsStream("/fulldata.txt")));
            readQuakes(qrdr);
            for (int i = 0; i < m_seismicInfos.length; i++) {
                m_seismicInfos[i].fix();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Read region information. This reads the major and minor regions used for
     * earthquake information, with one region per line.
     * 
     * @param rdr region reader
     * @throws IOException
     * @throws ParseException
     */
    private void readRegions(BufferedReader rdr)
        throws IOException, ParseException {
        
        // read region configuration from input file
        String line;
        while ((line = rdr.readLine()) != null) {
            
            // check type of line
            line = line.trim();
            if (line.startsWith("*")) {
                
                // process new top-level seismic region
                line = line.substring(1).trim();
                int mark = line.indexOf(' ');
                int index = Integer.parseInt(line.substring(0, mark));
                m_seismicInfos[index-1] =
                    new SeismicInfo(index, line.substring(mark+1).trim());
                    
            } else {
                
                // process new second-level seismic region
                int mark = line.indexOf(' ');
                int index = Integer.parseInt(line.substring(0, mark));
                line = line.substring(mark+1).trim();
                mark = line.indexOf(' ');
                int owner = Integer.parseInt(line.substring(0, mark));
                Region region =
                    new Region(owner, index, line.substring(mark+1).trim());
                m_regions[index-1] = region;
                m_seismicInfos[owner-1].m_regionList.add(region);
                
            }
        }
    }
    
    /**
     * Read quake information. This reads the set of quakes, one quake per line,
     * and organizes them by region.
     * 
     * @param rdr quake reader
     * @throws IOException
     * @throws ParseException
     */
    private void readQuakes(BufferedReader rdr)
        throws IOException, ParseException {
        
        // process actual quake listing input file
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setCalendar(calendar);
        String line;
        while ((line = rdr.readLine()) != null) {
            
            // parse the date and milliseconds
            int mark = line.indexOf(' ');
            mark = line.indexOf(' ', mark+1);
            Date date = format.parse(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            mark = line.indexOf(' ');
            int millis = Integer.parseInt(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            
            // parse the latitude and longitude
            mark = line.indexOf(' ');
            float lat = Float.parseFloat(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            mark = line.indexOf(' ');
            float lng = Float.parseFloat(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            
            // parse the depth, magnitude, and method
            mark = line.indexOf(' ');
            float depth = Float.parseFloat(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            mark = line.indexOf(' ');
            float mag = Float.parseFloat(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            mark = line.indexOf(' ');
            String method = line.substring(0, mark);
            line = line.substring(mark+1).trim();
        
            // finish with the region and seismic area codes
            mark = line.indexOf(' ');
            int region = Integer.parseInt(line.substring(0, mark));
            line = line.substring(mark+1).trim();
            int seismic = Integer.parseInt(line);
        
            // create and add quake information
            Quake quake = new Quake(date, millis, lat, lng, mag,
                method, depth, m_regions[region-1]);
            m_seismicInfos[seismic-1].addQuake(quake);
                
        }
    }
    
    /**
     * Handle a query, returning all matching quakes organized by major region
     * and in date order.
     * 
     * @param minlat minimum latitude (<code>null</code> if none)
     * @param maxlat maximum latitude (<code>null</code> if none)
     * @param minlng minimum longitude (<code>null</code> if none)
     * @param maxlng maximum longitude (<code>null</code> if none)
     * @param mindate earliest time (<code>null</code> if none)
     * @param maxdate latest time (<code>null</code> if none)
     * @param minmag minimum magnitude (<code>null</code> if none)
     * @param maxmag maximum magnitude (<code>null</code> if none)
     * @param mindepth minimum depth (<code>null</code> if none)
     * @param maxdepth maximum depth (<code>null</code> if none)
     * @return matching quake sets
     */
    public QuakeSet[] handleQuery(Float minlat, Float maxlat, Float minlng,
        Float maxlng, Date mindate, Date maxdate, Float minmag, Float maxmag,
        Float mindepth, Float maxdepth) {
        
/*        System.out.println("Processing query for date range from "
            + m_format.format(mindate) + " to "
            + m_format.format(maxdate) + ",");
        System.out.println("  longitude range from " + minlng + " to " + maxlng + ",");
        System.out.println("  latitude range from " + minlat + " to " + maxlat + ",");
*/        
        // initialize for query handling
        ArrayList sets = new ArrayList();
        ArrayList matches = new ArrayList();
        boolean[] rgnhits = new boolean[m_regions.length];
        for (int i = 0; i < m_seismicInfos.length; i++) {
            
            // check for potential overlap in geographic range
            SeismicInfo seis = m_seismicInfos[i];
            if (seis.isRangeOverlap(minlat, maxlat, minlng, maxlng)) {
                
                // clear matches for this seismic area
                matches.clear();
                for (int j = 0; j < rgnhits.length; j++) {
                    rgnhits[j] = false;
                }
                int rgncnt = 0;
                
                // run through all quakes to find matches
                Quake[] quakes = seis.m_quakes;
                int index = 0;
                if (mindate != null) {
                    index = seis.firstQuake(mindate.getTime());
                }
                int limit = quakes.length;
                if (maxdate != null) {
                    limit = seis.firstQuake(maxdate.getTime()+1);
                }
                for (; index < limit; index++) {
                    
                    // match quake information against query constraints
                    Quake quake = quakes[index];
                    boolean match = true;
                    if (minlng != null) {
                        match = minlng.floatValue() <= quake.getLongitude();
                    }
                    if (match && maxlng != null) {
                        match = maxlng.floatValue() > quake.getLongitude();
                    }
                    if (match && minlat != null) {
                        match = minlat.floatValue() <= quake.getLatitude();
                    }
                    if (match && maxlat != null) {
                        match = maxlat.floatValue() > quake.getLatitude();
                    }
                    if (match && minmag != null) {
                        match = minmag.floatValue() <= quake.getMagnitude();
                    }
                    if (match && maxmag != null) {
                        match = maxmag.floatValue() > quake.getMagnitude();
                    }
                    if (match && mindepth != null) {
                        match = mindepth.floatValue() <= quake.getDepth();
                    }
                    if (match && maxdepth != null) {
                        match = maxdepth.floatValue() > quake.getDepth();
                    }
                    
                    // check matched result
                    if (match) {
                        matches.add(quake);
                        int rcode = quake.getRegion().getRegionCode();
                        if (!rgnhits[rcode-1]) {
                            rgnhits[rcode-1] = true;
                            rgncnt++;
                        }
                    }
                }
                
                // add result set if matches found for this seismic area
                if (matches.size() > 0) {
                    QuakeSet set = new QuakeSet();
                    set.setSeismicRegion(seis.m_seismicCode);
                    set.setSeismicName(seis.m_seismicName);
                    Region[] regions = new Region[rgncnt];
                    int fill = 0;
                    for (int j = 0; j < rgnhits.length; j++) {
                        if (rgnhits[j]) {
                            regions[fill++] = m_regions[j];
                        }
                    }
                    set.setRegions(regions);
                    set.setQuakes((Quake[])matches.
                        toArray(new Quake[matches.size()]));
                    sets.add(set);
                }
                
            }
        }
        
        // finish by setting all the results found
        return (QuakeSet[])sets.toArray(new QuakeSet[sets.size()]);
    }
    
    /**
     * Get singleton instance of class.
     * 
     * @return instance
     */
    public static QuakeBase getInstance() {
        if (s_instance == null) {
            s_instance = new QuakeBase();
        }
        return s_instance;
    }
    
    /**
     * Process a query. This is the service method used by the JibxSoap code.
     * 
     * @param query
     * @return response
     */
    public static Response process(Query query) {
        QuakeSet[] results = getInstance().handleQuery(query.getMinLatitude(),
            query.getMaxLatitude(), query.getMinLongitude(),
            query.getMaxLongitude(), query.getMinDateTime(),
            query.getMaxDateTime(), query.getMinMagnitude(),
            query.getMaxMagnitude(), query.getMinDepth(), query.getMaxDepth());
        Response response = new Response();
        response.setSets(results);
        return response;
    }
    
	/**
	 * Data structure for the top-level seismic regions.
	 */
    
    public static class SeismicInfo
    {
        // basic information
        private final int m_seismicCode;
        private final String m_seismicName;
        private ArrayList m_regionList;
        private ArrayList m_quakeList;
        private Region[] m_regions;
        private Quake[] m_quakes;
        
        // -90.0 <= minLatitude < maxLatitude <= 90.0
        private double minLatitude;
        private double maxLatitude;
        
        // flag for contiguous longitude range (regular minimum and maximum)
        private boolean isContiguousLongitude;
        
        // regular minimum and maximum for longitude
        private double minLongitude;
        private double maxLongitude;
        
        // maximum value less than zero and minimum value greater than zero
        private double maxNegativeLongitude;
        private double minPositiveLongitude;
        
        /**
         * Constructor.
         * 
         * @param index region index
         * @param name region name
         */
        private SeismicInfo(int index, String name) {
            m_seismicCode = index;
            m_seismicName = name;
            m_regionList = new ArrayList();
            m_quakeList = new ArrayList();
            minLatitude = 90.0;
            maxLatitude = -90.0;
            maxNegativeLongitude = Double.NEGATIVE_INFINITY;
            minPositiveLongitude = Double.POSITIVE_INFINITY;
            minLongitude = 180.0;
            maxLongitude = -180.0;
        }
        
        /**
         * Add a quake to the set included in this region. Quakes must be added
         * in time-order.
         * 
         * @param quake
         */
        public void addQuake(Quake quake) {
            
            // check for first quake
            double lng = quake.getLongitude();
            double lat = quake.getLatitude();
            if (m_quakeList.size() == 0) {
                
                // first quake in region automatically sets range
                minLatitude = lat;
                maxLatitude = lat;
                minLongitude = lng;
                maxLongitude = lng;
                if (lng < 0) {
                    maxNegativeLongitude = lng;
                } else {
                    minPositiveLongitude = lng;
                }
                
            } else {
                
                // adjust latitude range if needed
                if (lat < minLatitude) {
                    minLatitude = lat;
                } else if (lat > maxLatitude) {
                    maxLatitude = lat;
                }
                
                // adjust longitude range
                if (lng < 0) {
                    if (maxNegativeLongitude < lng) {
                        maxNegativeLongitude = lng;
                    }
                } else {
                    if (minPositiveLongitude > lng) {
                        minPositiveLongitude = lng;
                    }
                }
                if (minLongitude > lng) {
                    minLongitude = lng;
                }
                if (maxLongitude < lng) {
                    maxLongitude = lng;
                }
            }
            m_quakeList.add(quake);
        }
        
        /**
         * Fix the arrays of regions and quakes, and the form of the longitude
         * range.
         */
        public void fix() {
            double range1 = maxLongitude - minLongitude;
            double range2 = Double.NEGATIVE_INFINITY;
            if (!Double.isInfinite(maxNegativeLongitude) &&
                !Double.isInfinite(minPositiveLongitude)) {
                range2 = 360.0 + maxNegativeLongitude - minPositiveLongitude;
            }
            isContiguousLongitude = range1 >= range2;
            m_regions = (Region[])
                m_regionList.toArray(new Region[m_regionList.size()]);
            m_quakes = (Quake[])
                m_quakeList.toArray(new Quake[m_quakeList.size()]);
        }
        
        /**
         * Find the index of the first quake which has a time greater than or
         * equal to the supplied time.
         *
         * @param time earliest start time to be found
         * @return index of first quake that happened at or after the start
         * time
         */
        public int firstQuake(long time) {
            int base = 0;
            int limit = m_quakes.length - 1;
            while (base <= limit) {
                int cur = (base + limit) >> 1;
                long diff = time - m_quakes[cur].getDateTime().getTime();
                if (diff < 0) {
                    limit = cur - 1;
                } else if (diff > 0) {
                    base = cur + 1;
                } else {
                    return cur;
                }
            }
            return base;
        }
        
        /**
         * Check for overlap between the bounding rectangle of this region and
         * a supplied rectangle.
         * 
         * @param minlat minimum latitude (<code>null</code> if no minimum)
         * @param maxlat maximum latitude (<code>null</code> if no maximum)
         * @param minlng minimum longitude (<code>null</code> if no minimum)
         * @param maxlng maximum longitude (<code>null</code> if no maximum)
         * @return <code>true</code> if any overlap, <code>false</code> if not
         */
        public boolean isRangeOverlap(Float minlat, Float maxlat, Float minlng,
            Float maxlng) {
            if (minlat == null || minlat.doubleValue() <= maxLatitude &&
                maxlat == null || maxlat.doubleValue() >= minLatitude) {
                double mnlng = minlng == null ?
                    Double.NEGATIVE_INFINITY : minlng.doubleValue();
                double mxlng = maxlng == null ?
                    Double.POSITIVE_INFINITY : maxlng.doubleValue();
                if (isContiguousLongitude) {
                    if (mnlng <= maxLongitude && mxlng >= minLongitude) {
                        return true;
                    }
                } else {
                    if (mnlng <= maxNegativeLongitude ||
                        mnlng >= minPositiveLongitude ||
                        mxlng <= maxNegativeLongitude ||
                        mxlng >= minPositiveLongitude) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}