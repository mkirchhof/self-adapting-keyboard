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

import org.junit.Test;

import static org.junit.Assert.*;

public class KeyStatsTest {
    final private static int KEYBOARD_HASH_1 = 1;
    final private static int KEYBOARD_HASH_2 = 2;

    @Test
    public void onlineMeanAndVarianceComputation(){
        KeyStats ks = new KeyStats(KEYBOARD_HASH_1);

        ks.add(1, 231, 738);
        ks.add(1, 473, 282);
        ks.add(1, 272, 112);
        ks.add(1, 282, 638);
        ks.add(1, 228, 283);
        ks.add(1, 385, 292);

        KeyStat key = ks.findCode(1);
        assertEquals(312, Math.round(key.getMeanX()));
        assertEquals(391, Math.round(key.getMeanY()));
        assertEquals(9473, Math.round(key.getVarX()));
        assertEquals(58521, Math.round(key.getVarY()));
        assertEquals(-8012, Math.round(key.getCovXY()));
        assertEquals(6, key.getN());
    }

    @Test
    public void merge(){
        KeyStats ks1 = new KeyStats(KEYBOARD_HASH_1);
        KeyStats ks2 = new KeyStats(KEYBOARD_HASH_2);

        ks1.add(1, 231, 738);
        ks1.add(1, 473, 282);
        ks2.add(1, 272, 112);
        ks2.add(1, 282, 638);
        ks2.add(1, 228, 283);
        ks2.add(1, 385, 292);

        ks2.merge(ks1);
        KeyStat key = ks2.findCode(1);
        assertEquals(312, Math.round(key.getMeanX()));
        assertEquals(391, Math.round(key.getMeanY()));
        assertEquals(9473, Math.round(key.getVarX()));
        assertEquals(58521, Math.round(key.getVarY()));
        assertEquals(-8012, Math.round(key.getCovXY()));
        assertEquals(6, key.getN());
    }

    @Test
    public void reduceDataTest(){
        KeyStats ks1 = new KeyStats(KEYBOARD_HASH_1);
        ks1.add(new KeyStat(1, 10, 20, 40, 10, 50, 20));
        ks1.add(new KeyStat(2, 10, 20, 40, 10, 50, 40));

        ks1.reduceData(0.5);

        assertEquals(10, ks1.findCode(1).getN());
        assertEquals(20, ks1.findCode(2).getN());
    }

    @Test
    public void reduceData0Test(){
        KeyStats ks1 = new KeyStats(KEYBOARD_HASH_1);
        ks1.add(new KeyStat(1, 10, 20, 40, 10, 50, 20));
        ks1.add(new KeyStat(2, 10, 20, 40, 10, 50, 40));

        ks1.reduceData(0.0);

        assertEquals(0, ks1.findCode(1).getN());
        assertEquals(0, ks1.findCode(2).getN());
    }

    @Test
    public void reduceData1Test(){
        KeyStats ks1 = new KeyStats(KEYBOARD_HASH_1);
        ks1.add(new KeyStat(1, 10, 20, 40, 10, 50, 20));
        ks1.add(new KeyStat(2, 10, 20, 40, 10, 50, 40));

        ks1.reduceData(1.0);

        assertEquals(20, ks1.findCode(1).getN());
        assertEquals(40, ks1.findCode(2).getN());
    }

    @Test
    public void reduceDataUnexpectedInputTest1(){
        KeyStats ks1 = new KeyStats(KEYBOARD_HASH_1);
        ks1.add(new KeyStat(1, 10, 20, 40, 10, 50, 20));
        ks1.add(new KeyStat(2, 10, 20, 40, 10, 50, 40));

        ks1.reduceData(-0.8);

        assertEquals(20, ks1.findCode(1).getN());
        assertEquals(40, ks1.findCode(2).getN());
    }
}
