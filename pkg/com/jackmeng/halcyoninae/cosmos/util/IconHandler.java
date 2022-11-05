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

package com.jackmeng.halcyoninae.cosmos.util;

import com.jackmeng.halcyoninae.halcyon.runtime.constant.ColorManager;
import com.jackmeng.halcyoninae.halcyon.runtime.constant.Global;
import com.jackmeng.halcyoninae.halcyon.utils.Debugger;
import com.jackmeng.locale.Localized;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jack Meng
 * @since 3.4.1
 */
@Localized
public class IconHandler {
    private final Map<String, ImageIcon> imageIcons;
    private String defaultLocale;

    /**
     * @param defaultLocale The default lookup location
     */
    public IconHandler(String defaultLocale) {
        imageIcons = new HashMap<>();
        this.defaultLocale = defaultLocale;
        load();
    }

    public void setDefaultLocale(String str) {
        this.defaultLocale = str;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void load() {
        return;
        /*
        File s = new File(defaultLocale);
        for (String x : Objects.requireNonNull(s.list((curr, str) -> new File(curr, str).isDirectory()))) {
            File xr = new File(s.getAbsolutePath() + "/" + x);
            if (Objects.requireNonNull(xr.listFiles()).length > 0) {
                for (File t : Objects.requireNonNull(xr.listFiles())) {
                    if (t.getAbsolutePath().endsWith(".png")) {
                        imageIcons.put(defaultLocale + "/" + x + "/" + t.getName(),
                                request(defaultLocale + "/" + x + "/" + t.getName()));
                    }
                }
            }
        }
        */
    }

    /**
     * Gets an ImageIcon from the resource folder.
     *
     * @param path The path to the image
     * @return ImageIcon The image icon
     */
    public ImageIcon request(String path) {
        try {
            return new ImageIcon(
                    java.util.Objects.requireNonNull(getClass().getResource(path)));
        } catch (NullPointerException e) {
            return new ImageIcon(path);
        }
    }

    @Localized(stability = true)
    public ImageIcon requestApplied(String key) {
        return ColorManager.hueTheme(imageIcons.get(key));
    }

    public ImageIcon getFromAsImageIcon(String key) {
        return request(key);
    }
}
