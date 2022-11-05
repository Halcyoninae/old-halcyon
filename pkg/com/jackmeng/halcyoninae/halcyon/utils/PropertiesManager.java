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

package com.jackmeng.halcyoninae.halcyon.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * This class is localized meaning
 * that one should not use it for anything besides
 * this program.
 * <p>
 * This program provides a simple Replace On Demand
 * properties reader, instead of the default {@link java.util.Properties}
 * class.
 * <p>
 * A Replace On Demand means that it will not let the
 * the receiver decide what to do with the properties, but will
 * go by rules. If a value is deemed unacceptable, it will be
 * be replaced by the default value, and the original value in
 * the file will be overwritten. Another thing to note is that,
 * on demand means only checking the current value it is being called upon
 * {@link #get(String)}.
 * This class does not do a pre-check of all of the properties, unless the
 * properties file does not
 * exist or is empty, then everything will be replaced
 * {@link #createWithDefaultVals()}.
 *
 * @author Jack Meng
 * @since 2.1
 */
public final class PropertiesManager {
    private final Map<String, PropertyValidator> allowedProperties;
    private final Properties util;
    private final String location;
    private Map<String, String> map;
    private FileReader fr;

    /**
     * Creates a new PropertiesManager instance with the defined rules and
     * allowed-properties for the
     * given properties file.
     *
     * @param defaultProperties Contains a key and a value with the value being the
     *                          fallback value for
     *                          that key if the key's value in the properties is not
     *                          allowed. This is not an optional parameter to set as
     *                          null or anything that makes it not have a value.
     * @param allowedProperties Contains a key and an array of allowed properties as
     *                          rules, if the value from file's key does not match
     *                          any of the given rules, the PropertiesManager will
     *                          return the default property and alter the file. This
     *                          is an optional parameter, which can be that the
     *                          array can be empty (NOT NULL).
     * @param location          The location of the properties file
     */
    public PropertiesManager(Map<String, String> defaultProperties, Map<String, PropertyValidator> allowedProperties,
                             String location) {
        this.map = defaultProperties;
        this.allowedProperties = allowedProperties;
        this.location = location;
        util = new Properties();
    }

    /// BEGIN PRIVATE METHODS

    /**
     * Checks if the file has all the necessary properties' keys.
     * <p>
     * As previously stated, this is an Replace On Demand method, meaning that
     * this method does not care about the value of the properties, it only cares
     * if the key exists.
     * <p>
     * If a key does not exist, it will be created with the default value.
     */
    public void checkAllPropertiesExistence() {
        try {
            if (!new File(location).exists() || !new File(location).isFile()) {
                new File(location).createNewFile();
                createWithDefaultVals();
            }
            util.load(new FileReader(location));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String key : allowedProperties.keySet()) {
            if (!util.containsKey(key)) {
                util.setProperty(key, map.get(key));
            }
        }
        save();
    }

    /**
     * Creates an empty file with the location given in the constructor.
     */
    private void wipeContents() {
        File f = new File(location);
        if (f.isFile() && f.exists()) {
            try {
                FileWriter writer = new FileWriter(location);
                writer.write("");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes to the file with the default values of all the properties.
     * <p>
     * It will also first wipe the contents of the file in order to prevent
     * overwrite.
     */
    private void createWithDefaultVals() {
        wipeContents();
        try {
            fr = new FileReader(location);
            util.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            util.setProperty(entry.getKey(), entry.getValue());
        }
        save();
    }

    /// END PRIVATE METHODS
    /// BEGIN PUBLIC METHODS

    /**
     * Returns the Map of default properties provided in the constructor.
     *
     * @return The Map of default properties provided in the constructor.
     */
    public Map<String, String> getDefaultProperties() {
        return map;
    }

    /**
     * UNSAFE: Resets the default properties to something else.
     * <p>
     * It is unadvised to use this method, as it does not do a
     * pre-check of the properties of the file for all the properties after,
     * forcing the receiver to check for all the properties.
     *
     * @param map The new default properties.
     */
    public void setDefaultProperties(Map<String, String> map) {
        this.map = map;
        createWithDefaultVals();
    }

    /**
     * Returns the current referenced properties file.
     *
     * @return The current referenced properties file.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns (true || false) based ont he existence of a key in the properties
     * file.
     *
     * @param key A key to check for
     * @return (true | | false) based on the existence of a key in the properties
     * file.
     */
    public boolean contains(String key) {
        return util.containsKey(key);
    }

    /**
     * Consults if the given key and value are allowed to be paired or
     * is allowed in general based on the rules provided in the constructor.
     * <p>
     * If the allowed-properties' string array is empty, then it will always return
     * true.
     *
     * @param key   The key to check for
     * @param value The value to check for
     * @return (true | | false) based the allowance of the value upon the key
     */
    public boolean allowed(String key, String value) {
        if (allowedProperties.containsKey(key)) {
            if (allowedProperties.get(key) == null) {
                return true;
            }
            return allowedProperties.get(key).isValid(value);
        }
        return false;
    }

    /**
     * Returns the value of the key in the properties file.
     *
     * @param key The key to get the value of
     * @return The value of the key in the properties file.
     */
    public String get(String key) {
        if (!new File(location).exists() || !new File(location).isFile()) {
            try {
                new File(location).createNewFile();
            } catch (IOException ignored) {
                // IGNORED
            }
            createWithDefaultVals();
        }
        if (fr == null) {
            try {
                if (!new File(location).exists() || !new File(location).isFile()) {
                    new File(location).createNewFile();
                    createWithDefaultVals();
                }
                fr = new FileReader(location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fr = new FileReader(location);
            util.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (util.getProperty(key) == null) {
            util.setProperty(key, map.get(key));
            save();
        }
        return util.getProperty(key) == null || !allowed(key, util.getProperty(key)) ? map.get(key)
            : util.getProperty(key);
    }

    /**
     * Sets the value of the key in the properties file.
     *
     * @param key      The key to set the value of
     * @param value    The value to set the key to
     * @param comments The comments to add to the file
     */
    public void set(String key, String value, String comments) {
        try {
            util.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        util.setProperty(key, value);
        try {
            FileWriter writer = new FileWriter(location);
            util.store(writer, comments);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the properties file with comments.
     *
     * @param comments The comments to add to the top of the file.
     */
    public void save(String comments) {
        try {
            util.store(new FileWriter(location), comments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save with no comments parameter
     */
    public void save() {
        save("");
    }

    /**
     * Opens the properties file for editing and consulting.
     *
     * @return (true | | false) based on if the file was able to be opened.
     */
    public boolean open() {
        if (!new File(location).exists()) {
            File f = new File(location);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            createWithDefaultVals();
        } else {
            try {
                fr = new FileReader(location);
                util.load(fr);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Attempts to expose the underling properties
     * object that holds the access points to the main
     * properties config file.
     * @return A Properties object (value copy)
     */
    public Properties exposeProperties() {
        return util;
    }

    /// END PUBLIC METHODS
}
