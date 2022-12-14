/*
 *	AudioFileFormat.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class AudioFileFormat {


    private final Type m_type;
    private final AudioFormat m_audioFormat;
    private final int m_nLengthInFrames;
    private final int m_nLengthInBytes;

    private Map<String, Object> m_properties;
    private Map<String, Object> m_unmodifiableProperties;


    public AudioFileFormat(Type type,
                           AudioFormat audioFormat,
                           int nLengthInFrames) {
        this(type,
                audioFormat,
                nLengthInFrames,
                null);
    }


    public AudioFileFormat(Type type,
                           AudioFormat audioFormat,
                           int nLengthInFrames,
                           Map<String, Object> properties) {
        this(type,
                AudioSystem.NOT_SPECIFIED,
                audioFormat,
                nLengthInFrames);
        initProperties(properties);
    }


    protected AudioFileFormat(Type type,
                              int nLengthInBytes,
                              AudioFormat audioFormat,
                              int nLengthInFrames) {
        m_type = type;
        m_audioFormat = audioFormat;
        m_nLengthInFrames = nLengthInFrames;
        m_nLengthInBytes = nLengthInBytes;
        initProperties(null);
    }


    private void initProperties(Map<String, Object> properties) {
		/* Here, we make a shallow copy of the map. It's unclear if this
		   is sufficient (or if a deep copy should be made).
		*/
        m_properties = new HashMap<String, Object>();
        if (properties != null) {
            m_properties.putAll(properties);
        }
        m_unmodifiableProperties = Collections.unmodifiableMap(m_properties);
    }


    public Type getType() {
        return m_type;
    }


    public int getByteLength() {
        return m_nLengthInBytes;
    }


    public AudioFormat getFormat() {
        return m_audioFormat;
    }


    public int getFrameLength() {
        return m_nLengthInFrames;
    }


    // IDEA: output "not specified" of length == AudioSystem.NOT_SPECIFIED
    public String toString() {
        return super.toString() +
                "[type=" + getType() +
                ", format=" + getFormat() +
                ", lengthInFrames=" + getByteLength() +
                ", lengthInBytes=" + getFrameLength() + "]";
    }


    public Map<String, Object> properties() {
        return m_unmodifiableProperties;
    }


    public Object getProperty(String key) {
        return m_properties.get(key);
    }


    protected void setProperty(String key, Object value) {
        m_properties.put(key, value);
    }


    public static class Type {
        // $$fb 2000-03-31: extension without dot
        public static final Type AIFC = new Type("AIFC", "aifc");
        public static final Type AIFF = new Type("AIFF", "aiff");
        public static final Type AU = new Type("AU", "au");
        public static final Type SND = new Type("SND", "snd");
        public static final Type WAVE = new Type("WAVE", "wav");


        private final String m_strName;
        private final String m_strExtension;


        public Type(String strName, String strExtension) {
            m_strName = strName;
            m_strExtension = strExtension;
        }


        public String getExtension() {
            return m_strExtension;
        }


        /*
         */
        public final boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            } else {
                Type t = (Type) obj;
                return toString().equals(t.toString()) &&
                        getExtension().equals(t.getExtension());
            }
        }


        /* TODO: we have to make sure that the strings aren't null.
           Otherwise, we get a NullPointerException here.
        */
        public final int hashCode() {
            int nHash = 11;
            nHash = 31 * nHash + toString().hashCode();
            nHash = 31 * nHash + getExtension().hashCode();
            // solution if we can't otherwise assure that the strings aren't null:
            // nHash = 31 * nHash + (null == data ? 0 : data.hashCode());
            return nHash;
        }


        public final String toString() {
            return m_strName;
        }
    }

}


/*** AudioFileFormat.java ***/
