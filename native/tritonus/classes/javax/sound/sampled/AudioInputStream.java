/*
 *	AudioInputStream.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
 *
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

package javax.sound.sampled;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.AudioUtils;

import java.io.IOException;
import java.io.InputStream;


public class AudioInputStream
        extends InputStream {
    /**
     * Says whether we check lengths.
     * If true, lengths in bytes are asserted to be multiples
     * of the frame size.
     */
    private static final boolean CHECK_LENGTHS = true;


    /**
     * The stream that is backing the AudioInputStream.
     * Accessing this property, especially writing, is discouraged.
     */
    protected InputStream stream;


    /**
     * The format of this stream.
     * Accessing this property, especially writing, is discouraged.
     * You should use the method getFormat().
     */
    protected AudioFormat format;


    /**
     * The length of the audio data.
     * <p>
     * This value is in frames.  May have the value
     * AudioSystem.NOT_SPECIFIED to express that the length in frames
     * is unknown.  Accessing this property, especially writing, is
     * strongly discouraged.  You should use the method
     * getFrameLength().
     */
    protected long frameLength;


    /**
     * The size of an frame of audio data.
     * <p>
     * This value is in bytes.  May have the value
     * AudioSystem.NOT_SPECIFIED to express that the size of frames
     * is unknown.  Accessing this property, especially writing, is
     * strongly discouraged. Its semantics is not clearly defined.
     */
    protected int frameSize;
    /**
     * The read position in the stream.
     * <p>
     * This value is in frames.  NOTE: this property only has a
     * useful value if the encoding of the stream is PCM.  Accessing
     * this property, especially writing, is strongly
     * discouraged. Its semantics is not clearly defined.
     */
    protected long framePos;
    /**
     * The size of an frame of audio data.
     * <p>
     * This value is used internally to check whether the number of
     * bytes requested in read() or skip() is a multiple of the frame
     * size.  This value is in bytes.  In contrary to frameSize,
     * which may be AudioSystem.NOT_SPECIFIED, this variable always
     * has a value >= 1. If the length in frames or the frame size of
     * the audio data is unknown, this value is set to 1.
     */
    private int m_nCheckFrameSize;
    /**
     * The read position in the stream.
     * <p>
     * This value is in bytes.
     */
    private long m_lPosition;


    /**
     * The length of the audio data.
     * <p>
     * This value is in bytes.  May have the value
     * AudioSystem.NOT_SPECIFIED to express that the length is
     * unknown.
     */
    private final long m_lLengthInBytes;


    /**
     * Position where the mark was set in the stream.
     * <p>
     * This value is in bytes.
     */
    private long m_lMarkedPosition;


    public AudioInputStream(InputStream inputStream,
                            AudioFormat audioFormat,
                            long lLengthInFrames) {
        if (TDebug.TraceAudioInputStream) {
            TDebug.out("AudioInputStream.<init>: inputStream: " + inputStream);
        }
        stream = inputStream;
        format = audioFormat;
        frameLength = lLengthInFrames;
        m_nCheckFrameSize = frameSize = audioFormat.getFrameSize();
        if (m_nCheckFrameSize < 1) {
            m_nCheckFrameSize = 1;
        }
        m_lLengthInBytes = AudioUtils.getLengthInBytes(
                audioFormat,
                lLengthInFrames);
        m_lPosition = 0;
        updateFramePosition();
    }


    public AudioInputStream(TargetDataLine targetDataLine) {
        this(new TargetDataLineInputStream(targetDataLine),
                targetDataLine.getFormat(),
                AudioSystem.NOT_SPECIFIED);
    }


    public AudioFormat getFormat() {
        return format;
    }


    public long getFrameLength() {
        return frameLength;
    }


    public int read()
            throws IOException {
        if (CHECK_LENGTHS) {
            if (getCheckFrameSize() != 1) {
                throw new IOException("frame size must be 1 to read a single byte");
            }
        }
        if (isEndReached()) {
            return -1;
        }
        int nByte = stream.read();
        if (nByte != -1) {
            m_lPosition++;
            updateFramePosition();
        }
        return nByte;
    }


    public int read(byte[] abData)
            throws IOException {
        return read(abData, 0, abData.length);
    }


    public int read(byte[] abData, int nOffset, int nLength)
            throws IOException {
        //$$fb better to first check if end is reached.
        //otherwise, on un-aligned nLength, it will lead to a misleading exception
        // (as it occurs with DataInputStream.readFully)
        if (isEndReached()) {
            return -1;
        }
        if (CHECK_LENGTHS) {
            if (nLength % getCheckFrameSize() != 0) {
                throw new IOException("length must be a multiple of the frame size (length=" + nLength + ", frameSize=" + getCheckFrameSize() + ")");
            }
        }
        if (m_lLengthInBytes != AudioSystem.NOT_SPECIFIED) {
            nLength = (int) Math.min(nLength, m_lLengthInBytes - m_lPosition);
        }
        //$$fb try to read frame-aligned (when stream returns unaligned data)
        //$$fb e.g. SequenceInputStream
        int nBytesRead = 0;
        int thisRead;
        do {
            thisRead = stream.read(abData, nOffset, nLength);
            if (thisRead > 0) {
                nBytesRead += thisRead;
                nLength -= thisRead;
                nOffset += thisRead;
            }
        }
        while (thisRead > 0 && nLength > 0);
        if (nBytesRead <= 0 && thisRead == -1) {
            nBytesRead = -1;
        } else {
            m_lPosition += nBytesRead;
            updateFramePosition();
        }
        return nBytesRead;
    }


    public long skip(long nSkip)
            throws IOException {
        if (CHECK_LENGTHS) {
            if (nSkip % getCheckFrameSize() != 0) {
                throw new IOException("skip must be a multiple of the frame size");
            }
        }
        if (m_lLengthInBytes != AudioSystem.NOT_SPECIFIED) {
            nSkip = (int) Math.min(nSkip, m_lLengthInBytes - m_lPosition);
        }
        long nSkipped = stream.skip(nSkip);
        m_lPosition += nSkipped;
        updateFramePosition();
        return nSkipped;
    }


    public int available()
            throws IOException {
        if (m_lLengthInBytes == AudioSystem.NOT_SPECIFIED) {
            return stream.available();
        } else {
            return (int) Math.min(stream.available(), m_lLengthInBytes - m_lPosition);
        }
    }


    public void close()
            throws IOException {
        stream.close();
    }


    public void mark(int readlimit) {
        stream.mark(readlimit);
        m_lMarkedPosition = m_lPosition;
    }


    public void reset()
            throws IOException {
        stream.reset();
        m_lPosition = m_lMarkedPosition;
        updateFramePosition();
    }


    public boolean markSupported() {
        return stream.markSupported();
    }


    /**
     * Returns the frame size for internal checks.
     * <p>
     * This number is is bytes.
     */
    private int getCheckFrameSize() {
        return m_nCheckFrameSize;
    }


    private boolean isEndReached() {
        return m_lPosition >= m_lLengthInBytes && m_lLengthInBytes != AudioSystem.NOT_SPECIFIED;
    }


    /**
     * Calculates the position in frames from the position in bytes.
     * <p>
     * This only produces meaningful values if the encoding is PCM.
     * Maintaining framePos is only done for backwards compatibility
     * with really badly designed programs that directely access the
     * protected member framePos.
     */
    private void updateFramePosition() {
        this.framePos = m_lPosition / getCheckFrameSize();
    }


    /**
     * Helper class to enable AudioInputStream to read from a TargetDataLine.
     */
    private static class TargetDataLineInputStream
            extends InputStream {
        /**
         * The TargetDataLine from which to read.
         */
        private final TargetDataLine m_targetDataLine;


        /**
         * A buffer used for reading single bytes.
         * <p>
         * It is allocated and used in read().
         */
        private byte[] m_abSingleByteBuffer = null;


        public TargetDataLineInputStream(TargetDataLine targetDataLine) {
            m_targetDataLine = targetDataLine;
        }


        public int read() {
            if (m_abSingleByteBuffer == null) {
                m_abSingleByteBuffer = new byte[1];
            }
            int nReturn = read(m_abSingleByteBuffer, 0, 1);
            if (nReturn < 0) {
                return -1;
            } else {
                return m_abSingleByteBuffer[0];
            }
        }


        public int read(byte[] abData, int nOffset, int nLength) {
            return m_targetDataLine.read(abData, nOffset, nLength);
        }
    }


}


/*** AudioInputStream.java ***/
