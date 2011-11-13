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
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.impl.IOutByteBuffer;

/**
 * Byte buffer for output using DIME encoding. DIME messages are sent as a sequence of one or more parts. Each part is
 * sent as one or more chunks. Each chunk starts with a header giving the length and blocking flags, and potentially
 * other information such as content type and message identifier. In this implementation, each buffer of output data is
 * written as a separate record, which allows some simplifications; in particular, space for the record header can
 * always be set aside at the start of the buffer, with the header details filled in once the buffer has been filled and
 * is ready for output. Parts must be fully written before moving on to the next part (using {@link #flush()}), and
 * messages must be fully written (using {@link #finish()}) before moving on to the next message. Information about each
 * part must be configured (using the {@link #nextPart(String, int, String)} method) before output of data for the part
 * begins.
 * 
 * @author Dennis M. Sosnoski
 */
public class DimeOutputBuffer implements IOutByteBuffer
{
    private static final Log s_logger = LogFactory.getLog(DimeOutputBuffer.class);
    
    /** Byte buffer used for output. */
    private IOutByteBuffer m_byteBuffer;
    
    /** Cached reference to byte array used by buffer. */
    private byte[] m_buffer;
    
    /** Number of bytes in current header. This is only used for logging purposes. */
    private int m_headerSize;
    
    /** Base offset for start of buffer data (actual start of header). */
    private int m_base;
    
    /** Current offset for adding bytes to buffer. */
    private int m_offset;
    
    /** Message state. */
    private int m_messageState;
    
    /** Message ended flag. This flag is set by {@link #endMessage()} and checked by {@link #flush()}, to avoid the need
     for writing a separate header for the end of the message. */
    private boolean m_ended;
    
    /**
     * Constructor.
     */
    public DimeOutputBuffer() {
        m_messageState = DimeCommon.MESSAGE_END;
    }
    
