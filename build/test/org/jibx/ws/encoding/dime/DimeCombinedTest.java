package org.jibx.ws.encoding.dime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.jibx.runtime.impl.InByteBuffer;
import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.encoding.dime.DimeCommon;
import org.jibx.ws.encoding.dime.DimeInputBuffer;
import org.jibx.ws.encoding.dime.DimeOutputBuffer;

public class DimeCombinedTest extends TestCase
{
    private static final int BLOCK_SIZE = 128;
    private static final int BUFFER_SIZE = 256;
    
    private DimeOutputBuffer m_dimeOut;
    private OutByteBuffer m_outBuffer;
    private ByteArrayOutputStream m_outStream;
    private DimeInputBuffer m_dimeIn;

    /**
     * Set up for test. This initializes the output configuration and opens the output message.
     * 
     * @throws Exception 
     */
    protected void setUp() throws Exception {
        m_dimeOut = new DimeOutputBuffer();
        m_outBuffer = new OutByteBuffer(BUFFER_SIZE);
        m_dimeOut.setBuffer(m_outBuffer);
        m_outStream = new ByteArrayOutputStream();
        m_outBuffer.setOutput(m_outStream);
        m_dimeOut.nextMessage();
        super.setUp();
    }

    /**
     * Change direction from writing to reading. This ends output and sets up the input to read whatever was written.
     * 
     * @param size input byte buffer size
     * @throws IOException
     */
    private void changeDirection(int size) throws IOException {
        m_dimeOut.finish();
        m_dimeIn = new DimeInputBuffer();
        InByteBuffer bytein = new InByteBuffer(size);
        bytein.setInput(new ByteArrayInputStream(m_outStream.toByteArray()));
        m_dimeIn.setBuffer(bytein);
        m_dimeIn.nextMessage();
    }
    
    /**
     * Write sequential int values as a message part.
     * 
     * @param start first write value
     * @param count number of values to write
     * @throws Exception
     */
    private void writePart(int start, int count) throws Exception {
        m_dimeOut.nextPart(null, DimeCommon.TYPE_NONE, null);
        int limit = start + count;
        for (int value = start; value < limit; value++) {
            m_dimeOut.free(BUFFER_SIZE, 2);
            int offset = m_dimeOut.getOffset();
            byte[] buff = m_dimeOut.getBuffer();
            buff[offset++] = (byte)(value >> 8);
            buff[offset++] = (byte)value;
            m_dimeOut.setOffset(offset);
        }
    }

    /**
     * Read and verify sequential int values as a message part.
     * 
     * @param start first expected value
     * @param count number of values to read
     * @throws Exception
     */
    private void readPart(int start, int count) throws Exception {
        assertTrue("Missing expected message part", m_dimeIn.nextPart());
        int limit = start + count;
        for (int value = start; value < limit; value++) {
            assertTrue("Missing data", m_dimeIn.require(2));
            byte[] buff = m_dimeIn.getBuffer();
            int offset = m_dimeIn.getOffset();
            int match = ((buff[offset++] & 0xFF) << 8) + (buff[offset++] & 0xFF);
            m_dimeIn.setOffset(offset);
            assertEquals("Data mismatch", value, match);
        }
    }
    
    // this test uses an in buffer larger than the out buffer, with a single message part
    public void testOnePartInBig() throws Exception {
        writePart(1, 1000);
        changeDirection(BUFFER_SIZE + 10);
        readPart(1, 1000);
    }
    
    // this test uses an in buffer smaller than the out buffer, with a single message part
    public void testOnePartInSmall() throws Exception {
        writePart(1, 1000);
        changeDirection(BUFFER_SIZE - 9);
        readPart(1, 1000);
    }
    
    // this test uses an in buffer larger than the out buffer, with multiple message parts
    public void testMultiPartInBig() throws Exception {
        writePart(1, 1000);
        m_dimeOut.flush();
        writePart(1001, 10);
        m_dimeOut.flush();
        writePart(1011, 1000);
        m_dimeOut.finish();
        changeDirection(BUFFER_SIZE + 10);
        readPart(1, 1000);
        readPart(1001, 10);
        readPart(1011, 1000);
    }
    
    // this test uses an in buffer smaller than the out buffer, with multiple message parts
    public void testMultiPartInSmall() throws Exception {
        writePart(1, 1000);
        m_dimeOut.flush();
        writePart(1001, 10);
        m_dimeOut.flush();
        writePart(1011, 1000);
        m_dimeOut.finish();
        changeDirection(BUFFER_SIZE - 9);
        readPart(1, 1000);
        readPart(1001, 10);
        readPart(1011, 1000);
    }
    
    // this test uses an in buffer larger than the out buffer, with multiple messages and multiple parts
    public void testMultiMessageInBig() throws Exception {
        writePart(1, 1000);
        m_dimeOut.flush();
        writePart(1001, 10);
        m_dimeOut.endMessage();
        m_dimeOut.flush();
        m_dimeOut.nextMessage();
        writePart(1011, 1000);
        m_dimeOut.flush();
        writePart(2011, 3);
        m_dimeOut.flush();
        writePart(2014, 1000);
        m_dimeOut.finish();
        changeDirection(BUFFER_SIZE + 10);
        readPart(1, 1000);
        readPart(1001, 10);
        assertTrue("Missing message", m_dimeIn.nextMessage());
        readPart(1011, 1000);
        readPart(2011, 3);
        readPart(2014, 1000);
    }
    
    // this test uses an in buffer larger than the out buffer, with multiple messages and multiple parts
    public void testMultiMessageSkip() throws Exception {
        writePart(1, 1000);
        m_dimeOut.flush();
        writePart(1001, 10);
        m_dimeOut.endMessage();
        m_dimeOut.flush();
        m_dimeOut.nextMessage();
        writePart(1011, 1000);
        m_dimeOut.flush();
        writePart(2011, 3);
        m_dimeOut.flush();
        writePart(2014, 1000);
        m_dimeOut.finish();
        changeDirection(BUFFER_SIZE + 10);
        readPart(1, 500);
        assertTrue("Missing message", m_dimeIn.nextMessage());
        readPart(1011, 500);
        readPart(2011, 3);
        readPart(2014, 1000);
    }
}