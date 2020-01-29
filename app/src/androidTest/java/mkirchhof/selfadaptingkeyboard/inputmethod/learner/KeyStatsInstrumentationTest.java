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
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
//import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class KeyStatsInstrumentationTest {
    final private static int KEYBOARD_HASH_1 = 1;

    Context cn;

    @Before
    public void setup(){
        cn = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void saveLoadTest(){
        KeyStats ks = new KeyStats(KEYBOARD_HASH_1);

        ks.add(1, 231, 738);
        ks.add(1, 473, 282);
        ks.add(1, 272, 112);
        ks.add(1, 282, 638);
        ks.add(2, 228, 283);
        ks.add(2, 385, 292);

        ks.save(cn);
        KeyStats ks2 = KeyStats.load(cn, KEYBOARD_HASH_1);

        //assertEquals(ks, ks2);
        // Check the single things instead of the whole objects to handle double inaccuracy
        assertEquals(ks.size(), ks2.size());
        Iterator<KeyStat> it1 = ks.iterator();
        Iterator<KeyStat> it2 = ks2.iterator();
        KeyStat cur1;
        KeyStat cur2;
        while(it1.hasNext() | it2.hasNext()){
            cur1 = it1.next();
            cur2 = it2.next();
            assertEquals(cur1.getCode(), cur2.getCode());
            assertEquals(cur1.getN(), cur2.getN());
            assertEquals(cur1.getMeanX(), cur2.getMeanX(), 10^-4);
            assertEquals(cur1.getMeanY(), cur2.getMeanY(), 10^-4);
            assertEquals(cur1.getCovXY(), cur2.getCovXY(), 10^-4);
            assertEquals(cur1.getVarX(), cur2.getVarX(), 10^-4);
            assertEquals(cur1.getVarY(), cur2.getVarY(), 10^-4);
        }
    }
}