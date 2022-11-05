package com.jackmeng.halcyoninae.tailwind;

import de.jarnbjo.flac.FlacStream;
import de.jarnbjo.ogg.EndOfOggStreamException;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jack Meng
 * @since 3.3
 */
public class FlacIn extends InputStream {
    private final FlacStream stream;

    public FlacIn(FlacStream s) {
        this.stream = s;
    }


    /**
     * @param buffer
     * @param offset
     * @param length
     * @return int
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        try {
            return this.stream.readPcm(buffer, offset, length);
        } catch (EndOfOggStreamException e) {
            // IGNORED
        }
        return -1;
    }


    /**
     * @param buffer
     * @return int
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer) throws IOException {
        return this.read(buffer, 0, buffer.length);
    }


    /**
     * @return int
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        return 0;
    }

    /**
     * @return AudioFormat Return this ogg stream's format
     */
    public AudioFormat getFormat() {
        return new AudioFormat(stream.getStreamInfo().getSampleRate(), stream.getStreamInfo().getBitsPerSample(), stream.getStreamInfo().getChannels(), true, true);
    }

}
