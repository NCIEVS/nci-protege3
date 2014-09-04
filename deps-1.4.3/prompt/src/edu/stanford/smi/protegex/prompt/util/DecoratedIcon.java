/*
=====================================================================

  DecoratedIcon.java
  
  Created by Claude Duguay
  Copyright (c) 2002
  
  Modified by Natasha Noy
  
=====================================================================
*/
package edu.stanford.smi.protegex.prompt.util;

import java.awt.*;
import javax.swing.*;

public class DecoratedIcon
  implements Icon, SwingConstants
{
  public static final int[]
    VALID_X = {LEFT, RIGHT, CENTER};
  public static final int[]
    VALID_Y = {TOP, BOTTOM, CENTER};
  
  protected Icon mainIcon, decorator;
  protected int yAlignment = BOTTOM;
  protected int xAlignment = LEFT;
  
  public DecoratedIcon(
    Icon mainIcon, Icon decorator,
    int xAlignment, int yAlignment)
  {
    if (decorator.getIconWidth() > mainIcon.getIconWidth())
    {
      throw new IllegalArgumentException(
        "decorator icon is wider than main icon");
    }
    if (decorator.getIconHeight() > mainIcon.getIconHeight())
    {
      throw new IllegalArgumentException(
        "decorator icon is higher than main icon");
    }
    if (!isLegalValue(xAlignment, VALID_X))
    {
      throw new IllegalArgumentException(
        "xAlignment must be LEFT, RIGHT or CENTER");
    }
    if (!isLegalValue(yAlignment, VALID_Y))
    {
      throw new IllegalArgumentException(
        "yAlignment must be TOP, BOTTOM or CENTER");
    }
    
    this.mainIcon = mainIcon;
    this.decorator = decorator;
    this.xAlignment = xAlignment;
    this.yAlignment = yAlignment;
  }
  
  public boolean isLegalValue(int value, int[] legal)
  {
    for (int i = 0; i < legal.length; i++)
    {
      if (value == legal[i]) return true;
    }
    return false;
  }
  
  public int getIconWidth()
  {
    return mainIcon.getIconWidth();
  }

  public int getIconHeight()
  {
    return mainIcon.getIconHeight();
  }
  
  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    mainIcon.paintIcon(c, g, x, y);
    int w = getIconWidth();
    int h = getIconHeight();
    if (xAlignment == CENTER)
    {
      x += (w - decorator.getIconWidth()) / 2;
    }
    if (xAlignment == RIGHT)
    {
      x += (w - decorator.getIconWidth());
    }
    if (yAlignment == CENTER)
    {
      y += (h - decorator.getIconHeight()) / 2;
    }
    if (yAlignment == BOTTOM)
    {
      y += (h - decorator.getIconHeight());
    }
    decorator.paintIcon(c, g, x, y);
  }
}

