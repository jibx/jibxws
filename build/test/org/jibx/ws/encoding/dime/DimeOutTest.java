package org.jibx.ws.encoding.dime;

import java.io.ByteArrayOutputStream;

import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.encoding.dime.DimeCommon;
import org.jibx.ws.encoding.dime.DimeOutputBuffer;

import junit.framework.TestCase;

public class DimeOutTest extends TestCase
{
    private static final int DATA_SIZE = DimeCommon.HEADER_SIZE;
    
    private DimeOutputBuffer m_dimeBuffer;
    private OutByteBuffer m_streamBuffer;
    private ByteArrayOutputStream m_outStream;
    
    /**
     * Set up for test. This creates a small byte buffer, to allow forcing multiple blocks in a record.
     */
    protected void setUp() throws Exception {
        m_dimeBuffer = new DimeOutputBuffer();
        m_streamBuffer = new OutByteBuffer(DimeCommon.HEADER_SIZE+DATA_SIZE);
        m_dimeBuffer.setBuffer(m_streamBuffer);
        m_outStream = new ByteArrayOutputStream();
        m_streamBuffer.setOutput(m_outStream);
        m_dimeBuffer.nextMessage();
        super.setUp();
    }

    private static void match(byte[] actual, byte[] expect) {
        if (actual.length != expect.length) {
            fail("Data length mismatch\n actual: " + DimeCommon.dumpBytes(actual, 0, actual.length) +
                "\n expect: " + DimeCommon.dumpBytes(expect, 0, expect.length));
        }
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != expect[i]) {
                fail("Data mistmatch at offset " + i + "\n actual: " + DimeCommon.dumpBytes(actual, 0, actual.length) +
                    "\n expect: " + DimeCommon.dumpBytes(expect, 0, expect.length));
            }
        }
    }
    
    private void fillByte(int value) {
        int offset = m_dimeBuffer.getOffset();
        m_dimeBuffer.getBuffer()[offset++] = (byte)value;
        m_dimeBuffer.setOffset(offset);
    }
    
    public void testEmptyMessage() throws Exception {
        m_dimeBuffer.nextPart(null, DimeCommon.TYPE_NONE, null);
        m_dimeBuffer.finish();
        match(m_outStream.toByteArray(), TestData.messageBytes(TestData.EMPTY_MESSAGE));
    }
    
    public void testOptIdTypeOnlyMessage() throws Exception {
        m_dimeBuffer.nextPart("bc", DimeCommon.TYPE_MEDIA, "def");
        m_dimeBuffer.finish();
        match(m_outStream.toByteArray(), TestData.messageBytes(TestData.IDTYPEONLY_MESSAGE));
    }
    
    public void testOneBlockOneDataMessage() throws Exception {
        m_dimeBuffer.nextPart(null, DimeCommon.TYPE_NONE, null);
        m_dimeBuffer.free(Integer.MAX_VALUE, 1);
        int offset = m_dimeBuffer.getOffset();
        m_dimeBuffer.getBuffer()[offset++] = 1;
        m_dimeBuffer.setOffset(offset);
        m_dimeBuffer.finish();
        match(m_outStream.toByteArray(), TestData.messageBytes(TestData.ONEBLOCKONEDATA_MESSAGE));
    }
    
    public void testTwoBlockOneData1Message() throws Exception {
        m_dimeBuffer.nextPart(null, DimeCommon.TYPE_NONE, null);
        m_dimeBuffer.free(Integer.MAX_VALUE, 1);
        fillByte(1);
        m_dimeBuffer.free(Integer.MAX_VALUE, DATA_SIZE);
        m_dimeBuffer.finish();
        match(m_outStream.toByteArray(), TestData.messageBytes(TestData.TWOBLOCKONEDATA1_MESSAGE));
    }
    
    public void testTwoBlockTwoPartsMessage() throws Exception {
        m_dimeBuffer.nextPart(null, DimeCommon.TYPE_NONE, null);
        m_dimeBuffer.free(Integer.MAX_VALUE, 6);
        fillByte(1);
        fillByte(2);
        fillByte(3);
        fillByte(4);
        fillByte(5);
        fillByte(6);
        m_dimeBuffer.free(Integer.MAX_VALUE, DATA_SIZE);
        fillByte(7);
        fillByte(8);
        fillByte(9);
        m_dimeBuffer.flush();
        m_dimeBuffer.nextPart("bc", DimeCommon.TYPE_MEDIA, "def");
        m_dimeBuffer.free(Integer.MAX_VALUE, 3);
        fillByte(1);
        fillByte(2);
        fillByte(3);
        m_dimeBuffer.free(Integer.MAX_VALUE, DATA_SIZE);
        fillByte(4);
        fillByte(5);
        fillByte(6);
        m_dimeBuffer.finish();
        match(m_outStream.toByteArray(), TestData.messageBytes(TestData.TWOBLOCK_TWOPARTS_MESSAGE));
    }
}