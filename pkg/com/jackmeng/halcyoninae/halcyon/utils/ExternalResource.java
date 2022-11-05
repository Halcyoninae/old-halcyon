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

import com.jackmeng.halcyoninae.halcyon.DefaultManager;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ResourceFolder is a general class that holds information about
 * the external resource folder.
 * <p>
 * The resource folder is named under the name "halcyon-mp4j".
 *
 * @author Jack Meng
 * @since 2.1
 */
public class ExternalResource {
    /**
     * Represents the global instance of the PropertiesManager for the
     * user-modifiable "halcyon.properties" file
     * <p>
     * The program has one global instance to reduce overhead.
     */
    public static final PropertiesManager pm = new PropertiesManager(ProgramResourceManager.getProgramDefaultProperties(),
        ProgramResourceManager.getAllowedProperties(),
        ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH
            + ProgramResourceManager.PROGRAM_RESOURCE_FILE_PROPERTIES);

    private ExternalResource() {
    }

    /**
     * An internal method used to retrieve a random string of letters
     * based on the parameterized length.
     *
     * @param length The length of the random string
     * @return String The random string
     */
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) (Math.random() * 26 + 'a'));
        }
        return sb.toString();
    }

    /**
     * Based on provided folders
     * it checks if these subfolders exists from within the main resource folder.
     * <p>
     * If a folder does not exist, it will be created under the main resource
     * folder.
     * <p>
     * 3.0:
     * Changed to have a parameter being taken
     *
     * @param folderName The folder to be checked
     */
    public static void checkResourceFolder(String folderName) {
        File folder = new File(folderName);
        if (!folder.isDirectory() || !folder.exists()) {
            if (folder.mkdir()) {
                Debugger.log("LOG > Resource folder created.");
            } else {
                Debugger.log("LOG > Resource folder creation failed.");
            }
        }
    }

    /**
     * This is a standard method to write a log file to the resource's log folder.
     * <p>
     * This can be used for anything, however is not a place for things that are
     * cached to be written to.
     * <p>
     * It will only write String based files to the files.
     *
     * @param folderName The name of the folder to write to
     * @param f          The file to write
     */
    public static void writeLog(String folderName, String f) {
        if (!new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH + folderName)
            .isDirectory() || !new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH + folderName).exists()) {
            new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH + folderName).mkdir();
        }
        File logFile = new File(
            ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH + folderName
                + ProgramResourceManager.FILE_SLASH
                + System.currentTimeMillis() + "_log.halcylog");
        try {
            logFile.createNewFile();
            FileWriter writer = new FileWriter(logFile);
            writer.write(f);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a buffered-image to the resource folder.
     * <p>
     * Planned:
     * - Make it so that it will write to the "cache" folder
     * of the main resource folder.
     *
     * @param bi The buffered image to write
     * @return String the absolute path of the image
     */
    public static String writeBufferedImageCacheFile(BufferedImage bi, String cacheFolder, String fileName) {
        DeImage.write(bi, ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + "/" + cacheFolder
            + "/" + fileName);
        return new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + "/" + cacheFolder
            + "/" + fileName).getAbsolutePath();

    }

    /**
     * Creates a folder in the standard external resource folder.
     *
     * @param name The name of the folder (can be a subdirectory)
     * @return File The folder that was created
     */
    public static boolean createFolder(String name) {
        File folder = new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH + name);
        if (!folder.exists() || !folder.isDirectory()) {
            return folder.mkdir();
        }
        return false;
    }

    /**
     * Creates a cache file in the standard usr folder that is
     * for this program.
     *
     * @param fileName The file to be created, just file name.
     * @param content  The contents of the file as a varargs of Objects
     * @return (true | | false) depending on if the creation process was a
     * success or not.
     */
    public static boolean cacheFile(String fileName, String[] content) {
        File f = new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH
            + ProgramResourceManager.RESOURCE_SUBFOLDERS[2] + ProgramResourceManager.FILE_SLASH + fileName);
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
            for (String str : content) {
                bw.write(str + "\n");
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            Debugger.warn(e);
            ExternalResource.dispatchLog(e);
        }
        return true;
    }

    /**
     * @param fileName
     * @return File
     */
    public static File getCacheFile(String fileName) {
        return new File(ProgramResourceManager.PROGRAM_RESOURCE_FOLDER + ProgramResourceManager.FILE_SLASH
            + ProgramResourceManager.RESOURCE_SUBFOLDERS[2] + ProgramResourceManager.FILE_SLASH + fileName);
    }

    /**
     * Creates a log file with a varargs of exceptions to be logged.
     * This is done asyncronously, however, the user may be alerted.
     *
     * @param ex The exceptions to be logged
     */
    public static synchronized void dispatchLog(Exception... ex) {
        long start = System.currentTimeMillis();
        ExecutorService dispatch = Executors.newFixedThreadPool(1);
        Future<Void> writableTask = dispatch.submit(() -> {
            StringBuilder sb = new StringBuilder();
            for (Exception e : ex) {
                sb.append(e.toString());

                Date d = new Date(System.currentTimeMillis());
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                StringBuilder sbt = new StringBuilder();
                for (StackTraceElement ste : e.getStackTrace()) {
                    sbt.append(ste.toString()).append("\n");
                }
                writeLog("log",
                    "Halcyon/MP4J - LOG EXCEPTION | PLEASE KNOW WHAT YOU ARE DOING\nException caught time: " + df.format(d)
                        + "\n"
                        + e.getClass() + "\n" + e + "\n" +
                        e.getMessage() + "\nLOCALIZED: " + e.getLocalizedMessage() + "\n==BEGIN_STACK_TRACE==\n"
                        + sbt + "\n==END_STACK_TRACE==\n"
                        + "Submit an issue by making a PR to the file BUGS at " + DefaultManager.PROJECT_GITHUB_PAGE);
            }
            return null;
        });
        while (!writableTask.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        Debugger.log("LOG > Log dispatch finished in " + (end - start) + "ms.");
    }
}
