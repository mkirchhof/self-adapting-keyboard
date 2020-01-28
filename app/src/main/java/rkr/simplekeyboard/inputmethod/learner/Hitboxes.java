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
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.keyboard.Keyboard;

//stores the hitboxes of all known keys of a keyboard
public class Hitboxes extends LinkedList<Hitbox> implements Serializable {
    final String TAG = Hitboxes.class.getSimpleName();
    private static final long serialVersionUID = 2L;
    private final int mKeyboardHash;

    public Hitboxes(int keyboardHash){
        super();
        mKeyboardHash = keyboardHash;
    }

    public int getKeyboardHash(){ return mKeyboardHash; }

    public Hitbox findCode(int code){
        Iterator<Hitbox> it = iterator();
        while(it.hasNext()){
            Hitbox cur = it.next();
            if(cur.getCode() == code){
                return cur;
            }
        }

        return null;
    }

    // finds the first hitbox of a rows that's top coordinate is yTop
    public Hitbox findRowStart(int yTop){
        Iterator<Hitbox> it = iterator();
        while(it.hasNext()){
            Hitbox cur = it.next();
            if(cur.getTopLeft().getY() == yTop){
                return cur;
            }
        }
        return null;
    }

    // saves this object to an internal file
    public void save(Context context){
        try{
            File folder = new File(context.getFilesDir(),"Hitboxes");
            if(!folder.exists()){
                folder.mkdir();
            }
            File file = new File(folder, "Hitboxes" + mKeyboardHash + ".ser");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();
        } catch(Exception e){
            Log.e(TAG, "Failed to save Hitboxes: " + e.toString());
        }
    }

    // loads Hitboxes object from an internal file (/Hitboxes/Hitboxes<HASH>.ser)
    public static Hitboxes load(Context context, int keyboardHash){
        Hitboxes hit = new Hitboxes(keyboardHash);
        try{
            File folder = new File(context.getFilesDir(),"Hitboxes");
            File file = new File(folder, "Hitboxes" + keyboardHash + ".ser");
            if(file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);

                hit = (Hitboxes) ois.readObject();

                ois.close();
                fis.close();
            } else {
                Log.d("Hitboxes.load", "Could not find Hitboxes for keyboard " + keyboardHash);
            }
        } catch(Exception e){
            Log.e("Hitboxes.load", "Failed to load Hitboxes for hash " + keyboardHash +
                    ": " + e.toString());
        }
        return hit;
    }

    // returns whether there is a saved hitbox instance
    public static boolean savedHitboxesExists(Context context, int layoutHashCode){
        File folder = new File(context.getFilesDir(),"Hitboxes");
        File file = new File(folder, "Hitboxes" + layoutHashCode + ".ser");
        if(file.exists()){
            return true;
        } else {
            return false;
        }
    }

    // returns last change date of the hitboxes file or null if no file exists
    public static Long lastUpdateHitboxes(Context context, int layoutHashCode){
        File folder = new File(context.getFilesDir(),"Hitboxes");
        File file = new File(folder, "Hitboxes" + layoutHashCode + ".ser");
        if(file.exists()){
            return file.lastModified();
        } else {
            return null;
        }
    }

    // extracts information on a keyboard's keys' positions
    //TODO: Be careful with the paddings
    //TODO: Maybe make this a class generator?
    public static Hitboxes toHitboxes(Keyboard kb){
        Hitboxes hitb = new Hitboxes(kb.mId.layoutHashCode());
        List<Key> keys = kb.getSortedKeys();
        Iterator<Key> keyIt = keys.iterator();
        Key cur;
        while(keyIt.hasNext()){
            cur = keyIt.next();

            Point curTopLeft = new Point(cur.getXInclGap(), cur.getYInclGap());
            Point curTopRight = new Point(cur.getXInclGap() + cur.getWidthInclGap(), cur.getYInclGap());
            Point curBotLeft = new Point(cur.getXInclGap(), cur.getYInclGap() + cur.getHeightInclGap());
            Point curBotRight = new Point(cur.getXInclGap() + cur.getWidthInclGap(), cur.getYInclGap() + cur.getHeightInclGap());

            Hitbox curHitbox = new Hitbox(cur.getCode(), curTopLeft, curTopRight, curBotLeft, curBotRight);
            Log.d("Hitboxes.toHitboxes", curHitbox.toString());
            hitb.add(curHitbox);
        }

        return hitb;
    }

    // Don't merge Hitboxes. Differentiate between different Hitboxes objects for each keyboard instead
    /*public void merge(Hitboxes hitb){
        Iterator<Hitbox> it = hitb.iterator();
        while(it.hasNext()){
            Hitbox cur = it.next();

            Hitbox foundHitbox = this.findCode(cur.getCode());
            if(foundHitbox == null){
                this.add(cur);
            }
        }
    }
     */
}
