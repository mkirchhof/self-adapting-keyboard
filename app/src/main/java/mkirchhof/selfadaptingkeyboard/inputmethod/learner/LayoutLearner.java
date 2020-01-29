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

package mkirchhof.selfadaptingkeyboard.inputmethod.learner;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.ListIterator;

public class LayoutLearner {
    public static final int MIN_FREQUENCY_FOR_ADAPTION = 5;
    private final int NUMBER_DATA_POINTS_CAP = 7500;
    private final int MIN_DP_WIDTH = 18;
    private final int MIN_DP_HEIGHT = 24;
    private final int MIN_PIXEL_WIDTH;
    private final int MIN_PIXEL_HEIGHT;

    final static String TAG = LayoutLearner.class.getSimpleName();
    Context mContext;

    public LayoutLearner(Context context){
        mContext = context;
        MIN_PIXEL_WIDTH = Math.round(MIN_DP_WIDTH * context.getResources().getDisplayMetrics().density);
        MIN_PIXEL_HEIGHT = Math.round(MIN_DP_HEIGHT * context.getResources().getDisplayMetrics().density);
    }

    public LayoutLearner(Context context, int minPixelWidth, int minPixelHeight){
        mContext = context;
        MIN_PIXEL_WIDTH = minPixelWidth;
        MIN_PIXEL_HEIGHT = minPixelHeight;
    }

    public void learn(){
        Log.i(TAG, "Launching LayoutLearner");
        logStatus();

        // read cache
        Logger cache = readCache();
        Log.d(TAG, "Found " + cache.size() + " logged keystrokes");

        // summarize cache into corresponding keyStats
        HitboxesSwitcher oldHitboxes = new HitboxesSwitcher(mContext);
        KeyStatsSwitcher newStats = summarizeCache(cache, oldHitboxes);

        // merge with the existing KeyStats and cap data (so that new value will be weighted more
        // than old data by exponential smoothing)
        KeyStatsSwitcher oldStats = new KeyStatsSwitcher(mContext);
        for(KeyStats ks:newStats){
            oldStats.getKeyStats(ks.getKeyboardHash());
        }
        newStats.merge(oldStats);
        for(KeyStats ks:newStats){
            ks.reduceData(Math.min(((double) NUMBER_DATA_POINTS_CAP) / Math.min(1, ks.getN()), 1.0));
        }
        newStats.save();

        // compute the key hitboxes
        computeAllHitboxes(newStats, oldHitboxes, MIN_PIXEL_WIDTH, MIN_PIXEL_HEIGHT);
        oldHitboxes.save();

        Log.i(TAG, "LayoutLearner done.");
        logStatus();
    }

