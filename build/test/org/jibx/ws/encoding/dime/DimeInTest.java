package org.jibx.ws.encoding.dime;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jibx.runtime.impl.InByteBuffer;
import org.jibx.ws.encoding.dime.DimeCommon;
import org.jibx.ws.encoding.dime.DimeInputBuffer;

import junit.framework.TestCase;

public class DimeInTest extends TestCase
{
    /**
     * Get DIME input buffer for message.
     * 
     * @param msg string containing message data
     * @return buffer
     */
    private static DimeInputBuffer messageBuffer(String msg) {
        DimeInputBuffer dbuff = new DimeInputBuffer();
        InByteBuffer bbuff = new InByteBuffer();
        bbuff.setInput(new ByteArrayInputStream(TestData.messageBytes(msg)));
        dbuff.setBuffer(bbuff);
        return dbuff;
    }

    public void testEmptyMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.EMPTY_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertEquals("Wrong offset", DimeCommon.HEADER_SIZE, buff.getOffset());
        assertEquals("Wrong limit", DimeCommon.HEADER_SIZE, buff.getLimit());
        assertFalse("No data in message", buff.require(1));
        assertFalse("No more parts in message", buff.nextPart());
        assertFalse("No more messages", buff.nextMessage());
    }

    public void testBadVersionMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.BAD_VERSION_MESSAGE);
        try {
            assertTrue("Initial message", buff.nextMessage());
            buff.nextPart();
            fail();
        } catch (IOException e) {
        }
    }

    public void testNotFirstMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.NOT_FIRST_MESSAGE);
        try {
            assertTrue("Initial message", buff.nextMessage());
            buff.nextPart();
            fail();
        } catch (IOException e) {
        }
    }

    public void testNotLastMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.NOT_LAST_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        try {
            buff.nextPart();
            fail();
        } catch (IOException e) {
        }
    }

    public void testChunkedNoChunkMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.CHUNKED_NOCHUNK_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        try {
            buff.require(1);
            fail();
        } catch (IOException e) {
        }
    }

    public void testOptIdTypeOnlyMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.OPTIDTYPEONLY_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertEquals("Record id", "bc", buff.getPartIdentifier());
        assertEquals("Record type", DimeCommon.TYPE_MEDIA, buff.getPartTypeCode());
        assertEquals("Record type text", "def", buff.getPartTypeText());
    }

    public void testOneBlockOneDataMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.ONEBLOCKONEDATA_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertTrue("Missing data", buff.require(1));
        assertEquals("Data value", 1, buff.getBuffer()[buff.getOffset()]);
        assertFalse("No more parts in message", buff.nextPart());
        assertFalse("No more messages", buff.nextMessage());
    }

    public void testTwoBlockOneData1Message() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.TWOBLOCKONEDATA1_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertTrue("Missing data", buff.require(1));
        assertEquals("Data value", 1, buff.getBuffer()[buff.getOffset()]);
        assertFalse("No more parts in message", buff.nextPart());
        assertFalse("No more messages", buff.nextMessage());
    }

    public void testTwoBlockOneData2Message() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.TWOBLOCKONEDATA2_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertTrue("Missing data", buff.require(1));
        assertEquals("Data value", 1, buff.getBuffer()[buff.getOffset()]);
        assertFalse("No more parts in message", buff.nextPart());
        assertFalse("No more messages", buff.nextMessage());
    }

    public void testOneBlockBadMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.ONEBLOCKBAD_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertTrue("Missing data", buff.require(1));
        try {
            buff.require(2);
            fail();
        } catch (IOException e) {
        }
    }

    public void testTwoBlockTwoPartMessage() throws Exception {
        DimeInputBuffer buff = messageBuffer(TestData.TWOBLOCK_TWOPARTS_MESSAGE);
        assertTrue("Initial message", buff.nextMessage());
        assertTrue("Initial part", buff.nextPart());
        assertTrue("Missing data", buff.require(1));
        int offset = buff.getOffset();
        assertEquals("Data value", 1, buff.getBuffer()[offset++]);
        buff.setOffset(offset);
        assertTrue("Missing data", buff.require(8));
        offset = buff.getOffset();
        for (int i = 0; i < 8; i++) {
            assertEquals("Data value", i + 2, buff.getBuffer()[offset + i]);
        }
        assertTrue("Missing part", buff.nextPart());
        assertFalse("No more messages", buff.nextMessage());
    }
}
