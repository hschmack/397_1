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
        List<Point> allNodes = new ArrayList<Point>(mAllNodes);
        //HOMEWORK SECTION 1
        Log.d("Tag1", "In GetPattern"); //REMOVE BEFORE SUBMISSION
        int patternLength = mMaxNodes - mRng.nextInt(mMinNodes);

        while (pattern.size() != patternLength) { //iterate until we have a pattern of the right size
            Point startPoint;
            if( pattern.isEmpty() ) {
                startPoint = allNodes.get(mRng.nextInt(mAllNodes.size()));
                pattern.add(startPoint);
            } else {
                startPoint = pattern.get( pattern.size() - 1 );
            }
            // now that the initial point is chosen, remove it from the list of available nodes
            allNodes.remove(startPoint);
            List<Point> candidateList = new ArrayList<Point>(allNodes);

            // REMOVE 'UNUSED' POINTS + points already in the pattern
            Iterator<Point> iter = candidateList.iterator();
            while (iter.hasNext()) {
                Point candidate = iter.next();

                if ( pattern.contains(candidate) ){
                    iter.remove();
                    Log.d("P", "The point: " + candidate.toString() + " already exists in the pattern, breaking"); //REMOVE BEFORE SUBMISSION
                    break;
                }
                int deltaX = candidate.x - startPoint.x;
                int deltaY = candidate.y - startPoint.y;
                int gcd = Math.abs(computeGcd(deltaX, deltaY));
                Log.d("Tag1", "Considering candidate: " + candidate.toString() +" from start point: " + startPoint.toString()+ " WITH GCD: " + gcd); //REMOVE BEFORE SUBMISSION

                if (gcd > 1) {
                    for (int j = 1; j < gcd; j++){
                        int unusedX = startPoint.x + deltaX / (gcd * j);
                        int unusedY = startPoint.y + deltaY / (gcd * j);
                        Log.d("P", "considering point : (" + unusedX + ", "+unusedY); //REMOVE BEFORE SUBMISSION

                        if ( (unusedX >= 0 && unusedX <= 2) && (unusedY >= 0 && unusedY <= 2)){
                            Log.d("P", "Point : (" + unusedX + ", "+unusedY + " is a valid point, removing: " + candidate.toString() ); //REMOVE BEFORE SUBMISSION
                            iter.remove();
                        }
                    }
                }
            }
            //this should add a valid point to the pattern
            Point toAdd = candidateList.get (mRng.nextInt( candidateList.size()) );
            pattern.add(toAdd);
            allNodes.remove(toAdd);
        }

        for (Point p : pattern){
            Log.d("P", p.toString());
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
