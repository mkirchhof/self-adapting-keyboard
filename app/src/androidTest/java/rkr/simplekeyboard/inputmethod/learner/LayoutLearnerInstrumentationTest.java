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

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestWorkerBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LayoutLearnerInstrumentationTest {
    private Context mContext;
    private Executor mExecutor;

    @Before
    public void setup(){
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mExecutor = Executors.newSingleThreadExecutor();
        cleanUp();
    }

    // deletes existing KeyStats, Hitboxes and Logger files
    public void cleanUp(){
        // delete all Loggers:
        File folder = new File(mContext.getCacheDir(),"Logger");
        File[] cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("Logger") & filename.endsWith(".ser");
            }
        });
        if(cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }

        // delete all hitboxes:
        folder = new File(mContext.getFilesDir(),"Hitboxes");
        cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("Hitboxes") & filename.endsWith(".ser");
            }
        });
        if(cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }

        // delete all KeyStats:
        folder = new File(mContext.getFilesDir(),"KeyStats");
        cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("KeyStats") & filename.endsWith(".ser");
            }
        });
        if(cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }
    }

    // Verfiy that the whole cache is read in properly
    @Test
    public void readCacheTest(){
        int KEYBOARD_HASH_1 = 1;

        Logger logger1 = new Logger();
        logger1.set(0, 0, 1, 'A', 6, 3, KEYBOARD_HASH_1);
        logger1.set(1, 1, 1, 'A', 12, 4, KEYBOARD_HASH_1);
        logger1.set(2, 2, 1, 'A', 11, 4, KEYBOARD_HASH_1);
        logger1.set(3, 3, 1, 'A', 8, 3, KEYBOARD_HASH_1);
        logger1.set(4, 4, 1, 'A', 14, 2, KEYBOARD_HASH_1);
        logger1.set(5, 5, 1, 'A', 12, 5, KEYBOARD_HASH_1);
        logger1.save(mContext);

        Logger logger2 = new Logger();
        logger2.set(0, 0, 2, 'B', 18, 3, KEYBOARD_HASH_1);
        logger2.set(1, 1, 2, 'B', 15, 4, KEYBOARD_HASH_1);
        logger2.set(2, 2, 2, 'B', 24, 1, KEYBOARD_HASH_1);
        logger2.set(3, 3, 2, 'B', 23, 2, KEYBOARD_HASH_1);
        logger2.set(4, 4, 2, 'B', 22, 2, KEYBOARD_HASH_1);
        logger2.set(5, 5, 2, 'B', 20, 4, KEYBOARD_HASH_1);
        logger2.set(6, 6, 2, 'B', 19, 2, KEYBOARD_HASH_1);
        logger2.set(7, 7, 2, 'B', 21, 3, KEYBOARD_HASH_1);
        logger2.save(mContext);

        Logger logger3 = new Logger();
        logger3.set(0, 0, 3, 'C', 28, 3, KEYBOARD_HASH_1);
        logger3.set(1, 1, 3, 'C', 25, 5, KEYBOARD_HASH_1);
        logger3.set(2, 2, 3, 'C', 29, 5, KEYBOARD_HASH_1);
        logger3.set(3, 3, 3, 'C', 21, 5, KEYBOARD_HASH_1);
        logger3.set(4, 4, 3, 'C', 27, 4, KEYBOARD_HASH_1);
        logger3.set(5, 5, 3, 'C', 28, 4, KEYBOARD_HASH_1);
        logger3.save(mContext);

        LayoutLearner ll = new LayoutLearner(mContext);

        Logger cachedLogs = ll.readCache();

        // assert that all logs where read in
        assertEquals(20, cachedLogs.size());
        int[][] logger1Array = logger1.toArray();
        int[][] logger2Array = logger2.toArray();
        int[][] logger3Array = logger3.toArray();
        int[][] cacheArray = cachedLogs.toArray();

        boolean allFound = true;
        for(int[] log:logger1Array){
            boolean found = false;
            for(int[] cachedLog:cacheArray){
                if(Arrays.equals(log, cachedLog)){
                    found = true;
                }
            }
            if(!found){
                allFound = false;
            }
        }
        for(int[] log:logger2Array){
            boolean found = false;
            for(int[] cachedLog:cacheArray){
                if(Arrays.equals(log, cachedLog)){
                    found = true;
                }
            }
            if(!found){
                allFound = false;
            }
        }
        for(int[] log:logger3Array){
            boolean found = false;
            for(int[] cachedLog:cacheArray){
                if(Arrays.equals(log, cachedLog)){
                    found = true;
                }
            }
            if(!found){
                allFound = false;
            }
        }

        assertTrue(allFound);

        // assert that the layoutLearner cleaned up the cache properly
        File folder = new File(mContext.getCacheDir(),"Logger");
        File[] cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("Logger") & filename.endsWith(".ser");
            }
        });
        assertEquals(0, cachedFiles.length);
    }

    @Test
    public void summarizeCacheTest(){
        int KEYBOARD_HASH_1 = 1;

        Hitboxes hitb = new Hitboxes(KEYBOARD_HASH_1);
        hitb.add(new Hitbox(1, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb.add(new Hitbox(2, new Point(11, 1), new Point(20, 1), new Point(11, 5), new Point(20, 5)));
        hitb.add(new Hitbox(3, new Point(21, 1), new Point(30, 1), new Point(21, 5), new Point(30, 5)));
        HitboxesSwitcher hbs = new HitboxesSwitcher(mContext);
        hbs.add(hitb);

        Logger logger1 = new Logger();
        logger1.set(0, 0, 1, 'A', 6, 3, KEYBOARD_HASH_1);
        logger1.set(1, 1, 1, 'A', 12, 4, KEYBOARD_HASH_1);
        logger1.set(2, 2, 1, 'A', 11, 4, KEYBOARD_HASH_1);
        logger1.set(3, 3, 1, 'A', 8, 3, KEYBOARD_HASH_1);
        logger1.set(4, 4, 1, 'A', 14, 2, KEYBOARD_HASH_1);
        logger1.set(5, 5, 1, 'A', 12, 5, KEYBOARD_HASH_1);
        logger1.set(6, 0, 2, 'B', 18, 3, KEYBOARD_HASH_1);
        logger1.set(7, 1, 2, 'B', 15, 4, KEYBOARD_HASH_1);
        logger1.set(8, 2, 2, 'B', 24, 1, KEYBOARD_HASH_1);
        logger1.set(9, 3, 2, 'B', 23, 2, KEYBOARD_HASH_1);
        logger1.set(10, 4, 2, 'B', 22, 2, KEYBOARD_HASH_1);
        logger1.set(11, 5, 2, 'B', 20, 4, KEYBOARD_HASH_1);
        logger1.set(12, 6, 2, 'B', 19, 2, KEYBOARD_HASH_1);
        logger1.set(13, 7, 2, 'B', 21, 3, KEYBOARD_HASH_1);
        logger1.set(14, 0, 3, 'C', 28, 3, KEYBOARD_HASH_1);
        logger1.set(15, 1, 3, 'C', 25, 5, KEYBOARD_HASH_1);
        logger1.set(16, 2, 3, 'C', 29, 5, KEYBOARD_HASH_1);
        logger1.set(17, 3, 3, 'C', 21, 5, KEYBOARD_HASH_1);
        logger1.set(18, 4, 3, 'C', 27, 4, KEYBOARD_HASH_1);
        logger1.set(19, 5, 3, 'C', 28, 4, KEYBOARD_HASH_1);

        LayoutLearner ll = new LayoutLearner(mContext);

        KeyStatsSwitcher kss = ll.summarizeCache(logger1, hbs);

        // We only have one keyboard, so kss should include only one keyStats object.
        // That object should contain info about all three keys
        assertEquals(1, kss.size());
        assertEquals(3, kss.getKeyStats(KEYBOARD_HASH_1).size());
        assertEquals(6, kss.getKeyStats(KEYBOARD_HASH_1).findCode(1).getN());
        assertEquals(8, kss.getKeyStats(KEYBOARD_HASH_1).findCode(2).getN());
        assertEquals(6, kss.getKeyStats(KEYBOARD_HASH_1).findCode(3).getN());
    }

    @Test
    public void computeAllHitboxesTest(){
        int KEYBOARD_HASH_1 = 1;
        int KEYBOARD_HASH_2 = 2;

        Hitboxes hitb1 = new Hitboxes(KEYBOARD_HASH_1);
        hitb1.add(new Hitbox(1, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb1.add(new Hitbox(2, new Point(11, 1), new Point(20, 1), new Point(11, 5), new Point(20, 5)));
        hitb1.add(new Hitbox(3, new Point(21, 1), new Point(30, 1), new Point(21, 5), new Point(30, 5)));

        Hitboxes hitb2 = new Hitboxes(KEYBOARD_HASH_2);
        hitb2.add(new Hitbox(2, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb2.add(new Hitbox(3, new Point(11, 1), new Point(20, 1), new Point(11, 5), new Point(20, 5)));
        hitb2.add(new Hitbox(4, new Point(21, 1), new Point(30, 1), new Point(21, 5), new Point(30, 5)));

        HitboxesSwitcher hbs = new HitboxesSwitcher(mContext);
        hbs.add(hitb1);
        hbs.add(hitb2);

        KeyStats keySt1 = new KeyStats(KEYBOARD_HASH_1);
        keySt1.add(new KeyStat(1, 12, 3, 16, 4, 0, 30));
        keySt1.add(new KeyStat(2, 20, 3, 16, 4, 0, 30));
        keySt1.add(new KeyStat(3, 26, 3, 16, 4, 0, 30));

        KeyStats keySt2 = new KeyStats(KEYBOARD_HASH_2);
        keySt2.add(new KeyStat(2, 12, 3, 16, 4, 0, 30));
        keySt2.add(new KeyStat(3, 20, 3, 16, 4, 0, 30));
        keySt2.add(new KeyStat(4, 26, 3, 16, 4, 0, 30));

        KeyStatsSwitcher kss = new KeyStatsSwitcher(mContext);
        kss.add(keySt1);
        kss.add(keySt2);

        LayoutLearner.computeAllHitboxes(kss, hbs, 0, 0);

        assertTrue(hbs.getHitboxes(KEYBOARD_HASH_1).findCode(1).getBottomRight().getX() > 10);
        assertTrue(hbs.getHitboxes(KEYBOARD_HASH_1).findCode(2).getBottomRight().getX() > 20);
        assertTrue(hbs.getHitboxes(KEYBOARD_HASH_2).findCode(2).getBottomRight().getX() > 10);
        assertTrue(hbs.getHitboxes(KEYBOARD_HASH_2).findCode(3).getBottomRight().getX() > 20);
    }

    @Test
    public void doWorkTest(){
        int KEYBOARD_HASH_1 = 1;

        Hitboxes hitb = new Hitboxes(KEYBOARD_HASH_1);
        hitb.add(new Hitbox(1, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb.add(new Hitbox(2, new Point(10, 1), new Point(20, 1), new Point(10, 5), new Point(20, 5)));
        hitb.add(new Hitbox(3, new Point(20, 1), new Point(30, 1), new Point(20, 5), new Point(30, 5)));
        hitb.save(mContext);

        /*
        Hitboxes hitb2 = new Hitboxes(KEYBOARD_HASH_2);
        hitb2.add(new Hitbox(2, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb2.add(new Hitbox(3, new Point(11, 1), new Point(20, 1), new Point(11, 5), new Point(20, 5)));
        hitb2.add(new Hitbox(4, new Point(21, 1), new Point(30, 1), new Point(21, 5), new Point(30, 5)));
        hitb2.save(mContext);
         */

        Logger logger1 = new Logger();
        logger1.set(0, 0, 1, 'A', 6, 3, KEYBOARD_HASH_1);
        logger1.set(1, 1, 1, 'A', 12, 4, KEYBOARD_HASH_1);
        logger1.set(2, 2, 1, 'A', 11, 4, KEYBOARD_HASH_1);
        logger1.set(3, 3, 1, 'A', 8, 3, KEYBOARD_HASH_1);
        logger1.set(4, 4, 1, 'A', 14, 2, KEYBOARD_HASH_1);
        logger1.set(5, 5, 1, 'A', 12, 5, KEYBOARD_HASH_1);
        logger1.save(mContext);

        Logger logger2 = new Logger();
        logger2.set(0, 0, 2, 'B', 18, 3, KEYBOARD_HASH_1);
        logger2.set(1, 1, 2, 'B', 19, 4, KEYBOARD_HASH_1);
        logger2.set(2, 2, 2, 'B', 24, 1, KEYBOARD_HASH_1);
        logger2.set(3, 3, 2, 'B', 23, 2, KEYBOARD_HASH_1);
        logger2.set(4, 4, 2, 'B', 22, 2, KEYBOARD_HASH_1);
        logger2.set(5, 5, 2, 'B', 20, 4, KEYBOARD_HASH_1);
        logger2.set(6, 6, 2, 'B', 19, 2, KEYBOARD_HASH_1);
        logger2.set(7, 7, 2, 'B', 21, 3, KEYBOARD_HASH_1);
        logger2.save(mContext);

        Logger logger3 = new Logger();
        logger3.set(0, 0, 3, 'C', 28, 3, KEYBOARD_HASH_1);
        logger3.set(1, 1, 3, 'C', 25, 5, KEYBOARD_HASH_1);
        logger3.set(2, 2, 3, 'C', 29, 5, KEYBOARD_HASH_1);
        logger3.set(3, 3, 3, 'C', 26, 5, KEYBOARD_HASH_1);
        logger3.set(4, 4, 3, 'C', 27, 4, KEYBOARD_HASH_1);
        logger3.set(5, 5, 3, 'C', 28, 4, KEYBOARD_HASH_1);
        logger3.save(mContext);

        LayoutLearner ll = new LayoutLearner(mContext, 0, 0);
        ll.learn();

        KeyStats newKs = KeyStats.load(mContext, KEYBOARD_HASH_1);
        Hitboxes newHitb = Hitboxes.load(mContext, KEYBOARD_HASH_1);

        // assert that no duplicate data was created
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
        assertEquals(1, hitboxes.length);
        assertEquals(1, keyStats.length);

        // assert that KeyStats were updated correctly
        assertEquals(6, newKs.findCode(1).getN());
        assertEquals(8, newKs.findCode(2).getN());
        assertEquals(6, newKs.findCode(3).getN());

        // assert that keyboard layout was changed correctly
        assertTrue(newHitb.findCode(1).getBottomRight().getX() >
                hitb.findCode(1).getBottomRight().getX());
        assertTrue(newHitb.findCode(2).getBottomLeft().getX() >
                hitb.findCode(2).getBottomLeft().getX());
        assertTrue(newHitb.findCode(2).getBottomRight().getX() >
                hitb.findCode(2).getBottomRight().getX());
        assertTrue(newHitb.findCode(3).getBottomLeft().getX() >
                hitb.findCode(3).getBottomLeft().getX());

    }

    @Test
    public void doWorkWithExistingKeyStatsTest(){
        int KEYBOARD_HASH_1 = 1;

        Hitboxes hitb = new Hitboxes(KEYBOARD_HASH_1);
        hitb.add(new Hitbox(1, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb.add(new Hitbox(2, new Point(10, 1), new Point(20, 1), new Point(10, 5), new Point(20, 5)));
        hitb.add(new Hitbox(3, new Point(20, 1), new Point(30, 1), new Point(20, 5), new Point(30, 5)));
        hitb.save(mContext);

        KeyStats keySt = new KeyStats(KEYBOARD_HASH_1);
        keySt.add(new KeyStat(1, 12, 3, 16, 4, 0, 30));
        keySt.add(new KeyStat(2, 20, 3, 16, 4, 0, 30));
        keySt.add(new KeyStat(3, 26, 3, 16, 4, 0, 30));
        keySt.save(mContext);

        Logger logger1 = new Logger();
        logger1.set(0, 0, 1, 'A', 6, 3, KEYBOARD_HASH_1);
        logger1.set(1, 1, 1, 'A', 12, 4, KEYBOARD_HASH_1);
        logger1.set(2, 2, 1, 'A', 11, 4, KEYBOARD_HASH_1);
        logger1.set(3, 3, 1, 'A', 8, 3, KEYBOARD_HASH_1);
        logger1.set(4, 4, 1, 'A', 14, 2, KEYBOARD_HASH_1);
        logger1.set(5, 5, 1, 'A', 12, 5, KEYBOARD_HASH_1);
        logger1.save(mContext);

        Logger logger2 = new Logger();
        logger2.set(0, 0, 2, 'B', 18, 3, KEYBOARD_HASH_1);
        logger2.set(1, 1, 2, 'B', 19, 4, KEYBOARD_HASH_1);
        logger2.set(2, 2, 2, 'B', 24, 1, KEYBOARD_HASH_1);
        logger2.set(3, 3, 2, 'B', 23, 2, KEYBOARD_HASH_1);
        logger2.set(4, 4, 2, 'B', 22, 2, KEYBOARD_HASH_1);
        logger2.set(5, 5, 2, 'B', 20, 4, KEYBOARD_HASH_1);
        logger2.set(6, 6, 2, 'B', 19, 2, KEYBOARD_HASH_1);
        logger2.set(7, 7, 2, 'B', 21, 3, KEYBOARD_HASH_1);
        logger2.save(mContext);

        Logger logger3 = new Logger();
        logger3.set(0, 0, 3, 'C', 28, 3, KEYBOARD_HASH_1);
        logger3.set(1, 1, 3, 'C', 25, 5, KEYBOARD_HASH_1);
        logger3.set(2, 2, 3, 'C', 29, 5, KEYBOARD_HASH_1);
        logger3.set(3, 3, 3, 'C', 26, 5, KEYBOARD_HASH_1);
        logger3.set(4, 4, 3, 'C', 27, 4, KEYBOARD_HASH_1);
        logger3.set(5, 5, 3, 'C', 28, 4, KEYBOARD_HASH_1);
        logger3.save(mContext);

        LayoutLearner ll = new LayoutLearner(mContext, 0, 0);
        ll.learn();

        KeyStats newKs = KeyStats.load(mContext, KEYBOARD_HASH_1);
        Hitboxes newHitb = Hitboxes.load(mContext, KEYBOARD_HASH_1);

        // assert that no duplicate data was created
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
        assertEquals(1, hitboxes.length);
        assertEquals(1, keyStats.length);

        // assert that KeyStats were updated correctly
        assertEquals(36, newKs.findCode(1).getN());
        assertEquals(38, newKs.findCode(2).getN());
        assertEquals(36, newKs.findCode(3).getN());

        // assert that keyboard layout was changed correctly
        assertTrue(newHitb.findCode(1).getBottomRight().getX() >
                hitb.findCode(1).getBottomRight().getX());
        assertTrue(newHitb.findCode(2).getBottomLeft().getX() >
                hitb.findCode(2).getBottomLeft().getX());
        assertTrue(newHitb.findCode(2).getBottomRight().getX() >
                hitb.findCode(2).getBottomRight().getX());
        assertTrue(newHitb.findCode(3).getBottomLeft().getX() >
                hitb.findCode(3).getBottomLeft().getX());

    }

    @Test
    public void doWorkMultipleKeyboardsTest(){
        int KEYBOARD_HASH_1 = 1;
        int KEYBOARD_HASH_2 = 2;

        Hitboxes hitb = new Hitboxes(KEYBOARD_HASH_1);
        hitb.add(new Hitbox(1, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb.add(new Hitbox(2, new Point(10, 1), new Point(20, 1), new Point(10, 5), new Point(20, 5)));
        hitb.add(new Hitbox(3, new Point(20, 1), new Point(30, 1), new Point(20, 5), new Point(30, 5)));
        hitb.save(mContext);

        Hitboxes hitb2 = new Hitboxes(KEYBOARD_HASH_2);
        hitb2.add(new Hitbox(2, new Point(1, 1), new Point(10, 1), new Point(1, 5), new Point(10, 5)));
        hitb2.add(new Hitbox(3, new Point(10, 1), new Point(20, 1), new Point(10, 5), new Point(20, 5)));
        hitb2.add(new Hitbox(4, new Point(20, 1), new Point(30, 1), new Point(20, 5), new Point(30, 5)));
        hitb2.save(mContext);

        KeyStats keySt = new KeyStats(KEYBOARD_HASH_1);
        keySt.add(new KeyStat(1, 12, 3, 16, 4, 0, 30));
        keySt.add(new KeyStat(2, 20, 3, 16, 4, 0, 30));
        keySt.add(new KeyStat(3, 26, 3, 16, 4, 0, 30));
        keySt.save(mContext);

        Logger logger1 = new Logger();
        logger1.set(0, 0, 1, 'A', 6, 3, KEYBOARD_HASH_1);
        logger1.set(1, 1, 1, 'A', 12, 4, KEYBOARD_HASH_1);
        logger1.set(2, 2, 1, 'A', 11, 4, KEYBOARD_HASH_1);
        logger1.set(3, 3, 1, 'A', 8, 3, KEYBOARD_HASH_1);
        logger1.set(4, 4, 1, 'A', 14, 2, KEYBOARD_HASH_1);
        logger1.set(5, 5, 1, 'A', 12, 5, KEYBOARD_HASH_1);
        logger1.save(mContext);

        Logger logger2 = new Logger();
        logger2.set(0, 0, 2, 'B', 18, 3, KEYBOARD_HASH_1);
        logger2.set(1, 1, 2, 'B', 19, 4, KEYBOARD_HASH_1);
        logger2.set(2, 2, 2, 'B', 24, 1, KEYBOARD_HASH_1);
        logger2.set(3, 3, 2, 'B', 23, 2, KEYBOARD_HASH_1);
        logger2.set(4, 4, 2, 'B', 22, 2, KEYBOARD_HASH_1);
        logger2.set(5, 5, 2, 'B', 20, 4, KEYBOARD_HASH_1);
        logger2.set(6, 6, 2, 'B', 19, 2, KEYBOARD_HASH_1);
        logger2.set(7, 7, 2, 'B', 21, 3, KEYBOARD_HASH_1);
        logger2.save(mContext);

        Logger logger3 = new Logger();
        logger3.set(0, 0, 3, 'C', 28, 3, KEYBOARD_HASH_1);
        logger3.set(1, 1, 3, 'C', 25, 5, KEYBOARD_HASH_1);
        logger3.set(2, 2, 3, 'C', 29, 5, KEYBOARD_HASH_1);
        logger3.set(3, 3, 3, 'C', 26, 5, KEYBOARD_HASH_1);
        logger3.set(4, 4, 3, 'C', 27, 4, KEYBOARD_HASH_1);
        logger3.set(5, 5, 3, 'C', 28, 4, KEYBOARD_HASH_1);
        logger3.save(mContext);

        Logger logger4 = new Logger();
        logger4.set(0, 0, 2, 'B', 7, 1, KEYBOARD_HASH_2);
        logger4.set(1, 1, 2, 'B', 9, 2, KEYBOARD_HASH_2);
        logger4.set(2, 2, 3, 'C', 17, 3, KEYBOARD_HASH_2);
        logger4.set(3, 3, 2, 'B', 7, 1, KEYBOARD_HASH_1);
        logger4.save(mContext);

        Logger logger5 = new Logger();
        logger5.set(0, 0, 3, 'C', 19, 2, KEYBOARD_HASH_2);
        logger5.set(1, 1, 3, 'C', 24, 2, KEYBOARD_HASH_2);
        logger5.set(2, 2, 2, 'B', 13, 2, KEYBOARD_HASH_2);
        logger5.set(3, 3, 3, 'C', 23, 2, KEYBOARD_HASH_2);
        logger5.set(4, 4, 3, 'C', 23, 2, KEYBOARD_HASH_2);
        logger5.set(5, 5, 3, 'C', 25, 2, KEYBOARD_HASH_2);
        logger5.save(mContext);

        Logger logger6 = new Logger();
        logger6.set(0, 0, 2, 'B', 14, 3, KEYBOARD_HASH_2);
        logger6.set(1, 1, 2, 'B', 13, 3, KEYBOARD_HASH_2);
        logger6.set(2, 2, 2, 'B', 11, 3, KEYBOARD_HASH_2);
        logger6.set(3, 3, 2, 'B', 15, 3, KEYBOARD_HASH_2);
        // The following entry is too far off and should be detected and removed by the learner
        logger6.set(4, 4, 2, 'B', 25, 3, KEYBOARD_HASH_2);
        logger6.save(mContext);

        LayoutLearner ll = new LayoutLearner(mContext, 0, 0);
        ll.learn();

        KeyStats newKs = KeyStats.load(mContext, KEYBOARD_HASH_1);
        Hitboxes newHitb = Hitboxes.load(mContext, KEYBOARD_HASH_1);
        KeyStats newKs2 = KeyStats.load(mContext, KEYBOARD_HASH_2);
        Hitboxes newHitb2 = Hitboxes.load(mContext, KEYBOARD_HASH_2);

        // assert that no duplicate data was created
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
        assertEquals(2, hitboxes.length);
        assertEquals(2, keyStats.length);

        // assert that KeyStats were updated correctly
        assertEquals(36, newKs.findCode(1).getN());
        assertEquals(39, newKs.findCode(2).getN());
        assertEquals(36, newKs.findCode(3).getN());
        assertEquals(7, newKs2.findCode(2).getN());
        assertEquals(6, newKs2.findCode(3).getN());
        assertEquals(null, newKs2.findCode(4));

        // assert that keyboard layout was changed correctly
        assertTrue(newHitb.findCode(1).getBottomRight().getX() >
                hitb.findCode(1).getBottomRight().getX());
        assertTrue(newHitb.findCode(2).getBottomLeft().getX() >
                hitb.findCode(2).getBottomLeft().getX());
        assertTrue(newHitb.findCode(2).getBottomRight().getX() >
                hitb.findCode(2).getBottomRight().getX());
        assertTrue(newHitb.findCode(3).getBottomLeft().getX() >
                hitb.findCode(3).getBottomLeft().getX());
        assertTrue(newHitb2.findCode(2).getBottomRight().getX() >
                hitb2.findCode(2).getBottomRight().getX());
        // 4 has no logged keystrokes, thus its border must not be adapted
        assertEquals(hitb2.findCode(4).getBottomLeft().getX(),
                newHitb2.findCode(4).getBottomLeft().getX());

    }
}
