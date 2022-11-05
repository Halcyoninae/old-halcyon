/*
 * Halcyon ~ exoad
 *
 * A simplistic & robust audio library that
 * is created OPENLY and distributed in hopes
 * that it will be benefitial.
 * ============================================
 * Copyright (C) 2021 Jack Meng
 * ============================================
 * The VENDOR_LICENSE is defined as:
 * "Standard Halcyoninae Protective 1.0 (MODIFIED) License or
 * later"
 *
 * Subsiding Wrappers are defined as:
 * "GUI Wrappers, Audio API Wrappers provided within the base
 * build of the original software"
 * ============================================
 * The Halcyon Audio API & subsiding wrappers
 * are licensed under the provided VENDOR_LICENSE
 * You are permitted to redistribute and/or modify this
 * piece of software in the source or binary form under
 * the VENDOR_LICENSE. You are permitted to link this
 * software statically or dynamically under the descriptions
 * of VENDOR_LICENSE without classpath exception.
 *
 * THE SOFTWARE AND ALL SUBSETS ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * ============================================
 * If you did not receive a copy of the VENDOR_LICENSE,
 * consult the following link:
 * https://raw.githubusercontent.com/Halcyoninae/Halcyon/live/LICENSE.txt
 * ============================================
 */

package com.jackmeng.locale;

/**
 * This class dedicates a Map style data structure,
 * however it is not a list-like data structure.
 *
 * @author Jack Meng
 * @since 3.0
 */
public class Pair<T, E> {
    public T first;
    public E second;

    /**
     * Constructs the Pair object
     *
     * @param first  First element of T type (generic)
     * @param second Second element of E type (generic)
     */
    public Pair(T first, E second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Retrieves the first element
     *
     * @return The first element returned as the original T type (generic)
     */
    public T getFirst() {
        return first;
    }

    /**
     * Retrieves the second element
     *
     * @return The second element returned as the original E type (generic)
     */
    public E getSecond() {
        return second;
    }


    /**
     * @return int
     */
    public int getHashCode() {
        int hash = 17;
        hash = 31 * hash + first.hashCode();
        hash = 31 * hash + second.hashCode();
        return hash;
    }
}