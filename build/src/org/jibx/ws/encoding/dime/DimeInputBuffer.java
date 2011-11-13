/*
 * Copyright (c) 2007-2009, Sosnoski Software Associates Limited. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.ws.encoding.dime;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.impl.IInByteBuffer;

/**
 * Byte buffer for input using DIME encoding.DIME messages are sent as a sequence of one or more parts. Each part is
 * sent as one or more chunks. Each chunk starts with a header giving the length and blocking flags, and potentially
 * other information such as content type and message identifier.
 * 
 * @author Dennis M. Sosnoski
 */
public class DimeInputBuffer implements IInByteBuffer
{
    private static final Log s_logger = LogFactory.getLog(DimeInputBuffer.class);
    
    /** Message state. */
    private int m_messageState;
    
    /** Input buffer. */
    private IInByteBuffer m_byteBuffer;
    
    /** Cached reference to byte array used by buffer. */
    private byte[] m_buffer;
    
    /** Offset past end of current record data in buffer. */
    private int m_endOffset;
    
    /** Current offset for reading data from buffer. */
    private int m_emptyOffset;
    
    /** Number of bytes in current chunk past end of data now in buffer. */
    private int m_sizeRemaining;
    
    /** Number of bytes of padding needed from last record. */
    private int m_paddingNeeded;
    
    /** Flag for chunked data with more to come. */
    private boolean m_chunked;
    
    /** Identifier for current message part (<code>null</code> if none). */
    private String m_partIdentifier;
    
    /** Type format code for current message part. This uses the constants defined for the second byte of the header. */
    private int m_partTypeCode;
    
    /** Type text for current message part. The interpretation depends on the {@link #m_partTypeCode} value. */
    private String m_partTypeText;
    
    /**
     * Constructor.
     */
    public DimeInputBuffer() {
        m_messageState = DimeCommon.MESSAGE_END;
    }
    
