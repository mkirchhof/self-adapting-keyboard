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

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class LayoutLearnerTest {
    final private static int KEYBOARD_HASH_1 = 1;

    @Test
    public void computeBorderTest(){
        int border = LayoutLearner.computeBorder(0, 5, 6, 10, 3, 6, 4, 3, 0.75, 0.25);
        assertEquals(5, border);
        int border2 = LayoutLearner.computeBorder(0, 5, 6, 10, 3, 5, 4, 4, 0.5, 0.5);
        assertEquals(4, border2);
        int border3 = LayoutLearner.computeBorder(0, 5, 6, 10, 3, 7, 1, 5, 0.9, 0.1);
        assertEquals(5, border3);
        int border4 = LayoutLearner.computeBorder(0, 5, 6, 10, 4, 3, 5, 1, 0.1, 0.9);
        assertEquals(5, border4);
    }

    @Test
    public void computeHitboxesTest(){
        // assign a sample ks and hb for a keyboard with two rows, where the first row has 3 and
        // the second has 2 Keys. Also, the first Key in the first Row is a special key
        Hitboxes hb = new Hitboxes(KEYBOARD_HASH_1);
        hb.add(new Hitbox(1, new Point(1, 1), new Point(7, 1),
                new Point(1, 5), new Point(7, 5)));
        hb.add(new Hitbox(2, new Point(8, 1), new Point(15, 1),
                new Point(8, 5), new Point(15, 5)));
        hb.add(new Hitbox(3, new Point(16, 1), new Point(20, 1),
                new Point(16, 5), new Point(20, 5)));
        hb.add(new Hitbox(4, new Point(1, 6), new Point(14, 6),
                new Point(1, 10), new Point(14, 10)));
        hb.add(new Hitbox(5, new Point(15, 6), new Point(20, 6),
                new Point(15, 10), new Point(20, 10)));

        KeyStats ks = new KeyStats(KEYBOARD_HASH_1);
        ks.add(new KeyStat(2, 10, 2, 4, 3, 0, 6));
        ks.add(new KeyStat(3, 17, 3, 9, 3, 0, 6));
        ks.add(new KeyStat(4, 4, 7.5, 4, 16, 0, 16));
        ks.add(new KeyStat(5, 15, 7.5, 16, 16, 0, 16));

        LayoutLearner.computeHitboxes(ks, hb, 0, 0);
        Iterator<Hitbox> hitIt = hb.iterator();
        while(hitIt.hasNext()){
            Hitbox hit = hitIt.next();

            // keys should not exceed the keyboard
            assertTrue(hit.getTopLeft().getX() >= 0);
            assertTrue(hit.getTopLeft().getY() >= 0);
            assertTrue(hit.getBottomRight().getX() <= 20);
            assertTrue(hit.getBottomRight().getY() <= 20);

            // keys should stay rectangles
            assertTrue(hit.getTopLeft().getX() == hit.getBottomLeft().getX());
            assertTrue(hit.getTopRight().getX() == hit.getBottomRight().getX());
            assertTrue(hit.getTopLeft().getY() == hit.getTopRight().getY());
            assertTrue(hit.getBottomLeft().getY() == hit.getBottomRight().getY());
        }

        // keys in the same row should share the same y bound
        assertTrue(hb.findCode(1).getTopRight().getY() == hb.findCode(2).getTopRight().getY());
        assertTrue(hb.findCode(1).getBottomRight().getY() == hb.findCode(2).getBottomRight().getY());
        assertTrue(hb.findCode(1).getTopRight().getY() == hb.findCode(3).getTopRight().getY());
        assertTrue(hb.findCode(1).getBottomRight().getY() == hb.findCode(3).getBottomRight().getY());
        assertTrue(hb.findCode(4).getTopRight().getY() == hb.findCode(5).getTopRight().getY());
        assertTrue(hb.findCode(4).getBottomRight().getY() == hb.findCode(5).getBottomRight().getY());

        // adjacent keys should not overlap horizontally
        assertTrue(hb.findCode(1).getTopRight().getX() <= hb.findCode(2).getTopLeft().getX());
        assertTrue(hb.findCode(2).getTopRight().getX() <= hb.findCode(3).getTopLeft().getX());
        assertTrue(hb.findCode(4).getTopRight().getX() <= hb.findCode(5).getTopLeft().getX());

        // keys beneath each other should not overlap vertically
        assertTrue(hb.findCode(1).getBottomRight().getY() <= hb.findCode(4).getBottomRight().getY());

        // y Border should have shifted
        assertEquals(4, hb.findCode(1).getBottomRight().getY());
    }

    // TODO: Make tests for more edge cases (empty rows, non-clicked rows, etc)
}
