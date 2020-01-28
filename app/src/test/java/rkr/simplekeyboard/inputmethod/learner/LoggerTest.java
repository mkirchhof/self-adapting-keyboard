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
import rkr.simplekeyboard.inputmethod.learner.Logger;

import static org.junit.Assert.*;


public class LoggerTest {
    final private static int KEYBOARD_HASH_1 = 1;
    final private static int KEYBOARD_HASH_2 = 2;

    @Test
    public void initNewLogger(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ', 0, 0, KEYBOARD_HASH_1);
        int[][] expected = {{0, 0, 0, 0, 0, KEYBOARD_HASH_1}};
        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void addAtEnd(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ', 0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ', 0, 0, KEYBOARD_HASH_2);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_2);
        l.set(3, 3, 3, ' ',0, 0, KEYBOARD_HASH_1);

        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 1, 0, 0, 0, KEYBOARD_HASH_2},
                {2, 2, 0, 0, 0, KEYBOARD_HASH_2},
                {3, 3, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void addWithinText(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 3, ' ',0, 0, KEYBOARD_HASH_1);

        // should add the 3 as second object and keep all other textPos in order
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 3, 0, 0, 0, KEYBOARD_HASH_1},
                {2, 1, 0, 0, 0, KEYBOARD_HASH_1},
                {3, 2, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void deleteAtBeginning(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.delete(0, 0);

        //should mark first object as "isDeleted"
        int[][] expected = {
                {0, 0, 0, 0, 1, KEYBOARD_HASH_1},
                {0, 1, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 2, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void deleteAtEnd(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.delete(2, 2);

        // should mark last object as "isDeleted"
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 1, 0, 0, 0, KEYBOARD_HASH_1},
                {2, 2, 0, 0, 1, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void deleteWithinText(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.delete(1, 1);

        // should mark second object as "isDeleted"
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 1, 0, 0, 1, KEYBOARD_HASH_1},
                {1, 2, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void replaceWithinText(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.delete(1, 1);
        l.set(1, 1, 3, ' ',3, 3, KEYBOARD_HASH_1);

        // should overwrite the second object's code but keep its original posX and posY
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 3, 1, 1, 0, KEYBOARD_HASH_1},
                {2, 2, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void replaceSeveral(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2,' ', 2, 2, KEYBOARD_HASH_1);
        l.set(3, 3, 3, ' ',3, 3, KEYBOARD_HASH_1);
        l.set(1, 3, 4, ' ',4, 4, KEYBOARD_HASH_1);

        // should mark second and third object as "isDeleted"
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 4, 1, 1, 0, KEYBOARD_HASH_1},
                {2, 2, 2, 2, 1, KEYBOARD_HASH_1},
                {2, 3, 3, 3, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void deleteSeveral(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(3, 3, 3, ' ',0, 0, KEYBOARD_HASH_1);
        l.delete(1, 3);

        // should mark second and third object as "isDeleted"
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 1, 0, 0, 1, KEYBOARD_HASH_1},
                {1, 2, 0, 0, 1, KEYBOARD_HASH_1},
                {1, 3, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void insert(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1,' ', 0, 0, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(3, 3, 3, ' ',0, 0, KEYBOARD_HASH_1);
        l.insert(1, 1, 3);

        // should insert three 0 codes after first object
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0},
                {3, 0, 0, 0, 0, 0},
                {4, 1, 0, 0, 0, KEYBOARD_HASH_1},
                {5, 2, 0, 0, 0, KEYBOARD_HASH_1},
                {6, 3, 0, 0, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void replace(){
        Logger l = new Logger();
        l.set(0, 0, 0, ' ',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, ' ',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2, ' ',2, 2, KEYBOARD_HASH_1);
        l.set(3, 3, 3, ' ',3, 3, KEYBOARD_HASH_1);
        int[] newCodes = {1, 2, 3};
        l.insert(1, 3, 3);

        // should replace second and third object with three 0-code objects
        int[][] expected = {
                {0, 0, 0, 0, 0, KEYBOARD_HASH_1},
                {1, 0, 1, 1, 0, KEYBOARD_HASH_1},
                {2, 0, 2, 2, 0, KEYBOARD_HASH_1},
                {3, 0, 0, 0, 0, 0},
                {4, 3, 3, 3, 0, KEYBOARD_HASH_1}};

        assertArrayEquals(expected, l.toArray());
    }

    @Test
    public void getText(){
        Logger l = new Logger();
        l.set(0, 0, 0, 'a',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, 'b',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2, 'c',2, 2, KEYBOARD_HASH_1);
        l.set(3, 3, 3, '!',3, 3, KEYBOARD_HASH_1);

        assertEquals("abc!", l.getText());
    }

    @Test
    public void matchesTextTrue(){
        Logger l = new Logger();
        l.set(0, 0, 0, 'a',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, 'b',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2, 'c',2, 2, KEYBOARD_HASH_1);
        l.set(3, 3, 3, '!',3, 3, KEYBOARD_HASH_1);

        boolean isEqualText = l.matchesText("abc!");

        assertEquals(true, isEqualText);
    }

    @Test
    public void matchesTextFalse(){
        Logger l = new Logger();
        l.set(0, 0, 0, 'a',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, 'b',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2, 'c',2, 2, KEYBOARD_HASH_1);
        l.set(3, 3, 3, '!',3, 3, KEYBOARD_HASH_1);

        boolean isEqualText = l.matchesText("adc!");

        assertEquals(false, isEqualText);
    }

    @Test
    public void matchesTextUnknowns(){
        Logger l = new Logger();
        l.set(0, 0, 0, 'a',0, 0, KEYBOARD_HASH_1);
        l.set(1, 1, 1, 'b',1, 1, KEYBOARD_HASH_1);
        l.set(2, 2, 2, 'c',2, 2, KEYBOARD_HASH_1);
        l.set(3, 3, 3, '!',3, 3, KEYBOARD_HASH_1);
        l.insert(2, 2, 3);

        boolean isEqualText = l.matchesText("abXYZc!");

        assertEquals(true, isEqualText);
    }

}