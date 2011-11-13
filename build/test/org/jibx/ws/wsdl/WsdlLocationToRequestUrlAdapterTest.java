package org.jibx.ws.wsdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.jibx.ws.wsdl.InputStreamWsdlProvider;
import org.jibx.ws.wsdl.WsdlLocationToRequestUrlAdapter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Tests for WsdlLocationToRequestUrlAdapter.  For the purpose of these tests, the "base url" means 
 * <code>&lt;scheme>://&lt;server>:&lt;port></code>, eg <code>http://localhost:8080</code>. 
 */
public class WsdlLocationToRequestUrlAdapterTest
{
    private static final String WSDL_FILE_PATH = "build/test/MyService.wsdl";
    private static final String WSDL_MULTI_LOC_FILE_PATH = "build/test/TestServices.wsdl";
    private static MockHttpServletRequest request;
    private static String requestBaseUrl;
    private static String baseWsdl;

    @BeforeClass
    public static void setUp() throws Exception {
        request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("test.example.com");
        request.setServerPort(80);
        requestBaseUrl = "http://test.example.com:80";
        File wsdlFile = new File(WSDL_FILE_PATH);
        baseWsdl = FileUtils.readFileToString(wsdlFile);
    }

    /** Tests relative URL. */
    @Test
    public void givenWsdlWithRelativeUrl_writeWsdl_shouldReturnBaseUrlFromRequestPlusRelativeUrl() throws Exception {
        String initialWsdl = wsdlWithLocation("/example/service");
        WsdlLocationToRequestUrlAdapter adapter = createAdapter(initialWsdl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        String responseWsdl = new String(baos.toByteArray());

        String expectedWsdl = wsdlWithLocation("http://test.example.com:80/example/service");
        XMLAssert.assertXMLEqual(expectedWsdl, responseWsdl);
    }

    /** Tests absolute URL. */
    @Test
    public void givenWsdlWithAbsoluteUrl_writeWsdl_shouldReturnUrlWithBaseUrlReplaced() throws Exception {
        String initialWsdl = wsdlWithLocation("http://localhost:8081/example/service");
        WsdlLocationToRequestUrlAdapter adapter = createAdapter(initialWsdl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        String responseWsdl = new String(baos.toByteArray());

        String expectedWsdl = wsdlWithLocation("http://test.example.com:80/example/service");
        XMLAssert.assertXMLEqual(expectedWsdl, responseWsdl);
    }
    
    /** Tests absolute URL. */
    @Test
    public void givenWsdlWithUnknownUrlFormat_writeWsdl_shouldReturnInitialUrl() throws Exception {
        String initialWsdl = wsdlWithLocation("service");
        WsdlLocationToRequestUrlAdapter adapter = createAdapter(initialWsdl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        String responseWsdl = new String(baos.toByteArray());

        XMLAssert.assertXMLEqual(initialWsdl, responseWsdl);
    }
    
    /** Tests caching. */
    @Test
    public void givenWsdlAdapter_whenWsdlIsRequestedTwice_shouldReturnAdaptedWsdl() throws Exception {
        String initialWsdl = wsdlWithLocation("/example/service");
        WsdlLocationToRequestUrlAdapter adapter = createAdapter(initialWsdl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        String responseWsdl = new String(baos.toByteArray());

        String expectedWsdl = wsdlWithLocation("http://test.example.com:80/example/service");
        XMLAssert.assertXMLEqual(expectedWsdl, responseWsdl);
    }

    /** Tests multiple locations in WSDL. */
    @Test
    public void givenWsdlWithMultipleLocations_writeWsdl_shouldAdaptMultipleLocations() throws Exception {
        File wsdlFile = new File(WSDL_MULTI_LOC_FILE_PATH);
        baseWsdl = FileUtils.readFileToString(wsdlFile);
        String initialWsdl = baseWsdl.replace("$serviceLocation1$", "/service1");
        initialWsdl = initialWsdl.replace("$serviceLocation2$", "/example2/service2");
        initialWsdl = initialWsdl.replace("$serviceLocation3$", "http://foo.com/baa");
        WsdlLocationToRequestUrlAdapter adapter = createAdapter(initialWsdl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        baos = new ByteArrayOutputStream(1024);
        adapter.writeWSDL(baos, request);
        String responseWsdl = new String(baos.toByteArray());

        String expectedWsdl = baseWsdl.replace("$serviceLocation1$", "http://test.example.com:80/service1");
        expectedWsdl = expectedWsdl.replace("$serviceLocation2$", "http://test.example.com:80/example2/service2");
        expectedWsdl = expectedWsdl.replace("$serviceLocation3$", "http://test.example.com:80/baa");
        XMLAssert.assertXMLEqual(expectedWsdl, responseWsdl);
    }

    /**
     * Returns WSDL with the service address location set to the given location. 
     */
    private String wsdlWithLocation(String location) {
        return baseWsdl.replace("$serviceLocation$", location);
    }
    
    private WsdlLocationToRequestUrlAdapter createAdapter(String wsdl) throws IOException {
        InputStreamWsdlProvider wsdlProvider = new InputStreamWsdlProvider(new ByteArrayInputStream(wsdl.getBytes()));
        return new WsdlLocationToRequestUrlAdapter(wsdlProvider);
    }
}
