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

package com.jackmeng.halcyoninae.cosmos.components.waveform.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Jack Meng
 * @since 3.4.1
 */
public class BarForm extends JPanel {
  private int[] bars;
  private int barsWidth, barsGap, arcH, arcW, xOffset, yPadding;
  private Color fg, bg;

  public static record BoxWaveConf(int barsXOffset, int barsYPad, int barsArcH, int barsArcW) {
    public boolean assertNonNegative() {
      return barsYPad >= 0 && barsArcH >= 0 && barsArcW >= 0;
    }
  }

  public static record ColorConf(Color fg, Color bg) {
  }

  public static final int START_CENTER = -1;

  public BarForm(int width, int height, int barsGap, int barsWidth, BoxWaveConf bwc, ColorConf colors) {
    super();
    assert height > 0 && barsGap > 0 && barsWidth > 0
        && (bwc != null ? bwc.assertNonNegative() : bwc == null) && colors != null;
    this.barsGap = barsGap;
    this.barsWidth = barsWidth;
    this.arcH = bwc.barsArcH;
    this.arcW = bwc.barsArcW;
    this.xOffset = bwc.barsXOffset > width? bwc.barsXOffset - width: bwc.barsXOffset;
    this.yPadding = bwc.barsYPad > height ? bwc.barsYPad - height : bwc.barsYPad;
    this.bg = colors.bg;
    this.fg = colors.fg == null ? Color.BLACK : colors.fg;
    setPreferredSize(new Dimension(width, height));
    if (bg == null)
      setOpaque(false);

    bars = new int[width - (width / (barsGap + barsWidth))];
    bars = Utils.fillArr(bars, () -> Utils.rng(0, height));
    make(bars, 10L);
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        make(bars, 50L);
      }
    });
  }


  /**
   * @param g
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

    g2.setColor(fg);
    for (int i = xOffset, j = 0; i < getWidth() - xOffset && j < bars.length; j++, i += barsGap) {
      if (bars[j] <= 0) {
        bars[j] = yPadding;
      }
      g2.fillRoundRect(i, getHeight() / 2 - bars[j] / 2, barsWidth, bars[j],
          arcW, arcH);
    }
  }


  /**
   * @param bars
   * @param schedule
   */
  public void make(int[] bars, long schedule) {
    this.bars = bars;
    SwingUtilities.invokeLater(() -> repaint(schedule));
  }

  public void makeRNG() {
    this.bars = Utils.fillArr(bars, () -> Utils.rng(0, getHeight()));
    SwingUtilities.invokeLater(() -> repaint(30L));
  }


  /**
   * @return int[]
   */
  public int[] getCurrentDrawable() {
    return bars;
  }


  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    JFrame f = new JFrame();
    f.setPreferredSize(new Dimension(300, 100));
    BarForm bf = new BarForm(300, 100, 4, 3, new BoxWaveConf(10, 5, 5, 5),
        new ColorConf(Color.ORANGE, Color.BLACK));
    f.getContentPane().add(bf);
    f.pack();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.setVisible(true);
  }
}
