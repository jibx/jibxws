package com.sosnoski.seismic.client;

import java.io.IOException;
import java.util.Date;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.client.Client;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.protocol.Protocol;
import org.jibx.ws.protocol.ProtocolDirectory;
import org.jibx.ws.soap.SoapProtocol;
import org.jibx.ws.soap.client.SoapClient;

import com.sosnoski.seismic.common.QuakeSet;
import com.sosnoski.seismic.common.Query;
import com.sosnoski.seismic.common.Response;

public class BindClient extends TestClient
{
    protected String defaultPath() {
        return "/jibx-ws-seismic/soap/quake-service";
//        return "/jibx-ws-seismic/pox/quake-service";
//        return "/spring-ws-seismic/soap/quakeEndpoint";
    }
    
    protected String defaultProtocol() {
        return SoapProtocol.SOAP1_1.getName();
    }
    
    protected Object configure(String path, String protocol, String reqtype, String[] rsptypes) {
        try {
            IBindingFactory fact = BindingDirectory.getFactory(Query.class);
            System.out.println("Connecting to service at " + path + " using protocol " + protocol);
            Protocol p = ProtocolDirectory.getProtocol(protocol);
            MessageOptions options = new MessageOptions();
            options.setOutMediaTypeCode(reqtype);
            options.setInMediaTypeCodes(rsptypes);
            return p.createClient(path, fact, options);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new RuntimeException("Error: " + ex.getMessage(), ex);
        }
    }

    protected Object runQuery(QueryData data, Object stub) {
        Query query = new Query();
        query.setMinDateTime(new Date(data.m_timeMin));
        query.setMaxDateTime(new Date(data.m_timeMax));
        query.setMinLatitude(new Float(data.m_latMin));
        query.setMaxLatitude(new Float(data.m_latMax));
        query.setMinLongitude(new Float(data.m_longMin));
        query.setMaxLongitude(new Float(data.m_longMax));
        Response response = null;
        try {
//            SoapClient client = (SoapClient)stub;
//            client.setOperationName("urn:query");
            Client client = (Client)stub;
            response = (Response)(client).call(query);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        return response;
    }

    protected int summarize(boolean verbose, Object obj) {
        Response resp = (Response) obj;
        int count = 0;
        for (int j = 0; j < resp.getSets().length; j++) {
            QuakeSet set = resp.getSets()[j];
            if (verbose) {
                System.out.println("Seismic region " + set.getSeismicName() + " has " + set.getRegions().length
                    + " regions and " + set.getQuakes().length + " matching quakes");
            }
            count += set.getQuakes().length;
        }
        return count;
    }
    
    protected void close(Object obj) {
        try {
            ((Client)obj).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        BindClient client = new BindClient();
        client.runTest(args);
    }
}
