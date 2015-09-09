/*
Copyright 2010-2013 Michael Shick

This file is part of 'Lock Pattern Generator'.

'Lock Pattern Generator' is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

'Lock Pattern Generator' is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
'Lock Pattern Generator'.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.example.haotian.haotianalp;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PatternGenerator
{
    protected int mGridLength;
    protected int mMinNodes;
    protected int mMaxNodes;
    protected Random mRng;
    protected List<Point> mAllNodes;

    public PatternGenerator()
    {
        mRng = new Random();
        setGridLength(0);
        setMinNodes(0);
        setMaxNodes(0);
    }

    public List<Point> getPattern()
    {
        List<Point> pattern = new ArrayList<Point>();
        //HOMEWORK SECTION 1
        Log.d("Tag1", "In GetPattern");
        int patternLength = mMaxNodes - mRng.nextInt(mMinNodes);

        while (pattern.size() < patternLength) { //iterate until we have a pattern of the right size
            Point startPoint = mAllNodes.get( mRng.nextInt(mAllNodes.size()) );
            pattern.add(startPoint);

            // now that the initial point is chosen, remove it from the list of available nodes
            mAllNodes.remove(startPoint);
            List<Point> candidateList = new ArrayList<Point>(mAllNodes);

            // REMOVE 'UNUSED' POINTS
            Iterator<Point> iter = candidateList.iterator();
            while (iter.hasNext()) {
                Log.d("Tag1", "Got inside loop");
                Point candidate = iter.next();
                int deltaX = candidate.x - startPoint.x;
                int deltaY = candidate.y - startPoint.y;
                int gcd = computeGcd(deltaX, deltaY);

                if (gcd > 1) {
                    for (int j = 1; j < gcd; j++){
                        int unusedX = startPoint.x + deltaX / (gcd * j);
                        int unusedY = startPoint.y + deltaY / (gcd * j);

                        if ( (unusedX >= 0 && unusedX <= 9) && (unusedY >= 0 && unusedY <= 9)){
                            iter.remove();
                        }
                    }
                }
            }
            //this should add a valid point to the pattern
            pattern.add(candidateList.get (mRng.nextInt( candidateList.size()) ) );
        }

        for (Point p : pattern){
            Log.d("P", "(" + p.x + ", " + p.y + ")");
        }
        return pattern;
    }

    //
    // Accessors / Mutators
    //

    public void setGridLength(int length)
    {
        // build the prototype set to copy from later
        List<Point> allNodes = new ArrayList<Point>();
        for(int y = 0; y < length; y++)
        {
            for(int x = 0; x < length; x++)
            {
                allNodes.add(new Point(x,y));
            }
        }
        mAllNodes = allNodes;

        mGridLength = length;
    }
    public int getGridLength()
    {
        return mGridLength;
    }

    public void setMinNodes(int nodes)
    {
        mMinNodes = nodes;
    }
    public int getMinNodes()
    {
        return mMinNodes;
    }

    public void setMaxNodes(int nodes)
    {
        mMaxNodes = nodes;
    }
    public int getMaxNodes()
    {
        return mMaxNodes;
    }

    //
    // Helper methods
    //

    public static int computeGcd(int a, int b)
    /* Implementation taken from
     * http://en.literateprograms.org/Euclidean_algorithm_(Java)
     * Accessed on 12/28/10
     */
    {
        if(b > a)
        {
            int temp = a;
            a = b;
            b = temp;
        }

        while(b != 0)
        {
            int m = a % b;
            a = b;
            b = m;
        }

        return a;
    }
}
