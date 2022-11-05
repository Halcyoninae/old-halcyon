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

package com.jackmeng.halcyoninae.cloudspin;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * This class is used for general graphical manipulation.
 * <p>
 * For example taking an image and blurring it.
 * <p>
 * This package is intended to replace the overburdened
 * {@link com.jackmeng.halcyoninae.halcyon.utils.DeImage}
 * which has been here since 2.0 and features the old burden class style of
 * having a util
 * class that handles a lot of functions.
 *
 * @author Jack Meng
 * @since 3.2
 */
public final class CloudSpin {
    private CloudSpin() {
    }

    /**
     * Macro Methods
     *
     * @param img A bufferedImage
     * @return An int array representing the individual pixels of the image.
     */
    public static int[] forPixels(BufferedImage img) {
        return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    }

    /**
     * This method will apply a toner on the original image.
     *
     * @param img  The original image.
     * @param tone The tone of the color to shift to.
     * @deprecated Not used and defunct AF
     */
    @Deprecated(forRemoval = true)
    public static void colorTone(BufferedImage img, Color tone) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                img.setRGB(x, y, tone.getRGB());
            }
        }
    }

    /**
     * @param img
     * @param mask
     * @param method
     * @return BufferedImage
     */
    public static BufferedImage applyMask(BufferedImage img, BufferedImage mask, int method) {
        BufferedImage maskedImage = null;
        if (img != null) {
            int width = mask.getWidth();
            int height = mask.getHeight();
            maskedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = maskedImage.createGraphics();
            int x = (width - img.getWidth()) / 2;
            int y = (height - img.getHeight()) / 2;
            mg.drawImage(img, x, y, null);
            mg.setComposite(AlphaComposite.getInstance(method));
            mg.drawImage(mask, 0, 0, null);
            mg.dispose();
        }
        return maskedImage;
    }

    /**
     * @param src
     * @param startOpacity
     * @param endOpacity
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return BufferedImage
     */
    public static BufferedImage createGradient(BufferedImage src, int startOpacity, int endOpacity, int startX,
            int startY, int endX, int endY) {
        BufferedImage alphamask = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = alphamask.createGraphics();
        LinearGradientPaint lgp = new LinearGradientPaint(
                new Point(startX, startY),
                new Point(endX, endY),
                new float[] { 0.0f, 1.0f },
                new Color[] { new Color(0, 0, 0, startOpacity), new Color(0, 0, 0, endOpacity) });
        g2d.setPaint(lgp);
        g2d.fillRect(0, 0, alphamask.getWidth(), alphamask.getHeight());
        g2d.dispose();
        return applyMask(src, alphamask, AlphaComposite.DST_IN);
    }

    public static int[] pixelRGBA(int pixel) {
        return new int[] { ((pixel >> 16) & 0xFF), ((pixel >> 8) & 0xFF), (pixel & 0xFF), ((pixel >> 24) & 0xFF) };
    }

    /**
     * A lite util method to convert an Icon to an Image
     * preferable for use as a BufferedImage.
     *
     * @param icon An Icon to convert.
     * @return An Image after the conversion.
     */
    public static Image iconToImage(final Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            final int w = icon.getIconWidth();
            final int h = icon.getIconHeight();
            final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }

    /**
     * @param target
     * @param w
     * @return BufferedImage
     */
    public static BufferedImage grabCrop(BufferedImage target, Rectangle w, int... points) {
        int width, height;
        if (w.getWidth() > target.getWidth() && w.getHeight() > target.getHeight()) {
            return target;
        } else {
            // since the decimals are just stripped it will be fine.
            width = (int) w.getWidth();
            height = (int) w.getHeight();
        }
        BufferedImage cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cropped.createGraphics();
        // crop the image from (x1, y1) to (x2, y2)
        if (points != null && points.length > 0) {
            g.drawImage(target, points[0], points[1], width, height, (int) w.getX(), (int) w.getY(),
                    (int) (w.getX() + w.getWidth()),
                    (int) (w.getY() + w.getHeight()), null);
            g.dispose();
        } else {
            g.drawImage(target, 0, 0, width, height, (int) w.getX(), (int) w.getY(),
                    (int) (w.getX() + w.getWidth()),
                    (int) (w.getY() + w.getHeight()), null);
            g.dispose();
        }
        return cropped;
    }

    /**
     * @param target
     * @param w
     */
    public static void grabCropSafe(BufferedImage target, Rectangle w) {
        int width, height;
        if (w.getWidth() > target.getWidth() && w.getHeight() > target.getHeight()) {
            return;
        } else {
            width = (int) w.getWidth();
            height = (int) w.getHeight();
        }
        Graphics2D g = target.createGraphics();
        g.drawImage(target, 0, 0, width, height, (int) w.getX(), (int) w.getY(), (int) (w.getX() + w.getWidth()),
                (int) (w.getY() + w.getHeight()), null);
        g.dispose();
    }

    /**
     * Applies a specific hue upon the image.
     * This method tries to preserve the pixel's original Alpha, however
     * depending on the given hue, this is not guranteed.
     * <p>
     * This method will not return anything, use the same object as you passed in
     * by reference.
     * <p>
     * This method will perform bound checks upon the hue and will only take
     * the first 3 elements
     *
     * @param image A desired image to alter (java.awt.image.BufferedImage)
     * @param color A Color to shift the pixels to (int[]). Where hue[0] is RED,
     *              hue[1] is GREEN, and hue[2] is BLUE
     * @author Jack Meng
     * @since 3.3
     */
    public static void hueImage(BufferedImage image, int[] color) {
        image.setAccelerationPriority(1);
        WritableRaster raster = image.getRaster();
        for (int i = 0; i < raster.getWidth(); i++) {
            for (int j = 0; j < raster.getHeight(); j++) {
                for (int d = 0; d < 3; d++) {
                    raster.setSample(i, j, d, color[d]);
                }
            }
        }
    }

    /**
     * @param img
     * @param color
     * @return BufferedImage
     */
    public static BufferedImage hueImageUnsafe(BufferedImage img, int[] color) {
        hueImage(img, color);
        return img;
    }

    /**
     * @return BufferedImage
     */
    public static BufferedImage createUnknownIMG() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 16, 16);
        g2.fillRect(16, 16, 16, 16);
        g2.setColor(Color.MAGENTA);
        g2.fillRect(0, 16, 16, 16);
        g2.fillRect(16, 0, 16, 16);
        g2.dispose();
        return img;
    }

    /**
     * @param viewport
     * @param src
     * @return
     */
    public static BufferedImage resizeToFitViewport(Dimension viewport, BufferedImage src) {
        double widthRatio = (double) viewport.width / (double) src.getWidth();
        double heightRatio = (double) viewport.height / (double) src.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);
        int width = (int) (src.getWidth() * ratio);
        int height = (int) (src.getHeight() * ratio);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

}
