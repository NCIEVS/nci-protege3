package com.clarkparsia.protege.explanation;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;

/**
 * Title: <br>
* Description: <br>
* Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
* Created: Jul 24, 2008 10:30:54 AM
*
* @author Michael Grove <mike@clarkparsia.com>
*/
public class ThrobberIcon extends JPanel {
    private static final Color DEFAULT_BASE = new Color(200, 200, 200);
    private static final Color DEFAULT_HIGHLIGHT = Color.black;
    private static final int DEFAULT_NUM_SPOKES = 8;

    private float mSpokeWidth = 3;
    private float mSpokeLength = 8;

    private float mCenterSize;
    private int mNumSpokes;

    private Color mBaseColor;
    private Color mHighlightColor;

    private int mCurrentFrame;
    private boolean mIsRunning = false;

    private boolean mIsSkewed = false;

    private int mType;

    private static final int TYPE_RECTANGLE = 0;
    private static final int TYPE_CIRCLE = 1;

    private ThrobberIcon(int theType, int theNum, float theCenterSize, Color theBase, Color theHighlight) {
        super();

        mType = theType;
        mCenterSize = theCenterSize;
        mBaseColor = theBase;
        mHighlightColor = theHighlight;
        mNumSpokes = theNum;

        mSpokeWidth = 3;
        mSpokeLength = 8;
    }

    public static ThrobberIcon rectangleThrobber(float theCenterSize, float theWidth, float theLength) {
        return rectangleThrobber(DEFAULT_NUM_SPOKES, theCenterSize, theWidth, theLength, false, DEFAULT_BASE, DEFAULT_HIGHLIGHT);
    }

    public static ThrobberIcon rectangleThrobber(float theCenterSize, float theWidth, float theLength, boolean theSkew) {
        return rectangleThrobber(DEFAULT_NUM_SPOKES, theCenterSize, theWidth, theLength, theSkew, DEFAULT_BASE, DEFAULT_HIGHLIGHT);
    }

    public static ThrobberIcon rectangleThrobber(int theNumSpokes, float theCenterSize, float theWidth, float theLength, boolean theSkew,
                                                 Color theBase, Color theHighlight) {

        ThrobberIcon aThrobber = new ThrobberIcon(TYPE_RECTANGLE, theNumSpokes, theCenterSize, theBase, theHighlight);

        aThrobber.setSpokeWidth(theWidth);
        aThrobber.setSpokeLength(theLength);
        aThrobber.mIsSkewed = theSkew;

        return aThrobber;
    }

    public static ThrobberIcon circleThrobber(float theCenterSize, float theWidth) {
        return circleThrobber(DEFAULT_NUM_SPOKES, theCenterSize, theWidth, DEFAULT_BASE, DEFAULT_HIGHLIGHT);
    }

    public static ThrobberIcon circleThrobber(int theNumCircles, float theCenterSize, float theWidth) {
        return circleThrobber(theNumCircles, theCenterSize, theWidth, DEFAULT_BASE, DEFAULT_HIGHLIGHT);
    }

    public static ThrobberIcon circleThrobber(int theNumCircles, float theCenterSize, float theWidth, Color theBase, Color theHighlight) {
        ThrobberIcon aThrobber = new ThrobberIcon(TYPE_CIRCLE, theNumCircles, theCenterSize, theBase, theHighlight);

        aThrobber.setSpokeWidth(theWidth);

        return aThrobber;
    }

    public void startAnimationRunning() {
        if (!mIsRunning) {
            mIsRunning = true;
            new AnimationWorker().start();
            paintImmediately(0, 0,getWidth(), getHeight());
        }
    }

    public void stopAnimationRunning() {
        if (mIsRunning) {
            mIsRunning = false;
            paintImmediately(0, 0,getWidth(), getHeight());
        }
    }

    public boolean isAnimationRunning() {
        return mIsRunning;
    }

