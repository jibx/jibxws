package org.jibx.ws.soap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import junit.framework.TestCase;

import org.jibx.runtime.QName;
import org.jibx.ws.WsException;

/**
 * Tests for {@link SoapFault} class. 
 */
public class SoapFaultTest extends TestCase
{
    /**
     * Tests that a prefix must be specified if a custom fault code is specified. 
     *
     * @throws Exception
     */
    public void testFaultCodeWithNoPrefix() throws Exception {
        try {
            QName faultCode = new QName("http://example.org/faultcodes", "ProcessingError");
            new SoapFault(faultCode, "An error occured while processing the message", null);
            fail("Expected WsException to be thrown if prefix is null");
        } catch (WsException e) {
            assertThat(e.getMessage(), is("faultCode prefix must be non-null for custom URIs"));
        }
    }

    /**
     * Tests that a URI must be specified with a fault code. 
     *
     * @throws Exception
     */
    public void testFaultCodeWithNoURI() throws Exception {
        try {
            QName faultCode = new QName("ProcessingError");
            new SoapFault(faultCode, "An error occured while processing the message", null);
            fail("Expected WsException to be thrown if URI is null");
        } catch (WsException e) {
            assertThat(e.getMessage(), is("faultCode URI must be non-null"));
        }
    }
}
