package com.sosnoski.seismic.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimeZone;

/**
 * Base client test program. This base program is used for testing all variations of the seismic service. It takes a
 * pair of basic parameters for controlling the test run, first a decimal fraction giving the portion of the total space
 * and total time ranges specified for each query, and second the number of queries to execute.
 */
public abstract class TestClient
{
    protected static long s_minDate;
    
    protected static long s_maxDate;
    
    protected static float s_minLong = -180.0f;
    
    protected static float s_maxLong = 180.0f;
    
    protected static float s_minLat = -90.0f;
    
    protected static float s_maxLat = 90.0f;

    protected abstract String defaultPath();

    protected abstract String defaultProtocol();
    
    protected abstract Object configure(String path, String m_protocol, String reqtype, String[] rsptypes);
    
    protected abstract Object runQuery(QueryData query, Object stub);
    
    protected abstract int summarize(boolean verbose, Object obj);
    
    protected void close(Object obj) {}
    
    protected void runTest(String[] args) throws Exception {
        
        // make sure required arguments are supplied
        if (args.length < 2) {
            System.out.println("Usage: java TestClient fraction loops [options]\n"
                + "Where fraction is a decimal range fraction, loops is the number of requests\n"
                + "to time, and options are:\n"
                + "  -n=n        number of threads to run (default is 1),\n"
                + "  -p=path     path to the server application (the default for this\n"
                + "              client is " + defaultPath() + ")"
                + "  -q          flags printing only summary information\n"
                + "  -r=type[,type...] media type(s) to be accepted for the responses (default is\n"
                + "             'xml')\n"
                + "  -s=type     media type to be used for sending the requests (default is 'xml')\n"
                + "  -t=target   target host and port specification (http://localhost:8080\n"
                + "              by default)"
                + "  -z=protocol protocol to use (the default for this client is " + defaultProtocol() + ")");
        } else {
            
            // initialize everything for test loop
            double fraction = Double.parseDouble(args[0]);
            int loops = Integer.parseInt(args[1]);
            String reqtype = "xml";
            String[] rsptypes = new String[] { "xml" };
            String target = "http://localhost:8080";
            String path = defaultPath();
            String protocol = defaultProtocol();
            
            int numthread = 1;
            boolean verbose = true;
            int index = 2;
            while (index < args.length) {
                String arg = args[index];
                if (arg.length() >= 2 && arg.charAt(0) == '-') {
                    if (arg.charAt(1) == 'q') {
                        verbose = false;
                    } else if (arg.length() > 3 && arg.charAt(2) == '=') {
                        switch (arg.charAt(1)) {
                            case 'n':
                                numthread = Integer.parseInt(arg.substring(3));
                                break;
                            case 'p':
                                path = arg.substring(3);
                                break;
                            case 'r':
                            {
                                ArrayList types = new ArrayList();
                                int base = 3;
                                int split;
                                while ((split = arg.indexOf(',', base)) > 0) {
                                    types.add(arg.substring(base, split));
                                    base = split + 1;
                                }
                                types.add(arg.substring(base));
                                rsptypes = (String[])types.toArray(new String[types.size()]);
                                break;
                            }
                            case 's':
                                reqtype = arg.substring(3);
                                break;
                            case 't':
                                target = arg.substring(3);
                                break;
                            case 'z':
                                protocol = arg.substring(3);
                                break;
                            default:
                                System.err.println("Unknown argument " + index + ": '" + arg + '\'');
                                System.exit(1);
                        }
                    } else {
                        System.err.println("Illegal argument format on argument " + index + ": '" + arg + '\'');
                        System.exit(1);
                    }
                    index++;
                } else {
                    System.err.println("Illegal argument format on argument " + index + ": '" + arg + '\'');
                    System.exit(1);
                }
            }
            
            // create the test threads
            Thread[] threads = new Thread[numthread];
            TestRunnable[] runnables = new TestRunnable[numthread];
            for (int i = 0; i < threads.length; i++) {
                String lead = "";
                if (numthread > 1) {
                    lead = "Thread " + i + ": ";
                }
                runnables[i] = new TestRunnable(lead, fraction, target + path, protocol, verbose, loops, reqtype, rsptypes);
                threads[i] = new Thread(runnables[i]);
            }
            
            // run the tests and wait for completion
            long start = System.currentTimeMillis();
            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
            }
            int total = 0;
            for (int i = 0; i < threads.length; i++) {
                Thread thread = threads[i];
                synchronized (thread) {
                    if (thread.isAlive()) {
                        thread.wait();
                    }
                }
                total += runnables[i].m_resultCount;
            }
            if (numthread > 1) {
                System.out.println("Total elapsed time for test run " + (System.currentTimeMillis() - start)
                    + " ms. with " + total + " results");
            }
        }
    }
    
    protected class TestRunnable implements Runnable
    {
        private final String m_lead;
        
        private final String m_path;

        private final String m_protocol;

        private final String m_requestType;
        
        private final String[] m_responseTypes;
        
        private final boolean m_verbose;
        
        private final int m_loops;
        
        private final Random m_random;
        
        private final TimeZone m_utcZone;
        
        private final DateFormat m_format;
        
        private double m_halfRangeFraction;
        
        private double m_halfRangeFractionRoot;
        
        private double m_complementFraction;
        
        protected int m_resultCount;
        
        public TestRunnable(String lead, double fraction, String path, String protocol, boolean verbose, int loops, String reqtype,
            String[] rsptypes) {
            m_lead = lead;
            m_path = path;
            m_protocol = protocol;
            m_requestType = reqtype;
            m_responseTypes = rsptypes;
            m_verbose = verbose;
            m_loops = loops;
            m_random = new Random(5);
            m_halfRangeFraction = fraction * 0.5;
            m_halfRangeFractionRoot = Math.sqrt(fraction) * 0.5;
            m_utcZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = new GregorianCalendar(m_utcZone);
            m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            m_format.setCalendar(calendar);
            try {
                s_minDate = m_format.parse("2000-01-01 01:19:26").getTime();
                s_maxDate = m_format.parse("2003-08-31 23:07:59").getTime();
            } catch (ParseException e) {
                throw new IllegalStateException("Error initializing");
            }
        }
        
        protected QueryData nextQuery() {
            
            // generate date range for query
            long ddiff = s_maxDate - s_minDate;
            long drange = (long)((m_random.nextDouble() * m_halfRangeFraction + m_halfRangeFraction) * ddiff);
            long dbase = s_minDate + (long)(m_random.nextDouble() * (ddiff - drange));
            
            // generate longitude range for query
            double ldiff = s_maxLong - s_minLong;
            float lngrng = (float)((m_random.nextDouble() * m_halfRangeFractionRoot + m_halfRangeFractionRoot) * ldiff);
            float lngbase = s_minLong + (float)(m_random.nextDouble() * (ldiff - lngrng));
            
            // generate latitude range for query
            ldiff = s_maxLat - s_minLat;
            float latrng = (float)((m_random.nextDouble() * m_halfRangeFractionRoot + m_halfRangeFractionRoot) * ldiff);
            float latbase = s_minLat + (float)(m_random.nextDouble() * (ldiff - latrng));
            
            // return generated query information
            return new QueryData(dbase, dbase + drange, lngbase, lngbase + lngrng, latbase, latbase + latrng);
        }
        
        public void run() {
            
            Object stub = configure(m_path, m_protocol, m_requestType, m_responseTypes);
            // run timed test
            long start = System.currentTimeMillis();
            for (int i = 0; i < m_loops; i++) {
                
                // generate and report query being sent
                QueryData query = nextQuery();
                if (m_verbose) {
                    System.out.println(m_lead + "Running query for date range from "
                        + m_format.format(new Date(query.m_timeMin)) + " to "
                        + m_format.format(new Date(query.m_timeMax)) + ",");
                    System.out.println("  longitude range from " + query.m_longMin + " to " + query.m_longMax + ",");
                    System.out.println("  latitude range from " + query.m_latMin + " to " + query.m_latMax + ",");
                }
                
                // time the actual call to the server
                long base = System.currentTimeMillis();
                Object resp = runQuery(query, stub);
                long time = System.currentTimeMillis() - base;
                
                // process returned results
                if (m_verbose) {
                    System.out.println(m_lead + "Results from query:");
                }
                int count = summarize(m_verbose, resp);
                m_resultCount += count;
                if (m_verbose) {
                    System.out.println(m_lead + "Result match count " + count + " in " + time + " ms.");
                    System.out.println();
                }
            }
            System.out.println(m_lead + "Total elapsed time for test " + (System.currentTimeMillis() - start)
                + " ms. with " + m_resultCount + " results");
            close(stub);
        }
    }
    
    protected static class QueryData
    {
        protected final long m_timeMin;
        
        protected final long m_timeMax;
        
        protected final float m_longMin;
        
        protected final float m_longMax;
        
        protected final float m_latMin;
        
        protected final float m_latMax;
        
        protected QueryData(long tmin, long tmax, float lngmin, float lngmax, float latmin, float latmax) {
            m_timeMin = tmin;
            m_timeMax = tmax;
            m_longMin = lngmin;
            m_longMax = lngmax;
            m_latMin = latmin;
            m_latMax = latmax;
        }
    }
}