    public void removeNotify() {
        super.removeNotify();

        // this component won't be in its parent container anymore, ie its' not on screen, so stop the animation thread
        stopAnimationRunning();
    }

    public void addNotify() {
        super.addNotify();

		if (mIsRunning) {
        	// we only want to kick off the animation thread if this is going to be in a component and displayed
        	startAnimationRunning();
		}
    }

    public void paintComponent(Graphics theGraphics) {
        Graphics2D g = (Graphics2D) theGraphics;

        // clear the background, if necessary
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fill(new Rectangle(0, 0, getWidth(), getHeight()));
        }

        // set rendering hints so it looks a little cleaner
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // move to the middle of the canvas
        g.translate(getWidth()/2,getHeight()/2);

        for (int aIndex = 0; aIndex < getNumSpokes(); aIndex++) {
            if (isAnimationRunning()) {
                g.setColor(getColorForSpoke(aIndex));
            }
            else {
                g.setColor(mBaseColor);
            }

            g.fill(getSpokeShape());

            // rotations are in radians, 2pi radians in a circle, so we'll rotate in subunits of that, dividing 2pi
            // by the number of spokes (i.e. rotations) we will have
            g.rotate(Math.PI*2.0/(double) getNumSpokes());
        }
    }

    private Shape getSpokeShape() {
        Shape aSpokeShape = null;

        switch (mType) {
            case TYPE_RECTANGLE:
                aSpokeShape = new RoundRectangle2D.Float(getCenterSize(), getSpokeWidth()/2 * (mIsSkewed ? 1 : -1),
                                                         getSpokeLength(), getSpokeWidth(),
                                                         getSpokeWidth(), getSpokeWidth());
                break;
            case TYPE_CIRCLE:
                aSpokeShape = new Ellipse2D.Float(getCenterSize(), getSpokeWidth()/2, getSpokeWidth(), getSpokeWidth());
                break;
        }

        return aSpokeShape;
    }

    private Color getColorForSpoke(int theFrame) {
        int aTrail = 4;

        for(int aTrailIndex = 0; aTrailIndex < aTrail; aTrailIndex++) {
            int aTrailStep = (mCurrentFrame + (getNumSpokes() - aTrailIndex)) % getNumSpokes();
            if (theFrame == aTrailStep) {
                float[] bc = mBaseColor.getRGBComponents(null);
                float[] hc = mHighlightColor.getRGBComponents(null);

                // figure out a color between the base and the highlight based on what step in the animation we're on
                return new Color(Math.min(hc[0], bc[0]) + ((Math.abs(bc[0] - hc[0]) * (1-((float)aTrail - (float)aTrailIndex)/(float)aTrail))),
                                 Math.min(hc[1], bc[1]) + ((Math.abs(bc[1] - hc[1]) * (1-((float)aTrail - (float)aTrailIndex)/(float)aTrail))),
                                 Math.min(hc[2], bc[2]) + ((Math.abs(bc[2] - hc[2]) * (1-((float)aTrail - (float)aTrailIndex)/(float)aTrail)))
                );
            }
        }

        // if we couldnt figure out which step in the animation we're on, just return the base color
        return mBaseColor;
    }

    private int getNumSpokes() {
        return mNumSpokes;
    }

    private float getSpokeWidth() {
        return mSpokeWidth;
    }

    private void setSpokeWidth(float theWidth) {
        mSpokeWidth = theWidth;
    }

    private float getSpokeLength() {
        return mSpokeLength;
    }

    private void setSpokeLength(float theLength) {
        mSpokeLength = theLength;
    }

    private float getCenterSize() {
        return mCenterSize;
    }

    private class AnimationWorker extends Thread {
        public void run() {
            while (mIsRunning) {
                mCurrentFrame = (mCurrentFrame +1) % 8;

                paintImmediately(0, 0, getWidth(), getHeight());

                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) {
                    // no-top we dont care about the interrupted exception
                }
            }
        }
    }
}
