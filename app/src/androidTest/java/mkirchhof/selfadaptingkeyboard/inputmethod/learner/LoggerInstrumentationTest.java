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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LoggerInstrumentationTest {
    final private static int KEYBOARD_HASH_1 = 1;
    Context cn;

    @Before
    public void setup(){
        cn = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void saveLoadTest(){
        Logger logger = new Logger();

        logger.set(0, 0, 1, 'a', 100, 200, KEYBOARD_HASH_1);
        logger.set(1, 1, 2, 'b', 150, 230, KEYBOARD_HASH_1);
        logger.set(2, 2, 3, 'c', 200, 187, KEYBOARD_HASH_1);
        logger.set(3, 3, 4, 'd', 100, 200, KEYBOARD_HASH_1);

        String savePath = logger.save(cn);
        Logger logger2 = Logger.load(cn, savePath);

        assertEquals(logger.toArray(), logger2.toArray());
    }
}
