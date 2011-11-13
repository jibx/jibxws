package org.jibx.ws.encoding.dime;

public class TestData
{

    static final String EMPTY_MESSAGE = "\u0E40\u0000\u0000\u0000\u0000\u0000";
    static final String BAD_VERSION_MESSAGE = "\u0640\u0000\u0000\u0000\u0000\u0000";
    static final String NOT_FIRST_MESSAGE = "\u0A40\u0000\u0000\u0000\u0000\u0000";
    static final String NOT_LAST_MESSAGE = "\u0C40\u0000\u0000\u0000\u0000\u0000";
    static final String CHUNKED_NOCHUNK_MESSAGE = "\u0D40\u0000\u0000\u0000\u0000\u0000";
    static final String IDTYPEONLY_MESSAGE = "\u0E10\u0000\u0002\u0003\u0000\u0000\u6263\u0000\u6465\u6600";
    static final String OPTIDTYPEONLY_MESSAGE = "\u0E10\u0001\u0002\u0003\u0000\u0000\u6100\u0000\u6263\u0000\u6465\u6600";
    static final String ONEBLOCKONEDATA_MESSAGE = "\u0E40\u0000\u0000\u0000\u0000\u0001\u0100\u0000";
    static final String TWOBLOCKONEDATA1_MESSAGE = "\u0D40\u0000\u0000\u0000\u0000\u0001\u0100\u0000" +
    "\u0A00\u0000\u0000\u0000\u0000\u0000";
    static final String TWOBLOCKONEDATA2_MESSAGE = "\u0D40\u0000\u0000\u0000\u0000\u0000" +
    "\u0A00\u0000\u0000\u0000\u0000\u0001\u0100\u0000";
    // missing padding and final block
    static final String ONEBLOCKBAD_MESSAGE = "\u0D40\u0001\u0002\u0003\u0000\u0001\u6100\u0000\u6263\u0000\u6465\u6600\u0100";
    static final String TWOBLOCK_TWOPARTS_MESSAGE = "\u0D40\u0000\u0000\u0000\u0000\u0006\u0102\u0304\u0506\u0000" +
    "\u0800\u0000\u0000\u0000\u0000\u0003\u0708\u0900" +
    "\u0910\u0000\u0002\u0003\u0000\u0003\u6263\u0000\u6465\u6600\u0102\u0300" +
    "\u0A00\u0000\u0000\u0000\u0000\u0003\u0405\u0600";
    /**
     * Get message as a byte array.
     * 
     * @param msg string containing message data
     * @return bytes
     */
    static byte[] messageBytes(String msg) {
        byte[] byts = new byte[msg.length()*2];
        for (int i = 0; i < msg.length(); i++) {
            char chr = msg.charAt(i);
            byts[i*2] = (byte)(chr >> 8);
            byts[i*2+1] = (byte)chr;
        }
        return byts;
    }
    
}