    // reads all Logger objects saved in the cache and appends them together to one Logger object
    public Logger readCache(){
        Logger allLogs = new Logger();
        try{
            // get a list of all saved Logger objects in cache
            File folder = new File(mContext.getCacheDir(),"Logger");
            if(!folder.exists()){
                return allLogs;
            }
            File[] cachedFiles = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String filename) {
                    return filename.startsWith("Logger") & filename.endsWith(".ser");
                }
            });

            // append all cached Loggers together
            for(File file:cachedFiles){
                Logger cur = Logger.load(mContext, file.getPath());
                allLogs.append(cur);
                file.delete();
            }

            Log.d(TAG, "Read " + cachedFiles.length + " cached Loggers");
        } catch(Exception e){
            Log.e(TAG, "Failed to load cache: " + e.toString());
        }

        return allLogs;
    }

    // Takes the cache and summarizes the logged button presses into a KeyStats object.
    // Uses oldHitboxes for outlier removal
    public KeyStatsSwitcher summarizeCache(Logger cache, HitboxesSwitcher oldHitboxes){
        KeyStatsSwitcher kss = new KeyStatsSwitcher(mContext);

        Iterator<Logger.LogItem> it = cache.iterator();
        while(it.hasNext()){
            Logger.LogItem cur = it.next();
            boolean isTooFarAway = false;
            if(oldHitboxes != null){
                Hitbox hitb = oldHitboxes.getHitboxes(cur.getKeyboardHash()).findCode(cur.getCode());
                if(hitb != null) {
                    isTooFarAway = !hitb.isClose(cur.getPosX(), cur.getPosY());
                }
            }

            if(!isTooFarAway & !cur.isDeleted()){
                // we have to explicitly look this up or else getKeyStats will load old existing KeyStats objects
                if(!kss.hasKeyStats(cur.getKeyboardHash())){
                    kss.createKeyStats(cur.getKeyboardHash());
                }
                kss.getKeyStats(cur.getKeyboardHash()).add(cur.getCode(), cur.getPosX(), cur.getPosY());
            }
        }

        return kss;
    }

    // wrapper that applies computeHitboxes to all KeyStats and Hitboxes inside the switchers
    public static void computeAllHitboxes(KeyStatsSwitcher kss, HitboxesSwitcher hbs,
                                          int minWidth, int minHeight){
        ListIterator<KeyStats> it = kss.listIterator();
        while(it.hasNext()){
            KeyStats ks = it.next();
            Hitboxes hb = hbs.getHitboxes(ks.getKeyboardHash());
            Log.d(TAG, "Computing Hitboxes of keyboard " + ks.getKeyboardHash());
            computeHitboxes(ks, hb, minWidth, minHeight);
        }
    }

    // updates the hitboxes by using the statistics gathered in some Keystats object using
    // normal-distribution modelling under some constraints (see documentation)
    public static void computeHitboxes(KeyStats stats, Hitboxes hitb, int minWidth, int minHeight){
        // we will use the fact that the hitboxes object containts all keys in the order topleft to
        // bottomright

        // first we care about the vertical layout, that is how much height each row is assigned
        // we will abuse the KeyStats class for this a little. The code gives the top coordinate of
        // the row to identify whether a key belongs to this row or not
        KeyStats cumRow = new KeyStats(stats.getKeyboardHash());
        KeyStat curRow = null;
        int nAllKeys = 0;
        int nSpecialKeys = 0;
        Iterator<Hitbox> hitIt = hitb.iterator();
        while(hitIt.hasNext()){
            Hitbox curHit = hitIt.next();

            // check if we have entered a new row
            if(curRow == null || curRow.getCode() != curHit.getTopLeft().getY()){
                // if we have reached the end of a row, add some aritifical n for the special keys
                if(curRow != null & (nAllKeys - nSpecialKeys) > 0){
                    curRow.multiplyN((double) nAllKeys / (nAllKeys - nSpecialKeys));
                }

                curRow = new KeyStat(curHit.getTopLeft().getY());
                cumRow.add(curRow);
                nAllKeys = 0;
                nSpecialKeys = 0;
            }
            nAllKeys++;

            // check if we have a special key (that we cant track data for) or a normal one
            KeyStat curStat = stats.findCode(curHit.getCode());
            if(curStat == null){
                nSpecialKeys++;
            } else {
                curRow.merge(curStat);
            }
        }
        // TODO: this implementation is extremely dirty. Refactor it to get it inside the loop
        // if we have reached the end of a row, add some aritifical n for the special keys
        if(curRow != null & (nAllKeys - nSpecialKeys) > 0){
            curRow.multiplyN((double) nAllKeys / (nAllKeys - nSpecialKeys));
        }

        int sumN = 0;
        Iterator<KeyStat> rowIt = cumRow.iterator();
        while(rowIt.hasNext()){
            sumN += rowIt.next().getN();
        }

        // compute the actual borders of each row and insert it into the corresponding hitboxes
        if(cumRow.size() > 1){
            rowIt = cumRow.iterator();
            curRow = rowIt.next();
            Hitbox curHit = hitb.findRowStart(curRow.getCode());
            while(rowIt.hasNext()){
                KeyStat nextRow = rowIt.next();
                Hitbox nextHit = hitb.findRowStart(nextRow.getCode());

                // if a row has too few observations (such as the bottom row, which only consists of
                // special keys), dont do any changes
                if(curHit != null && nextHit != null &&
                        curRow.getN() > MIN_FREQUENCY_FOR_ADAPTION &&
                        nextRow.getN() > MIN_FREQUENCY_FOR_ADAPTION) {
                        int curTop = curHit.getTopLeft().getY();
                        int curBottom = curHit.getBottomLeft().getY();
                        int nextTop = nextHit.getTopLeft().getY();
                        int nextBottom = nextHit.getBottomLeft().getY();
                        int border = computeBorder(curTop, curBottom, nextTop, nextBottom,
                                curRow.getMeanY(), nextRow.getMeanY(), curRow.getVarY(), nextRow.getVarY(),
                                (double) curRow.getN() / sumN, (double) nextRow.getN() / sumN);

                        boolean fulfillsMinSize = Math.abs(border - curTop) >= minHeight &
                                Math.abs(border - nextBottom) >= minHeight;

                        if (border != curBottom & fulfillsMinSize) {
                            //Log.d(TAG, "Adapted border of row " + curHit.getCode() + " from " + curBottom + " to " + border);
                            // assign the new border to all involved hitboxes
                            hitIt = hitb.iterator();
                            while (hitIt.hasNext()) {
                                Hitbox curHitb = hitIt.next();
                                if (curHitb.getBottomLeft().getY() == curBottom) {
                                    curHitb.setBottom(border);
                                } else if (curHitb.getTopLeft().getY() == nextTop) {
                                    curHitb.setTop(border);
                                }
                            }
                        }


                }

                curRow = nextRow;
                curHit = nextHit;
            }
        }

        // now we'll take care of the horizontal layout
        hitIt = hitb.iterator();
        Hitbox curHit = hitIt.next();
        KeyStat curStat = stats.findCode(curHit.getCode());
        //TODO: Set curHit's xLeft to 0
        Hitbox nextHit;
        rowIt = cumRow.iterator();
        curRow = rowIt.next();
        while(hitIt.hasNext()){
            nextHit = hitIt.next();
            KeyStat nextStat = stats.findCode(nextHit.getCode());
            // check if the two keys are in the same row
            if(curHit.getTopLeft().getY() == nextHit.getTopLeft().getY()){
                // Don't edit special keys or rarely occuring keys
                if(curStat != null && nextStat != null &&
                        curStat.getN() > MIN_FREQUENCY_FOR_ADAPTION &&
                        nextStat.getN() > MIN_FREQUENCY_FOR_ADAPTION &&
                        curRow.getN() > MIN_FREQUENCY_FOR_ADAPTION){
                    int curLeft = curHit.getTopLeft().getX();
                    int curRight = curHit.getTopRight().getX();
                    int nextLeft = nextHit.getTopLeft().getX();
                    int nextRight = nextHit.getTopRight().getX();
                    int border = computeBorder(curLeft, curRight, nextLeft, nextRight,
                            curStat.getMeanX(), nextStat.getMeanX(), curStat.getVarX(), nextStat.getVarX(),
                            (double) curStat.getN() / curRow.getN(), (double) nextStat.getN() / curRow.getN());

                    // prevent making the keys too small
                    if(Math.abs(border - curLeft) < minWidth){
                        border = curLeft + minWidth;
                    }
                    if(Math.abs(border - nextRight) < minWidth){
                        border = nextRight - minWidth;
                    }


                    // border could still be conflicting minSize if the keys were already too small
                    // before; Then we do not change anything
                    boolean fulfillsMinSize = Math.abs(border - curLeft) >= minWidth &
                            Math.abs(border - nextRight) >= minWidth;

                    if(border != curRight & fulfillsMinSize){
                        //Log.d(TAG, "Adapted border of key " + curHit.getCode() + " from " + curRight + " to " + border);
                        curHit.setRight(border);
                        nextHit.setLeft(border);
                    }
                }
            } else {
                //TODO: Set curHit's xRight to keyboard width and nextHit's xLeft to 0
                curRow = rowIt.next();
            }
            curHit = nextHit;
            curStat = nextStat;
        }
    }

    // computes the breakpoint at which the right normal distribution gets bigger than the left one
    // (breakpoint is rounded down to next int)
    public static int computeBorder(int startLeft, int endLeft, int startRight, int endRight,
                                    double meanLeft, double meanRight, double varLeft, double varRight,
                                    double priorLeft, double priorRight){
        // Handle special keys
        if(priorRight == 0 | varRight == 0 | priorLeft == 0 | varLeft == 0){
            return startRight;
        }

        // the following is just computing at which point two normal distributions intersect
        int border;
        if(varLeft - varRight != 0){
            double p = (2 * meanRight * varLeft - 2 * meanLeft * varRight) / (varRight - varLeft);
            double q = (varRight * meanLeft*meanLeft - varLeft * meanRight*meanRight - 2 * varLeft * varRight *
                    (Math.log(priorLeft / Math.sqrt(varLeft)) - Math.log(priorRight / Math.sqrt(varRight)))) /
                    (varRight - varLeft);
            border = (int) Math.floor(- p / 2 - Math.sqrt((p / 2)*(p / 2) - q));
            if(!(startLeft < border & border < endRight)){
                border = (int) Math.floor(- p / 2 + Math.sqrt((p / 2)*(p / 2) - q));
                if(!(startLeft < border & border < endRight)){
                    border = endLeft;
                }
            }
        } else {
            border = (int) Math.floor((varLeft * 2 * (Math.log(priorLeft / Math.sqrt(varLeft)) - Math.log(priorRight / Math.sqrt(varRight))) - meanLeft*meanLeft + meanRight*meanRight) /
                    (2 * (meanRight - meanLeft)));
        }

        return border;
    }

    // logs the number of known hitboxes, keystats etc to the console
    // TODO: Move this to utils
    public void logStatus(){
        File[] hitboxes = null;
        File[] keyStats = null;
        File hbFolder = new File(mContext.getFilesDir(),"Hitboxes");
        if(hbFolder.exists()) {
            hitboxes = hbFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String filename) {
                    return filename.startsWith("Hitboxes") & filename.endsWith(".ser");
                }
            });
        }
        File ksFolder = new File(mContext.getFilesDir(),"KeyStats");
        if(ksFolder.exists()) {
            keyStats = ksFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String filename) {
                    return filename.startsWith("KeyStats") & filename.endsWith(".ser");
                }
            });
        }
        if(hitboxes != null){
            Log.i(TAG, "Found " + hitboxes.length + " Hitboxes files:");
            for(File file:hitboxes){
                Log.i(TAG, file.getName());
            }
        }
        if(keyStats != null){
            Log.i(TAG, "Found " + keyStats.length + " KeyStats files:");
            for(File file:keyStats){
                Log.i(TAG, file.getName());
            }
        }
    }
}