    /**
     * Set the input buffer. If a different buffer was set previously that buffer is closed, with any errors ignored.
     * 
     * @param buff onput iuffer
     */
    public void setBuffer(IInByteBuffer buff) {
        if (m_byteBuffer != null && m_byteBuffer != buff) {
            try {
                m_byteBuffer.finish();
            } catch (IOException e) { /* nothing to be done */ }
        }
        m_byteBuffer = buff;
        m_buffer = buff.getBuffer();
        m_messageState = DimeCommon.MESSAGE_END;
        m_emptyOffset = m_endOffset = m_sizeRemaining = 0;
        m_chunked = false;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Set buffer to instance of " + buff.getClass().getName());
        }
    }
    
    /**
     * Read a short (two-byte) value from header.
     * 
     * @param offset first byte offset
     * @return value
     */
    private int buildShort(int offset) {
        return ((m_buffer[offset] & 0xFF) << 8) + (m_buffer[offset + 1] & 0xFF);
    }
    
    /**
     * Read record header. If data is being retained, this also copy the retained data up over the header once it has
     * been processed. 
     * 
     * @param retain number of bytes of data to be retained from current chunk
     * @return <code>true</code> if header read, <code>false</code> if no more data
     * @throws IOException on read error
     */
    private boolean readHeader(int retain) throws IOException {
        
        // read at least padding and the fixed-length portion of header
        if (m_byteBuffer.require(retain + DimeCommon.HEADER_SIZE + m_paddingNeeded)) {
            
            // discard padding from start of data
            byte[] buffer = m_buffer = m_byteBuffer.getBuffer();
            int start = m_byteBuffer.getOffset();
            int offset = start + retain + m_paddingNeeded;
            boolean debug = s_logger.isDebugEnabled();
            if (debug) {
                s_logger.debug("Read header:" + DimeCommon.dumpBytes(buffer, offset, DimeCommon.HEADER_SIZE));
            }
            
            // handle version and state change flags in first byte
            boolean first = true;
            m_chunked = false;
            byte byt = buffer[offset];
            int version = (byt & DimeCommon.VERSION_MASK);
            if (version != DimeCommon.VERSION_VALUE) {
                s_logger.error("Invalid DIME version: " + version);
                throw new IOException("Invalid DIME version");
            }
            if ((byt & DimeCommon.MESSAGE_BEGIN_FLAG) != 0) {
                if (m_messageState == DimeCommon.MESSAGE_START) {
                    m_messageState = DimeCommon.MESSAGE_MIDDLE;
                } else {
                    s_logger.error("Unexpected MB (Message Begin) DIME record");
                    throw new IOException("Unexpected MB (Message Begin) DIME record");
                }
            } else if (m_messageState == DimeCommon.MESSAGE_START) {
                s_logger.error("Missing expected MB (Message Begin) DIME record");
                throw new IOException("Missing expected MB (Message Begin) DIME record");
            }
            if ((byt & DimeCommon.CHUNK_FLAG) != 0) {
                m_chunked = true;
                if (m_messageState == DimeCommon.MESSAGE_MIDDLE) {
                    m_messageState = DimeCommon.MESSAGE_CHUNK;
                } else if (m_messageState == DimeCommon.MESSAGE_CHUNK) {
                    first = false;
                } else {
                    s_logger.error("Invalid CF (Chunk Flag) DIME record");
                    throw new IOException("Invalid CF (Chunk Flag) DIME record");
                }
            } else if (m_messageState == DimeCommon.MESSAGE_CHUNK) {
                m_messageState = DimeCommon.MESSAGE_MIDDLE;
                first = false;
            }
            if ((byt & DimeCommon.MESSAGE_END_FLAG) != 0) {
                if (m_messageState == DimeCommon.MESSAGE_MIDDLE) {
                    m_messageState = DimeCommon.MESSAGE_END;
                } else {
                    s_logger.error("Unexpected ME (Message End) DIME record");
                    throw new IOException("Unexpected ME (Message End) DIME record");
                }
            }
            
            // validate type code and fixed part of second byte
            byt = buffer[offset + 1];
            if (!first) {
                if (byt != 0) {
                    s_logger.error("DIME chunk record read with non-zero type or reserved field");
                    throw new IOException("DIME chunk record read with non-zero type or reserved field");
                }
            } else {
                m_partTypeCode = byt & DimeCommon.TYPE_FORMAT_MASK;
                if ((byt & ~DimeCommon.TYPE_FORMAT_MASK) != 0) {
                    s_logger.error("DIME record read with non-zero reserved field");
                    throw new IOException("DIME record read with non-zero reserved field");
                }
            }
            
            // get the variable-length field sizes
            int optlength = buildShort(offset + 2);
            int idlength = buildShort(offset + 4);
            int typelength = buildShort(offset + 6);
            int length = (buildShort(offset + 8) << 16) + buildShort(offset + 10);
            if (first) {
                
                // read the actual variable-length fields
                int optpadded = (optlength + 3) & -4;
                int idpadded = (idlength + 3) & -4;
                int typepadded = (typelength + 3) & -4;
                m_byteBuffer.require(length + optpadded + idpadded + typepadded);
                buffer = m_byteBuffer.getBuffer();
                start = m_byteBuffer.getOffset();
                offset = start + retain + m_paddingNeeded + DimeCommon.HEADER_SIZE + optpadded;
                if (idlength > 0) {
                    m_partIdentifier = new String(buffer, offset, idlength, "UTF-8");
                    offset += idpadded;
                } else {
                    m_partIdentifier = null;
                }
                if (typelength > 0) {
                    m_partTypeText = new String(buffer, offset, typelength, "UTF-8");
                    offset += typepadded;
                } else {
                    m_partTypeText = null;
                }
                
            } else {
                if (optlength != 0 || idlength != 0 || typelength != 0) {
                    s_logger.error("DIME chunk record read with non-zero field length(s)");
                    throw new IOException("DIME chunk record read with non-zero field length(s)");
                }
                offset += DimeCommon.HEADER_SIZE;
            }
            
            // move retained data up in buffer to overwrite header
            if (retain > 0) {
                offset -= retain;
                System.arraycopy(buffer, start, buffer, offset, retain);
                m_byteBuffer.setOffset(offset);
            }
            m_buffer = buffer;
            m_emptyOffset = offset;
            m_endOffset = m_byteBuffer.getLimit();
            
            // adjust end of data offset if read past end of current chunk
            int limit = offset + retain + length;
            if (m_endOffset > limit) {
                m_sizeRemaining = 0;
                m_endOffset = limit;
            } else {
                m_sizeRemaining = limit - m_endOffset;
            }
            m_paddingNeeded = ((length + 3) & -4) - length;
            if (debug) {
                s_logger.debug("Processed header with data size " + length + " and message state " + m_messageState
                    + ", empty at " + m_emptyOffset + " and limit " + m_endOffset + ", with " + m_sizeRemaining
                    + " remaining in block (needs " + m_paddingNeeded + " bytes padding)");
            }
            return true;
            
        } else {
            return false;
        }
    }
    
    /**
     * Read next chunk of data in a record.
     * 
     * @return <code>true</code> if read, <code>false</code> if last chunk already read
     * @throws IOException
     */
    private boolean nextChunk() throws IOException {
        if (m_chunked) {
            m_byteBuffer.setOffset(m_emptyOffset);
            if (readHeader(m_endOffset - m_emptyOffset)) {
                return true;
            } else {
                s_logger.error("Missing final DIME record chunk");
                throw new IOException("Missing final DIME record chunk");
            }
        } else {
            return false;
        }
    }
    
    /**
     * Move to next logical part in message.
     * 
     * @return <code>true</code> if part found, <code>false</code> if not
     * @throws IOException on error reading stream
     */
    public boolean nextPart() throws IOException {
        
        // first discard all chunks in current part
        s_logger.debug("Advancing to next part");
        do {
            
            // skip past any data not yet read from this chunk
            while (m_sizeRemaining > 0) {
                m_byteBuffer.setOffset(m_endOffset);
                m_byteBuffer.require(1);
                int offset = m_byteBuffer.getOffset();
                m_endOffset = m_byteBuffer.getLimit();
                int avail = m_endOffset - offset;
                if (avail >= m_sizeRemaining) {
                    m_endOffset = offset + m_sizeRemaining;
                    m_sizeRemaining = 0;
                } else {
                    m_sizeRemaining -= avail;
                }
            }
            m_emptyOffset = m_endOffset;
            
        } while (nextChunk());
        m_byteBuffer.setOffset(m_emptyOffset);
        if (m_messageState != DimeCommon.MESSAGE_END) {
            
            // read first chunk of next part
            if (readHeader(0)) {
                return true;
            } else {
                s_logger.error("End of input before end of message");
                throw new IOException("End of input before end of message");
            }
            
        } else if (s_logger.isDebugEnabled()) {
            s_logger.debug("End of message reached");
        }
        return false;
    }
    
    /**
     * Get the identifier for the current message part.
     * 
     * @return identifier
     */
    public String getPartIdentifier() {
        return m_partIdentifier;
    }
    
    /**
     * Get the type code for the type text of the current message part. This type code (with values defined in {@link
     * DimeCommon}) tells how the type text returned by {@link #getPartTypeText()} should be interpreted.
     * 
     * @return type code
     */
    public int getPartTypeCode() {
        return m_partTypeCode;
    }
    
    /**
     * Get the type text for the current message part.
     * 
     * @return text
     */
    public String getPartTypeText() {
        return m_partTypeText;
    }
    
    /**
     * Move to next message from stream.
     * 
     * @return <code>true</code> if another message found, <code>false</code> if not
     * @throws IOException on error reading stream
     */
    public boolean nextMessage() throws IOException {
        s_logger.debug("Checking for next message");
        while (nextPart()) {
            m_byteBuffer.setOffset(m_endOffset);
        }
        m_messageState = DimeCommon.MESSAGE_START;
        int size = DimeCommon.HEADER_SIZE + m_paddingNeeded;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Attempting to read required " + size + " bytes of data");
        }
        boolean ret = m_byteBuffer.require(size);
        m_buffer = null;
        m_emptyOffset = m_endOffset = m_byteBuffer.getOffset();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Returning " + ret);
        }
        return ret;
    }
    
    //
    // IInByteBuffer implementation
    
    /**
     * Get the byte array buffer.
     * 
     * @return array
     */
    public byte[] getBuffer() {
        return m_buffer;
    }
    
    /**
     * Get the index of the next byte to be read. After reading data, the {@link #setOffset(int)} method must be used to
     * update the current offset before any other operations are performed on the buffer.
     * 
     * @return offset
     */
    public int getOffset() {
        return m_emptyOffset;
    }
    
    /**
     * Set the current offset. This must be used to update the stored buffer state after reading any data.
     * 
     * @param offset offset
     */
    public void setOffset(int offset) {
        m_emptyOffset = offset;
        m_byteBuffer.setOffset(offset);
    }
    
    /**
     * Get offset past the end of data in buffer.
     * 
     * @return offset past end of data
     */
    public int getLimit() {
        return m_endOffset;
    }
    
    /**
     * Require some number of bytes of data. When this call is made the buffer can discard all data up to the current
     * offset, and will copy retained data down to the start of the buffer array and read more data from the input
     * stream if necessary to make the requested number of bytes available. This call may cause the byte array buffer to
     * be replaced, so {@link #getBuffer()}, {@link #getLimit()}, and {@link #getOffset()} must all be called again
     * before any further use of the buffer.
     * 
     * @param size desired number of bytes
     * @return <code>true</code> if request satisfied, <code>false</code> if not
     * @throws IOException on I/O error
     */
    public boolean require(int size) throws IOException {
        if (m_buffer == null) {
            throw new IllegalStateException("Internal error - not inside message part");
        }
        int avail = m_endOffset - m_emptyOffset;
        if (size > avail) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Require " + size + " with " + avail + " available and " + m_sizeRemaining
                    + " remaining in block");
            }
            
            // need to loop in case multiple blocks are required
            do {
                
                // check if another chunk needed to fill requirement
                if (m_sizeRemaining == 0) {
                    if (!nextChunk()) {
                        return false;
                    }
                    avail = m_endOffset - m_emptyOffset;
                } else {
                    
                    // adjust request size to avoid going past end of chunk
                    int request = size;
                    int limit = avail + m_sizeRemaining;
                    if (size > limit) {
                        request = limit;
                    }
                    
                    // request more data from buffer
                    m_byteBuffer.setOffset(m_emptyOffset);
                    boolean result = m_byteBuffer.require(request);
                    
                    // cache new buffer information
                    m_buffer = m_byteBuffer.getBuffer();
                    m_emptyOffset = m_byteBuffer.getOffset();
                    m_endOffset = m_byteBuffer.getLimit();
                    
                    // adjust to set data end at end of chunk
                    avail = m_endOffset - m_emptyOffset;
                    if (avail > limit) {
                        m_endOffset = m_emptyOffset + limit;
                        m_sizeRemaining = 0;
                    } else {
                        m_sizeRemaining = limit - avail;
                    }
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Requested more data with result " + result + ", empty at " + m_emptyOffset
                            + " and limit " + m_endOffset + ", with " + m_sizeRemaining + " remaining in block");
                    }
                    if (!result) {
                        return false;
                    }
                    
                }
                
            } while (size > avail);
            s_logger.debug("Got required bytes");
            return true;
            
        } else {
            return true;
        }
    }
    
    /**
     * Complete usage of the current stream. This method should be called whenever the application is done reading from
     * the buffered stream. It skips past any data remaining in the current message.
     * 
     * @throws IOException on I/O error
     */
    public void finish() throws IOException {
        while (nextPart()) { /* do nothing */ };
    }
}
