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

import java.util.LinkedList;
import java.util.ListIterator;

// this class is a helper for the layoutLearner. It will store KeyStats objects and automatically
// load new ones when requested
// TODO: Make an interface to combine this with HitboxesSwitcher
// TODO: Refactor so that this class always loads all possible KeyStats?
public class KeyStatsSwitcher extends LinkedList<KeyStats> {
    private final Context mContext;

    public KeyStatsSwitcher(Context cn){
        super();
        mContext = cn;
    }

    // finds a KeyStats stored in this list by its keyboardHash ID
    private KeyStats findKeyboardHash(int keyboardHash){
        ListIterator<KeyStats> it = listIterator();
        while(it.hasNext()){
            KeyStats ks = it.next();
            if(ks.getKeyboardHash() == keyboardHash){
                return ks;
            }
        }
        return null;
    }

    // finds a KeyStats object by keyboardHash ID and if necessary loads or creates a new one
    public KeyStats getKeyStats(int keyboardHash){
        KeyStats ks = findKeyboardHash(keyboardHash);
        if(ks == null){
            ks = KeyStats.load(mContext, keyboardHash);
            this.add(ks);
        }
        return ks;
    }

    // explicitely creates a new KeyStats object regardless of whether some already exists in a file
    public KeyStats createKeyStats(int keyboardHash){
        KeyStats ks = new KeyStats(keyboardHash);
        this.add(ks);
        return ks;
    }

    public boolean hasKeyStats(int keyboardHash){
        KeyStats ks = findKeyboardHash(keyboardHash);
        return ks != null;
    }

    public void merge(KeyStatsSwitcher other){
        // merge those KeyStats that are in both objects (ignore ones that are only in other)
        ListIterator<KeyStats> it = this.listIterator();
        while(it.hasNext()){
            KeyStats ks = it.next();
            ks.merge(other.getKeyStats(ks.getKeyboardHash()));
        }
    }

    public void save(){
        ListIterator<KeyStats> it = this.listIterator();
        while(it.hasNext()){
            KeyStats ks = it.next();
            ks.save(mContext);
        }
    }
}
