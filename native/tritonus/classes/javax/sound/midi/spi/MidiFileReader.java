/*
 *	MidiFileReader.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999 by Matthias Pfisterer
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

package javax.sound.midi.spi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public abstract class MidiFileReader {
    public abstract MidiFileFormat getMidiFileFormat(InputStream inputStream)
            throws InvalidMidiDataException, IOException;

    public abstract MidiFileFormat getMidiFileFormat(URL url)
            throws InvalidMidiDataException, IOException;

    public abstract MidiFileFormat getMidiFileFormat(File file)
            throws InvalidMidiDataException, IOException;

    public abstract Sequence getSequence(InputStream inputStream)
            throws InvalidMidiDataException, IOException;

    public abstract Sequence getSequence(URL url)
            throws InvalidMidiDataException, IOException;

    public abstract Sequence getSequence(File file)
            throws InvalidMidiDataException, IOException;
}


/*** MidiFileReader.java ***/
