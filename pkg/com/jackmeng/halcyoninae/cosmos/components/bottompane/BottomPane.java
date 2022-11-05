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

package com.jackmeng.halcyoninae.cosmos.components.bottompane;

import com.jackmeng.halcyoninae.cosmos.components.TabButton;
import com.jackmeng.halcyoninae.cosmos.components.bottompane.filelist.FileList;
import com.jackmeng.halcyoninae.halcyon.runtime.Program;
import com.jackmeng.halcyoninae.halcyon.utils.ColorTool;
import com.jackmeng.halcyoninae.halcyon.utils.Debugger;
import com.jackmeng.locale.PhysicalFolder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the Tabs in the BottomPane.
 *
 * @author Jack Meng
 * @since 3.0
 */
public class BottomPane extends JTabbedPane {
    private final List<FileList> tabs;

    /**
     * Holds a list of folder paths in correspondence with their index
     * in the JTabbedPane.
     */
    private final Map<String, Integer> tabsMap;

    private final TitledBorder t = BorderFactory.createTitledBorder("Playlists");

    /**
     * Creates a bottom viewport
     */
    public BottomPane() {
        super();
        tabsMap = new HashMap<>();
        t.setBorder(BorderFactory.createLineBorder(ColorTool.hexToRGBA("#323842")));
        setPreferredSize(new Dimension(FileList.FILEVIEW_MAX_WIDTH, FileList.FILEVIEW_MIN_HEIGHT));
        setMinimumSize(new Dimension(FileList.FILEVIEW_MIN_WIDTH, FileList.FILEVIEW_MIN_HEIGHT));
        this.tabs = new ArrayList<>(20);
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        setBorder(t);
        addChangeListener(x -> {
            if (getTabComponentAt(getSelectedIndex()) != null) {
                t.setTitle((getSelectedIndex() + 1) + " | " + (getTabComponentAt(getSelectedIndex()).getName() == null ? " "
                    : new File(getTabComponentAt(
                    getSelectedIndex()).getName()).getName()) + " | " + getTabComponentAt(
                    getSelectedIndex()).getName() + "");
            } else {
                t.setTitle("Playlist");
            }
        });
    }

    /**
     * @return list that represents the
     * FileList tabs.
     */
    public List<FileList> getTabs() {
        return tabs;
    }

    /**
     * Goes through all of the avaliable
     * FileList components and gets which ever
     * has the same instance of a JTree
     *
     * @param tree The JTree to compare against
     * @return The FileList component holding the JTree component
     */
    public FileList findByTree(JTree tree) {
        for (FileList tab : tabs) {
            if (tab.getTree().equals(tree)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * @param folderAbsoluteStr A folder's absolute path
     * @return (true | | false) if said folder is within the list.
     */
    public boolean containsFolder(String folderAbsoluteStr) {
        return Program.cacher.getSavedPlaylists().contains(folderAbsoluteStr);
    }

    /**
     * @return A List of String representsin the tabs with the different names.
     */
    public List<String> getStrTabs() {
        return Program.cacher.getSavedPlaylists();
    }

    /**
     * Pokes a direct File List object
     * <p>
     * This should only be used to add a static
     * viewport, which means this tab cannot be deleted
     * by the end-user
     *
     * @param plain A File List Component
     */
    public void pokeNewFileListTab(FileList plain) {
        add(plain.getFolderInfo().getName(), plain);
        tabs.add(plain);
    }

    /**
     * Adds a new File List object to the viewport of tabs.
     *
     * @param folder An absolute path to a folder.
     */
    public void pokeNewFileListTab(String folder) {
        FileList list = new FileList(new PhysicalFolder(folder));
        Program.cacher.getSavedPlaylists().add(new File(folder).getAbsolutePath());
        add(new File(folder).getName(), list);
        TabButton button = new TabButton(this);
        button.setName(folder);
        setTabComponentAt(getTabCount() - 1, button);
        setToolTipTextAt(getTabCount() - 1, folder);
        tabsMap.put(folder, getTabCount() - 1);
        button.setListener(() -> {
            Debugger.warn("Removing tab > " + folder);
            int i = tabsMap.get(folder);
            Program.cacher.getSavedPlaylists().remove(folder);
            tabsMap.remove(folder);
            tabs.remove(i);
            Program.cacher.pingSavedPlaylists();
        });
        Program.cacher.pingSavedPlaylists();
        this.revalidate();
        tabs.add(list);
    }

    /**
     * Runs a master revalidation of all of the
     * FileLists and checks if every added folder exists
     * and all of it's sub-files.
     *
     * @see com.jackmeng.halcyoninae.cosmos.components.bottompane.filelist.FileList#revalidateFiles()
     */
    public synchronized void mastRevalidate() {
        List<Integer> needToRemove = new ArrayList<>(tabs.size());
        int i = 0;
        for (FileList l : tabs) {
            if (!l.isVirtual) {
                if (!new File(l.getFolderInfo().getAbsolutePath()).exists()
                    || !new File(l.getFolderInfo().getAbsolutePath()).isDirectory()) {
                    removeTabAt(i);
                    needToRemove.add(i);
                    Program.cacher.getSavedPlaylists().remove(l.getFolderInfo().getAbsolutePath());
                    tabsMap.remove(l.getFolderInfo().getAbsolutePath());
                } else {
                    l.revalidateFiles();
                }
            }
        }
        for (int ix : needToRemove) {
            tabs.remove(ix);
        }
    }
}