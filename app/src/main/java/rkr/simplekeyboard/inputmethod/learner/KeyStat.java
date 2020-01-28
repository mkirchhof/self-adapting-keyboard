/*
 * Copyright (C) 2020 Michael Kirchhof
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.learner;

import java.io.Serializable;

// saves all statistical information (mean and variance matrix) of a key button
public class KeyStat extends Object implements Serializable {
    private static final long serialVersionUID = 1L;

    private int mCode;
    private double mMeanX;
    private double mMeanY;
    private double mSumXX;
    private double mSumYY;
    private double mSumXY;
    private int n;

    public KeyStat(int code){
        mCode = code;
        mMeanX = 0;
        mMeanY = 0;
        mSumXX = 0;
        mSumYY = 0;
        mSumXY = 0;
        n = 0;
    }

    public KeyStat(int code, int x, int y){
        mCode = code;
        mMeanX = x;
        mMeanY = y;
        mSumXX = 0;
        mSumYY = 0;
        mSumXY = 0;
        n = 1;
    }

    public KeyStat(int code, double meanX, double meanY, double varX, double varY, double covXY, int N){
        mCode = code;
        mMeanX = meanX;
        mMeanY = meanY;
        n = N;
        if(n == 0){
            mSumXX = 0;
            mSumYY = 0;
            mSumXY = 0;
        } else {
            mSumXX = varX * (n - 1);
            mSumYY = varY * (n - 1);
            mSumXY = covXY * (n - 1);
        }
    }

    public boolean hasSameCode(KeyStat other){
        return mCode == other.getCode();
    }

    // Add an observation and update mean and cov using online algorithm
    // (see https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online)
    public void add(int x, int y){
        n++;
        if(n == 1){
            mMeanX += x;
            mMeanY += y;
        } else {
            double dx = x - mMeanX;
            double dy = y - mMeanY;
            mMeanX += dx / n;
            mMeanY += dy / n;
            mSumXX += dx * dx * (n - 1) / n;
            mSumYY += dy * dy * (n - 1) / n;
            mSumXY += dx * dy * (n - 1) / n;
        }
    }

    // update the mMean, mSumXX etc by using the information in other KeyStat object
    public void merge(KeyStat other){
        if(other.getN() > 0){
            double pooledMeanX = (getMeanX() * getN() + other.getMeanX() * other.getN()) / (getN() + other.getN());
            double pooledMeanY = (getMeanY() * getN() + other.getMeanY() * other.getN()) / (getN() + other.getN());

            double dx1 = getMeanX() - pooledMeanX;
            double dy1 = getMeanY() - pooledMeanY;
            double dx2 = other.getMeanX() - pooledMeanX;
            double dy2 = other.getMeanY() - pooledMeanY;

            mMeanX = pooledMeanX;
            mMeanY = pooledMeanY;
            mSumXX = mSumXX + n * dx1 * dx1 + other.getVarX() * (other.getN() - 1) + other.getN() * dx2 * dx2;
            mSumYY = mSumYY + n * dy1 * dy1 + other.getVarY() * (other.getN() - 1) + other.getN() * dy2 * dy2;
            mSumXY = mSumXY + n * dx1 * dy1 + other.getCovXY() * (other.getN() - 1) + other.getN() * dx2 * dy2;
            n += other.getN();
        }
    }

    public int getCode(){return mCode;}

    public double getMeanX(){ return mMeanX;}

    public double getMeanY(){ return mMeanY;}

    public double getVarX(){
        if(n <= 1) {
            return 0;
        } else {
            return mSumXX / (n - 1);
        }
    }

    public double getVarY(){
        if(n <= 1) {
            return 0;
        } else {
            return mSumYY / (n - 1);
        }
    }

    public double getCovXY(){
        if(n <= 1) {
            return 0;
        } else {
            return mSumXY / (n - 1);
        }
    }

    public int getN(){ return n;}

    // multiply n by some factor. Use with caution. Currently only used for a special case in LayoutLearnerWorker
    public void multiplyN(double factor) { n = (int) Math.round(factor * n); }

    @Override
    public String toString(){
        return "" + mCode + "," + mMeanX + "," + mMeanY + "," + mSumXX + "," + mSumYY + "," +
                mSumXY + "," + n;
    }

    // delete a part of the learnt data to reduce its size to the given percentage
    // Returns the actual percentage that was reduced (due to rounding)
    public double reduceData(double percentage){
        if(percentage < 0 | percentage > 1){
            return 1.0;
        }

        // due to rounding, we will not be able to reduce the data exactly by this percentage,
        // find the closest possible percentage
        double actualPerc =  Math.floor(percentage * n) / n;

        mSumXX = actualPerc * mSumXX;
        mSumXY = actualPerc * mSumXY;
        mSumYY = actualPerc * mSumYY;
        n = (int) Math.round(actualPerc * n);

        return actualPerc;
    }
}