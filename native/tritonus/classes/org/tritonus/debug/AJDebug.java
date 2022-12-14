/*
 *	AJDebug.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999 - 2002 by Matthias Pfisterer
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
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

package com.jackmeng.halcyoninae.tailwind.env.env.classes.org.tritonus.debug;

/**
 * Debugging output aspect.
 */
public aspect AJDebug
        extends Utils
        {
        pointcut allExceptions():handler(Throwable+);


        // TAudioConfig, TMidiConfig, TInit

        pointcut TMidiConfigCalls():execution(*TMidiConfig.*(..));
        pointcut TInitCalls():execution(*TInit.*(..));


        // share

        // midi

        pointcut MidiSystemCalls():execution(*MidiSystem.*(..));

        pointcut Sequencer():execution(TSequencer+.new(..))||
        execution(*TSequencer+.*(..))||
        execution(*PlaybackAlsaMidiInListener.*(..))||
        execution(*RecordingAlsaMidiInListener.*(..))||
        execution(*AlsaSequencerReceiver.*(..))||
        execution(*AlsaSequencerTransmitter.*(..))||
        execution(LoaderThread.new(..))||
        execution(*LoaderThread.*(..))||
        execution(MasterSynchronizer.new(..))||
        execution(*MasterSynchronizer.*(..));

        // audio

        pointcut AudioSystemCalls():execution(*AudioSystem.*(..));

        pointcut sourceDataLine():
        call(*SourceDataLine+.*(..));

        // OLD

// 	pointcut playerStates():
// 		execution(private void TPlayer.setState(int));


        // currently not used
        pointcut printVelocity():execution(*JavaSoundToneGenerator.playTone(..))&&call(JavaSoundToneGenerator.ToneThread.new(..));

        // pointcut tracedCall(): execution(protected void JavaSoundAudioPlayer.doRealize() throws Exception);


        ///////////////////////////////////////////////////////
        //
        //	ACTIONS
        //
        ///////////////////////////////////////////////////////


        before():MidiSystemCalls()
        {
        if(TDebug.TraceMidiSystem)outEnteringJoinPoint(thisJoinPoint);
        }

        after():MidiSystemCalls()
        {
        if(TDebug.TraceSequencer)outLeavingJoinPoint(thisJoinPoint);
        }

        before():Sequencer()
        {
        if(TDebug.TraceSequencer)outEnteringJoinPoint(thisJoinPoint);
        }

        after():Sequencer()
        {
        if(TDebug.TraceSequencer)outLeavingJoinPoint(thisJoinPoint);
        }

        before():TInitCalls()
        {
        if(TDebug.TraceInit)outEnteringJoinPoint(thisJoinPoint);
        }

        after():TInitCalls()
        {
        if(TDebug.TraceInit)outLeavingJoinPoint(thisJoinPoint);
        }

        before():TMidiConfigCalls()
        {
        if(TDebug.TraceMidiConfig)outEnteringJoinPoint(thisJoinPoint);
        }

        after():TMidiConfigCalls()
        {
        if(TDebug.TraceMidiConfig)outLeavingJoinPoint(thisJoinPoint);
        }

// execution(* TAsynchronousFilteredAudioInputStream.read(..))

        before():execution(*TAsynchronousFilteredAudioInputStream.read())
        {
        if(TDebug.TraceAudioConverter)outEnteringJoinPoint(thisJoinPoint);
        }

        after():execution(*TAsynchronousFilteredAudioInputStream.read())
        {
        if(TDebug.TraceAudioConverter)outLeavingJoinPoint(thisJoinPoint);
        }

        before():execution(*TAsynchronousFilteredAudioInputStream.read(byte[]))
        {
        if(TDebug.TraceAudioConverter)outEnteringJoinPoint(thisJoinPoint);
        }

        after():execution(*TAsynchronousFilteredAudioInputStream.read(byte[]))
        {
        if(TDebug.TraceAudioConverter)outLeavingJoinPoint(thisJoinPoint);
        }

        before():execution(*TAsynchronousFilteredAudioInputStream.read(byte[],int,int))
        {
        if(TDebug.TraceAudioConverter)outEnteringJoinPoint(thisJoinPoint);
        }

        after():execution(*TAsynchronousFilteredAudioInputStream.read(byte[],int,int))
        {
        if(TDebug.TraceAudioConverter)outLeavingJoinPoint(thisJoinPoint);
        }

        after()returning(int nBytes):call(*TAsynchronousFilteredAudioInputStream.read(byte[],int,int))
        {
        if(TDebug.TraceAudioConverter)TDebug.out("returning bytes: "+nBytes);
        }


// 	before(int nState): playerStates() && args(nState)
// 		{
// // 			if (TDebug.TracePlayerStates)
// // 			{
// // 				TDebug.out("TPlayer.setState(): " + nState);
// // 			}
// 		}

// 	before(): playerStateTransitions()
// 		{
// // 			if (TDebug.TracePlayerStateTransitions)
// // 			{
// // 				TDebug.out("Entering: " + thisJoinPoint);
// // 			}
// 		}


// 	Synthesizer around(): call(* MidiSystem.getSynthesizer())
// 		{
// // 			Synthesizer	s = proceed();
// // 			if (TDebug.TraceToneGenerator)
// // 			{
// // 				TDebug.out("MidiSystem.getSynthesizer() gives:  " + s);
// // 			}
// // 			return s;
// 			// only to get no compilation errors
// 			return null;
// 		}

// TODO: v gives an error; find out what to do
// 	before(int v): printVelocity() && args(nVelocity)
// 		{
// 			if (TDebug.TraceToneGenerator)
// 			{
// 				TDebug.out("velocity: " + v);
// 			}
// 		}


        before(Throwable t):allExceptions()&&args(t)
        {
        if(TDebug.TraceAllExceptions)TDebug.out(t);
        }
        }


/*** AJDebug.java ***/
