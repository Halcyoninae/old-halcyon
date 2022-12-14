/**
 * Copyright 2019 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.opengl;

import com.jogamp.nativewindow.CapabilitiesFilter;

import java.util.ArrayList;

/**
 * Diverse reusable {@link GLCapabilitiesImmutable} list filter
 * @see {@link CapabilitiesFilter}
 */
public class GLCapabilitiesFilter extends CapabilitiesFilter {
    protected GLCapabilitiesFilter() {}

    public static class TestLessDepthBits<C extends GLCapabilitiesImmutable> implements Test<C> {
        final int minDepthBits;
        public TestLessDepthBits(final int minDepthBits) {
            this.minDepthBits = minDepthBits;
        }
        public final boolean match(final C cap) {
            return cap.getDepthBits() < minDepthBits;
        }
    }
    public static class TestMoreDepthBits<C extends GLCapabilitiesImmutable> implements Test<C> {
        final int maxDepthBits;
        public TestMoreDepthBits(final int maxDepthBits) {
            this.maxDepthBits = maxDepthBits;
        }
        public final boolean match(final C cap) {
            return cap.getDepthBits() > maxDepthBits;
        }
    }

    /**
     * Filter removing all {@link GLCapabilitiesImmutable} derived elements having depth bits < {@code minDepthBits}.
     * @param availableCaps list of {@link GLCapabilitiesImmutable} derived elements to be filtered
     * @param minDepthBits minimum tolerated depth bits
     * @return the list of removed {@link GLCapabilitiesImmutable} derived elements, might be of size 0 if none were removed.
     */
    public static <C extends GLCapabilitiesImmutable> ArrayList<C> removeLessDepthBits(final ArrayList<C> availableCaps,
                                                                      final int minDepthBits) {
        final ArrayList<Test<C>> criteria = new ArrayList<Test<C>>();
        criteria.add(new TestLessDepthBits<C>(minDepthBits));
        return CapabilitiesFilter.removeMatching(availableCaps, criteria);
    }
}
