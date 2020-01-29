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

// this class is a helper for the layoutLearner. It will store Hitboxes objects and automatically
// load new ones when requested
// TODO: Make an interface to combine this with KeyStatsSwitcher
// TODO: Replace redundant list iterators by forEach
public class HitboxesSwitcher extends LinkedList<Hitboxes> {
    private final Context mContext;

    public HitboxesSwitcher(Context cn){
        super();
        mContext = cn;
    }

    // finds a Hitboxes stored in this list by its keyboardHash ID
    private Hitboxes findHitboxes(int keyboardHash){
        ListIterator<Hitboxes> it = listIterator();
        while(it.hasNext()){
            Hitboxes hitb = it.next();
            if(hitb.getKeyboardHash() == keyboardHash){
                return hitb;
            }
        }
        return null;
    }

    // finds a Hitboxes object by keyboardHash ID and if necessary loads or creates a new one
    public Hitboxes getHitboxes(int keyboardHash){
        Hitboxes hitb = findHitboxes(keyboardHash);
        if(hitb == null){
            hitb = Hitboxes.load(mContext, keyboardHash);
            add(hitb);
        }
        return(hitb);
    }

    public void save(){
        ListIterator<Hitboxes> it = listIterator();
        while(it.hasNext()){
            Hitboxes hb = it.next();
            hb.save(mContext);
        }
    }
}