    /**
     * Set the byte buffer. If a different buffer was set previously that buffer is closed, with any errors ignored.
     *
     * @param buff buffer
     */
    public void setBuffer(IOutByteBuffer buff) {
        if (m_byteBuffer != null && m_byteBuffer != buff) {
            try {
                m_byteBuffer.finish();
            } catch (IOException e) { /* nothing to be done */ }
        }
        m_byteBuffer = buff;
        m_buffer = buff.getBuffer();
        m_offset = m_base = buff.getOffset();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Set buffer to instance of " + buff.getClass().getName() + " with base " + m_offset);
        }
    }
    
    /**
     * Initialize output for next message. This must be called after the last message is completed, but before starting
     * the first part in the next message. It must be followed by a call to {@link #nextPart(String, int, String)}
     * before actually writing any data.
     */
    public void nextMessage() {
        if (m_messageState == DimeCommon.MESSAGE_END) {
            m_messageState = DimeCommon.MESSAGE_START;
            m_ended = false;
            s_logger.debug("Advanced to next message");
        } else {
            throw new IllegalStateException("Internal error - cannot start message until previous message finished");
        }
    }
    
    /**
     * Initialize output for next message part. This must be called after the last part is completed, but before
     * starting any output of data for the next part.
     * 
     * @param id part identifier (<code>null</code> if none)
     * @param typecode code for type of type information (values from {@link DimeCommon})
     * @param type type text (<code>null</code> if none)
     */
    public void nextPart(String id, int typecode, String type) {
        if (m_messageState == DimeCommon.MESSAGE_END) {
            throw new IllegalStateException("Internal error - cannot start part after message finished");
        } else if (m_messageState == DimeCommon.MESSAGE_CHUNK) {
            throw new IllegalStateException("Internal error - cannot start part until last record finished");
        } else {
            m_offset = initFirstHeader(id, typecode, type);
            s_logger.debug("Advanced to next record");
        }
    }
    
    /**
     * Fill a short value at a specified offset.
     * 
     * @param offset
     * @param value
     */
    private void fillShort(int offset, int value) {
        m_buffer[offset + 1] = (byte)value;
        m_buffer[offset] = (byte)(value >> 8);
    }
    
    /**
     * Fill a string value (as UTF-8 bytes) at a specified offset.
     * 
     * @param offset
     * @param text
     * @return offset past end of string value
     */
    private int fillString(int offset, String text) {
        if (text != null && text.length() > 0) {
            try {
                byte[] byts = text.getBytes("UTF-8");
                System.arraycopy(byts, 0, m_buffer, offset, byts.length);
                offset += byts.length;
            } catch (UnsupportedEncodingException e) {
                /* nothing to be done */
            }
        }
        return offset;
    }
    
    /**
     * Fill zero padding bytes to a multiple of four bytes.
     * 
     * @param offset
     * @return offset after padding added
     */
    private int fillPadding(int offset) {
        while ((offset - m_base & 3) != 0) {
            m_buffer[offset++] = 0;
        }
        return offset;
    }
    
    /**
     * Initialize the DIME record header at the start of the buffer for a new record. This sets the fill offset to the
     * rounded boundary following the end of the header. It must be called before any output is stored to the buffer.
     * 
     * @param id part identifier (<code>null</code> if none)
     * @param typecode code for type of type information (values from {@link DimeCommon})
     * @param type type text (<code>null</code> if none)
     * @return data start offset, following the header
     */
    private int initFirstHeader(String id, int typecode, String type) {
        int offset = m_base + DimeCommon.HEADER_SIZE;
        switch (m_messageState) {
            case DimeCommon.MESSAGE_START:
            case DimeCommon.MESSAGE_MIDDLE:
                
                // build the header with variable-length fields
                m_buffer[m_base + 1] = (byte)typecode;
                int fill = fillString(offset, id);
                fillShort(m_base + 4, fill - offset);
                offset = fillPadding(fill);
                fill = fillString(offset, type);
                fillShort(m_base + 6, fill - offset);
                offset = fillPadding(fill);
                break;
                
            default:
                throw new IllegalStateException("Internal error - not in valid header state");
                
        }
        m_headerSize = offset;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Initialized new record header ending at offset " + offset + " with id " + id
                + ", typecode " + typecode + ", type " + type);
        }
        return offset;
    }
    
    /**
     * Initialize the DIME record header at the start of the buffer for a non-initial record chunk. This sets the fill
     * offset to the rounded boundary following the end of the header. It must be called before any output is stored to
     * the buffer.
     * 
     * @return data start offset, following the header
     */
    private int initFollowHeader() {
        for (int i = 1; i <  DimeCommon.HEADER_SIZE; i++) {
            m_buffer[m_base + i] = 0;
        }
        m_headerSize = m_base + DimeCommon.HEADER_SIZE;
        int offset = m_base + m_headerSize;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Initialized chunk record header ending at offset " + offset);
        }
        return offset;
    }
    
    /**
     * Finish the DIME record header. This fills in the final details for the previously-initialized header.
     * 
     * @param length number of bytes in block
     * @param last final block flag
     * @param end final part in message flag (ignored unless last is <code>true</code>)
     */
    private void finishHeader(int length, boolean last, boolean end) {
        
        // configure the flags in first byte of header
        int byt = DimeCommon.VERSION_VALUE;
        if (m_messageState == DimeCommon.MESSAGE_START) {
            byt |= DimeCommon.MESSAGE_BEGIN_FLAG;
            m_messageState = DimeCommon.MESSAGE_MIDDLE;
        } else if (m_messageState == DimeCommon.MESSAGE_END) {
            throw new IllegalStateException("Internal error - cannot write after message finished");
        }
        if (last) {
            if (end) {
                byt |= DimeCommon.MESSAGE_END_FLAG;
            }
        } else {
            byt |= DimeCommon.CHUNK_FLAG;
        }
        m_buffer[m_base] = (byte)byt;
        
        // fill in the record size
        fillShort(m_base + 8, length >> 16);
        fillShort(m_base + 10, length);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Finished header:" + DimeCommon.dumpBytes(m_buffer, 0, m_headerSize));
        }
        m_headerSize = 0;
    }
    
    /**
     * Format current buffer data as a record chunk. This finishes the header for the data and updates the buffer offset
     * to reflect the data and padding, but does not actually force the data out.
     * 
     * @param last final chunk flag
     * @param end final part in message flag (ignored unless last is <code>true</code>)
     * @throws IOException
     */
    private void createChunk(boolean last, boolean end) throws IOException {
        
        // handle writing the block
        int length = m_offset - m_base;
        finishHeader(length - m_headerSize, last, end);
        int total = fillPadding(length);
        m_offset = m_base + total;
        m_byteBuffer.setOffset(m_offset);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Created chunk of length " + total);
        }
        
        // handle message state changes
        if (last) {
            if (end) {
                m_messageState = DimeCommon.MESSAGE_END;
            } else {
                m_messageState = DimeCommon.MESSAGE_MIDDLE;
            }
        } else {
            m_messageState = DimeCommon.MESSAGE_CHUNK;
        }
    }
    
    /**
     * End the current message. This sets the current data as the final block of a message. A call to {@link #flush()}
     * is needed following this call to actually write the data and flush the output.
     * 
     * @throws IOException on I/O error
     */
    public void endMessage() throws IOException {
        s_logger.debug("Ending message");
        m_ended = true;
    }
    
    //
    // IOutByteBuffer implementation
    
    /**
     * Get the byte array buffer.
     * 
     * @return array
     */
    public byte[] getBuffer() {
        return m_buffer;
    }
    
    /**
     * Get the index of the next byte to be written. After writing data, the {@link #setOffset(int)} method must be used
     * to update the current offset before any other operations are performed on the buffer.
     * 
     * @return offset
     */
    public int getOffset() {
        return m_offset;
    }
    
    /**
     * Set the current offset. This must be used to update the stored buffer state after reading any data.
     * 
     * @param offset offset
     */
    public void setOffset(int offset) {
        m_offset = offset;
    }
    
    /**
     * Free at least some number of bytes of space in the byte array.
     * 
     * @param reserve offset of data to be preserved in buffer (nothing preserved if greater than or equal to current
     * offset)
     * @param size desired number of bytes
     * @throws IOException on I/O error
     */
    public void free(int reserve, int size) throws IOException {
        if (m_buffer.length - m_offset < size) {
            int adjsize = (size + DimeCommon.HEADER_SIZE + 3) & -4;
            if (reserve >= m_offset) {
                
                // no reserve, just write the existing data as a record and verify space available
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Creating chunk of size " + size);
                }
                createChunk(false, false);
                m_byteBuffer.free(m_offset, adjsize);
                m_buffer = m_byteBuffer.getBuffer();
                m_base = m_byteBuffer.getOffset();
                m_offset = initFollowHeader();
                
            } else if (reserve > DimeCommon.DEFAULT_BUFFER_SIZE / 4) {
                
                // data to be preserved, but enough going to be worth writing as separate block
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Copying to free " + size + " with offset " + m_offset + " and reserve " + reserve);
                }
                int keep = m_offset - reserve;
                byte[] byts = new byte[keep];
                System.arraycopy(m_buffer, reserve, byts, 0, keep);
                m_offset = reserve;
                createChunk(false, false);
                m_byteBuffer.free(m_offset, adjsize);
                m_buffer = m_byteBuffer.getBuffer();
                m_base = m_byteBuffer.getOffset();
                m_offset = initFollowHeader();
                System.arraycopy(byts, 0, m_buffer, m_offset, keep);
                m_offset += keep;
                
            } else {
                
                // data to be preserved, but not enough to write - just force resize
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Resizing to free " + size + " with offset " + m_offset + " and reserve " + reserve);
                }
                m_byteBuffer.setOffset(m_offset);
                m_byteBuffer.free(0, adjsize);
                m_buffer = m_byteBuffer.getBuffer();
                
            }
        }
    }
    
    /**
     * Empty the buffer. Writes all data from the buffer as the final chunk of a record.
     * 
     * @throws IOException on I/O error
     */
    public void flush() throws IOException {
        s_logger.debug("Flushing output");
        if (m_messageState != DimeCommon.MESSAGE_END) {
            if (m_headerSize == 0) {
                nextPart(null, DimeCommon.TYPE_NONE, null);
            }
            createChunk(true, m_ended);
        }
        m_byteBuffer.flush();
    }
    
    /**
     * Complete usage of the current buffer. This method should be called whenever the application is done writing to
     * the buffer. It writes the current data as the final block of a message.
     * 
     * @throws IOException on I/O error
     */
    public void finish() throws IOException {
        endMessage();
        flush();
        m_byteBuffer.finish();
    }
}
